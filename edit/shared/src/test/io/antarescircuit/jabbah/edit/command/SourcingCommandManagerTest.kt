package io.antarescircuit.jabbah.edit.command

import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.edit.EditTestRule
import io.antarescircuit.jabbah.io.*
import kotlin.test.*

/** Unit tests for [SourcingCommandManager]. */
class SourcingCommandManagerTest {

	private val app: ApplicationDummy
	private var cmdManager: SourcingCommandManager

	init {
		Translations.withAnyKey()
		EditTestRule.configure()
		IOModule.typeMap.register("storableString", StorableString::class)

		app = ApplicationDummy()
		cmdManager = SourcingCommandManager()

		cmdManager.bindDataHolder(app)
	}

	/** ---- Initialization and querying tests */

	@Test
	fun shouldNotCreateSnapshotWithoutData() {
		app.data = null
		cmdManager.reset()
	}

	@Test
	fun shouldResetWhenBindingDataHolder() {
		assertEquals(1, cmdManager.snapshotCount)
	}

	@Test
	fun shouldCreateSnapshotOnReset() {
		cmdManager.reset()
		assertEquals(1, cmdManager.snapshotCount)
		assertEquals(0, cmdManager.redoSnapshotCount)
	}

	@Test
	fun shouldGetUndoDescription() {
		cmdManager.execute(AppendCommand(app, "a"))
		assertEquals("anyDescription", cmdManager.getUndoDescription())
	}

	@Test
	fun shouldGetRedoDescription() {
		cmdManager.execute(AppendCommand(app, "a"))
		cmdManager.undo()

		assertEquals("anyDescription", cmdManager.getRedoDescription())
	}

	/** ---- Simple undo/redo tests */

	@Test
	fun shouldExecute() {
		cmdManager.execute(AppendCommand(app, "a"))
		cmdManager.execute(AppendCommand(app, "b"))
		cmdManager.execute(AppendCommand(app, "c"))

		assertEquals("abc", app.mandatoryData.value)
	}

	@Test
	fun shouldUndo() {
		cmdManager.execute(AppendCommand(app, "abc"))

		cmdManager.undo()

		assertEquals("", app.mandatoryData.value)
		assertEquals(1, cmdManager.snapshotCount)
		assertEquals(0, cmdManager.redoSnapshotCount)
	}

	@Test
	fun shouldRedo() {
		cmdManager.execute(AppendCommand(app, "abc"))
		cmdManager.undo()

		cmdManager.redo()

		assertEquals("abc", app.mandatoryData.value)
		assertEquals(1, cmdManager.snapshotCount)
		assertEquals(0, cmdManager.redoSnapshotCount)
	}

	@Test
	fun shouldCreateAdditionalSnapshot() {
		BaseModule.properties.set(SourcingCommandManager.PROP_MAX_COMMAND_COUNT_PER_SNAPSHOT, 2)
		cmdManager = SourcingCommandManager()
		cmdManager.bindDataHolder(app)

		cmdManager.execute(AppendCommand(app, "a"))
		cmdManager.execute(AppendCommand(app, "b"))
		cmdManager.execute(AppendCommand(app, "c"))

		assertEquals(2, cmdManager.snapshotCount)
	}

	@Test
	fun shouldUndoWithAdditionalSnapshot() {
		BaseModule.properties.set(SourcingCommandManager.PROP_MAX_COMMAND_COUNT_PER_SNAPSHOT, 2)
		cmdManager = SourcingCommandManager()
		cmdManager.bindDataHolder(app)

		cmdManager.execute(AppendCommand(app, "a"))
		cmdManager.execute(AppendCommand(app, "b"))
		cmdManager.execute(AppendCommand(app, "c"))

		cmdManager.undo()
		assertEquals("ab", app.mandatoryData.value)

		cmdManager.undo()
		assertEquals("a", app.mandatoryData.value)

		cmdManager.undo()
		assertEquals("", app.mandatoryData.value)

		assertEquals(1, cmdManager.snapshotCount)
		assertEquals(1, cmdManager.redoSnapshotCount)
	}

	@Test
	fun shouldRedoWithAdditionalSnapshot() {
		BaseModule.properties.set(SourcingCommandManager.PROP_MAX_COMMAND_COUNT_PER_SNAPSHOT, 2)
		cmdManager = SourcingCommandManager()
		cmdManager.bindDataHolder(app)

		cmdManager.execute(AppendCommand(app, "a"))
		cmdManager.execute(AppendCommand(app, "b"))
		cmdManager.execute(AppendCommand(app, "c"))

		cmdManager.undo()
		cmdManager.undo()
		cmdManager.undo()

		cmdManager.redo()
		assertEquals("a", app.mandatoryData.value)

		cmdManager.redo()
		assertEquals("ab", app.mandatoryData.value)

		cmdManager.redo()
		assertEquals("abc", app.mandatoryData.value)

		assertEquals(2, cmdManager.snapshotCount)
		assertEquals(0, cmdManager.redoSnapshotCount)
	}

	@Test
	fun shouldNotExecuteRegister() {
		app.mandatoryData.append("a")

		cmdManager.register(AppendCommand(app, "a"))

		assertEquals("a", app.mandatoryData.value)
		assertTrue(cmdManager.canUndo())
	}

	@Test
	fun shouldUseSnapshotForNonUndoableCommand() {
		val oldData = app.data
		cmdManager.execute(AppendCommand(app, "abc"))

		cmdManager.undo()

		assertNotSame(oldData, app.data)
	}

	@Test
	fun shouldNotUseSnapshotForUndoableCommand() {
		val oldData = app.data
		cmdManager.execute(UndoableAppendCommand(app, "abc"))

		cmdManager.undo()

		assertSame(oldData, app.data)
	}

	@Test
	fun shouldClearRedoAfterUndoAndExecute() {
		cmdManager.execute(AppendCommand(app, "a"))
		cmdManager.execute(AppendCommand(app, "b"))
		cmdManager.undo()

		cmdManager.execute(AppendCommand(app, "c"))

		assertFalse(cmdManager.canRedo())
	}

	/** ---- Transaction tests */

	@Test
	fun shouldNotCommitNotExistingTransaction() {
		assertFailsWith<IllegalStateException> {
			cmdManager.commitTransaction()
		}
	}

	@Test
	fun shouldAutoCommitExecute() {
		cmdManager.execute(AppendCommand(app, "a"))
		assertEquals("a", app.mandatoryData.value)
	}

	@Test
	fun shouldAutoCommitRegister() {
		app.mandatoryData.append("a")

		cmdManager.register(AppendCommand(app, "a"))

		assertEquals("a", app.mandatoryData.value)
		assertTrue(cmdManager.canUndo())
	}

	@Test
	fun shouldDoSingleTransaction() {
		cmdManager.beginTransaction(AppendCommand(app, "a"))
		cmdManager.commitTransaction()

		assertEquals("a", app.mandatoryData.value)
		assertTrue(cmdManager.canUndo())
	}

	@Test
	fun shouldExecuteCommandInExistingTransaction() {
		cmdManager.beginTransaction(AppendCommand(app, "a"))
		cmdManager.execute(AppendCommand(app, "b"))
		cmdManager.commitTransaction()

		assertEquals("ab", app.mandatoryData.value)
		assertTrue(cmdManager.canUndo())
	}

	@Test
	fun shouldNotCommitInnerTransaction() {
		cmdManager.beginTransaction(AppendCommand(app, "a"))
		cmdManager.beginTransaction(AppendCommand(app, "b"))
		cmdManager.commitTransaction()

		assertEquals("ab", app.mandatoryData.value)
		assertFalse(cmdManager.canUndo())
	}

	@Test
	fun shouldCommitOuterTransaction() {
		cmdManager.beginTransaction(AppendCommand(app, "a"))
		cmdManager.beginTransaction(AppendCommand(app, "b"))
		cmdManager.commitTransaction()
		cmdManager.commitTransaction()

		assertEquals("ab", app.mandatoryData.value)
		assertTrue(cmdManager.canUndo())
	}

	@Test
	fun shouldRollback() {
		cmdManager.beginTransaction(AppendCommand(app, "a"))

		cmdManager.rollbackTransaction()

		assertEquals("", app.mandatoryData.value)
		assertFalse(cmdManager.canUndo())
	}

	@Test
	fun shouldRollbackOnExceptionInBeginTransaction() {
		cmdManager.execute(AppendCommand(app, "a"))
		cmdManager.beginTransaction(AppendCommand(app, "b"))
		try {
			cmdManager.beginTransaction(ExceptionCommand())
		} catch (e: Exception) {
			// empty
		}

		cmdManager.execute(AppendCommand(app, "c"))

		assertEquals("ac", app.mandatoryData.value)
		assertTrue(cmdManager.canUndo())
		cmdManager.undo()
		assertEquals("a", app.mandatoryData.value)
	}

	@Test
	fun shouldRollbackOnExceptionInExecute() {
		cmdManager.execute(AppendCommand(app, "a"))
		try {
			cmdManager.execute(ExceptionCommand())
		} catch (e: Exception) {
			// empty
		}

		cmdManager.execute(AppendCommand(app, "b"))

		assertEquals("ab", app.mandatoryData.value)
		assertTrue(cmdManager.canUndo())
		cmdManager.undo()
		assertEquals("a", app.mandatoryData.value)
	}

	@Test
	fun shouldNotRedoRolledBackTransaction() {
		cmdManager.execute(AppendCommand(app, "a"))

		cmdManager.beginTransaction(AppendCommand(app, "b"))
		cmdManager.rollbackTransaction()
		assertEquals("a", app.mandatoryData.value)

		cmdManager.undo()
		assertEquals("", app.mandatoryData.value)

		cmdManager.redo()
		assertEquals("a", app.mandatoryData.value)

		assertFalse(cmdManager.canRedo())
	}

	/** GitHub issue #410. */
	@Test
	fun shouldHandleExceptionInUndoDuringRollback() {
		assertFails("Error in execute") {
			cmdManager.execute(UndoableExceptionCommand())
		}

		cmdManager.execute(AppendCommand(app, "a"))

		assertEquals("a", app.mandatoryData.value)
		assertTrue(cmdManager.canUndo())
		cmdManager.undo()
		assertEquals("", app.mandatoryData.value)
	}

	/** ---- Checkpoint tests */

	@Test
	fun shouldNotBeUndoableWithNewCheckpoint() {
		val checkpointApp = ApplicationDummy()
		cmdManager.execute(AppendCommand(app, "a"))

		cmdManager.openCheckpoint("test", checkpointApp)

		assertFalse(cmdManager.canUndo())
	}

	@Test
	fun shouldBeUndoableAfterOpeningCheckpoint() {
		val checkpointApp = ApplicationDummy()
		cmdManager.execute(AppendCommand(app, "a"))

		cmdManager.openCheckpoint("test", checkpointApp)
		cmdManager.execute(AppendCommand(checkpointApp, "b"))

		assertTrue(cmdManager.canUndo())
	}

	@Test
	fun shouldUndoInCheckpoint() {
		val checkpointApp = ApplicationDummy()
		cmdManager.openCheckpoint("test", checkpointApp)
		cmdManager.execute(AppendCommand(checkpointApp, "a"))
		cmdManager.execute(AppendCommand(checkpointApp, "b"))

		cmdManager.undo()

		assertEquals("a", checkpointApp.mandatoryData.value)
	}

	@Test
	fun shouldRedoInCheckpoint() {
		val checkpointApp = ApplicationDummy()

		cmdManager.openCheckpoint("test", checkpointApp)
		cmdManager.execute(AppendCommand(checkpointApp, "a"))
		cmdManager.execute(AppendCommand(checkpointApp, "b"))
		cmdManager.undo()

		cmdManager.redo()

		assertEquals("ab", checkpointApp.mandatoryData.value)

	}

	@Test
	fun shouldPurgeCommandsWhenClosingCheckpoint() {
		val checkpointApp = ApplicationDummy()
		cmdManager.execute(AppendCommand(app, "a"))

		cmdManager.openCheckpoint("test", checkpointApp)
		cmdManager.execute(AppendCommand(checkpointApp, "b"))
		cmdManager.closeCheckpoint()

		cmdManager.undo()

		assertEquals("", app.mandatoryData.value)
	}

	@Test
	fun shouldUseCheckpoint() {
		val checkpointApp = ApplicationDummy()
		cmdManager.execute(AppendCommand(app, "a"))

		cmdManager.openCheckpoint("test", checkpointApp)
		cmdManager.execute(AppendCommand(checkpointApp, "b"))
		cmdManager.execute(AppendCommand(checkpointApp, "c"))
		cmdManager.closeCheckpoint()

		// The data in Application has changed since checkpoint, but checkpoint command are thrown away when
		// closing a checkpoint, therefore register a Command for the accumulated changes since the checkpoint
		cmdManager.execute(AppendCommand(app, "bc"))

		assertEquals("abc", app.mandatoryData.value)
	}

	@Test
	fun shouldUndoAfterOpeningCheckpoint() {
		cmdManager.execute(AppendCommand(app, "a"))
		val checkpointApp = ApplicationDummy(StorableString(app.mandatoryData.value))

		cmdManager.openCheckpoint("test", checkpointApp)
		cmdManager.execute(AppendCommand(checkpointApp, "b"))
		cmdManager.execute(AppendCommand(checkpointApp, "c"))
		cmdManager.undo()

		cmdManager.execute(AppendCommand(app, "b"))
		assertEquals("ab", app.mandatoryData.value)
	}

	@Test
	fun shouldAbandonCheckpointChanges() {
		val checkpointApp = ApplicationDummy()
		cmdManager.execute(AppendCommand(app, "a"))

		cmdManager.openCheckpoint("test", checkpointApp)
		cmdManager.execute(AppendCommand(app, "b"))
		cmdManager.closeCheckpoint()
		// Abandon checkpoint changes by not registering a corresponding command

		cmdManager.undo()
		assertEquals("", app.mandatoryData.value)
	}

	/** ---- [Iterable] tests */

	@Test
	fun shouldIterateNoCommands() {
		assertFalse(cmdManager.iterator().hasNext())
	}

	@Test
	fun shouldIterateSimpleCommands() {
		cmdManager.execute(AppendCommand(app, "a"))
		cmdManager.execute(AppendCommand(app, "b"))
		val iterator = cmdManager.iterator()

		assertEquals("a", (iterator.next() as AppendCommand).s)
		assertEquals("b", (iterator.next() as AppendCommand).s)
		assertFalse(iterator.hasNext())
	}

	@Test
	fun shouldIterateTransactionCommands() {
		cmdManager.execute(AppendCommand(app, "a"))
		cmdManager.beginTransaction(AppendCommand(app, "b"))
		cmdManager.execute(AppendCommand(app, "c"))
		cmdManager.commitTransaction()
		cmdManager.beginTransaction(AppendCommand(app, "d"))
		cmdManager.execute(AppendCommand(app, "e"))
		cmdManager.commitTransaction()
		val iterator = cmdManager.iterator()

		assertEquals("a", (iterator.next() as AppendCommand).s)
		assertEquals("b", (iterator.next() as AppendCommand).s)
		assertEquals("c", (iterator.next() as AppendCommand).s)
		assertEquals("d", (iterator.next() as AppendCommand).s)
		assertEquals("e", (iterator.next() as AppendCommand).s)
		assertFalse(iterator.hasNext())
	}

	@Test
	fun shouldIterateSnapshotCommands() {
		BaseModule.properties.set(SourcingCommandManager.PROP_MAX_COMMAND_COUNT_PER_SNAPSHOT, 1)
		cmdManager = SourcingCommandManager()
		cmdManager.bindDataHolder(app)

		cmdManager.execute(AppendCommand(app, "a"))
		cmdManager.beginTransaction(AppendCommand(app, "b"))
		cmdManager.execute(AppendCommand(app, "c"))
		cmdManager.commitTransaction()
		cmdManager.beginTransaction(AppendCommand(app, "d"))
		cmdManager.execute(AppendCommand(app, "e"))
		cmdManager.commitTransaction()
		val iterator = cmdManager.iterator()

		assertEquals("a", (iterator.next() as AppendCommand).s)
		assertEquals("b", (iterator.next() as AppendCommand).s)
		assertEquals("c", (iterator.next() as AppendCommand).s)
		assertEquals("d", (iterator.next() as AppendCommand).s)
		assertEquals("e", (iterator.next() as AppendCommand).s)
		assertFalse(iterator.hasNext())
	}

	@Test
	fun shouldNotifyUndo() {
		val cmd = NotifiableAppendCommand(app, "hallo")
		cmdManager.execute(cmd)

		cmdManager.undo()

		assertEquals("", app.mandatoryData.value)
		assertEquals("", cmd.state)
	}
}
