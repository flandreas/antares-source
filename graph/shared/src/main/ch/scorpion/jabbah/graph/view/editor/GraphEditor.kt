package ch.scorpion.jabbah.graph.view.editor

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.editor.EditorImpl
import ch.scorpion.jabbah.graph.model.Vertice
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVertice
import ch.scorpion.jabbah.graph.view.ControlViewSource
import ch.scorpion.jabbah.graph.view.ControlViewSourceEvent
import ch.scorpion.jabbah.graph.view.GraphPortView
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView

/**
 * An [Editor] for editing a [GraphView].
 */
class GraphEditor(
    view: DrawingView<Drawing<Component>>,
    private val eventBus: EventBus = BaseModule.eventBus
) : EditorImpl(view) {

	init {
		AutoConnector.drawingView = view
	}

    override fun handleComponentAdded(component: Component) {
        if (component is GraphPortView<*>) {
            eventBus.post(GraphPortViewEvent(GraphPortViewEvent.Type.ADD, component))
        }
        if (component is ControlViewSource<*>) {
            eventBus.post(ControlViewSourceEvent(ControlViewSourceEvent.Type.ADD, component as ControlViewSource<Vertice>))
        }
	    if (component is SubGraphVerticeView<*>) {
		    eventBus.post(SubGraphVerticeViewEvent(SubGraphVerticeViewEvent.Type.ADD, component as SubGraphVerticeView<SubGraphVertice>))
	    }
    }

    override fun handleComponentRemoved(component: Component) {
        if (component is GraphPortView<*>) {
            eventBus.post(GraphPortViewEvent(GraphPortViewEvent.Type.REMOVE, component))
        }
        if (component is ControlViewSource<*>) {
            eventBus.post(ControlViewSourceEvent(ControlViewSourceEvent.Type.REMOVE, component as ControlViewSource<Vertice>))
        }
	    if (component is SubGraphVerticeView<*>) {
		    eventBus.post(SubGraphVerticeViewEvent(SubGraphVerticeViewEvent.Type.REMOVE, component as SubGraphVerticeView<SubGraphVertice>))
	    }
    }
}

/** Posted by [GraphEditor] whenever a [GraphPortView] has been added or removed. */
class GraphPortViewEvent(val type: Type, val graphPortView: GraphPortView<*>) {
    enum class Type {
        ADD, REMOVE
    }
}

/** Posted by [GraphEditor] whenever a [SubGraphVerticeView] has been added or removed. */
class SubGraphVerticeViewEvent(val type: Type, val subGraphVerticeView: SubGraphVerticeView<*>) {
	enum class Type {
		ADD, REMOVE
	}
}