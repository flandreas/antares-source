package ch.scorpion.antares.view.gate

import ch.scorpion.antares.model.gate.NonUnaryLogicGate
import ch.scorpion.antares.view.module.AntaresViewModule
import ch.scorpion.antares.view.symbolstyle.CurrentSymbolStyle
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.Stroke
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider

class NorGateView(
    styleProvider: StyleProvider = DrawStyleModule.styleProvider,
    currentSymbolStyle: CurrentSymbolStyle = AntaresViewModule.currentSymbolStyle,
    norGate: NonUnaryLogicGate = NonUnaryLogicGate.norGate()
) : AbstractOrLikeGateView<NonUnaryLogicGate>(styleProvider, currentSymbolStyle, "≥1", norGate) {
    override fun drawShape(context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke) {
        currentSymbolStyle.symbolStyle.drawNorGate(this, context, foregroundColor, backgroundColor, stroke)
    }

	override fun drawMnemonics(context: DrawContext, foregroundColor: Color, backgroundColor: Color) {
		GateMnemonic.drawNor(this, context, foregroundColor, backgroundColor)
	}
}