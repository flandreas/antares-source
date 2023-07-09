package ch.scorpion.antares.view.gate

import ch.scorpion.antares.model.PortCount
import ch.scorpion.antares.model.gate.AbstractLogicGate
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.view.app.AntaresGraphViewService
import ch.scorpion.antares.view.port.DigitalPortView
import ch.scorpion.antares.view.symbolstyle.CurrentSymbolStyle
import ch.scorpion.antares.view.truthtable.TruthTableView
import ch.scorpion.jabbah.base.resettableLazy
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.DrawableExplanation
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.Font
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.graph.model.GraphElementEvent
import ch.scorpion.jabbah.graph.model.Port
import ch.scorpion.jabbah.graph.view.port.PortLabelPosition
import ch.scorpion.jabbah.graph.view.vertice.AbstractVerticeView

/**
 * Base view implementation for [AbstractLogicGate] views.
 *
 * Must be declared public in order to support ComponentPropertyPanel property access.
 * @param T the type of gate model displayed by this view.
 */
abstract class AbstractLogicGateView<T: AbstractLogicGate>(
	styleProvider: StyleProvider,
	protected val currentSymbolStyle: CurrentSymbolStyle,
	text: String,
	gate: T
) : BoxGateView<T>(styleProvider, text, gate), CustomShapeContent {

	companion object {
		const val BASE_KEY_INPUT_PORT_NAME = "element.property.inputPort"
		const val BASE_KEY_OUTPUT_PORT_NAME = "element.property.outputPort"
		const val BASE_KEY_NEGATE_INPUT = "element.property.Gate.negateInput"
	}

	/** Use [AntaresGraphViewService] for changing this value.*/
	val chosenInputCount: PortCount get() = model.chosenInputCount

	var outputPortName: String?
		get() = model.getOutput<DigitalSignal>().name
		set(value) {
			invalidate()
			model.getOutput<DigitalSignal>().name = value
			invalidate()
		}

	var bitWidth: BitWidth
		get() = model.bitWidth
		set(value) {
			if (value != model.bitWidth) {
				invalidate()
				model.bitWidth = value
				updateInputBitWidthAnnotations()
				invalidate()
				validate()
			}
		}

	private val explanation = resettableLazy {
		if (model.inputCount <= 2) {
			val truthTableView = TruthTableView(model.calculateTruthTable(), model, passive = model.bitWidth.width > 1)
			DrawableExplanation(truthTableView, boundingBox)
		} else null
	}

	override val symbolFont: Font get() = currentSymbolStyle.symbolStyle.getFont(font)

	// Explicit properties needed for reflective Commands on the JVM platform

	var negateInput1: Boolean
		get() = model.getNegateInput(1)
		set(value) = setInputNegation(1, value)

	var negateInput2: Boolean
		get() = model.getNegateInput(2)
		set(value) = setInputNegation(2, value)

	var negateInput3: Boolean
		get() = model.getNegateInput(3)
		set(value) = setInputNegation(3, value)

	var negateInput4: Boolean
		get() = model.getNegateInput(4)
		set(value) = setInputNegation(4, value)

	var negateInput5: Boolean
		get() = model.getNegateInput(5)
		set(value) = setInputNegation(5, value)

	var negateInput6: Boolean
		get() = model.getNegateInput(6)
		set(value) = setInputNegation(6, value)

	var negateInput7: Boolean
		get() = model.getNegateInput(7)
		set(value) = setInputNegation(7, value)

	var negateInput8: Boolean
		get() = model.getNegateInput(8)
		set(value) = setInputNegation(8, value)

	fun setInputNegation(portId: Int, value: Boolean) {
		if (model.getNegateInput(portId) != value) {
			model.setNegateInput(portId, value)

			// This causes connected EdgeViews to adjust their end locations
			update()
		}
	}

	fun getInputNegation(portId: Int): Boolean = model.getNegateInput(portId)

	var inputPortName1: String?
		get() = getInputPortName(1)
		set(value) { setInputPortName(1, value) }

	var inputPortName2: String?
		get() = getInputPortName(2)
		set(value) { setInputPortName(2, value) }

	var inputPortName3: String?
		get() = getInputPortName(3)
		set(value) { setInputPortName(3, value) }

	var inputPortName4: String?
		get() = getInputPortName(4)
		set(value) { setInputPortName(4, value) }

	var inputPortName5: String?
		get() = getInputPortName(5)
		set(value) { setInputPortName(5, value) }

	var inputPortName6: String?
		get() = getInputPortName(6)
		set(value) { setInputPortName(6, value) }

	var inputPortName7: String?
		get() = getInputPortName(7)
		set(value) { setInputPortName(7, value) }

	var inputPortName8: String?
		get() = getInputPortName(8)
		set(value) { setInputPortName(8, value) }

	fun setInputPortName(portId: Int, name: String?) {
		if (model.getPort<DigitalSignal>(portId).name != name) {
			model.getPort<DigitalSignal>(portId).name = name
			update()
		}
	}

	fun getInputPortName(portId: Int): String? = model.getPort<DigitalSignal>(portId).name

	override fun drawCustomShapeContent(context: DrawContext, foregroundColor: Color, backgroundColor: Color) {
		drawMnemonics(context, foregroundColor, backgroundColor)
	}

	override fun getExplanation(x: Double, y: Double): DrawableExplanation<*>? =
		explanation.value?.also {
			it.sourceRect = boundingBox
		}

	protected abstract fun drawMnemonics(context: DrawContext, foregroundColor: Color, backgroundColor: Color)

	override fun handleStateChanged(event: GraphElementEvent) {
		super.handleStateChanged(event)
		if (event.signalHandler == null) {
			explanation.reset()
		}
	}

	/** ---- [AbstractVerticeView] */

	override fun modelExchanged(oldModel: T?) {
		super.modelExchanged(oldModel)

		for (inputPort in model.getInputs()) {
			addPortView(createInputPortView(inputPort as Port<DigitalSignal>, portLabelPosition = PortLabelPosition.EXTERNAL))
		}
		updateInputBitWidthAnnotations()
		addPortView(createOutputPortView(model.getOutput(), portLabelPosition = PortLabelPosition.EXTERNAL))

		updateLayout()
	}

	fun updateInputBitWidthAnnotations() {
		val inputCount = model.getInputs().size
		getPortViews().forEach { portView ->
			if (portView.port.portId <= inputCount) {
				(portView as DigitalPortView).showBitWidthAnnotation = inputCount <= 2 || portView.port.portId == inputCount
			}
		}
	}
}