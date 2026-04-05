package io.antarescircuit.jabbah.io

import kotlin.test.Test
import kotlin.test.assertEquals

class StoreXmlReaderTest {

    private val xmlReader = XmlReaderMockBuilder()
    private val storeXmlReader = StoreXmlReader(xmlReader.build())

    @Test
    fun shouldReadSmallDouble() {
        xmlReader.withAttributeValue("1.0E-7")
        assertEquals(1E-7, storeXmlReader.readDouble("test"))
    }
}