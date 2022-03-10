package ch.scorpion.antares.model.expression

import ch.scorpion.jabbah.edit.Undoable
import ch.scorpion.jabbah.edit.command.AbstractCommand

class BooleanExpressionCommand(
	private val ref: BooleanExpressionReference,
	private val expressions: String
) : AbstractCommand("antares.command.booleanExpression", null), Undoable {

	private lateinit var oldValue: String

	override fun execute() {
		oldValue = ref.expressions.expressions
		ref.expressions = BooleanExpressionStorable(expressions)
	}

	override fun undo() {
		ref.expressions = BooleanExpressionStorable(oldValue)
	}
}