package io.antarescircuit.jabbah.edit.select

import io.antarescircuit.jabbah.base.geom.Rectangle2D
import io.antarescircuit.jabbah.base.geom.RectangularShape
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.Drawable
import io.antarescircuit.jabbah.draw.style.DrawStyleModule
import io.antarescircuit.jabbah.draw.style.StyleProvider
import io.antarescircuit.jabbah.draw.style.StyleType
import io.antarescircuit.jabbah.edit.Component
import io.antarescircuit.jabbah.edit.SelectionDrawingStrategy
import io.antarescircuit.jabbah.edit.SelectionModel
import io.antarescircuit.jabbah.edit.style.EditStyleType

/**
 * A [SelectionModel] for [SelectionDrawingStrategy.BELOW] that draws a rounded rectangle that is slightly
 * larger than the [Component]'s bounding box.
 */
class BoundingBoxBelowSelectionModel(
	component: Component,
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	styleType: StyleType = EditStyleType.SELECTION,
	outset: Int = DEF_OUTSET
) : AbstractBelowSelectionModel<Component>(component, styleProvider, styleType, outset) {

	private var bounds = Rectangle2D()

	/** ---- [Drawable] interface */

	override val boundingBox: RectangularShape get() = bounds

	override fun contains(x: Double, y: Double): Boolean = bounds.contains(x, y)

	override fun draw(context: DrawContext) {
		val oldColor = context.g.color
		context.g.color = color.foregroundColor

		context.g.fillRoundRect(
			bounds.x.toInt(),
			bounds.y.toInt(),
			bounds.width.toInt(),
			bounds.height.toInt(),
			ARC_SIZE, ARC_SIZE)

		context.g.color = oldColor
	}

	/** ---- [AbstractSelectionModel] */

	override fun componentUpdated() {
		invalidate()
		val bbox = component.boundingBox
		bounds = Rectangle2D(
			bbox.x - outset,
			bbox.y - outset,
			bbox.width + 2 * outset,
			bbox.height + 2 * outset)
		invalidate()
		validate()
	}
}