package ch.scorpion.jabbah.base

import ch.scorpion.jabbah.base.module.BaseModuleJvm
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [Properties].
 */
class PropertiesTest {

    @Before
    fun setup() {
        BaseModuleJvm.require()
    }

    @Test
    fun shouldGetPredefined() {
        val properties = Properties()
        properties.set("name", "ABC")
        assertThat(properties.getString("name"), `is`("ABC"))
    }
}