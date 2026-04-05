package io.antarescircuit.antares.view.fsm

import io.antarescircuit.antares.model.fsm.FSMDrawing
import io.antarescircuit.antares.model.fsm.FSMState
import io.antarescircuit.antares.model.fsm.FSMTransition
import io.antarescircuit.antares.model.module.AntaresModelModule
import io.antarescircuit.jabbah.base.Status
import io.antarescircuit.jabbah.base.StatusType
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.logger
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.base.state.StateMachine
import io.antarescircuit.jabbah.base.state.UnhandledEventBehaviour
import io.antarescircuit.jabbah.base.state.stateMachine
import io.antarescircuit.jabbah.draw.Drawable
import io.antarescircuit.jabbah.draw.DrawableContainer
import io.antarescircuit.jabbah.draw.StateMachineInputEventHandler
import io.antarescircuit.jabbah.draw.StateMachineInputEventHandler.Companion.mouseDragged
import io.antarescircuit.jabbah.draw.StateMachineInputEventHandler.Companion.mouseLeftPressed
import io.antarescircuit.jabbah.draw.StateMachineInputEventHandler.Companion.mouseLeftReleased
import io.antarescircuit.jabbah.draw.StateMachineInputEventHandler.Companion.mouseMoved
import io.antarescircuit.jabbah.draw.graphics.Cursor
import io.antarescircuit.jabbah.edit.CommandEvent
import io.antarescircuit.jabbah.edit.EditInputEventContext
import io.antarescircuit.jabbah.edit.module.EditModule

/**
 * Provides a [StateMachine] for interactively creating a [FSMTransition].
 */
object FSMTransitionToolHandler {

    private val LOG by logger(FSMTransitionToolHandler::class)

    private val highlight = FSMStateHighlight()

    private var insideState: FSMState? = null

    private var originState: FSMState? = null

    private var transitionGhost: TransitionGhost? = null

    /** The [DrawableContainer] where [transitionGhost] has been added. */
    private var transitionGhostContainer: DrawableContainer<Drawable>? = null

    val fsmTransitionToolHandler = StateMachineInputEventHandler(
        stateMachine<EditInputEventContext>(UnhandledEventBehaviour.Unhandled) {

            state("sense") {
                onEntry {
                    it.view.setCursor(Cursor.DEFAULT)
                    Status.set(StatusType.Tool, Translations.getString("antares.fsm.transitionTool.sense"))
                }
                transitTo("insideOrigin") {
                    given { mouseMoved(it) && insideState(it) }
                }
            }

            state("insideOrigin") {
                onEntry {
                    displayHighlight(it)
                    it.view.setCursor(Cursor.CROSSHAIR)
                    Status.set(StatusType.Tool, Translations.getString("antares.fsm.transitionTool.insideOrigin"))
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
                onEntry {
                    Status.set(StatusType.Tool, Translations.getString("antares.fsm.transitionTool.drag"))
                }
                transitTo("sense") {
                    given { mouseLeftReleased(it) }
                    onTransit {
                        hideTransitionGhost()
                        Status.set(StatusType.Tool, null)
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
                    Status.set(StatusType.Tool, Translations.getString("antares.fsm.transitionTool.insideDestination"))
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
                        hideTransitionGhost()
                        addTransition(it)
                        hideHighlightedState(it)
                    }
                }
            }
        }
    )

    init {
        BaseModule.eventBus.register(CommandEvent::class) { hideTransitionGhost() }
    }

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
        transitionGhostContainer = context.editor.view.animationContainer
        transitionGhostContainer!!.add(transitionGhost!!)
        transitionGhost!!.validate()
    }

    private fun hideTransitionGhost() {
        if (transitionGhost != null && transitionGhostContainer != null) {
            transitionGhostContainer!!.remove(transitionGhost!!)
            transitionGhostContainer!!.validate()
            transitionGhost = null
            transitionGhostContainer = null
        }
    }

    private fun addTransition(context: EditInputEventContext) {
        val transition = if (originState!!.id == insideState!!.id) {
            FSMTransition(
                originState!!.id, insideState!!.id,
                AntaresModelModule.fsmEditorService.optimalSelfTransitionAngle(
                    originState!!,
                    context.drawingView.drawing as FSMDrawing)
            )
        } else {
            FSMTransition(originState!!.id, insideState!!.id)
        }
        EditModule.drawingAppService.add(transition, context.editor.view)
        context.editor.toolDone()
    }
}