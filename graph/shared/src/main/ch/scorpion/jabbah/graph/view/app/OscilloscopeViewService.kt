package ch.scorpion.jabbah.graph.view.app

import ch.scorpion.jabbah.base.collection.ImmutableList
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.Command
import ch.scorpion.jabbah.edit.CommandManager
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.command.AbstractCommand
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.graph.view.GraphElementView
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.oscilloscope.OscilloscopeView
import ch.scorpion.jabbah.graph.view.oscilloscope.OscilloscopeProbeVerticeView

/**
 * Application-level services related with [OscilloscopeView].
 */
interface OscilloscopeViewService {

    /**
     * Determines whether the [OscilloscopeView] is currently displayed in the specified [GraphView].
     * That property is not explicitly stored in [GraphView]. It is rather controlled by
     * the visibility property of [OscilloscopeView] and the corresponding [OscilloscopeProbeVerticeView]s.
     */
    fun isOscilloscopeDisplayed(graphView: GraphView<GraphElementView<*>>): Boolean

    /**
     * Changes displaying the [OscilloscopeView] in the specified [GraphView] according to the specified parameter.
     * Does nothing if displaying doesn't have to be changed. Creates an instance of [OscilloscopeView] if necessary
     * and no one exists yet, and positions it right below the bounding box of the [GraphView]'s contents. Posts an
     * [OscilloscopeDisplayEvent] on [EventBus] if displaying of the [OscilloscopeView] has been changed, and
     * registers a [Command] with [CommandManager] to be used for undoing the change.
     */
    fun displayOscilloscope(display: Boolean, graphView: GraphView<GraphElementView<*>>)

    /** Adds a new row at the end of the specified [OscilloscopeView]*/
    fun addRow(oscilloscopeView: OscilloscopeView)

    /** Removes a row from the specified [OscilloscopeView].*/
    fun removeRow(index: Int, oscilloscopeView: OscilloscopeView)

}

/**
 * Posted by [OscilloscopeViewService] on [EventBus] when the displaying of [OscilloscopeView] has changed
 * in a particular [GraphView].
 */
data class OscilloscopeDisplayEvent(val graphView: GraphView<*>)

/**
 * A application service that deals with [OscilloscopeView].
 */
class OscilloscopeViewServiceImpl(
        private val commandManager: CommandManager = EditModule.commandManager,
        private val eventBus: EventBus = BaseModule.eventBus
) : OscilloscopeViewService {

    companion object {
        private val LOG by logger(OscilloscopeViewServiceImpl::class)

        /** The vertical distance between the [GraphView]'s bounding box when positioning [OscilloscopeView].*/
        private val DISTANCE = 20.0
    }

    /** ---- [OscilloscopeViewService] */

    override fun isOscilloscopeDisplayed(graphView: GraphView<GraphElementView<*>>): Boolean {
        val ov = findOscilloscopeView(graphView)
        return ov != null && ov.visible
    }

    override fun displayOscilloscope(display: Boolean, graphView: GraphView<GraphElementView<*>>) {
        val existed = findOscilloscopeView(graphView) != null
        if (display) {
            commandManager.execute(DisplayOscilloscopeCommand(existed, graphView, this))
        } else {
            commandManager.execute(HideOscilloscopeCommand(existed, graphView, this))
        }
    }

    override fun addRow(oscilloscopeView: OscilloscopeView) {
        commandManager.execute(AddRowCommand(oscilloscopeView))
    }

    override fun removeRow(index: Int, oscilloscopeView: OscilloscopeView) {
        commandManager.execute(RemoveRowCommand(index, oscilloscopeView))
    }

    /** ---- [OscilloscopeViewServiceImpl] */

    private fun findOscilloscopeView(graphView: GraphView<GraphElementView<*>>): OscilloscopeView? {
        return graphView.getDrawable { it is OscilloscopeView } as OscilloscopeView?
    }

    private fun findProbeViews(graphView: GraphView<GraphElementView<*>>): ImmutableList<Component> {
        return graphView.getDrawables { it is OscilloscopeProbeVerticeView<*> }
    }

    /** Positions [OscilloscopeView] right beneath [GraphView]'s bounding box.*/
    private fun positionOscilloscope(ov: OscilloscopeView, graphView: GraphView<GraphElementView<*>>) {
        val bbox = graphView.boundingBox
        ov.location = Point2D(bbox.centerX - ov.width / 2, bbox.maxY + DISTANCE)
    }

    private fun displayOscilloscopeImpl(create: Boolean, graphView: GraphView<GraphElementView<*>>) {
        if (create) {
            LOG.debug("OscilloscopeViewService: display Oscilloscope by creating")
            val ov = OscilloscopeView()
            positionOscilloscope(ov, graphView)
            graphView.add(ov)
        } else {
            LOG.debug("OscilloscopeViewService: display Oscilloscope by making visible")
            findOscilloscopeView(graphView)!!.visible = true
            findProbeViews(graphView).forEach { it.visible = true }
        }
        eventBus.post(OscilloscopeDisplayEvent(graphView))
    }

    private fun hideOscilloscopeImpl(delete: Boolean, graphView: GraphView<GraphElementView<*>>) {
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

    private class DisplayOscilloscopeCommand(
            private val existed: Boolean,
            private val graphView: GraphView<GraphElementView<*>>,
            private val service: OscilloscopeViewServiceImpl
    ) : AbstractCommand("graph.command.displayOscilloscope") {

        override fun execute() { service.displayOscilloscopeImpl(!existed, graphView) }
        override fun undo() { service.hideOscilloscopeImpl(!existed, graphView) }
    }

    private class HideOscilloscopeCommand(
            private val existed: Boolean,
            private val graphView: GraphView<GraphElementView<*>>,
            private val service: OscilloscopeViewServiceImpl
    ) : AbstractCommand("graph.command.hideOscilloscope") {

        override fun execute() { service.hideOscilloscopeImpl(!existed, graphView) }
        override fun undo() { service.displayOscilloscopeImpl(!existed, graphView) }
    }

    private class AddRowCommand(
            private val oscilloscopeView: OscilloscopeView
    ) : AbstractCommand("graph.command.addOscilloscopeRow") {

        override fun execute() {
            oscilloscopeView.addRow()
        }

        override fun undo() {
            oscilloscopeView.removeRow(oscilloscopeView.rowsCount)
        }
    }

    private class RemoveRowCommand(
            private val index: Int,
            private val oscilloscopeView: OscilloscopeView
    ) : AbstractCommand("graph.command.removeOscilloscopeRow") {

        override fun execute() {
            oscilloscopeView.removeRow(index)
        }

        override fun undo() {
            // TODO Should add the new row at the old index!
            oscilloscopeView.addRow()
        }
    }
}



