package ch.scorpion.jabbah.base

import ch.scorpion.jabbah.base.StringUtils.countChar
import ch.scorpion.jabbah.base.StringUtils.endWithPeriod
import ch.scorpion.jabbah.base.StringUtils.formatLong
import ch.scorpion.jabbah.base.StringUtils.fromList
import ch.scorpion.jabbah.base.StringUtils.toList
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Unit tests for [StringUtils].
 */
class StringUtilsTest {

    @Test
    fun shouldCountChars() {
        assertEquals(3, countChar("This is a test", ' '))
        assertEquals(0, countChar("ThisIsATest", ' '))
    }

	@Test
	fun shouldAddPeriod() {
		assertEquals("Test.", endWithPeriod("Test"))
	}

	@Test
	fun shouldNotAddPeriodIfAlreadyExisting() {
		assertEquals("Test.", endWithPeriod("Test."))
	}

	@Test
	fun shouldFormatLong() {
		assertEquals("123", formatLong(123L))
		assertEquals("123_456_789", formatLong(123456789L))
	}

	@Test
	fun shouldCreateStringList() {
		assertEquals("", fromList(listOf("")))
		assertEquals("A", fromList(listOf("A")))
		assertEquals("A,B,C", fromList(listOf("A", "B", "C")))
		assertEquals("A,B\\,Bla,C", fromList(listOf("A", "B,Bla", "C")))
		assertEquals("A,\\\\,C", fromList(listOf("A", "\\", "C")))
	}

	@Test
	fun shouldParseListString() {
		assertEquals(listOf(), toList(""))
		assertEquals(listOf("A"), toList("A"))
		assertEquals(listOf("A", "B", "C"), toList("A,B,C"))
		assertEquals(listOf("A", "B,Bla", "C"), toList("A,B\\,Bla,C"))
		assertEquals(listOf("A", "\\", "C"), toList("A,\\\\,C"))
	}

	@Test
	fun shouldListAndUnList() {
		assertList(listOf("A", "B", "C"))
		assertList(listOf("A,B\\,Bla,C"))
	}

	@Test
	fun shouldOrNull() {
		assertNull(StringUtils.orNull(""))
		assertNull(StringUtils.orNull(" "))
		assertNull(StringUtils.orNull(null))
		assertEquals("test", StringUtils.orNull("test"))
	}

	private fun assertList(list: List<String>) {
		assertEquals(list, toList(fromList(list)))
	}
}