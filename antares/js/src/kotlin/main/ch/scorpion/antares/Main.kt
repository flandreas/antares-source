package ch.scorpion.antares

external fun encodeURI(uri: String): String

fun main() {
	val path = kotlinx.browser.window.location.pathname
	println("Path: $path")
	when (path) {
		encodeURI("/iframe.html"), encodeURI("/docs/web/iframe.html") -> AntaresIFrame().show()
		encodeURI("/desktop.html") -> AntaresJs().start()
		//else -> AntaresPage().show()
		else -> TestPage().show()
	}
}



