package ch.scorpion.jabbah.edit.command

import ch.scorpion.jabbah.edit.Command
import ch.scorpion.jabbah.edit.EditTestRule
import org.junit.Assert.*
import org.junit.ClassRule
import org.junit.Test
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.reset
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import ch.scorpion.jabbah.base.exception.IllegalStateException
import org.hamcrest.CoreMatchers.`is`
import org.junit.Rule

/**
 * Unit tests for [CommandManagerImpl].
 */
class CommandManagerImplTest {

    companion object {
        @ClassRule @JvmField
        val editTestRule = EditTestRule()
    }

    private val cmdManager = CommandManagerImpl()

    @Test(expected = IllegalStateException::class)
    fun shouldNotAllowNestedTransaction() {
        cmdManager.beginTransaction(command("A"))
        cmdManager.beginTransaction(command("B"))
    }

    @Test(expected = IllegalStateException::class)
    fun shouldNotCommitInexistingTransaction() {
        cmdManager.commitTransaction()
    }

    @Test
    fun shouldBeginExecuteTransaction() {
        val cmd = command("A")
        cmdManager.beginTransaction(cmd)
        verify(cmd).execute()
        verify(cmd).validate()
    }

    @Test
    fun shouldBeginRegisterTransaction() {
        val cmd = command("A")
        cmdManager.beginTransaction(cmd, register = true)
        verify(cmd).registered()
    }

    @Test
    fun shouldNotBeUndoableBeforeCommit() {
        val cmd = command("A")
        cmdManager.beginTransaction(cmd)
        assertThat(cmdManager.canUndo(), `is`(false))
    }

    @Test
    fun shouldCommitTransaction() {
        val cmd = command("A")
        cmdManager.beginTransaction(cmd)
        cmdManager.commitTransaction()
        assertThat(cmdManager.canUndo(), `is`(true))
    }

    @Test
    fun shouldExecuteCommandInTransaction() {
        val cmdA = command("A")
        val cmdB = command("B")
        cmdManager.beginTransaction(cmdA)
        cmdManager.execute(cmdB)
        verify(cmdB).execute()
        verify(cmdB).validate()
    }

    @Test
    fun shouldRegisterCommandInTransaction() {
        val cmdA = command("A")
        val cmdB = command("B")
        cmdManager.beginTransaction(cmdA)
        cmdManager.register(cmdB)
        verify(cmdB).registered()
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

    private fun command(desc: String): Command {
        val command = mock<Command>()
        whenever(command.getDescription()).thenReturn(desc)
        return command
    }
}