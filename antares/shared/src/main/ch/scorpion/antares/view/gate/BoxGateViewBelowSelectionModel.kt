package ch.scorpion.antares.view.gate

import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.graphics.Stroke
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.edit.SelectionModel
import ch.scorpion.jabbah.edit.SelectionDrawingStrategy
import ch.scorpion.jabbah.edit.select.AbstractSelectionModel
import ch.scorpion.jabbah.edit.style.EditStyleType
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.geom.RectangularShape

/**
 * A [SelectionModel] for [SelectionDrawingStrategy.BELOW] of [BoxGateView]s.
 * It draws the outline of the [BoxGateView] with a very thick [Stroke], which looks like the [SelectionModel] would
 * be slightly bigger than the [BoxGateView].
 */
class BoxGateViewBelowSelectionModel(
    component: BoxGateView<*>,
    private val styleProvider: StyleProvider
) : AbstractSelectionModel<BoxGateView<*>>(component) {

    @Suppress("unused")
    constructor(component: BoxGateView<*>): this(component, DrawStyleModule.styleProvider)

    private var bounds = Rectangle2D()

    /** ---- [AbstractSelectionModel] */

    override val boundingBox: RectangularShape = bounds

    override fun draw(context: DrawContext) {
        component.draw(context) {
	        val style = styleProvider.getStyle(EditStyleType.HIGHLIGHT)
	        component.drawShape(
		        it,
		        style.color.foregroundColor,
		        style.color.backgroundColor,
		        style.stroke)
        }
    }

    override fun componentUpdated() {
        invalidate()
        val bbox = component.boundingBox
        val outset = strokeWidth / 2
        bounds = Rectangle2D(
                bbox.x - outset,
                bbox.y - outset,
                bbox.width + 2 * outset,
                bbox.height + 2 * outset)
        invalidate()
        validate()
    }

    override fun contains(x: Double, y: Double): Boolean = bounds.contains(x, y)

    /** ---- [BoxGateViewBelowSelectionModel] */

    private val strokeWidth: Float = styleProvider.getStyle(EditStyleType.HIGHLIGHT).stroke.width

}