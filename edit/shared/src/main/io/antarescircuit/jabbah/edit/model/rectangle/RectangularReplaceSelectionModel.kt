package io.antarescircuit.jabbah.edit.model.rectangle

import io.antarescircuit.jabbah.base.geom.Rectangle2D
import io.antarescircuit.jabbah.base.geom.RectangularShape
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.Drawable
import io.antarescircuit.jabbah.draw.style.Themes
import io.antarescircuit.jabbah.edit.Component
import io.antarescircuit.jabbah.edit.SelectionDrawingStrategy
import io.antarescircuit.jabbah.edit.SelectionModel
import io.antarescircuit.jabbah.edit.select.AbstractHandleSelectionModel
import io.antarescircuit.jabbah.edit.select.AbstractSelectedColorWrappingSelectionModel
import io.antarescircuit.jabbah.edit.style.EditTheme

/**
 * A [SelectionModel] for [AbstractRectangularComponent] to be used with [SelectionDrawingStrategy.REPLACE].
 */
open class RectangularReplaceSelectionModel(
	component: AbstractRectangularComponent,
	private val drawStrategy: DrawStrategy = DrawStrategy.SHAPE
) : AbstractSelectedColorWrappingSelectionModel<AbstractRectangularComponent>(component) {

	enum class DrawStrategy {

		/** Draws only the shape of the [Component] in selection color.*/
		SHAPE {
			override fun draw(component: AbstractRectangularComponent, context: DrawContext) {
				val oldStroke = context.g.stroke
				context.g.color = Themes.get<EditTheme>().selection.color.foregroundColor
				context.g.stroke = component.stroke
				context.g.draw(component.shapeToDraw)
				component.drawText(context)
				context.g.stroke = oldStroke
			}
		},

		/** Draws the entire [Component] in selection color.*/
		COMPONENT {
			override fun draw(component: AbstractRectangularComponent, context: DrawContext) {
				val oldUseContextColor = context.useContextColors
				val oldContextColor = context.color
				context.useContextColors = true
				context.color = Themes.get<EditTheme>().selection.color
				component.draw(context)
				context.useContextColors = oldUseContextColor
				context.color = oldContextColor
			}
		};

		abstract fun draw(component: AbstractRectangularComponent, context: DrawContext)
	}

	/** ---- [Drawable] */

	override val boundingBox: RectangularShape
		get() = Rectangle2D(component.boundingBox).expandBy(component.stroke.width.toDouble())

	override fun draw(context: DrawContext) {
		drawStrategy.draw(component, context)
	}

	override fun contains(x: Double, y: Double): Boolean = handleSelectionModel.contains(x, y)

	/** ---- [AbstractSelectedColorWrappingSelectionModel] */

	override fun createInnerSelectionModel(component: AbstractRectangularComponent): AbstractHandleSelectionModel<AbstractRectangularComponent> {
		return RectangularHandleSelectionModel(component)
	}
}