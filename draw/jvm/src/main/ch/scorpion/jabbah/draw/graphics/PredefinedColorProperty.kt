package ch.scorpion.jabbah.draw.graphics

import com.l2fprod.common.beans.editor.ComboBoxPropertyEditor
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.swing.ColorIcon
import javafx.beans.value.ObservableValue
import javafx.scene.control.ComboBox
import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import javafx.scene.shape.Rectangle
import javafx.util.Callback
import org.controlsfx.control.PropertySheet
import org.controlsfx.property.editor.AbstractPropertyEditor
import java.awt.Component
import javax.swing.*
import javax.swing.table.TableCellRenderer

// TODO Remove when Swing is not used any more
class PredefinedColorRenderer : DefaultListCellRenderer(), TableCellRenderer {

    private val colorIcon = ColorIcon()

    override fun getListCellRendererComponent(list: JList<*>, value: Any?, index: Int, isSelected: Boolean, cellHasFocus: Boolean): Component {
        setValue(value as PredefinedColor?)

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
		setValue(value as PredefinedColor?)
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

    private fun setValue(color: PredefinedColor?) {
        if (color == null) {
            icon = null
            text = Translations.getString("edit.style.property.fromStyle.name")
        } else {
            colorIcon.backgroundColor = Graphics2DJvm.toAwtColor(color.color.backgroundColor)
            colorIcon.foregroundColor = Graphics2DJvm.toAwtColor(color.color.foregroundColor)
            text = color.description
            icon = colorIcon
        }
    }
}

// TODO Remove when Swing is not used any more
class PredefinedColorEditor(colorProvider: PredefinedColorProvider) : ComboBoxPropertyEditor() {
    init {
        val list = mutableListOf<PredefinedColor?>(null)
        list.addAll(colorProvider.provideAll())
        setAvailableValues(list.toTypedArray())
        (editor as JComboBox<*>).renderer = PredefinedColorRenderer()
    }
}

object PredefinedColorRendererFx : Callback<ListView<PredefinedColor>, ListCell<PredefinedColor>> {
	override fun call(param: ListView<PredefinedColor>?): ListCell<PredefinedColor> {
		return object : ListCell<PredefinedColor>() {
			override fun updateItem(item: PredefinedColor?, empty: Boolean) {
				super.updateItem(item, empty)
				if (item == null || empty) {
					text = Translations.getString("edit.style.property.fromStyle.name")
					graphic = null
				} else {
					val rect = Rectangle(10.0, 10.0, Graphics2DFx.toFxColor(item.color.backgroundColor))
					rect.stroke = Graphics2DFx.toFxColor(item.color.foregroundColor)
					text = item.toString()
					graphic = rect
				}
			}
		}
	}
}

class PredefinedColorEditorFx(
	item: PropertySheet.Item
) : AbstractPropertyEditor<PredefinedColor,ComboBox<PredefinedColor>>(item, ComboBox()) {

	companion object {
		/**
		 * [ComboBox] doesn't work with `null` values, so use a "null pattern" object for displaying
		 * and replace with `null` when transferring from and to property.
		 */
		private val nullPredefinedColor = PredefinedColor(PredefinedColorIdentity.Black, CompositeColor())
	}

	init {
		val colors = mutableListOf<PredefinedColor?>(nullPredefinedColor)
		colors.addAll(PredefinedColorRepository.provideAll())
		editor.items.setAll(colors)
		editor.cellFactory = PredefinedColorRendererFx
		editor.buttonCell = PredefinedColorRendererFx.call(null)
	}

	override fun setValue(value: PredefinedColor?) {
		if (value == null) {
			editor.value = nullPredefinedColor
		} else {
			editor.value = value
		}
	}

	override fun getValue(): PredefinedColor? {
		if (super.getValue() === nullPredefinedColor) {
			return null
		}
		return super.getValue()
	}

	override fun getObservableValue(): ObservableValue<PredefinedColor> {
		return editor.valueProperty()
	}
}
