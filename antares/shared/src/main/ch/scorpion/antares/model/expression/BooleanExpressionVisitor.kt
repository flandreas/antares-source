package ch.scorpion.antares.model.expression

import ch.scorpion.jabbah.base.EmptyHierarchyVisitor
import ch.scorpion.jabbah.base.dsl.*

/**
 * Base class for implementing visitors of a [BooleanExpression] [Node]
 * that transform node visits to type-specific handler method calls.
 *
 * @property isNotPostfix `true` if NOT operators are to be visited as postfix, i.e. after to
 * the negated factor
 */
abstract class AbstractBooleanExpressionVisitor(
	private val isNotPostfix: Boolean = false
) : EmptyHierarchyVisitor() {

	abstract fun handleAnd()
	abstract fun handleOr()
	abstract fun handleConstant(value: Boolean)
	abstract fun handleNot()
	abstract fun handleVariable(name: String)
	abstract fun handleCompound(begin: Boolean)

	override fun visitEnter(node: Any): Boolean {
		when (node) {
			is UnaryOperation -> {
				when (node.op.type) {
					DslTokenType.NOT -> if (!isNotPostfix) {
						handleNot()
					}
					else -> { }
				}
			}
			is Compound<*> -> handleCompound(begin = true)
		}
		return true
	}

	override fun visit(node: Any): Boolean {
		when (node) {
			is Variable -> {
				handleVariable(node.token.value as String)
			}
			is Literal -> {
				when (node.token.value) {
					true -> handleConstant(true)
					false -> handleConstant(false)
					else -> throw IllegalStateException("unsupported literal ${node.token.value}")
				}
			}

		}
		return true
	}

	override fun visitInfix(node: Any, child: Any): Boolean {
		when (node) {
			is BinaryOperation -> {
				when (node.op.type) {
					DslTokenType.OR -> handleOr()
					DslTokenType.AND -> handleAnd()
					else -> throw IllegalStateException("unsupported binary operation ${node.op.type}")
				}
			}
		}
		return true
	}

	override fun visitLeave(node: Any): Boolean {
		when (node) {
			is UnaryOperation -> {
				when (node.op.type) {
					DslTokenType.NOT -> if (isNotPostfix) {
						handleNot()
					}
					else -> throw IllegalStateException("unsupported unary operation ${node.op.type}")
				}
			}
			is Compound<*> -> handleCompound(begin = false)
		}
		return true
	}
}

/** An implementation of [AbstractBooleanExpressionVisitor] with empty handler method implementations. */
@Suppress("unused")
open class EmptyBooleanExpressionVisitor(
	isNotPostfix: Boolean = false
) : AbstractBooleanExpressionVisitor(isNotPostfix) {
	override fun handleAnd() { }
	override fun handleOr() { }
	override fun handleConstant(value: Boolean) { }
	override fun handleNot() { }
	override fun handleVariable(name: String) { }
	override fun handleCompound(begin: Boolean) { }
}