package io.antarescircuit.antares.view.addressable

import io.antarescircuit.antares.AntaresTestRule
import io.antarescircuit.antares.model.signal.BitWidth
import io.antarescircuit.jabbah.edit.model.text.TranslatableText
import io.antarescircuit.jabbah.io.StorableCloner
import kotlin.test.Test
import kotlin.test.assertEquals

class RAMViewTest {

	private val ramView: RAMView

	init {
		AntaresTestRule.configure()
		ramView = RAMView()
	}

	@Test
	fun shouldUpdateLabelWhenSettingAddressWidth() {
		ramView.addressWidth = BitWidth.BW_16

		assertEquals("RAM 64Ki x 8", ramView.label.text)
	}

	@Test
	fun shouldUpdateLabelWhenSettingText() {
		ramView.text = TranslatableText("Test")

		assertEquals("Test", ramView.label.text)
	}

	@Test
	fun shouldResetLabelWhenClearingText() {
		ramView.text = TranslatableText("Test")
		ramView.text = null

		assertEquals("RAM 256 x 8", ramView.label.text)
	}

	@Test
	fun shouldPersistText() {
		ramView.text = TranslatableText("Test")

		val clone = StorableCloner.clone(ramView)

		assertEquals("Test", clone.label.text)
	}
}