package ch.scorpion.jabbah.base

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Unit tests for [StringUtils].
 */
class StringUtilsTest {

    @Test
    fun shouldCountChars() {
        assertEquals(3, StringUtils.countChar("This is a test", ' '))
        assertEquals(0, StringUtils.countChar("ThisIsATest", ' '))
    }

    @Test
    fun shouldReplaceSingleNegation() {
        assertEquals("Q" + StringUtils.OVERLINE, StringUtils.replaceNegation("!Q"))
    }

	@Test
	fun shouldAddPeriod() {
		assertEquals("Test.", StringUtils.endWithPeriod("Test"))
	}

	@Test
	fun shouldNotAddPeriodIfAlreadyExisting() {
		assertEquals("Test.", StringUtils.endWithPeriod("Test."))
	}

	@Test
	fun shouldFormatLong() {
		assertEquals("123", StringUtils.formatLong(123L))
		assertEquals("123_456_789", StringUtils.formatLong(123456789L))
	}

	// Not yet supported.
    /*
    fun shouldReplaceBlockNegation() {
        assertEquals(StringUtils.replaceNegation("!(AB)"), "A\u0305B\u0305"))
    }
    */
}