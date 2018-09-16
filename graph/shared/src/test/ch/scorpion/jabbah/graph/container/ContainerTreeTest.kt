package ch.scorpion.jabbah.graph.container

import ch.scorpion.jabbah.base.TestTranslationsBuilder
import ch.scorpion.jabbah.base.swing.JTreeUtil
import ch.scorpion.jabbah.graph.TestLibraryBuilder
import ch.scorpion.jabbah.graph.library.FileLibraryPersistenceService
import ch.scorpion.jabbah.graph.library.LibraryImpl
import ch.scorpion.jabbah.graph.library.LibraryModule
import ch.scorpion.jabbah.graph.model.TestControlVertice
import ch.scorpion.jabbah.graph.model.TestVertice
import ch.scorpion.jabbah.graph.model.Vertice
import ch.scorpion.jabbah.graph.model.port.SubGraphPortImpl
import ch.scorpion.jabbah.graph.model.vertice.GraphInputImpl
import ch.scorpion.jabbah.graph.model.vertice.GraphOutputImpl
import ch.scorpion.jabbah.graph.view.*
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.graph.view.port.TestPortFactory
import ch.scorpion.jabbah.graph.view.port.TestPortView
import ch.scorpion.jabbah.graph.view.vertice.TestControlVerticeView
import ch.scorpion.jabbah.graph.view.vertice.TestVerticeView
import ch.scorpion.jabbah.io.IOModule
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.Matcher
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.ClassRule
import org.junit.Test
import java.io.File
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.TreeNode

/** Unit tests for [ContainerTree].*/
class ContainerTreeTest {

	companion object {
		@ClassRule
		@JvmField
		val rule = GraphViewTestRule()
	}

	@Before
	fun setup() {
		IOModule.typeMap.register("testVertice", TestVertice::class)
		IOModule.typeMap.register("testVerticeView", TestVerticeView::class)
		IOModule.typeMap.register("testControl", TestControlVertice::class)
		IOModule.typeMap.register("testControlView", TestControlVerticeView::class)
		IOModule.typeMap.register("testGraphPortView", TestGraphPortView::class)
		IOModule.typeMap.register("testPortView", TestPortView::class)
		IOModule.typeMap.register("graphInputImpl", GraphInputImpl::class)
		IOModule.typeMap.register("graphOutputImpl", GraphOutputImpl::class)
		IOModule.typeMap.register("portViewComponent", PortViewComponent::class)
		IOModule.typeMap.register("subGraphPortImpl", SubGraphPortImpl::class)

		val file = File.createTempFile("library", ".lib")
		TestTranslationsBuilder().withAnyKey()
		LibraryModule.libraryPersistenceService = FileLibraryPersistenceService(file.parentFile.absolutePath)
		LibraryModule.libraryHolder.l = LibraryImpl("test", libraryService = LibraryModule.libraryService.invoke())
		GraphViewModule.portFactory = TestPortFactory()
	}

	@Test
	fun shouldBuildWithControlView() {
		val testGraphView = TestGraphView()
		val source = TestControlVerticeView()
		testGraphView.graphView.add(source)

		val containerTree = ContainerTree(graphView = testGraphView.graphView, containerDrawing = ContainerDrawing())

		assertControlView(containerTree, notNullValue())
	}

	@Test
	fun controlOfContainerShouldNotBeAddedToTree() {
		val testGraphView = TestGraphView()
		val source = TestControlVerticeView()
		testGraphView.graphView.add(source)
		val containerDrawing = ContainerDrawing()
		containerDrawing.add(ControlViewComponent(controlView = source.createControlView() as ControlView<Vertice>))

		val containerTree = ContainerTree(graphView = testGraphView.graphView, containerDrawing = containerDrawing)

		assertControlView(containerTree, nullValue())
	}

	@Test
	fun shouldAddControlView() {
		val testGraphView = TestGraphView()
		val containerTree = ContainerTree(graphView = testGraphView.graphView, containerDrawing = ContainerDrawing())
		val source = TestControlVerticeView()
		testGraphView.graphView.add(source)

		containerTree.addControlViewSource(source as ControlViewSource<Vertice>)

		assertControlView(containerTree, notNullValue())
	}

	@Test
	fun shouldRemoveControlView() {
		val testGraphView = TestGraphView()
		val source = TestControlVerticeView()
		testGraphView.graphView.add(source)
		val containerTree = ContainerTree(graphView = testGraphView.graphView, containerDrawing = ContainerDrawing())

		containerTree.removeControlViewSource(source.controlId!!)

		assertControlView(containerTree, nullValue())
	}

	@Test
	fun shouldBla() {
		TestLibraryBuilder().addInnerCustomComponent(LibraryModule.libraryHolder.library)
	}

	private fun assertControlView(containerTree: ContainerTree, matcher: Matcher<in TreeNode?>) {
		assertThat(JTreeUtil.findTreeNode(containerTree.treeModel.root as TreeNode) {
			it is DefaultMutableTreeNode
				&& it.userObject is DraggableTreeItem
				&& (it.userObject as DraggableTreeItem).type == ContainerTreeItemType.Control }, matcher)
	}
}