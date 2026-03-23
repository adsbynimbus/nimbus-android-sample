package com.adsbynimbus.android.sample.demand

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.adsbynimbus.*
import com.adsbynimbus.UnityExtension.Companion.initialize
import com.adsbynimbus.android.sample.BuildConfig
import com.adsbynimbus.android.sample.databinding.LayoutInlineAdBinding
import com.adsbynimbus.android.sample.rendering.ScreenAdLogger
import com.adsbynimbus.android.sample.rendering.showPropertyMissingDialog
import kotlinx.coroutines.launch

/** Initializes the Unity SDK and integrates it with the Nimbus SDK. */
fun Context.initializeUnity(unityGameId: String) {
    initialize(this, gameId = unityGameId, testMode = Nimbus.testMode)
}

class UnityFragment : Fragment() {

    val ads = mutableListOf<Ad>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = LayoutInlineAdBinding.inflate(inflater, container, false).apply {
        when (val item = requireArguments().getString("item")) {
            "Unity Rewarded Video" -> viewLifecycleOwner.lifecycleScope.launch {
                val screenLogger = ScreenAdLogger(identifier = item, logView = logs)

                ads += Nimbus.rewardedAd(item).onEvent {
                    screenLogger.onAdEvent(it)
                }.onError {
                    screenLogger.onError(it)
                }.show(this@UnityFragment)
            }
        }
        if (BuildConfig.UNITY_GAME_ID.isEmpty()) requireContext().showPropertyMissingDialog("sample_unity_game_id")
    }.root
}
