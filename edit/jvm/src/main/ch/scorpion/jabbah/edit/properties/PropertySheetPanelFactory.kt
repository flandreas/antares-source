package ch.scorpion.jabbah.edit.properties

import com.l2fprod.common.propertysheet.PropertyEditorRegistry
import com.l2fprod.common.propertysheet.PropertyRendererRegistry
import com.l2fprod.common.propertysheet.PropertySheet
import com.l2fprod.common.propertysheet.PropertySheetPanel

interface PropertySheetPanelFactory {
    fun create(): PropertySheetPanel
}

class PropertySheetPanelFactoryImpl(
    private val rendererRegistry: PropertyRendererRegistry,
    private val editorRegistry: PropertyEditorRegistry
) : PropertySheetPanelFactory {

    override fun create(): PropertySheetPanel {
        val sheet = PropertySheetPanel(JabbahPropertySheetButtonProvider())
        sheet.rendererFactory = rendererRegistry
        sheet.editorFactory = editorRegistry
        sheet.setMode(PropertySheet.VIEW_AS_FLAT_LIST)
        return sheet
    }
}
