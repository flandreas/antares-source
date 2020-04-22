package ch.scorpion.jabbah.edit.model

import ch.scorpion.jabbah.edit.CommandManager
import ch.scorpion.jabbah.edit.UndoableDataHolder
import ch.scorpion.jabbah.io.Storable

class GenericUndoableDataHolder(
	state: Storable? = null,
	commandManager: CommandManager? = null
) : UndoableDataHolder {

	init {
		commandManager?.bindDataHolder(this)
	}

	private var state: Storable? = state

	override fun getUndoableState(): Storable? {
		return state
	}

	override fun setUndoableState(state: Storable) {
		this.state = state
	}
}