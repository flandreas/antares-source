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
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Direction.*
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.geom.Rotation
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.drawable.AbstractDrawable
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.style.StyleType
import ch.scorpion.jabbah.edit.model.AbstractComponent
import ch.scorpion.jabbah.edit.model.text.HorizontalLabel
import ch.scorpion.jabbah.graph.view.port.PortLabelPosition
import ch.scorpion.jabbah.graph.view.vertice.AbstractVerticeView
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

class TransistorView(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	model: Transistor = Transistor(),
	handedness: Handedness = DEFAULT_HANDEDNESS
) : DigitalComponentView<Transistor>(styleProvider, model)
{
	constructor(type: TransistorType): this(model = Transistor(type), handedness = defaultHandedness(type))

	companion object {

		/** The name of the [Boolean] property in [Properties] defining whether transistors are drawn with a circle. */
		const val PROP_TRANSISTOR_CIRCLE = "antares.transistor.circle"

		private const val LABEL_DIST = SCALE
		private val DEFAULT_HANDEDNESS = RIGHT
		const val WIDTH = 6 * SCALE
		const val HEIGHT = 6 * SCALE

		private val hasCircle: Boolean get() = BaseModule.properties.getBoolean(PROP_TRANSISTOR_CIRCLE)

		private fun defaultHandedness(type: TransistorType): Handedness {
			return when (type) {
				TransistorType.P -> LEFT
				TransistorType.N -> RIGHT
			}
		}
	}

	/** [Handedness.RIGHT] means that gate and source are in [Direction.SOUTH].*/
	var handedness: Handedness = handedness
		set(value) {
			if (field != value) {
				invalidate()
				field = value
				updateGeometry()
				invalidate()
				update()
			}
		}

	var symbol: TransistorViewSymbol = TransistorViewSymbol.configured
		set(value) {
			if (value != field) {
				invalidate()
				field = value
				updateGeometry()
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
			0, 0,
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
			0, 0,
			NORTH
		)
		source.setPortName("S")
		source.portLabelPosition = PortLabelPosition.HIDE
		addPortView(source)

		// Drain
		val drain = DigitalPortView(
			styleProvider,
			model.getDrainPort(),
			0, 0,
			SOUTH
		)
		drain.setPortName("D")
		drain.portLabelPosition = PortLabelPosition.HIDE
		addPortView(drain)

		updateGeometry()
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
		writer.writeString("symbol", symbol.customName)
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		if (reader.hasAttribute("handedness")) {
			handedness = Handedness.withName(reader.readString("handedness"))
		}
		if (reader.hasAttribute("symbol")) {
			symbol = TransistorViewSymbol.withName(reader.readString("symbol"))
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
		symbol.render(this, context)
	}

	private fun drawLabel(context: DrawContext) {
		context.g.color = styleProvider.getStyle(StyleType.BACKGROUND).color.textColor
		label.draw(context)
	}

	/** ---- [TransistorView] */

	private fun updateGeometry() {
		getPortView(model.getGatePort())?.apply {
			setLocation(
				DigitalPortView.LENGTH,
				symbol.getGatePositionY(this@TransistorView))
		}
		getPortView(model.getSourcePort())?.apply {
			setLocation(
				DigitalPortView.LENGTH + 4 * SCALE,
				when (handedness) {
					RIGHT -> SCALE
					LEFT -> -5 * SCALE
				})
			direction = when (handedness) {
				RIGHT -> SOUTH
				LEFT -> NORTH
			}
		}
		getPortView(model.getDrainPort())?.apply {
			setLocation(
				DigitalPortView.LENGTH + 4 * SCALE,
				when (handedness) {
					RIGHT -> -5 * SCALE
					LEFT -> SCALE
				})
			direction = when (handedness) {
				RIGHT -> NORTH
				LEFT -> SOUTH
			}
		}
		label.relLocation = Point2D(
			DigitalPortView.LENGTH + WIDTH + LABEL_DIST,
			when (handedness) {
				RIGHT -> -2 * SCALE
				LEFT -> 2 * SCALE
			})
	}

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
}