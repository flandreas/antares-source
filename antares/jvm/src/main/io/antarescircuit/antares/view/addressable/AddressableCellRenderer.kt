package io.antarescircuit.antares.view.addressable

import io.antarescircuit.antares.model.addressable.Addressable
import io.antarescircuit.antares.model.addressable.AddressableReference
import io.antarescircuit.jabbah.edit.Look
import io.antarescircuit.jabbah.draw.graphics.Graphics2DJvm
import io.antarescircuit.jabbah.graph.GraphApplicationContextHolder
import com.l2fprod.common.swing.renderer.DefaultCellRenderer
import java.awt.Component
import javax.swing.JTable

/**
 * Renders data of an [Addressable] in a [JTable] differently during editing and during simulation.
 */
class AddressableCellRenderer(
	private val applicationContextHolder: GraphApplicationContextHolder,
	private val addressableRef: AddressableReference,
	private val addressableDisplayLayout: AddressableDisplayLayout
) : DefaultCellRenderer() {

	override fun getTableCellRendererComponent(table: JTable?, value: Any?, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int): Component {
		val component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column)

		if (applicationContextHolder.scheduler.isActive
			&& addressableRef.addressable.isSelected
			&& addressableDisplayLayout.getCellAddress(row, column) == addressableRef.addressable.currentAddress
		) {
			component.background = Graphics2DJvm.toAwtColor(Look.highlightWithSelectionColor)
		} else {
			if (isSelected) {
				component.background = table!!.selectionBackground
			} else {
				component.background = table!!.background
			}
		}
		return component
	}
}