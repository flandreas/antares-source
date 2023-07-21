package ch.scorpion.antares.model.expression

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.dsl.*

class BooleanExpressionInterpreter(
	rootNode: Node,
	memory: Memory = Memory()
) : AbstractBaseInterpreter(rootNode, memory) {

	constructor(parser: BooleanExpressionParser): this(parser.parse())
	constructor(expectAssignment: Boolean, program: String): this(BooleanExpressionParser(expectAssignment, program))

	private val _assignedVariables = mutableListOf<String>()

	val assignedVariables: List<String> get() = _assignedVariables

	override fun interpret(node: Node): Any {
		return when (node) {
			is Literal -> literal(node)
			is Variable -> variable(node)
			is UnaryOperation -> unaryOperation(node)
			is BinaryOperation -> binaryOperation(node)
			is Assignment -> assignment(node)
			else -> super.interpret(node)
		}
	}

	private fun variable(node: Variable): Boolean = memory.getValue(node) as Boolean

	private fun literal(node: Literal): Boolean = node.token.value as Boolean

	private fun unaryOperation(node: UnaryOperation): Boolean =
		when (node.op.type) {
			DslTokenType.NOT -> !(interpret(node.expr) as Boolean)
			else -> throw SyntaxError(node.location, Translations.getString("base.dsl.unknownUnaryOperation.msg", node.op.type.id))
		}

	private fun binaryOperation(node: BinaryOperation): Boolean =
		when (node.op.type) {
			DslTokenType.AND -> (interpret(node.left) as Boolean) && (interpret(node.right) as Boolean)
			DslTokenType.OR -> (interpret(node.left) as Boolean) || (interpret(node.right) as Boolean)
			else -> throw SyntaxError(node.location, Translations.getString("base.dsl.unknownBinaryOperation.msg", node.op.type.id))
		}

	private fun assignment(node: Assignment): Boolean {
		_assignedVariables.add(node.left.token.value!!)
		return interpret(node.right) as Boolean
	}
}