package ch.scorpion.jabbah.base.text

import kotlin.test.Test
import kotlin.test.assertEquals

class PropertiesFileParserTest {

	@Test
	fun shouldParseMultipleProperties() {
		val text = """
			name1 = value1
			name2 = value2 with whitespace
		""".trimIndent()

		val properties = PropertiesFileParser.parse(text)

		assertEquals(2, properties.size)
		assertEquals("value1", properties["name1"])
		assertEquals("value2 with whitespace", properties["name2"])
	}

	@Test
	fun shouldParseValueWithNewline() {
		val text = """
			name = value1\nTest
		""".trimIndent()

		val properties = PropertiesFileParser.parse(text)

		assertEquals(1, properties.size)
		assertEquals("value1\nTest", properties["name"])
	}

	@Test
	fun shouldSkipCommentLines() {
		val text = """
			name1 = value1
			# Comment
			name2 = value2
		""".trimIndent()

		val properties = PropertiesFileParser.parse(text)

		assertEquals(2, properties.size)
		assertEquals("value1", properties["name1"])
		assertEquals("value2", properties["name2"])
	}

	@Test
	fun shouldIgnoreEmptyLines() {
		val text = """
			name1 = value1
			
			name2 = value2
		""".trimIndent()

		val properties = PropertiesFileParser.parse(text)

		assertEquals(2, properties.size)
		assertEquals("value1", properties["name1"])
		assertEquals("value2", properties["name2"])
	}

	@Test
	fun shouldParseValueSpanningMultipleLines() {
		val text = """
			name = line1,\
				line2
		""".trimIndent()

		val properties = PropertiesFileParser.parse(text)

		assertEquals(1, properties.size)
		assertEquals("line1,line2", properties["name"])
	}
}