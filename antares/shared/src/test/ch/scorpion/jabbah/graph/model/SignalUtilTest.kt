package ch.scorpion.jabbah.graph.model

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SignalUtilTest {

	@Test
	fun bothNullShouldBeEqual() {
		assertTrue(SignalUtil.equals(null, null))
	}

	@Test
	fun nullShouldNotBeEqualToNonNull() {
		assertFalse(SignalUtil.equals(null, "a"))
	}

	@Test
	fun nonNullShouldNotBeEqualToNull() {
		assertFalse(SignalUtil.equals("a", null))
	}

	@Test
	fun shouldBeEqual() {
		assertTrue(SignalUtil.equals("a", "a"))
	}

	@Test
	fun shouldNotBeEqual() {
		assertFalse(SignalUtil.equals("a", "b"))
	}
}