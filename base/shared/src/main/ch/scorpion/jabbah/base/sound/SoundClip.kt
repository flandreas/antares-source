package ch.scorpion.jabbah.base.sound

interface SoundClip {
    fun play()
}

expect object SoundClipFactory {
    fun create(path: String): SoundClip
}