package io.antarescircuit.jabbah.app

import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock

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