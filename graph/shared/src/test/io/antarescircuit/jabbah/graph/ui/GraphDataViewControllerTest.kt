package io.antarescircuit.jabbah.graph.ui

import io.antarescircuit.jabbah.app.ApplicationData
import io.antarescircuit.jabbah.edit.CommandManagerMock
import io.antarescircuit.jabbah.graph.MetaGraph
import io.antarescircuit.jabbah.graph.library.ContainerLibraryElement
import io.antarescircuit.jabbah.graph.project.Project
import io.antarescircuit.jabbah.graph.project.ProjectManagementService
import io.antarescircuit.jabbah.graph.project.ProjectModule
import io.antarescircuit.jabbah.graph.project.ProjectSavable
import io.antarescircuit.jabbah.graph.view.GraphViewTestRule
import dev.mokkery.mock
import kotlin.test.Test
import kotlin.test.assertSame

class GraphDataViewControllerTest {

	private val commandManagerMock = CommandManagerMock()
	private val controller: GraphDataViewController
	private val metaGraph: MetaGraph
	private val containerLibraryElement: ContainerLibraryElement
	private val projectSavable: ProjectSavable

	init {
		GraphViewTestRule.configure()
		ProjectModule.projectManagementService = ProjectManagementService()

		controller = GraphDataViewController(commandManagerMock.build())
		metaGraph = MetaGraph()
		containerLibraryElement = ContainerLibraryElement()
		projectSavable = ProjectSavable(containerLibraryElement)
		containerLibraryElement.bindTo(mock<Project>())
		containerLibraryElement.updateStorable(metaGraph)
	}

	@Test
	fun shouldUpdateSavableWhenSettingUndoableState() {
		commandManagerMock.cannotUndo()
		val data = ApplicationData(content = metaGraph, savable = projectSavable)
		controller.open { data }

		val newMetaGraph = MetaGraph()
		controller.setUndoableState(newMetaGraph)

		assertSame(newMetaGraph, containerLibraryElement.storable)
	}
}