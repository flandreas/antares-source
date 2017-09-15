package ch.scorpion.jabbah.graph.view.app

import ch.scorpion.jabbah.base.collection.ImmutableList
import ch.scorpion.jabbah.base.event.EventBus
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

    private val LOG by logger(OscilloscopeViewServiceImpl::class)

    /** ---- [OscilloscopeViewService] */

    override fun isOscilloscopeDisplayed(graphView: GraphView<GraphElementView<*>>): Boolean {
        val ov = findOscilloscopeView(graphView)
        return ov != null && ov.visible
    }

    /**
     * Logic:
     * 1. If not existing:
     * 1a. Create OscilloscopeView
     * 1.b. Add OscilloscopeView to Graph
     * 2. Make OscilloscopeView visible
     * 3. Make all ProbeVerticeViews visible
     */
    override fun displayOscilloscope(display: Boolean, graphView: GraphView<GraphElementView<*>>) {
        val existed = findOscilloscopeView(graphView) != null
        if (display) {
            commandManager.beginTransaction(DisplayOscilloscopeCommand(existed, graphView, this))
        } else {
            commandManager.beginTransaction(HideOscilloscopeCommand(existed, graphView, this))
        }
        commandManager.commitTransaction()
    }

    /** ---- [OscilloscopeViewServiceImpl] */

    private fun findOscilloscopeView(graphView: GraphView<GraphElementView<*>>): OscilloscopeView? {
        return graphView.getDrawable { it is OscilloscopeView } as OscilloscopeView?
    }

    private fun findProbeViews(graphView: GraphView<GraphElementView<*>>): ImmutableList<Component> {
        return graphView.getDrawables { it is OscilloscopeProbeVerticeView<*> }
    }

    private fun displayOscilloscopeImpl(create: Boolean, graphView: GraphView<GraphElementView<*>>) {
        if (create) {
            LOG.debug("OscilloscopeViewService: display Oscilloscope by creating")
            graphView.add(OscilloscopeView())
            // TODO Positioning
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
}



