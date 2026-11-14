package io.antarescircuit.jabbah.graph.view

import io.antarescircuit.jabbah.base.swing.EnumRenderer
import io.antarescircuit.jabbah.edit.componentBeanProvider
import io.antarescircuit.jabbah.edit.module.EditModuleUiJvm
import io.antarescircuit.jabbah.edit.properties.CommandPropertySwing
import io.antarescircuit.jabbah.graph.view.module.GraphViewModuleJvm
import io.antarescircuit.jabbah.graph.view.vertice.SubGraphDisplayMode
import javax.swing.JComboBox
import javax.swing.JTable
import javax.swing.SwingUtilities
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull

class SubGraphDisplayModePropertyTest {

	@Test
	fun shouldRenderAndEditEveryDisplayMode() {
		SwingUtilities.invokeAndWait {
			GraphViewModuleJvm.require()
			val property = CommandPropertySwing("displayMode", "graph.property.displayMode", SubGraphDisplayMode::class.java, componentBeanProvider)
			val renderer = EditModuleUiJvm.propertyRendererRegistry.getRenderer(property)
			assertIs<EnumRenderer<*>>(renderer)
			val editor = EditModuleUiJvm.propertyEditorRegistry.getEditor(property)
			assertIs<SubGraphDisplayModeEditor>(editor)
			val combo = editor.customEditor as JComboBox<*>
			assertEquals(SubGraphDisplayMode.entries, (0 until combo.itemCount).map { combo.getItemAt(it) })
			for (mode in SubGraphDisplayMode.entries) {
				assertNotNull(renderer.getTableCellRendererComponent(JTable(), mode, false, false, 0, 0))
				editor.value = mode
				assertEquals(mode, combo.selectedItem)
				assertEquals(mode, editor.value)
			}
		}
	}
}
