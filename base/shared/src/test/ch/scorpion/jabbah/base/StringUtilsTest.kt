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

    @Test
    fun shouldReplaceSingleNegation() {
        assertThat(StringUtils.replaceNegation("!Q"), `is`("Q" + StringUtils.OVERLINE))
    }

    // Not yet supported.
    /*
    fun shouldReplaceBlockNegation() {
        assertThat(StringUtils.replaceNegation("!(AB)"), `is`("A\u0305B\u0305"))
    }
    */
}