package io.antarescircuit.antares.view.addressable

import io.antarescircuit.antares.AntaresTestRule
import io.antarescircuit.antares.model.addressable.Addressable
import io.antarescircuit.antares.model.addressable.AddressableClearCommand
import io.antarescircuit.antares.model.addressable.MemoryStorable
import io.antarescircuit.antares.model.signal.BitWidth
import io.antarescircuit.jabbah.base.event.EventBusImpl
import io.antarescircuit.jabbah.edit.command.SourcingCommandManager
import io.antarescircuit.jabbah.edit.model.GenericUndoableDataHolder
import io.antarescircuit.jabbah.graph.model.Graph
import io.antarescircuit.jabbah.graph.model.vertice.ObjectLink
import java.nio.file.Files
import kotlin.test.*

class AddressableContentsCommandTest {

	private fun link(memory: Addressable) = object : ObjectLink<Addressable> {
		override fun getLinkedObject(startGraph: Graph?): Addressable = memory
	}

	@Test
	fun shouldImportUndoAndRedoEvenAfterDeletingSourceFile() {
		AntaresTestRule.configure()
		val memory = MemoryStorable(addressWidth = BitWidth.BW_32).apply {
			memory.writeCommentedValue(100, 42UL, "original")
			dataSource = "original.amd"
		}
		val path = Files.createTempFile("antares-memory-", ".raw")
		try {
			Files.writeString(path, "v2.0 raw\nff 02")
			val command = AddressableContentsCommand(null, link(memory), path.toString())
			command.execute()
			assertEquals(255UL, memory.memory.read(0))
			assertEquals(0UL, memory.memory.read(100))
			command.undo()
			assertEquals(42UL, memory.memory.read(100))
			assertEquals("original", memory.memory.readComment(100))
			assertEquals("original.amd", memory.dataSource)
			Files.delete(path)
			command.execute()
			assertEquals(255UL, memory.memory.read(0))
			assertEquals(path.toString(), memory.dataSource)
		} finally {
			Files.deleteIfExists(path)
		}
	}

	@Test
	fun shouldPreserveContentsAndSourceWhenImportExceedsWidthOrCapacity() {
		AntaresTestRule.configure()
		val memory = MemoryStorable(addressWidth = BitWidth.BW_4).apply {
			memory.writeCommentedValue(3, 42UL, "original")
			dataSource = "original.amd"
		}
		val manager = SourcingCommandManager(EventBusImpl())
		manager.bindDataHolder(GenericUndoableDataHolder(memory))
		val path = Files.createTempFile("antares-invalid-memory-", ".raw")
		try {
			for (contents in listOf("v2.0 raw\n01 100", "v2.0 raw\n17*00", "01 100", "v3.0 hex words addressed\n10: ff")) {
				Files.writeString(path, contents)
				assertFailsWith<IllegalArgumentException> { manager.execute(AddressableContentsCommand(null, link(memory), path.toString())) }
				assertEquals(42UL, memory.memory.read(3))
				assertEquals("original", memory.memory.readComment(3))
				assertEquals("original.amd", memory.dataSource)
				assertEquals(0UL, memory.memory.read(0))
				assertFalse(manager.isInTransaction)
				assertFalse(manager.canUndo())
				assertFalse(manager.canRedo())
			}
			Files.writeString(path, "v2.0 raw\nff")
			manager.execute(AddressableContentsCommand(null, link(memory), path.toString()))
			assertEquals(255UL, memory.memory.read(0))
			manager.undo()
			assertEquals(42UL, memory.memory.read(3))
			assertEquals("original.amd", memory.dataSource)
		} finally {
			Files.deleteIfExists(path)
		}
	}

	@Test
	fun shouldUndoClearOfSparseMemoryIncludingComments() {
		AntaresTestRule.configure()
		val memory = MemoryStorable(addressWidth = BitWidth.BW_32)
		memory.memory.writeCommentedValue(100_000_000, 42UL, "sparse")
		val command = AddressableClearCommand(null, link(memory))
		command.execute()
		assertFalse(memory.memory.getNonZeroCells().hasNext())
		command.undo()
		assertEquals(42UL, memory.memory.read(100_000_000))
		assertEquals("sparse", memory.memory.readComment(100_000_000))
	}
}
