package io.antarescircuit.jabbah.io

import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock

class XmlReaderMockBuilder {

    private val xmlReader = mock<XmlReader>()

    fun withAttributeValue(value: String): XmlReaderMockBuilder {
        every { xmlReader.getAttributeValue(any()) } returns value
        return this;
    }

    fun build(): XmlReader = xmlReader
}