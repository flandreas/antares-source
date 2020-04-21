package ch.scorpion.jabbah.edit.command

import ch.scorpion.jabbah.base.collection.Stack
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.exception.IllegalStateException
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.edit.*

/**
 * Standard implementation of the [CommandManager] interface.
 */
class CommandManagerImpl(
	override val eventBus: EventBus = BaseModule.eventBus
) : CommandManager {

	companion object {
		private val LOG by logger(CommandManagerImpl::class)
		private const val DEFAULT_STATE_NAME = "default"
	}

	private class State(val name: String) {

		val undoStack = Stack<CommandTransaction>()

		val redoStack = Stack<CommandTransaction>()

		/** Holds the current [CommandTransaction].*/
		var transaction: CommandTransaction? = null

		/** The level of transaction stacking.*/
		var level: Int = 0
	}

	private val states = Stack<State>();

	private val state: State get() = states.peek()

	init {
		states.push(State(DEFAULT_STATE_NAME))
	}

	/** ---- [CommandManager] interface */

	override var active: Boolean = true
		set(value) {
			if (value != field) {
				field = value
				eventBus.post(CommandManagerActiveEvent(this))
			}
		}

	override val applicationDataChanged: Boolean
		get() = state.undoStack.items.reversed().firstOrNull { it.changesApplicationData } != null

	override fun beginTransaction(command: Command, register: Boolean) {
		if (state.transaction == null) {
			state.transaction = CommandTransaction()
		}
		state.level++
		state.transaction?.let {
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
		if (state.transaction == null) {
			beginTransaction(command, register = false)
			commitTransaction()
		} else {
			state.transaction!!.add(command)
			command.execute()
			command.validate()
		}
	}

	override fun register(command: Command) {
		if (state.transaction == null) {
			beginTransaction(command, register = true)
			commitTransaction()
		} else {
			state.transaction!!.add(command)
			command.validate()
		}
	}

	override fun commitTransaction() {
		if (state.transaction == null) {
			throw IllegalStateException("no transaction")
		}
		state.level--
		if (state.level == 0) {
			LOG.debug("commit transaction '${state.transaction!!.headCommand.getDescription()}'")
			state.undoStack.push(state.transaction!!)
			eventBus.post(CommandEvent(this))
			state.transaction = null
		}
	}

	override fun rollbackTransaction() {
		if (state.transaction == null) {
			throw IllegalStateException("no transaction")
		}
		state.transaction!!.undo()
		state.level = 0
		state.transaction = null
	}

	override fun canUndo(): Boolean {
		return !state.undoStack.empty
	}

	override fun canRedo(): Boolean {
		return !state.redoStack.empty
	}

	override fun getUndoDescription(): String? {
		if (!canUndo()) {
			return null
		}
		return state.undoStack.peek().headCommand.getDescription()
	}

	override fun getRedoDescription(): String? {
		if (!canRedo()) {
			return null
		}
		return state.redoStack.peek().headCommand.getDescription()
	}

	override fun undo() {
		if (!canUndo()) {
			throw IllegalStateException("no undoable transaction")
		}
		val undoTransaction = state.undoStack.pop()
		state.redoStack.push(undoTransaction)

		undoTransaction.undo()
		eventBus.post(CommandEvent(this))
	}

	override fun redo() {
		if (!canRedo()) {
			throw IllegalStateException("no redoable transaction")
		}
		val redoTransaction = state.redoStack.pop()
		state.undoStack.push(redoTransaction)

		redoTransaction.execute()
		eventBus.post(CommandEvent(this))
	}

	override fun reset() {
		if (state.transaction != null) {
			throw IllegalStateException("cannot reset while in transaction")
		}
		state.undoStack.clear()
		state.redoStack.clear()
		eventBus.post(CommandEvent(this))
	}

	override fun openCheckpoint(name: String) {
		LOG.debug("opening checkpoint '$name'")
		states.push(State(name))
		eventBus.post(CommandEvent(this))
	}

	override fun closeCheckpoint() {
		if (states.size < 2) {
			throw IllegalStateException("no checkpoint to close")
		}
		LOG.debug("closing checkpoint ${states.peek().name}")
		states.pop()
		eventBus.post(CommandEvent(this))
	}

	/** ---- [CommandManagerImpl] */

	private class CommandTransaction {

		private val commands = mutableListOf<Command>()

		val headCommand: Command get() = commands.first()

		val changesApplicationData: Boolean get() = commands.find { it.changesApplicationData } != null

		fun add(command: Command) {
			commands.add(command)
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