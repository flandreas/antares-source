package ch.scorpion.jabbah.app

import ch.scorpion.jabbah.edit.CommandManager
import io.mockk.every
import io.mockk.mockk

/** TODO Move to the edit package once test helpers can be reused from other modules with Kotlin MPP/gradle. */
class CommandManagerMock {

	private val commandManager = mockk<CommandManager>(relaxed = true)

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