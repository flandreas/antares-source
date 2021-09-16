package ch.scorpion.jabbah.base.dsl

import ch.scorpion.jabbah.base.dsl.TokenType.*

/**
 * Interprets an AST according to the grammar parsed by [Parser].
 */
open class Interpreter(
	protected val node: Node,
	protected val memory: Memory = Memory()
) {

	constructor(parser: Parser): this(parser.parse())
	constructor(program: String): this(Parser(program))

	protected var params: Any? = null
		private set

	/** Set by "return" statement to the expression to be returned and immediately quit interpretation.*/
	private var returnValue: Any? = null

	/**
	 * Runs the program defined by the AST in [node].
	 * @param params the optional parameters on which execution logic might depend on. The
	 * values of these parameters might be different for every call of [interpret].
	 */
	fun interpret(params: Any? = null): Any {
		returnValue = null
		this.params = params
		try {
			return interpret(node)
		} finally {
			// Don't clear memory BEFORE interpretation in order not to break Memory.preset()
			memory.clear()
		}
	}

	protected open fun interpret(node: Node): Any {
		return when (node) {
			is Block -> block(node)
			is Compound -> compound(node)
			is NoOp -> 0L
			is UnaryOperation -> unaryOperation(node)
			is BinaryOperation -> binaryOperation(node)
			is Literal -> literal(node)
			is Assignment -> assignment(node)
			is Variable -> variable(node)
			is Declaration -> declaration(node)
			is IfStatement -> ifStatement(node)
			is WhenStatement -> whenStatement(node)
			is ForStatement -> forStatement(node)
			is ReturnStatement -> returnStatement(node)
			else -> throw SyntaxError(node.location, "Unknown AST node '${node::class.simpleName}'")
		}
	}

	protected fun interpretAsLong(node: Node): Long {
		val result = interpret(node)
		if (result !is Long) {
			throw RuntimeError(node.location, "Not a number")
		}
		return result
	}

	private fun compound(node: Compound): Any {
		var result: Any = 0L
		node.children.forEach { child ->
			result = interpret(child)
			returnValue?.let { return it }
		}
		return result
	}

	private fun block(node: Block): Any {
		memory.enterScope("block")
		val result = compound(node)
		memory.exitScope(node)
		return result
	}

	private fun binaryOperation(node: BinaryOperation): Any {
		return when (node.op.type) {
			PLUS -> typedBinaryOp(node)
			MINUS -> interpretAsLong(node.left) - interpretAsLong(node.right)
			MULTIPLY -> interpretAsLong(node.left) * interpretAsLong(node.right)
			DIVIDE -> interpretAsLong(node.left) / interpretAsLong(node.right)
			EQUAL -> typedBinaryOp(node)
			DIFF -> typedBinaryOp(node)
			AND -> typedBinaryOp(node)
			OR -> typedBinaryOp(node)
			SMALLER -> typedBinaryOp(node)
			GREATER -> typedBinaryOp(node)
			SMALLER_EQUAL -> typedBinaryOp(node)
			GREATER_EQUAL -> typedBinaryOp(node)
			SHIFT_LEFT -> typedBinaryOpWithRightInt(node)
			SHIFT_RIGHT -> typedBinaryOpWithRightInt(node)
			MOD -> typedBinaryOp(node)
			else -> throw SyntaxError(node.location, "Unknown binary operation '${node.op.type.name}'")
		}
	}

	protected open fun typedBinaryOp(node: BinaryOperation): Any {
		return when (node.op.type) {
			AND -> binaryOp(node) { l, r -> l.and(r) }
			OR -> binaryOp(node) { l, r -> l.or(r) }
			SMALLER -> binaryOp(node) { l, r -> if (l < r) 1L else 0L }
			SMALLER_EQUAL -> binaryOp(node) { l, r -> if (l <= r) 1L else 0L }
			GREATER -> binaryOp(node) { l, r -> if (l > r) 1L else 0L }
			GREATER_EQUAL -> binaryOp(node) { l, r -> if (l >= r) 1L else 0L }
			PLUS -> binaryOp(node) { l, r -> l + r }
			EQUAL -> binaryOp(node) { l, r -> if (l == r) 1L else 0L }
			DIFF -> binaryOp(node) { l, r -> if (l != r) 1L else 0L }
			MOD -> binaryOp(node) { l, r -> l.mod(r) }
			else -> throw SyntaxError(node.location, "Unknown binary operation '${node.op.type.name}'")
		}
	}

	private fun binaryOp(
		node: BinaryOperation,
		longOp: (Long, Long) -> Long
	): Any {
		val left = interpret(node.left)
		val right = interpret(node.right)
		return if (left is Long && right is Long) {
			longOp(left, right)
		} else {
			throw RuntimeError(node.location, "Incompatible types for '${node.op.type}'")
		}
	}

	protected open fun typedBinaryOpWithRightInt(node: BinaryOperation): Any =
		when (node.op.type) {
			SHIFT_LEFT -> binaryOpWithRightInt(node) { l, r -> l.shl(r) }
			SHIFT_RIGHT -> binaryOpWithRightInt(node)  { l, r -> l.shr(r) }
			else -> throw SyntaxError(node.location, "Unknown binary operation '${node.op.type.name}'")
		}

	private fun binaryOpWithRightInt(
		node: BinaryOperation,
		longOp: (Long, Int) -> Long,
	): Any {
		val left = interpret(node.left)
		val right = interpretAsLong(node.right)
		return when (left) {
			is Long -> longOp(left, right.toInt())
			else -> throw RuntimeError(node.location, "Incompatible type for '${node.op.type}'")
		}
	}

	private fun literal(node: Literal): Any = node.token.value!!

	private fun unaryOperation(node: UnaryOperation): Any =
		when (node.op.type) {
			PLUS -> +interpretAsLong(node.expr)
			MINUS -> -interpretAsLong(node.expr)
			NOT -> typedUnaryOp(node)
			else -> throw SyntaxError(node.location, "Unknown unary operation '${node.op.type.name}'")
		}

	protected open fun typedUnaryOp(node: UnaryOperation): Any =
		when (node.op.type) {
			NOT -> unaryOp(node) { it.inv() }
			else -> throw SyntaxError(node.location, "Unknown unary operation '${node.op.type.name}'")
		}

	private fun unaryOp(
		node: UnaryOperation,
		longOp: (Long) -> Long,
	): Any {
		val value = interpret(node.expr)
		return when (value) {
			is Long -> longOp(value)
			else -> throw RuntimeError(node.location, "Incompatible type for '${node.op.type}'")
		}
	}

	protected open fun storeValue(variable: Variable, value: Any): Any {
		if (variable is AssocArray) {
			var assocArray = memory.getOptionalValue(variable) as MutableMap<Any, Any>?
			if (assocArray == null) {
				memory.setValue(variable, mutableMapOf(variable.token.value to value))
			} else {
				assocArray[variable.key] = value
			}
		} else {
			memory.setValue(variable, value)
		}
		return value
	}

	protected open fun loadValue(variable: Variable): Any {
		return if (variable is AssocArray) {
			val assocArray = memory.getValue(variable) as MutableMap<Any, Any>
			assocArray[variable.token.value!!] ?: throw RuntimeError(node.location, "No value for key")
		} else {
			memory.getValue(variable)
		}
	}

	/** Also supports implicit declaration. */
	private fun assignment(node: Assignment): Any {
		if (!memory.isDefined(node.left)) {
			memory.define(node.left, inStore = false)
		}
		return storeValue(node.left, interpret(node.right))
	}

	private fun variable(node: Variable): Any =
		loadValue(node)

	private fun declaration(node: Declaration): Any {
		return if (node.store) {
			declarationInStore(node)
		} else {
			declarationInScope(node)
		}
	}

	private fun declarationInStore(node: Declaration): Any {
		memory.define(node.left, inStore = true)
		val value = node.right?.let { interpret(it) }
		value?.let { storeValue(node.left, it) }
		return value ?: 0L
	}

	private fun declarationInScope(node: Declaration): Any {
		if (!memory.isLocallyDefined(node.left)) {
			memory.define(node.left, inStore = false)
		}
		val value = node.right?.let { interpret(it) }
		value?.let { storeValue(node.left, it) }
		return value ?: 0L
	}

	private fun ifStatement(node: IfStatement): Any {
		if (evaluateTrueCondition(interpret(node.condition))) {
			return interpret(node.thenStatement)
		}
		return node.elseStatement?.let { interpret(it) } ?: 0L
	}

	protected open fun evaluateTrueCondition(value: Any): Boolean = value != 0L

	private fun whenStatement(node: WhenStatement): Any {
		val expr = interpret(node.expression)
		for (clause in node.clauses) {
			if (clause.condition == null || evaluateTrueCondition(evaluateEqualCondition(clause, expr, interpret(clause.condition) ))) {
				return interpret(clause.then)
			}
		}
		return 0L
	}

	protected open fun evaluateEqualCondition(node: Node, left: Any, right: Any): Long = if (left == right) 1L else 0L

	private fun forStatement(node: ForStatement): Any {
		val startValue = interpretAsLong(node.inExpr)
		val endValue = interpretAsLong(node.toExpr)

		memory.enterScope("for")
		memory.define(node.variable, inStore = false)

		if (startValue <= endValue) {
			for (value in startValue..endValue) {
				memory.setValue(node.variable, value)
				interpret(node.statement)
				returnValue?.let { return it }
			}
		} else {
			for (value in startValue downTo endValue) {
				memory.setValue(node.variable, value)
				interpret(node.statement)
				returnValue?.let { return it }
			}
		}

		memory.exitScope(node)
		return 0L
	}

	private fun returnStatement(node: ReturnStatement): Any {
		returnValue = node.expr?.let { interpret(it) } ?: 0
		return returnValue!!
	}
}