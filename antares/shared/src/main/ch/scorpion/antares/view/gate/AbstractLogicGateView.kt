package ch.scorpion.antares.view.gate

import ch.scorpion.antares.model.gate.AbstractDigitalGate
import ch.scorpion.antares.view.symbolstyle.CurrentSymbolStyle
import ch.scorpion.antares.view.truthtable.TruthTableView
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.DrawableExplanation
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.style.StyleProvider

abstract class AbstractLogicGateView<T: AbstractDigitalGate>(
	styleProvider: StyleProvider,
	protected val currentSymbolStyle: CurrentSymbolStyle,
	text: String,
	gate: T
) : AbstractDigitalGateView<T>(styleProvider, text, gate), CustomShapeContent {

	var negateInput1: Boolean
		get() = model.getNegateInput(1)
		set(value) = model.setNegateInput(1, value)

	var negateInput2: Boolean
		get() = model.getNegateInput(2)
		set(value) = model.setNegateInput(2, value)

	var negateInput3: Boolean
		get() = model.getNegateInput(3)
		set(value) = model.setNegateInput(3, value)

	var negateInput4: Boolean
		get() = model.getNegateInput(4)
		set(value) = model.setNegateInput(4, value)

	var negateInput5: Boolean
		get() = model.getNegateInput(5)
		set(value) = model.setNegateInput(5, value)

	var negateInput6: Boolean
		get() = model.getNegateInput(6)
		set(value) = model.setNegateInput(6, value)

	var negateInput7: Boolean
		get() = model.getNegateInput(7)
		set(value) = model.setNegateInput(7, value)

	var negateInput8: Boolean
		get() = model.getNegateInput(8)
		set(value) = model.setNegateInput(8, value)

	override fun drawCustomShapeContent(context: DrawContext, foregroundColor: Color, backgroundColor: Color) {
		drawMnemonics(context, foregroundColor, backgroundColor)
	}

	override fun getExplanation(x: Double, y: Double): DrawableExplanation<*>? {
		return if (model.inputCount == 2) {
			DrawableExplanation(TruthTableView(model.calculateTruthTable(), model), Point2D(boundingBox.centerX, boundingBox.minY))
		} else null
	}

	protected abstract fun drawMnemonics(context: DrawContext, foregroundColor: Color, backgroundColor: Color)
}