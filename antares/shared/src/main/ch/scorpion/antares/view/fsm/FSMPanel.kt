package ch.scorpion.antares.view.fsm

import ch.scorpion.antares.model.fsm.FSMDrawing
import ch.scorpion.jabbah.app.ApplicationDataContentEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.ui.AbstractUIController
import ch.scorpion.jabbah.base.ui.UIView
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.CurrentEditorEvent
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.editor.EditEditorModule
import ch.scorpion.jabbah.edit.view.DrawingViewImpl

interface FSMPanelView : UIView

class FSMPanelController(
    private val eventBus: EventBus = BaseModule.eventBus
) : AbstractUIController<FSMPanelView>() {

    private val applicationDataContentHandler: EventHandler<ApplicationDataContentEvent> = { handle(it) }

    val drawingView: DrawingView<FSMDrawing> = DrawingViewImpl(FSMDrawing())

    val editor = EditEditorModule.createEditor(drawingView as DrawingView<Drawing<Component>>)

    val transitionTool = FSMTransitionTool(editor)

    init {
        eventBus.register(ApplicationDataContentEvent::class, applicationDataContentHandler)
        eventBus.post(CurrentEditorEvent(editor))
    }

    override fun dispose() {
        super.dispose()
        editor.dispose()
        eventBus.unregister(applicationDataContentHandler)
    }

    private fun handle(event: ApplicationDataContentEvent) {
       setDrawing(event.data.content as FSMDrawing)
    }

    fun setDrawing(fsmDrawing: FSMDrawing) {
        drawingView.setDrawing(fsmDrawing)
    }
}