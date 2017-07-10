package ch.scorpion.antares.view.gate

import ch.scorpion.antares.model.gate.XnorGate
import ch.scorpion.antares.view.module.AntaresViewModule
import ch.scorpion.antares.view.symbolstyle.CurrentSymbolStyle
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.Stroke

/**
 * A view of a [XnorGate].
 */
class XnorGateView(
        styleProvider: StyleProvider = DrawStyleModule.styleProvider,
        currentSymbolStyle: CurrentSymbolStyle = AntaresViewModule.currentSymbolStyle,
        xnorGate: XnorGate = XnorGate()
) : AbstractOrLikeGateView<XnorGate>(styleProvider, currentSymbolStyle, "=1", "library.element.XnorGate", xnorGate) {

    override fun drawShape(context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke) {
        currentSymbolStyle.symbolStyle.drawXnorGate(this, context, foregroundColor, backgroundColor, stroke)
        GateMnemonic.drawXnor(this, context, foregroundColor, backgroundColor)
    }
}