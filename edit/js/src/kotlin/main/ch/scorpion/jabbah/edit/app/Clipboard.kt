package ch.scorpion.jabbah.edit.app

actual object Clipboard {

	actual fun getStringContents(): String? {
		throw NotImplementedError()
	}

	actual fun setStringContents(contents: String) {
		throw NotImplementedError()
	}
}