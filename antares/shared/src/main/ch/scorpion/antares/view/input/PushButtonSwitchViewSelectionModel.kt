package ch.scorpion.antares.view.input

import ch.scorpion.antares.view.style.AntaresTheme
import ch.scorpion.jabbah.base.geom.RectangularShape
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.style.Themes
import ch.scorpion.jabbah.edit.select.AbstractSelectionModel

class PushButtonSwitchViewSelectionModel(component: AbstractPushButtonSwitchView<*>)
	: AbstractSelectionModel<AbstractPushButtonSwitchView<*>>(component) {

	override fun draw(context: DrawContext) {
		val oldUseContextColors = context.useContextColors
		context.useContextColors = true
		context.color = Themes.get<AntaresTheme>().selection.color
		component.drawSelected(context)
		context.useContextColors = oldUseContextColors
	}

	override val boundingBox: RectangularShape get() = component.boundingBox

	override fun contains(x: Double, y: Double): Boolean {
		return component.contains(x, y)
	}

	override fun componentUpdated() {
		validate()
	}
}