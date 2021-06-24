package ch.scorpion.antares.view.output

import ch.scorpion.antares.model.output.LED
import ch.scorpion.antares.view.DigitalComponentView
import ch.scorpion.antares.view.Look
import ch.scorpion.antares.view.port.DigitalPortView
import ch.scorpion.antares.view.style.AntaresTheme
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.geom.*
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.drawable.AbstractDrawable
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.DropShadow
import ch.scorpion.jabbah.draw.style.*
import ch.scorpion.jabbah.edit.SelectionDrawingStrategy
import ch.scorpion.jabbah.edit.model.AbstractComponent
import ch.scorpion.jabbah.edit.model.Size
import ch.scorpion.jabbah.edit.model.text.HorizontalLabel
import ch.scorpion.jabbah.edit.select.AbstractSelectionModel
import ch.scorpion.jabbah.graph.GraphApplicationContext
import ch.scorpion.jabbah.graph.model.Vertice
import ch.scorpion.jabbah.graph.view.ControlView
import ch.scorpion.jabbah.graph.view.ControlViewSource
import ch.scorpion.jabbah.graph.view.vertice.AbstractVerticeView
import ch.scorpion.jabbah.graph.view.vertice.AbstractRectangularVerticeView
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

/** Base class for different LED view implementations.*/
abstract class AbstractLEDView<T: Vertice>(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	model: T,
	square: Boolean = false,
	private val eventBus: EventBus = BaseModule.eventBus
) : DigitalComponentView<T>(styleProvider, model), ControlView<T>, ControlViewSource<T> {

	companion object {
		const val LABEL_DIST = Look.SCALE
		private val DEFAULT_SIZE = Size.LARGE
		private const val DEFAULT_HAS_BORDER = true
	}

	var name: String?
		get() = model.name
		set(value) {
			if (value != name) {
				model.name = value
				updateLabel()
				postControlViewSourceChangeEvent(eventBus)
			}
		}

	/** Determines the shape in which the LED is drawn. Default is circular.*/
	var square: Boolean = square
		set(value) {
			if (field != value) {
				invalidate()
				field = value
				invalidate()
				validate()
			}
		}

	var size: Size = DEFAULT_SIZE
		set(value) {
			if (field != value) {
				invalidate()
				field = value
				updateGeometry()
				validate()
				postControlViewSourceChangeEvent(eventBus)
			}
		}

	var hasBorder: Boolean = DEFAULT_HAS_BORDER
		set(value) {
			if (field != value) {
				invalidate()
				field = value
				invalidate()
				validate()
				postControlViewSourceChangeEvent(eventBus)
			}
		}

	private val widthOfSize: Int get() = when (size) {
		Size.SMALL -> 2 * Look.SCALE
		Size.MEDIUM -> 3 * Look.SCALE
		Size.LARGE -> 4 * Look.SCALE
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

	private val label = HorizontalLabel(
		owner = this,
		relLocation = Point2D(widthOfSize + DigitalPortView.LENGTH + LABEL_DIST, 0),
		font = font)

	private fun updateGeometry() {
		setBounds(getInput().unconnectedLength, -widthOfSize / 2, widthOfSize, widthOfSize)
		label.relLocation = Point2D(widthOfSize + DigitalPortView.LENGTH + LABEL_DIST, 0)
	}

	init {
		modelExchanged(null)
		//setBounds(getInput().unconnectedLength, -widthOfSize / 2, widthOfSize, widthOfSize)
		updateGeometry()
	}

	override fun modelExchanged(oldModel: T?) {
		super.modelExchanged(oldModel)
		val portView = DigitalPortView(
			styleProvider = styleProvider,
			port = model.getInput(),
			direction = Direction.WEST)
		portView.setLocation(portView.unconnectedLength, 0)
		addPortView(portView)
		updateLabel()
	}

	/** ---- [Storable] interface */

	override fun write(writer: StoreWriter) {
		super.write(writer)
		if (square) {
			writer.writeBoolean("square", square)
		}
		writer.writeString("size", size.customName)
		if (!hasBorder) {
			writer.writeBoolean("hasBorder", hasBorder)
		}
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		if (reader.hasAttribute("square")) {
			square = reader.readBoolean("square")
		}
		if (reader.hasAttribute("size")) {
			size = Size.withName(reader.readString("size"))
		}
		if (reader.hasAttribute("hasBorder")) {
			hasBorder = reader.readBoolean("hasBorder")
		}
	}

	/** ---- [ControlView] */

	override fun sourcePropertiesChanged(source: ControlViewSource<T>) {
		if (source is AbstractLEDView) {
			copyControlViewProperties(source, this)
		}
	}

	override fun bindControlView(subGraphVerticeView: SubGraphVerticeView<*>, model: T) {
		this.model = model
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

	override val controlName: String get() = super.controlName

	protected fun copyControlViewProperties(source: AbstractLEDView<*>, dest: AbstractLEDView<*>) {
		dest.name = source.name
		dest.square = source.square
		dest.size = source.size
		dest.hasBorder = source.hasBorder
	}

	/** ---- [AbstractComponent] */

	override var preferredSelectionDrawingStrategy: SelectionDrawingStrategy?
		get() = SelectionDrawingStrategy.REPLACE
		set(value) {super.preferredSelectionDrawingStrategy = value}

	override fun rotationChanged(newRotation: Rotation) {
		super.rotationChanged(newRotation)
		label.rotationChanged()
	}

	/** ---- [AbstractDrawable] */

	override val boundingBox: Rectangle2D
		get() {
			val bb = Rectangle2D(super.boundingBox)
			val lbb = label.boundingBox.moveBy(location)
			bb.add(lbb)
			return bb
		}

	/** ---- [AbstractVerticeView] */

	override fun draw(context: DrawContext) {
		super.draw(context)
		drawLabel(context)
	}

	override fun drawImpl(context: DrawContext) {
		super.drawImpl(context)
		drawBody(context)
	}

	private fun drawLabel(context: DrawContext) {
		context.g.color = styleProvider.getStyle(StyleType.BACKGROUND).color.textColor
		label.draw(context)
	}

	/** ---- [AbstractRectangularVerticeView] */

	override fun drawSelected(context: DrawContext) {
		context.g.color = context.color!!.foregroundColor
		draw(context) { c ->
			super.drawImpl(c)
			context.g.stroke = stroke
			if (square) {
				context.g.drawRect(xInt, yInt, widthOfSize, widthOfSize)
			} else {
				context.g.drawOval(xInt, yInt, widthOfSize, widthOfSize)
			}
			context.g.stroke = Themes.get<AntaresTheme>().annotation.stroke
			if (square) {
				context.g.drawRect(
					xInt + borderOfSize, yInt + borderOfSize,
					widthOfSize - 2 * borderOfSize, widthOfSize - 2 * borderOfSize)
			} else {
				context.g.drawOval(
					xInt + borderOfSize, yInt + borderOfSize,
					widthOfSize - 2 * borderOfSize, widthOfSize - 2 * borderOfSize)
			}
		}
		label.draw(context)
	}

	/** ---- [AbstractLEDView] */

	/** Returns the [Color] to be used for drawing the bulb of this [LED].*/
	protected abstract fun getBulbColor(): Color

	/** Draws the bulb in the color returned by [getBulbColor].*/
	protected open fun drawBulb(context: DrawContext) {
		drawBulb(context, getBulbColor())
	}

	/** Draws the bulb using the specified [Color].*/
	protected fun drawBulb(context: DrawContext, color: Color) {
		context.g.color = color
		if (square) {
			context.g.fillRect(xInt + borderOfSize, yInt + borderOfSize,
				widthOfSize - 2 * borderOfSize, widthOfSize - 2 * borderOfSize)
			context.g.drawRect(xInt + borderOfSize, yInt + borderOfSize,
				widthOfSize - 2 * borderOfSize, widthOfSize - 2 * borderOfSize)
		} else {
			context.g.fillOval(xInt + borderOfSize, yInt + borderOfSize,
				widthOfSize - 2 * borderOfSize, widthOfSize - 2 * borderOfSize)
			context.g.drawOval(xInt + borderOfSize, yInt + borderOfSize,
				widthOfSize - 2 * borderOfSize, widthOfSize - 2 * borderOfSize)
		}
	}

	private fun updateLabel() {
		invalidate()
		label.text = StringUtils.orEmpty(name)
		label.rotationChanged()
		invalidate()
		update()
	}

	private fun drawBody(context: DrawContext) {
		if (shadow) {
			DropShadow.draw(context, transparency) {
				if (square) {
					context.g.fillRect(xInt, yInt, widthOfSize, widthOfSize)
				} else {
					context.g.fillOval(xInt, yInt, widthOfSize, widthOfSize)
				}
			}
		}
		context.g.color = Themes.get<AntaresTheme>().screen.foregroundColor
		context.g.stroke = stroke
		if (square) {
			context.g.fillRect(xInt, yInt, widthOfSize, widthOfSize)
		} else {
			context.g.fillOval(xInt, yInt, widthOfSize, widthOfSize)
		}
		drawBulb(context)

		if (model.inactive && context.castedAppContext<GraphApplicationContext>()!!.isExecute) {
			context.g.color = Look.inactiveColor
			if (square) {
				context.g.fillRect(xInt, yInt, widthOfSize, widthOfSize)
			} else {
				context.g.fillOval(xInt, yInt, widthOfSize, widthOfSize)
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

	override fun contains(x: Double, y: Double): Boolean {
		return component.contains(x, y)
	}

	override fun componentUpdated() {
		validate()
	}
}