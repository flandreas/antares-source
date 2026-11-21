package io.antarescircuit.jabbah.io

/** Creates platform-specific [XmlWriter]s and [XmlReader]s that operate on XML [String]s. */
expect object XmlStrings {

	/** Lets [block] write to an [XmlWriter] and returns the produced XML. */
	fun write(block: (XmlWriter) -> Unit): String

	/** Creates an [XmlReader] that reads from [xml]. */
	fun reader(xml: String): XmlReader
}
