package ch.scorpion.antares

external fun encodeURI(uri: String): String

fun main() {
	when (kotlinx.browser.window.location.pathname) {
		encodeURI("/"), encodeURI("/index.html") -> AntaresPage().show()
		encodeURI("/iframe.html") -> AntaresIFrame().show()
		encodeURI("/desktop.html") -> AntaresJs().start()
	}
}



