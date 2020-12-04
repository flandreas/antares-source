package ch.scorpion.jabbah.app

import io.mockk.every
import io.mockk.mockk

class ApplicationDataViewMockBuilder(controller: ApplicationDataViewController) {

	private val view = mockk<ApplicationDataView>()

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