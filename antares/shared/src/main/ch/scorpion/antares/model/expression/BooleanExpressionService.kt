package ch.scorpion.antares.model.expression

import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.antares.model.truthtable.TruthTable
import ch.scorpion.antares.model.truthtable.TruthTableActivationRecord
import ch.scorpion.jabbah.base.EmptyHierarchyVisitor
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.dsl.*

/**
 * The result of parsing a list on individual boolean expression assignments.
 * Intended to be used for creating a [TruthTable] and calculating its output columns
 * according to the parsed output expressions.
 *
 * @param inputNames the list of all input variable names
 * @param outputs maps names of output variables to their result expression AST [Node] ready
 * to be interpreted by [BooleanExpressionInterpreter]
 */
data class BooleanExpressionParseResult(
	val inputNames: List<String>,
	val outputs: Map<String, Node>
)

/**
 * Provides domain-level methods for parsing [BooleanExpression]s, and for creating and
 * filling [TruthTable]s based on those [BooleanExpression]s.
 */
class BooleanExpressionService {

	/**
	 * Parses a list of boolean expressions..
	 * @param text the newline-separated expressions to be parsed
	 * @param notation the [BooleanExpressionNotation] used in [text]
	 * @throws DslError if parsing or semantic analysing fails
	 */
	fun parseExpressions(
		text: String,
		notation: BooleanExpressionNotation
	): BooleanExpressionParseResult {

		val ast = BooleanExpressionParser(expectAssignment = true, text).parse()
		val analyser = Analyser()
		ast.accept(analyser)

		return BooleanExpressionParseResult(analyser.inputNames, analyser.outputs)
	}

	/**
	 * Creates a [TruthTable] for the output created by [parseResult] and fills
	 * all output columns by interpreting the expression nodes in [BooleanExpressionParseResult].
	 */
	fun createTruthTable(parseResult: BooleanExpressionParseResult): TruthTable {
		val truthTable = TruthTable(
			inputColumnNames = parseResult.inputNames,
			outputColumnNames = parseResult.outputs.keys.toList()
		)
		val activationRecord = TruthTableActivationRecord(truthTable)

		parseResult.outputs.values.onEachIndexed { index, node ->
			fillOutputColumn(truthTable, activationRecord,truthTable.inputColumnCount + index, node)
		}

		return truthTable
	}

	/**
	 * Fills the output column with index [column] by interpreting the [expression] [Node]
	 * for every row in [truthTable]. Uses [activationRecord] to provide values for
	 * variables in [expression].
	 */
	private fun fillOutputColumn(
		truthTable: TruthTable,
		activationRecord: TruthTableActivationRecord,
		column: Int,
		expression: Node
	) {
		val memory = Memory(activationRecord)
		val interpreter = BooleanExpressionInterpreter(expression, memory)

		(0 until truthTable.rowsCount).forEach { row ->
			activationRecord.currentRow = row
			truthTable.setValue(row, column, Bit.of(interpreter.interpret() as Boolean))
		}
	}

	/**
	 * Used for parsing [BooleanExpression]s by extracting information about input names, output names
	 * and expression [Node]s. Also some semantic checks.
	 *
	 * @throws SemanticError if a semantic check fails
	 */
	private class Analyser : EmptyHierarchyVisitor() {

		val inputNames = mutableListOf<String>()
		val outputs = mutableMapOf<String, Node>()

		private var assignedOutputName: String? = null

		override fun visitEnter(node: Any): Boolean {
			when (node) {
				is Assignment -> {
					val outputName = node.left.token.value as String
					if (outputs.keys.contains(outputName)) {
						throw SemanticError(node.left.location, Translations.getString("antares.booleanExpression.outputAlreadyDefined.msg"))
					} else if (inputNames.contains(outputName)) {
						throw SemanticError(node.left.location, Translations.getString("antares.booleanExpression.outputAsInput.msg"))
					} else {
						outputs[outputName] = node.right
						assignedOutputName = outputName
					}
				}
			}
			return true
		}

		override fun visitLeave(node: Any): Boolean {
			when (node) {
				is Assignment -> assignedOutputName = null
			}
			return true
		}

		override fun visit(node: Any): Boolean {
			when (node) {
				is Variable -> {
					val name = node.token.value!!
					if (outputs.keys.contains(name)) {
						if (name != assignedOutputName) {
							throw SemanticError(
								node.location,
								Translations.getString("antares.booleanExpression.outputAsInput.msg")
							)
						}
					} else if (!inputNames.contains(name)) {
						inputNames.add(name)
					}
				}
			}
			return true
		}
	}
}