package ch.scorpion.antares.view.gate

import ch.scorpion.antares.model.gate.NotGate
import ch.scorpion.antares.view.module.AntaresViewModule
import ch.scorpion.antares.view.symbolstyle.CurrentSymbolStyle
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.Stroke

/**
 * A view of a [NotGate].
 */
class NotGateView(
    styleProvider: StyleProvider = DrawStyleModule.styleProvider,
    val currentSymbolStyle: CurrentSymbolStyle = AntaresViewModule.currentSymbolStyle,
    notGate: NotGate = NotGate()
) : AbstractDigitalGateView<NotGate>(styleProvider, "1", "library.element.NotGate", notGate) {

    init {
        modelExchanged(null)
    }

    override fun drawShape(context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke) {
        currentSymbolStyle.symbolStyle.drawNotGate(this, context, foregroundColor, backgroundColor, stroke)
        GateMnemonic.drawNot(this, context, foregroundColor, backgroundColor)
    }
}