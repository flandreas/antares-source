package ch.scorpion.jabbah.base.ui

expect object Clipboard {

	fun getStringContents(): String?

	fun setStringContents(contents: String)
}