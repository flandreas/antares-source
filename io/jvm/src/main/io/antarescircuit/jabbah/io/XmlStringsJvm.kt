package io.antarescircuit.jabbah.io

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets

actual object XmlStrings {

	actual fun write(block: (XmlWriter) -> Unit): String {
		val out = ByteArrayOutputStream()
		block(ElectricXmlWriter(out))
		return String(out.toByteArray(), StandardCharsets.UTF_8)
	}

	actual fun reader(xml: String): XmlReader =
		ElectricXmlReader(ByteArrayInputStream(xml.toByteArray(StandardCharsets.UTF_8)))
}
