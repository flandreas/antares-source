package io.antarescircuit.antares.view.output

import io.antarescircuit.antares.model.output.LED
import io.antarescircuit.antares.view.output.LEDShape.*
import io.antarescircuit.antares.view.port.AbstractAntaresPortView
import io.antarescircuit.antares.view.port.DigitalPortView
import io.antarescircuit.antares.view.style.AntaresTheme
import io.antarescircuit.jabbah.base.StringUtils
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
import io.antarescircuit.jabbah.edit.Look
import io.antarescircuit.jabbah.edit.SelectionDrawingStrategy
import io.antarescircuit.jabbah.edit.model.AbstractComponent
import io.antarescircuit.jabbah.edit.model.Size
import io.antarescircuit.jabbah.edit.select.AbstractSelectionModel
import io.antarescircuit.jabbah.graph.GraphApplicationContext
import io.antarescircuit.jabbah.graph.model.Graph
import io.antarescircuit.jabbah.graph.model.Vertice
import io.antarescircuit.jabbah.graph.model.vertice.VerticeLink
import io.antarescircuit.jabbah.graph.view.ControlView
import io.antarescircuit.jabbah.graph.view.ControlViewSource
import io.antarescircuit.jabbah.graph.view.ControlViewSourceProperty
import io.antarescircuit.jabbah.graph.view.LabeledRectangularVerticeView
import io.antarescircuit.jabbah.graph.view.port.PortView
import io.antarescircuit.jabbah.graph.view.vertice.AbstractRectangularVerticeView
import io.antarescircuit.jabbah.graph.view.vertice.AbstractVerticeView
import io.antarescircuit.jabbah.graph.view.vertice.SubGraphVerticeView
import io.antarescircuit.jabbah.io.Storable
import io.antarescircuit.jabbah.io.StoreReader
import io.antarescircuit.jabbah.io.StoreWriter

/**
 * Base class for different LED view implementations.
 */
abstract class AbstractLEDView<T: Vertice>(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	model: T,
	ledShape: LEDShape = Circle,
	eventBus: EventBus = BaseModule.eventBus
) : LabeledRectangularVerticeView<T>(styleProvider, model, eventBus = eventBus),
	ControlView<T>,
	ControlViewSource<T>
{
	companion object {
		private val DEFAULT_SIZE = Size.LARGE
		private const val DEFAULT_HAS_BORDER = true
	}

	/** Determines the shape in which the LED is drawn. Default is circular.*/
	var ledShape: LEDShape by ControlViewSourceProperty(ledShape, eventBus, ::updateGeometry)

	var size: Size by ControlViewSourceProperty(DEFAULT_SIZE, eventBus, ::updateGeometry)

	@Suppress("MemberVisibilityCanBePrivate") // Reflection
	var hasBorder: Boolean by ControlViewSourceProperty(DEFAULT_HAS_BORDER, eventBus)

	private val widthOfShape: Int get() = when (ledShape) {
        Circle -> Look.SCALE
        Square -> Look.SCALE
        Striped -> 3 * Look.SCALE
    }

	private val heightOfShape: Int get() = when (ledShape) {
        Circle -> Look.SCALE
        Square -> Look.SCALE
        Striped -> Look.SCALE / 2
    }

	private val widthOfSize: Int get() = when (size) {
		Size.SMALL -> 2 * widthOfShape
		Size.MEDIUM -> 3 * widthOfShape
		Size.LARGE -> 4 * widthOfShape
	}

	private val heightOfSize: Int get() = when (size) {
		Size.SMALL -> 2 * heightOfShape
		Size.MEDIUM -> 3 * heightOfShape
		Size.LARGE -> 4 * heightOfShape
	}

	private val borderOfSize: Int get() {
		return if (!hasBorder) {
			0
		} else when (size) {
			Size.SMALL -> 2
			Size.MEDIUM -> 2
			Size.LARGE -> 3
		}
	}

	init {
		initExternalLabel()
		modelExchanged(null)
		updateGeometry()
	}

	override val relativeExternalLabelLocation: Point2D get() = Point2D(widthOfSize + AbstractAntaresPortView.LENGTH + LABEL_DIST, 0)

	override fun modelExchanged(oldModel: T?) {
		super.modelExchanged(oldModel)
		createPortView().let {
            it.setLocation(it.unconnectedLength, 0)
            addPortView(it)
        }
	}

	override fun updateGeometry() {
		setBounds(getInput().unconnectedLength, -heightOfSize / 2, widthOfSize, heightOfSize)
		super.updateGeometry()
	}

	protected open fun createPortView(): PortView<*> =
		DigitalPortView(
			styleProvider = styleProvider,
			port = model.getInput(),
			direction = Direction.WEST)

	/** ---- [Storable] interface */

	override fun write(writer: StoreWriter) {
		super.write(writer)
		writer.writeString("shape", ledShape.customName)
		writer.writeString("size", size.customName)
		if (!hasBorder) {
			writer.writeBoolean("hasBorder", hasBorder)
		}
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		if (reader.hasAttribute("square")) {
			// backward compatibility
			ledShape = if (reader.readBoolean("square")) {
				Square
			} else {
				Circle
			}
		} else if (reader.hasAttribute("shape")) {
			ledShape = LEDShape.withName(reader.readString("shape"))
		}
		if (reader.hasAttribute("size")) {
			size = Size.withName(reader.readString("size"))
		}
		if (reader.hasAttribute("hasBorder")) {
			hasBorder = reader.readBoolean("hasBorder")
		}
	}

	/** ---- [ControlView] */

	override var isActiveControlView: Boolean = false

	override val mirrorWidth: Double get() = 2 * AbstractAntaresPortView.LENGTH + width

	override fun sourcePropertiesChanged(source: ControlViewSource<T>) {
		if (source is AbstractLEDView) {
			copyControlViewProperties(source, this)
		}
	}

	override fun bindControlView(subGraphVerticeView: SubGraphVerticeView<*>, link: VerticeLink, startGraph: Graph) {
		@Suppress("UNCHECKED_CAST")
		this.model = link.getLinkedObject(startGraph) as T
	}

	override fun writeModelProperties(writer: StoreWriter) {
		if (StringUtils.isNotEmpty(name)) {
			writer.writeString("name", name!!)
		}
	}

	override fun readModelProperties(reader: StoreReader) {
		if (reader.hasAttribute("name")) {
			name = reader.readString("name")
		}
	}

	protected open fun copyControlViewProperties(source: AbstractLEDView<*>, dest: AbstractLEDView<*>) {
		dest.name = source.name
		dest.ledShape = source.ledShape
		dest.size = source.size
		dest.hasBorder = source.hasBorder
	}

	/** ---- [AbstractComponent] */

	override var preferredSelectionDrawingStrategy: SelectionDrawingStrategy?
		get() = SelectionDrawingStrategy.REPLACE
		set(value) { super.preferredSelectionDrawingStrategy = value }

	/** ---- [AbstractVerticeView] */

	override fun drawImpl(context: DrawContext) {
		super.drawImpl(context)
		drawBody(context)
	}

	/** ---- [AbstractRectangularVerticeView] */

	override fun drawSelected(context: DrawContext) {
		context.g.color = context.color!!.foregroundColor
		draw(context) { c ->
			super.drawImpl(c)
			context.g.stroke = stroke
			if (ledShape.oval) {
				context.g.drawOval(xInt, yInt, widthOfSize, heightOfSize)
			} else {
				context.g.drawRect(xInt, yInt, widthOfSize, heightOfSize)
			}
			context.g.stroke = Themes.get<AntaresTheme>().annotation.stroke
			if (ledShape.oval) {
				context.g.drawOval(
					xInt + borderOfSize, yInt + borderOfSize,
					widthOfSize - 2 * borderOfSize, heightOfSize - 2 * borderOfSize)
			} else {
				context.g.drawRect(
					xInt + borderOfSize, yInt + borderOfSize,
					widthOfSize - 2 * borderOfSize, heightOfSize - 2 * borderOfSize)
			}
		}
		super.drawSelected(context)
	}

	/** ---- [AbstractLEDView] */

	/** Returns the [Color] to be used for drawing the bulb of this [LED] when executing.*/
	protected abstract fun getBulbExecuteColor(): Color

	protected open fun getBulbEditColor(): Color = backgroundColor

	/** Draws the bulb in the color returned by [getBulbExecuteColor].*/
	private fun drawBulb(context: DrawContext) {
		if (context.castedAppContext<GraphApplicationContext>()!!.isExecute) {
			drawBulb(context, transparent.applyTo(getBulbExecuteColor()))
		} else {
			drawBulbEdited(context)
		}
	}

	protected open fun drawBulbEdited(context: DrawContext) {
		drawBulb(context, context.chooseBackground(getBulbEditColor()))
	}

	/** Draws the bulb using the specified [Color].*/
	protected fun drawBulb(context: DrawContext, color: Color) {
		context.g.color = color
		if (ledShape.oval) {
			context.g.fillOval(xInt + borderOfSize, yInt + borderOfSize,
				widthOfSize - 2 * borderOfSize, heightOfSize - 2 * borderOfSize)
		} else {
			context.g.fillRect(xInt + borderOfSize, yInt + borderOfSize,
				widthOfSize - 2 * borderOfSize, heightOfSize - 2 * borderOfSize)
		}
	}

	private fun drawBody(context: DrawContext) {
		if (shadow) {
			DropShadow.draw(context, transparency) {
				if (ledShape.oval) {
					context.g.fillOval(xInt, yInt, widthOfSize, heightOfSize)
				} else {
					context.g.fillRect(xInt, yInt, widthOfSize, heightOfSize)
				}
			}
		}
		context.g.color = Themes.get<AntaresTheme>().screen.foregroundColor
		context.g.stroke = stroke
		if (ledShape.oval) {
			context.g.fillOval(xInt, yInt, widthOfSize, heightOfSize)
		} else {
			context.g.fillRect(xInt, yInt, widthOfSize, heightOfSize)
		}
		drawBulb(context)

		if (model.inactive && context.castedAppContext<GraphApplicationContext>()!!.isExecute) {
			context.g.color = Look.inactiveColor
			if (ledShape.oval) {
				context.g.fillOval(xInt, yInt, widthOfSize, heightOfSize)
			} else {
				context.g.fillRect(xInt, yInt, widthOfSize, heightOfSize)
			}
		}
	}
}

class LEDViewSelectionModel(c: AbstractLEDView<*>) : AbstractSelectionModel<AbstractLEDView<*>>(c) {

	override fun draw(context: DrawContext) {
		val oldUseContextColors = context.useContextColors
		context.useContextColors = true
		context.color = Themes.get<AntaresTheme>().selection.color
		component.drawSelected(context)
		context.useContextColors = oldUseContextColors
	}

	override val boundingBox: RectangularShape get() = component.boundingBox

	override fun contains(x: Double, y: Double): Boolean = component.contains(x, y)

	override fun componentUpdated() {
		validate()
	}
}