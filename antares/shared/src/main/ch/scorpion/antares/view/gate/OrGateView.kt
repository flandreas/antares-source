package ch.scorpion.antares.view.gate

import ch.scorpion.antares.model.gate.NonUnaryLogicGate
import ch.scorpion.antares.view.module.AntaresViewModule
import ch.scorpion.antares.view.symbolstyle.CurrentSymbolStyle
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.Stroke
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider

class OrGateView(
    styleProvider: StyleProvider = DrawStyleModule.styleProvider,
    currentSymbolStyle: CurrentSymbolStyle = AntaresViewModule.currentSymbolStyle,
    orGate: NonUnaryLogicGate = NonUnaryLogicGate.orGate()
) : AbstractOrLikeGateView<NonUnaryLogicGate>(styleProvider, currentSymbolStyle, "≥1", orGate) {

    override fun drawShape(context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke) {
        currentSymbolStyle.symbolStyle.drawOrGate(this, context, foregroundColor, backgroundColor, stroke)
    }

	override fun drawMnemonics(context: DrawContext, foregroundColor: Color, backgroundColor: Color) {
		GateMnemonic.drawOr(this, context, foregroundColor, backgroundColor, -currentSymbolStyle.symbolStyle.orShapeConnectedPortViewLength)
	}
}