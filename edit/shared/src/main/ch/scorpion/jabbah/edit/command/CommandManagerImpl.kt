package ch.scorpion.jabbah.edit.command

import ch.scorpion.jabbah.base.collection.Stack
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.exception.IllegalStateException
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.Command
import ch.scorpion.jabbah.edit.CommandEvent
import ch.scorpion.jabbah.edit.CommandManager
import ch.scorpion.jabbah.base.logger

/**
 * Standard implementation of the [CommandManager] interface.
 */
class CommandManagerImpl(override val eventBus: EventBus) : CommandManager {

    constructor(): this(BaseModule.eventBus)

    private val LOG by logger()

    private val undoStack = Stack<CommandTransaction>()

    private val redoStack = Stack<CommandTransaction>()

    /** Holds the current [CommandTransaction].*/
    private var transaction: CommandTransaction? = null

    /** ---- [CommandManager] interface */

    override fun beginTransaction(command: Command, register: Boolean) {
        if (transaction != null) {
            throw IllegalStateException("nested transactions not supported")
        }
        LOG.debug("CommandManagerImpl: begin transaction '${command.getDescription()}'")
        transaction = CommandTransaction()
        transaction!!.add(command)
        transaction?.let {
            if (register) {
                command.registered()
            } else {
                command.execute()
                command.validate()
            }
        }
    }

    override fun execute(command: Command) {
        if (transaction == null) {
            throw IllegalStateException("no transaction")
        }
        LOG.debug("CommandManagerImpl: execute command '${command.getDescription()}'")
        transaction?.let {
            it.add(command)
            command.execute()
            command.validate()
        }
    }

    override fun register(command: Command) {
        if (transaction == null) {
            throw IllegalStateException("no transaction")
        }
        LOG.debug("CommandManagerImpl: register command '${command.getDescription()}'")
        transaction?.let {
            it.add(command)
            command.registered()
        }
    }

    override fun commitTransaction() {
        if (transaction == null) {
            throw IllegalStateException("no transaction")
        }
        LOG.debug("CommandManagerImpl: commit transaction '${transaction!!.headCommand.getDescription()}'")
        undoStack.push(transaction!!)
        eventBus.post(CommandEvent(this))
        transaction = null
    }

    override fun rollbackTransaction() {
        throw UnsupportedOperationException("not implemented")
    }

    override fun canUndo(): Boolean {
        return !undoStack.empty
    }

    override fun canRedo(): Boolean {
        return !redoStack.empty
    }

    override fun getUndoDescription(): String? {
        if (!canUndo()) {
            return null
        }
        return undoStack.peek().headCommand.getDescription()
    }

    override fun getRedoDescription(): String? {
        if (!canRedo()) {
            return null
        }
        return redoStack.peek().headCommand.getDescription()
    }

    override fun undo() {
        if (!canUndo()) {
            throw IllegalStateException("no undoable transaction")
        }
        val undoTransaction = undoStack.pop()
        redoStack.push(undoTransaction)

        undoTransaction.undo()
        eventBus.post(CommandEvent(this))
    }

    override fun redo() {
        if (!canRedo()) {
            throw IllegalStateException("no redoable transaction")
        }
        val redoTransaction = redoStack.pop()
        undoStack.push(redoTransaction)

        redoTransaction.execute()
        eventBus.post(CommandEvent(this))
    }

    override fun reset() {
        undoStack.clear()
        redoStack.clear()
        eventBus.post(CommandEvent(this))
    }

    /** ---- [CommandManagerImpl] */

    private class CommandTransaction {

        val headCommand: Command get() = commands.first()

        private val commands = mutableListOf<Command>()

        fun add(command: Command) {
            commands.add(command)
            command.addedToTransaction()
        }

        fun execute() {
            for (c in commands) {
                c.execute()
                c.validate()
            }
        }

        fun undo() {
            for (c in commands.reversed()) {
                c.undo()
                c.validate()
            }
        }
    }
}