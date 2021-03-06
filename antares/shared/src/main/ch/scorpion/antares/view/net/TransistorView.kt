package ch.scorpion.antares.view.net

import ch.scorpion.antares.model.net.Transistor
import ch.scorpion.antares.model.net.TransistorType
import ch.scorpion.antares.view.DigitalComponentView
import ch.scorpion.antares.view.Handedness
import ch.scorpion.antares.view.Handedness.LEFT
import ch.scorpion.antares.view.Handedness.RIGHT
import ch.scorpion.antares.view.Look
import ch.scorpion.antares.view.Look.SCALE
import ch.scorpion.antares.view.port.DigitalPortView
import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Direction.*
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.style.StyleType
import ch.scorpion.jabbah.draw.style.Themes
import ch.scorpion.jabbah.graph.view.style.GraphTheme
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
		gate.setPortName("")
		addPortView(gate)

		// Source
		addPortView(DigitalPortView(
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
		))

		// Drain
		addPortView(DigitalPortView(
			styleProvider,
			model.getDrainPort(),
			DigitalPortView.LENGTH + 4 * SCALE,
			when (handedness) {
				RIGHT -> -5 * SCALE
				LEFT -> SCALE
			},
			when(handedness) {
				RIGHT -> NORTH
				LEFT -> SOUTH
			}
		))
	}

	/** ---- UI properties */

	var transistorType: TransistorType
		get() = model.transistorType
		set(value) {
			if (value != model.transistorType) {
				invalidate()
				model.transistorType = value
				invalidate()
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

	/** ---- [AbstractVerticeView] */

	override fun drawImpl(context: DrawContext) {
		super.drawImpl(context)

		context.g.stroke = stroke

		if (hasCircle) {
			context.g.color = if (Look.FILL_BASIC_COMPONENTS) backgroundColor else DrawStyleModule.styleProvider.getStyle(StyleType.BACKGROUND).color.backgroundColor
			context.g.fillOval(
				DigitalPortView.LENGTH.toDouble(), -5.0 * SCALE,
				WIDTH.toDouble(), HEIGHT.toDouble())

			context.g.color = context.choose(color).foregroundColor
			context.g.drawOval(
				DigitalPortView.LENGTH.toDouble(), -5.0 * SCALE,
				WIDTH.toDouble(), HEIGHT.toDouble())
		}

		context.g.color = context.choose(color).foregroundColor

		// Gate
		context.g.drawLine(
			GATE_LINE_X, -4.0 * SCALE,
			GATE_LINE_X, 0.0)

		// Source
		context.g.drawLine(
			SIGNAL_LINE_X, -1.0 * SCALE,
			SIGNAL_LINE_X, 0.0)

		// Bulk
		context.g.drawLine(
			SIGNAL_LINE_X, -2.5 * SCALE,
			SIGNAL_LINE_X, -1.5 * SCALE)

		// Drain
		context.g.drawLine(
			SIGNAL_LINE_X, -4.0 * SCALE,
			SIGNAL_LINE_X, -3.0 * SCALE)

		context.g.stroke = Themes.get<GraphTheme>().edge.stroke

		// Gate connection
		val gateConnectionY = when(handedness) {
			RIGHT -> 0.0
			LEFT -> - 4.0 * SCALE
		}
		context.g.drawLine(
			DigitalPortView.LENGTH.toDouble(), gateConnectionY,
			GATE_LINE_X, gateConnectionY)

		// Source connection
		context.g.drawLine(
			SIGNAL_LINE_X, -0.5 * SCALE,
			SIGNAL_PORT_X, -0.5 * SCALE)
		context.g.drawLine(
			SIGNAL_PORT_X, -0.5 * SCALE,
			SIGNAL_PORT_X, 1.0 * SCALE)

		// Drain connection
		context.g.drawLine(
			SIGNAL_LINE_X, -3.5 * SCALE,
			SIGNAL_PORT_X, -3.5 * SCALE)
		context.g.drawLine(
			SIGNAL_PORT_X, -3.5 * SCALE,
			SIGNAL_PORT_X, -5.0 * SCALE)

		drawBulkArrow(context)
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