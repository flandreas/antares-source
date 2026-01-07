package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.app.ApplicationData
import ch.scorpion.jabbah.edit.CommandManagerMock
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.library.ContainerLibraryElement
import ch.scorpion.jabbah.graph.project.Project
import ch.scorpion.jabbah.graph.project.ProjectManagementService
import ch.scorpion.jabbah.graph.project.ProjectModule
import ch.scorpion.jabbah.graph.project.ProjectSavable
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
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