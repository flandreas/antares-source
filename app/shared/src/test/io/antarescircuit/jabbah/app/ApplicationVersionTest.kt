package io.antarescircuit.jabbah.app

import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ApplicationVersionTest {

	@BeforeTest
	fun setup() {
		AppTestRule.configure()
	}

	@Test
	fun shouldParseFromString() {
		val version = ApplicationVersion.parse("0.3.1")

		assertEquals(0, version.major)
		assertEquals(3, version.minor)
		assertEquals(1, version.patch)
		assertNull(version.additionalLabel)
	}

	@Test
	fun shouldParseWithAdditionalLabel() {
		val version = ApplicationVersion.parse("0.3.1-alpha")

		assertEquals(0, version.major)
		assertEquals(3, version.minor)
		assertEquals(1, version.patch)
		assertEquals("alpha", version.additionalLabel)
	}

	@Test
	fun shouldParseWithStructuredAdditionalLabel() {
		val version = ApplicationVersion.parse("0.3.1-alpha-27.5")

		assertEquals(0, version.major)
		assertEquals(3, version.minor)
		assertEquals(1, version.patch)
		assertEquals("alpha-27.5", version.additionalLabel)
	}

	@Test
	fun shouldFormatAsString() {
		assertEquals("0.3.1", ApplicationVersion(0, 3, 1).toString())
		assertEquals("0.3.1-alpha", ApplicationVersion(0, 3, 1, "alpha").toString())
	}

	@Test
	fun shouldCompare() {
		assertTrue(ApplicationVersion("1.0.0") > ApplicationVersion("0.3.1"))
		assertTrue(ApplicationVersion("1.4.0") > ApplicationVersion("1.3.8"))
		assertTrue(ApplicationVersion("0.3.2") > ApplicationVersion("0.3.1"))
		assertTrue(ApplicationVersion("1.0.0-alpha") < ApplicationVersion("1.0.0"))
		assertTrue(ApplicationVersion("1.0.0-alpha") > ApplicationVersion("0.14.0"))
		assertTrue(ApplicationVersion("0.8.4").compareTo(ApplicationVersion("0.8.4")) == 0)
	}
}