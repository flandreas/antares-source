package io.antarescircuit.jabbah.graph.view.vertice

import io.antarescircuit.jabbah.base.geom.RectangularShape
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.Drawable
import io.antarescircuit.jabbah.draw.InputEventContext
import io.antarescircuit.jabbah.draw.InputEventHandler
import io.antarescircuit.jabbah.draw.style.Themes
import io.antarescircuit.jabbah.edit.Component
import io.antarescircuit.jabbah.edit.DrawingView
import io.antarescircuit.jabbah.edit.SelectionDrawingStrategy
import io.antarescircuit.jabbah.edit.SelectionModel
import io.antarescircuit.jabbah.edit.SelectionModelProvider
import io.antarescircuit.jabbah.edit.model.image.ImageComponent
import io.antarescircuit.jabbah.edit.model.rectangle.RectangularHandleSelectionModel
import io.antarescircuit.jabbah.edit.select.AbstractSelectionModel
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

	private var selectedView: DrawingView<*, *>? = null
	private var handlesAdded = false
	private val graphHandles = RectangularHandleSelectionModel(component)

	private val selectionModels = mutableMapOf<ControlViewComponent, SelectionModel<Component>>()

	override fun notifyAdded(view: DrawingView<*, *>) {
		selectedView = view
		graphHandles.notifyAdded(view)
		updateGraphHandles()
	}

	override fun notifyRemoved(view: DrawingView<*, *>) {
		view.ghostContainer.remove(graphHandles)
		selectedView = null
		handlesAdded = false
	}

	override fun <T : InputEventContext> getInputEventHandler(context: T): InputEventHandler<T> =
		if (component.displayMode == SubGraphDisplayMode.GRAPH) graphHandles.getInputEventHandler(context) else super.getInputEventHandler(context)

	/** ---- [Drawable] */

	override fun dispose() {
		selectedView?.ghostContainer?.remove(graphHandles)
		selectedView = null
		super.dispose()
		graphHandles.dispose()
		clearSelectionModels()
	}

	override fun draw(context: DrawContext) {
		if (component.displayMode == SubGraphDisplayMode.GRAPH) {
			val oldColor = context.color
			val oldUseContextColors = context.useContextColors
			context.color = Themes.get<EditTheme>().selection.color
			context.useContextColors = true
			try { component.draw(context) } finally {
				context.color = oldColor
				context.useContextColors = oldUseContextColors
			}
			return
		}
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

	private fun updateGraphHandles() {
		val view = selectedView ?: return
		val wanted = component.displayMode == SubGraphDisplayMode.GRAPH && view.editable
		if (wanted && !handlesAdded) view.ghostContainer.add(graphHandles)
		if (!wanted && handlesAdded) view.ghostContainer.remove(graphHandles)
		handlesAdded = wanted
	}

	override fun componentUpdated() {
		updateGraphHandles()
		graphHandles.componentUpdated()
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
