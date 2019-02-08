package ch.scorpion.jabbah.base

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.BeforeTest
import kotlin.test.assertTrue

class MathJvmTest {

	@BeforeTest
	fun setup() {
		Math = MathJvm()
	}

	@Test
	fun shouldCalculateRandomInt() {
		val value = Random.nextInt(10, 100)
		assertTrue(value >= 10)
		assertTrue(value <= 100)
	}
}