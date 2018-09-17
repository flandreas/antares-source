package ch.scorpion.jabbah.graph.container

import ch.scorpion.jabbah.base.TestTranslationsBuilder
import ch.scorpion.jabbah.base.swing.JTreeUtil
import ch.scorpion.jabbah.graph.TestLibraryBuilder
import ch.scorpion.jabbah.graph.library.FileLibraryPersistenceService
import ch.scorpion.jabbah.graph.library.LibraryElement
import ch.scorpion.jabbah.graph.library.LibraryImpl
import ch.scorpion.jabbah.graph.library.LibraryModule
import ch.scorpion.jabbah.graph.model.Vertice
import ch.scorpion.jabbah.graph.model.vertice.DeepVerticeLink
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVertice
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVerticeRef
import ch.scorpion.jabbah.graph.view.*
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.graph.view.port.TestPortFactory
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView
import ch.scorpion.jabbah.graph.view.vertice.TestControlVerticeView
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
	fun shouldContainDeepControlInInitialTree() {
		val graphView = createDeepLinkGraphView()

		val containerTree = ContainerTree(graphView = graphView, containerDrawing = ContainerDrawing())
		JTreeUtil.expandAll(containerTree.treeModel.root as TreeNode)

		assertControlView(containerTree, notNullValue())
	}

	@Test
	fun deepControlOfContainerShouldNotBeContainedInInitalTree() {
		val graphView = createDeepLinkGraphView()
		val containerDrawing = createDeepControlContainerDrawing(graphView)

		val containerTree = ContainerTree(graphView = graphView, containerDrawing = containerDrawing)
		JTreeUtil.expandAll(containerTree.treeModel.root as TreeNode)

		assertControlView(containerTree, nullValue())
	}

	@Test
	fun shouldAddSubGraphVerticeViewToExpandedTree() {
		val testGraphView = TestGraphView()
		val containerTree = ContainerTree(graphView = testGraphView.graphView, containerDrawing = ContainerDrawing())
		JTreeUtil.expandAll(containerTree.treeModel.root as TreeNode)
		val library = LibraryModule.libraryHolder.library
		TestLibraryBuilder().addInnerCustomComponent(library)
		val vv = (library.get(TestLibraryBuilder.INNER_CUSTOM_COMP) as LibraryElement).getNewInstance<SubGraphVerticeRef>()
		testGraphView.graphView.add(vv)

		containerTree.addSubGraphVerticeView(vv as SubGraphVerticeView<SubGraphVertice>)
		JTreeUtil.expandAll(containerTree.treeModel.root as TreeNode)

		assertControlView(containerTree, notNullValue())
	}

	@Test
	fun shouldRemoveSubGraphVerticeViewFromExpandedTree() {
		val graphView = createDeepLinkGraphView()
		val containerDrawing = createDeepControlContainerDrawing(graphView)

		val containerTree = ContainerTree(graphView = graphView, containerDrawing = containerDrawing)
		JTreeUtil.expandAll(containerTree.treeModel.root as TreeNode)

		containerTree.removeSubGraphVerticeView(graphView.getSubGraphVerticeViews().first())

		assertControlView(containerTree, nullValue())
	}

	/** Creates a [GraphView] that contains a [SubGraphVerticeView] referencing the outer custom component. */
	private fun createDeepLinkGraphView(): GraphView<GraphElementView<*>> {
		val library = LibraryModule.libraryHolder.library
		TestLibraryBuilder().addInnerCustomComponent(library)
		val testGraphView = TestGraphView()
		testGraphView.graphView.add((library.get(TestLibraryBuilder.INNER_CUSTOM_COMP) as LibraryElement).getNewInstance<SubGraphVerticeRef>())
		return testGraphView.graphView
	}

	/**
	 * Creates a [ContainerDrawing] that contains a [ControlViewComponent] for the deeply
	 * nested [ControlView] in the specified [GraphView].
	 */
	private fun createDeepControlContainerDrawing(graphView: GraphView<GraphElementView<*>>): ContainerDrawing {
		val containerDrawing = ContainerDrawing()
		val subGraphVerticeView = graphView.getSubGraphVerticeViews()[0]
		val source = subGraphVerticeView.createSubGraphView().getControlViewSources()[0]
		val link = DeepVerticeLink(subGraphVerticeView.id)
		containerDrawing.add(ControlViewComponent(controlView = source.createControlView(), baseLink = link))
		return containerDrawing
	}

	private fun assertControlView(containerTree: ContainerTree, matcher: Matcher<in TreeNode?>) {
		assertThat(JTreeUtil.findTreeNode(containerTree.treeModel.root as TreeNode) {
			it is DefaultMutableTreeNode
				&& it.userObject is DraggableTreeItem
				&& (it.userObject as DraggableTreeItem).type == ContainerTreeItemType.Control }, matcher)
	}
}