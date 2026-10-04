package com.example.game3d.collision

import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.math.collision.BoundingBox
import com.badlogic.gdx.math.collision.Ray
import com.example.game3d.world.mapper.FarmWorldPositionMapper

object WorldCollisionSystem {

    const val WORLD_BOUND_MIN_X = -34.0f
    const val WORLD_BOUND_MAX_X = 34.0f
    const val WORLD_BOUND_MIN_Z = -34.0f
    const val WORLD_BOUND_MAX_Z = 34.0f

    // 12 Tree trunk coordinates
    val treeCoords = listOf(
        Triple(-28f, -20f, true), Triple(-25f, -10f, false), Triple(-28f, 5f, true),
        Triple(-28f, 20f, false), Triple(25f, -20f, false), Triple(28f, -5f, true),
        Triple(28f, 10f, false), Triple(25f, 22f, true), Triple(-8f, 28f, true),
        Triple(8f, 28f, false), Triple(-15f, -28f, false), Triple(15f, -28f, true)
    )

    // Complete list of static world obstacle bounding boxes
    val obstacles: List<BoundingBox> = buildList {
        // Farm House
        add(BoundingBox(Vector3(-4.8f, 0f, -17.2f), Vector3(4.8f, 5.8f, -8.8f)))
        // Workshop
        add(BoundingBox(Vector3(-16.2f, 0f, -22.2f), Vector3(-7.8f, 5.8f, -13.8f)))
        // Eco Shop
        add(BoundingBox(Vector3(7.8f, 0f, -22.2f), Vector3(16.2f, 5.8f, -13.8f)))
        // Barn
        add(BoundingBox(Vector3(-24.2f, 0f, -6.2f), Vector3(-12.8f, 6.8f, 7.8f)))
        // Chicken Coop
        add(BoundingBox(Vector3(-21.2f, 0f, 8.8f), Vector3(-14.8f, 4.8f, 15.2f)))
        // Greenhouse
        add(BoundingBox(Vector3(9.8f, 0f, 11.8f), Vector3(18.2f, 4.8f, 20.2f)))
        // Market Stall / Dock
        add(BoundingBox(Vector3(-4.2f, 0f, -28.5f), Vector3(4.2f, 4.8f, -21.5f)))

        // Large Props & Energy Grid
        // Rain Tower / Cistern
        add(BoundingBox(Vector3(-2.0f, 0f, 19.2f), Vector3(2.0f, 6.8f, 22.8f)))
        // Composter Digester
        add(BoundingBox(Vector3(-7.8f, 0f, 19.2f), Vector3(-4.2f, 3.8f, 22.8f)))
        // Solar Array
        add(BoundingBox(Vector3(4.0f, 0f, 19.5f), Vector3(8.0f, 2.8f, 22.5f)))
        // Wind Turbine Tower
        add(BoundingBox(Vector3(11.2f, 0f, 23.2f), Vector3(12.8f, 9.5f, 24.8f)))
        // Duck Pond / Water Hazard
        add(BoundingBox(Vector3(-20.5f, -1f, 14.8f), Vector3(-9.5f, 0.8f, 25.2f)))

        // NPCs
        add(BoundingBox(Vector3(3.4f, 0f, -20.6f), Vector3(4.6f, 2.0f, -19.4f)))
        add(BoundingBox(Vector3(-4.6f, 0f, -20.6f), Vector3(-3.4f, 2.0f, -19.4f)))

        // 12 Tree Trunks
        for ((tx, tz, _) in treeCoords) {
            add(BoundingBox(Vector3(tx - 0.7f, 0f, tz - 0.7f), Vector3(tx + 0.7f, 5.0f, tz + 0.7f)))
        }
    }

    private val tempVec1 = Vector3()
    private val tempVec2 = Vector3()
    private val boxMin = Vector3()
    private val boxMax = Vector3()
    private val rayDirVec = Vector3()
    private val tempRay = Ray(Vector3(), Vector3())
    private val intersectionVec = Vector3()

    // Smooth movement resolution with decoupled axis checks for wall sliding
    fun resolvePlayerMovement(
        currentX: Float,
        currentZ: Float,
        targetX: Float,
        targetZ: Float,
        radius: Float = 0.45f
    ): Pair<Float, Float> {
        // Clamp to world boundaries
        val clampedTargetX = MathUtils.clamp(targetX, WORLD_BOUND_MIN_X + radius, WORLD_BOUND_MAX_X - radius)
        val clampedTargetZ = MathUtils.clamp(targetZ, WORLD_BOUND_MIN_Z + radius, WORLD_BOUND_MAX_Z - radius)

        var finalX = currentX
        var finalZ = currentZ

        // 1. Test X axis movement
        var canMoveX = true
        for (box in obstacles) {
            if (isPointInsideExpandedBox(clampedTargetX, currentZ, box, radius)) {
                canMoveX = false
                break
            }
        }
        if (canMoveX) {
            finalX = clampedTargetX
        }

        // 2. Test Z axis movement
        var canMoveZ = true
        for (box in obstacles) {
            if (isPointInsideExpandedBox(finalX, clampedTargetZ, box, radius)) {
                canMoveZ = false
                break
            }
        }
        if (canMoveZ) {
            finalZ = clampedTargetZ
        }

        return Pair(finalX, finalZ)
    }

    private fun isPointInsideExpandedBox(
        px: Float,
        pz: Float,
        box: BoundingBox,
        expand: Float
    ): Boolean {
        return px >= box.min.x - expand && px <= box.max.x + expand &&
               pz >= box.min.z - expand && pz <= box.max.z + expand
    }

    // Camera obstruction collision solver: Raycasts from focus point to desired camera position
    fun resolveCameraObstruction(
        focusPoint: Vector3,
        desiredCamPos: Vector3,
        minDistance: Float = 1.2f
    ): Vector3 {
        val result = Vector3(desiredCamPos)

        // Prevent camera going below terrain + safety margin
        val terrainY = FarmWorldPositionMapper.getTerrainHeight(desiredCamPos.x, desiredCamPos.z)
        if (result.y < terrainY + 0.6f) {
            result.y = terrainY + 0.6f
        }

        rayDirVec.set(desiredCamPos).sub(focusPoint)
        val maxDist = rayDirVec.len()
        if (maxDist < 0.01f) return result

        rayDirVec.nor()
        tempRay.set(focusPoint, rayDirVec)

        var closestHitDist = maxDist

        for (box in obstacles) {
            // Expand slightly for camera clearance
            val expandedBox = BoundingBox(
                boxMin.set(box.min).sub(0.35f, 0.2f, 0.35f),
                boxMax.set(box.max).add(0.35f, 0.2f, 0.35f)
            )

            if (com.badlogic.gdx.math.Intersector.intersectRayBounds(tempRay, expandedBox, intersectionVec)) {
                val hitDist = focusPoint.dst(intersectionVec)
                if (hitDist > 0.1f && hitDist < closestHitDist) {
                    closestHitDist = hitDist
                }
            }
        }

        if (closestHitDist < maxDist) {
            val safeDistance = Math.max(closestHitDist - 0.25f, minDistance)
            result.set(focusPoint).add(tempVec1.set(rayDirVec).scl(safeDistance))
        }

        return result
    }
}
