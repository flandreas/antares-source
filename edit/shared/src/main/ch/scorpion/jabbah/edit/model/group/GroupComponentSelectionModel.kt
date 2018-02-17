package ch.scorpion.jabbah.edit.model.group

import ch.scorpion.jabbah.base.geom.RectangularShape
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.style.Themes
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.SelectionDrawingStrategy
import ch.scorpion.jabbah.edit.SelectionModel
import ch.scorpion.jabbah.edit.SelectionModelProvider
import ch.scorpion.jabbah.edit.select.AbstractSelectionModel
import ch.scorpion.jabbah.edit.select.EditSelectModule
import ch.scorpion.jabbah.edit.select.SelectedColorSelectionModel
import ch.scorpion.jabbah.edit.style.EditTheme

/** A [SelectionModel] for [GroupComponent] that draws the [SelectionModel]s of the grouped [Component]s.*/
class GroupComponentSelectionModel(
	model: GroupComponent,
	private val provider: SelectionModelProvider = EditSelectModule.selectionModelProvider
) : AbstractSelectionModel<GroupComponent>(model) {

	private val selectionModels = mutableMapOf<Component, SelectionModel<Component>>()

	init {
		component.components.forEach {
			selectionModels.put(it, provider.provideFor(it, SelectionDrawingStrategy.REPLACE) ?: SelectedColorSelectionModel(it))
		}
	}

	/** ---- [Drawable] */

	override val boundingBox: RectangularShape get() = component.boundingBox

	override fun draw(context: DrawContext) {
		val oldUseContextColors = context.useContextColors

		context.useContextColors = true
		context.selectionColor = Themes.get<EditTheme>().selection
		context.g.color = context.selectionColor!!.foregroundColor
		context.color = context.selectionColor
		component.components.forEach { selectionModels[it]!!.draw(context) }

		context.useContextColors = oldUseContextColors
	}

	override fun contains(x: Double, y: Double): Boolean = component.contains(x, y)

	override fun componentUpdated() {
		// empty
	}
}