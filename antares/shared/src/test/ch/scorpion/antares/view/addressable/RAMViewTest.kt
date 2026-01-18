package ch.scorpion.antares.view.addressable

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.io.StorableCloner
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