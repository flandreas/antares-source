package ch.scorpion.jabbah.app

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/** Unit tests for [ApplicationFileFilter].*/
class ApplicationFileFilterTest {

    @Test
    fun shouldAcceptExtension() {
        val filter = ApplicationFileFilter("cir", "test")
        assertTrue(filter.accept(File("test.cir")))
        assertFalse(filter.accept(File("test.bla")))
    }

    @Test
    fun shouldNotAcceptWrongExtension() {
        val filter = ApplicationFileFilter("cir", "test")
	    assertFalse(filter.accept(File("test.bla")))
    }
}