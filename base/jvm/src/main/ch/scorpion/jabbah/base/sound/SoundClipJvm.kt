package ch.scorpion.jabbah.base.sound

import javax.sound.sampled.AudioSystem
import javax.sound.sampled.Clip

private class SoundClipJvm(private val clip: Clip) : SoundClip {

    override fun play() {
        clip.microsecondPosition = 0
        clip.start()
    }
}

actual object SoundClipFactory {

    actual fun create(path: String): SoundClip {
        val audioInputStream = AudioSystem.getAudioInputStream(this::class.java.getResourceAsStream(path))
        val clip = AudioSystem.getClip()
        clip.open(audioInputStream)
        return SoundClipJvm(clip)
    }
}