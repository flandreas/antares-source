package ch.scorpion.antares.view.net

import ch.scorpion.antares.model.net.Transistor
import ch.scorpion.antares.model.net.TransistorType
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.view.DigitalComponentView
import ch.scorpion.antares.view.Handedness
import ch.scorpion.antares.view.Handedness.LEFT
import ch.scorpion.antares.view.Handedness.RIGHT
import ch.scorpion.antares.view.Look
import ch.scorpion.antares.view.Look.SCALE
import ch.scorpion.antares.view.port.DigitalPortView
import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Direction.*
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.geom.Rotation
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.drawable.AbstractDrawable
import ch.scorpion.jabbah.draw.graphics.LineCap
import ch.scorpion.jabbah.draw.graphics.LineJoin
import ch.scorpion.jabbah.draw.graphics.Stroke
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.style.StyleType
import ch.scorpion.jabbah.edit.model.AbstractComponent
import ch.scorpion.jabbah.edit.model.text.HorizontalLabel
import ch.scorpion.jabbah.graph.GraphApplicationContext
import ch.scorpion.jabbah.graph.view.port.PortLabelPosition
import ch.scorpion.jabbah.graph.view.vertice.AbstractVerticeView
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

class TransistorView(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	model: Transistor = Transistor()
) : DigitalComponentView<Transistor>(styleProvider, model)
{
	companion object {

		/** The name of the [Boolean] property in [Properties] defining whether transistors are drawn with a circle. */
		const val PROP_TRANSISTOR_CIRCLE = "antares.transistor.circle"

		private val BAR_STROKE = Stroke(1.5f, LineCap.BUTT, LineJoin.ROUND)
		private const val LABEL_DIST = SCALE
		private val DEFAULT_HANDEDNESS = RIGHT
		private const val WIDTH = 6 * SCALE
		private const val HEIGHT = 6 * SCALE
		private const val GATE_LINE_X = DigitalPortView.LENGTH + 2.0 * SCALE
		private const val SIGNAL_LINE_X = DigitalPortView.LENGTH + 3.0 * SCALE
		private const val SIGNAL_PORT_X = DigitalPortView.LENGTH + 4.0 * SCALE

		private val N_ARROW_PATH = System.createPath()
			.moveTo(SIGNAL_LINE_X, -2.0 * SCALE)
			.lineTo(SIGNAL_PORT_X, -2.4 * SCALE)
			.lineTo(SIGNAL_PORT_X, -1.6 * SCALE)
			.close()

		private val P_ARROW_PATH = System.createPath()
			.moveTo(DigitalPortView.LENGTH + 5.0 * SCALE, -2.0 * SCALE)
			.lineTo(SIGNAL_PORT_X, -2.4 * SCALE)
			.lineTo(SIGNAL_PORT_X, -1.6 * SCALE)
			.close()

		private val SOUTH_SIGNAL_PATH = System.createPath()
			.moveTo(SIGNAL_LINE_X, -0.5 * SCALE)
			.lineTo(SIGNAL_PORT_X, -0.5 * SCALE)
			.lineTo(SIGNAL_PORT_X, 1.0 * SCALE)

		private val NORTH_SIGNAL_PATH = System.createPath()
			.moveTo(SIGNAL_LINE_X, -3.5 * SCALE)
			.lineTo(SIGNAL_PORT_X, -3.5 * SCALE)
			.lineTo(SIGNAL_PORT_X, -5.0 * SCALE)

		private val hasCircle: Boolean get() = BaseModule.properties.getBoolean(PROP_TRANSISTOR_CIRCLE)
	}

	/** [Handedness.RIGHT] means that gate and source are in [Direction.SOUTH].*/
	var handedness: Handedness = DEFAULT_HANDEDNESS
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
			model.bitWidth = value
		}

	private val label = HorizontalLabel(
		owner = this,
		relLocation = Point2D.ZERO,
		font = font)

	init {
		modelExchanged(null)
		setBounds(DigitalPortView.LENGTH, -5 * SCALE, WIDTH, HEIGHT)
	}

	override fun modelExchanged(oldModel: Transistor?) {
		super.modelExchanged(oldModel)

		// Gate
		val gate = DigitalPortView(
			styleProvider,
			model.getGatePort(),
			DigitalPortView.LENGTH,
			when (handedness) {
				RIGHT -> 0
				LEFT -> -4 * SCALE
			},
			WEST,
			showLogicAnnotation = false
		)
		gate.setPortName("G")
		gate.portLabelPosition = PortLabelPosition.HIDE
		addPortView(gate)

		// Source
		val source = DigitalPortView(
			styleProvider,
			model.getSourcePort(),
			DigitalPortView.LENGTH + 4 * SCALE,
			when (handedness) {
				RIGHT -> SCALE
				LEFT -> -5 * SCALE
			},
			when (handedness) {
				RIGHT -> SOUTH
				LEFT -> NORTH
			}
		)
		source.setPortName("S")
		source.portLabelPosition = PortLabelPosition.HIDE
		addPortView(source)

		// Drain
		val drain = DigitalPortView(
			styleProvider,
			model.getDrainPort(),
			DigitalPortView.LENGTH + 4 * SCALE,
			when (handedness) {
				RIGHT -> -5 * SCALE
				LEFT -> SCALE
			},
			when (handedness) {
				RIGHT -> NORTH
				LEFT -> SOUTH
			}
		)
		drain.setPortName("D")
		drain.portLabelPosition = PortLabelPosition.HIDE
		addPortView(drain)

		label.relLocation = Point2D(
			DigitalPortView.LENGTH + WIDTH + LABEL_DIST,
			when (handedness) {
				RIGHT -> -2 * SCALE
				LEFT -> 2 * SCALE
			})
	}

	/** ---- UI properties */

	var transistorType: TransistorType
		get() = model.transistorType
		set(value) {
			if (value != model.transistorType) {
				invalidate()
				model.transistorType = value
				tooltip.reset()
				invalidate()
			}
		}

	var name: String?
		get() = model.name
		set(value) {
			if (value != name) {
				model.name = value
				updateLabel()
			}
		}

	/** ---- [Storable] interface */

	override fun write(writer: StoreWriter) {
		super.write(writer)
		if (handedness != DEFAULT_HANDEDNESS) {
			writer.writeString("handedness", handedness.customName)
		}
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		if (reader.hasAttribute("handedness")) {
			handedness = Handedness.withName(reader.readString("handedness"))
		}
	}

	override fun resolutionDone() {
		label.text = StringUtils.orEmpty(name)
	}

	/** ---- [AbstractDrawable] */

	override val boundingBox: Rectangle2D
		get() {
			val bb = Rectangle2D(super.boundingBox)
			val lbb = label.boundingBox.moveBy(location)
			bb.add(lbb)
			return bb
		}

	/** ---- [AbstractComponent] */

	override fun rotationChanged(newRotation: Rotation) {
		super.rotationChanged(newRotation)
		label.rotationChanged()
	}

	/** ---- [AbstractVerticeView] */

	override fun draw(context: DrawContext) {
		super.draw(context)
		drawLabel(context)
	}

	override fun drawImpl(context: DrawContext) {
		super.drawImpl(context)

		drawBody(context)
		drawGate(context)
		drawBulk(context)

		when (handedness) {
			RIGHT -> {
				drawSouthSignalPort(context, getPortView(model.getSourcePort()) as DigitalPortView)
				drawNorthSignalPort(context, getPortView(model.getDrainPort()) as DigitalPortView)
			}
			LEFT -> {
				drawSouthSignalPort(context, getPortView(model.getDrainPort()) as DigitalPortView)
				drawNorthSignalPort(context, getPortView(model.getSourcePort()) as DigitalPortView)
			}
		}
	}

	private fun drawLabel(context: DrawContext) {
		context.g.color = styleProvider.getStyle(StyleType.BACKGROUND).color.textColor
		label.draw(context)
	}

	/** ---- [TransistorView] */

	private fun updateLabel() {
		invalidate()
		label.text = StringUtils.orEmpty(name)
		label.rotationChanged()
		invalidate()
		update()
	}

	private fun drawBody(context: DrawContext) {
		if (hasCircle) {
			context.g.stroke = stroke
			context.g.color = transparent.applyTo(context.choose(
				if (Look.FILL_BASIC_COMPONENTS) color else DrawStyleModule.styleProvider.getStyle(StyleType.BACKGROUND).color
			).backgroundColor)
			context.g.fillOval(
				DigitalPortView.LENGTH.toDouble(), -5.0 * SCALE,
				WIDTH.toDouble(), HEIGHT.toDouble())

			context.g.color = transparent.applyTo(context.choose(color).foregroundColor)
			context.g.drawOval(
				DigitalPortView.LENGTH.toDouble(), -5.0 * SCALE,
				WIDTH.toDouble(), HEIGHT.toDouble())
		}
	}

	private fun drawGate(context: DrawContext) {
		(getPortView(model.getGatePort()) as DigitalPortView).prepareConnectionDrawContext(context)

		// Gate connection
		val gateConnectionY = when(handedness) {
			RIGHT -> 0.0
			LEFT -> - 4.0 * SCALE
		}
		context.g.drawLine(
			DigitalPortView.LENGTH.toDouble(), gateConnectionY,
			GATE_LINE_X, gateConnectionY)

		// Gate bar
		context.g.stroke = BAR_STROKE
		context.g.drawLine(
			GATE_LINE_X, -4.0 * SCALE,
			GATE_LINE_X, 0.0)
	}

	private fun drawSouthSignalPort(context: DrawContext, portView: DigitalPortView) {
		portView.prepareConnectionDrawContext(context)

		// connection
		context.g.draw(SOUTH_SIGNAL_PATH)

		// bar
		context.g.stroke = BAR_STROKE
		context.g.drawLine(
			SIGNAL_LINE_X, -1.0 * SCALE,
			SIGNAL_LINE_X, 0.0)
	}

	private fun drawNorthSignalPort(context: DrawContext, portView: DigitalPortView) {
		portView.prepareConnectionDrawContext(context)

		// connection
		context.g.draw(NORTH_SIGNAL_PATH)

		// bar
		context.g.stroke = BAR_STROKE
		context.g.drawLine(
			SIGNAL_LINE_X, -4.0 * SCALE,
			SIGNAL_LINE_X, -3.0 * SCALE)
	}

	private fun drawBulk(context: DrawContext) {
		(getPortView(model.getDrainPort()) as DigitalPortView).prepareConnectionDrawContext(context)

		val dx = if (!model.isOn && context.castedAppContext<GraphApplicationContext>()!!.isExecute) {
			0.5 * SCALE
		} else {
			0.0
		}

		(getPortView(model.getDrainPort()) as DigitalPortView).prepareConnectionDrawContext(context)

		context.g.translate(dx, 0.0)

		// arrow
		drawBulkArrow(context)

		// bar
		context.g.stroke = BAR_STROKE
		context.g.drawLine(
			SIGNAL_LINE_X, -2.5 * SCALE,
			SIGNAL_LINE_X, -1.5 * SCALE)

		context.g.translate(-dx, 0.0)
	}

	private fun drawBulkArrow(context: DrawContext) {
		when (model.transistorType) {
			TransistorType.P -> drawPTypeBulkArrow(context)
			TransistorType.N -> drawNTypeBulkArrow(context)
		}
	}

	private fun drawNTypeBulkArrow(context: DrawContext) {
		context.g.fill(N_ARROW_PATH)
		context.g.drawLine(
			SIGNAL_PORT_X, -2.0 * SCALE,
			SIGNAL_PORT_X + 1.0 * SCALE, -2.0 * SCALE)
	}

	private fun drawPTypeBulkArrow(context: DrawContext) {
		context.g.fill(P_ARROW_PATH)
		context.g.drawLine(
			SIGNAL_PORT_X, -2.0 * SCALE,
			SIGNAL_LINE_X, -2.0 * SCALE)
	}
}