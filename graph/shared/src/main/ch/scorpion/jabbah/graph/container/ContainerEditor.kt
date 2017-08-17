package ch.scorpion.jabbah.graph.container

import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.draw.ZoomStrategy
import ch.scorpion.jabbah.draw.ZoomStrategyType
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.SelectionDrawingStrategy
import ch.scorpion.jabbah.edit.editor.EditorImpl
import ch.scorpion.jabbah.graph.model.GraphPortNameChanged
import ch.scorpion.jabbah.graph.view.editor.GraphPortViewEvent

/**
 * An [Editor] for editing the outside view of a [SubGraphVerticeView] as a [ContainerDrawing].
 */
open class ContainerEditor(
    view: DrawingView<Drawing<Component>>,
    eventBus: EventBus
) : EditorImpl(view) {

    init {
        view.defaultZoomStrategy = ZoomStrategy(ZoomStrategyType.VALUE, 2.0)
        view.drawing = ContainerDrawing()
        view.defaultSelectionDrawingStrategy = SelectionDrawingStrategy.ABOVE

        eventBus.register(GraphPortViewEvent::class, {
            if (it.type == GraphPortViewEvent.Type.REMOVE) {
                removePortViewComponent(it.graphPortView.model!!.name!!)
            }
        })

        eventBus.register(GraphPortNameChanged::class, {
            if (StringUtils.isNotEmpty(it.oldName)) {
                val pvc = getContainerDrawing().getPortViewComponent(it.oldName!!)
                if (pvc != null) {
                    pvc.portView!!.setPortName(it.newName!!)
                }
            }
        })
    }

    protected fun getContainerDrawing(): ContainerDrawing {
        return drawing as ContainerDrawing
    }

    /** Removes the [PortViewComponent] for the [Port] with the specified name from the [ContainerDrawing].*/
    private fun removePortViewComponent(name: String) {
        for (c in view.drawing.frontToBackIterator()) {
            if (c is PortViewComponent<*> && c.port.name == name) {
                view.drawing.remove(c)
                view.drawing.validate()
                return
            }
        }
    }
}