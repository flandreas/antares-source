package ch.scorpion.antares.view.input

import ch.scorpion.antares.model.input.Clock
import ch.scorpion.antares.view.Look
import ch.scorpion.antares.view.gate.AbstractDigitalGateView
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.base.geom.Path
import ch.scorpion.jabbah.graph.view.style.GraphStyleType
import ch.scorpion.jabbah.base.System


/**
 * A view representation of a [Clock].
 */
class ClockView(
    styleProvider: StyleProvider,
    model: Clock
) : AbstractDigitalGateView<Clock>(styleProvider, "", "library.element.Clock", model) {

    constructor(styleProvider: StyleProvider): this(styleProvider, Clock())
    constructor(): this(DrawStyleModule.styleProvider)

    companion object {
        val SEG_X = Look.SCALE
        val SEG_Y = SEG_X * 3 / 2
        val ICON_PATH = createIconPath()

        fun createIconPath(): Path {
            return System.get().createPath()
                .moveTo(0, 0)
                .lineTo(SEG_X, 0)
                .lineTo(SEG_X, -SEG_Y)
                .lineTo(2 * SEG_X, -SEG_Y)
                .lineTo(2 * SEG_X, 0)
                .lineTo(3 * SEG_X, 0)
        }
    }

    init {
        modelExchanged(null)
    }

    /** ---- Properties */

    /** Contains the period of this [ClockView] in microseconds.*/
    var period: Long
        get() = model!!.propagationDelay / 1_000
        set(value) {
            model!!.propagationDelay = value * 1_000
        }

    var isEnabled: Boolean
        get() = model!!.isEnabled
        set(value) {
            model!!.isEnabled = value
        }

    /** ---- [Drawable] */

    override fun drawImpl(context: DrawContext) {
        val oldColor = context.g.color
        super.drawImpl(context)

        if (context.useContextColors) {
            context.g.color = context.color!!.foregroundColor
        } else {
            context.g.color = foregroundColor
        }
        context.g.stroke = styleProvider.getStyle(GraphStyleType.ANNOTATION).stroke

        val dx = bounds.centerX - 3 * SEG_X / 2
        val dy = bounds.centerY - SEG_Y / 2

        context.g.translate(dx, dy)
        context.g.draw(ICON_PATH)
        context.g.translate(-dx, -dy)

        context.g.color = oldColor
    }
}