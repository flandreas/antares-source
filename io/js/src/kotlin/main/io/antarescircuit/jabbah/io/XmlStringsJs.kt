package io.antarescircuit.jabbah.io

import io.antarescircuit.jabbah.io.module.DomXmlWriter

actual object XmlStrings {

	actual fun write(block: (XmlWriter) -> Unit): String {
		var xml = ""
		block(DomXmlWriter { xml = it })
		return xml
	}

	actual fun reader(xml: String): XmlReader = DomXmlReader(xml)
}
