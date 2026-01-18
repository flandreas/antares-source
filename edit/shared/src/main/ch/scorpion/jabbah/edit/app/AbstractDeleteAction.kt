package ch.scorpion.jabbah.edit.app

import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.view.ContentViewManager
import ch.scorpion.jabbah.draw.view.DrawViewModule
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.model.ComponentMessage
import ch.scorpion.jabbah.edit.model.ComponentMessageType
import ch.scorpion.jabbah.edit.module.EditModule

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

    protected abstract fun executeImpl(components: List<Component>, drawingView: DrawingView<*>)

    override fun execute(event: ActionEvent) {
        val drawingView = viewManager.activeView!!.view as DrawingView<*>
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