package io.antarescircuit.jabbah.base

import io.antarescircuit.jabbah.base.module.BaseModule
import kotlin.test.*

/** Unit tests for [Settings].*/
class SettingsTest {

	private val settings = Settings()

    @BeforeTest
    fun setup() {
        BaseModule.require()
    }

    @Test
    fun shouldGetInt() {
        settings.set("test", 42)
        assertEquals(42, settings.getInt("test", 100))
    }

    @Test
    fun shouldGetDefaultInt() {
        assertEquals(100, settings.getInt("test", 100))
    }

	@Test
	fun shouldStoreBoolean() {
		settings.set("true", true)
		settings.set("false", false)
		assertTrue(settings.getBoolean("true", false))
		assertFalse(settings.getBoolean("false", true))
	}

	@Test
	fun shouldStoreIntegers() {
		settings.set("list", listOf(4, 18, -3))
		val ints = settings.getIntegers("list")
		assertEquals(4, ints[0])
		assertEquals(18, ints[1])
		assertEquals(-3, ints[2])
	}

	@Test
	fun shouldStoreEmptyIntegers() {
		settings.set("list", listOf())
		val ints = settings.getIntegers("list")
		assertTrue(ints.isEmpty())
	}
}