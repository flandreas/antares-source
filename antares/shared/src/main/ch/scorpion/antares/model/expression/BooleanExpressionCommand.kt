package ch.scorpion.antares.model.expression

import ch.scorpion.jabbah.edit.Undoable
import ch.scorpion.jabbah.edit.command.AbstractCommand

class BooleanExpressionCommand(
	private val ref: BooleanExpressionReference,
	private val expressions: String,
	private val singleCharIdentifier: Boolean
) : AbstractCommand("antares.command.booleanExpression", null), Undoable {

	private lateinit var oldValue: BooleanExpressionStorable

	override fun execute() {
		oldValue = BooleanExpressionStorable(ref.expressions.name.translation, ref.expressions.expressions, ref.expressions.singleCharIdentifier)
		ref.expressions = BooleanExpressionStorable(ref.expressions.name.translation, expressions, singleCharIdentifier)
	}

	override fun undo() {
		ref.expressions = oldValue
	}
}