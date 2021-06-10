package ch.scorpion.antares.view.output

import ch.scorpion.antares.model.output.LEDMatrix
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.Word
import ch.scorpion.antares.view.DigitalComponentView
import ch.scorpion.antares.view.Look
import ch.scorpion.antares.view.port.DigitalPortView
import ch.scorpion.antares.view.style.AntaresTheme
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.RectangularShape
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
import ch.scorpion.jabbah.edit.select.AbstractSelectionModel
import ch.scorpion.jabbah.graph.GraphApplicationContext
import ch.scorpion.jabbah.graph.view.ControlView
import ch.scorpion.jabbah.graph.view.ControlViewSource
import ch.scorpion.jabbah.graph.view.port.PortLabelPosition
import ch.scorpion.jabbah.graph.view.vertice.AbstractVerticeView
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter
import kotlin.math.ceil

/**
 * A view of a [LEDMatrix].
 */
class LEDMatrixView(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	model: LEDMatrix = LEDMatrix(),
	lightColor: LightColor = DEFAULT_LIGHT_COLOR,
	private val eventBus: EventBus = BaseModule.eventBus
) : DigitalComponentView<LEDMatrix>(styleProvider, model), LightEmitter, ControlView<LEDMatrix>, ControlViewSource<LEDMatrix> {

	companion object {
		const val PROP_ICON_PATH = "ch.scorpion.antares.view.output.LEDMatrixView.iconPath"
		private val DEBUG_COLUMN_COLOR = Color(255, 255, 0, 128)
		private val DEBUG_ROW_COLOR = Color(0, 255, 255, 128)
		private val DEFAULT_LIGHT_COLOR = LightColor.RED
		private val DEFAULT_SIZE = Size.MEDIUM
		private const val DOT_SIZE = Look.SCALE
	}

	override var lightColor: LightColor = lightColor
		set(value) {
			invalidate()
			field = value
			postControlViewSourceChangeEvent(eventBus)
		}

	var size: Size = DEFAULT_SIZE
		set(value) {
			if (value != field) {
				field = value
				updateGeometry()
				postControlViewSourceChangeEvent(eventBus)
			}
		}

	/** `true` if the dots are drawn as circles, `false` if the are drawn as squares .*/
	var isCircleDots: Boolean = true
		set(value) {
			if (field != value) {
				field = value
				postControlViewSourceChangeEvent(eventBus)
			}
		}

	/** `true` if the dots additionally show whether the corresponding port bits are set to 1.*/
	var isDebug: Boolean = false

	private val factor: Double
		get() = when (size) {
			Size.SMALL -> 1.0
			Size.MEDIUM -> 2.0
			Size.LARGE -> 3.0
		}

	private val inset = 0.0

	init {
		modelExchanged(null)
	}

	/** ---- UI controllable properties */

	var columnWidth: BitWidth
		get() = model.columnWidth
		set(value) {
			if (value != columnWidth) {
				model.columnWidth = value
				modelExchanged(model)
				postControlViewSourceChangeEvent(eventBus)
			}
		}

	var rowWidth: BitWidth
		get() = model.rowWidth
		set(value) {
			if (value != rowWidth) {
				model.rowWidth = value
				modelExchanged(model)
				postControlViewSourceChangeEvent(eventBus)
			}
		}

	var afterglowDuration: Long
		get() = model.afterglowDuration
		set(value) {
			model.afterglowDuration = value
		}

	/** ---- [ControlView] */

	override val controlId: String
		get() = "ledMatrix:${model.id}"

	override fun bindControlView(subGraphVerticeView: SubGraphVerticeView<*>, model: LEDMatrix) {
		this.model = model
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
		writer.writeString("lightColor", lightColor.customName)
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
		lightColor = LightColor.withName(reader.readString("lightColor"))
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

	/** ---- [AbstractVerticeView] */

	override fun drawImpl(context: DrawContext) {
		if (shadow) {
			DropShadow.draw(context, transparency) {
				context.g.fillRect(0, 0, width.toInt(), height.toInt())
			}
		}

		super.drawImpl(context)

		val oldColor = context.g.color
		val oldStroke = context.g.stroke

		context.g.color = Themes.get<AntaresTheme>().screen.backgroundColor
		context.g.fillRect(0, 0, width.toInt(), height.toInt())

		var x = width - inset - DOT_SIZE * factor
		for (column in 0 until model.columnWidth.width) {
			var y = height - inset - DOT_SIZE * factor
			for (row in 0 until model.rowWidth.width) {
				if (model.isOn(column, row)) {
					context.g.color = lightColor.onColor
				} else {
					context.g.color = lightColor.offColor
				}

				if (isCircleDots) {
					context.g.fillOval(
						x.toInt() + 1,
						y.toInt() + 1,
						(DOT_SIZE * factor).toInt() - 2,
						(DOT_SIZE * factor).toInt() - 2)
				} else {
					context.g.fillRect(
						x.toInt(),
						y.toInt(),
						ceil(DOT_SIZE * factor).toInt(),
						ceil(DOT_SIZE * factor).toInt())
					context.g.drawRect(
						x.toInt(),
						y.toInt(),
						ceil(DOT_SIZE * factor).toInt(),
						ceil(DOT_SIZE * factor).toInt())
				}

				if (isDebug) {
					if ((model.columnPort.getIncomingSignal() as Word).bitAt(column).isSet) {
						context.g.color = DEBUG_COLUMN_COLOR
						context.g.drawRect(
							x.toInt() + 1,
							y.toInt() + 1,
							(DOT_SIZE * factor).toInt() - 2,
							(DOT_SIZE * factor).toInt() - 2)
					}

					if ((model.rowPort.getIncomingSignal() as Word).bitAt(row).isSet) {
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

	private fun updateGeometry() {
		invalidate()

		width = calculateWidth()
		height = calculateHeight()

		getPortView(model.columnPort)!!.location = calculateColumnPortPos()
		getPortView(model.rowPort)!!.location = calculateRowPortPos()
		//modelExchanged(model)

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