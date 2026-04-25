package io.antarescircuit.jabbah.edit.app

import io.antarescircuit.jabbah.base.Action
import io.antarescircuit.jabbah.base.event.ActionEvent
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.draw.view.ContentViewManager
import io.antarescircuit.jabbah.draw.view.DrawViewModule
import io.antarescircuit.jabbah.edit.Component
import io.antarescircuit.jabbah.edit.Drawing
import io.antarescircuit.jabbah.edit.DrawingView
import io.antarescircuit.jabbah.edit.model.ComponentMessage
import io.antarescircuit.jabbah.edit.model.ComponentMessageType
import io.antarescircuit.jabbah.edit.module.EditModule

/**
 * A base class for [Actions][Action] that delete selected [Component]s in a [Drawing].
 * This can be "delete", but also "cut" to clipboard.
 */
abstract class AbstractDeleteAction(
    baseKey: String,
    eventBus: EventBus = BaseModule.eventBus,
    viewManager: ContentViewManager = DrawViewModule.viewManager,
    protected val service: DrawingAppService = EditModule.drawingAppService
) : AbstractSelectionAwareAction(baseKey, eventBus, viewManager) {

    companion object {
        /** Creates a new [List] containing only those [Component] that can really be deleted.*/
        fun getComponentsToDelete(components: Collection<Component>): List<Component> {
            return components.filter { it.deletable }.toCollection(mutableListOf())
        }
    }

    protected abstract fun executeImpl(components: List<Component>, drawingView: DrawingView<*,*>)

    override fun execute(event: ActionEvent) {
        val drawingView = viewManager.activeView!!.view as DrawingView<*,*>
        val selection = drawingView.selectionManager.selection
        val components = getComponentsToDelete(selection)

        eventBus.postTwoPhase(
            DeleteQuestion(components, drawingView),
            thenHandler = {
                executeImpl(components, drawingView)
            },
            elseHandler = {
                postUndeleteableMessage(it.source as? Component)
            }
        )
    }

    protected fun postUndeleteableMessage(source: Component? = null) {
        eventBus.post(ComponentMessage(
            ComponentMessageType.Info,
            source = source,
            "edit.action.undeletable.msg"
        ))
    }
}