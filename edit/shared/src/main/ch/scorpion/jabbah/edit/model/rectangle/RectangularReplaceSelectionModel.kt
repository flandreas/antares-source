package ch.scorpion.jabbah.edit.model.rectangle

import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.geom.RectangularShape
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.style.Themes
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.SelectionDrawingStrategy
import ch.scorpion.jabbah.edit.SelectionModel
import ch.scorpion.jabbah.edit.select.AbstractHandleSelectionModel
import ch.scorpion.jabbah.edit.select.AbstractSelectedColorWrappingSelectionModel
import ch.scorpion.jabbah.edit.style.EditTheme

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