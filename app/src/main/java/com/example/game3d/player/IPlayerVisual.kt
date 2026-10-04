package com.example.game3d.player

import com.badlogic.gdx.graphics.g3d.Environment
import com.badlogic.gdx.graphics.g3d.ModelBatch
import com.badlogic.gdx.math.Vector3

interface IPlayerVisual {
    fun updateAnimation(delta: Float, isMoving: Boolean, isRunning: Boolean, walkTimer: Float)
    fun render(modelBatch: ModelBatch, environment: Environment, position: Vector3, yaw: Float)
    fun dispose()
}
