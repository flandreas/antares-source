package ch.scorpion.jabbah.base

import org.junit.Assert.*
import org.junit.BeforeClass
import org.junit.Test
import org.hamcrest.CoreMatchers.`is`

class MathJvmTest {

	companion object {
		@BeforeClass @JvmStatic
		fun setup() {
			Math = MathJvm()
		}
	}

	@Test
	fun shouldCalculateRandomInt() {
		val value = Math.randomInt(10, 100)
		assertThat(value >= 10, `is`(true))
		assertThat(value <= 100, `is`(true))
	}
}