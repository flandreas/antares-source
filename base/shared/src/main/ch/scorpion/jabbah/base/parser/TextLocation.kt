package ch.scorpion.jabbah.base.parser

/** Identifies a location in the scanned text to identify error locations.*/
data class TextLocation(val pos: Int, val row: Int, val column: Int) {

	companion object {
		val UNDEFINED = TextLocation(0, 0, 0)
	}

	override fun toString(): String = "$row:$column"
}