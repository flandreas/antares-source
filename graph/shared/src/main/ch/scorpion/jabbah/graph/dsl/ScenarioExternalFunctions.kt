package ch.scorpion.jabbah.graph.dsl

import ch.scorpion.jabbah.base.dsl.*
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.parser.TextLocation
import ch.scorpion.jabbah.execution.actor.ActorState
import ch.scorpion.jabbah.graph.view.GraphView

object GraphScenarioExternalFunctions : ScenarioExternalFunctions()

open class ScenarioExternalFunctions(
    private val delegate: GraphViewExternalFunctions = GraphDslModule.graphViewExternalFunctionsFactory()
) : DslExternalFunctions {

    fun bind(
        graphView: GraphView,
        origin: String,
        context: String,
        eventBus: EventBus = BaseModule.eventBus
    ) {
        delegate.bind(graphView, origin, context, eventBus)
    }

    override fun defineIn(symbolTable: SymbolTable) {
        with(symbolTable) {
            define(ExternalFunctionSymbol("haveAllState", 1, ::haveAllStateImpl))
        }
    }

    /**
     * Checks if all graph elements have a particular simulation state.
     *
     * @param state the required state with the following supported values:
     * - 0: Idle
     * @return 1 if all graph elements have state [state], 0 otherwise
     */
    private fun haveAllState(state: Long): Long {
        val actorState = when(state) {
            0L -> ActorState.Idle
            else -> throw RuntimeError(TextLocation.UNDEFINED, "Unsupported state $state")
        }
        return if (delegate.graphView.graph?.allElementHaveState(actorState) == true) 1L else 0L
    }

    private fun haveAllStateImpl(params: List<Any>, @Suppress("UNUSED_PARAMETER") context: Any? = null): Any =
        haveAllState(longParam(0, params))
}