package ch.scorpion.jabbah.edit.model.rectangle

import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.drawable.AbstractDrawable
import ch.scorpion.jabbah.draw.style.Themes
import ch.scorpion.jabbah.edit.SelectionDrawingStrategy
import ch.scorpion.jabbah.edit.SelectionModel
import ch.scorpion.jabbah.edit.select.AbstractSelectionModel
import ch.scorpion.jabbah.edit.style.EditTheme

/**
 * A [SelectionModel] for [AbstractRectangularComponent] to be used with [SelectionDrawingStrategy.BELOW].
 */
class RectangularBelowSelectionModel(
	component: AbstractRectangularComponent
) : AbstractSelectionModel<AbstractRectangularComponent>(component) {

    companion object {
        private const val OUTSET: Int = 5
    }

    private val bounds: Rectangle2D = Rectangle2D()

    /** ---- [AbstractDrawable] */

    override val boundingBox: Rectangle2D
        get() = Rectangle2D(bounds.x - 1, bounds.y - 1, bounds.width + 2, bounds.height + 2)

    override fun draw(context: DrawContext) {
        context.g.color = Themes.get<EditTheme>().selection.color.foregroundColor
        context.g.fillRect(bounds.x.toInt(), bounds.y.toInt(), bounds.width.toInt(), bounds.height.toInt())
    }

    override fun contains(x: Double, y: Double): Boolean {
        return bounds.contains(x, y)
    }

    /** ---- [AbstractSelectionModel] */

    override fun componentUpdated() {
        invalidate()
        bounds.setFrame(
                component.shape.x - OUTSET,
                component.shape.y - OUTSET,
                component.shape.width + 2 * OUTSET,
                component.shape.height + 2 * OUTSET
        )
        invalidate()
        validate()
    }
}