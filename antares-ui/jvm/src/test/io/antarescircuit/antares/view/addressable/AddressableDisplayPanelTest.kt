package io.antarescircuit.antares.view.addressable

import dev.mokkery.MockMode
import dev.mokkery.mock
import io.antarescircuit.antares.model.addressable.Addressable
import io.antarescircuit.antares.model.addressable.AddressableReference
import io.antarescircuit.antares.model.addressable.MemoryStorable
import io.antarescircuit.antares.model.signal.BitWidth
import io.antarescircuit.jabbah.app.ApplicationData
import io.antarescircuit.jabbah.app.ApplicationDataContentEstablishedEvent
import io.antarescircuit.jabbah.app.Savable
import io.antarescircuit.jabbah.animation.Animator
import io.antarescircuit.jabbah.base.module.BaseModuleJvm
import io.antarescircuit.jabbah.base.Settings
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.event.EventBusImpl
import io.antarescircuit.jabbah.edit.CommandManager
import io.antarescircuit.jabbah.execution.scheduler.Scheduler
import io.antarescircuit.jabbah.graph.GraphApplicationContextHolder
import io.antarescircuit.jabbah.graph.model.Graph
import io.antarescircuit.jabbah.graph.model.vertice.ObjectLink
import java.awt.Container
import javax.swing.JTextField
import javax.swing.JTable
import javax.swing.SwingUtilities
import kotlin.test.*

class AddressableDisplayPanelTest {

	@Test
	fun shouldClampPageAndRebuildRowsWhenSnapshotRestoresSmallerMemory() {
		SwingUtilities.invokeAndWait {
			BaseModuleJvm.require()
			Translations.addBundle("antares")
			val bus = EventBusImpl()
			var memory = MemoryStorable(addressWidth = BitWidth.BW_16)
			val ref = AddressableReference(object : ObjectLink<Addressable> {
				override fun getLinkedObject(startGraph: Graph?): Addressable = memory
			}, null, bus)
			val context = GraphApplicationContextHolder(mock<Scheduler>(MockMode.autofill), bus, animator = mock<Animator>(MockMode.autofill))
			val panel = AddressableDisplayPanel(ref, { true }, context, mock<CommandManager>(), Settings())
			try {
				assertTrue(panel.goToAddress(60_000))
				val data = ApplicationData(memory, mock<Savable>(MockMode.autofill), bus)
				val smaller = MemoryStorable(addressWidth = BitWidth.BW_4)
				smaller.memory.write(15, 42UL)
				data.content = smaller
				memory = smaller
				bus.post(ApplicationDataContentEstablishedEvent(data))
				val table = findMemoryTable(panel)!!
				assertEquals(16, table.rowCount)
				assertEquals("2A", table.getValueAt(15, 0))
				assertEquals(15, (table.model as AbstractAddressableTableModel).addressAt(table.selectedRow, table.selectedColumn))
				assertFalse(panel.goToAddress(16))
			} finally {
				panel.dispose()
				ref.dispose()
				context.dispose()
			}
		}
	}

	@Test
	fun shouldCancelCellEditingWhenSnapshotReplacesMemory() {
		SwingUtilities.invokeAndWait {
			BaseModuleJvm.require()
			Translations.addBundle("antares")
			for (restoredWidth in listOf(BitWidth.BW_4, BitWidth.BW_16)) {
				val bus = EventBusImpl()
				var memory = MemoryStorable(addressWidth = BitWidth.BW_16).apply { this.memory.write(0, 0x11UL) }
				val ref = AddressableReference(object : ObjectLink<Addressable> {
					override fun getLinkedObject(startGraph: Graph?): Addressable = memory
				}, null, bus)
				val context = GraphApplicationContextHolder(mock<Scheduler>(MockMode.autofill), bus, animator = mock<Animator>(MockMode.autofill))
				val panel = AddressableDisplayPanel(ref, { true }, context, mock<CommandManager>(MockMode.autofill), Settings())
				try {
					val table = findMemoryTable(panel)!!
					assertTrue(table.editCellAt(0, 0))
					assertEquals("11", (table.editorComponent as JTextField).text)
					val data = ApplicationData(memory, mock<Savable>(MockMode.autofill), bus)
					val restored = MemoryStorable(addressWidth = restoredWidth).apply { this.memory.write(0, 0x22UL) }
					data.content = restored
					memory = restored
					bus.post(ApplicationDataContentEstablishedEvent(data))
					assertFalse(table.isEditing)
					assertEquals(0x22UL, restored.memory.read(0))
					assertEquals("22", table.getValueAt(0, 0))
					if (restoredWidth == BitWidth.BW_16) {
						assertTrue(table.editCellAt(0, 0))
						(table.editorComponent as JTextField).text = "33"
						assertTrue(panel.goToAddress(2_000))
						assertEquals(0x33UL, restored.memory.read(0))
					}
				} finally {
					panel.dispose()
					ref.dispose()
					context.dispose()
				}
			}
		}
	}

	private fun findMemoryTable(container: Container): JTable? {
		for (component in container.components) {
			if (component is JTable && component.model is AbstractAddressableTableModel) return component
			if (component is Container) findMemoryTable(component)?.let { return it }
		}
		return null
	}
}
