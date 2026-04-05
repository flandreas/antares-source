package io.antarescircuit.jabbah.edit.model.rectangle

import io.antarescircuit.jabbah.base.geom.Rectangle2D
import io.antarescircuit.jabbah.base.geom.RectangularShape
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.drawable.AbstractDrawable
import io.antarescircuit.jabbah.draw.graphics.Stroke
import io.antarescircuit.jabbah.draw.style.Themes
import io.antarescircuit.jabbah.edit.SelectionDrawingStrategy
import io.antarescircuit.jabbah.edit.SelectionModel
import io.antarescircuit.jabbah.edit.select.AbstractSelectionModel
import io.antarescircuit.jabbah.edit.style.EditTheme

/**
 * A [SelectionModel] for [AbstractRectangularComponent] to be used with [SelectionDrawingStrategy.BELOW].
 */
class RectangularBelowSelectionModel(
	component: AbstractRectangularComponent
) : AbstractSelectionModel<AbstractRectangularComponent>(component) {

	companion object {
		private const val WITH_HALF: Int = 5
		private const val WITH = 2 * WITH_HALF
		private val STROKE = Stroke(WITH.toFloat())
	}

	private val bounds: Rectangle2D = Rectangle2D()

	/** ---- [AbstractDrawable] */

	override val boundingBox: RectangularShape
		get() = Rectangle2D(bounds.x - WITH_HALF - 1, bounds.y - WITH_HALF - 1, bounds.width + WITH + 1, bounds.height + WITH + 1)

	override fun draw(context: DrawContext) {
		context.g.stroke = STROKE
		context.g.color = Themes.get<EditTheme>().selection.color.foregroundColor
		context.g.draw(bounds)
	}

	override fun contains(x: Double, y: Double): Boolean {
		return bounds.contains(x, y)
	}

	/** ---- [AbstractSelectionModel] */

	override fun componentUpdated() {
		invalidate()
		bounds.setFrame(
			component.shape.x,
			component.shape.y,
			component.shape.width,
			component.shape.height
		)
		invalidate()
		validate()
	}
}