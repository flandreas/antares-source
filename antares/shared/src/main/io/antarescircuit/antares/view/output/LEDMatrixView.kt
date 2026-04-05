package io.antarescircuit.antares.view.output

import io.antarescircuit.antares.model.output.LEDMatrix
import io.antarescircuit.antares.model.output.LightEmitterModel
import io.antarescircuit.antares.model.signal.BitWidth
import io.antarescircuit.antares.view.port.DigitalPortView
import io.antarescircuit.antares.view.style.AntaresTheme
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.geom.Direction
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.geom.RectangularShape
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.graphics.Color
import io.antarescircuit.jabbah.draw.graphics.DropShadow
import io.antarescircuit.jabbah.draw.style.DrawStyleModule
import io.antarescircuit.jabbah.draw.style.StyleProvider
import io.antarescircuit.jabbah.draw.style.Themes
import io.antarescircuit.jabbah.edit.Component
import io.antarescircuit.jabbah.edit.Look
import io.antarescircuit.jabbah.edit.SelectionDrawingStrategy
import io.antarescircuit.jabbah.edit.model.Size
import io.antarescircuit.jabbah.edit.select.AbstractSelectionModel
import io.antarescircuit.jabbah.graph.GraphApplicationContext
import io.antarescircuit.jabbah.graph.model.Graph
import io.antarescircuit.jabbah.graph.model.GraphElementEvent
import io.antarescircuit.jabbah.graph.model.vertice.VerticeLink
import io.antarescircuit.jabbah.graph.view.*
import io.antarescircuit.jabbah.graph.view.port.PortLabelPosition
import io.antarescircuit.jabbah.graph.view.vertice.AbstractVerticeView
import io.antarescircuit.jabbah.graph.view.vertice.SubGraphVerticeView
import io.antarescircuit.jabbah.io.Storable
import io.antarescircuit.jabbah.io.StoreReader
import io.antarescircuit.jabbah.io.StoreWriter
import kotlin.math.ceil

class LEDMatrixView(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	model: LEDMatrix = LEDMatrix(),
	lightColor: LightColor = DEFAULT_LIGHT_COLOR,
	private val eventBus: EventBus = BaseModule.eventBus
) : LabeledRectangularVerticeView<LEDMatrix>(styleProvider, model),
	LightEmitter,
	ControlView<LEDMatrix>,
	ControlViewSource<LEDMatrix>
{
	companion object {
		const val PROP_ICON_PATH = "io.antarescircuit.antares.view.output.LEDMatrixView.iconPath"
		private val DEBUG_COLUMN_COLOR = Color(255, 255, 0, 128)
		private val DEBUG_ROW_COLOR = Color(0, 255, 255, 128)
		private val DEFAULT_LIGHT_COLOR = LightColor.RED
		private val DEFAULT_SIZE = Size.MEDIUM
		private const val DOT_SIZE = Look.SCALE
	}

	// Cannot extract to delegate because of dependency on ControlViewSource
	override var lightColor: LightColor by ControlViewSourceProperty(lightColor)

	override val hasGraphParameter: Boolean get() = lightColor is LightColorExpression

	var size: Size by ControlViewSourceGeometryProperty(DEFAULT_SIZE, eventBus, ::updateGeometry)

	/** `true` if the dots are drawn as circles, `false` if they are drawn as squares .*/
	@Suppress("MemberVisibilityCanBePrivate") // Reflection
	var isCircleDots: Boolean by ControlViewSourceProperty(true)

	/** `true` if the dots additionally show whether the corresponding port bits are set to 1.*/
	@Suppress("MemberVisibilityCanBePrivate") // Reflection
	var isDebug: Boolean = false

	private val factor: Double
		get() = when (size) {
			Size.SMALL -> 1.0
			Size.MEDIUM -> 2.0
			Size.LARGE -> 3.0
		}

	private val inset = 0.0

	init {
		initExternalLabel(Direction.NORTH)
		modelExchanged(null)
	}

	override val relativeExternalLabelLocation: Point2D get() =
		Point2D(calculateWidth() / 2, -LABEL_DIST.toDouble())

	/** ---- UI controllable properties */

	@Suppress("MemberVisibilityCanBePrivate") // Reflection
	var columnWidth: BitWidth
		get() = model.columnWidth
		set(value) {
			if (value != columnWidth) {
				model.columnWidth = value
				modelExchanged(model)
				postControlViewSourceChangeEvent(eventBus)
			}
		}

	@Suppress("MemberVisibilityCanBePrivate") // Reflection
	var rowWidth: BitWidth
		get() = model.rowWidth
		set(value) {
			if (value != rowWidth) {
				model.rowWidth = value
				modelExchanged(model)
				postControlViewSourceChangeEvent(eventBus)
			}
		}

	@Suppress("MemberVisibilityCanBePrivate") // Reflection
	var afterglowDuration: Long
		get() = model.afterglowDuration
		set(value) {
			model.afterglowDuration = value
		}

	/** ---- [ControlView] */

	override var isActiveControlView: Boolean = false

	override val mirrorWidth: Double get() = width

	override val mirrorHeight: Double get() = -height

	override val controlId: String
		get() = "ledMatrix:${model.id}"

	override fun bindControlView(subGraphVerticeView: SubGraphVerticeView<*>, link: VerticeLink, startGraph: Graph) {
		this.model = link.getLinkedObject(startGraph) as LEDMatrix
	}

	override fun sourcePropertiesChanged(source: ControlViewSource<LEDMatrix>) {
		if (source is LEDMatrixView) {
			copyControlViewProperties(source, this)
		}
	}

	override fun writeModelProperties(writer: StoreWriter) {
		writer.writeString("columnWidth", columnWidth.customName)
		writer.writeString("rowWidth", rowWidth.customName)
		writer.writeLong("afterglow", afterglowDuration)
	}

	override fun readModelProperties(reader: StoreReader) {
		// conditional access in order to support backward compatibility
		if (reader.hasAttribute("columnWidth")) {
			columnWidth = BitWidth.withName(reader.readString("columnWidth"))
		}
		if (reader.hasAttribute("rowWidth")) {
			rowWidth = BitWidth.withName(reader.readString("rowWidth"))
		}
		if (reader.hasAttribute("afterglow")) {
			afterglowDuration = reader.readLong("afterglow")
		}
	}

	private fun copyControlViewProperties(source: LEDMatrixView, dest: LEDMatrixView) {
		dest.model.name = source.model.name
		dest.lightColor = source.lightColor
		dest.isCircleDots = source.isCircleDots
		dest.size = source.size
		dest.columnWidth = source.columnWidth
		dest.rowWidth = source.rowWidth
	}

	/** ---- [ControlViewSource] */

	override val controlName: String get() = super.controlName

	override val iconPath: String get() = BaseModule.properties.getString(PROP_ICON_PATH)

	override fun createControlView(): ControlView<LEDMatrix> {
		val clone = LEDMatrixView(styleProvider, model, lightColor)
		clone.isShowPortViews = false
		clone.isDebug = false
		copyControlViewProperties(this, clone)
		return clone
	}


	/** ---- [AbstractVerticeView] */

	override fun modelExchanged(oldModel: LEDMatrix?) {
		super.modelExchanged(oldModel)

		val columnPort = DigitalPortView(
			styleProvider = styleProvider,
			port = model.getInput(LEDMatrix.COLUMN_PORT_NAME),
			direction = Direction.SOUTH,
			portLabelPosition = PortLabelPosition.EXTERNAL,
			x = calculateColumnPortPos().x.toInt(),
			y = calculateColumnPortPos().y.toInt())
		columnPort.showBitWidthAnnotation = false
		addPortView(columnPort)

		val rowPort = DigitalPortView(
			styleProvider = styleProvider,
			port = model.getInput(LEDMatrix.ROW_PORT_NAME),
			direction = Direction.SOUTH,
			portLabelPosition = PortLabelPosition.EXTERNAL,
			x = calculateRowPortPos().x.toInt(),
			y = calculateRowPortPos().y.toInt())
		rowPort.showBitWidthAnnotation = false
		addPortView(rowPort)

		updateGeometry()
	}

	/** ---- [Storable] */

	override fun write(writer: StoreWriter) {
		super.write(writer)
		lightColor.write("lightColor", writer)
		writer.writeString("size", size.customName)
		if (isCircleDots) {
			writer.writeBoolean("circle", true)
		}
		if (isDebug) {
			writer.writeBoolean("debug", true)
		}
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		lightColor = LightColor.read("lightColor", reader)
		size = Size.withName(reader.readString("size"))
		isCircleDots = reader.hasAttribute("circle")
		isDebug = reader.hasAttribute("debug")
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
				context.g.fillRect(0, 0, width.toInt(), height.toInt())
			}
		}

		super.drawImpl(context)

		val isExecute = context.castedAppContext<GraphApplicationContext>()!!.isExecute

		val oldColor = context.g.color
		val oldStroke = context.g.stroke

		context.g.color = if (isExecute) {
			Themes.get<AntaresTheme>().screen.backgroundColor
		} else {
			context.chooseBackground(backgroundColor)
		}
		context.g.fillRect(0, 0, width.toInt(), height.toInt())

		context.g.stroke = Themes.get<AntaresTheme>().annotation.stroke
		var x = width - inset - DOT_SIZE * factor
		for (column in 0 until model.columnWidth.width) {
			var y = height - inset - DOT_SIZE * factor
			for (row in 0 until model.rowWidth.width) {
				context.g.color = if (isExecute) {
					lightColor.executeColor(model.isOn(column, row))
				} else {
					context.chooseForeground(foregroundColor)
				}

				if (isCircleDots) {
					if (isExecute) {
						context.g.fillOval(
							x.toInt() + 1,
							y.toInt() + 1,
							(DOT_SIZE * factor).toInt() - 2,
							(DOT_SIZE * factor).toInt() - 2
						)
					} else {
						context.g.drawOval(
							x.toInt() + 1,
							y.toInt() + 1,
							(DOT_SIZE * factor).toInt() - 2,
							(DOT_SIZE * factor).toInt() - 2
						)
					}
				} else {
					if (isExecute) {
						context.g.fillRect(
							x.toInt(),
							y.toInt(),
							ceil(DOT_SIZE * factor).toInt(),
							ceil(DOT_SIZE * factor).toInt()
						)
					} else {
						context.g.drawRect(
							x.toInt(),
							y.toInt(),
							ceil(DOT_SIZE * factor).toInt(),
							ceil(DOT_SIZE * factor).toInt()
						)
					}
				}

				if (isDebug) {
					if ((model.columnPort.getIncomingSignal()!!).bitAt(column).isSet) {
						context.g.color = DEBUG_COLUMN_COLOR
						context.g.drawRect(
							x.toInt() + 1,
							y.toInt() + 1,
							(DOT_SIZE * factor).toInt() - 2,
							(DOT_SIZE * factor).toInt() - 2)
					}

					if ((model.rowPort.getIncomingSignal()!!).bitAt(row).isSet) {
						context.g.color = DEBUG_ROW_COLOR
						context.g.drawRect(
							x.toInt() + 1,
							y.toInt() + 1,
							(DOT_SIZE * factor).toInt() - 2,
							(DOT_SIZE * factor).toInt() - 2)
					}
				}

				y -= DOT_SIZE * factor
			}
			x -= DOT_SIZE * factor
		}

		if (model.inactive && context.castedAppContext<GraphApplicationContext>()!!.isExecute) {
			context.g.color = Look.inactiveColor
			context.g.fill(bounds)
		}

		context.g.stroke = oldStroke
		context.g.color = oldColor
	}

	/** ---- [LEDMatrixView] */

	override fun drawSelected(context: DrawContext) {
		draw(context) {
			super.drawImpl(it)
			context.g.stroke = stroke
			context.g.color = context.color!!.foregroundColor
			context.g.drawRect(0, 0, width.toInt(), height.toInt())
		}
	}

	private fun calculateWidth() = model.columnWidth.width * DOT_SIZE * factor + 2 * inset

	private fun calculateHeight() = model.rowWidth.width * DOT_SIZE * factor + 2 * inset

	private fun calculateColumnPortPos() = Point2D(calculateWidth() / 2 - 2 * Look.SCALE, calculateHeight())

	private fun calculateRowPortPos() = Point2D(calculateWidth() / 2 + 2 * Look.SCALE, calculateHeight())

	override fun updateGeometry() {
		invalidate()

		width = calculateWidth()
		height = calculateHeight()

		getPortView(model.columnPort)!!.location = calculateColumnPortPos()
		getPortView(model.rowPort)!!.location = calculateRowPortPos()

		super.updateGeometry()

		invalidate()
	}
}

class LEDMatrixViewSelectionModel(c: LEDMatrixView) : AbstractSelectionModel<LEDMatrixView>(c) {

	override fun draw(context: DrawContext) {
		val oldUseContextColors = context.useContextColors
		context.useContextColors = true
		context.color = Themes.get<AntaresTheme>().selection.color
		component.drawSelected(context)
		context.useContextColors = oldUseContextColors
	}

	override val boundingBox: RectangularShape
		get() = component.boundingBox

	override fun contains(x: Double, y: Double): Boolean {
		return component.contains(x, y)
	}

	override fun componentUpdated() {
		validate()
	}
}