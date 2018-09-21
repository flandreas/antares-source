package ch.scorpion.jabbah.graph.container

import ch.scorpion.jabbah.base.TestTranslationsBuilder
import ch.scorpion.jabbah.base.swing.JTreeUtil
import ch.scorpion.jabbah.graph.TestLibraryBuilder
import ch.scorpion.jabbah.graph.library.FileLibraryPersistenceService
import ch.scorpion.jabbah.graph.library.LibraryElement
import ch.scorpion.jabbah.graph.library.LibraryImpl
import ch.scorpion.jabbah.graph.library.LibraryModule
import ch.scorpion.jabbah.graph.model.GraphElement
import ch.scorpion.jabbah.graph.model.Vertice
import ch.scorpion.jabbah.graph.model.port.PortImpl
import ch.scorpion.jabbah.graph.model.vertice.DeepVerticeLink
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVertice
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVerticeRef
import ch.scorpion.jabbah.graph.view.*
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.graph.view.port.PortView
import ch.scorpion.jabbah.graph.view.port.TestPortFactory
import ch.scorpion.jabbah.graph.view.port.TestPortView
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
		val builder = GraphViewBuilder<Boolean>()
		val source = TestControlVerticeView()
		builder.addVerticeView(source)

		val containerTree = ContainerTree(mainGraphView = builder.build(), containerDrawing = ContainerDrawing())

		assertControlView(containerTree, notNullValue())
	}

	@Test
	fun controlOfContainerShouldNotBeAddedToTree() {
		val builder = GraphViewBuilder<Boolean>()
		val source = TestControlVerticeView()
		builder.addVerticeView(source)
		val containerDrawing = ContainerDrawing()
		containerDrawing.add(ControlViewComponent(controlView = source.createControlView() as ControlView<Vertice>))

		val containerTree = ContainerTree(mainGraphView = builder.build(), containerDrawing = containerDrawing)

		assertControlView(containerTree, nullValue())
	}

	@Test
	fun shouldAddControlView() {
		val builder = GraphViewBuilder<Boolean>()
		val containerTree = ContainerTree(mainGraphView = builder.build(), containerDrawing = ContainerDrawing())
		val source = TestControlVerticeView()
		builder.build().add(source)

		containerTree.model.addControlViewSource(source as ControlViewSource<Vertice>)

		assertControlView(containerTree, notNullValue())
	}

	@Test
	fun shouldRemoveControlView() {
		val builder = GraphViewBuilder<Boolean>()
		val source = TestControlVerticeView()
		builder.build().add(source)
		val containerTree = ContainerTree(mainGraphView = builder.build(), containerDrawing = ContainerDrawing())

		containerTree.model.removeControlViewSource(source.controlId!!)

		assertControlView(containerTree, nullValue())
	}

	@Test
	fun shouldContainDeepControlInInitialTree() {
		val builder = createDeepLinkGraphView()

		val containerTree = ContainerTree(mainGraphView = builder.build(), containerDrawing = ContainerDrawing())
		JTreeUtil.expandAll(containerTree.model.treeModel.root as TreeNode)

		assertControlView(containerTree, notNullValue())
	}

	@Test
	fun deepControlOfContainerShouldNotBeContainedInInitialTree() {
		val builder = createDeepLinkGraphView()
		val containerDrawing = createDeepControlContainerDrawing(builder.build())

		val containerTree = ContainerTree(mainGraphView = builder.build(), containerDrawing = containerDrawing)
		JTreeUtil.expandAll(containerTree.model.treeModel.root as TreeNode)

		assertControlView(containerTree, nullValue())
	}

	@Test
	fun shouldAddSubGraphVerticeViewToExpandedTree() {
		val setup = Setup().withInnerCustomSubGraphVerticeView()

		assertControlView(setup.containerTree, notNullValue())
	}

	@Test
	fun shouldRemoveSubGraphVerticeViewFromExpandedTree() {
		val setup = Setup().withInnerCustomSubGraphVerticeView()

		setup.containerTree.model.removeSubGraphVerticeView(setup.graphView.getSubGraphVerticeViews().first())

		assertControlView(setup.containerTree, nullValue())
	}

	@Test
	fun shouldRemoveAddedPortViewFromTree() {
		val setup = Setup().withPortViewComponent()

		assertPortView(setup.containerTree, nullValue())
	}

	@Test
	fun shouldAddRemovedPortViewToTree() {
		val setup = Setup().withPortViewComponent()

		setup.containerDrawing.remove(setup.portViewComponent)

		assertPortView(setup.containerTree, notNullValue())
	}

	/**
	 * Utility class for building a configurable test data setup of main [GraphView] and [ContainerDrawing].
	 * Expands all nodes of its [ContainerTree] after instantiation.
	 */
	private class Setup {

		private val builder = createGraphPortViewGraphView()
		val graphView: GraphView<GraphElementView<out GraphElement>> get() = builder.graphView
		val containerDrawing = ContainerDrawing()
		val containerTree = ContainerTree(mainGraphView = builder.build(), containerDrawing = containerDrawing)
		val portViewComponent: PortViewComponent<Any> = GraphViewModule.portFactory.createPortViewComponent(
			GraphViewModule.portFactory.createPortView(
				GraphViewModule.portFactory.createSubGraphPort(
					builder.graphView.getGraphPortView("I1")!!.model!!)))

		init {
			expandAll()
		}

		/**
		 * Adds a [PortViewComponent] with an input [GraphPortView] to the container drawing
		 * and expands all nodes of its [ContainerTree].
		 */
		fun withPortViewComponent(): Setup {
			containerDrawing.add(portViewComponent)
			JTreeUtil.expandAll(containerTree.model.treeModel.root as TreeNode)
			return this
		}

		/** Adds a "custom comp" [SubGraphVerticeView] to the main [GraphView] and expands all nodes of its [ContainerTree].*/
		fun withInnerCustomSubGraphVerticeView(): Setup {
			val library = LibraryModule.libraryHolder.library
			TestLibraryBuilder().addInnerCustomComponent(library)
			val vv = (library.get(TestLibraryBuilder.INNER_CUSTOM_COMP) as LibraryElement).getNewInstance<SubGraphVerticeRef>()
			graphView.add(vv)
			containerTree.model.addSubGraphVerticeView(vv as SubGraphVerticeView<SubGraphVertice>)
			expandAll()
			return this
		}

		fun expandAll(): Setup {
			JTreeUtil.expandAll(containerTree.model.treeModel.root as TreeNode)
			return this
		}

		private fun createGraphPortViewGraphView(): GraphViewBuilder<Boolean> {
			val builder = GraphViewBuilder<Boolean>()
			builder.addVerticeView(TestGraphPortView.input("I1"))
			return builder
		}
	}

	/** Creates a [GraphView] that contains a [SubGraphVerticeView] referencing the outer custom component. */
	private fun createDeepLinkGraphView(): GraphViewBuilder<Boolean> {
		val library = LibraryModule.libraryHolder.library
		TestLibraryBuilder().addInnerCustomComponent(library)
		val builder = GraphViewBuilder<Boolean>()
		val subGraphVerticeView = (library.get(TestLibraryBuilder.INNER_CUSTOM_COMP) as LibraryElement).getNewInstance<SubGraphVerticeRef>() as SubGraphVerticeView<SubGraphVertice>
		builder.addVerticeView(subGraphVerticeView)
		return builder
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
		assertContainerTreeItem(containerTree, matcher, ContainerTreeItemType.Control)
	}

	private fun assertPortView(containerTree: ContainerTree, matcher: Matcher<in TreeNode?>) {
		assertContainerTreeItem(containerTree, matcher, ContainerTreeItemType.Port)
	}

	private fun assertContainerTreeItem(containerTree: ContainerTree, matcher: Matcher<in TreeNode?>, type: ContainerTreeItemType) {
		assertThat(JTreeUtil.findTreeNode(containerTree.model.treeModel.root as TreeNode) {
			it is DefaultMutableTreeNode
				&& it.userObject is DraggableTreeItem
				&& (it.userObject as DraggableTreeItem).type == type }, matcher)
	}
}