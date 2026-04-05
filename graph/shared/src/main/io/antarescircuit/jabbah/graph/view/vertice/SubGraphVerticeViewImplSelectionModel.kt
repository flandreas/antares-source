package io.antarescircuit.jabbah.graph.view.vertice

import io.antarescircuit.jabbah.draw.Drawable
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.edit.Component
import io.antarescircuit.jabbah.edit.SelectionDrawingStrategy
import io.antarescircuit.jabbah.edit.SelectionModel
import io.antarescircuit.jabbah.edit.SelectionModelProvider
import io.antarescircuit.jabbah.edit.select.AbstractSelectionModel
import io.antarescircuit.jabbah.base.geom.RectangularShape
import io.antarescircuit.jabbah.draw.style.Themes
import io.antarescircuit.jabbah.edit.model.image.ImageComponent
import io.antarescircuit.jabbah.edit.style.EditTheme
import io.antarescircuit.jabbah.graph.container.ControlViewComponent
import io.antarescircuit.jabbah.graph.view.ControlView

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
				is ControlViewComponent -> getControlViewSelectionModel(it).draw(context)
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

	private fun getControlViewSelectionModel(c: ControlViewComponent): SelectionModel<Component> =
        selectionModels.getOrPut(c) { provider.provideFor(c.controlView, SelectionDrawingStrategy.REPLACE)!! }

	/** ---- [SubGraphVerticeViewImplSelectionModel] */

	private fun clearSelectionModels() {
		selectionModels.values.forEach { it.dispose() }
		selectionModels.clear()
	}
}