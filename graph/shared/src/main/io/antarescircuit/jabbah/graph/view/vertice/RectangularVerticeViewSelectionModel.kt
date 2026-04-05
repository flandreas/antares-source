package io.antarescircuit.jabbah.graph.view.vertice

import io.antarescircuit.jabbah.base.geom.RectangularShape
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.style.Themes
import io.antarescircuit.jabbah.edit.SelectionModel
import io.antarescircuit.jabbah.edit.select.AbstractSelectionModel
import io.antarescircuit.jabbah.edit.style.EditTheme

/**
 * A [SelectionModel] that calls [AbstractRectangularVerticeView.drawSelected].
 */
class RectangularVerticeViewSelectionModel(component: AbstractRectangularVerticeView<*>)
	: AbstractSelectionModel<AbstractRectangularVerticeView<*>>(component) {

	override fun draw(context: DrawContext) {
		val oldUseContextColors = context.useContextColors
		context.useContextColors = true
		context.color = Themes.get<EditTheme>().selection.color
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