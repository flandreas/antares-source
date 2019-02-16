package ch.scorpion.antares.model.input

import ch.scorpion.antares.AntaresTestRule
import kotlin.test.Test

class DipSwitchTest {

	companion object {
		init {
			AntaresTestRule.configure()
		}
	}

	@Test
	fun shouldInstantiate() {
		DipSwitch()
	}
}