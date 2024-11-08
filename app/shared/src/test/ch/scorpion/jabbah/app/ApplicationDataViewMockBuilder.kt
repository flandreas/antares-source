package ch.scorpion.jabbah.app

import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock

class ApplicationDataViewMockBuilder(controller: ApplicationDataViewController) {

	private val view = mock<ApplicationDataView>()

	init {
		controller.view = view
	}

	fun withSaveUnchangedDataDecision(decision: SaveUnchangedDataDecision): ApplicationDataViewMockBuilder {
		every { view.decideSaveChangedData(any()) } returns decision
		return this
	}

	fun withSavableForStoring(savable: Savable?): ApplicationDataViewMockBuilder {
		every { view.defineSavableForStoring(any(), any()) } returns savable
		return this
	}

	fun withSavableForLoading(savable: Savable?): ApplicationDataViewMockBuilder {
		every { view.defineSavableForLoading() } returns savable
		return this
	}

	fun build(): ApplicationDataView = view
}