package ch.scorpion.jabbah.base.sound

import java.util.concurrent.Executors
import java.util.concurrent.ThreadPoolExecutor
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.Clip

private class SoundClipJvm(private val clip: Clip) : SoundClip, Runnable {

    companion object {
        private val executor: ThreadPoolExecutor by lazy { Executors.newCachedThreadPool() as ThreadPoolExecutor }
    }

    override fun play() {
        executor.submit(::run)
    }

    override fun run() {
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