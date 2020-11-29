package ch.scorpion.jabbah.app

import io.mockk.every
import io.mockk.mockk

class ApplicationViewMockBuilder(controller: ApplicationDataViewController) {

	private val view = mockk<ApplicationDataView>()

	init {
		controller.view = view
	}

	fun withSaveUnchangedDataDecision(decision: SaveUnchangedDataDecision): ApplicationViewMockBuilder {
		every { view.decideSaveChangedData(any()) } returns decision
		return this
	}

	fun withSavableForStoring(savable: Savable?): ApplicationViewMockBuilder {
		every { view.defineSavableForStoring(any(), any()) } returns savable
		return this
	}

	fun withSavableForLoading(savable: Savable?): ApplicationViewMockBuilder {
		every { view.defineSavableForLoading() } returns savable
		return this
	}

	fun build(): ApplicationDataView = view
}