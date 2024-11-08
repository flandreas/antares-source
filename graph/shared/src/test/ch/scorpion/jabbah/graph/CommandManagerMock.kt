package ch.scorpion.jabbah.graph

import ch.scorpion.jabbah.edit.CommandManager
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock

/**
 * TODO Copy/paste of corresponding class in app module.
 * Resolve once test helpers can be reused from other modules with Kotlin MPP/gradle.
 */
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