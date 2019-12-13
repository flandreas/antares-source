package ch.scorpion.jabbah.graph.library.uifx

import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.drawable.DefaultDrawableDrawer
import ch.scorpion.jabbah.draw.drawable.DrawableDrawer
import ch.scorpion.jabbah.draw.graphics.Graphics2DFx
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.ComponentDataFormat
import ch.scorpion.jabbah.graph.GraphApplicationContext
import ch.scorpion.jabbah.graph.GraphStorable
import ch.scorpion.jabbah.graph.library.LibraryDirectory
import ch.scorpion.jabbah.graph.library.LibraryElement
import ch.scorpion.jabbah.graph.library.LibraryHolder
import ch.scorpion.jabbah.graph.library.LibraryModule
import ch.scorpion.jabbah.graph.model.GraphElement
import ch.scorpion.jabbah.graph.view.GraphElementView
import ch.scorpion.jabbah.graph.view.VerticeView
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.io.IOModule
import javafx.application.Platform
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.canvas.Canvas
import javafx.scene.control.Label
import javafx.scene.control.TitledPane
import javafx.scene.control.Tooltip
import javafx.scene.image.WritableImage
import javafx.scene.input.ClipboardContent
import javafx.scene.input.TransferMode
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox

/**
 * A controller of a [Node] for displaying the contents of a [Library].
 */
class LibraryPaneFx(
	private val eventBus: EventBus = BaseModule.eventBus,
	private val libraryHolder: LibraryHolder = LibraryModule.libraryHolder,
	outerDrawableDrawer: DrawableDrawer<Component>? = null
) {

	companion object {
		private val LOG by logger(LibraryPaneFx::class)
	}

	private lateinit var _node: VBox
	val node: VBox get() = _node

	private var drawableDrawer: DrawableDrawer<Component> =
		if (outerDrawableDrawer == null) {
			DefaultDrawableDrawer<Component>()
		} else {
			outerDrawableDrawer.successor = DefaultDrawableDrawer<Component>()
			outerDrawableDrawer
		}

	init {
		buildUI()
	}

	/** ---- [LibraryPaneFx] */

	private fun buildUI() {
		_node = VBox()
		_node.isFillWidth = true
		fillNodes()
	}

	private fun fillNodes() {
		val nodes = mutableListOf<Node>()
		for (directory in libraryHolder.library.getItems()) {
			val list = VBox()
			if (directory is LibraryDirectory) {
				directory.getItems().filter { it is LibraryElement }.forEach {
					LOG.debug("Creating LibraryElementNode for ${it.name}")
					val component = (it as LibraryElement).getNewInstance<GraphElement>() as VerticeView<*>
					list.children.add(LibraryElementNode(component).node)
				}
			}
			val pane = TitledPane(directory.name.value, list)
			pane.isExpanded = false
			nodes.add(pane)
		}
		Platform.runLater {
			_node.children.setAll(nodes)
		}
	}
}

class LibraryElementNode(
	private val component: VerticeView<*>,
	private val drawableDrawer: DrawableDrawer<Component> = DefaultDrawableDrawer()
) {

	companion object {
		private val LOG by logger(LibraryElementNode::class)
		private val CANVAS_WIDTH = 60.0
		private val CANVAS_HEIGHT = 60.0
		private val DUMMY_IMAGE = WritableImage(1, 1)
	}

	private lateinit var _node: HBox
	val node: Node get() = _node

	/** Displays the graphical representation of the current [Component]*/
	private val canvas = Canvas(CANVAS_WIDTH, CANVAS_HEIGHT)

	/** Displays the name and the short description of the current [Component].*/
	private val label = Label(component.type)

	private var scale = 1.0

	private val transferData: String = createTransferData()

	init {
		buildUI()
		layout()
		setupDragSource()
		draw()
		if (StringUtils.isNotEmpty(component.shortDescription)) {
			Tooltip.install(_node, Tooltip(component.shortDescription))
		}
	}

	private fun buildUI() {
		_node = HBox()
		_node.alignment = Pos.CENTER_LEFT
		_node.spacing = 10.0
		_node.padding = Insets(3.0, 0.0, 3.0, 0.0)

		_node.children.addAll(canvas, label)
	}

	private fun layout() {
		component.location = Point2D(0, 0)
		val bbox = component.boundingBox
		val fx = canvas.width / bbox.width
		val fy = canvas.height / bbox.height

		if (fx < 1 || fy < 1) {
			scale = Math.min(1.0, Math.min(fx, fy))
		}

		// Horizontally and vertically centered
		component.moveBy(
			(canvas.width - bbox.width * scale) / 2 - bbox.x,
			(canvas.height - bbox.height * scale) / 2 - bbox.y)
	}

	private fun draw() {
		val g = Graphics2DFx(canvas.graphicsContext2D)
		g.scale(scale, scale)
		drawableDrawer.process(DrawContext(g, GraphApplicationContext()), component)
		g.scale(1 / scale, 1 / scale)
	}

	private fun setupDragSource() {
		node.setOnDragDetected {
			LOG.debug("LibraryElementNode: starting drag")
			val dragboard = node.startDragAndDrop(TransferMode.COPY)
			dragboard.setDragView(DUMMY_IMAGE, 0.0, 0.0)

			val content = ClipboardContent()
			content.put(ComponentDataFormat, transferData)
			dragboard.setContent(content)

			it.consume()
		}
	}

	private fun createTransferData(): String {
		val graphView = GraphViewModule.createGraphView<GraphElementView<*>>()
		graphView.add(component)
		return IOModule.storableClonerProvider.invoke().serialize(GraphStorable(graphView))
	}
}