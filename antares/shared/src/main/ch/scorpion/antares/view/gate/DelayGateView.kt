package ch.scorpion.antares.view.gate

import ch.scorpion.antares.model.gate.DelayGate
import ch.scorpion.antares.view.Look
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.graph.view.style.GraphStyleType

/**
 * A view of a [DelayGate].
 */
class DelayGateView(
    styleProvider: StyleProvider = DrawStyleModule.styleProvider,
    delayGate: DelayGate = DelayGate()
) : AbstractDigitalGateView<DelayGate>(styleProvider, delayGate.delay.toString(), "library.element.Delay", delayGate) {

    init {
        customFont = Look.ANNOTATION_FONT
        modelExchanged(null)
    }

    var delay: Long
        get() = model!!.delay
        set(value) {
            model!!.delay = value
            labelText = value.toString()
        }

    override fun modelExchanged(oldModel: DelayGate?) {
        super.modelExchanged(oldModel)
        labelText = delay.toString()
    }

    override fun drawImpl(context: DrawContext) {
        val oldColor = context.g.color
        super.drawImpl(context)

        if (context.useContextColors) {
            context.g.color = context.color!!.foregroundColor
        } else {
            context.g.color = foregroundColor
        }
        context.g.stroke = styleProvider.getStyle(GraphStyleType.ANNOTATION).stroke

        val sizeHalf = 12
        context.g.translate(bounds.centerX, 0.0)
        context.g.rotate(-rotation.angle)

        context.g.drawLine(-sizeHalf, 0, sizeHalf, 0)
        context.g.drawLine(-sizeHalf, -3, -sizeHalf, 3)
        context.g.drawLine(sizeHalf, -3, sizeHalf, 3)

        context.g.rotate(rotation.angle)
        context.g.translate(-bounds.centerX, 0.0)

        context.g.color = oldColor
    }
}