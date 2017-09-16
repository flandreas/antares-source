package ch.scorpion.jabbah.edit.command

import ch.scorpion.jabbah.base.collection.Stack
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.exception.IllegalStateException
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.Command
import ch.scorpion.jabbah.edit.CommandEvent
import ch.scorpion.jabbah.edit.CommandManager
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.edit.DrawingView

/**
 * Standard implementation of the [CommandManager] interface.
 */
class CommandManagerImpl(override val eventBus: EventBus) : CommandManager {

    constructor(): this(BaseModule.eventBus)

    private val LOG by logger(CommandManagerImpl::class)

    private val undoStack = Stack<CommandTransaction>()

    private val redoStack = Stack<CommandTransaction>()

    /** Holds the current [CommandTransaction].*/
    private var transaction: CommandTransaction? = null

    private var level: Int = 0

    /** ---- [CommandManager] interface */

    override fun beginTransaction(command: Command, register: Boolean) {
        if (transaction == null) {
            transaction = CommandTransaction()
        }
        level++
        transaction?.let {
            it.add(command)
            if (register) {
                command.registered()
            } else {
                command.execute()
                command.validate()
            }
        }
    }

    override fun beginTransaction(descriptionKey: String, drawingView: DrawingView<*>?) {
        beginTransaction(TransactionCommand(descriptionKey, drawingView), true)
    }

    override fun execute(command: Command) {
        if (transaction == null) {
            beginTransaction(command, register = false)
            commitTransaction()
        } else {
            transaction!!.add(command)
            command.execute()
            command.validate()
        }
    }

    override fun register(command: Command) {
        if (transaction == null) {
            beginTransaction(command, register = true)
            commitTransaction()
        } else {
            transaction!!.add(command)
            command.validate()
        }
    }

    override fun commitTransaction() {
        if (transaction == null) {
            throw IllegalStateException("no transaction")
        }
        level--
        if (level == 0) {
            LOG.debug("CommandManagerImpl: commit transaction '${transaction!!.headCommand.getDescription()}'")
            undoStack.push(transaction!!)
            eventBus.post(CommandEvent(this))
            transaction = null
        }
    }

    override fun rollbackTransaction() {
        if (transaction == null) {
            throw IllegalStateException("no transaction")
        }
        transaction!!.undo()
        level = 0
        transaction = null
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
        if (transaction != null) {
            throw IllegalStateException("cannot reset while in transaction")
        }
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

    /**
     * A [Command] implementation that does nothing, but serves only as a dummy [Command]
     * for holding inner transaction [Command]s.
     *
     * @param descriptionKey the translation key of the transaction's description
     * @property drawingView the [DrawingView] to validate, if any
     */
    private class TransactionCommand(
            descriptionKey: String,
            private val drawingView: DrawingView<*>? = null
    ) : AbstractCommand(descriptionKey, null) {

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
}