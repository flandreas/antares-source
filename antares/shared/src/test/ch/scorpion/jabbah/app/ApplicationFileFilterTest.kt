package ch.scorpion.jabbah.app

import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.*
import org.junit.Test
import java.io.File

/** Unit tests for [ApplicationFileFilter].*/
class ApplicationFileFilterTest {

    @Test
    fun shouldAcceptExtension() {
        val filter = ApplicationFileFilter("cir", "test")
        assertThat(filter.accept(File("test.cir")), `is`(true))
        assertThat(filter.accept(File("test.bla")), `is`(false))
    }

    @Test
    fun shouldNotAcceptWrongExtension() {
        val filter = ApplicationFileFilter("cir", "test")
        assertThat(filter.accept(File("test.bla")), `is`(false))
    }

}