package ch.scorpion.antares

import ch.scorpion.antares.shaulamock.ShaulaMock
import ch.scorpion.jabbah.base.util.encodeURI

fun main() {
	val path = kotlinx.browser.window.location.pathname
	println("Path: $path")
	when (path) {
		encodeURI("/iframe.html"), encodeURI("/docs/web/iframe.html") -> AntaresIFrame().show()
		encodeURI("/desktop.html") -> AntaresJs().start()
		encodeURI("/shaulaMock.html") -> ShaulaMock().show()
		else -> AntaresPage().show()
	}
}



