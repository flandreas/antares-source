package io.antarescircuit.jabbah.edit.model

import io.antarescircuit.jabbah.edit.CommandManager
import io.antarescircuit.jabbah.edit.UndoableDataHolder
import io.antarescircuit.jabbah.io.Storable

class GenericUndoableDataHolder(
	state: Storable? = null,
	commandManager: CommandManager? = null,
	private val changeHandler: (Storable) -> Unit = {}
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
		changeHandler(state)
	}

	override fun undoableStateEstablished(state: Storable) {}
}