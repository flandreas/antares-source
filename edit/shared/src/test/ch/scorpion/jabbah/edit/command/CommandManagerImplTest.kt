package ch.scorpion.jabbah.edit.command

import ch.scorpion.jabbah.base.TestTranslationsBuilder
import ch.scorpion.jabbah.base.exception.IllegalStateException
import ch.scorpion.jabbah.edit.Command
import ch.scorpion.jabbah.edit.EditTestRule
import com.nhaarman.mockitokotlin2.*
import org.junit.Assert.*
import org.hamcrest.CoreMatchers.`is`
import org.junit.Before
import org.junit.ClassRule
import org.junit.Test

/** Unit tests for [CommandManagerImpl].*/
class CommandManagerImplTest {

    companion object {
        @ClassRule
        @JvmField
        val editTestRule = EditTestRule()
    }

    @Before
    fun setup() {
        TestTranslationsBuilder().withAnyKey()
    }

    private val cmdManager = CommandManagerImpl()

    @Test(expected = IllegalStateException::class)
    fun shouldNotCommitInexistentTransaction() {
        cmdManager.commitTransaction()
    }

    @Test
    fun shouldAutoCommitExecute() {
        val cmd = command()
        cmdManager.execute(cmd)
        verify(cmd).execute()
        assertThat(cmdManager.canUndo(), `is`(true))
    }

    @Test
    fun shouldAutoCommitRegister() {
        val cmd = command()
        cmdManager.register(cmd)
        verify(cmd, never()).execute()
        assertThat(cmdManager.canUndo(), `is`(true))
    }

    @Test
    fun shouldDoSingleTransaction() {
        val cmd = command()
        cmdManager.beginTransaction(cmd)
        cmdManager.commitTransaction()
        verify(cmd).execute()
        assertThat(cmdManager.canUndo(), `is`(true))
    }

    @Test
    fun shouldExecuteCommandInExistingTransaction() {
        val cmd1 = command()
        val cmd2 = command()
        cmdManager.beginTransaction(cmd1)
        cmdManager.execute(cmd2)
        cmdManager.commitTransaction()
        verify(cmd1).execute()
        verify(cmd2).execute()
        assertThat(cmdManager.canUndo(), `is`(true))
    }

    @Test
    fun shouldNotCommitInnerTransation() {
        val cmd1 = command()
        val cmd2 = command()
        cmdManager.beginTransaction(cmd1)
        cmdManager.beginTransaction(cmd2)
        cmdManager.commitTransaction()
        verify(cmd1).execute()
        verify(cmd2).execute()
        assertThat(cmdManager.canUndo(), `is`(false))
    }

    @Test
    fun shouldCommitOuterTransation() {
        val cmd1 = command()
        val cmd2 = command()
        cmdManager.beginTransaction(cmd1)
        cmdManager.beginTransaction(cmd2)
        cmdManager.commitTransaction()
        cmdManager.commitTransaction()
        verify(cmd1).execute()
        verify(cmd2).execute()
        assertThat(cmdManager.canUndo(), `is`(true))
    }

    @Test
    fun shouldUndo() {
        val cmdA = command("A")
        val cmdB = command("B")
        cmdManager.beginTransaction(cmdA)
        cmdManager.execute(cmdB)
        cmdManager.commitTransaction()

        cmdManager.undo()

        verify(cmdB).undo()
        verify(cmdA).undo()
        assertThat(cmdManager.canUndo(), `is`(false))
        assertThat(cmdManager.canRedo(), `is`(true))
    }

    @Test
    fun shouldRedo() {
        val cmdA = command("A")
        val cmdB = command("B")
        cmdManager.beginTransaction(cmdA)
        cmdManager.execute(cmdB)
        cmdManager.commitTransaction()
        cmdManager.undo()
        reset(cmdA)
        reset(cmdB)

        cmdManager.redo()

        verify(cmdA).execute()
        verify(cmdB).execute()
        assertThat(cmdManager.canUndo(), `is`(true))
        assertThat(cmdManager.canRedo(), `is`(false))
    }

    @Test
    fun shouldReset() {
        val cmd = command("A")
        cmdManager.beginTransaction(cmd)
        cmdManager.commitTransaction()

        cmdManager.reset()

        assertThat(cmdManager.canUndo(), `is`(false))
        assertThat(cmdManager.canRedo(), `is`(false))
    }

	/** ---- Checkpoints */

	@Test
	fun shouldNotBeUndoableWithNewCheckpoint() {
		cmdManager.execute(command("A"))

		cmdManager.openCheckpoint("CP")

		assertThat(cmdManager.canUndo(), `is`(false))
	}

	@Test
	fun shouldBeUndoableAfterUsingCheckpoint() {
		cmdManager.execute(command("A"))
		cmdManager.openCheckpoint("CP")

		cmdManager.execute(command("B"))

		assertThat(cmdManager.canUndo(), `is`(true))
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

		verify(cmdB, never()).undo()
		verify(cmdA, times(1)).undo()
	}

	/** ---- Helper methods */

    private fun command(desc: String = "Cmd"): Command {
        val command = mock<Command>()
        whenever(command.getDescription()).thenReturn(desc)
        return command
    }
}