package ch.scorpion.antares.view.input

import ch.scorpion.antares.AntaresTestRule
import org.junit.ClassRule
import org.junit.Test

class DipSwitchViewTest {

	companion object {
		@ClassRule
		@JvmField
		val rule = AntaresTestRule()
	}

	@Test
	fun shouldInstantiate() {
		DipSwitchView()
	}
}