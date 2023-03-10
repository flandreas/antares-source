package ch.scorpion.antares.view.net

import ch.scorpion.antares.model.net.TransistorIF
import ch.scorpion.antares.model.net.TransistorType
import ch.scorpion.antares.view.Handedness
import ch.scorpion.antares.view.Look
import ch.scorpion.antares.view.OrientableRectangularVerticeView
import ch.scorpion.antares.view.port.AbstractAntaresPortView
import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.geom.Rotation
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.drawable.AbstractDrawable
import ch.scorpion.jabbah.draw.graphics.DropShadow
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.style.StyleType
import ch.scorpion.jabbah.edit.model.AbstractComponent
import ch.scorpion.jabbah.edit.model.text.HorizontalLabel
import ch.scorpion.jabbah.graph.view.port.PortView
import ch.scorpion.jabbah.graph.view.vertice.AbstractVerticeView
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter
import kotlin.jvm.JvmStatic

abstract class AbstractTransistorView<T: TransistorIF<*>>(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	model: T,
	handedness: Handedness = DEFAULT_HANDEDNESS
) : OrientableRectangularVerticeView<T>(styleProvider, model) {

	companion object {

		@JvmStatic
		protected val DEFAULT_HANDEDNESS = Handedness.RIGHT

		protected const val LABEL_DIST = Look.SCALE
		const val WIDTH = 6 * Look.SCALE
		const val HEIGHT = 6 * Look.SCALE

		protected val hasCircle: Boolean get() = BaseModule.properties.getBoolean(PROP_TRANSISTOR_CIRCLE)

		/** The name of the [Boolean] property in [Properties] defining whether transistors are drawn with a circle. */
		const val PROP_TRANSISTOR_CIRCLE = "antares.transistor.circle"
	}

	/** Displays [name]. */
	private val label = HorizontalLabel(
		owner = this,
		relLocation = Point2D.ZERO,
		font = font)

	var name: String?
		get() = model.name
		set(value) {
			if (value != name) {
				model.name = value
				updateLabel()
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

	/** ---- UI properties */

	var transistorType: TransistorType
		get() = model.transistorType
		set(value) {
			if (value != model.transistorType) {
				invalidate()
				model.transistorType = value
				updateGeometry()
				tooltip.reset()
				invalidate()
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
		if (hasCircle && shadow) {
			DropShadow.draw(context, transparency) {
				context.g.fillOval(
					AbstractAntaresPortView.LENGTH.toDouble(), -5.0 * Look.SCALE,
					WIDTH.toDouble(), HEIGHT.toDouble())
			}
		}
		super.drawImpl(context)

		drawBody(context)
		symbol.render(this, context)
	}

	private fun drawLabel(context: DrawContext) {
		context.g.color = styleProvider.getStyle(StyleType.BACKGROUND).color.textColor
		label.draw(context)
	}

	/** ---- [TransistorView] */

	/** If `true`, the symbol visualizes the on/off state of this [AbstractTransistorView] (if supported by the symbol style). */
	abstract val drawOnOff: Boolean

	val northPortView: PortView<*> get() =
		if (handedness == DEFAULT_HANDEDNESS) {
			when (transistorType) {
				TransistorType.N -> getPortView(model.drainPort)!!
				TransistorType.P -> getPortView(model.sourcePort)!!
			}
		} else {
			when (transistorType) {
				TransistorType.N -> getPortView(model.sourcePort)!!
				TransistorType.P -> getPortView(model.drainPort)!!
			}
		}

	val southPortView: PortView<*> get() =
		if (handedness == DEFAULT_HANDEDNESS) {
			when (transistorType) {
				TransistorType.N -> getPortView(model.sourcePort)!!
				TransistorType.P -> getPortView(model.drainPort)!!
			}
		} else {
			when (transistorType) {
				TransistorType.N -> getPortView(model.drainPort)!!
				TransistorType.P -> getPortView(model.sourcePort)!!
			}
		}

	private fun updateLabel() {
		invalidate()
		label.text = StringUtils.orEmpty(name)
		label.rotationChanged()
		invalidate()
		update()
	}

	protected fun updateGeometry() {
		getPortView(model.gatePort)?.apply {
			setLocation(
				AbstractAntaresPortView.LENGTH,
				symbol.getGatePositionY(this@AbstractTransistorView))
		}
		northPortView.apply {
			setLocation(AbstractAntaresPortView.LENGTH + 4 * Look.SCALE, -5 * Look.SCALE)
			direction = Direction.NORTH
		}
		southPortView.apply {
			setLocation(AbstractAntaresPortView.LENGTH + 4 * Look.SCALE, Look.SCALE)
			direction = Direction.SOUTH
		}
		label.relLocation = Point2D(
			AbstractAntaresPortView.LENGTH + WIDTH + LABEL_DIST,
			when (handedness) {
				Handedness.RIGHT -> -2 * Look.SCALE
				Handedness.LEFT -> 2 * Look.SCALE
			})
	}

	private fun drawBody(context: DrawContext) {
		if (hasCircle) {
			context.g.stroke = stroke
			context.g.color = transparent.applyTo(context.choose(
				if (Look.FILL_BASIC_COMPONENTS) color else DrawStyleModule.styleProvider.getStyle(StyleType.BACKGROUND).color
			).backgroundColor)
			context.g.fillOval(
				AbstractAntaresPortView.LENGTH.toDouble(), -5.0 * Look.SCALE,
				WIDTH.toDouble(), HEIGHT.toDouble())

			context.g.color = transparent.applyTo(context.choose(color).foregroundColor)
			context.g.drawOval(
				AbstractAntaresPortView.LENGTH.toDouble(), -5.0 * Look.SCALE,
				WIDTH.toDouble(), HEIGHT.toDouble())
		}
	}
}