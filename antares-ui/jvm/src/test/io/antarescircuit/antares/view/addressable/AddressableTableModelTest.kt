package io.antarescircuit.antares.view.addressable

import io.antarescircuit.antares.model.addressable.Addressable
import io.antarescircuit.antares.model.addressable.AddressableReference
import io.antarescircuit.antares.model.addressable.MemoryStorable
import io.antarescircuit.antares.model.signal.BitWidth
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.event.EventBusImpl
import io.antarescircuit.jabbah.graph.model.Graph
import io.antarescircuit.jabbah.graph.model.vertice.ObjectLink
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AddressableTableModelTest {
	private fun reference(memory: Addressable): AddressableReference = AddressableReference(object : ObjectLink<Addressable> {
		override fun getLinkedObject(startGraph: Graph?): Addressable = memory
	}, null, EventBusImpl())

	@Test
	fun shouldReadAndWriteHighAddressesInEveryLayout() {
		Translations.addBundle("antares")
		for (columns in listOf(1, 4, 8, 16)) {
			val memory = MemoryStorable(addressWidth = BitWidth.BW_16)
			val ref = reference(memory)
			try {
				val layout = FixedWidthLayout(columns, ref, { true }, { AddressableValueConverter.Hexadecimal })
				layout.startAddress = MemoryPage.containing(65_535, 65_535, columns).start
				val model = layout.createTableModel()
				model.setValueAt("AB", 1_023, columns - 1)
				assertEquals(0xabUL, memory.dataAt(65_535))
				assertEquals("AB", model.getValueAt(1_023, columns - 1))
				assertEquals(65_535, layout.getCellAddress(1_023, columns - 1))
				assertEquals(0UL, memory.dataAt(0))
			} finally {
				ref.dispose()
			}
		}
	}

	@Test
	fun shouldLeavePaddingCellsBlankAndReadOnly() {
		val memory = MemoryStorable(addressWidth = BitWidth.BW_1)
		val ref = reference(memory)
		try {
			val model = FixedWidthLayout(16, ref, { true }, { AddressableValueConverter.Hexadecimal }).createTableModel()
			assertEquals(1, model.rowCount)
			assertTrue(model.isCellEditable(0, 1))
			assertFalse(model.isCellEditable(0, 2))
			assertEquals("", model.getValueAt(0, 2))
			model.setValueAt("AB", 0, 2)
			assertEquals(0UL, memory.dataAt(2))
		} finally {
			ref.dispose()
		}
	}

	@Test
	fun shouldEditCommentAtThePagedAddressIncludingDisassemblyLayout() {
		Translations.addBundle("antares")
		val memory = MemoryStorable(addressWidth = BitWidth.BW_16)
		val disassembled = object : Addressable by memory {
			override val disassemblyWidth: Int get() = 8
			override fun disassemblyAt(address: Int): String = "instruction $address"
		}
		val ref = reference(disassembled)
		try {
			val model = SingleColumnTableModel(ref, 1_024, { true }, { AddressableValueConverter.Hexadecimal }, startAddress = 1_024)
			assertFalse(model.isCellEditable(3, 1))
			assertTrue(model.isCommentColumn(2))
			assertTrue(model.isCellEditable(3, 2))
			assertEquals("instruction 1027", model.getValueAt(3, 1))
			model.setValueAt("entry", 3, 2)
			assertEquals("entry", memory.commentAt(1_027))
			assertEquals("entry", model.getValueAt(3, 2))
		} finally {
			ref.dispose()
		}
	}
}
