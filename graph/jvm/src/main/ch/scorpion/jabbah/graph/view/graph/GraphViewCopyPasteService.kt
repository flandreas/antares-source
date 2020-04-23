package ch.scorpion.jabbah.graph.view.graph

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.draw.drawable.Locatable
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.model.CopyPasteService
import ch.scorpion.jabbah.graph.GraphStorable
import ch.scorpion.jabbah.graph.model.Net
import ch.scorpion.jabbah.graph.model.Port
import ch.scorpion.jabbah.graph.model.Vertice
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.GraphElementView
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.VerticeView
import ch.scorpion.jabbah.graph.view.port.PortView
import ch.scorpion.jabbah.io.*
import java.awt.Toolkit
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class GraphViewCopyPasteService(
	private val typeMap: TypeMap = IOModule.typeMap,
	private val storableCreator: StorableCreator = IOModule.storableCreator
) : CopyPasteService() {

	companion object {
		private val LOG by logger(GraphViewCopyPasteService::class)

		private const val DEFAULT_DISTANCE_FACTOR = 3
	}

	/** Remembers the first copies [Component] in order to repeat dislocations for consecutive pasts. */
	private var origAnchorComponent: Component? = null

	/** The pasted [Component] that corresponds with [origAnchorComponent]. */
	private var pastedAnchorComponent: Component? = null

	/** Tracks the number of consecutive pasts without an intermediate copy. Used to produce equal dislocations. */
	private var pasteCount: Int = 0

	override fun copy(componentIds: Collection<Int>, drawing: Drawing<Component>): String {
		var contents: String = ""
		ByteArrayOutputStream().use {
			try {
				val xmlWriter = ElectricXmlWriter(it)
				val writer = StoreXmlWriter(
					xmlWriter,
					typeMap,
					GlobalIdentityCreator()
				) { c -> c !is GraphElementView<*> || componentIds.contains(c.id) }
				val graphStorable = GraphStorable(drawing as GraphView)
				writer.writeStorable(graphStorable)

				contents = String(it.toByteArray())

				origAnchorComponent = drawing.getWithId(componentIds.iterator().next())
				pastedAnchorComponent = null
				pasteCount = 1
			} catch(e: Exception) {
				LOG.error("Error while copying Components to clipboard: ${e.message}")
				throw RuntimeException(e)
			}

			return contents
		}
	}

	override fun paste(contents: String, view: DrawingView<Drawing<Component>>): List<Component> {
		val components = mutableListOf<Component>()
		ByteArrayInputStream(contents.toByteArray()).use {
			try {
				val xmlReader = ElectricXmlReader(it)
				val reader = StoreXmlReader(xmlReader, typeMap, storableCreator)
				val copy: Storable = reader.readStorable()
				val dislocation: Point2D = if (pastedAnchorComponent != null) {
					pasteCount++
					pastedAnchorComponent!!.location.subtract(origAnchorComponent!!.location).multiply(pasteCount.toDouble())
				} else {
					Point2D(
						DEFAULT_DISTANCE_FACTOR * view.grid.distance,
						DEFAULT_DISTANCE_FACTOR * view.grid.distance)
				}

				if (copy is GraphStorable) {
					for (cv in copy.graphView.backToFrontIterator()) {
						if (cv is VerticeView<*>) {
							strip(cv, copy.graphView)
						}
						if (pastedAnchorComponent == null && origAnchorComponent!!.location == cv.location) {
							pastedAnchorComponent = cv
						}
						components.add(cv)
					}
					Locatable.moveLocatables(components, dislocation)
					components.forEach { view.drawing.add(it) }
				}
			} catch(e: Exception) {
				LOG.error("Error while reading Components from clipboard: ${e.message}")
				throw RuntimeException(e)
			}
		}

		return components
	}

	/**
	 * Disconnects all [Port]s of a [Vertice] from [Net]s that don't have a
	 * corresponding [EdgeView] in the specified [GraphView].
	 */
	private fun strip(verticeView: VerticeView<*>, graphView: GraphView) {
		for (pv in verticeView.getPortViews()) {
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