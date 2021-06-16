package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.app.ApplicationDataView
import ch.scorpion.jabbah.app.ApplicationDataViewController
import ch.scorpion.jabbah.app.Savable
import ch.scorpion.jabbah.app.SaveUnchangedDataDecision
import io.mockk.every
import io.mockk.mockk

/**
 * TODO: Copy/Paste from corresponding class in ch.scorpion.jabbah.app test package
 * due to missing Kotlin MPP feature KT-35073.
 */
class ApplicationDataViewMockBuilder(controller: ApplicationDataViewController) {

	private val view = mockk<ApplicationDataView>(relaxed = true)

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