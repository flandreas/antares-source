package ch.scorpion.antares

import ch.scorpion.antares.shaulamock.ShaulaMock

fun main() {
	val path = kotlinx.browser.window.location.pathname

	console.log("Path: $path")

	if (path.startsWith("/jabbah/desktop")) {
		AntaresJs().start()
	} else if (path.startsWith("/iframe.html") || path.startsWith("/docs/web/iframe.html")) {
		AntaresIFrame().show()
	} else if (path.startsWith("/binaryAddition.html")) {
		BinaryAddition().show()
	} else {
		ShaulaMock().show()
	}
}