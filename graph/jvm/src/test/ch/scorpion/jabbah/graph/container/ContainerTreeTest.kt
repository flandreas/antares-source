package ch.scorpion.jabbah.graph.container

import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.swing.JTreeUtil
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.graph.GraphUITestRule
import ch.scorpion.jabbah.graph.TestLibraryBuilder
import ch.scorpion.jabbah.graph.library.FileLibraryPersistenceService
import ch.scorpion.jabbah.graph.library.LibraryElement
import ch.scorpion.jabbah.graph.library.LibraryImpl
import ch.scorpion.jabbah.graph.library.LibraryModule
import ch.scorpion.jabbah.graph.model.GraphPort
import ch.scorpion.jabbah.graph.model.Vertice
import ch.scorpion.jabbah.graph.model.module.GraphModelModule
import ch.scorpion.jabbah.graph.model.port.TestPortFactory
import ch.scorpion.jabbah.graph.model.vertice.DeepVerticeLink
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVerticeRef
import ch.scorpion.jabbah.graph.view.*
import ch.scorpion.jabbah.graph.view.editor.GraphPortViewEvent
import ch.scorpion.jabbah.graph.view.editor.SubGraphVerticeViewEvent
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.graph.view.port.TestPortViewFactory
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView
import ch.scorpion.jabbah.graph.view.vertice.TestControlVerticeView
import java.io.File
import java.nio.file.Files
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.TreeNode
import kotlin.io.path.absolutePathString
import kotlin.io.path.name
import kotlin.test.*

/** Unit tests for [ContainerTree].*/
class ContainerTreeTest {

	companion object {

		private fun findDraggableTreeItemOfType(containerTree: ContainerTree, type: ContainerTreeItemType): DefaultMutableTreeNode? {
			return JTreeUtil.findTreeNode(containerTree.model.treeModel.root as TreeNode) {
				it is DefaultMutableTreeNode
					&& it.userObject is DraggableTreeItem
					&& (it.userObject as DraggableTreeItem).type == type
			} as DefaultMutableTreeNode?
		}

		init {
			GraphUITestRule.configure()
			BaseModule.properties.set(ContainerDrawingLayouter.PROP_CONTAINER_DRAWING_LAYOUTER, ContainerDrawingLayouter.Narrow.customName)
		}
	}

	@BeforeTest
	fun setup() {
		val dir = Files.createTempDirectory(null)
		File.createTempFile("library", ".lib", dir.toFile())
		LibraryModule.userLibraryPersistenceService = FileLibraryPersistenceService({ dir.parent.absolutePathString() }, dir.name)
		LibraryModule.libraryHolder.l = LibraryImpl(TranslatableText("test"))
		GraphModelModule.portFactory = TestPortFactory()
		GraphViewModule.portViewFactory = TestPortViewFactory()
	}

	@Test
	fun shouldBuildWithToplevelControlView() {
		val setup = Setup().addToplevelControlToGraphView().build()

		assertControlViewNotNull(setup.containerTree)
	}

	@Test
	fun controlOfContainerShouldNotBeAddedToTree() {
		val source = TestControlVerticeView()
		val setup = Setup()
		setup.graphView.add(source)
		setup.containerDrawing.add(ControlViewComponent(source = source as ControlViewSource<Vertice>))
		setup.build()

		assertControlViewNull(setup.containerTree)
	}

	@Test
	fun shouldAddControlView() {
		val setup = Setup().build().addToplevelControlToGraphView()
		val source = TestControlVerticeView()

		setup.graphView.add(source)
		// The following event is normally posted by GraphEditor
		BaseModule.eventBus.post(ControlViewSourceEvent(ControlViewSourceEvent.Type.ADD, source as ControlViewSource<Vertice>))

		assertControlViewNotNull(setup.containerTree)
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


		assertControlViewNull(setup.containerTree)
	}

	@Test
	fun shouldContainDeepControlInInitialTree() {
		val setup = Setup().build().addInnerCustomSubGraphVerticeView().expandAll()

		assertControlViewNotNull(setup.containerTree)
	}

	@Test
	fun deepControlOfContainerShouldNoBeContainedInInitialTree() {
		val setup = Setup().addInnerCustomSubGraphVerticeView().addDeepControlViewToContainer().build()

		assertControlViewNull(setup.containerTree)
	}

	@Test
	fun shouldAddControlOfAddedSubGraphVerticeViewToExpandedTree() {
		val setup = Setup().build().addInnerCustomSubGraphVerticeView().expandAll()

		assertControlViewNotNull(setup.containerTree)
	}

	@Test
	fun shouldRemoveControlOfRemovedSubGraphVerticeViewFromExpandedTree() {
		val setup = Setup().build().addInnerCustomSubGraphVerticeView().expandAll()

		// The following event is normally posted by GraphEditor
		BaseModule.eventBus.post(SubGraphVerticeViewEvent(SubGraphVerticeViewEvent.Type.REMOVE, setup.graphView.getSubGraphVerticeViews().first()))

		assertControlViewNull(setup.containerTree)
	}

	@Test
	fun shouldAddPortViewToTree() {
		val setup = Setup().build().addGraphInputPortViewToGraphView().expandAll()
		val portView = setup.graphView.getGraphPortView("I1")!!

		// The following event is normally posted by GraphEditor
		BaseModule.eventBus.post(GraphPortViewEvent(GraphPortViewEvent.Type.ADD, portView))

		assertPortViewNotNull(setup.containerTree)
	}

	@Test
	fun shouldRemoveAddedPortViewFromTree() {
		val setup = Setup()
			.build()
			.addGraphInputPortViewToGraphView()
			.addPortViewComponent()

		assertPortViewNull(setup.containerTree)
	}

	@Test
	fun shouldAddRemovedPortViewToTree() {
		val setup = Setup()
			.build()
			.addGraphInputPortViewToGraphView()
			.addPortViewComponent()

		setup.removePortViewFromContainer()

		assertPortViewNotNull(setup.containerTree)
	}

	@Test
	fun shouldRemoveAddedToplevelControlViewFromTree() {
		val setup = Setup().addToplevelControlToGraphView().build()

		setup.addToplevelControlToContainer().expandAll()

		assertControlViewNull(setup.containerTree)
	}

	@Test
	fun shouldRemoveAddedDeepControlViewFromTree() {
		val setup = Setup().addInnerCustomSubGraphVerticeView().build()

		setup.addDeepControlViewToContainer().expandAll()

		assertControlViewNull(setup.containerTree)
	}

	@Test
	fun shouldRemoveRepeatedlyAddedDeepControlViewFromTree() {
		val setup = Setup().addInnerCustomSubGraphVerticeView().build()
		setup.addDeepControlViewToContainer().expandAll()
		setup.removeControlFromContainer()

		setup.addDeepControlViewToContainerUsingTree()

		assertControlViewNull(setup.containerTree)
	}

	@Test
	fun shouldAddRemovedDeepControlViewFromTree() {
		val setup = Setup()
			.addInnerCustomSubGraphVerticeView()
			.addDeepControlViewToContainer()
			.build()
			.expandAll()

		setup.removeControlFromContainer()

		assertControlViewNotNull(setup.containerTree)
	}

	@Test
	fun shouldAddRemovedToplevelControlViewToTree() {
		val setup = Setup()
			.addToplevelControlToGraphView()
			.addToplevelControlToContainer()
			.build()

		setup.removeControlFromContainer()

		assertControlViewNotNull(setup.containerTree)
	}

	@Test
	fun shouldChangePortName() {
		val setup = Setup().build().addGraphInputPortViewToGraphView().expandAll()
		val portView = setup.graphView.getGraphPortView("I1")!!
		// The following event is normally posted by GraphEditor
		BaseModule.eventBus.post(GraphPortViewEvent(GraphPortViewEvent.Type.ADD, portView))

		portView.model.name = "newName"

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

		val graphView: GraphView get() = graphViewBuilder.graphView
		val containerDrawing = ContainerDrawing()
		val containerTree: ContainerTree get() = _containerTree!!

		fun build(): Setup {
			_containerTree = ContainerTree(mainGraphView = graphViewBuilder.build(), containerDrawing = containerDrawing)
			_containerTree!!.isManualContainer = true
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
			containerDrawing.add(ControlViewComponent(source = toplevelControlViewSource as ControlViewSource<Vertice>))
			return this
		}

		/** Adds a [ControlViewComponent] with a [ControlView] to the [ContainerDrawing].*/
		fun addDeepControlViewToContainer(): Setup {
			val subGraphVerticeView = graphView.getSubGraphVerticeViews()[0]
			val source = subGraphVerticeView.createSubGraphView(null).getControlViewSources()[0]
			val link = DeepVerticeLink(subGraphVerticeView.id)
			containerDrawing.add(ControlViewComponent(source = source, baseLink = link))
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
			graphViewBuilder.addVerticeView(TestGraphPortView.input<Boolean>(name))
			return this
		}

		/**
		 * Adds a new [PortViewComponent] to the [ContainerDrawing] for an input [GraphPortView] with
		 * specified name in the main [GraphView].
		 */
		fun addPortViewComponent(graphPortName: String = "I1"): Setup {
			val graphPortView = graphViewBuilder.graphView.getGraphPortView(graphPortName)
			containerDrawing.add(createPortViewComponent(graphPortView!!.model))
			JTreeUtil.expandAll(containerTree.model.treeModel.root as TreeNode)
			return this
		}


		fun expandAll(): Setup {
			JTreeUtil.expandAll(containerTree.model.treeModel.root as TreeNode)
			return this
		}

		private fun createPortViewComponent(graphPort: GraphPort<*>): PortViewComponent<*> {
			return GraphViewModule.portViewFactory.createPortViewComponent(
				GraphViewModule.portViewFactory.createPortView(
					GraphModelModule.portFactory.createSubGraphPort(graphPort, graphView.graph!!.type)))
		}
	}

	private fun assertControlViewNull(containerTree: ContainerTree) {
		assertNull(findDraggableTreeItemOfType(containerTree, ContainerTreeItemType.Control))
	}

	private fun assertControlViewNotNull(containerTree: ContainerTree) {
		assertNotNull(findDraggableTreeItemOfType(containerTree, ContainerTreeItemType.Control))
	}


	private fun assertPortViewNull(containerTree: ContainerTree) {
		assertNull(findDraggableTreeItemOfType(containerTree, ContainerTreeItemType.Port))
	}

	private fun assertPortViewNotNull(containerTree: ContainerTree) {
		assertNotNull(findDraggableTreeItemOfType(containerTree, ContainerTreeItemType.Port))
	}

	private fun assertPortViewName(containerTree: ContainerTree, name: String) {
		assertEquals((findDraggableTreeItemOfType(containerTree, ContainerTreeItemType.Port)!!.userObject as ContainerTreePortItem).portName, name)
	}
}