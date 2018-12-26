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

	@Test
    fun shouldGetObject() {
		val properties = Properties()
		properties.set("myObject", SomeObject(1, 2))
		assertThat(properties.get<SomeObject>("myObject"), `is`(SomeObject(1, 2)))
    }

	@Test
	fun shouldCustomizeInt() {
		val properties = Properties()
		properties.set("myInt", 5)
		properties.customize("myInt", 6)
		assertThat(properties.getInt("myInt"), `is`(6))
	}

	@Test
	fun shouldGetCustomizesKeys() {
		val properties = Properties()
		properties.set("myInt", 5)
		properties.customize("myInt", 6)
		assertThat(properties.getCustomizedKeys().next(), `is`("myInt"))
	}

	@Test
	fun shouldLoadBoolean() {
		val properties = Properties()
		properties.load("bool", false.toString())
		assertThat(properties.getBoolean("bool"), `is`(false))
	}

	data class SomeObject(val a: Int, val b: Int)
}