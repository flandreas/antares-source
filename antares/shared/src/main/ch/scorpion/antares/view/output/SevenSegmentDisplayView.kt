package ch.scorpion.antares.view.output

import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.RectangularShape
import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.Graphics2D
import ch.scorpion.jabbah.draw.style.Themes
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.SelectionDrawingStrategy
import ch.scorpion.jabbah.edit.model.Size
import ch.scorpion.jabbah.edit.select.AbstractSelectionModel
import ch.scorpion.jabbah.graph.view.ControlView
import ch.scorpion.jabbah.graph.view.ControlViewSource
import ch.scorpion.jabbah.graph.view.port.PortLabelPosition
import ch.scorpion.jabbah.graph.view.vertice.AbstractVerticeView
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter
import ch.scorpion.antares.model.output.SevenSegmentDisplay
import ch.scorpion.antares.model.output.SevenSegmentDisplayScheme
import ch.scorpion.antares.view.DigitalComponentView
import ch.scorpion.antares.view.Look
import ch.scorpion.antares.view.port.DigitalPortView
import ch.scorpion.antares.view.style.AntaresTheme
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.draw.graphics.DropShadow
import ch.scorpion.jabbah.graph.GraphApplicationContext
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView
import kotlin.math.PI


/**
 * A view of a [SevenSegmentDisplay].
 */
class SevenSegmentDisplayView(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	model: SevenSegmentDisplay = SevenSegmentDisplay(),
	lightColor: LightColor = DEFAULT_LIGHT_COLOR,
	private val eventBus: EventBus = BaseModule.eventBus
) : DigitalComponentView<SevenSegmentDisplay>(styleProvider, model),
	LightEmitter, ControlView<SevenSegmentDisplay>, ControlViewSource<SevenSegmentDisplay> {

	companion object {
		const val PROP_ICON_PATH = "ch.scorpion.antares.view.output.SevenSegmentDisplayView.iconPath"
		private val DEFAULT_LIGHT_COLOR = LightColor.RED
		private val DEFAULT_SIZE = Size.MEDIUM
		private const val DEFAULT_HAS_BORDER = true

		private val geometries = mapOf(
			Size.SMALL to Geometry(factor = 1f),
			Size.MEDIUM to Geometry(factor = 1.5f),
			Size.LARGE to Geometry(factor = 2.0f))
	}

	override var lightColor: LightColor = lightColor
		set(value) {
			if (field != value) {
				invalidate()
				field = value
				postControlViewSourceChangeEvent(eventBus)
			}
		}

	var size: Size = DEFAULT_SIZE
		set(value) {
			if (value != field) {
				invalidate()
				field = value
				if (field != Size.LARGE) {
					// Medium and small display can only have combined connection scheme (no enough space for more ports)
					portScheme = SevenSegmentDisplayScheme.COMBINED
				}
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
	private val geom: Geometry get() = geometries.getValue(size)

	init {
		modelExchanged(null)
		width = geom.width.toDouble()
		height = geom.height.toDouble()
	}

	override fun modelExchanged(oldModel: SevenSegmentDisplay?) {
		super.modelExchanged(oldModel)

		if (model.portScheme == SevenSegmentDisplayScheme.COMBINED) {
			val dot = DigitalPortView(
				styleProvider = styleProvider,
				port = model.getInput("s"),
				direction = Direction.SOUTH,
				portLabelPosition = PortLabelPosition.EXTERNAL,
				x = geom.scaledFactor,
				y = geom.height)
			dot.showBitWidthAnnotation = false
			addPortView(dot)
			addPortView(DigitalPortView(
				styleProvider = styleProvider,
				port = model.getInput("p"),
				direction = Direction.SOUTH,
				portLabelPosition = PortLabelPosition.EXTERNAL,
				x = geom.width - geom.scaledFactor,
				y = geom.height))
		} else {
			for (i in 1..4) {
				addPortView(DigitalPortView(
					styleProvider = styleProvider,
					port = model.getInput(i),
					direction = Direction.NORTH,
					portLabelPosition = PortLabelPosition.EXTERNAL,
					x = geom.scaledFactor * i,
					y = 0))
			}
			for (i in 5..8) {
				addPortView(DigitalPortView(
					styleProvider = styleProvider,
					port = model.getInput(i),
					direction = Direction.SOUTH,
					portLabelPosition = PortLabelPosition.EXTERNAL,
					x = geom.scaledFactor * (i - 4),
					y = geom.height))
			}
		}
	}

	/** ---- UI properties */

	var portScheme: SevenSegmentDisplayScheme
		get() = model.portScheme
		set(value) {
			if (value != portScheme) {
				invalidate()
				model.portScheme = value
				modelExchanged(model)
				invalidate()
				validate()
			}
		}

	var name: String?
		get() = model.name
		set(value) {
			if (value != name) {
				model.name = value
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

	override val controlId: String? get() = "7seg:" + model.id

	override fun bindControlView(subGraphVerticeView: SubGraphVerticeView<*>, model: SevenSegmentDisplay) {
		this.model = model
	}

	override fun sourcePropertiesChanged(source: ControlViewSource<SevenSegmentDisplay>) {
		if (source is SevenSegmentDisplayView) {
			copyControlViewProperties(source, this)
		}
	}

	override fun writeModelProperties(writer: StoreWriter) {
		if (StringUtils.isNotEmpty(name)) {
			writer.writeString("name", name!!)
		}
		writer.writeString("portScheme", portScheme.customName)
	}

	override fun readModelProperties(reader: StoreReader) {
		// conditional access in order to support backward compatibility
		if (reader.hasAttribute("name")) {
			name = reader.readString("name")
		}
		if (reader.hasAttribute("portScheme")) {
			portScheme = SevenSegmentDisplayScheme.withName(reader.readString("portScheme"))
		}
	}

	private fun copyControlViewProperties(source: SevenSegmentDisplayView, dest: SevenSegmentDisplayView) {
		dest.model.name = source.model.name
		dest.lightColor = source.lightColor
		dest.size = source.size
	}

	/** ---- [ControlViewSource] */

	override val controlName: String get() = super.controlName

	override val iconPath: String get() = BaseModule.properties.getString(PROP_ICON_PATH)

	override fun createControlView(): ControlView<SevenSegmentDisplay> {
		val clone = SevenSegmentDisplayView(styleProvider, model, lightColor)
		clone.isShowPortViews = false
		clone.location = Point2D(0, 0)
		return clone
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

		context.g.color = Themes.get<AntaresTheme>().screen.backgroundColor
		context.g.fillRect(0, 0, geom.width, geom.height)

		if (hasBorder) {
			context.g.stroke = stroke
			context.g.color = Themes.get<AntaresTheme>().screen.foregroundColor
			context.g.drawRect(0, 0, geom.width, geom.height)
		}

		drawHorizontalSegment(context.g, model.portScheme.inputValueOf(model, "a"),
			0.5f * geom.scaledFactor + geom.segHalfWidth,
			geom.scaledFactor + geom.segHalfWidth)

		drawVerticalSegment(context.g, model.portScheme.inputValueOf(model, "b"),
			0.5f * geom.scaledFactor + geom.segLength + geom.segHalfWidth,
			geom.scaledFactor + geom.segHalfWidth)

		drawVerticalSegment(context.g, model.portScheme.inputValueOf(model, "c"),
			0.5f * geom.scaledFactor + geom.segLength + geom.segHalfWidth,
			4 * geom.scaledFactor + geom.segHalfWidth)

		drawHorizontalSegment(context.g, model.portScheme.inputValueOf(model, "d"),
			0.5f * geom.scaledFactor + geom.segHalfWidth,
			7 * geom.scaledFactor + geom.segHalfWidth)

		drawVerticalSegment(context.g, model.portScheme.inputValueOf(model, "e"),
			0.5f * geom.scaledFactor + geom.segHalfWidth,
			4 * geom.scaledFactor + geom.segHalfWidth)

		drawVerticalSegment(context.g, model.portScheme.inputValueOf(model, "f"),
			0.5f * geom.scaledFactor + geom.segHalfWidth,
			1 * geom.scaledFactor + geom.segHalfWidth)

		drawHorizontalSegment(context.g, model.portScheme.inputValueOf(model, "g"),
			0.5f * geom.scaledFactor + geom.segHalfWidth,
			4 * geom.scaledFactor + geom.segHalfWidth)

		drawDot(context.g, model.portScheme.inputValueOf(model, "p"),
			0.5f * geom.scaledFactor + geom.segLength + geom.scaledFactor,
			7 * geom.scaledFactor + geom.segHalfWidth)

		if (model.inactive && context.castedAppContext<GraphApplicationContext>()!!.isExecute) {
			context.g.color = Look.inactiveColor
			context.g.fillRect(0, 0, geom.width, geom.height)
		}
	}

	/** ---- [SevenSegmentDisplayView] */

	private fun updateGeometry() {
		width = geom.width.toDouble()
		height = geom.height.toDouble()
		modelExchanged(model)
	}

	private class Geometry(val factor: Float) {
		val width: Int = (5 * factor * Look.SCALE).toInt()
		val height: Int = (8 * factor * Look.SCALE).toInt()
		val segLength: Float = 3 * factor * Look.SCALE
		val segHalfWidth: Float = 0.25f * factor * Look.SCALE
		val segInset: Float = 1f * factor
		val dotSize: Int = (4 * factor).toInt()
		val path = System.createPath()
			.moveTo(segInset, 0f)
			.lineTo(segInset + segHalfWidth, -segHalfWidth)
			.lineTo(segLength - segHalfWidth - segInset, -segHalfWidth)
			.lineTo(segLength - segInset, 0f)
			.lineTo(segLength - segHalfWidth - segInset, segHalfWidth)
			.lineTo(segInset + segHalfWidth, segHalfWidth)
			.close()
		val scaledFactor: Int get() = (factor * Look.SCALE).toInt()
	}

	override fun drawSelected(context: DrawContext) {
		draw(context) { c ->
			super.drawImpl(c)
			context.g.color = context.color!!.foregroundColor
			context.g.drawRect(0, 0, geom.width, geom.height)
		}
	}

	private fun drawHorizontalSegment(g: Graphics2D, value: Boolean, relX: Float, relY: Float) {
		g.translate(relX.toDouble(), relY.toDouble())
		g.color = getColor(value)
		g.fill(geom.path)
		g.translate(-relX.toDouble(), -relY.toDouble())
	}

	private fun drawVerticalSegment(g: Graphics2D, value: Boolean, relX: Float, relY: Float) {
		g.translate(relX.toDouble(), relY.toDouble())
		g.rotate(PI / 2)

		g.color = getColor(value)
		g.fill(geom.path)

		g.rotate(-PI / 2)
		g.translate(-relX.toDouble(), -relY.toDouble())
	}

	private fun drawDot(g: Graphics2D, value: Boolean, relX: Float, relY: Float) {
		g.translate(relX.toDouble(), relY.toDouble())
		g.color = getColor(value)
		g.fillOval(-geom.dotSize / 2, -geom.dotSize / 2, geom.dotSize, geom.dotSize)
		g.translate(-relX.toDouble(), -relY.toDouble())
	}

	private fun getColor(value: Boolean): Color {
		if (value) {
			return lightColor.onColor
		}
		return lightColor.offColor
	}
}

class SevenSegmentDisplayViewSelectionModel(c: SevenSegmentDisplayView) : AbstractSelectionModel<SevenSegmentDisplayView>(c) {

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