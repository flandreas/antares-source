package ch.scorpion.antares.view.gate

import ch.scorpion.antares.model.gate.OrGate
import ch.scorpion.antares.view.module.AntaresViewModule
import ch.scorpion.antares.view.symbolstyle.CurrentSymbolStyle
import ch.scorpion.antares.view.truthtable.TruthTableView
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.DrawableExplanation
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.Stroke
import ch.scorpion.jabbah.graph.view.style.GraphStyleType

/**
 * A view of a [OrGate].
 */
class OrGateView(
    styleProvider: StyleProvider = DrawStyleModule.styleProvider,
    currentSymbolStyle: CurrentSymbolStyle = AntaresViewModule.currentSymbolStyle,
    orGate: OrGate = OrGate()
) : AbstractOrLikeGateView<OrGate>(styleProvider, currentSymbolStyle, "≥1", "library.element.OrGate", orGate) {

    private val explanation: DrawableExplanation by lazy {
        DrawableExplanation(
                TruthTableView(OrGate.TRUTH_TABLE, model!!, GraphStyleType.EXPLANATION),
                Point2D(boundingBox.centerX, boundingBox.minY))
    }

    override fun drawShape(context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke) {
        currentSymbolStyle.symbolStyle.drawOrGate(this, context, foregroundColor, backgroundColor, stroke)
        GateMnemonic.drawOr(this, context, foregroundColor, backgroundColor)
    }

    override fun getExplanation(x: Double, y: Double): DrawableExplanation? {
        return if (model!!.inputCount == 2) explanation else null
    }
}