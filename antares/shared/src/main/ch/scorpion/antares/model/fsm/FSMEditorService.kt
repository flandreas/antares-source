package ch.scorpion.antares.model.fsm

import ch.scorpion.jabbah.base.geom.Geometry
import kotlin.math.PI
import kotlin.math.min

/**
 * A service to support creating and editing [FSMDrawings][FSMDrawing].
 */
interface FSMEditorService {

    fun createState(drawing: FSMDrawing): FSMState

    /** Returns all [FSMTransitions][FSMTransition] that are either outgoing of or incoming to [fsmState]. */
    fun getTransitions(fsmState: FSMState, drawing: FSMDrawing): List<FSMTransition>

    fun getState(id: Int, drawing: FSMDrawing): FSMState

    fun getOutgoingTransitions(fsmState: FSMState, drawing: FSMDrawing): List<FSMTransition>

    fun handleTransitionAdded(fsmTransition: FSMTransition, drawing: FSMDrawing)

    fun handleTransitionRemoved(fsmTransition: FSMTransition, drawing: FSMDrawing)

    /**
     * Called if a [FSMState] was updated (geometry, position) in order to update connected
     * [FSMTransition] accordingly.
     */
    fun handleStateUpdated(fsmState: FSMState, drawing: FSMDrawing)

    fun freeStateNumber(drawing: FSMDrawing): Int

    /**
     * Determines the optimal angle (in radians) for a new self-transition on [state].
     * Considers all [FSMTransitions][FSMTransition] connected to [state], and finds an angle in
     * the region with the most free space.
     */
    fun optimalSelfTransitionAngle(state: FSMState, drawing: FSMDrawing): Double
}

class FSMEditorServiceImpl : FSMEditorService {

    override fun createState(drawing: FSMDrawing): FSMState =
        FSMState().also { it.stateNumber = freeStateNumber(drawing) }

    override fun getTransitions(fsmState: FSMState, drawing: FSMDrawing): List<FSMTransition> =
        drawing
            .drawables
            .filterIsInstance<FSMTransition>()
            .filter { it.origStateId == fsmState.id || it.destinationStateId == fsmState.id }
            .toList()

    override fun getOutgoingTransitions(fsmState: FSMState, drawing: FSMDrawing): List<FSMTransition> =
        drawing
            .drawables
            .filterIsInstance<FSMTransition>()
            .filter { it.origStateId == fsmState.id}
            .toList()

    override fun handleTransitionAdded(fsmTransition: FSMTransition, drawing: FSMDrawing) {
        handleStateUpdated(getState(min(fsmTransition.origStateId, fsmTransition.destinationStateId), drawing), drawing)
    }

    override fun handleTransitionRemoved(fsmTransition: FSMTransition, drawing: FSMDrawing) {
        handleStateUpdated(getState(min(fsmTransition.origStateId, fsmTransition.destinationStateId), drawing), drawing)
    }

    override fun handleStateUpdated(fsmState: FSMState, drawing: FSMDrawing) {
        getTransitions(fsmState, drawing)
            .groupBy { it.otherStateThan(fsmState) }
            .entries
            .forEach { updateGeometry(it.value) }
    }

    override fun freeStateNumber(drawing: FSMDrawing): Int {
        val numbers = drawing.drawables.filterIsInstance<FSMState>().map { it.stateNumber }.toSet()
        var number = 0
        while (number in numbers) {
            number++
        }
        return number
    }

    override fun getState(id: Int, drawing: FSMDrawing): FSMState =
        drawing.getWithId(id) as FSMState

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

    override fun optimalSelfTransitionAngle(state: FSMState, drawing: FSMDrawing): Double {
        val angles = collectAngles(state, drawing).sorted()
        if (angles.isEmpty()) {
            return FSMTransition.DEF_SELF_TRANSITION_ANGLE
        }
        if (angles.size == 1) {
            return -(angles[0] - PI)
        }

        var maxRange = 0.0
        var range0 = 0.0
        for (i in angles.indices) {
            val range = if (i < angles.indices.last) {
                angles[i + 1] - angles[i]
            } else {
                // wrap around 0 angle
                2 * PI + angles[0] - angles[i]
            }
            if (range > maxRange) {
                range0 = angles[i]
                maxRange = range
            }
        }

        return -(range0 + maxRange / 2)
    }

    private fun collectAngles(state: FSMState, drawing: FSMDrawing): List<Double> {
        val result = mutableListOf<Double>()
        getTransitions(state, drawing).forEach { t ->
            if (t.isSelfTransition) {
                result.add(Geometry.angle(state.center, t.originPoint))
            } else {
                t.getConnectionPoint(state)?.let { cp ->
                    result.add(Geometry.angle(state.center, cp))
                }
            }
        }
        return result;
    }
}