package ch.scorpion.jabbah.edit

import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock

class CommandManagerMock {

	private val commandManager = mock<CommandManager>(MockMode.autofill)

	init {
		cannotUndo()
	}

	fun canUndo(): CommandManagerMock {
		every { commandManager.canUndo() } returns true
		return this
	}

	fun cannotUndo(): CommandManagerMock {
		every { commandManager.canUndo() } returns false
		return this
	}

	fun build(): CommandManager = commandManager
}