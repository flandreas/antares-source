package ch.scorpion.jabbah.edit.command

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.collection.EmptyIterator
import ch.scorpion.jabbah.base.exception.IllegalStateException
import ch.scorpion.jabbah.edit.EditTestRule
import ch.scorpion.jabbah.io.*
import kotlin.test.*

/** Unit tests for [SourcingCommandManager]. */
class SourcingCommandManagerTest {

	companion object {
		init {
			Translations.withAnyKey()
			EditTestRule.configure()
			IOModule.typeMap.register("storableString", StorableString::class)
		}
	}

	private var application = Application()
	private var cmdManager = SourcingCommandManager(application)

	/** ---- Initialization tests */

	@Test
	fun shouldNotCreateSnapshotWithoutData() {
		application.data = null
		cmdManager.reset()
	}

	@Test
	fun shouldResetOnConstruction() {
		assertEquals(1, cmdManager.snapshotCount)
	}

	@Test
	fun shouldCreateSnapshotOnReset() {
		assertEquals(1, cmdManager.snapshotCount)
		assertEquals(0, cmdManager.redoSnapshotCount)
	}

	/** ---- Simple undo/redo tests */

	@Test
	fun shouldExecute() {
		cmdManager.execute(AppendCommand("a"))
		cmdManager.execute(AppendCommand("b"))
		cmdManager.execute(AppendCommand("c"))

		assertEquals("abc", application.mandatoryData.value)
	}

	@Test
	fun shouldUndo() {
		cmdManager.execute(AppendCommand("abc"))

		cmdManager.undo()

		assertEquals("", application.mandatoryData.value)
		assertEquals(1, cmdManager.snapshotCount)
		assertEquals(0, cmdManager.redoSnapshotCount)
	}

	@Test
	fun shouldRedo() {
		cmdManager.execute(AppendCommand("abc"))
		cmdManager.undo()

		cmdManager.redo()

		assertEquals("abc", application.mandatoryData.value)
		assertEquals(1, cmdManager.snapshotCount)
		assertEquals(0, cmdManager.redoSnapshotCount)
	}

	@Test
	fun shouldCreateAdditionalSnapshot() {
		cmdManager = SourcingCommandManager(application, maxCommandCountPerSnapshot = 2)

		cmdManager.execute(AppendCommand("a"))
		cmdManager.execute(AppendCommand("b"))
		cmdManager.execute(AppendCommand("c"))

		assertEquals(2, cmdManager.snapshotCount)
	}

	@Test
	fun shouldUndoWithAdditionalSnapshot() {
		cmdManager = SourcingCommandManager(application, maxCommandCountPerSnapshot = 2)

		cmdManager.execute(AppendCommand("a"))
		cmdManager.execute(AppendCommand("b"))
		cmdManager.execute(AppendCommand("c"))

		cmdManager.undo()
		assertEquals("ab", application.mandatoryData.value)

		cmdManager.undo()
		assertEquals("a", application.mandatoryData.value)

		cmdManager.undo()
		assertEquals("", application.mandatoryData.value)

		assertEquals(1, cmdManager.snapshotCount)
		assertEquals(1, cmdManager.redoSnapshotCount)
	}

	@Test
	fun shouldRedoWithAdditionalSnapshot() {
		cmdManager = SourcingCommandManager(application, maxCommandCountPerSnapshot = 2)

		cmdManager.execute(AppendCommand("a"))
		cmdManager.execute(AppendCommand("b"))
		cmdManager.execute(AppendCommand("c"))

		cmdManager.undo()
		cmdManager.undo()
		cmdManager.undo()

		cmdManager.redo()
		assertEquals("a", application.mandatoryData.value)

		cmdManager.redo()
		assertEquals("ab", application.mandatoryData.value)

		cmdManager.redo()
		assertEquals("abc", application.mandatoryData.value)

		assertEquals(2, cmdManager.snapshotCount)
		assertEquals(0, cmdManager.redoSnapshotCount)
	}

	@Test
	fun shouldGetUndoDescription() {
		cmdManager.execute(AppendCommand("a"))
		assertEquals("anyDescription", cmdManager.getUndoDescription())
	}

	@Test
	fun shouldGetRedoDescription() {
		cmdManager.execute(AppendCommand("a"))
		cmdManager.undo()

		assertEquals("anyDescription", cmdManager.getRedoDescription())
	}

	@Test
	fun shouldNotExecuteRegister() {
		application.mandatoryData.append("a")

		cmdManager.register(AppendCommand("a"))

		assertEquals("a", application.mandatoryData.value)
		assertTrue(cmdManager.canUndo())
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
		cmdManager.execute(AppendCommand("a"))
		assertEquals("a", application.mandatoryData.value)
	}

	@Test
	fun shouldAutoCommitRegister() {
		application.mandatoryData.append("a")

		cmdManager.register(AppendCommand("a"))

		assertEquals("a", application.mandatoryData.value)
		assertTrue(cmdManager.canUndo())
	}

	@Test
	fun shouldDoSingleTransaction() {
		cmdManager.beginTransaction(AppendCommand("a"))
		cmdManager.commitTransaction()

		assertEquals("a", application.mandatoryData.value)
		assertTrue(cmdManager.canUndo())
	}

	@Test
	fun shouldExecuteCommandInExistingTransaction() {
		cmdManager.beginTransaction(AppendCommand("a"))
		cmdManager.execute(AppendCommand("b"))
		cmdManager.commitTransaction()

		assertEquals("ab", application.mandatoryData.value)
		assertTrue(cmdManager.canUndo())
	}

	@Test
	fun shouldNotCommitInnerTransaction() {
		cmdManager.beginTransaction(AppendCommand("a"))
		cmdManager.beginTransaction(AppendCommand("b"))
		cmdManager.commitTransaction()

		assertEquals("ab", application.mandatoryData.value)
		assertFalse(cmdManager.canUndo())
	}

	@Test
	fun shouldCommitOuterTransaction() {
		cmdManager.beginTransaction(AppendCommand("a"))
		cmdManager.beginTransaction(AppendCommand("b"))
		cmdManager.commitTransaction()
		cmdManager.commitTransaction()

		assertEquals("ab", application.mandatoryData.value)
		assertTrue(cmdManager.canUndo())
	}

	@Test
	fun shouldRollback() {
		cmdManager.beginTransaction(AppendCommand("a"))

		cmdManager.rollbackTransaction()

		assertEquals("", application.mandatoryData.value)
		assertFalse(cmdManager.canUndo())
	}

	/** ---- Checkpoint tests */

	/*
	@Test
	fun shouldCommitCheckpoint() {
		cmdManager.execute(AppendCommand("a"))
		cmdManager.openCheckpoint("test")
		cmdManager.execute(AppendCommand("b"))

		cmdManager.commitCheckpoint()

		assertEquals("ab", application.data.value)
	}

	@Test
	fun shouldRollbackCheckpoint() {
		cmdManager.execute(AppendCommand("a"))
		cmdManager.openCheckpoint("test")
		cmdManager.execute(AppendCommand("b"))

		cmdManager.rollbackCheckpoint()

		assertEquals("a", application.data.value)
	}

	@Test
	fun shouldUndoInCheckpoint() {
		cmdManager.execute(AppendCommand("a"))
		cmdManager.openCheckpoint("test")
		cmdManager.execute(AppendCommand("b"))

		cmdManager.undo()

		assertEquals("a", application.data.value)
		assertTrue(cmdManager.canUndo())
	}

	@Test
	fun shouldNotUndoBeyondCheckpoint() {
		cmdManager.execute(AppendCommand("a"))
		cmdManager.openCheckpoint("text")

		assertFalse(cmdManager.canUndo())
	}
	*/

	/** ---- Helper classes and methods */

	private class Application(
		var data: StorableString? = StorableString()
	) : UndoableDataHolder {

		val mandatoryData: StorableString get() = data!!

		override fun getUndoableState(): Storable? {
			return data
		}

		override fun setUndoableState(state: Storable) {
			data = state as StorableString
		}
	}

	private inner class AppendCommand(
		private val s: String
	) : AbstractCommand("anyDescription") {

		override fun execute() {
			application.mandatoryData.append(s)
		}

		override fun undo() {
			// This will not be needed any more
			throw UnsupportedOperationException("not implemented")
		}
	}
}

class StorableString(value: String = "") : Storable {

	var value: String = value
		private set

	fun append(s: String) {
		value = "$value$s"
	}

	override var storableId: Int = 0

	override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) { }

	override fun write(writer: StoreWriter) {
		writer.writeString("value", value)
	}

	override fun read(reader: StoreReader) {
		value = reader.readString("value")
	}

	override fun getStorableChildren(): Iterator<Storable> {
		return EmptyIterator()
	}
}