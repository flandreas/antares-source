package io.antarescircuit.antares.view.net

import io.antarescircuit.antares.model.net.TransistorIF
import io.antarescircuit.antares.model.net.TransistorType
import io.antarescircuit.antares.view.Handedness
import io.antarescircuit.antares.view.Handedness.LEFT
import io.antarescircuit.antares.view.Handedness.RIGHT
import io.antarescircuit.antares.view.port.AbstractAntaresPortView.Companion.LENGTH
import io.antarescircuit.antares.view.port.ExternalPortLabelDistance
import io.antarescircuit.jabbah.base.Properties
import io.antarescircuit.jabbah.base.StringUtils
import io.antarescircuit.jabbah.base.geom.Direction
import io.antarescircuit.jabbah.base.geom.Direction.SOUTH
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.geom.Rectangle2D
import io.antarescircuit.jabbah.base.geom.RectangularShape
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.drawable.AbstractDrawable
import io.antarescircuit.jabbah.draw.graphics.CompositeColor
import io.antarescircuit.jabbah.draw.graphics.DropShadow
import io.antarescircuit.jabbah.draw.style.DrawStyleModule
import io.antarescircuit.jabbah.draw.style.StyleProvider
import io.antarescircuit.jabbah.draw.style.StyleType
import io.antarescircuit.jabbah.edit.Look
import io.antarescircuit.jabbah.edit.Look.SCALE
import io.antarescircuit.jabbah.graph.GraphApplicationContext
import io.antarescircuit.jabbah.graph.view.LabeledRectangularVerticeView
import io.antarescircuit.jabbah.graph.view.port.PortLabelPosition
import io.antarescircuit.jabbah.graph.view.port.PortView
import io.antarescircuit.jabbah.graph.view.vertice.AbstractVerticeView
import io.antarescircuit.jabbah.io.Storable
import io.antarescircuit.jabbah.io.StoreReader
import io.antarescircuit.jabbah.io.StoreWriter
import kotlin.jvm.JvmStatic

abstract class AbstractTransistorView<T: TransistorIF<*>>(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	model: T,
	handedness: Handedness = DEFAULT_HANDEDNESS
) : LabeledRectangularVerticeView<T>(styleProvider, model) {

	companion object {

		@JvmStatic
		protected val DEFAULT_HANDEDNESS = RIGHT

		protected const val LABEL_DIST = SCALE
		const val WIDTH = 6 * SCALE
		const val HEIGHT = 6 * SCALE

		protected val hasCircle: Boolean get() = BaseModule.properties.getBoolean(PROP_TRANSISTOR_CIRCLE)

		private val showPortNames: Boolean get() = BaseModule.properties.getBoolean(PROP_TRANSISTOR_PORT_NAMES)

		/** The name of the [Boolean] property in [Properties] defining whether transistors are drawn with a circle. */
		const val PROP_TRANSISTOR_CIRCLE = "antares.transistor.circle"

		/** The name of the [Boolean] property in [Properties] defining whether transistors display port names.*/
		const val PROP_TRANSISTOR_PORT_NAMES = "antares.transistor.portNames"

		val portLabelPosition get() = if (showPortNames) {
			PortLabelPosition.EXTERNAL
		} else {
			PortLabelPosition.HIDE
		}

		val externalPortLabelDistance get() = if (hasCircle) {
			ExternalPortLabelDistance.Small
		} else {
			ExternalPortLabelDistance.None
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
				update()
			}
		}

	init {
		initExternalLabel()
	}

	override val relativeExternalLabelLocation: Point2D get() =
		Point2D(
			LENGTH + WIDTH + LABEL_DIST,
			when (handedness) {
				RIGHT -> -2 * SCALE
				LEFT -> 2 * SCALE
			})

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

	override val boundingBox: RectangularShape
		get() {
			val bb = Rectangle2D(super.boundingBox)
			val lbb = Rectangle2D(label.boundingBox).moveBy(location)
			bb.add(lbb)
			return bb
		}

	/** ---- [AbstractVerticeView] */

	override fun drawImpl(context: DrawContext) {
		if (hasCircle && shadow) {
			DropShadow.draw(context, transparency) {
				context.g.fillOval(
					LENGTH.toDouble(), -5.0 * SCALE,
					WIDTH.toDouble(), HEIGHT.toDouble())
			}
		}
		super.drawImpl(context)

		drawBody(context)
		symbol.render(this, context)
	}

	override fun getEditPortViewColor(styleProvider: StyleProvider): CompositeColor =
		customColor?.color ?: super.getEditPortViewColor(styleProvider)

	/** ---- [TransistorView] */

	/** If `true`, the symbol visualizes the on/off state of this [AbstractTransistorView] (if supported by the symbol style). */
	abstract val drawOnOff: Boolean

	/**
	 * The displacement (in view coordinates) of the on/off indication. For digital transistors, this will either be
	 * 0 (if the transistor is "on") or a maximum value (if the transistor is "off"). For analog transistors, this will
	 * be a value that corresponds with the transistor's source/drain conductance.
	 */
	abstract val switchOffDisplacement: Double

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

	override fun updateGeometry() {
		getPortView(model.gatePort)?.apply {
			setLocation(
				LENGTH,
				symbol.getGatePositionY(this@AbstractTransistorView))
		}
		northPortView.apply {
			setLocation(LENGTH + 4 * SCALE, -5 * SCALE)
			direction = Direction.NORTH
		}
		southPortView.apply {
			setLocation(LENGTH + 4 * SCALE, SCALE)
			direction = SOUTH
		}
	}

	private fun drawBody(context: DrawContext) {
		if (hasCircle) {
			context.g.stroke = stroke

			val localBackgroundColor = if (context.castedAppContext<GraphApplicationContext>()!!.showNetState) {
				// During execution, Transistor internals are drawn in signal colors, so use the drawing background
				// color to make the signal colors more recognizable
				styleProvider.getStyle(StyleType.BACKGROUND).color.backgroundColor
			} else {
				context.chooseBackground(
					if (Look.FILL_BASIC_COMPONENTS) backgroundColor else DrawStyleModule.styleProvider.getStyle(StyleType.BACKGROUND).color.backgroundColor
				)
			}
			context.g.color = localBackgroundColor

			context.g.fillOval(
				LENGTH.toDouble(), -5.0 * SCALE,
				WIDTH.toDouble(), HEIGHT.toDouble())

			context.g.color = transparent.applyTo(context.chooseForeground(foregroundColor))
			context.g.drawOval(
				LENGTH.toDouble(), -5.0 * SCALE,
				WIDTH.toDouble(), HEIGHT.toDouble())
		}
	}
}