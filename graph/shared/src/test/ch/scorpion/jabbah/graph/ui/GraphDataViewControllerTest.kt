package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.app.ApplicationData
import ch.scorpion.jabbah.graph.CommandManagerMock
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.library.ContainerLibraryElement
import ch.scorpion.jabbah.graph.project.Project
import ch.scorpion.jabbah.graph.project.ProjectModule
import ch.scorpion.jabbah.graph.project.ProjectSavable
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertSame

class GraphDataViewControllerTest {

	companion object {
		init {
			GraphViewTestRule.configure()
			ProjectModule.projectManagementService = mockk()
		}
	}

	private val commandManagerMock = CommandManagerMock()
	private val controller = GraphDataViewController(commandManagerMock.build())
	private val metaGraph = MetaGraph()
	private val containerLibraryElement = ContainerLibraryElement()
	private val project = mockk<Project>()
	private val projectSavable = ProjectSavable(containerLibraryElement, project)

	init {
		containerLibraryElement.updateMetaGraph(metaGraph)
	}

	@Test
	fun shouldUpdateSavableWhenSettingUndoableState() {
		commandManagerMock.cannotUndo()
		val data = ApplicationData(content = metaGraph, savable = projectSavable)
		controller.open { data }

		val newMetaGraph = MetaGraph()
		controller.setUndoableState(newMetaGraph)

		assertSame(newMetaGraph, containerLibraryElement.metaGraph)
	}
}