package ch.scorpion.antares.view.input

import ch.scorpion.antares.AntaresTestRule
import kotlin.test.Test

class DipSwitchViewTest {

	companion object {
		init {
			AntaresTestRule.configure()
		}
	}

	@Test
	fun shouldInstantiate() {
		DipSwitchView()
	}
}