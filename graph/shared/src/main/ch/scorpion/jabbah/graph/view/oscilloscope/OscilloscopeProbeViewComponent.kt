package ch.scorpion.jabbah.graph.view.oscilloscope

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.RectangularShape
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.graphics.CompositeColor
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.edit.model.AbstractComponent
import ch.scorpion.jabbah.graph.view.GraphView

/** The probe view that is being dragged around and placed in the [GraphView].*/
class OscilloscopeProbeViewComponent(
        rowNumber: Int,
        color: CompositeColor,
        styleProvider: StyleProvider = DrawStyleModule.styleProvider
) : AbstractComponent() {

    private val drawable = OscilloscopeProbeViewDrawable(Point2D(), rowNumber, color, styleProvider)

    var rowNumber: Int
        get() = drawable.rowNumber
        set(value) {
            invalidate()
            drawable.rowNumber = value
            validate()
        }

    override val boundingBox: RectangularShape get() =  drawable.boundingBox

    override fun draw(context: DrawContext) {
        drawable.draw(context)
    }

    override fun contains(x: Double, y: Double): Boolean {
        return drawable.contains(x, y)
    }

    override val type: String? get() = null

    override var location: Point2D
        get() = drawable.location
        set(value) {
            invalidate()
            drawable.location = value
            invalidate()
            validate()
        }
}