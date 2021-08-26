package ch.scorpion.antares.view.gate

import ch.scorpion.antares.model.gate.NotGate
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.view.module.AntaresViewModule
import ch.scorpion.antares.view.symbolstyle.CurrentSymbolStyle
import ch.scorpion.antares.view.truthtable.TruthTableView
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.resettableLazy
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.DrawableExplanation
import ch.scorpion.jabbah.draw.drawable.RectangularDrawable
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.Stroke
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider

/** A view of a [NotGate].*/
class NotGateView(
    styleProvider: StyleProvider = DrawStyleModule.styleProvider,
    private val currentSymbolStyle: CurrentSymbolStyle = AntaresViewModule.currentSymbolStyle,
    notGate: NotGate = NotGate()
) : BoxGateView<NotGate>(styleProvider, "1", notGate), CustomShapeContent {

	private val explanation = resettableLazy {
		DrawableExplanation(
			TruthTableView(NotGate.TRUTH_TABLE, model, passive = model.bitWidth.width > 1),
			Rectangle2D.ZERO)
	}

    init {
        modelExchanged(null)
    }

	override fun modelExchanged(oldModel: NotGate?) {
		super.modelExchanged(oldModel)
		addPortView(createInputPortView(model.getInput()))
		addPortView(createOutputPortView(model.getOutput()))
		updateLayout()
	}

	var bitWidth: BitWidth
		get() = model.bitWidth
		set(value) {
			model.bitWidth = value
		}

	override fun getExplanation(x: Double, y: Double): DrawableExplanation<RectangularDrawable> =
		explanation.value.also {
			it.sourceRect = boundingBox
		}

    override fun drawShape(context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke) {
        currentSymbolStyle.symbolStyle.drawNotGate(this, context, foregroundColor, backgroundColor, stroke)
    }

	override fun drawCustomShapeContent(context: DrawContext, foregroundColor: Color, backgroundColor: Color) {
		drawMnemonics(context, foregroundColor, backgroundColor)
	}

	private fun drawMnemonics(context: DrawContext, foregroundColor: Color, backgroundColor: Color) {
		GateMnemonic.drawNot(this, context, foregroundColor, backgroundColor)
	}
}