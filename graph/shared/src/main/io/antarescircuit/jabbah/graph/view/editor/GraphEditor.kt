package io.antarescircuit.jabbah.graph.view.editor

import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.event.EventHandler
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.edit.Component
import io.antarescircuit.jabbah.edit.Drawing
import io.antarescircuit.jabbah.edit.DrawingView
import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.editor.EditorImpl
import io.antarescircuit.jabbah.edit.model.text.description.NameChangedEvent
import io.antarescircuit.jabbah.graph.library.ContainerLibraryElement
import io.antarescircuit.jabbah.graph.model.Graph
import io.antarescircuit.jabbah.graph.model.Vertice
import io.antarescircuit.jabbah.graph.model.vertice.SubGraphVertice
import io.antarescircuit.jabbah.graph.view.ControlViewSource
import io.antarescircuit.jabbah.graph.view.ControlViewSourceEvent
import io.antarescircuit.jabbah.graph.view.GraphPortView
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.vertice.SubGraphVerticeView

/**
 * An [Editor] for editing a [GraphView].
 */
class GraphEditor(
    view: DrawingView<Drawing<Component>>,
    name: String,
    private val eventBus: EventBus = BaseModule.eventBus
) : EditorImpl(view, name = name) {

    private val containerLibraryElementRenamedHandler: EventHandler<NameChangedEvent> = { handle(it) }

    private val graph: Graph? get() = (view.drawing as GraphView).graph

    init {
        eventBus.register(NameChangedEvent::class, containerLibraryElementRenamedHandler)
    }

    override fun dispose() {
        super.dispose()
        eventBus.unregister(containerLibraryElementRenamedHandler)
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

    private fun handle(event: NameChangedEvent) {
        if (event.owner is ContainerLibraryElement) {
            graph?.handleSubGraphNameChanged((event.owner as ContainerLibraryElement).uuid)
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