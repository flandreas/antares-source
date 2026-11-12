package io.antarescircuit.antares.view.output

import io.antarescircuit.antares.AntaresTestRule
import io.antarescircuit.jabbah.edit.model.Size
import kotlin.test.Test
import kotlin.test.assertTrue

class TerminalViewTest {

	init {
		AntaresTestRule.configure()
	}

	@Test
	fun shouldGrowWhenSizeIncreases() {
		val view = TerminalView()
		val mediumWidth = view.width
		val mediumHeight = view.height

		view.size = Size.LARGE

		assertTrue(view.width > mediumWidth)
		assertTrue(view.height > mediumHeight)
	}
}
