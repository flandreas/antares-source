package io.antarescircuit.antares.model.fsm

import io.antarescircuit.antares.model.gate.CurrentUndefinedGateInputBehavior
import io.antarescircuit.antares.model.signal.DigitalSignal
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.dsl.*
import io.antarescircuit.jabbah.base.dsl.DslTokenType.*
import io.antarescircuit.jabbah.base.parser.TextLocation
import io.antarescircuit.jabbah.base.parser.TokenType

class FSMTransitionConditionInterpreter(
    rootNode: Node,
    memory: Memory = Memory()
) : AbstractBaseInterpreter(rootNode, memory) {

    constructor(parser: FSMTransitionConditionParser) : this(parser.parse())

    override fun interpret(node: Node): Any {
        return when (node) {
            is BinaryOperation -> binaryOperation(node)
            is Literal -> literal(node)
            is Variable -> variable(node)
            else -> super.interpret(node)
        }
    }

    private fun binaryOperation(node: BinaryOperation): Any =
        binaryOpInterpreted(node.location, node.op.type, interpret(node.left), interpret(node.right))

    private fun literal(node: Literal): Any = node.token.value!!

    private fun variable(variable: Variable): Any= memory.getValue(variable)

    private fun binaryOpInterpreted(l: TextLocation, type: TokenType, left: Any, right: Any): Any =
        when (type) {
            EQUAL -> equalL(left, right, l)
            AND -> andL(left, right, l)
            OR -> orL(left, right, l)
            else -> throw SyntaxError(l, Translations.getString("base.dsl.unknownBinaryOperation.msg", type.id))
        }

    private fun equalL(l: Any, r: Any, loc: TextLocation): Any =
        when (l) {
            is Long -> equalR(l, r, loc)
            is Float -> equalR(l, r, loc)
            is DigitalSignal -> equalR(l, r, loc)
            else -> throwIncompatibleTypes(loc, EQUAL)
        }

    private fun equalR(l: Long, r: Any, loc: TextLocation): Any =
        when (r) {
            is Long -> if (l == r) 1L else 0L
            is Float -> if (l.toFloat() == r) 1L else 0L
            is DigitalSignal -> if (l == signalToLong(r)) 1L else 0L
            else -> throwIncompatibleTypes(loc, EQUAL)
        }

    private fun equalR(l: Float, r: Any, loc: TextLocation): Any =
        when (r) {
            is Long -> if (l == r.toFloat()) 1L else 0L
            is Float -> if (l == r) 1L else 0L
            is DigitalSignal -> if (l == signalToLong(r).toFloat()) 1L else 0L
            else -> throwIncompatibleTypes(loc, EQUAL)
        }

    private fun equalR(l: DigitalSignal, r: Any, loc: TextLocation): Any =
        when (r) {
            is DigitalSignal -> if (l.toLong() == r.toLong()) 1L else 0L
            is Long -> if (signalToLong(l) == r) 1L else 0L
            else -> throwIncompatibleTypes(loc, EQUAL)
        }

    private fun andL(l: Any, r: Any, loc: TextLocation): Any =
        when (l) {
            is Long -> andR(l, r, loc)
            is DigitalSignal -> andR(l, r, loc)
            else -> throwIncompatibleTypes(loc, AND)
        }

    private fun andR(l: Long, r: Any, loc: TextLocation): Any =
        when (r) {
            is Long -> l.and(r)
            is DigitalSignal -> l.and(signalToLong(r))
            else -> throwIncompatibleTypes(loc, AND)
        }

    private fun andR(l: DigitalSignal, r: Any, loc: TextLocation): Any =
        when (r) {
            is DigitalSignal -> l.and(r)
            is Long -> l.and(r.toULong())
            else -> throwIncompatibleTypes(loc, AND)
        }

    private fun orL(l: Any, r: Any, loc: TextLocation): Any =
        when (l) {
            is Long -> orR(l, r, loc)
            is DigitalSignal -> orR(l, r, loc)
            else -> throwIncompatibleTypes(loc, OR)
        }

    private fun orR(l: Long, r: Any, loc: TextLocation): Any =
        when (r) {
            is Long -> l.or(r)
            is DigitalSignal -> l.or(signalToLong(r))
            else -> throwIncompatibleTypes(loc, OR)
        }

    private fun orR(l: DigitalSignal, r: Any, loc: TextLocation): Any =
        when (r) {
            is DigitalSignal -> l.or(r)
            is Long -> l.or(r.toULong())
            else -> throwIncompatibleTypes(loc, OR)
        }

    private fun throwIncompatibleTypes(l: TextLocation, type: TokenType): Nothing {
        throw RuntimeError(l, Translations.getString("base.dsl.incompatibleTypes.msg", type.id))
    }

    private fun signalToLong(signal: DigitalSignal): Long =
        signal.toLong()?.toLong() ?: CurrentUndefinedGateInputBehavior.value.definedValue(signal.bitWidth).toLong()!!.toLong()
}