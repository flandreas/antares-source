package ch.scorpion.antares.view.fsm

import ch.scorpion.antares.model.fsm.FSMState
import ch.scorpion.antares.model.fsm.FSMTransition
import ch.scorpion.jabbah.base.event.MouseEvent
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.draw.graphics.Cursor
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.app.DrawingAppService
import ch.scorpion.jabbah.edit.model.AbstractComponentTool
import ch.scorpion.jabbah.edit.module.EditModule

class FSMTransitionTool(
    editor: Editor,
    service: DrawingAppService = EditModule.drawingAppService,
) : AbstractComponentTool<FSMTransition>(editor, service, { FSMTransition() }) {

    private val highlight = FSMStateHighlight()

    private var highlightedState: FSMState? = null

    private var originState: FSMState? = null

    private var transitionGhost: TransitionGhost? = null

    override fun activate() {
        editor.view.setCursor(Cursor.CROSSHAIR)
    }

    override fun mouseMoved(e: MouseEvent, p: Point2D) {
        super.mouseMoved(e, p)
        updateHighlight(p)
    }

    override fun mousePressed(e: MouseEvent, p: Point2D) {
        super.mousePressed(e, p)
        if (highlightedState != null) {
            originState = highlightedState
            transitionGhost = TransitionGhost(originState!!)
            editor.view.animationContainer.add(transitionGhost!!)
            transitionGhost!!.validate()
        }
    }

    override fun mouseDragged(e: MouseEvent, p: Point2D) {
        super.mouseDragged(e, p)
        if (transitionGhost != null) {
            if (highlightedState != null) {
                transitionGhost!!.destinationState = highlightedState
            } else {
                transitionGhost!!.draggedPoint = p
            }
            updateHighlight(p)
        }
    }

    override fun mouseReleased(e: MouseEvent, p: Point2D) {
        super.mouseReleased(e, p)
        if (transitionGhost != null) {
            editor.view.animationContainer.remove(transitionGhost!!)
            transitionGhost = null

            if (highlightedState != null) {
                addComponent(FSMTransition(originState!!.id, highlightedState!!.id))
            }

            editor.drawing.validate()
        }
    }

    private fun updateHighlight(p: Point2D) {
        updateHighlight(editor.drawing.getDrawable { it is FSMState && it.contains(p) } as FSMState?)
    }

    private fun updateHighlight(state: FSMState?) {
        if (state == null) {
            if (highlightedState != null) {
                editor.view.animationContainer.remove(highlight)
                highlightedState = null
                editor.drawing.validate()
            }
        } else {
            if (highlightedState == null) {
                highlightedState = state
                editor.view.animationContainer.add(highlight)
            }
            highlight.updateForState(highlightedState!!)
            highlight.validate()
        }
    }
}