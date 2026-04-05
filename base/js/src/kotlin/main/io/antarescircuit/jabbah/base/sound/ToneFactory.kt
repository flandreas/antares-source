package io.antarescircuit.jabbah.base.sound

actual object ToneFactory {
	actual fun create(params: ToneParams): Tone {
		throw UnsupportedOperationException("not implemented")
	}
}