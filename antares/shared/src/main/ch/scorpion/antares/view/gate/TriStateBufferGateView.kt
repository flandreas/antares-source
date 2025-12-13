package ch.scorpion.antares.view.gate

import ch.scorpion.antares.model.Logic
import ch.scorpion.antares.model.OutputAnnotation
import ch.scorpion.antares.model.gate.TriStateBufferGate
import ch.scorpion.antares.model.port.DigitalPort
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.view.Handedness
import ch.scorpion.antares.view.OrientableLabeledRectangularVerticeView
import ch.scorpion.antares.view.module.AntaresViewModule
import ch.scorpion.antares.view.port.AbstractAntaresPortView
import ch.scorpion.antares.view.port.DigitalPortView
import ch.scorpion.antares.view.symbolstyle.SymbolStyle
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.graph.view.port.PortLabelPosition
import ch.scorpion.jabbah.graph.view.vertice.AbstractRectangularVerticeView
import ch.scorpion.jabbah.graph.view.vertice.AbstractVerticeView
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter


/**
 * A view of a [TriStateBufferGate]
 */
class TriStateBufferGateView(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	model: TriStateBufferGate = TriStateBufferGate()
) : OrientableLabeledRectangularVerticeView<TriStateBufferGate>(styleProvider, "1", model) {

	companion object {
		private const val CONTROL_PORT_VIEW_OFFSET_Y = (AbstractAntaresPortView.LENGTH * 0.75).toInt()
	}

	var handedness: Handedness = Handedness.RIGHT
		set(value) {
			if (field != value) {
				invalidate()
				field = value
				updateEnablePortViewForHandedness()
				invalidate()
				update()
			}
		}

	@Suppress("unused") // Reflection
	var inputPortName: String?
		get() = model.getPort<DigitalSignal>(1).name
		set(value) {
			if (model.getPort<DigitalSignal>(1).name != value) {
				model.getPort<DigitalSignal>(1).name = value
				update()
			}
		}

	@Suppress("unused") // Reflection
	var outputPortName: String?
		get() = model.getOutput<DigitalSignal>().name
		set(value) {
			invalidate()
			model.getOutput<DigitalSignal>().name = value
			invalidate()
		}

	init {
		modelExchanged(null)
	}

	/** ---- [AbstractVerticeView] */

	override fun modelExchanged(oldModel: TriStateBufferGate?) {
		super.modelExchanged(oldModel)

		val bounds = SymbolStyle.NOT_PATHS[LogicGateSize.LARGE]!!.boundingBox

		val inputPortView = DigitalPortView(
			styleProvider = styleProvider,
			port = model.getInput(),
			direction = Direction.WEST,
			portLabelPosition = PortLabelPosition.EXTERNAL)
		inputPortView.setLocation(inputPortView.unconnectedLength.toDouble(), 0.0)
		addPortView(inputPortView)

		addPortView(DigitalPortView(
			styleProvider = styleProvider,
			port = model.getOutput(),
			direction = Direction.EAST,
			portLabelPosition = PortLabelPosition.EXTERNAL,
			x = inputPortView.unconnectedLength + bounds.width.toInt(),
			y = 0))

		addPortView(DigitalPortView(
			styleProvider = styleProvider,
			port = model.getInput(2),
			direction = directionOfHandedness,
			portLabelPosition = PortLabelPosition.HIDE,
			x = (inputPortView.unconnectedLength + bounds.width / 2).toInt(),
			y = if (handedness == Handedness.RIGHT) CONTROL_PORT_VIEW_OFFSET_Y else -CONTROL_PORT_VIEW_OFFSET_Y,
			length = AbstractAntaresPortView.LENGTH - (AbstractAntaresPortView.LENGTH * 0.25).toInt()))

		if (AntaresViewModule.currentSymbolStyle.symbolStyle == SymbolStyle.EUROPEAN && !SymbolStyle.triStateAlwaysTriangle) {
			(model.getPort<DigitalSignal>(3) as DigitalPort).outputAnnotation = OutputAnnotation.TRI_STATE
		}

		// These bounds must fit both the American style (triangle) and the European style (rectangle)
		with(SymbolStyle.NOT_PATHS[LogicGateSize.LARGE]!!) {
			setBounds(
				AbstractAntaresPortView.LENGTH.toDouble(),
				-boundingBox.height / 2,
				boundingBox.width,
				boundingBox.height
			)
		}
	}

	/** ---- [Storable] interface */

	override fun write(writer: StoreWriter) {
		super.write(writer)
		writer.writeString("controlOrientation", handedness.customName)
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		if (reader.hasAttribute("controlOrientation")) {
			handedness = Handedness.withName(reader.readString("controlOrientation"))
		}
	}

	/** ---- [AbstractRectangularVerticeView] */

	override fun drawImpl(context: DrawContext) {
		val oldColor = context.g.color

		drawImplBeforeBorder(context)

		AntaresViewModule.currentSymbolStyle.symbolStyle.drawTriStateBufferGate(
			this, context, getApplicableForegroundColor(context), getApplicableBackgroundColor(context), stroke)
		GateMnemonic.drawTriStateBuffer(this, context, getApplicableForegroundColor(context))

		drawImplAfterBorder(context)

		context.g.color = oldColor
	}

	/** ---- [TriStateBufferGateView] */

	@Suppress("unused") // Reflection
	var enableLogic: Logic
		get() = model.enableLogic
		set(value) {
			model.enableLogic = value
			update()
		}

	var bitWidth: BitWidth
		get() = model.bitWidth
		set(value) {
			model.bitWidth = value
		}

	private val directionOfHandedness: Direction get() =
		when (handedness) {
			Handedness.RIGHT -> Direction.SOUTH
			Handedness.LEFT -> Direction.NORTH
		}

	private fun updateEnablePortViewForHandedness() {
		val inputPortView = getPortView(model.getInputPort())!!
		val enablePortView = getPortView(model.getEnablePort())!!
		enablePortView.location = Point2D(
			x = (inputPortView.unconnectedLength + bounds.width / 2).toInt(),
			y = if (handedness == Handedness.RIGHT) CONTROL_PORT_VIEW_OFFSET_Y else -CONTROL_PORT_VIEW_OFFSET_Y
		)
		enablePortView.direction = directionOfHandedness
	}
}