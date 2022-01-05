package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.app.AbstractApplication
import ch.scorpion.jabbah.app.Application
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.graph.VirtualCanvas
import ch.scorpion.jabbah.graph.app.ApplicationMode
import ch.scorpion.jabbah.graph.library.LibraryModule
import ch.scorpion.jabbah.graph.project.ProjectModule
import ch.scorpion.jabbah.graph.ui.library.LibraryTreeViewActions


/** A test [Application] implementation used for integration testing. */
class TestGraphApplication : AbstractApplication(GraphDataViewController()) {

	val graphFrameController = GraphFrameController<GraphFrame>(controller)
	val canvas = VirtualCanvas(graphFrameController.editor.view)

	// Used to create an instance of OpenContainerLibraryElementAction
	private val actions = LibraryTreeViewActions(
		graphFrameController.graphPanelViewController.libraryPanelController.libraryTreeViewController,
		this)

	/** ---- [Application] */

	override val displayName: String = "Test"

	override fun start() {
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
				.createHelloProject(LibraryModule.libraryHolder.library.uuid)
				.also { dataViewController.openProject(it.uuid) }
			return
		}
		dataViewController.closeData()
	}
}