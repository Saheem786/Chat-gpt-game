package com.example.game3d.renderer

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.VertexAttributes.Usage
import com.badlogic.gdx.graphics.g3d.Material
import com.badlogic.gdx.graphics.g3d.Model
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.math.Vector3
import com.example.data.model.AnimalSpecies
import com.example.data.model.CropType

class ModelFactory {

    private val builder = ModelBuilder()

    private val attr = (Usage.Position or Usage.Normal).toLong()

    // Solarpunk Material Palette
    private val matGrass = Material(ColorAttribute.createDiffuse(Color(0.26f, 0.62f, 0.28f, 1f)))
    private val matDarkGrass = Material(ColorAttribute.createDiffuse(Color(0.20f, 0.50f, 0.22f, 1f)))
    private val matStonePath = Material(ColorAttribute.createDiffuse(Color(0.68f, 0.65f, 0.58f, 1f)))
    private val matWater = Material(ColorAttribute.createDiffuse(Color(0.18f, 0.55f, 0.78f, 0.85f)))
    private val matSoil = Material(ColorAttribute.createDiffuse(Color(0.35f, 0.22f, 0.12f, 1f)))
    private val matWood = Material(ColorAttribute.createDiffuse(Color(0.55f, 0.35f, 0.20f, 1f)))
    private val matDarkWood = Material(ColorAttribute.createDiffuse(Color(0.38f, 0.24f, 0.14f, 1f)))
    private val matSolarBlue = Material(ColorAttribute.createDiffuse(Color(0.12f, 0.32f, 0.65f, 1f)))
    private val matCleanWhite = Material(ColorAttribute.createDiffuse(Color(0.92f, 0.94f, 0.95f, 1f)))
    private val matBarnRed = Material(ColorAttribute.createDiffuse(Color(0.72f, 0.22f, 0.18f, 1f)))
    private val matRoofTerracotta = Material(ColorAttribute.createDiffuse(Color(0.80f, 0.42f, 0.25f, 1f)))
    private val matMetal = Material(ColorAttribute.createDiffuse(Color(0.60f, 0.65f, 0.70f, 1f)))
    private val matGold = Material(ColorAttribute.createDiffuse(Color(0.95f, 0.78f, 0.22f, 1f)))
    private val matLeafGreen = Material(ColorAttribute.createDiffuse(Color(0.18f, 0.70f, 0.25f, 1f)))
    private val matFoliagePine = Material(ColorAttribute.createDiffuse(Color(0.12f, 0.45f, 0.20f, 1f)))

    fun createTerrain(): Model {
        builder.begin()
        // Main Ground Pasture
        builder.part("ground", GL20.GL_TRIANGLES, attr, matGrass)
            .box(70f, 0.2f, 70f)
        return builder.end()
    }

    fun createWaterPond(): Model {
        builder.begin()
        builder.part("pond", GL20.GL_TRIANGLES, attr, matWater)
            .box(10f, 0.15f, 8f)
        return builder.end()
    }

    fun createStonePath(width: Float, length: Float): Model {
        builder.begin()
        builder.part("path", GL20.GL_TRIANGLES, attr, matStonePath)
            .box(width, 0.05f, length)
        return builder.end()
    }

    fun createCropPlotBed(): Model {
        builder.begin()
        // Soil Bed
        builder.part("soil", GL20.GL_TRIANGLES, attr, matSoil)
            .box(3.2f, 0.25f, 3.2f)
        return builder.end()
    }

    fun createCropModel(cropType: CropType, isMature: Boolean): Model {
        builder.begin()
        val cropMat = when (cropType) {
            CropType.WHEAT -> if (isMature) matGold else Material(ColorAttribute.createDiffuse(Color(0.4f, 0.75f, 0.2f, 1f)))
            CropType.CORN -> if (isMature) Material(ColorAttribute.createDiffuse(Color(0.92f, 0.82f, 0.15f, 1f))) else matLeafGreen
            CropType.TOMATO -> if (isMature) Material(ColorAttribute.createDiffuse(Color(0.88f, 0.18f, 0.14f, 1f))) else matLeafGreen
            CropType.CARROT -> Material(ColorAttribute.createDiffuse(Color(0.25f, 0.75f, 0.25f, 1f)))
            CropType.STRAWBERRY -> if (isMature) Material(ColorAttribute.createDiffuse(Color(0.90f, 0.20f, 0.30f, 1f))) else matLeafGreen
            CropType.HERBS -> Material(ColorAttribute.createDiffuse(Color(0.20f, 0.65f, 0.40f, 1f)))
        }

        val height = if (isMature) when (cropType) {
            CropType.CORN -> 1.8f
            CropType.WHEAT -> 1.0f
            CropType.TOMATO -> 1.2f
            else -> 0.7f
        } else 0.4f

        val radius = if (isMature) 0.35f else 0.18f

        builder.part("crop", GL20.GL_TRIANGLES, attr, cropMat)
            .cone(radius * 2f, height, radius * 2f, 6)
        return builder.end()
    }

    fun createAnimalModel(species: AnimalSpecies): Model {
        builder.begin()
        val mainMat = when (species) {
            AnimalSpecies.CHICKEN -> matCleanWhite
            AnimalSpecies.DUCK -> Material(ColorAttribute.createDiffuse(Color(0.2f, 0.5f, 0.3f, 1f)))
            AnimalSpecies.COW -> Material(ColorAttribute.createDiffuse(Color(0.9f, 0.9f, 0.88f, 1f)))
            AnimalSpecies.GOAT -> Material(ColorAttribute.createDiffuse(Color(0.7f, 0.65f, 0.55f, 1f)))
            AnimalSpecies.SHEEP -> Material(ColorAttribute.createDiffuse(Color(0.95f, 0.95f, 0.92f, 1f)))
            AnimalSpecies.PIG -> Material(ColorAttribute.createDiffuse(Color(0.98f, 0.72f, 0.75f, 1f)))
            AnimalSpecies.BEES -> matGold
        }

        when (species) {
            AnimalSpecies.CHICKEN, AnimalSpecies.DUCK -> {
                // Body
                builder.part("body", GL20.GL_TRIANGLES, attr, mainMat)
                    .sphere(0.45f, 0.45f, 0.55f, 8, 8)
                // Head
                builder.part("head", GL20.GL_TRIANGLES, attr, mainMat)
                    .sphere(0.25f, 0.28f, 0.25f, 6, 6)
            }
            AnimalSpecies.COW, AnimalSpecies.GOAT, AnimalSpecies.SHEEP, AnimalSpecies.PIG -> {
                val size = if (species == AnimalSpecies.COW) 1.6f else 1.1f
                // Torso
                builder.part("body", GL20.GL_TRIANGLES, attr, mainMat)
                    .box(size * 0.6f, size * 0.65f, size * 1.1f)
                // Head
                builder.part("head", GL20.GL_TRIANGLES, attr, mainMat)
                    .box(size * 0.4f, size * 0.45f, size * 0.45f)
            }
            AnimalSpecies.BEES -> {
                // Beehive Apiary Box
                builder.part("hive", GL20.GL_TRIANGLES, attr, matWood)
                    .box(0.7f, 0.9f, 0.7f)
                // Solar Cap
                builder.part("cap", GL20.GL_TRIANGLES, attr, matSolarBlue)
                    .cone(1.0f, 0.3f, 1.0f, 6)
            }
        }
        return builder.end()
    }

    fun createPlayerBody(): Model {
        builder.begin()
        // Torso with Solarpunk green shirt
        val matShirt = Material(ColorAttribute.createDiffuse(Color(0.18f, 0.55f, 0.35f, 1f)))
        builder.part("torso", GL20.GL_TRIANGLES, attr, matShirt)
            .box(0.55f, 0.65f, 0.35f)
        return builder.end()
    }

    fun createPlayerHead(): Model {
        builder.begin()
        // Head skin
        val matSkin = Material(ColorAttribute.createDiffuse(Color(0.94f, 0.78f, 0.66f, 1f)))
        builder.part("head", GL20.GL_TRIANGLES, attr, matSkin)
            .sphere(0.35f, 0.38f, 0.35f, 8, 8)
        // Straw Sunhat
        val matHat = Material(ColorAttribute.createDiffuse(Color(0.85f, 0.75f, 0.45f, 1f)))
        builder.part("hat_brim", GL20.GL_TRIANGLES, attr, matHat)
            .cylinder(0.75f, 0.05f, 0.75f, 10)
        builder.part("hat_crown", GL20.GL_TRIANGLES, attr, matHat)
            .cylinder(0.40f, 0.18f, 0.40f, 10)
        return builder.end()
    }

    fun createPlayerLimb(isArm: Boolean): Model {
        builder.begin()
        val mat = if (isArm) {
            Material(ColorAttribute.createDiffuse(Color(0.94f, 0.78f, 0.66f, 1f)))
        } else {
            // Pants
            Material(ColorAttribute.createDiffuse(Color(0.28f, 0.35f, 0.48f, 1f)))
        }
        val width = if (isArm) 0.14f else 0.18f
        val height = if (isArm) 0.55f else 0.65f
        builder.part("limb", GL20.GL_TRIANGLES, attr, mat)
            .box(width, height, width)
        return builder.end()
    }

    fun createFarmHouse(): Model {
        builder.begin()
        // Base structure
        builder.part("walls", GL20.GL_TRIANGLES, attr, matWood)
            .box(6.5f, 3.5f, 5.5f)
        // Solar Shingle Roof
        builder.part("roof", GL20.GL_TRIANGLES, attr, matSolarBlue)
            .cone(8.0f, 2.4f, 6.8f, 4)
        // Stone Chimney
        builder.part("chimney", GL20.GL_TRIANGLES, attr, matStonePath)
            .box(0.8f, 4.5f, 0.8f)
        return builder.end()
    }

    fun createBarn(): Model {
        builder.begin()
        // Barn Main Hall
        builder.part("barn_walls", GL20.GL_TRIANGLES, attr, matBarnRed)
            .box(8.5f, 4.5f, 10.0f)
        // Barn Solar Roof
        builder.part("barn_roof", GL20.GL_TRIANGLES, attr, matCleanWhite)
            .cone(9.8f, 2.8f, 11.2f, 4)
        return builder.end()
    }

    fun createChickenCoop(): Model {
        builder.begin()
        builder.part("coop_body", GL20.GL_TRIANGLES, attr, matDarkWood)
            .box(3.5f, 2.5f, 3.5f)
        builder.part("coop_roof", GL20.GL_TRIANGLES, attr, matRoofTerracotta)
            .cone(4.2f, 1.4f, 4.2f, 4)
        return builder.end()
    }

    fun createWorkshopBuilding(): Model {
        builder.begin()
        // Stone Base & Masonry
        builder.part("workshop_walls", GL20.GL_TRIANGLES, attr, matStonePath)
            .box(6.0f, 3.8f, 5.5f)
        // Solar Workshop Roof
        builder.part("workshop_roof", GL20.GL_TRIANGLES, attr, matSolarBlue)
            .cone(7.2f, 2.0f, 6.5f, 4)
        // Artisan Smokestack
        builder.part("smokestack", GL20.GL_TRIANGLES, attr, matDarkWood)
            .cylinder(0.7f, 5.0f, 0.7f, 8)
        return builder.end()
    }

    fun createEcoShopBuilding(): Model {
        builder.begin()
        // Storefront Timber Walls
        builder.part("shop_walls", GL20.GL_TRIANGLES, attr, matWood)
            .box(5.5f, 3.2f, 5.0f)
        // Green Striped Awning
        builder.part("shop_awning", GL20.GL_TRIANGLES, attr, matLeafGreen)
            .box(6.2f, 0.4f, 3.0f)
        return builder.end()
    }

    fun createMarketDock(): Model {
        builder.begin()
        builder.part("market_deck", GL20.GL_TRIANGLES, attr, matDarkWood)
            .box(6.0f, 0.4f, 6.0f)
        builder.part("market_canopy", GL20.GL_TRIANGLES, attr, matCleanWhite)
            .cone(6.5f, 1.8f, 6.5f, 4)
        return builder.end()
    }

    fun createSolarPanelArray(): Model {
        builder.begin()
        // Frame
        builder.part("frame", GL20.GL_TRIANGLES, attr, matMetal)
            .box(3.2f, 0.2f, 1.8f)
        // Solar Cells
        builder.part("cells", GL20.GL_TRIANGLES, attr, matSolarBlue)
            .box(3.0f, 0.25f, 1.6f)
        return builder.end()
    }

    fun createWindTurbineTower(): Model {
        builder.begin()
        // Tower Mast
        builder.part("mast", GL20.GL_TRIANGLES, attr, matCleanWhite)
            .cylinder(0.45f, 8.5f, 0.45f, 10)
        // Nacelle
        builder.part("nacelle", GL20.GL_TRIANGLES, attr, matCleanWhite)
            .box(0.7f, 0.7f, 1.4f)
        return builder.end()
    }

    fun createWindTurbineBlades(): Model {
        builder.begin()
        // 3 Aerodynamic Rotor Blades
        builder.part("blade1", GL20.GL_TRIANGLES, attr, matCleanWhite)
            .box(0.2f, 3.2f, 0.05f)
        builder.part("hub", GL20.GL_TRIANGLES, attr, matMetal)
            .sphere(0.4f, 0.4f, 0.4f, 8, 8)
        return builder.end()
    }

    fun createRainTower(): Model {
        builder.begin()
        // Stilts
        builder.part("stilts", GL20.GL_TRIANGLES, attr, matDarkWood)
            .box(2.2f, 3.0f, 2.2f)
        // Water Cistern Tank
        builder.part("tank", GL20.GL_TRIANGLES, attr, matMetal)
            .cylinder(2.4f, 3.0f, 2.4f, 12)
        return builder.end()
    }

    fun createComposterDigester(): Model {
        builder.begin()
        builder.part("tank1", GL20.GL_TRIANGLES, attr, matLeafGreen)
            .cylinder(1.4f, 2.0f, 1.4f, 10)
        builder.part("tank2", GL20.GL_TRIANGLES, attr, matLeafGreen)
            .cylinder(1.4f, 2.0f, 1.4f, 10)
        return builder.end()
    }

    fun createGreenhouse(): Model {
        builder.begin()
        builder.part("frame", GL20.GL_TRIANGLES, attr, matCleanWhite)
            .box(5.5f, 3.0f, 6.5f)
        builder.part("glass_roof", GL20.GL_TRIANGLES, attr, matWater)
            .cone(6.0f, 1.8f, 7.0f, 4)
        return builder.end()
    }

    fun createTree(isPine: Boolean): Model {
        builder.begin()
        // Trunk
        builder.part("trunk", GL20.GL_TRIANGLES, attr, matDarkWood)
            .cylinder(0.4f, 2.0f, 0.4f, 6)
        // Foliage
        if (isPine) {
            builder.part("foliage1", GL20.GL_TRIANGLES, attr, matFoliagePine)
                .cone(2.8f, 2.8f, 2.8f, 6)
            builder.part("foliage2", GL20.GL_TRIANGLES, attr, matFoliagePine)
                .cone(2.0f, 2.2f, 2.0f, 6)
        } else {
            builder.part("foliage", GL20.GL_TRIANGLES, attr, matLeafGreen)
                .sphere(2.6f, 2.6f, 2.6f, 8, 8)
        }
        return builder.end()
    }

    fun createFenceSegment(): Model {
        builder.begin()
        builder.part("post1", GL20.GL_TRIANGLES, attr, matWood)
            .box(0.15f, 1.0f, 0.15f)
        builder.part("post2", GL20.GL_TRIANGLES, attr, matWood)
            .box(0.15f, 1.0f, 0.15f)
        builder.part("rail1", GL20.GL_TRIANGLES, attr, matWood)
            .box(2.0f, 0.12f, 0.08f)
        builder.part("rail2", GL20.GL_TRIANGLES, attr, matWood)
            .box(2.0f, 0.12f, 0.08f)
        return builder.end()
    }

    fun createNPC(role: String): Model {
        builder.begin()
        val shirtColor = when (role) {
            "MERCHANT" -> Color(0.85f, 0.65f, 0.20f, 1f)
            "CHEF" -> Color(0.95f, 0.95f, 0.95f, 1f)
            else -> Color(0.35f, 0.55f, 0.75f, 1f)
        }
        val matShirt = Material(ColorAttribute.createDiffuse(shirtColor))
        val matSkin = Material(ColorAttribute.createDiffuse(Color(0.92f, 0.76f, 0.65f, 1f)))
        val matPants = Material(ColorAttribute.createDiffuse(Color(0.30f, 0.30f, 0.35f, 1f)))

        builder.part("torso", GL20.GL_TRIANGLES, attr, matShirt)
            .box(0.50f, 0.60f, 0.30f)
        builder.part("head", GL20.GL_TRIANGLES, attr, matSkin)
            .sphere(0.32f, 0.35f, 0.32f, 6, 6)
        builder.part("legs", GL20.GL_TRIANGLES, attr, matPants)
            .box(0.40f, 0.60f, 0.25f)
        return builder.end()
    }
}
