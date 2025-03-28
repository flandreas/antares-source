package ch.scorpion.antares.model.fsm

import ch.scorpion.jabbah.base.EmptyHierarchyVisitor
import ch.scorpion.jabbah.base.dsl.*

/**
 * A service for parsing [FSMTransition] conditions.
 */
interface FSMTransitionService {

    /** Parses the condition of a [FSMTransition]. */
    fun parseTransitionCondition(condition: String): FSMTransitionConditionParseResult
}

/**
 * Contains the result of parsing a [FSMTransition] condition.
 * @property variableNames the collected variable names
 * @property ast the abstract syntax tree of the parsed condition
 */
data class FSMTransitionConditionParseResult(
    val variableNames: Set<String>,
    val ast: Node
)

class FSMTransitionServiceImpl : FSMTransitionService {

    override fun parseTransitionCondition(condition: String): FSMTransitionConditionParseResult {
        val ast = FSMTransitionConditionParser(condition).parse()

        val inputNameCollector = InputNameCollector()
        ast.accept(inputNameCollector)

        return FSMTransitionConditionParseResult(inputNameCollector.inputNames, ast)
    }

    /**
     * Traverses the AST (abstract syntax tree) of a parsed [FSMTransition] condition and
     * collects the variable names occurring in all expressions to interpret them as
     * FSM input names.
     */
    private class InputNameCollector : EmptyHierarchyVisitor() {

        /** Contains the collected input names.*/
        val inputNames = mutableSetOf<String>()

        override fun visitEnter(node: Any): Boolean {
            when (node) {
                is BinaryOperation -> {
                    if (node.left is Variable) {
                        inputNames.add((node.left as Variable).token.value as String)
                    }
                }
            }
            return true
        }
    }
}