package ch.scorpion.jabbah.base

import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for [StringUtils].
 */
class StringUtilsTest {

    @Test
    fun shouldCountChars() {
        assertThat(StringUtils.countChar("This is a test", ' '), `is`(3))
        assertThat(StringUtils.countChar("ThisIsATest", ' '), `is`(0))
    }
}