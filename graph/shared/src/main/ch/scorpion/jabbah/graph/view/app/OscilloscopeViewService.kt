package ch.scorpion.jabbah.graph.view.app

import ch.scorpion.jabbah.base.collection.ImmutableList
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.CommandManager
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Undoable
import ch.scorpion.jabbah.edit.command.AbstractCommand
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.oscilloscope.OscilloscopeProbeVerticeView
import ch.scorpion.jabbah.graph.view.oscilloscope.OscilloscopeView

/**
 * Application-level services related with [OscilloscopeView].
 *
 * Displaying an [OscilloscopeView] in a [GraphView] is a non-persistent property. When displaying an [OscilloscopeView],
 * the visibility property of the [OscilloscopeView] and its [OscilloscopeProbeVerticeView]s is set to `true`, and
 * these properties are not stored.
 */
interface OscilloscopeViewService {

	/**
	 * Determines whether the [OscilloscopeView] is currently displayed in the specified [GraphView].
	 * That property is not explicitly stored in [GraphView]. It is rather controlled by
	 * the visibility property of [OscilloscopeView] and the corresponding [OscilloscopeProbeVerticeView]s.
	 */
	fun isOscilloscopeDisplayed(graphView: GraphView): Boolean

	/**
	 * Displays the [OscilloscopeView] in the specified [GraphView].
	 * Does nothing if it is already displayed. Creates an instance of [OscilloscopeView] if necessary
	 * and no one exists yet, and positions it just below the bounding box of the [GraphView]'s contents. Posts an
	 * [OscilloscopeDisplayEvent] on [EventBus] if displaying of the [OscilloscopeView] has been changed.
	 */
	fun displayOscilloscope(graphView: GraphView)

	/**
	 * Hides the [OscilloscopeView] in the specified [GraphView].
	 * Does nothing if it is not displayed. Posts an [OscilloscopeDisplayEvent] on [EventBus].
	 */
	fun hideOscilloscope(graphView: GraphView)

	/** Adds a new row at the end of the specified [OscilloscopeView]*/
	fun addRow(oscilloscopeView: OscilloscopeView)

	/** Removes a row from the specified [OscilloscopeView].*/
	fun removeRow(name: String, oscilloscopeView: OscilloscopeView)

}

/**
 * Posted by [OscilloscopeViewService] on [EventBus] when the displaying of [OscilloscopeView] has changed
 * in a particular [GraphView].
 */
data class OscilloscopeDisplayEvent(val graphView: GraphView)

/**
 * An application service dealing with [OscilloscopeView].
 */
class OscilloscopeViewServiceImpl(
	private val commandManager: CommandManager = EditModule.commandManager,
	private val eventBus: EventBus = BaseModule.eventBus
) : OscilloscopeViewService {

	companion object {
		private val LOG by logger(OscilloscopeViewServiceImpl::class)

		/** The vertical distance between the [GraphView]'s bounding box when positioning [OscilloscopeView].*/
		private const val DISTANCE = 20.0
	}

	/** ---- [OscilloscopeViewService] */

	override fun isOscilloscopeDisplayed(graphView: GraphView): Boolean {
		val ov = findOscilloscopeView(graphView)
		return ov != null && ov.visible
	}

	override fun displayOscilloscope(graphView: GraphView) {
		val existed = findOscilloscopeView(graphView) != null
		displayOscilloscopeImpl(!existed, graphView)
	}

	override fun hideOscilloscope(graphView: GraphView) {
		val existed = findOscilloscopeView(graphView) != null
		hideOscilloscopeImpl(!existed, graphView)
	}

	override fun addRow(oscilloscopeView: OscilloscopeView) {
		commandManager.execute(AddRowCommand(oscilloscopeView))
	}

	override fun removeRow(name: String, oscilloscopeView: OscilloscopeView) {
		commandManager.execute(RemoveRowCommand(name, oscilloscopeView))
	}

	/** ---- [OscilloscopeViewServiceImpl] */

	private fun findOscilloscopeView(graphView: GraphView): OscilloscopeView? {
		return graphView.getDrawable { it is OscilloscopeView } as OscilloscopeView?
	}

	private fun findProbeViews(graphView: GraphView): ImmutableList<Component> {
		return graphView.getDrawables { it is OscilloscopeProbeVerticeView<*> }
	}

	/** Positions [OscilloscopeView] right beneath [GraphView]'s bounding box.*/
	private fun positionOscilloscope(ov: OscilloscopeView, graphView: GraphView) {
		val bbox = graphView.boundingBox
		ov.location = Point2D(bbox.centerX - ov.width / 2, bbox.maxY + DISTANCE)
	}

	private fun displayOscilloscopeImpl(create: Boolean, graphView: GraphView) {
		if (create) {
			LOG.debug("OscilloscopeViewService: display Oscilloscope by creating")
			val ov = OscilloscopeView()
			positionOscilloscope(ov, graphView)
			graphView.add(ov)
			ov.visible = true
		} else {
			LOG.debug("OscilloscopeViewService: display Oscilloscope by making visible")
			findOscilloscopeView(graphView)!!.visible = true
			findProbeViews(graphView).forEach { it.visible = true }
		}
		eventBus.post(OscilloscopeDisplayEvent(graphView))
	}

	private fun hideOscilloscopeImpl(delete: Boolean, graphView: GraphView) {
		if (delete) {
			LOG.debug("OscilloscopeViewService: hide Oscilloscope by deleting")
			graphView.remove(findOscilloscopeView(graphView)!!)
		} else {
			LOG.debug("OscilloscopeViewService: hide Oscilloscope by making invisible")
			findOscilloscopeView(graphView)!!.visible = false
			findProbeViews(graphView).forEach { it.visible = false }
		}
		eventBus.post(OscilloscopeDisplayEvent(graphView))
	}

	private class AddRowCommand(
		private val oscilloscopeView: OscilloscopeView
	) : AbstractCommand("graph.command.addOscilloscopeRow"), Undoable {

		override fun execute() {
			oscilloscopeView.addRow()
		}

		override fun undo() {
			oscilloscopeView.removeLastRow()
		}
	}

	private class RemoveRowCommand(
		private val name: String,
		private val oscilloscopeView: OscilloscopeView
	) : AbstractCommand("graph.command.removeOscilloscopeRow"), Undoable {

		override fun execute() {
			oscilloscopeView.removeRow(name)
		}

		override fun undo() {
			// TODO Should add the new row at the old index!
			oscilloscopeView.addRow()
		}
	}
}



