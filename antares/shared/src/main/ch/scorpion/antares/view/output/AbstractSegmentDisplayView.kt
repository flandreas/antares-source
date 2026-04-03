package ch.scorpion.antares.view.output

import ch.scorpion.antares.model.Logic
import ch.scorpion.antares.model.output.AbstractSegmentDisplay
import ch.scorpion.antares.model.output.LightEmitterModel
import ch.scorpion.antares.model.output.SixteenSegmentDisplay
import ch.scorpion.antares.view.port.DigitalPortView
import ch.scorpion.antares.view.style.AntaresTheme
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.container.DrawableProperty
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.DropShadow
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.style.Themes
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Look
import ch.scorpion.jabbah.edit.SelectionDrawingStrategy
import ch.scorpion.jabbah.edit.model.Size
import ch.scorpion.jabbah.graph.GraphApplicationContext
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.model.GraphElementEvent
import ch.scorpion.jabbah.graph.model.vertice.VerticeLink
import ch.scorpion.jabbah.graph.view.*
import ch.scorpion.jabbah.graph.view.port.PortLabelPosition
import ch.scorpion.jabbah.graph.view.vertice.AbstractVerticeView
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter
import kotlin.jvm.JvmStatic
import kotlin.math.PI
import kotlin.math.floor

abstract class AbstractSegmentDisplayView<T: AbstractSegmentDisplay<T>>(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	model: T,
	lightColor: LightColor = DEFAULT_LIGHT_COLOR,
	size: Size = DEFAULT_SIZE,
	private val narrowSegments: Boolean = false,
	protected val eventBus: EventBus = BaseModule.eventBus
) : LabeledRectangularVerticeView<T>(styleProvider, model),
	LightEmitter,
	ControlViewSource<T>,
	ControlView<T>
{

	companion object {

		@JvmStatic
		protected val DEFAULT_LIGHT_COLOR = LightColor.RED

		@JvmStatic
		protected val DEFAULT_SIZE = Size.MEDIUM

		protected const val DEFAULT_HAS_BORDER = true

		protected val wideGeometries = mapOf(
			Size.SMALL to Geometry(factor = 1f),
			Size.MEDIUM to Geometry(factor = 1.5f),
			Size.LARGE to Geometry(factor = 2.0f)
		)

		protected val narrowGeometries = mapOf(
			Size.SMALL to Geometry(factor = 1f, segmentInset = 0.5f),
			Size.MEDIUM to Geometry(factor = 1.5f, segmentInset = 0.5f),
			Size.LARGE to Geometry(factor = 2.0f, segmentInset = 0.5f)
		)
	}

	var size: Size by ControlViewSourceGeometryProperty(size, eventBus) {
		handleSizeChanged()
		updateGeometry()
	}

	@Suppress("MemberVisibilityCanBePrivate") // Reflection
	var hasBorder: Boolean by DrawableProperty(DEFAULT_HAS_BORDER)

	/** Returns the [Geometry] of the current [SevenSegmentDisplayView] size.*/
	protected val geom: Geometry get() = if (narrowSegments) {
		narrowGeometries.getValue(size)
	} else {
		wideGeometries.getValue(size)
	}

	init {
		initExternalLabel(Direction.NORTH)
		modelExchanged(null)
		width = geom.width.toDouble()
		height = geom.height.toDouble()
	}

	override val relativeExternalLabelLocation: Point2D get() = Point2D(geom.width / 2, - LABEL_DIST)

	/** ---- UI properties */

	var logic: Logic
		get() = model.logic
		set(value) {
			model.logic = value
		}

	/** ---- [LightEmitter] interface */

	// Cannot extract to delegate because of dependency on ControlViewSource
	override var lightColor: LightColor by ControlViewSourceProperty(lightColor, eventBus)

	override val hasGraphParameter: Boolean get() = lightColor is LightColorExpression

	/** ---- [Storable] */

	override fun write(writer: StoreWriter) {
		super.write(writer)
		lightColor.write("lightColor", writer)
		writer.writeString("size", size.customName)
		writer.writeBoolean("hasBorder", hasBorder)
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		lightColor = LightColor.read("lightColor", reader)
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

	override fun bindControlView(subGraphVerticeView: SubGraphVerticeView<*>, link: VerticeLink, startGraph: Graph) {
		this.model = link.getLinkedObject(startGraph) as T
	}

	override fun writeModelProperties(writer: StoreWriter) {
		if (StringUtils.isNotEmpty(name)) {
			writer.writeString("name", name!!)
		}
	}

	override fun readModelProperties(reader: StoreReader) {
		// conditional access to support backward compatibility
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

	/** ---- [AbstractGraphElementView] */

	override fun bind(graphView: GraphView, deep: Boolean) {
		super.bind(graphView, deep)
		graphParamsChanged(graphView.graph!!)
	}

	override fun handleStateChanged(event: GraphElementEvent) {
		super.handleStateChanged(event)
		if (event.reason == LightEmitterModel.REASON_GRAPH_PARAM_CHANGED && event.argument is Graph) {
			graphParamsChanged(event.argument as Graph)
		}
	}

	override fun graphParamsChanged(graph: Graph) {
		(lightColor as? LightColorExpression)?.let { it.evaluateIn(graph)?.let { lc -> lightColor = lc } }
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
			0.5f * geom.snappedScaledFactor + geom.segLength + geom.snappedScaledFactor,
			7 * geom.snappedScaledFactor + geom.segHalfWidth)
	}

	private fun drawDot(context: DrawContext, value: Boolean, relX: Float, relY: Float) {
		context.g.color = getColor(value, context)
		context.translated(relX.toDouble(), relY.toDouble()) {
			it.g.fillOval(-geom.dotSize / 2, -geom.dotSize / 2, geom.dotSize, geom.dotSize)
		}
	}

	protected fun getColor(value: Boolean, context: DrawContext): Color =
		transparent.applyTo(
			if (context.castedAppContext<GraphApplicationContext>()!!.isExecute) {
				lightColor.executeColor(logic.evaluate(value))
			} else {
				context.chooseForeground(foregroundColor)
			}
		)

	/** ---- [AbstractSegmentDisplayView] */

	protected open fun handleSizeChanged() { }

	protected fun createCombinedPortViews() {
		val startX = when (size) {
			Size.LARGE, Size.MEDIUM -> 2 * Look.SCALE
			else -> Look.SCALE
		}
		val deltaX = when (size) {
			Size.LARGE -> 6 * Look.SCALE
			Size.MEDIUM -> 4 * Look.SCALE
			Size.SMALL -> 3 * Look.SCALE
		}
		val pv = DigitalPortView(
			styleProvider = styleProvider,
			port = model.getInput(SixteenSegmentDisplay.SEG_INPUT_NAME),
			direction = Direction.SOUTH,
			portLabelPosition = PortLabelPosition.EXTERNAL,
			x = startX,
			y = geom.height)
		pv.showBitWidthAnnotation = false
		addPortView(pv)
		addPortView(
			DigitalPortView(
				styleProvider = styleProvider,
				port = model.getInput(SixteenSegmentDisplay.DP_INPUT_NAME),
				direction = Direction.SOUTH,
				portLabelPosition = PortLabelPosition.EXTERNAL,
				x = startX + deltaX,
				y = geom.height)
		)
	}

	protected fun drawFullHorizontalSegment(context: DrawContext, value: Boolean, relX: Float, relY: Float) {
		context.g.color = getColor(value, context)
		context.translated(relX.toDouble(), relY.toDouble()) {
			it.g.fill(geom.path)
		}
	}

	protected fun drawHalfHorizontalSegment(context: DrawContext, value: Boolean, relX: Float, relY: Float) {
		context.g.color = getColor(value, context)
		context.translated(relX.toDouble(), relY.toDouble()) {
			it.g.fill(geom.halfPath)
		}
	}

	protected fun drawVerticalSegment(context: DrawContext, value: Boolean, relX: Float, relY: Float) {
		context.g.color = getColor(value, context)
		context.translatedAndRotated(relX.toDouble(), relY.toDouble(), PI / 2) {
			it.g.fill(geom.path)
		}
	}

	protected fun drawB(context: DrawContext) {
		with (geom) {
			drawVerticalSegment(context, model.inputValueOf("b"),
				0.5f * snappedScaledFactor + segLength + segHalfWidth,
				snappedScaledFactor + segHalfWidth)
		}
	}

	protected fun drawC(context: DrawContext) {
		with (geom) {
			drawVerticalSegment(
				context, model.inputValueOf("c"),
				0.5f * snappedScaledFactor + segLength + segHalfWidth,
				4 * snappedScaledFactor + segHalfWidth
			)
		}
	}

	protected fun drawE(context: DrawContext) {
		with (geom) {
		drawVerticalSegment(context, model.inputValueOf("e"),
			0.5f * snappedScaledFactor + segHalfWidth,
			4 * snappedScaledFactor + segHalfWidth)}
	}

	protected fun drawF(context: DrawContext) {
		with (geom) {
			drawVerticalSegment(
				context, model.inputValueOf("f"),
				0.5f * snappedScaledFactor + segHalfWidth,
				1 * snappedScaledFactor + segHalfWidth
			)
		}
	}

	override fun updateGeometry() {
		width = geom.width.toDouble()
		height = geom.height.toDouble()
		modelExchanged(model)
		super.updateGeometry()
	}

	class Geometry(val factor: Float, segmentInset: Float = 1.0f) {
		val snappedScaledFactor: Float = floor(factor * Look.SCALE)
		// Width and height must snap to SCALE
		val width: Int = ((5 * factor * Look.SCALE) / Look.SCALE).toInt() * Look.SCALE
		val height: Int = ((8 * factor * Look.SCALE) / Look.SCALE).toInt() * Look.SCALE
		val segLength: Float = 3 * snappedScaledFactor
		val segHalfWidth: Float = 0.25f * factor * Look.SCALE

		private val segInset: Float = segmentInset * factor
		private val diagW = 0.325f * snappedScaledFactor - segInset
		private val diagH = 0.75f * snappedScaledFactor - segInset
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
	}
}