package ch.scorpion.antares.view.gate

import ch.scorpion.antares.model.gate.NonUnaryLogicGate
import ch.scorpion.antares.view.module.AntaresViewModule
import ch.scorpion.antares.view.symbolstyle.CurrentSymbolStyle
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.Stroke
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider

class XnorGateView(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	currentSymbolStyle: CurrentSymbolStyle = AntaresViewModule.currentSymbolStyle,
	xnorGate: NonUnaryLogicGate = NonUnaryLogicGate.xnorGate()
) : AbstractOrLikeGateView<NonUnaryLogicGate>(styleProvider, currentSymbolStyle, "=1", xnorGate) {

	override fun drawShape(context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke) {
		currentSymbolStyle.symbolStyle.drawXnorGate(this, context, foregroundColor, backgroundColor, stroke)
	}

	override fun drawMnemonics(context: DrawContext, foregroundColor: Color, backgroundColor: Color) {
		GateMnemonic.drawXnor(this, context, foregroundColor, backgroundColor, -currentSymbolStyle.symbolStyle.orShapeConnectedPortViewLength)
	}
}