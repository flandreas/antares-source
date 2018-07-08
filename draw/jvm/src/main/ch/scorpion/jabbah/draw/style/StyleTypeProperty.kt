package ch.scorpion.jabbah.draw.style

import com.l2fprod.common.beans.editor.ComboBoxPropertyEditor
import java.awt.Component
import javax.swing.DefaultListCellRenderer
import javax.swing.JComboBox
import javax.swing.JList
import javax.swing.JTable
import javax.swing.table.TableCellRenderer
import javafx.beans.value.ObservableValue
import javafx.scene.control.ComboBox
import org.controlsfx.control.PropertySheet
import org.controlsfx.property.editor.AbstractPropertyEditor

// TODO Remove when Swing is not used any more
class StyleTypeEditor(styleProvider: StyleProvider) : ComboBoxPropertyEditor() {

    constructor(): this(DrawStyleModule.styleProvider)

    init {
        setAvailableValues(styleProvider.getChoosableStyleTypes().toTypedArray())
        (editor as JComboBox<*>).renderer = StyleTypeRenderer()
    }
}

class StyleTypeEditorFx(
	item: PropertySheet.Item
) : AbstractPropertyEditor<StyleType,ComboBox<StyleType>>(item, ComboBox()) {

	private val styleProvider = DrawStyleModule.styleProvider

	init {
		editor.items.setAll(styleProvider.getChoosableStyleTypes())
	}

	override fun setValue(value: StyleType?) {
		editor.value = value
	}

	override fun getObservableValue(): ObservableValue<StyleType> {
		return editor.valueProperty()
	}
}

// TODO Remove when Swing is not used any more
class StyleTypeRenderer : DefaultListCellRenderer(), TableCellRenderer {

    override fun getListCellRendererComponent(list: JList<*>, value: Any?, index: Int, isSelected: Boolean, cellHasFocus: Boolean): Component {
        setValue(value as StyleType)

        if (isSelected) {
            foreground = list.selectionForeground
            background = list.selectionBackground
        } else {
            foreground = list.foreground
            background = list.background
        }
        font = list.font
        return this
    }

    override fun getTableCellRendererComponent(table: JTable, value: Any?, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int): Component {
        setValue(value as StyleType)

        if (isSelected) {
            foreground = table.selectionForeground
            background = table.selectionBackground
        } else {
            foreground = table.foreground
            background = table.background
        }
        font = table.font
        return this
    }

    private fun setValue(styleType: StyleType?) {
        // TODO I18N
        if (styleType == null) {
            icon = null
            text = "Keine"
        } else {
            text = styleType.description
        }
    }
}