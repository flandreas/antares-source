package io.antarescircuit.jabbah.base.sound

object SoundClipJs : SoundClip {
    override fun play() {
        // empty, not yet implemented on JS
    }
}

actual object SoundClipFactory {
    actual fun create(path: String): SoundClip = SoundClipJs
}