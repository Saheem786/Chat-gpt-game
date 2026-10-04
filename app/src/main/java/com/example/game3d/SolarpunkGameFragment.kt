package com.example.game3d

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration
import com.badlogic.gdx.backends.android.AndroidFragmentApplication

class SolarpunkGameFragment : AndroidFragmentApplication() {

    var game: SolarpunkGame? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val activeGame = game ?: SolarpunkGame()
        val config = AndroidApplicationConfiguration().apply {
            useImmersiveMode = false
            useGL30 = false
            numSamples = 2
        }
        return initializeForView(activeGame, config)
    }
}
