package io.antarescircuit.jabbah.edit.select

import io.antarescircuit.jabbah.base.geom.RectangularShape
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.style.Themes
import io.antarescircuit.jabbah.edit.Component
import io.antarescircuit.jabbah.edit.SelectionModel
import io.antarescircuit.jabbah.edit.style.EditTheme

val selectedColorSelectionModelFactory: (Component) -> SelectionModel<Component> = { SelectedColorSelectionModel(it) }

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