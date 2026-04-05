package io.antarescircuit.jabbah.base.dsl

import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.dsl.DslTokenType.*
import io.antarescircuit.jabbah.base.logger
import io.antarescircuit.jabbah.base.parser.TextLocation
import io.antarescircuit.jabbah.base.parser.TokenType
import kotlin.math.pow

typealias InterpreterFactory = (node: Node, memory: Memory) -> Interpreter

/**
 * Interprets an AST according to the grammar parsed by [DslParser].
 */
open class Interpreter(
	rootNode: Node,
	memory: Memory = Memory()
) : AbstractBaseInterpreter(rootNode, memory) {

	companion object {
		private val LOG by logger(Interpreter::class)
	}

	constructor(parser: DslParser): this(parser.parse())
	constructor(program: String): this(DslParser(program))

	override fun interpret(node: Node): Any {
		try {
			return when (node) {
				is Block -> block(node)
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
				is FunctionCall -> functionCall(node, params)
				else -> super.interpret(node)
			}
		} catch (e: DslError) {
			throw e
		} catch (e: Throwable) {
			LOG.error("Unexpected error in script interpretation", e)
			throw RuntimeError(node.location, Translations.getString("base.dsl.systemError.msg"))
		}
	}

	private fun interpretAsLong(node: Node): Long {
		val result = interpret(node)
		if (result !is Long) {
			throw RuntimeError(node.location, Translations.getString("base.dsl.expectedNumber.msg"))
		}
		return result
	}

	private fun block(node: Block): Any {
		memory.enterScope("block")
		val result = compound(node)
		memory.exitScope(node)
		return result
	}

	protected fun throwIncompatibleTypes(l: TextLocation, type: TokenType): Nothing {
		throw RuntimeError(l, Translations.getString("base.dsl.incompatibleTypes.msg", type.id))
	}

	protected open fun binaryOperation(node: BinaryOperation): Any =
		binaryOpInterpreted(node.location, node.op.type, interpret(node.left), interpret(node.right))

	protected fun binaryOpInterpreted(l: TextLocation, type: TokenType, left: Any, right: Any): Any =
		when (type) {
			PLUS -> addL(left, right, l)
			MINUS -> subtractL(left, right, l)
			MULTIPLY -> multiplyL(left, right, l)
			DIVIDE -> divideL(left, right, l)
			CARET -> powerL(left, right, l)
			EQUAL -> equalL(left, right, l)
			DIFF -> if (equalL(left, right, l) == 1L) 0L else 1L
			SMALLER -> smallerL(left, right, l)
			SMALLER_EQUAL -> if (smallerL(left, right, l) == 1L || equalL(left, right, l) == 1L) 1L else 0L
			GREATER -> greaterL(left, right, l)
			GREATER_EQUAL -> if (greaterL(left, right, l) == 1L || equalL(left, right, l) == 1L) 1L else 0L
			AND -> andL(left, right, l)
			OR -> orL(left, right, l)
			SHIFT_LEFT -> shiftLeftL(left, right, l)
			SHIFT_RIGHT -> shiftRightL(left, right, l)
			MOD -> modL(left, right, l)
			else -> throw SyntaxError(l, Translations.getString("base.dsl.unknownBinaryOperation.msg", type.id))
		}

	protected open fun addL(l: Any, r: Any, loc: TextLocation): Any =
		when (l) {
			is Long -> addR(l, r, loc)
			is Float -> addR(l, r, loc)
			else -> throwIncompatibleTypes(loc, PLUS)
		}

	protected open fun addR(l: Long, r: Any, loc: TextLocation): Any =
		when (r) {
			is Long -> l + r
			is Float -> l + r
			else -> throwIncompatibleTypes(loc, PLUS)
		}

	protected open fun addR(l: Float, r: Any, loc: TextLocation): Any =
		when (r) {
			is Long -> l + r
			is Float -> l + r
			else -> throwIncompatibleTypes(loc, PLUS)
		}

	protected open fun subtractL(l: Any, r: Any, loc: TextLocation): Any =
		when (l) {
			is Long -> subtractR(l, r, loc)
			is Float -> subtractR(l, r, loc)
			else -> throwIncompatibleTypes(loc, MINUS)
		}

	protected open fun subtractR(l: Long, r: Any, loc: TextLocation): Any =
		when (r) {
			is Long -> l - r
			is Float -> l - r
			else -> throwIncompatibleTypes(loc, MINUS)
		}

	protected open fun subtractR(l: Float, r: Any, loc: TextLocation): Any =
		when (r) {
			is Long -> l - r
			is Float -> l - r
			else -> throwIncompatibleTypes(loc, MINUS)
		}

	protected open fun multiplyL(l: Any, r: Any, loc: TextLocation): Any =
		when (l) {
			is Long -> multiplyR(l, r, loc)
			is Float -> multiplyR(l, r, loc)
			else -> throwIncompatibleTypes(loc, MULTIPLY)
		}

	protected open fun multiplyR(l: Long, r: Any, loc: TextLocation): Any =
		when (r) {
			is Long -> l * r
			is Float -> l * r
			else -> throwIncompatibleTypes(loc, MULTIPLY)
		}

	protected open fun multiplyR(l: Float, r: Any, loc: TextLocation): Any =
		when (r) {
			is Long -> l * r
			is Float -> l * r
			else -> throwIncompatibleTypes(loc, MULTIPLY)
		}

	protected open fun divideL(l: Any, r: Any, loc: TextLocation): Any =
		when (l) {
			is Long -> divideR(l, r, loc)
			is Float -> divideR(l, r, loc)
			else -> throwIncompatibleTypes(loc, DIVIDE)
		}

	protected open fun divideR(l: Long, r: Any, loc: TextLocation): Any =
		when (r) {
			is Long -> l / r
			is Float -> l / r
			else -> throwIncompatibleTypes(loc, DIVIDE)
		}

	protected open fun divideR(l: Float, r: Any, loc: TextLocation): Any =
		when (r) {
			is Long -> l / r
			is Float -> l / r
			else -> throwIncompatibleTypes(loc, DIVIDE)
		}

	protected open fun powerL(l: Any, r: Any, loc: TextLocation): Any =
		when (l) {
			is Long -> powerR(l, r, loc)
			is Float -> powerR(l, r, loc)
			else -> throwIncompatibleTypes(loc, CARET)
		}

	protected open fun powerR(l: Long, r: Any, loc: TextLocation): Any =
		when (r) {
			is Long -> l.toDouble().pow(r.toInt()).toLong()
			is Float -> l.toDouble().pow(r.toDouble()).toFloat()
			else -> throwIncompatibleTypes(loc, CARET)
		}

	protected open fun powerR(l: Float, r: Any, loc: TextLocation): Any =
		when (r) {
			is Long -> l.toDouble().pow(r.toInt()).toFloat()
			is Float -> l.toDouble().pow(r.toDouble()).toFloat()
			else -> throwIncompatibleTypes(loc, CARET)
		}

	protected open fun equalL(l: Any, r: Any, loc: TextLocation): Any =
		when (l) {
			is Long -> equalR(l, r, loc)
			is Float -> equalR(l, r, loc)
			else -> throwIncompatibleTypes(loc, EQUAL)
		}

	protected open fun equalR(l: Long, r: Any, loc: TextLocation): Any =
		when (r) {
			is Long -> if (l == r) 1L else 0L
			is Float -> if (l.toFloat() == r) 1L else 0L
			else -> throwIncompatibleTypes(loc, EQUAL)
		}

	protected open fun equalR(l: Float, r: Any, loc: TextLocation): Any =
		when (r) {
			is Long -> if (l == r.toFloat()) 1L else 0L
			is Float -> if (l == r) 1L else 0L
			else -> throwIncompatibleTypes(loc, EQUAL)
		}

	protected open fun smallerL(l: Any, r: Any, loc: TextLocation): Any =
		when (l) {
			is Long -> smallerR(l, r, loc)
			is Float -> smallerR(l, r, loc)
			else -> throwIncompatibleTypes(loc, SMALLER)
		}

	protected open fun smallerR(l: Long, r: Any, loc: TextLocation): Any =
		when (r) {
			is Long -> if (l < r) 1L else 0L
			is Float -> if (l < r) 1L else 0L
			else -> throwIncompatibleTypes(loc, SMALLER)
		}

	protected open fun smallerR(l: Float, r: Any, loc: TextLocation): Any =
		when (r) {
			is Long -> if (l < r) 1L else 0L
			is Float -> if (l < r) 1L else 0L
			else -> throwIncompatibleTypes(loc, SMALLER)
		}

	protected open fun greaterL(l: Any, r: Any, loc: TextLocation): Any =
		when (l) {
			is Long -> greaterR(l, r, loc)
			is Float -> greaterR(l, r, loc)
			else -> throwIncompatibleTypes(loc, GREATER)
		}

	protected open fun greaterR(l: Long, r: Any, loc: TextLocation): Any =
		when (r) {
			is Long -> if (l > r) 1L else 0L
			is Float -> if (l > r) 1L else 0L
			else -> throwIncompatibleTypes(loc, GREATER)
		}

	protected open fun greaterR(l: Float, r: Any, loc: TextLocation): Any =
		when (r) {
			is Long -> if (l > r) 1L else 0L
			is Float -> if (l > r) 1L else 0L
			else -> throwIncompatibleTypes(loc, GREATER)
		}

	protected open fun andL(l: Any, r: Any, loc: TextLocation): Any =
		when (l) {
			is Long -> andR(l, r, loc)
			else -> throwIncompatibleTypes(loc, AND)
		}

	protected open fun andR(l: Long, r: Any, loc: TextLocation): Any =
		when (r) {
			is Long -> l.and(r)
			else -> throwIncompatibleTypes(loc, AND)
		}

	protected open fun orL(l: Any, r: Any, loc: TextLocation): Any =
		when (l) {
			is Long -> orR(l, r, loc)
			else -> throwIncompatibleTypes(loc, OR)
		}

	protected open fun orR(l: Long, r: Any, loc: TextLocation): Any =
		when (r) {
			is Long -> l.or(r)
			else -> throwIncompatibleTypes(loc, OR)
		}

	protected open fun shiftLeftL(l: Any, r: Any, loc: TextLocation): Any =
		when (l) {
			is Long -> shiftLeftR(l, r, loc)
			else -> throwIncompatibleTypes(loc, SHIFT_LEFT)
		}

	protected open fun shiftLeftR(l: Long, r: Any, loc: TextLocation): Any =
		when (r) {
			is Long -> l.shl(r.toInt())
			else -> throwIncompatibleTypes(loc, SHIFT_LEFT)
		}

	protected open fun shiftRightL(l: Any, r: Any, loc: TextLocation): Any =
		when (l) {
			is Long -> shiftRightR(l, r, loc)
			else -> throwIncompatibleTypes(loc, SHIFT_RIGHT)
		}

	protected open fun shiftRightR(l: Long, r: Any, loc: TextLocation): Any =
		when (r) {
			is Long -> l.shr(r.toInt())
			else -> throwIncompatibleTypes(loc, SHIFT_RIGHT)
		}

	protected open fun modL(l: Any, r: Any, loc: TextLocation): Any =
		when (l) {
			is Long -> modR(l, r, loc)
			else -> throwIncompatibleTypes(loc, MOD)
		}

	protected open fun modR(l: Long, r: Any, loc: TextLocation): Any =
		when (r) {
			is Long -> l.mod(r)
			else -> throwIncompatibleTypes(loc, MOD)
		}

	private fun literal(node: Literal): Any = node.token.value!!

	private fun unaryOperation(node: UnaryOperation): Any =
		unaryOpInterpreted(node.location, node.op.type, interpret(node.expr))

	private fun unaryOpInterpreted(l: TextLocation, type: TokenType, value: Any): Any =
		when (type) {
			PLUS -> plus(value, l)
			MINUS -> minus(value, l)
			NOT -> not(value, l)
			else -> throw SyntaxError(l, Translations.getString("base.dsl.unknownUnaryOperation.msg", type.id))
		}

	protected open fun plus(value: Any, loc: TextLocation): Any =
		when (value) {
			is Long -> value
			is Float -> value
			else -> throwIncompatibleTypes(loc, PLUS)
		}

	protected open fun minus(value: Any, loc: TextLocation): Any =
		when (value) {
			is Long -> -value
			is Float -> -value
			else -> throwIncompatibleTypes(loc, MINUS)
		}

	protected open fun not(value: Any, loc: TextLocation): Any =
		when (value) {
			is Long -> value.inv()
			else -> throwIncompatibleTypes(loc, NOT)
		}

	protected open fun storeValue(variable: Variable, value: Any): Any {
		if (variable is AssocArray) {
			val assocArray = memory.getOptionalValue(variable)
			val key = interpretAssocArrayKey(variable)
			if (assocArray == null) {
				memory.setValue(variable, mutableMapOf(key to value))
			} else {
				if (assocArray !is MutableMap<*, *>) {
					throw RuntimeError(rootNode.location, Translations.getString("base.dsl.expectedArray.msg", variable.token.value!!))
				}
				@Suppress("UNCHECKED_CAST")
				(assocArray as MutableMap<Any, Any>)[key] = value
			}
		} else {
			memory.setValue(variable, value)
		}
		return value
	}

	protected open fun loadValue(variable: Variable): Any {
		return if (variable is AssocArray) {
			val assocArray = memory.getValue(variable)
			if (assocArray !is MutableMap<*,*>) {
				throw RuntimeError(rootNode.location, Translations.getString("base.dsl.expectedArray.msg", variable.token.value!!))
			}
			val key = interpretAssocArrayKey(variable)
			assocArray[key] ?: throw RuntimeError(rootNode.location, Translations.getString("base.dsl.noArrayKeyValue.msg"))
		} else {
			memory.getValue(variable)
		}
	}

	/**
	 * Currently only [Long] supported as assoc array keys to avoid problems with
	 * seemingly equal values, but different classes.
	 */
	protected open fun interpretAssocArrayKey(variable: AssocArray): Long {
		val key = interpret(variable.key)
		if (key !is Long) {
			throw RuntimeError(variable.location, Translations.getString("base.dsl.arrayIndexMustBeNumeric.msg"))
		}
		return key
	}

	private fun assignment(node: Assignment): Any {
		if (!memory.isDefined(node.left)) {
			throw RuntimeError(node.left.location, Translations.getString("base.dsl.variableNotDefined.msg", node.left.token.value!!))
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

	protected open fun evaluateTrueCondition(value: Any): Boolean {
		// Tuning
		return when (value) {
			is Long -> value != 0L
			is ULong -> value != 0UL
			else -> value != 0L
		}
	}

	private fun whenStatement(node: WhenStatement): Any {
		val expr = interpret(node.expression)
		for (clause in node.clauses) {
			if (clause.condition == null || evaluateTrueCondition(equalL(expr, interpret(clause.condition), clause.location))) {
				return interpret(clause.then)
			}
		}
		return 0L
	}

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

	private fun functionCall(node: FunctionCall, context: Any?): Any {
		if (node.function == null) {
			throw RuntimeError(node.location, Translations.getString("base.dsl.noImplementationOfFunction.msg", node.name.value!!))
		}
		try {
			return node.function!!.function.execute(node.params.map { interpret(it) }, context)
		} catch (e: RuntimeError) {
			// Catch and rethrow with CodeLocation to avoid passing CodeLocation as argument of the execute() method
			throw RuntimeError(node.location, e.message!!)
		}
	}
}