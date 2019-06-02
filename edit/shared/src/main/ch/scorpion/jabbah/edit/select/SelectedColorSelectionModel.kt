package ch.scorpion.jabbah.edit.select

import ch.scorpion.jabbah.base.geom.RectangularShape
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.style.Themes
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.SelectionModel
import ch.scorpion.jabbah.edit.style.EditTheme

/**
 * A [SelectionModel] that renders a [Component] in the selection color as of [EditTheme.selection].
 */
open class SelectedColorSelectionModel<T : Component>(component: T) : AbstractSelectionModel<T>(component) {

	override fun draw(context: DrawContext) {
		val oldColor = context.g.color
		val oldUseContextColors = context.useContextColors

		context.g.color = Themes.get<EditTheme>().selection.color.foregroundColor
		context.useContextColors = true
		context.color = Themes.get<EditTheme>().selection.color

		component.draw(context)
		context.g.color = oldColor
		context.useContextColors = oldUseContextColors
	}

	override val boundingBox: RectangularShape
		get() = component.boundingBox

	override fun contains(x: Double, y: Double): Boolean {
		return component.contains(x, y)
	}

	override fun componentUpdated() {
		validate()
	}
}