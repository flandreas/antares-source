package io.antarescircuit.jabbah.base.sound

import io.antarescircuit.jabbah.base.PreferencesChangedEvent
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.module.BaseModule

object SoundEffects {

    const val PROP_ENABLE_SOUND_EFFECTS = "jabbah.base.sound.enableSoundEffects"

    var ENABLED = false
        private set

    fun initialize(eventBus: EventBus) {
        eventBus.register(PreferencesChangedEvent:: class) { updateEnableSoundEffects() }
        updateEnableSoundEffects()
    }

    private fun updateEnableSoundEffects() {
        ENABLED = BaseModule.properties.getBoolean(PROP_ENABLE_SOUND_EFFECTS)
    }
}