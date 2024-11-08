package ch.scorpion.jabbah.graph.view.vertice

import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.SelectionDrawingStrategy
import ch.scorpion.jabbah.edit.SelectionModel
import ch.scorpion.jabbah.edit.SelectionModelProvider
import ch.scorpion.jabbah.edit.select.AbstractSelectionModel
import ch.scorpion.jabbah.base.geom.RectangularShape
import ch.scorpion.jabbah.draw.style.Themes
import ch.scorpion.jabbah.edit.model.image.ImageComponent
import ch.scorpion.jabbah.edit.style.EditTheme
import ch.scorpion.jabbah.graph.container.ControlViewComponent
import ch.scorpion.jabbah.graph.view.ControlView

/**
 * A [SelectionModel] for [SubGraphVerticeViewImpl] that draws its [ControlView]s using their [SelectionModel]s.
 */
class SubGraphVerticeViewImplSelectionModel(
	component: SubGraphVerticeViewImpl,
	private val provider: SelectionModelProvider
) : AbstractSelectionModel<SubGraphVerticeViewImpl>(component) {

	private val selectionModels = mutableMapOf<ControlViewComponent, SelectionModel<Component>>()

	/** ---- [Drawable] */

	override fun dispose() {
		super.dispose()
		clearSelectionModels()
	}

	override fun draw(context: DrawContext) {
		val oldUseContextColors = context.useContextColors

		context.useContextColors = true
		context.selectionColor = Themes.get<EditTheme>().selection.color
		context.g.color = context.selectionColor!!.foregroundColor
		context.color = context.selectionColor
		component.drawWithDrawableDrawer(context) {
			when (it) {
				is ControlViewComponent -> selectionModels[it]!!.draw(context)
				is ImageComponent -> it.drawSelected(context)
				else -> it.draw(context)
			}
		}

		context.useContextColors = oldUseContextColors
	}

	override val boundingBox: RectangularShape get() = component.boundingBox

	override fun contains(x: Double, y: Double): Boolean = component.contains(x, y)

	/** ---- [AbstractSelectionModel] */

	override fun componentUpdated() {
		if (component.getControlViewComponents().isNotEmpty() && selectionModels.isEmpty()) {
			component.getControlViewComponents().forEach {
				selectionModels[it] = provider.provideFor(it.controlView, SelectionDrawingStrategy.REPLACE)!!
			}
		} else {
			selectionModels.values.forEach { it.componentUpdated() }
		}
	}

	/** ---- [SubGraphVerticeViewImplSelectionModel] */

	private fun clearSelectionModels() {
		selectionModels.values.forEach { it.dispose() }
		selectionModels.clear()
	}
}