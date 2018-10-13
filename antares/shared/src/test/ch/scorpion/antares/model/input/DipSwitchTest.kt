package ch.scorpion.antares.model.input

import ch.scorpion.antares.AntaresTestRule
import org.junit.ClassRule
import org.junit.Test

class DipSwitchTest {

	companion object {
		@ClassRule
		@JvmField
		val rule = AntaresTestRule()
	}

	@Test
	fun shouldInstantiate() {
		DipSwitch()
	}
}