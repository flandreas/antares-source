package io.antarescircuit.antares.view.gate

import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.graphics.Stroke
import io.antarescircuit.jabbah.draw.style.DrawStyleModule
import io.antarescircuit.jabbah.draw.style.StyleProvider
import io.antarescircuit.jabbah.edit.SelectionModel
import io.antarescircuit.jabbah.edit.SelectionDrawingStrategy
import io.antarescircuit.jabbah.edit.select.AbstractSelectionModel
import io.antarescircuit.jabbah.edit.style.EditStyleType
import io.antarescircuit.jabbah.base.geom.Rectangle2D
import io.antarescircuit.jabbah.base.geom.RectangularShape

/**
 * A [SelectionModel] for [SelectionDrawingStrategy.BELOW] of [BoxGateView]s.
 * It draws the outline of the [BoxGateView] with a very thick [Stroke], which looks like the [SelectionModel] would
 * be slightly bigger than the [BoxGateView].
 */
class BoxGateViewBelowSelectionModel(
    component: BoxGateView<*>,
    private val styleProvider: StyleProvider = DrawStyleModule.styleProvider,
    private val styleType: EditStyleType = EditStyleType.SELECTION
) : AbstractSelectionModel<BoxGateView<*>>(component) {

    private var bounds = Rectangle2D()

    /** ---- [AbstractSelectionModel] */

    override val boundingBox: RectangularShape = bounds

    override fun draw(context: DrawContext) {
        component.draw(context) {
	        val style = styleProvider.getStyle(styleType)
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