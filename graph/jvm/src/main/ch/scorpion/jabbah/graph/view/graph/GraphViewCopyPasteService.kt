package ch.scorpion.jabbah.graph.view.graph

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.draw.View
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.draw.drawable.Movable
import ch.scorpion.jabbah.edit.SnapResult
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
	 * The first pasted [Component] after copying [origAnchorComponentId]. Used for locating future pastes from the same
	 * copy with equal offsets (aka "Array mode").
	 */
	private var pastedAnchorComponentId: Int? = null

	/**
	 * Tracks the number of consecutive pasts without an intermediate copy. Used to produce
	 * equal dislocations (aka "Array mode").
	 */
	private var pasteCount: Int = 0

	/** If set, pasting in "Array mode" is displayed. It gets set after the first paste outside the visible area.*/
	private var pasteAtMouseLocation: Boolean = false

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
				pasteAtMouseLocation = false
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

		return pasteImpl(contents, view.drawing, dislocation, view, adjustDislocation = true)
	}

	override fun paste(contents: String, drawing: Drawing<Component>, dislocation: Point2D, view: DrawingView<*>): PasteInfo =
        pasteImpl(contents, drawing, dislocation, view, adjustDislocation = false)

	private fun pasteImpl(
		contents: String,
		drawing: Drawing<Component>,
		dislocation: Point2D,
		view: DrawingView<*>,
		adjustDislocation: Boolean
	): PasteInfo {

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
			} catch (_: Exception) {
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

		val effDislocation = if (adjustDislocation) {
			effectiveDislocation(copyDrawing, dislocation, view)
		} else {
			dislocation
		}

		Movable.moveBy(copyDrawing.drawables, effDislocation)

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

		return PasteInfo(copyDrawing.drawables.map { it.id }, effDislocation)
	}

	private fun snap(p: Point2D, drawing: Drawing<Component>, view: DrawingView<*>): Point2D {
		if (!drawing.drawables.isEmpty()) {
			val snapResult = SnapResult()
			with (drawing.drawables.first().location.add(p)) {
				view.grid.snap(x, y, snapResult)
			}
			return p.add(snapResult.dx, snapResult.dy)
		}
		return p
	}

	/** Place pasted components at mouse location if standard dislocation would place them outside the visible area.*/
    private fun effectiveDislocation(copyDrawing: Drawing<Component>, dislocation: Point2D, view: DrawingView<*>): Point2D {
        if (copyDrawing.drawables.isEmpty()) {
            return dislocation
        }

        if (!pasteAtMouseLocation && viewContains(view.modelToView(copyDrawing.boundingBox.center.add(dislocation)), view)) {
            // Dislocated content center is inside visible area
            LOG.debug("Dislocated content center is inside visible area, use standard dislocation")
            return dislocation
        }

        // Dislocated content center would be outside visible area: Check other options
        if (viewContains(view.canvas.mouseLocation, view)) {
            // Mouse is inside visible view area: Place at mouse location
            LOG.debug("Mouse is inside visible view area: Place at mouse location")
			pasteAtMouseLocation = true
            return snap(
				view.viewToModel(view.canvas.mouseLocation).subtract(copyDrawing.boundingBox.center),
				copyDrawing,
				view
			)
        }

        // Mouse is outside visible view area: Place at center of visible area
        LOG.debug("Mouse is outside visible view area: Place at center of visible area")
		pasteAtMouseLocation = true
        return snap(
			view.viewToModel(view.center).subtract(copyDrawing.boundingBox.center),
			copyDrawing,
			view
		)
    }

	private fun viewContains(p: Point2D, view: View<*>): Boolean =
		p.xInt >= 0 && p.yInt >= 0 && p.xInt < view.width && p.yInt < view.height

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