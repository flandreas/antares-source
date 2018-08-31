package ch.scorpion.antares.view.gate

import ch.scorpion.antares.model.gate.NotGate
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

/**
 * A view of a [NotGate].
 */
class NotGateView(
    styleProvider: StyleProvider = DrawStyleModule.styleProvider,
    val currentSymbolStyle: CurrentSymbolStyle = AntaresViewModule.currentSymbolStyle,
    notGate: NotGate = NotGate()
) : AbstractDigitalGateView<NotGate>(styleProvider, "1", "library.element.NotGate", notGate) {

    companion object {
        private val EXPLANATION: DrawableExplanation<TruthTableView> = DrawableExplanation(
                TruthTableView(NotGate.TRUTH_TABLE, null), Point2D.ZERO)
    }

    init {
        modelExchanged(null)
    }

    override fun getExplanation(x: Double, y: Double): DrawableExplanation<*>? {
        EXPLANATION.explanation.vertice = model
        EXPLANATION.location = Point2D(boundingBox.centerX, boundingBox.minY)
        return EXPLANATION
    }

    override fun drawShape(context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke) {
        currentSymbolStyle.symbolStyle.drawNotGate(this, context, foregroundColor, backgroundColor, stroke)
        GateMnemonic.drawNot(this, context, foregroundColor, backgroundColor)
    }
}