package ch.scorpion.antares.view.output

import ch.scorpion.antares.model.Logic
import ch.scorpion.antares.model.output.AbstractSegmentDisplay
import ch.scorpion.antares.model.output.SixteenSegmentDisplay
import ch.scorpion.antares.view.OrientableRectangularVerticeView
import ch.scorpion.antares.view.Look
import ch.scorpion.antares.view.port.DigitalPortView
import ch.scorpion.antares.view.style.AntaresTheme
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.DropShadow
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.style.Themes
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.SelectionDrawingStrategy
import ch.scorpion.jabbah.edit.model.Size
import ch.scorpion.jabbah.graph.GraphApplicationContext
import ch.scorpion.jabbah.graph.view.ControlView
import ch.scorpion.jabbah.graph.view.ControlViewSource
import ch.scorpion.jabbah.graph.view.port.PortLabelPosition
import ch.scorpion.jabbah.graph.view.vertice.AbstractVerticeView
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter
import kotlin.jvm.JvmStatic
import kotlin.math.PI

abstract class AbstractSegmentDisplayView<T: AbstractSegmentDisplay<T>>(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	model: T,
	lightColor: LightColor = DEFAULT_LIGHT_COLOR,
	protected val eventBus: EventBus = BaseModule.eventBus
) : OrientableRectangularVerticeView<T>(styleProvider, model), LightEmitter, ControlViewSource<T>, ControlView<T> {

	companion object {

		@JvmStatic
		protected val DEFAULT_LIGHT_COLOR = LightColor.RED

		protected val DEFAULT_SIZE = Size.MEDIUM

		protected const val DEFAULT_HAS_BORDER = true

		protected val geometries = mapOf(
			Size.SMALL to Geometry(factor = 1f),
			Size.MEDIUM to Geometry(factor = 1.5f),
			Size.LARGE to Geometry(factor = 2.0f)
		)
	}

	var size: Size = DEFAULT_SIZE
		set(value) {
			if (value != field) {
				invalidate()
				field = value
				handleSizeChanged()
				updateGeometry()
				invalidate()
				postControlViewSourceChangeEvent(eventBus)
			}
		}

	var hasBorder: Boolean = DEFAULT_HAS_BORDER
		set(value) {
			if (field != value) {
				invalidate()
				field = value
				invalidate()
			}
		}

	/** Returns the [Geometry] of the current [SevenSegmentDisplayView] size.*/
	protected val geom: Geometry get() = geometries.getValue(size)

	init {
		modelExchanged(null)
		width = geom.width.toDouble()
		height = geom.height.toDouble()
	}

	/** ---- UI properties */

	var name: String?
		get() = model.name
		set(value) {
			if (value != name) {
				model.name = value
				postControlViewSourceChangeEvent(eventBus)
			}
		}

	var logic: Logic
		get() = model.logic
		set(value) {
			model.logic = value
		}

	/** ---- [LightEmitter] interface */

	override var lightColor: LightColor = lightColor
		set(value) {
			if (field != value) {
				invalidate()
				field = value
				postControlViewSourceChangeEvent(eventBus)
			}
		}

	/** ---- [Storable] */

	override fun write(writer: StoreWriter) {
		super.write(writer)
		writer.writeString("lightColor", lightColor.customName)
		writer.writeString("size", size.customName)
		writer.writeBoolean("hasBorder", hasBorder)
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		lightColor = LightColor.withName(reader.readString("lightColor"))
		size = Size.withName(reader.readString("size"))
		hasBorder = if (reader.hasAttribute("hasBorder")) {
			reader.readBoolean("hasBorder")
		} else {
			// Backward compatibility
			false
		}
	}

	/** ---- [ControlView] */

	override val controlName: String get() = super.controlName

	override var isActiveControlView: Boolean = false

	override val mirrorWidth: Double get() = width

	override val mirrorHeight: Double get() = -height

	override fun bindControlView(subGraphVerticeView: SubGraphVerticeView<*>, model: T) {
		this.model = model
	}

	override fun writeModelProperties(writer: StoreWriter) {
		if (StringUtils.isNotEmpty(name)) {
			writer.writeString("name", name!!)
		}
	}

	override fun readModelProperties(reader: StoreReader) {
		// conditional access in order to support backward compatibility
		if (reader.hasAttribute("name")) {
			name = reader.readString("name")
		}
	}

	protected fun copyControlViewProperties(source: AbstractSegmentDisplayView<T>, dest: AbstractSegmentDisplayView<T>) {
		dest.model.name = source.model.name
		dest.lightColor = source.lightColor
		dest.size = source.size
	}

	/** ---- [Component] */

	override var preferredSelectionDrawingStrategy: SelectionDrawingStrategy?
		get() = SelectionDrawingStrategy.REPLACE
		set(value) {
			super.preferredSelectionDrawingStrategy = value
		}

	/** ---- [AbstractVerticeView] */

	override fun drawImpl(context: DrawContext) {
		if (shadow) {
			DropShadow.draw(context, transparency) {
				context.g.fillRect(0, 0, geom.width, geom.height)
			}
		}
		super.drawImpl(context)

		val isExecute = context.castedAppContext<GraphApplicationContext>()!!.isExecute

		context.g.color = if (isExecute) {
			transparent.applyTo(Themes.get<AntaresTheme>().screen.backgroundColor)
		} else {
			context.chooseBackground(backgroundColor)
		}
		context.g.fillRect(0, 0, geom.width, geom.height)

		if (hasBorder) {
			context.g.stroke = stroke
			context.g.color = if (context.castedAppContext<GraphApplicationContext>()!!.isExecute) {
				transparent.applyTo(Themes.get<AntaresTheme>().screen.foregroundColor)
			} else {
				context.chooseForeground(foregroundColor)
			}
			context.g.drawRect(0, 0, geom.width, geom.height)
		}

		drawDot(context, model.inputValueOf("p"),
			0.5f * geom.scaledFactor + geom.segLength + geom.scaledFactor,
			7 * geom.scaledFactor + geom.segHalfWidth)
	}

	private fun drawDot(context: DrawContext, value: Boolean, relX: Float, relY: Float) {
		context.g.translate(relX.toDouble(), relY.toDouble())
		context.g.color = getColor(value, context)
		context.g.fillOval(-geom.dotSize / 2, -geom.dotSize / 2, geom.dotSize, geom.dotSize)
		context.g.translate(-relX.toDouble(), -relY.toDouble())
	}

	protected fun getColor(value: Boolean, context: DrawContext): Color =
		transparent.applyTo(
			if (context.castedAppContext<GraphApplicationContext>()!!.isExecute) {
				if (logic.evaluate(value)) lightColor.onColor else lightColor.offColor
			} else {
				context.chooseForeground(foregroundColor)
			}
		)

	/** ---- [AbstractSegmentDisplayView] */

	protected open fun handleSizeChanged() { }

	protected fun createCombinedPortViews() {
		val dot = DigitalPortView(
			styleProvider = styleProvider,
			port = model.getInput(SixteenSegmentDisplay.SEG_INPUT_NAME),
			direction = Direction.SOUTH,
			portLabelPosition = PortLabelPosition.EXTERNAL,
			x = geom.scaledFactor,
			y = geom.height)
		dot.showBitWidthAnnotation = false
		addPortView(dot)
		addPortView(
			DigitalPortView(
				styleProvider = styleProvider,
				port = model.getInput(SixteenSegmentDisplay.DP_INPUT_NAME),
				direction = Direction.SOUTH,
				portLabelPosition = PortLabelPosition.EXTERNAL,
				x = geom.width - geom.scaledFactor,
				y = geom.height)
		)
	}


	protected fun drawFullHorizontalSegment(context: DrawContext, value: Boolean, relX: Float, relY: Float) {
		context.g.translate(relX.toDouble(), relY.toDouble())
		context.g.color = getColor(value, context)
		context.g.fill(geom.path)
		context.g.translate(-relX.toDouble(), -relY.toDouble())
	}

	protected fun drawHalfHorizontalSegment(context: DrawContext, value: Boolean, relX: Float, relY: Float) {
		context.g.translate(relX.toDouble(), relY.toDouble())
		context.g.color = getColor(value, context)
		context.g.fill(geom.halfPath)
		context.g.translate(-relX.toDouble(), -relY.toDouble())
	}

	protected fun drawVerticalSegment(context: DrawContext, value: Boolean, relX: Float, relY: Float) {
		context.g.translate(relX.toDouble(), relY.toDouble())
		context.g.rotate(PI / 2)

		context.g.color = getColor(value, context)
		context.g.fill(geom.path)

		context.g.rotate(-PI / 2)
		context.g.translate(-relX.toDouble(), -relY.toDouble())
	}

	protected fun drawB(context: DrawContext) {
		drawVerticalSegment(context, model.inputValueOf("b"),
			0.5f * geom.scaledFactor + geom.segLength + geom.segHalfWidth,
			geom.scaledFactor + geom.segHalfWidth)
	}

	protected fun drawC(context: DrawContext) {
		drawVerticalSegment(context, model.inputValueOf("c"),
			0.5f * geom.scaledFactor + geom.segLength + geom.segHalfWidth,
			4 * geom.scaledFactor + geom.segHalfWidth)
	}

	protected fun drawE(context: DrawContext) {
		drawVerticalSegment(context, model.inputValueOf("e"),
			0.5f * geom.scaledFactor + geom.segHalfWidth,
			4 * geom.scaledFactor + geom.segHalfWidth)
	}

	protected fun drawF(context: DrawContext) {
		drawVerticalSegment(context, model.inputValueOf("f"),
			0.5f * geom.scaledFactor + geom.segHalfWidth,
			1 * geom.scaledFactor + geom.segHalfWidth)
	}

	private fun updateGeometry() {
		width = geom.width.toDouble()
		height = geom.height.toDouble()
		modelExchanged(model)
	}

	class Geometry(val factor: Float) {
		val width: Int = (5 * factor * Look.SCALE).toInt()
		val height: Int = (8 * factor * Look.SCALE).toInt()
		val segLength: Float = 3 * factor * Look.SCALE
		val segHalfWidth: Float = 0.25f * factor * Look.SCALE
		private val segInset: Float = 1f * factor
		private val diagW = 0.325f * scaledFactor - segInset
		private val diagH = 0.75f * scaledFactor - segInset
		val dotSize: Int = (4 * factor).toInt()

		// The horizontal, full width segment
		val path = System.createPath()
			.moveTo(segInset, 0f)
			.lineTo(segInset + segHalfWidth, -segHalfWidth)
			.lineTo(segLength - segHalfWidth - segInset, -segHalfWidth)
			.lineTo(segLength - segInset, 0f)
			.lineTo(segLength - segHalfWidth - segInset, segHalfWidth)
			.lineTo(segInset + segHalfWidth, segHalfWidth)
			.close()

		val halfPath = System.createPath()
			.moveTo(segInset, 0f)
			.lineTo(segInset + segHalfWidth, -segHalfWidth)
			.lineTo(segLength / 2 - segHalfWidth - segInset, -segHalfWidth)
			.lineTo(segLength / 2 - segInset, 0f)
			.lineTo(segLength / 2 - segHalfWidth - segInset, segHalfWidth)
			.lineTo(segInset + segHalfWidth, segHalfWidth)
			.close()

		// Clockwise
		val diagonalEastPath = System.createPath()
			.moveTo(segInset + segHalfWidth, segInset + segHalfWidth)
			.lineTo(segInset + segHalfWidth + diagW, segInset + segHalfWidth)
			.lineTo(segLength / 2 - segHalfWidth - segInset, segLength - segHalfWidth - segInset - diagH)
			.lineTo(segLength / 2 - segHalfWidth - segInset, segLength - segHalfWidth - segInset)
			.lineTo(segLength / 2 - segHalfWidth - segInset - diagW, segLength - segHalfWidth - segInset)
			.lineTo(segInset + segHalfWidth, segHalfWidth + segInset + diagH)
			.close()

		// Counter-clockwise
		val diagonalWestPath = System.createPath()
			.moveTo(-segInset - segHalfWidth, segInset + segHalfWidth)
			.lineTo(-segInset - segHalfWidth - diagW, segInset + segHalfWidth)
			.lineTo(-segLength / 2 + segHalfWidth + segInset, segLength - segHalfWidth - segInset - diagH)
			.lineTo(-segLength / 2 + segHalfWidth + segInset, segLength - segHalfWidth - segInset)
			.lineTo(-segLength / 2 + segHalfWidth + segInset + diagW, segLength - segHalfWidth - segInset)
			.lineTo(-segInset - segHalfWidth, segHalfWidth + segInset + diagH)
			.close()

		val scaledFactor: Int get() = (factor * Look.SCALE).toInt()
	}
}