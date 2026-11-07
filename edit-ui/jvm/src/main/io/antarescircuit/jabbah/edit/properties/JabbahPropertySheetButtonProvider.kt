package io.antarescircuit.jabbah.edit.properties

import io.antarescircuit.jabbah.base.swing.UiUtil
import com.l2fprod.common.propertysheet.PropertySheetButtonProvider
import javax.swing.AbstractButton
import javax.swing.Action

class JabbahPropertySheetButtonProvider : PropertySheetButtonProvider {

	override fun getToggleSortButton(action: Action): AbstractButton {
		val button = UiUtil.createToolBarButton(action, toggle = true)
		button.icon = UiUtil.themedIcon("/img/sort.png")
		return button
	}

	override fun getToggleDescriptionButton(action: Action): AbstractButton {
		val button = UiUtil.createToolBarButton(action, toggle = true)
		button.icon = UiUtil.themedIcon("/img/description.png")
		return button
	}

	override fun getToggleModeButton(action: Action): AbstractButton {
		val button = UiUtil.createToolBarButton(action, toggle = true)
		button.icon = UiUtil.themedIcon("/img/category.png")
		return button
	}
}