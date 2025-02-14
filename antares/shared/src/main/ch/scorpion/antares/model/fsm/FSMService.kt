package ch.scorpion.antares.model.fsm

import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import kotlin.math.min

interface FSMService {

    /** Returns all [FSMTransitions][FSMTransition] that are either outgoing of or incoming to [fsmState]. */
    fun getTransitions(fsmState: FSMState, drawing: Drawing<Component>): List<FSMTransition>

    fun handleTransitionAdded(fsmTransition: FSMTransition, drawing: Drawing<Component>)

    fun handleTransitionRemoved(fsmTransition: FSMTransition, drawing: Drawing<Component>)

    /**
     * Called if a [FSMState] was updated (geometry, position) in order to update connected
     * [FSMTransition] accordingly.
     */
    fun handleStateUpdated(fsmState: FSMState, drawing: Drawing<Component>)
}

class FSMServiceImpl : FSMService {

    override fun getTransitions(fsmState: FSMState, drawing: Drawing<Component>): List<FSMTransition> =
        drawing
            .drawables
            .filterIsInstance<FSMTransition>()
            .filter { it.origStateId == fsmState.id || it.destinationStateId == fsmState.id }
            .toList()

    override fun handleTransitionAdded(fsmTransition: FSMTransition, drawing: Drawing<Component>) {
        handleStateUpdated(getState(min(fsmTransition.origStateId, fsmTransition.destinationStateId), drawing), drawing)
    }

    override fun handleTransitionRemoved(fsmTransition: FSMTransition, drawing: Drawing<Component>) {
        handleStateUpdated(getState(min(fsmTransition.origStateId, fsmTransition.destinationStateId), drawing), drawing)
    }

    override fun handleStateUpdated(fsmState: FSMState, drawing: Drawing<Component>) {
        getTransitions(fsmState, drawing)
            .groupBy { it.otherStateThan(fsmState) }
            .entries
            .forEach { updateGeometry(it.value) }
    }

    private fun updateGeometry(transitions: List<FSMTransition>) {
        if (transitions.isEmpty()) {
            return
        }

        /**
         * Determines how far an [FSMTransition] is positioned away from the straight line.
         * 0 means straight, - means one side, + means the other side. The sequence is 0,
         */
        var level = if (transitions.size == 1) 0 else 1

        /**
         * Determines on which side of the straight line the [FSMTransition] is positioned.
         * 0 means straight, - means one side, + means the other side. The resulting sequence
         * (together with level) is 0, 1, -1, 2, -2 and so on.
         */
        var factor = 1

        /** Used to avoid that two [FSMTransition] A->B and B->A are located on each other.*/
        var firstOrig = transitions[0].origStateId

        transitions.forEach { fsmTransition ->
            val sign = if (fsmTransition.origStateId == firstOrig) 1 else -1
            fsmTransition.updateGeometry(level * factor * sign)
            if (factor == 1) {
                factor = -1
            } else {
                level++
                factor = 1
            }
        }
    }

    private fun getState(id: Int, drawing: Drawing<Component>): FSMState =
        drawing.getWithId(id) as FSMState
}