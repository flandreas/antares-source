package ch.scorpion.antares.view.net

import ch.scorpion.antares.model.PortCount
import ch.scorpion.antares.model.net.WireTap
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.view.DigitalComponentView
import ch.scorpion.antares.view.Handedness
import ch.scorpion.antares.view.Look
import ch.scorpion.antares.view.PortViewSpacing
import ch.scorpion.antares.view.app.DigitalGraphViewService
import ch.scorpion.antares.view.port.DigitalPortView
import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Path
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.graph.GraphApplicationContext
import ch.scorpion.jabbah.graph.model.Port
import ch.scorpion.jabbah.graph.view.port.PortLabelPosition
import ch.scorpion.jabbah.graph.view.style.GraphStyleType
import ch.scorpion.jabbah.graph.view.vertice.AbstractVerticeView
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

class WireTapView(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	model: WireTap = WireTap(),
	handedness: Handedness = Handedness.RIGHT
) : DigitalComponentView<WireTap>(styleProvider, model) {

	companion object {
		private const val X_DISPLACEMENT = 2 * Look.SCALE
		private const val Y_DISPLACEMENT = 2 * Look.SCALE
		private const val INPUT_PIN_LENGTH = 0

		private val RIGHT_NARROW_PATH = System.createPath()
			.moveTo(0, 0)
			.lineTo(-X_DISPLACEMENT, -Y_DISPLACEMENT)

		private val RIGHT_WIDE_PATH = System.createPath()
			.moveTo(0, 0)
			.lineTo(-X_DISPLACEMENT, -Y_DISPLACEMENT)

		private val LEFT_NARROW_PATH = System.createPath()
			.moveTo(0, 0)
			.lineTo(X_DISPLACEMENT, -Y_DISPLACEMENT)

		private val LEFT_WIDE_PATH = System.createPath()
			.moveTo(0, 0)
			.lineTo(X_DISPLACEMENT, -Y_DISPLACEMENT)
	}

	/** Use [DigitalGraphViewService] for changing this value.*/
	val tapCount: PortCount get() = model.tapCount

	// Explicit properties for tapPosition needed for reflective Commands on the JVM platform

	@Suppress("unused") // Reflection
	var tapPosition0: Int
		get() = model.getTapPosition(0)
		set(value) = model.setTapPosition(0, value)

	@Suppress("unused") // Reflection
	var tapPosition1: Int
		get() = model.getTapPosition(1)
		set(value) = model.setTapPosition(1, value)

	@Suppress("unused") // Reflection
	var tapPosition2: Int
		get() = model.getTapPosition(2)
		set(value) = model.setTapPosition(2, value)

	@Suppress("unused") // Reflection
	var tapPosition3: Int
		get() = model.getTapPosition(3)
		set(value) = model.setTapPosition(3, value)

	@Suppress("unused") // Reflection
	var tapPosition4: Int
		get() = model.getTapPosition(4)
		set(value) = model.setTapPosition(4, value)

	@Suppress("unused") // Reflection
	var tapPosition5: Int
		get() = model.getTapPosition(5)
		set(value) = model.setTapPosition(5, value)

	@Suppress("unused") // Reflection
	var tapPosition6: Int
		get() = model.getTapPosition(6)
		set(value) = model.setTapPosition(6, value)

	@Suppress("unused") // Reflection
	var tapPosition7: Int
		get() = model.getTapPosition(7)
		set(value) = model.setTapPosition(7, value)

	var handedness: Handedness = handedness
		set(value) {
			if (field != value) {
				invalidate()
				field = value
				modelExchanged(model)
				invalidate()
				update()
			}
		}

	var bitWidth: BitWidth
		get() = model.bitWidth
		set(value) {
			if (value != bitWidth) {
				model.bitWidth = value
			}
		}

	@Suppress("unused") // Reflection
	var narrowSideBitWidth: BitWidth
		get() = model.narrowSideBitWidth
		set(value) {
			if (value != narrowSideBitWidth) {
				model.narrowSideBitWidth = value
			}
		}

	var portViewSpacing: PortViewSpacing = PortViewSpacing.Narrow
		set(value) {
			if (field != value) {
				invalidate()
				field = value
				updateGeometry()
				invalidate()
				update()
			}
		}

	init {
		modelExchanged(null)
	}

	override fun modelExchanged(oldModel: WireTap?) {
		super.modelExchanged(oldModel)

		addPortView(createInputPortView())
		for (i in 0 until model.tapCount.count) {
			addPortView(createOutputPortView(model.getPort(i + 2)))
		}

		updateGeometry()
	}

	private fun createInputPortView(): DigitalPortView {
		return DigitalPortView(
			styleProvider,
			model.getPort(1),
			direction = Direction.NORTH,
			length = INPUT_PIN_LENGTH,
			customUnconnectedLength = INPUT_PIN_LENGTH,
			showBitWidthAnnotation = false)
	}

	fun createOutputPortView(port: Port<DigitalSignal>): DigitalPortView {
		return DigitalPortView(
			styleProvider,
			port,
			direction = when (handedness) {
				Handedness.RIGHT -> Direction.EAST
				Handedness.LEFT -> Direction.WEST
			},
			showBitWidthAnnotation = false,
			portLabelPosition = PortLabelPosition.EXTERNAL)
	}

	fun updateGeometry() {
		val width = X_DISPLACEMENT
		val height = Y_DISPLACEMENT + portViewSpacing.value * (model.tapCount.count - 1)
		when (handedness) {
			Handedness.RIGHT -> setBounds(Rectangle2D(0, INPUT_PIN_LENGTH, width, height))
			Handedness.LEFT -> setBounds(Rectangle2D(-X_DISPLACEMENT, INPUT_PIN_LENGTH, width, height))
		}

		val x = when (handedness) {
			Handedness.RIGHT -> X_DISPLACEMENT
			Handedness.LEFT -> -X_DISPLACEMENT
		}
		var y = Y_DISPLACEMENT
		for (i in 0 until model.tapCount.count) {
			getPortView(model.getPort(i + 2))!!.location = Point2D(x, y)
			y += portViewSpacing.value
		}
	}

	/** ---- [AbstractVerticeView] */

	override fun drawImpl(context: DrawContext) {
		super.drawImpl(context)
		if (context.useContextColors) {
			drawImpl(context, context.color!!.foregroundColor)
		} else {
			drawImpl(context, styleProvider.getStyle(GraphStyleType.EDGE).color.foregroundColor)
		}
	}

	private fun drawImpl(context: DrawContext, lineColor: Color) {
		for (i in 0 until model.tapCount.count) {
			val pv = getPortView(model.getPort(i + 2))!!
			val pvLoc = pv.location
			pv.prepareConnectionDrawContext(context)

			if (!context.castedAppContext<GraphApplicationContext>()!!.isExecute) {
				context.g.color = lineColor
			}

			context.g.translate(pvLoc)
			context.g.draw(getPath())
			context.g.translate(pvLoc.negate)
		}

		val pv1 = getPortView(model.getPort(1))!!
		pv1.prepareConnectionDrawContext(context)
		context.g.drawLine(0, 0, 0, portViewSpacing.value * (model.portsCount - 2))
	}

	private fun getPath(): Path = when (portViewSpacing) {
		PortViewSpacing.Narrow -> when (handedness) {
			Handedness.RIGHT -> RIGHT_NARROW_PATH
			Handedness.LEFT -> LEFT_NARROW_PATH
		}
		PortViewSpacing.Wide -> when (handedness) {
			Handedness.RIGHT -> RIGHT_WIDE_PATH
			Handedness.LEFT -> LEFT_WIDE_PATH
		}
	}

	/** ---- [Storable] interface */

	override fun write(writer: StoreWriter) {
		super.write(writer)
		writer.writeString("handedness", handedness.customName)
		if (portViewSpacing != PortViewSpacing.Narrow) {
			writer.writeString("portViewSpacing", portViewSpacing.customName)
		}
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		handedness = Handedness.withName(reader.readString("handedness"))
		if (reader.hasAttribute("portViewSpacing")) {
			portViewSpacing = PortViewSpacing.withName(reader.readString("portViewSpacing"))
		}
		updateGeometry()
	}
}