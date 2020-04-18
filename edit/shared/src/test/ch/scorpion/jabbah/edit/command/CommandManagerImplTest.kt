package ch.scorpion.jabbah.edit.command

import ch.scorpion.jabbah.base.exception.IllegalStateException
import ch.scorpion.jabbah.edit.Command
import ch.scorpion.jabbah.edit.EditTestRule
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** Unit tests for [CommandManagerImpl].*/
class CommandManagerImplTest {

	init {
		EditTestRule.configure()
	}

	private val cmdManager = CommandManagerImpl()

	/** ---- Transactions */

	@Test
	fun shouldNotCommitNotExistingTransaction() {
		assertFailsWith<IllegalStateException> {
			cmdManager.commitTransaction()
		}
	}

	@Test
	fun shouldAutoCommitExecute() {
		val cmd = command()
		cmdManager.execute(cmd)
		verify { cmd.execute() }
		assertTrue(cmdManager.canUndo())
	}

	@Test
	fun shouldAutoCommitRegister() {
		val cmd = command()
		cmdManager.register(cmd)
		verify(exactly = 0) { cmd.execute() }
		assertTrue(cmdManager.canUndo())
	}

	@Test
	fun shouldDoSingleTransaction() {
		val cmd = command()
		cmdManager.beginTransaction(cmd)
		cmdManager.commitTransaction()
		verify { cmd.execute() }
		assertTrue(cmdManager.canUndo())
	}

	@Test
	fun shouldExecuteCommandInExistingTransaction() {
		val cmd1 = command()
		val cmd2 = command()
		cmdManager.beginTransaction(cmd1)
		cmdManager.execute(cmd2)
		cmdManager.commitTransaction()
		verify { cmd1.execute() }
		verify { cmd2.execute() }
		assertTrue(cmdManager.canUndo())
	}

	@Test
	fun shouldNotCommitInnerTransaction() {
		val cmd1 = command()
		val cmd2 = command()
		cmdManager.beginTransaction(cmd1)
		cmdManager.beginTransaction(cmd2)
		cmdManager.commitTransaction()
		verify { cmd1.execute() }
		verify { cmd2.execute() }
		assertFalse(cmdManager.canUndo())
	}

	@Test
	fun shouldCommitOuterTransaction() {
		val cmd1 = command()
		val cmd2 = command()
		cmdManager.beginTransaction(cmd1)
		cmdManager.beginTransaction(cmd2)
		cmdManager.commitTransaction()
		cmdManager.commitTransaction()
		verify { cmd1.execute() }
		verify { cmd2.execute() }
		assertTrue(cmdManager.canUndo())
	}

	/** ---- Simple undo/redo */

	@Test
	fun shouldUndo() {
		val cmdA = command("A")
		val cmdB = command("B")
		cmdManager.beginTransaction(cmdA)
		cmdManager.execute(cmdB)
		cmdManager.commitTransaction()

		cmdManager.undo()

		verify { cmdB.undo() }
		verify { cmdA.undo() }
		assertFalse(cmdManager.canUndo())
		assertTrue(cmdManager.canRedo())
	}

	@Test
	fun shouldRedo() {
		val cmdA = command("A")
		val cmdB = command("B")
		cmdManager.beginTransaction(cmdA)
		cmdManager.execute(cmdB)
		cmdManager.commitTransaction()
		cmdManager.undo()

		cmdManager.redo()

		verify(exactly = 2) { cmdA.execute() }
		verify(exactly = 2) { cmdB.execute() }
		assertTrue(cmdManager.canUndo())
		assertFalse(cmdManager.canRedo())
	}

	@Test
	fun shouldReset() {
		val cmd = command("A")
		cmdManager.beginTransaction(cmd)
		cmdManager.commitTransaction()

		cmdManager.reset()

		assertFalse(cmdManager.canUndo())
		assertFalse(cmdManager.canRedo())
	}

	/** ---- Checkpoints */

	@Test
	fun shouldNotBeUndoableWithNewCheckpoint() {
		cmdManager.execute(command("A"))

		cmdManager.openCheckpoint("CP")

		assertFalse(cmdManager.canUndo())
	}

	@Test
	fun shouldBeUndoableAfterUsingCheckpoint() {
		cmdManager.execute(command("A"))
		cmdManager.openCheckpoint("CP")

		cmdManager.execute(command("B"))

		assertTrue(cmdManager.canUndo())
	}

	@Test
	fun shouldPurgeCommandsWhenClosingCheckpoint() {
		val cmdA = command("A")
		val cmdB = command("B")
		cmdManager.execute(cmdA)
		cmdManager.openCheckpoint("CP")
		cmdManager.execute(cmdB)
		cmdManager.closeCheckpoint()

		cmdManager.undo()

		verify(exactly = 0) { cmdB.undo() }
		verify(exactly = 1) { cmdA.undo() }
	}

	/** ---- Helper methods */

	private fun command(desc: String = "Cmd"): Command {
		val command = mockk<Command>(relaxed = true)
		every { command.getDescription() } returns desc
		return command
	}
}