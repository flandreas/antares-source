package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.app.AbstractApplication
import ch.scorpion.jabbah.app.Application
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.view.DrawingViewImpl
import ch.scorpion.jabbah.graph.VirtualCanvas
import ch.scorpion.jabbah.graph.app.ApplicationMode
import ch.scorpion.jabbah.graph.library.LibraryModule
import ch.scorpion.jabbah.graph.project.ProjectModule
import ch.scorpion.jabbah.graph.ui.library.LibraryTreeViewActions
import ch.scorpion.jabbah.graph.view.editor.GraphEditor
import ch.scorpion.jabbah.graph.view.graph.GraphViewImpl
import ch.scorpion.jabbah.graph.view.module.GraphViewModule


/** A test [Application] implementation used for integration testing. */
class TestGraphApplication : AbstractApplication(GraphDataViewController()) {

	private val applicationDataViewBuilder = ApplicationDataViewMockBuilder(controller)
	private val canvas = VirtualCanvas { DrawingViewImpl(GraphViewImpl() as Drawing<Component>)}
	val editor = GraphEditor(canvas.view as DrawingView<Drawing<Component>>)
	val graphFrameController = GraphFrameController<GraphFrame>(controller, editor = editor)
	val graphFrameBuilder = GraphFrameMockBuilder(graphFrameController)

	// Used to create an instance of OpenContainerLibraryElementAction
	private val actions = LibraryTreeViewActions(
		graphFrameController.graphPanelViewController.libraryPanelController.libraryTreeViewController,
		this)

	init {
		GraphViewModule.applicationModeHolder = graphFrameController.graphPanelViewController
	}

	/** ---- [Application] */

	override val displayName: String = "Test"

	override fun start() {
		openInitialSavable()
	}

	fun startSimulation() {
		graphFrameController.graphPanelViewController.setMode(ApplicationMode.EXECUTE)
	}

	fun stopSimulation() {
		graphFrameController.graphPanelViewController.setMode(ApplicationMode.EDIT)
	}

	private fun openInitialSavable() {
		val dataViewController = (controller as GraphDataViewController)

		if (!ProjectModule.projectManagementService.directoryExists) {
			ProjectModule.projectManagementService
				.createHelloProject(LibraryModule.libraryHolder.library.uuid)
				.also { dataViewController.openProject(it.uuid) }
			return
		}
		dataViewController.closeData()
	}
}