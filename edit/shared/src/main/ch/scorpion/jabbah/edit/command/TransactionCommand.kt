package ch.scorpion.jabbah.edit.command

import ch.scorpion.jabbah.edit.Command
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.Undoable

/**
 * A [Command] implementation that does nothing, but serves only as a dummy [Command]
 * for holding inner transaction [Command]s.
 *
 * @param descriptionKey the translation key of the transaction's description
 * @property drawingView the [DrawingView] to validate, if any
 */
internal class TransactionCommand(
	descriptionKey: String,
	private val drawingView: DrawingView<*>? = null
) : AbstractCommand(descriptionKey, null), Undoable {

	override fun execute() {
		// empty
	}

	override fun undo() {
		// empty
	}

	override fun validate() {
		drawingView?.drawing?.validate()
	}
}