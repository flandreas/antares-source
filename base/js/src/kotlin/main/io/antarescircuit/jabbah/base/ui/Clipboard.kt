package io.antarescircuit.jabbah.base.ui

actual object Clipboard {

	actual fun getStringContents(): String? {
		throw NotImplementedError()
	}

	actual fun setStringContents(contents: String) {
		throw NotImplementedError()
	}
}