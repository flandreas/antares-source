package ch.scorpion.antares.model.expression

import ch.scorpion.jabbah.edit.Undoable
import ch.scorpion.jabbah.edit.command.AbstractCommand

class BooleanExpressionsCommand(
	private val ref: BooleanExpressionReference,
	private val expressions: String
) : AbstractCommand("antares.command.booleanExpression", null), Undoable {

	private lateinit var oldValue: String

	override fun execute() {
		oldValue = ref.expressions.expressions
		ref.updateExpressions(expressions)
	}

	override fun undo() {
		ref.updateExpressions(oldValue)
	}
}

class BooleanExpressionsSingleCharCommand(
	private val ref: BooleanExpressionReference,
	private val singleChar: Boolean
) : AbstractCommand("antares.command.booleanExpression", null), Undoable {

	private var oldValue: Boolean = false

	override fun execute() {
		oldValue = ref.expressions.singleCharIdentifier
		ref.updateSingleChar(singleChar)
	}

	override fun undo() {
		ref.updateSingleChar(oldValue)
	}
}