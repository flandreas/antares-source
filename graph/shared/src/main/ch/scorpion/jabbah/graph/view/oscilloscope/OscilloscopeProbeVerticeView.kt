package ch.scorpion.jabbah.graph.view.oscilloscope

import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.graphics.CompositeColor
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.graph.model.oscilloscope.OscilloscopeProbe
import ch.scorpion.jabbah.graph.view.port.GenericPortView
import ch.scorpion.jabbah.graph.view.vertice.AbstractRectangularVerticeView

/**
 * The location of this [OscilloscopeProbeVerticeView] as a [Locatable] is the tip of the bubble shape, which is also
 * the connection point.
 * @param T the type of signal that this [OscilloscopeProbeVerticeView]'s [OscilloscopeProbe] can consume.
 */
class OscilloscopeProbeVerticeView<T: Any>(
        rowNumber: Int,
        color: CompositeColor,
        model: OscilloscopeProbe<T>? = OscilloscopeProbe(),
        styleProvider: StyleProvider = DrawStyleModule.styleProvider
) : AbstractRectangularVerticeView<OscilloscopeProbe<T>>(styleProvider, "graph.component.oscilloscope.port", model) {

    companion object {
        private val LOG by logger(OscilloscopeProbeVerticeView::class)
    }

    init {
        modelExchanged(null)
        setBounds(0.0, -OscilloscopeProbeViewDrawable.SIZE, OscilloscopeProbeViewDrawable.SIZE, OscilloscopeProbeViewDrawable.SIZE)
    }

    var rowNumber: Int
        get() = drawable.rowNumber
        set(value) {
            invalidate()
            drawable.rowNumber = value
            validate()
        }

    private val drawable = OscilloscopeProbeViewDrawable(Point2D(0.0, -OscilloscopeProbeViewDrawable.SIZE), rowNumber, color, styleProvider)

    override val type: String? get() = null

    /** ---- [AbstractRectangularVerticeView] */

    override fun modelExchanged(oldModel: OscilloscopeProbe<T>?) {
        super.modelExchanged(oldModel)
        addPortView(GenericPortView<T>(model!!.getInput(), 0, 0, Direction.SOUTH))
    }

    override fun drawImpl(context: DrawContext, drawPortViews: Boolean) {
        super.drawImpl(context, drawPortViews)
        drawable.draw(context)
    }
}