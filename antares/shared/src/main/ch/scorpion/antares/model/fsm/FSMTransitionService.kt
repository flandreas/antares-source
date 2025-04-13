package ch.scorpion.antares.model.fsm

import ch.scorpion.jabbah.base.EmptyHierarchyVisitor
import ch.scorpion.jabbah.base.dsl.*
import kotlin.math.max

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
    val maxValue: Long,
    val ast: Node
)

class FSMTransitionServiceImpl : FSMTransitionService {

    override fun parseTransitionCondition(condition: String): FSMTransitionConditionParseResult {
        val ast = FSMTransitionConditionParser(condition).parse()

        val inputNameCollector = InputNameCollector()
        ast.accept(inputNameCollector)

        val maxValueCollector = MaxValueCollector()
        ast.accept(maxValueCollector)

        return FSMTransitionConditionParseResult(inputNameCollector.inputNames, maxValueCollector.maxValue, ast)
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

    /**
     * Traverses the AST of the parsed [FSMTransition] condition and
     * collects the literal values occurring in all expressions, yielding the maximum
     * of these values.
     */
    private class MaxValueCollector : EmptyHierarchyVisitor() {

        var maxValue = 0L

        override fun visit(node: Any): Boolean {
            when (node) {
                is Literal -> maxValue = max(maxValue, node.token.value as Long)
            }
            return true
        }
    }
}