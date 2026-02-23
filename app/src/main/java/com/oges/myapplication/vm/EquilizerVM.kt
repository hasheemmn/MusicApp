package com.oges.myapplication.vm


import androidx.lifecycle.ViewModel
import com.oges.myapplication.api.AudioRepository
import com.oges.myapplication.utils.EqualizerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class EqualizerVM @Inject constructor(
    private val repository: AudioRepository
) : ViewModel() {

    private val equalizerManager = EqualizerManager()

    fun initEqualizer() {
        val sessionId = repository.getAudioSessionId()
        equalizerManager.init(sessionId)
    }

    fun enableEffects(enabled: Boolean) {
        equalizerManager.setEnabled(enabled)
    }

    fun setBand(band: Int, level: Int) {
        equalizerManager.setBandLevel(
            band.toShort(),
            level.toShort()
        )
    }

    fun setBass(level: Int) {
        equalizerManager.setBass(level.toShort())
    }

    fun setTreble(level: Int) {
        equalizerManager.setVirtualizer(level.toShort())
    }

    override fun onCleared() {
        equalizerManager.release()
        super.onCleared()
    }
}