package io.antarescircuit.jabbah.edit.command

import io.antarescircuit.jabbah.edit.Command
import io.antarescircuit.jabbah.edit.DrawingView
import io.antarescircuit.jabbah.edit.Undoable

/**
 * A [Command] implementation that does nothing, but serves only as a dummy [Command]
 * for holding inner transaction [Command]s.
 *
 * @param descriptionKey the translation key of the transaction's description
 * @property drawingView the [DrawingView] to validate, if any
 */
internal class TransactionCommand(
	descriptionKey: String,
	private val drawingView: DrawingView<*,*>? = null
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