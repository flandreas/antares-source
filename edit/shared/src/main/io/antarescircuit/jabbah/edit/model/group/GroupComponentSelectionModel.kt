package io.antarescircuit.jabbah.edit.model.group

import io.antarescircuit.jabbah.base.geom.RectangularShape
import io.antarescircuit.jabbah.draw.Drawable
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.style.Themes
import io.antarescircuit.jabbah.edit.Component
import io.antarescircuit.jabbah.edit.SelectionDrawingStrategy
import io.antarescircuit.jabbah.edit.SelectionModel
import io.antarescircuit.jabbah.edit.SelectionModelProvider
import io.antarescircuit.jabbah.edit.select.AbstractSelectionModel
import io.antarescircuit.jabbah.edit.select.EditSelectModule
import io.antarescircuit.jabbah.edit.select.SelectedColorSelectionModel
import io.antarescircuit.jabbah.edit.style.EditTheme

/** A [SelectionModel] for [GroupComponent] that draws the [SelectionModel]s of the grouped [Component]s.*/
class GroupComponentSelectionModel(
	model: GroupComponent,
	private val provider: SelectionModelProvider = EditSelectModule.selectionModelProvider
) : AbstractSelectionModel<GroupComponent>(model) {

	private val selectionModels = mutableMapOf<Component, SelectionModel<Component>>()

	init {
		component.components.forEach {
			selectionModels[it] = provider.provideFor(it, SelectionDrawingStrategy.REPLACE)
				?: SelectedColorSelectionModel(it)
		}
	}

	/** ---- [Drawable] */

	override val boundingBox: RectangularShape get() = component.boundingBox

	override fun draw(context: DrawContext) {
		val oldUseContextColors = context.useContextColors

		context.useContextColors = true
		context.selectionColor = Themes.get<EditTheme>().selection.color
		context.g.color = context.selectionColor!!.foregroundColor
		context.color = context.selectionColor
		component.components.asReversed().forEach { selectionModels[it]!!.draw(context) }

		context.useContextColors = oldUseContextColors
	}

	override fun contains(x: Double, y: Double): Boolean = component.contains(x, y)

	override fun componentUpdated() {
		// empty
	}
}