package com.ohshootstudio.resibooth.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.ohshootstudio.resibooth.R

class CaptureSoundPlayer(context: Context) {
    private val soundPool: SoundPool
    private val tickSoundId: Int
    private val shutterSoundId: Int

    init {
        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        soundPool = SoundPool.Builder()
            .setMaxStreams(2)
            .setAudioAttributes(attributes)
            .build()
        tickSoundId = soundPool.load(context, R.raw.countdown_tick, 1)
        shutterSoundId = soundPool.load(context, R.raw.shutter_click, 1)
    }

    fun playTick() {
        if (tickSoundId != 0) {
            soundPool.play(tickSoundId, 1f, 1f, 1, 0, 1f)
        }
    }

    fun playShutter() {
        if (shutterSoundId != 0) {
            soundPool.play(shutterSoundId, 1f, 1f, 1, 0, 1f)
        }
    }

    fun release() {
        soundPool.release()
    }
}

