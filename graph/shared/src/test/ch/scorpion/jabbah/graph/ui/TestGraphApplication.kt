package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.app.AbstractApplication
import ch.scorpion.jabbah.app.Application
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.graph.VirtualCanvas
import ch.scorpion.jabbah.graph.app.ApplicationMode
import ch.scorpion.jabbah.graph.project.ProjectModule
import ch.scorpion.jabbah.graph.ui.library.LibraryTreeViewActions


/** A test [Application] implementation used for integration testing. */
class TestGraphApplication : AbstractApplication(GraphDataViewController()) {

	val graphFrameController = GraphFrameController<GraphFrame>(controller)

	// Required to create an instance of OpenContainerLibraryElementAction
	private val actions = LibraryTreeViewActions(
		graphFrameController.graphPanelViewController.libraryPanelController.libraryTreeViewController,
		this)

	init {
		VirtualCanvas(graphFrameController.editor.view)
	}

	/** ---- [Application] */

	override val displayName: String = "Test"

	override fun start() {
		GraphDataViewMockBuilder(controller as GraphDataViewController)
		openInitialSavable()
	}

	val editor: Editor get() = graphFrameController.editor

	fun startSimulation() {
		graphFrameController.graphPanelViewController.applicationModeHolder.setMode(ApplicationMode.EXECUTE)
	}

	fun stopSimulation() {
		graphFrameController.graphPanelViewController.applicationModeHolder.setMode(ApplicationMode.EDIT)
	}

	private fun openInitialSavable() {
		val dataViewController = (controller as GraphDataViewController)

		if (!ProjectModule.projectManagementService.invoke().directoryExists) {
			ProjectModule.projectManagementService.invoke()
				.createHelloProject(null)
				.also { dataViewController.openProject(it.identification) }
			return
		}
		dataViewController.closeData()
	}
}