package ch.scorpion.jabbah.base

import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.module.BaseModuleJvm
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [Properties].
 */
class PropertiesTest {

	private val properties = Properties()

    @Before
    fun setup() {
        BaseModuleJvm.require()
    }

    @Test
    fun shouldOverwriteSystemProperty() {
        properties.predefine("name", "ABC")
        properties.set("name", "XYZ")
        assertThat(properties.getString("name"), `is`("XYZ"))
    }

    @Test
    fun shouldReturnDefaultValue() {
        properties.predefine("name", "ABC")
        assertThat(properties.getString("undefined", "default"), `is`("default"))
    }

    @Test
    fun shouldNotUseDefaultWhenValueFound() {
        properties.predefine("name", "ABC")
        assertThat(properties.getString("name", "default"), `is`("ABC"))
    }
}