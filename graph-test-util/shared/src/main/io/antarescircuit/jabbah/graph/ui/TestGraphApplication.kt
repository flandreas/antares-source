package io.antarescircuit.jabbah.graph.ui

import io.antarescircuit.jabbah.app.AbstractApplication
import io.antarescircuit.jabbah.app.Application
import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.draw.VirtualCanvas
import io.antarescircuit.jabbah.graph.app.ApplicationMode
import io.antarescircuit.jabbah.graph.project.ProjectModule
import io.antarescircuit.jabbah.graph.ui.library.LibraryTreeViewActions


/** A test [Application] implementation used for integration testing. */
class TestGraphApplication<T: GraphFrame>(
    graphDataViewController: GraphDataViewController = GraphDataViewController(),
    val graphFrameController: GraphFrameController<T> = GraphFrameController(graphDataViewController),
) : AbstractApplication(graphDataViewController) {

    init {
		// Required to create an instance of OpenContainerLibraryElementAction
		LibraryTreeViewActions(
			graphFrameController.graphPanelViewController.libraryPanelController.libraryTreeViewController,
			this
		)
		VirtualCanvas(graphFrameController.editor.view)
	}

	/** ---- [Application] */

	override val displayName: String = "Test"

	override val isFirstUsage: Boolean = false

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

		if (!ProjectModule.projectManagementService.directoryExists) {
			ProjectModule.projectManagementService
				.createHelloProject(null, ProjectModule.projectManagementService.createNewMetaGraph())
				.also { dataViewController.openProject(it.identification) }
			return
		}
		dataViewController.closeData()
	}
}