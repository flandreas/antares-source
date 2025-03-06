package ch.scorpion.antares.view.fsm

import ch.scorpion.antares.model.fsm.FSMState
import ch.scorpion.antares.model.fsm.FSMTransition
import ch.scorpion.jabbah.base.state.UnhandledEventBehaviour
import ch.scorpion.jabbah.base.state.stateMachine
import ch.scorpion.jabbah.draw.StateMachineInputEventHandler
import ch.scorpion.jabbah.draw.StateMachineInputEventHandler.Companion.mouseDragged
import ch.scorpion.jabbah.draw.StateMachineInputEventHandler.Companion.mouseLeftPressed
import ch.scorpion.jabbah.draw.StateMachineInputEventHandler.Companion.mouseLeftReleased
import ch.scorpion.jabbah.draw.StateMachineInputEventHandler.Companion.mouseMoved
import ch.scorpion.jabbah.draw.graphics.Cursor
import ch.scorpion.jabbah.edit.EditInputEventContext
import ch.scorpion.jabbah.edit.module.EditModule

object FSMTransitionToolHandler {

    private val highlight = FSMStateHighlight()

    private var insideState: FSMState? = null

    private var originState: FSMState? = null

    private var transitionGhost: TransitionGhost? = null

    val fsmTransitionToolHandler = StateMachineInputEventHandler(
        stateMachine<EditInputEventContext>(UnhandledEventBehaviour.Unhandled) {

            state("sense") {
                onEntry { it.view.setCursor(Cursor.DEFAULT) }
                transitTo("insideOrigin") {
                    given { mouseMoved(it) && insideState(it) }
                }
            }

            state("insideOrigin") {
                onEntry {
                    displayHighlight(it)
                    it.view.setCursor(Cursor.CROSSHAIR)
                }
                transitTo("sense") {
                    given { mouseMoved(it) && !insideState(it) }
                    onTransit { hideHighlightedState(it) }
                }
                transitTo("drag") {
                    given { mouseLeftPressed(it) }
                    onTransit {
                        originState = insideState
                        hideHighlightedState(it)
                        displayTransitionGhost(it)
                    }
                }
            }

            state("drag") {
                transitTo("sense") {
                    given { mouseLeftReleased(it) }
                    onTransit {
                        hideTransitionGhost(it)
                    }
                }
                transitTo("insideDestination") {
                    given { mouseDragged(it) && insideState(it) }
                }
                transitTo("drag") {
                    given { mouseDragged(it) }
                    onTransit {
                        transitionGhost!!.draggedPoint = it.location
                    }
                }
            }

            state("insideDestination") {
                onEntry {
                    displayHighlight(it)
                    it.view.setCursor(Cursor.CROSSHAIR)
                    transitionGhost!!.destinationState = insideState
                }
                transitTo("drag") {
                    given { mouseDragged(it) && !insideState(it) }
                    onTransit {
                        transitionGhost!!.destinationState = null
                        hideHighlightedState(it)
                    }
                }
                transitTo("sense") {
                    given { mouseLeftReleased(it) }
                    onTransit {
                        addTransition(it)
                        hideHighlightedState(it)
                        hideTransitionGhost(it)
                    }
                }
            }
        }
    )

    fun use(context: EditInputEventContext) {
        reset()
        fsmTransitionToolHandler.sm.start(context)
    }

    private fun reset() {
        insideState = null
        insideState = null
        originState = null
        transitionGhost = null
    }

    private fun insideState(context: EditInputEventContext): Boolean {
        insideState = context.editor.drawing.getDrawable { it is FSMState && it.contains(context.location) } as FSMState?
        return insideState != null
    }

    private fun displayHighlight(context: EditInputEventContext) {
        if (insideState != null) {
            highlight.updateForState(insideState!!)
            context.editor.view.animationContainer.add(highlight)
            highlight.validate()
        }
    }

    private fun hideHighlightedState(context: EditInputEventContext) {
        context.editor.view.animationContainer.remove(highlight)
        insideState = null
        context.editor.drawing.validate()
    }

    private fun displayTransitionGhost(context: EditInputEventContext) {
        transitionGhost = TransitionGhost(originState!!)
        context.editor.view.animationContainer.add(transitionGhost!!)
        transitionGhost!!.validate()
    }

    private fun hideTransitionGhost(context: EditInputEventContext) {
        if (transitionGhost != null) {
            context.editor.view.animationContainer.remove(transitionGhost!!)
            context.editor.view.drawing.validate()
            transitionGhost = null
        }
    }

    private fun addTransition(context: EditInputEventContext) {
        val transition = FSMTransition(originState!!.id, insideState!!.id)
        EditModule.drawingAppService.add(transition, context.editor.view)
        context.editor.toolDone()
    }
}