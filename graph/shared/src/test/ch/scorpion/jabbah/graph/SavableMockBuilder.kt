package ch.scorpion.jabbah.graph

import ch.scorpion.jabbah.app.Savable
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock

/**
 *  TODO: Move to app module when test helpers from other module can be used in Kotlin MPP (KT-35073).
 */
class SavableMockBuilder {

	private val savable = mock<Savable>(MockMode.autofill)

	init {
		editable()
	}

	fun editable(): SavableMockBuilder {
		every { savable.editable } returns true
		return this
	}

	fun nonEditable(): SavableMockBuilder {
		every { savable.editable } returns false
		return this
	}

	fun build(): Savable = savable

}