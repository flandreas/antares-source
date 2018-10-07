package ch.scorpion.jabbah.graph.container

import ch.scorpion.jabbah.base.TestTranslationsBuilder
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.swing.JTreeUtil
import ch.scorpion.jabbah.graph.TestLibraryBuilder
import ch.scorpion.jabbah.graph.library.FileLibraryPersistenceService
import ch.scorpion.jabbah.graph.library.LibraryElement
import ch.scorpion.jabbah.graph.library.LibraryImpl
import ch.scorpion.jabbah.graph.library.LibraryModule
import ch.scorpion.jabbah.graph.model.GraphElement
import ch.scorpion.jabbah.graph.model.GraphPort
import ch.scorpion.jabbah.graph.model.Vertice
import ch.scorpion.jabbah.graph.model.vertice.DeepVerticeLink
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVerticeRef
import ch.scorpion.jabbah.graph.view.*
import ch.scorpion.jabbah.graph.view.editor.GraphPortViewEvent
import ch.scorpion.jabbah.graph.view.editor.SubGraphVerticeViewEvent
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.graph.view.port.TestPortFactory
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView
import ch.scorpion.jabbah.graph.view.vertice.TestControlVerticeView
import org.hamcrest.CoreMatchers.*
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

		private fun findDraggableTreeItemOfType(containerTree: ContainerTree, type: ContainerTreeItemType): DefaultMutableTreeNode? {
			return JTreeUtil.findTreeNode(containerTree.model.treeModel.root as TreeNode) {
				it is DefaultMutableTreeNode
					&& it.userObject is DraggableTreeItem
					&& (it.userObject as DraggableTreeItem).type == type
			} as DefaultMutableTreeNode?
		}
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
	fun shouldBuildWithToplevelControlView() {
		val setup = Setup().addToplevelControlToGraphView().build()

		assertControlView(setup.containerTree, notNullValue())
	}

	@Test
	fun controlOfContainerShouldNotBeAddedToTree() {
		val source = TestControlVerticeView()
		val setup = Setup()
		setup.graphView.add(source)
		setup.containerDrawing.add(ControlViewComponent(controlView = source.createControlView() as ControlView<Vertice>))
		setup.build()

		assertControlView(setup.containerTree, nullValue())
	}

	@Test
	fun shouldAddControlView() {
		val setup = Setup().build().addToplevelControlToGraphView()
		val source = TestControlVerticeView()

		setup.graphView.add(source)
		// The following event is normally posted by GraphEditor
		BaseModule.eventBus.post(ControlViewSourceEvent(ControlViewSourceEvent.Type.ADD, source as ControlViewSource<Vertice>))

		assertControlView(setup.containerTree, notNullValue())
	}

	@Test
	fun shouldRemoveControlView() {
		val setup = Setup().build().addToplevelControlToGraphView()
		val source = TestControlVerticeView()
		setup.graphView.add(source)
		// The following event is normally posted by GraphEditor
		BaseModule.eventBus.post(ControlViewSourceEvent(ControlViewSourceEvent.Type.ADD, source as ControlViewSource<Vertice>))

		setup.expandAll()
		setup.graphView.remove(source)
		// The following event is normally posted by GraphEditor
		BaseModule.eventBus.post(ControlViewSourceEvent(ControlViewSourceEvent.Type.REMOVE, source as ControlViewSource<Vertice>))


		assertControlView(setup.containerTree, nullValue())
	}

	@Test
	fun shouldContainDeepControlInInitialTree() {
		val setup = Setup().build().addInnerCustomSubGraphVerticeView().expandAll()

		assertControlView(setup.containerTree, notNullValue())
	}

	@Test
	fun deepControlOfContainerShouldNoBeContainedInInitialTree() {
		val setup = Setup().addInnerCustomSubGraphVerticeView().addDeepControlViewToContainer().build()

		assertControlView(setup.containerTree, nullValue())
	}

	@Test
	fun shouldAddControlOfAddedSubGraphVerticeViewToExpandedTree() {
		val setup = Setup().build().addInnerCustomSubGraphVerticeView().expandAll()

		assertControlView(setup.containerTree, notNullValue())
	}

	@Test
	fun shouldRemoveControlOfRemovedSubGraphVerticeViewFromExpandedTree() {
		val setup = Setup().build().addInnerCustomSubGraphVerticeView().expandAll()

		// The following event is normally posted by GraphEditor
		BaseModule.eventBus.post(SubGraphVerticeViewEvent(SubGraphVerticeViewEvent.Type.REMOVE, setup.graphView.getSubGraphVerticeViews().first()))

		assertControlView(setup.containerTree, nullValue())
	}

	@Test
	fun shouldAddPortViewToTree() {
		val setup = Setup().build().addGraphInputPortViewToGraphView().expandAll()
		val portView = setup.graphView.getGraphPortView("I1")!!

		// The following event is normally posted by GraphEditor
		BaseModule.eventBus.post(GraphPortViewEvent(GraphPortViewEvent.Type.ADD, portView as GraphPortView<*>))

		assertPortView(setup.containerTree, notNullValue())
	}

	@Test
	fun shouldRemoveAddedPortViewFromTree() {
		val setup = Setup()
			.build()
			.addGraphInputPortViewToGraphView()
			.addPortViewComponent()

		assertPortView(setup.containerTree, nullValue())
	}

	@Test
	fun shouldAddRemovedPortViewToTree() {
		val setup = Setup()
			.build()
			.addGraphInputPortViewToGraphView()
			.addPortViewComponent()

		setup.removePortViewFromContainer()

		assertPortView(setup.containerTree, notNullValue())
	}

	@Test
	fun shouldRemoveAddedToplevelControlViewFromTree() {
		val setup = Setup().addToplevelControlToGraphView().build()

		setup.addToplevelControlToContainer().expandAll()

		assertControlView(setup.containerTree, nullValue())
	}

	@Test
	fun shouldRemoveAddedDeepControlViewFromTree() {
		val setup = Setup().addInnerCustomSubGraphVerticeView().build()

		setup.addDeepControlViewToContainer().expandAll()

		assertControlView(setup.containerTree, nullValue())
	}

	@Test
	fun shouldRemoveRepeatedlyAddedDeepControlViewFromTree() {
		val setup = Setup().addInnerCustomSubGraphVerticeView().build()
		setup.addDeepControlViewToContainer().expandAll()
		setup.removeControlFromContainer()

		setup.addDeepControlViewToContainerUsingTree()

		assertControlView(setup.containerTree, nullValue())
	}

	@Test
	fun shouldAddRemovedDeepControlViewFromTree() {
		val setup = Setup()
			.addInnerCustomSubGraphVerticeView()
			.addDeepControlViewToContainer()
			.build()
			.expandAll()

		setup.removeControlFromContainer()

		assertControlView(setup.containerTree, notNullValue())
	}

	@Test
	fun shouldAddRemovedToplevelControlViewToTree() {
		val setup = Setup()
			.addToplevelControlToGraphView()
			.addToplevelControlToContainer()
			.build()

		setup.removeControlFromContainer()

		assertControlView(setup.containerTree, notNullValue())
	}

	@Test
	fun shouldChangePortName() {
		val setup = Setup().build().addGraphInputPortViewToGraphView().expandAll()
		val portView = setup.graphView.getGraphPortView("I1")!!
		// The following event is normally posted by GraphEditor
		BaseModule.eventBus.post(GraphPortViewEvent(GraphPortViewEvent.Type.ADD, portView as GraphPortView<*>))

		portView.model!!.name = "newName"

		assertPortViewName(setup.containerTree, "newName")
	}

	/**
	 * Utility class for building a configurable test data setup of main [GraphView] and [ContainerDrawing].
	 * Expands all nodes of its [ContainerTree] after instantiation.
	 */
	private class Setup {

		private val graphViewBuilder = GraphViewBuilder<Boolean>()
		private var _containerTree: ContainerTree? = null
		private val toplevelControlViewSource = TestControlVerticeView()

		val graphView: GraphView<GraphElementView<out GraphElement>> get() = graphViewBuilder.graphView
		val containerDrawing = ContainerDrawing()
		val containerTree: ContainerTree get() = _containerTree!!

		fun build(): Setup {
			_containerTree = ContainerTree(mainGraphView = graphViewBuilder.build(), containerDrawing = containerDrawing)
			return this
		}

		fun addToplevelControlToGraphView(): Setup {
			graphViewBuilder.addVerticeView(toplevelControlViewSource)
			return this
		}

		/** Adds a "custom comp" [SubGraphVerticeView] to the main [GraphView].*/
		fun addInnerCustomSubGraphVerticeView(): Setup {
			val library = LibraryModule.libraryHolder.library
			TestLibraryBuilder().addInnerCustomComponent(library)
			val vv = (library.get(TestLibraryBuilder.INNER_CUSTOM_COMP) as LibraryElement).getNewInstance<SubGraphVerticeRef>() as VerticeView<*>
			graphViewBuilder.addVerticeView(vv)
			// TODO This shouldn't be necessary any more
			//containerTree.model.addSubGraphVerticeView(vv as SubGraphVerticeView<SubGraphVertice>)
			return this
		}

		fun addToplevelControlToContainer(): Setup {
			containerDrawing.add(ControlViewComponent(controlView = toplevelControlViewSource.createControlView() as ControlView<Vertice>))
			return this
		}

		/** Adds a [ControlViewComponent] with a [ControlView] to the [ContainerDrawing].*/
		fun addDeepControlViewToContainer(): Setup {
			val subGraphVerticeView = graphView.getSubGraphVerticeViews()[0]
			val source = subGraphVerticeView.createSubGraphView().getControlViewSources()[0]
			val link = DeepVerticeLink(subGraphVerticeView.id)
			containerDrawing.add(ControlViewComponent(controlView = source.createControlView(), baseLink = link))
			return this
		}

		fun addDeepControlViewToContainerUsingTree(): Setup {
			val node = findDraggableTreeItemOfType(containerTree, ContainerTreeItemType.Control)
			val control = (node!!.userObject as DraggableTreeItem).factory.invoke() as ControlViewComponent
			containerDrawing.add(control)
			return this
		}

		fun removeControlFromContainer(): Setup {
			containerDrawing.remove(containerDrawing.getDrawable { it is ControlViewComponent }!!)
			return this
		}

		fun removePortViewFromContainer(): Setup {
			containerDrawing.remove(containerDrawing.getDrawable { it is PortViewComponent<*> }!!)
			return this
		}

		fun addGraphInputPortViewToGraphView(name: String = "I1"): Setup {
			graphViewBuilder.addVerticeView(TestGraphPortView.input(name))
			return this
		}

		/**
		 * Adds a new [PortViewComponent] to the [ContainerDrawing] for an input [GraphPortView] with
		 * specified name in the main [GraphView].
		 */
		fun addPortViewComponent(graphPortName: String = "I1"): Setup {
			val graphPortView = graphViewBuilder.graphView.getGraphPortView(graphPortName)
			containerDrawing.add(createPortViewComponent(graphPortView!!.model!!))
			JTreeUtil.expandAll(containerTree.model.treeModel.root as TreeNode)
			return this
		}


		fun expandAll(): Setup {
			JTreeUtil.expandAll(containerTree.model.treeModel.root as TreeNode)
			return this
		}

		private fun createPortViewComponent(graphPort: GraphPort<*>): PortViewComponent<*> {
			return GraphViewModule.portFactory.createPortViewComponent(
				GraphViewModule.portFactory.createPortView(
					GraphViewModule.portFactory.createSubGraphPort(graphPort)))
		}
	}

	private fun assertControlView(containerTree: ContainerTree, matcher: Matcher<in TreeNode?>) {
		assertContainerTreeItem(containerTree, matcher, ContainerTreeItemType.Control)
	}

	private fun assertPortView(containerTree: ContainerTree, matcher: Matcher<in TreeNode?>) {
		assertContainerTreeItem(containerTree, matcher, ContainerTreeItemType.Port)
	}

	private fun assertPortViewName(containerTree: ContainerTree, name: String) {
		assertThat((findDraggableTreeItemOfType(containerTree, ContainerTreeItemType.Port)!!.userObject as ContainerTreePortItem).portName, `is`(name))
	}

	private fun assertContainerTreeItem(containerTree: ContainerTree, matcher: Matcher<in TreeNode?>, type: ContainerTreeItemType) {
		assertThat(findDraggableTreeItemOfType(containerTree, type), matcher)
	}
}