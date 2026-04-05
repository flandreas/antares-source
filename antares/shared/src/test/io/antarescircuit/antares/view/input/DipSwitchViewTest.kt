package io.antarescircuit.antares.view.input

import io.antarescircuit.antares.AntaresTestRule
import kotlin.test.Test

class DipSwitchViewTest {

	init {
		AntaresTestRule.configure()
	}

	@Test
	fun shouldInstantiate() {
		DipSwitchView()
	}
}