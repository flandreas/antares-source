package ch.scorpion.antares.view.gate

import ch.scorpion.antares.model.InputPortNumber
import ch.scorpion.antares.model.PortCount
import ch.scorpion.antares.model.gate.*
import ch.scorpion.antares.model.gate.NonUnaryLogicGateType.*
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.view.app.AntaresGraphViewService
import ch.scorpion.antares.view.module.AntaresViewModule
import ch.scorpion.antares.view.port.AbstractAntaresPortView
import ch.scorpion.antares.view.port.DigitalPortView
import ch.scorpion.antares.view.symbolstyle.CurrentSymbolStyle
import ch.scorpion.antares.view.symbolstyle.SymbolStyle
import ch.scorpion.antares.view.symbolstyle.SymbolStyle.AMERICAN
import ch.scorpion.antares.view.truthtable.TruthTableView
import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.help.HelpId
import ch.scorpion.jabbah.base.help.HelpProvider
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.resettableLazy
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.DrawableExplanation
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.Font
import ch.scorpion.jabbah.draw.graphics.Stroke
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.edit.Look.SCALE
import ch.scorpion.jabbah.edit.model.Size
import ch.scorpion.jabbah.edit.model.Size.LARGE
import ch.scorpion.jabbah.graph.model.GraphElementEvent
import ch.scorpion.jabbah.graph.model.Port
import ch.scorpion.jabbah.graph.view.InternalLabelStyle
import ch.scorpion.jabbah.graph.view.port.PortLabelPosition
import ch.scorpion.jabbah.graph.view.vertice.AbstractRectangularVerticeView
import ch.scorpion.jabbah.graph.view.vertice.AbstractVerticeView
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter
import kotlin.math.min

/**
 * Common view class for displaying all kinds of [AbstractLogicGate]s.
 */
class LogicGateView(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	private val currentSymbolStyle: CurrentSymbolStyle = AntaresViewModule.currentSymbolStyle,
	gate: AbstractLogicGate
) : BoxGateView<AbstractLogicGate>(styleProvider, getRenderer(gate.gateType).text, gate), CustomShapeContent {

	companion object {
		const val BASE_KEY_INPUT_PORT_NAME = "element.property.inputPort"
		const val BASE_KEY_OUTPUT_PORT_NAME = "element.property.outputPort"
		const val BASE_KEY_NEGATE_INPUT = "element.property.Gate.negateInput"
		const val BASE_KEY_DATA_PORT = "element.property.AndGate.dataPort"

		/** The name of the [Boolean] property in [Properties] defining whether the data flow feature is enabled.*/
		const val PROP_DATA_FLOW_ENABLED = "antares.andGate.dataFlow"

		val isDataFlowEnabled: Boolean get() = BaseModule.properties.getBoolean(PROP_DATA_FLOW_ENABLED)

		fun andGateView(): LogicGateView = LogicGateView(gate = NonUnaryLogicGate.andGate())
		fun nandGateView(): LogicGateView = LogicGateView(gate = NonUnaryLogicGate.nandGate())
		fun orGateView(): LogicGateView = LogicGateView(gate = NonUnaryLogicGate.orGate())
		fun norGateView(): LogicGateView = LogicGateView(gate = NonUnaryLogicGate.norGate())
		fun xorGateView(): LogicGateView = LogicGateView(gate = NonUnaryLogicGate.xorGate())
		fun xnorGateView(): LogicGateView = LogicGateView(gate = NonUnaryLogicGate.xnorGate())

		// Default parameter doesn't work in unit tests for some reason..
		fun notGateView(): LogicGateView = notGateView(BitWidth.BW_1)

		fun notGateView(bitWidth: BitWidth = BitWidth.BW_1): LogicGateView = LogicGateView(gate = UnaryLogicGate.notGate(bitWidth))
		fun bufferGateView(): LogicGateView = LogicGateView(gate = UnaryLogicGate.bufferGate())

		private fun getRenderer(type: LogicGateType): LogicGateViewRenderer =
			when (type) {
				is NonUnaryLogicGateType -> {
					when (type) {
						And -> LogicGateViewRenderers.And
						Nand -> LogicGateViewRenderers.Nand
						Or -> LogicGateViewRenderers.Or
						Nor -> LogicGateViewRenderers.Nor
						Xor -> LogicGateViewRenderers.Xor
						Xnor -> LogicGateViewRenderers.Xnor
					}
				}

				is UnaryLogicGateType -> {
					when (type) {
						UnaryLogicGateType.Not -> LogicGateViewRenderers.Not
						UnaryLogicGateType.Buffer -> LogicGateViewRenderers.Buffer
					}
				}

				else -> {
					throw IllegalStateException("Unsupported LogicGateType")
				}
			}
	}

	/** Use [AntaresGraphViewService] for changing this value.*/
	val chosenInputCount: PortCount get() = model.chosenInputCount

	@Suppress("unused") // Reflection
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

	/** Used for views of [NonUnaryLogicGateType.And]. */
	var dataPort: InputPortNumber = InputPortNumber.NONE
		set(value) {
			if (field == value) {
				return
			}
			require(value.id <= model.chosenInputCount.count) { "InputPortNumber must not be larger than InputCount" }
			invalidate()
			field = value
			internalLabelStyle = if (dataPort == InputPortNumber.NONE) InternalLabelStyle.LARGE_CENTERED else InternalLabelStyle.SMALL_UPPER_LEFT
			invalidate()
			update()
		}

	var logicGateType: LogicGateType
		get() = model.gateType
		set(value) {
			invalidate()

			model.gateType = value
			internalLabelText = getRenderer(value).text

			tooltip.reset()
			explanation.reset()

			invalidate()
			update()
		}

	var size: Size = LARGE
		set(value) {
			if (field != value) {
				invalidate()
				field = value
				internalLabelStyle?.updateLabel(this)
				updateLayout()
			}
		}

	private val explanation = resettableLazy {
		if (model.inputCount <= 2) {
			val truthTableView = TruthTableView(model.calculateTruthTable(), model, passive = model.bitWidth.width > 1)
			DrawableExplanation(truthTableView, boundingBox)
		} else null
	}

	// Explicit properties needed for reflective Commands on the JVM platform

	@Suppress("unused") // Reflection
	var negateInput1: Boolean
		get() = model.getNegateInput(1)
		set(value) = setInputNegation(1, value)

	@Suppress("unused") // Reflection
	var negateInput2: Boolean
		get() = model.getNegateInput(2)
		set(value) = setInputNegation(2, value)

	@Suppress("unused") // Reflection
	var negateInput3: Boolean
		get() = model.getNegateInput(3)
		set(value) = setInputNegation(3, value)

	@Suppress("unused") // Reflection
	var negateInput4: Boolean
		get() = model.getNegateInput(4)
		set(value) = setInputNegation(4, value)

	@Suppress("unused") // Reflection
	var negateInput5: Boolean
		get() = model.getNegateInput(5)
		set(value) = setInputNegation(5, value)

	@Suppress("unused") // Reflection
	var negateInput6: Boolean
		get() = model.getNegateInput(6)
		set(value) = setInputNegation(6, value)

	@Suppress("unused") // Reflection
	var negateInput7: Boolean
		get() = model.getNegateInput(7)
		set(value) = setInputNegation(7, value)

	@Suppress("unused") // Reflection
	var negateInput8: Boolean
		get() = model.getNegateInput(8)
		set(value) = setInputNegation(8, value)

	@Suppress("unused") // Reflection
	var negateInput9: Boolean
		get() = model.getNegateInput(9)
		set(value) { setInputNegation(9, value) }

	@Suppress("unused") // Reflection
	var negateInput10: Boolean
		get() = model.getNegateInput(10)
		set(value) { setInputNegation(10, value) }

	@Suppress("unused") // Reflection
	var negateInput11: Boolean
		get() = model.getNegateInput(11)
		set(value) { setInputNegation(11, value) }

	@Suppress("unused") // Reflection
	var negateInput12: Boolean
		get() = model.getNegateInput(12)
		set(value) { setInputNegation(12, value) }

	@Suppress("unused") // Reflection
	var negateInput13: Boolean
		get() = model.getNegateInput(13)
		set(value) { setInputNegation(13, value) }

	@Suppress("unused") // Reflection
	var negateInput14: Boolean
		get() = model.getNegateInput(14)
		set(value) { setInputNegation(14, value) }

	@Suppress("unused") // Reflection
	var negateInput15: Boolean
		get() = model.getNegateInput(15)
		set(value) { setInputNegation(15, value) }

	@Suppress("unused") // Reflection
	var negateInput16: Boolean
		get() = model.getNegateInput(16)
		set(value) { setInputNegation(16, value) }

	fun setInputNegation(portId: Int, value: Boolean) {
		if (model.getNegateInput(portId) != value) {
			model.setNegateInput(portId, value)

			// This causes connected EdgeViews to adjust their end locations
			update()
		}
	}

	@Suppress("unused") // Reflection
	fun getInputNegation(portId: Int): Boolean = model.getNegateInput(portId)

	@Suppress("unused") // Reflection
	var inputPortName1: String?
		get() = getInputPortName(1)
		set(value) { setInputPortName(1, value) }

	@Suppress("unused") // Reflection
	var inputPortName2: String?
		get() = getInputPortName(2)
		set(value) { setInputPortName(2, value) }

	@Suppress("unused") // Reflection
	var inputPortName3: String?
		get() = getInputPortName(3)
		set(value) { setInputPortName(3, value) }

	@Suppress("unused") // Reflection
	var inputPortName4: String?
		get() = getInputPortName(4)
		set(value) { setInputPortName(4, value) }

	@Suppress("unused") // Reflection
	var inputPortName5: String?
		get() = getInputPortName(5)
		set(value) { setInputPortName(5, value) }

	@Suppress("unused") // Reflection
	var inputPortName6: String?
		get() = getInputPortName(6)
		set(value) { setInputPortName(6, value) }

	@Suppress("unused") // Reflection
	var inputPortName7: String?
		get() = getInputPortName(7)
		set(value) { setInputPortName(7, value) }

	@Suppress("unused") // Reflection
	var inputPortName8: String?
		get() = getInputPortName(8)
		set(value) { setInputPortName(8, value) }

	@Suppress("unused") // Reflection
	var inputPortName9: String?
		get() = getInputPortName(9)
		set(value) { setInputPortName(9, value) }

	@Suppress("unused") // Reflection
	var inputPortName10: String?
		get() = getInputPortName(10)
		set(value) { setInputPortName(10, value) }

	@Suppress("unused") // Reflection
	var inputPortName11: String?
		get() = getInputPortName(11)
		set(value) { setInputPortName(11, value) }

	@Suppress("unused") // Reflection
	var inputPortName12: String?
		get() = getInputPortName(12)
		set(value) { setInputPortName(12, value) }

	@Suppress("unused") // Reflection
	var inputPortName13: String?
		get() = getInputPortName(13)
		set(value) { setInputPortName(13, value) }

	@Suppress("unused") // Reflection
	var inputPortName14: String?
		get() = getInputPortName(14)
		set(value) { setInputPortName(14, value) }

	@Suppress("unused") // Reflection
	var inputPortName15: String?
		get() = getInputPortName(15)
		set(value) { setInputPortName(15, value) }

	@Suppress("unused") // Reflection
	var inputPortName16: String?
		get() = getInputPortName(16)
		set(value) { setInputPortName(16, value) }

	fun setInputPortName(portId: Int, name: String?) {
		if (model.getPort<DigitalSignal>(portId).name != name) {
			model.getPort<DigitalSignal>(portId).name = name
			update()
		}
	}

	fun getInputPortName(portId: Int): String? = model.getPort<DigitalSignal>(portId).name

	init {
		initExternalLabel(Direction.NORTH)
		modelExchanged(null)
		internalLabel?.font = internalLabelFont
	}

	override val internalLabelFont: Font get() = SymbolStyle.getSymbolFont(size, font)

	override val relativeExternalLabelLocation: Point2D get() =
		Point2D(-AbstractAntaresPortView.LENGTH - width / 2, -height / 2 - LABEL_DIST)

	override val labelScale: Float get() = size.factor

	override fun drawShape(context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke) {
		getRenderer(model.gateType).drawShape(this, context, foregroundColor, backgroundColor, stroke)
	}

	override fun drawCustomShapeContent(context: DrawContext, foregroundColor: Color, backgroundColor: Color) {
		if (size == LARGE) {
			getRenderer(model.gateType).drawMnemonics(this, context, foregroundColor, backgroundColor)
		}
	}

	override fun getExplanation(x: Double, y: Double): DrawableExplanation<*>? =
		explanation.value?.also {
			it.sourceRect = boundingBox
		}

	override fun handleStateChanged(event: GraphElementEvent) {
		super.handleStateChanged(event)
		if (event.signalHandler == null) {
			explanation.reset()
		}
	}

	/** ---- [HelpProvider] */

	override val helpId: HelpId get() = logicGateType.helpId

	/** ---- [Storable] interface */

	override fun write(writer: StoreWriter) {
		super.write(writer)
		if (model.gateType == And && dataPort != InputPortNumber.NONE) {
			writer.writeInt("dataPort", dataPort.id)
		}
		if (size !== LARGE) {
			writer.writeString("size", size.customName)
		}
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		if (reader.hasAttribute("dataPort")) {
			dataPort = InputPortNumber.withId(reader.readInt("dataPort"))
		}
		if (reader.hasAttribute("size")) {
			size = Size.withName(reader.readString("size"))
		}
	}

	/** ---- [AbstractVerticeView] */

	override fun modelExchanged(oldModel: AbstractLogicGate?) {
		super.modelExchanged(oldModel)

		for (inputPort in model.getInputs()) {
			addPortView(createInputPortView(inputPort as Port<DigitalSignal>, portLabelPosition = PortLabelPosition.EXTERNAL))
		}
		updateInputBitWidthAnnotations()
		addPortView(createOutputPortView(model.getOutput(), portLabelPosition = PortLabelPosition.EXTERNAL))

		updateLayout()

		dataPort = InputPortNumber.withId(min(dataPort.id, model.chosenInputCount.count))
	}

	fun updateInputBitWidthAnnotations() {
		val inputCount = model.getInputs().size
		getPortViews().forEach { portView ->
			if (portView.port.portId <= inputCount) {
				(portView as DigitalPortView).showBitWidthAnnotation = inputCount <= 2 || portView.port.portId == inputCount
			}
		}
	}

	/** ---- [AbstractRectangularVerticeView] */

	override val outsetLeft: Int get() =
		when (model.gateType) {
			Or, Nor, Xor, Xnor -> {
				when (currentSymbolStyle.symbolStyle) {
					AMERICAN -> (2 * SCALE * labelScale).toInt()
					else -> 0
				}
			}
			else -> super.outsetTop
		}

	override val outsetTop: Int get() =
		when (model.gateType) {
			And, Nand, Or, Nor, Xor, Xnor -> {
				when (currentSymbolStyle.symbolStyle) {
					AMERICAN -> -(SCALE * labelScale).toInt()
					else -> 0
				}
			}
			else -> super.outsetTop
		}

	override val outsetBottom: Int get() =
		when (model.gateType) {
			And, Nand -> {
				when (currentSymbolStyle.symbolStyle) {
					AMERICAN -> -(SCALE * labelScale).toInt()
					else -> 0
				}
			}
			else -> super.outsetTop
		}
}