package ch.scorpion.jabbah.graph.view.graph

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.draw.drawable.Movable
import ch.scorpion.jabbah.edit.model.CopyPasteService
import ch.scorpion.jabbah.edit.model.PasteInfo
import ch.scorpion.jabbah.graph.GraphStorable
import ch.scorpion.jabbah.graph.model.Net
import ch.scorpion.jabbah.graph.model.Port
import ch.scorpion.jabbah.graph.view.GraphElementView
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.NetViewElement
import ch.scorpion.jabbah.graph.view.VerticeView
import ch.scorpion.jabbah.graph.view.connect.GraphViewConnectService
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.graph.view.net.node.NodeView
import ch.scorpion.jabbah.graph.view.port.PortView
import ch.scorpion.jabbah.io.*
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.math.max

class GraphViewCopyPasteService(
	private val typeMap: TypeMap = IOModule.typeMap,
	private val connectService: GraphViewConnectService = GraphViewModule.graphViewConnectService
) : CopyPasteService() {

	companion object {
		private val LOG by logger(GraphViewCopyPasteService::class)

		private const val DEFAULT_DISTANCE_FACTOR = 3
	}

	/** Remembers the ID of the first copied [Component] in order to repeat dislocations for consecutive pastes. */
	private var origAnchorComponentId: Int? = null

	/**
	 * he first pasted [Component] after copying [origAnchorComponentId]. Used for locating future pastes from the same
	 * copy with equal offsets.
	 */
	private var pastedAnchorComponentId: Int? = null

	/** Tracks the number of consecutive pasts without an intermediate copy. Used to produce equal dislocations. */
	private var pasteCount: Int = 0

	override fun reset() {
		origAnchorComponentId = null
		pastedAnchorComponentId = null
		pasteCount = 0
	}

	override fun decrementPasteCount() {
		pasteCount = max(0, pasteCount - 1)
	}

	override fun copy(componentIds: Collection<Int>, drawing: Drawing<*>): String {
		var contents: String
		ByteArrayOutputStream().use {
			try {
				val xmlWriter = ElectricXmlWriter(it)
				val writer = StoreXmlWriter(
					xmlWriter,
					typeMap,
					GlobalIdentityCreator()
				) { c, isToplevel -> c !is Component || !isToplevel || componentIds.contains(c.id) && c.copyable }

				if (drawing is GraphView) {
					writer.writeStorable(GraphStorable(drawing))
				} else {
					writer.writeStorable(drawing)
				}

				contents = String(it.toByteArray())

				origAnchorComponentId = componentIds.iterator().next()
				pastedAnchorComponentId = null
				pasteCount = 1
			} catch(e: Exception) {
				LOG.error("Error while copying Components to clipboard: ${e.message}")
				throw RuntimeException(e)
			}

			return contents
		}
	}

	override fun paste(contents: String, view: DrawingView<Drawing<Component>>): PasteInfo {
		val origAnchorComponent = origAnchorComponentId?.let { view.drawing.getWithId(it) }

		var dislocation: Point2D? = null
		if (origAnchorComponent != null && pastedAnchorComponentId != null) {
			val pastedAnchorComponent = view.drawing.getWithId(pastedAnchorComponentId!!)
			if (pastedAnchorComponent != null) {
				pasteCount++
				dislocation = pastedAnchorComponent.location.subtract(origAnchorComponent.location).multiply(pasteCount.toDouble())
			}
		}

		if (dislocation == null) {
			dislocation = Point2D(
				DEFAULT_DISTANCE_FACTOR * view.grid.distance,
				DEFAULT_DISTANCE_FACTOR * view.grid.distance)
		}

		return PasteInfo(paste(contents, view, dislocation).map { it.id }, dislocation)
	}

	override fun paste(contents: String, view: DrawingView<Drawing<Component>>, dislocation: Point2D): List<Component> =
		paste(contents, view.drawing, dislocation)

	override fun paste(contents: String, drawing: Drawing<Component>, dislocation: Point2D): List<Component> {
		lateinit var copy: Storable
		lateinit var copyDrawing: Drawing<Component>

		ByteArrayInputStream(contents.toByteArray()).use {
			try {
				val xmlReader = ElectricXmlReader(it)
				val reader = StoreXmlReader(xmlReader, typeMap)

				copy = reader.readStorable()

				copyDrawing = when (copy) {
					is GraphStorable -> (copy as GraphStorable).graphView as Drawing<Component>
					is Drawing<*> -> (copy as Drawing<Component>)
					else -> throw IllegalArgumentException("expecting pasted contents to be of type 'Drawing'")
				}
			} catch (e: Exception) {
				throw IllegalArgumentException("expecting pasted contents to be of type 'Drawing'")
			}
		}

		var pastedAnchorComponent: Component? = null
		for (c in copyDrawing.drawables) {
			if (pastedAnchorComponentId == null && getOrigAnchorComponent(drawing)?.location == c.location) {
				pastedAnchorComponent = c
			}
		}

		if (copy is GraphStorable) {
			val graphView = (copy as GraphStorable).graphView

			// Check GraphType
			if (graphView.graph?.type != (drawing as GraphView).graph?.type) {
				throw IllegalArgumentException("cannot paste: incompatible GraphTypes")
			}

			cleanupDanglingNodeViews(graphView)
			cleanupNets(graphView)
			disconnectVerticesFromNetsWithoutEdgeView(graphView)
		}

		Movable.moveBy(copyDrawing.drawables, dislocation)

		// First pass: Force possible errors before starting to alter Drawing.
		// If this produces an exception, the Drawing is still left unchanged.
		copyDrawing.drawables.reversed().forEach { c ->
			if (c is GraphElementView<*>) {
				(drawing as? GraphView)?.graph?.let {
					c.model.graphParamsChanged(it)
				}
			}
		}
		// Second pass: Alter Drawing by adding Components
		copyDrawing.drawables.reversed().forEach { c ->
			c.beforePaste(drawing)
			drawing.add(c)
		}

		if (pastedAnchorComponent != null) {
			pastedAnchorComponentId = pastedAnchorComponent.id
		}

		return copyDrawing.drawables
	}

	private fun getOrigAnchorComponent(drawing: Drawing<Component>): Component? =
		origAnchorComponentId?.let { drawing.getWithId(it) }

	/**
	 * Unconnects the [Port]s of all [VerticeView]s not contained in the [GraphView] from
	 * the [Net]s they are connected with.
	 */
	private fun cleanupNets(graphView: GraphView) {
		graphView.drawables
			.filterIsInstance<NetViewElement<*>>()
			.forEach { netViewElem ->
				val ports = (netViewElem.net!!.ports).toList()
				ports.forEach { port ->
					if (port.owner != null && graphView.getElementViews(port.owner!!).isEmpty()) {
						netViewElem.net!!.unconnect(port)
					}
				}
			}
	}

	private fun disconnectVerticesFromNetsWithoutEdgeView(graphView: GraphView) {
		graphView.getVerticeViews().forEach { vv ->
			vv.getPortViews().forEach { pv ->
				if (pv.port.net != null) {
					val edgeViews = graphView.getElementViews(pv.port.net!!)
					if (edgeViews.isEmpty()) {
						pv.port.disconnect()
						(pv as PortView<Any>).handleUnconnect(null)
					}
				}
			}
		}
	}

	private fun cleanupDanglingNodeViews(graphView: GraphView) {
		var done = false
		while (!done) {
			val nodeViews = graphView.drawables.filterIsInstance<NodeView<*>>().toList()
			done = !cleanedUpDanglingNodeViews(nodeViews, graphView)
		}
	}

	private fun cleanedUpDanglingNodeViews(nodeViews: List<NodeView<*>>, graphView: GraphView): Boolean {
		for (nodeView in nodeViews) {
			val edgeViewCount = nodeView.getEdgeViews().size
			if (edgeViewCount < 3) {
				connectService.removeNodeView(graphView, nodeView)
			}
			if (edgeViewCount in 1..2) {
				return true
			}
		}
		return false
	}
}