package ch.scorpion.jabbah.graph.view.app

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.Command
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
        private val eventBus: EventBus = BaseModule.eventBus
) : OscilloscopeViewService {

    /** ---- [OscilloscopeViewService] */

    override fun isOscilloscopeDisplayed(graphView: GraphView<GraphElementView<*>>): Boolean {
        return findOscilloscopeView(graphView) != null
    }

    override fun displayOscilloscope(display: Boolean, graphView: GraphView<GraphElementView<*>>) {
        val ov = findOscilloscopeView(graphView)
        if (ov != null) {
            if (ov.visible) {
                return
            }
            // TODO Command
            // TODO Show all OscilloscopeProbeVerticeView
            ov.visible = true
        }

        // TODO Command
        val newOv = OscilloscopeView()
        graphView.add(newOv)
        graphView.validate()

        eventBus.post(OscilloscopeDisplayEvent(graphView))
    }

    /** ---- [OscilloscopeViewServiceImpl] */

    private fun findOscilloscopeView(graphView: GraphView<*>): OscilloscopeView? {
        return graphView.getDrawable { it is OscilloscopeView } as OscilloscopeView?
    }

    private fun displayOscilloscope(graphView: GraphView<GraphElementView<*>>, oscilloscopeView: OscilloscopeView) {
        // TODO Logic without Commands. Make Oscilloscope and all its ProbeViews visible
    }

    private fun hideOscilloscope(graphView: GraphView<GraphElementView<*>>, oscilloscopeView: OscilloscopeView) {
        // TODO Logic without Commands. Make Oscilloscope and all its ProbeViews visible
    }
}