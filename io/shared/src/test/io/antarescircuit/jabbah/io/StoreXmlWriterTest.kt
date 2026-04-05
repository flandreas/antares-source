package io.antarescircuit.jabbah.io

import io.antarescircuit.jabbah.base.geom.Point2D
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class StoreXmlWriterTest {

	private val xmlWriter = XmlWriterStub()
	private val storeXmlWriter = StoreXmlWriter(xmlWriter)

	@BeforeTest
	fun setup() {
		IOModule.require()
		IOModule.typeMap.register("testStorable", TestStorable::class)
	}

	@Test
	fun shouldRoundDouble() {
		storeXmlWriter.writeDouble("test", 7.123000001)

		assertEquals("7.123", xmlWriter.attributes["test"])
	}

	@Test
	fun shouldWritePreciseDouble() {
		storeXmlWriter.writePreciseDouble("test", 1E-7)
		assertEquals("1.0E-7", xmlWriter.attributes["test"])
	}

	@Test
	fun shouldRoundPoint() {
		storeXmlWriter.writePoint("test", Point2D(8.25, 0.0000001))

		assertEquals("8.25", xmlWriter.attributes["x"])
		assertEquals("0.0", xmlWriter.attributes["y"])
	}

	@Test
	fun shouldRoundPoints() {
		storeXmlWriter.writePoints("test", listOf(Point2D(8.25, 0.0000001)))

		assertEquals("8.25,0.0", xmlWriter.attributes["test"])
	}

	@Test
	fun shouldWriteIntegers() {
		storeXmlWriter.writeIntegers("test", listOf(1, 7, 0))

		assertEquals("1,7,0", xmlWriter.attributes["test"])
	}

	@Test
	fun shouldNotWriteEmptyIntegers() {
		storeXmlWriter.writeIntegers("test", listOf())
		assertFalse(xmlWriter.attributes.containsKey("test"))
	}

	private class XmlWriterStub : XmlWriter {

		val attributes: MutableMap<String, String> = mutableMapOf()

		private var level = 0

		override fun isRoot(): Boolean = level == 0

		override fun addElementAndDescend(name: String) {
			level++
		}

		override fun ascend() {
			level--
		}

		override fun flush() { }

		override fun setAttributeValue(name: String, value: String) {
			attributes[name] = value
		}

		override fun setText(name: String, text: String) {
			throw UnsupportedOperationException("Not yet implemented")
		}
	}
}