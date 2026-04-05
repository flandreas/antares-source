package io.antarescircuit.jabbah.base

import io.antarescircuit.jabbah.base.module.BaseModule
import kotlin.test.*

/**
 * Unit tests for [Properties].
 */
class PropertiesTest {

	@BeforeTest
	fun setup() {
		BaseModule.require()
	}

	@Test
	fun shouldGetPredefined() {
		val properties = Properties()
		properties.set("name", "ABC")
		assertEquals("ABC", properties.getString("name"))
	}

	@Test
	fun shouldGetObject() {
		val properties = Properties()
		properties.set("myObject", SomeObject(1, 2))
		assertEquals(SomeObject(1, 2), properties.get("myObject"))
	}

	@Test
	fun shouldCustomizeInt() {
		val properties = Properties()
		properties.set("myInt", 5)
		properties.customize("myInt", 6)
		assertEquals(6, properties.getInt("myInt"))
	}

	@Test
	fun shouldGetCustomizesKeys() {
		val properties = Properties()
		properties.set("myInt", 5)
		properties.customize("myInt", 6)
		assertEquals("myInt", properties.getCustomizedKeys().next())
	}

	@Test
	fun shouldLoadBoolean() {
		val properties = Properties()
		properties.load("bool", false.toString())
		assertFalse(properties.getBoolean("bool"))
	}

	data class SomeObject(val a: Int, val b: Int)
}