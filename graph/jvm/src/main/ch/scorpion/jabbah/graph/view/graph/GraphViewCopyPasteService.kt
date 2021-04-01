package ch.scorpion.jabbah.graph.view.graph

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.Movable
import ch.scorpion.jabbah.edit.model.CopyPasteService
import ch.scorpion.jabbah.edit.model.PasteInfo
import ch.scorpion.jabbah.graph.GraphStorable
import ch.scorpion.jabbah.graph.model.Net
import ch.scorpion.jabbah.graph.model.Port
import ch.scorpion.jabbah.graph.model.Vertice
import ch.scorpion.jabbah.graph.view.*
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
	private val storableCreator: StorableCreator = IOModule.storableCreator,
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
				) { c -> c !is Component || componentIds.contains(c.id) && c.copyable }

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

	override fun paste(contents: String, view: DrawingView<Drawing<Component>>, dislocation: Point2D): List<Component> {
		val components = mutableListOf<Component>()
		ByteArrayInputStream(contents.toByteArray()).use {
			try {
				val xmlReader = ElectricXmlReader(it)
				val reader = StoreXmlReader(xmlReader, typeMap, storableCreator)
				val copy: Storable = reader.readStorable()

				val componentsIter = when (copy) {
					is GraphStorable -> copy.graphView.backToFrontIterator()
					is Drawing<*> -> copy.backToFrontIterator()
					else -> throw IllegalArgumentException("expecting pasted contents to be of type 'Drawing'")
				}

				var pastedAnchorComponent: Component? = null
				for (c in componentsIter) {
					if (keepAfterStripping(c, copy)) {
						components.add(c)
						if (pastedAnchorComponentId == null && getOrigAnchorComponent(view)?.location == c.location) {
							pastedAnchorComponent = c
						}
					}
				}

				if (copy is GraphStorable) {
					cleanupNets(components, copy.graphView)
				}

				Movable.moveBy(components, dislocation)
				components.forEach { c -> view.drawing.add(c) }

				if (pastedAnchorComponent != null) {
					pastedAnchorComponentId = pastedAnchorComponent.id
				}

				return components
			} catch (e: Exception) {
				LOG.error("Error while reading Components from clipboard: ${e.message}")
				throw RuntimeException(e)
			}
		}
	}

	private fun getOrigAnchorComponent(view: DrawingView<Drawing<Component>>): Component? =
		origAnchorComponentId?.let { view.drawing.getWithId(it) }

	override fun paste(contents: String, view: DrawingView<Drawing<Component>>): PasteInfo {
		val origAnchorComponent = origAnchorComponentId?.let { view.drawing.getWithId(it) }
		val dislocation: Point2D = if (origAnchorComponent != null && pastedAnchorComponentId != null) {
			pasteCount++
			val pastedAnchorComponent = view.drawing.getWithId(pastedAnchorComponentId!!)
			pastedAnchorComponent!!.location.subtract(origAnchorComponent.location).multiply(pasteCount.toDouble())
		} else {
			Point2D(
				DEFAULT_DISTANCE_FACTOR * view.grid.distance,
				DEFAULT_DISTANCE_FACTOR * view.grid.distance)
		}

		val components = paste(contents, view, dislocation)

		return PasteInfo(components, dislocation)
	}

	private fun keepAfterStripping(component: Component, copy: Storable): Boolean {
		if (copy !is GraphStorable) {
			// No stripping
			return true
		}
		val graphView = (copy as GraphStorable).graphView
		return when (component) {
			is VerticeView<*> -> keepAfterDisconnectingFromNetsWithoutEdgeView(component, graphView)
			is NodeView<*> -> keepAfterRemovingDanglingNodeView(component, graphView)
			else -> true
		}
	}

	/**
	 * Unconnects the [Port]s of all [VerticeView]s not contained in the [GraphView] from
	 * the [Net]s they are connected with.
	 */
	private fun cleanupNets(components: List<Component>, graphView: GraphView) {
		components
			.filter { it is NetViewElement<*> }
			.forEach { netViewElem ->
				val ports = ((netViewElem as NetViewElement<*>).net!!.ports).toList()
				ports.forEach { port ->
					if (port.owner != null && graphView.getElementViews(port.owner!!).isEmpty()) {
						netViewElem.net!!.unconnect(port)
					}
				}
			}
	}

	/**
	 * Disconnects all [Port]s of a [Vertice] from [Net]s that don't have a
	 * corresponding [EdgeView] in the specified [GraphView].
	 */
	private fun keepAfterDisconnectingFromNetsWithoutEdgeView(verticeView: VerticeView<*>, graphView: GraphView): Boolean {
		for (pv in verticeView.getPortViews()) {
			if (pv.port.net != null) {
				val edgeViews = graphView.getElementViews(pv.port.net!!)
				if (edgeViews.isEmpty()) {
					pv.port.disconnect()
					(pv as PortView<Any>).handleUnconnect(null)
				}
			}
		}
		return true
	}

	private fun keepAfterRemovingDanglingNodeView(nodeView: NodeView<*>, graphView: GraphView): Boolean {
		when (nodeView.getEdgeViews().size) {
			0 -> {
				connectService.removeNodeView(graphView, nodeView)
				return false
			}
			1 -> {
				val edgeView = nodeView.getEdgeViews()[0]
				if (edgeView.origin?.connectableView === nodeView) {
					connectService.unconnectEdgeViewOrigin(edgeView)
				} else if (edgeView.destination?.connectableView === nodeView) {
					connectService.unconnectEdgeViewDestination(edgeView)
				}
				return false
			}
		}
		return true
	}
}