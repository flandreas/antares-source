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
import ch.scorpion.jabbah.edit.model.text.HorizontalLabel
import ch.scorpion.jabbah.edit.select.AbstractSelectionModel
import ch.scorpion.jabbah.graph.model.Vertice
import ch.scorpion.jabbah.graph.view.ControlView
import ch.scorpion.jabbah.graph.view.ControlViewSource
import ch.scorpion.jabbah.graph.view.vertice.AbstractVerticeView
import ch.scorpion.jabbah.graph.view.vertice.AbstractRectangularVerticeView
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

/** Base class for different LED view implementations.*/
abstract class AbstractLEDView<T: Vertice>(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	model: T,
	private val eventBus: EventBus = BaseModule.eventBus
) : DigitalComponentView<T>(styleProvider, model), ControlView<T>, ControlViewSource<T> {

	companion object {
		protected const val SIZE = 4 * Look.SCALE
		const val BORDER_WIDTH = 3
		const val LABEL_DIST = Look.SCALE
	}

	private val label = HorizontalLabel(
		owner = this,
		relLocation = Point2D(SIZE + DigitalPortView.LENGTH + LABEL_DIST, 0),
		font = font)

	var name: String?
		get() = model!!.name
		set(value) {
			if (value != name) {
				model!!.name = value
				updateLabel()
				postControlViewSourceChangeEvent(eventBus)
			}
		}

	init {
		modelExchanged(null)
		setBounds(getInput().unconnectedLength, -SIZE / 2, SIZE, SIZE)
	}

	override fun modelExchanged(oldModel: T?) {
		super.modelExchanged(oldModel)
		val portView = DigitalPortView(
			styleProvider = styleProvider,
			port = model!!.getInput(),
			direction = Direction.WEST)
		portView.setLocation(portView.unconnectedLength, 0)
		addPortView(portView)
		updateLabel()
	}

	/** ---- [ControlView] */

	override fun sourcePropertiesChanged(source: ControlViewSource<T>) {
		if (source is AbstractLEDView) {
			name = source.name
		}
	}

	override fun bindToModel(model: T) {
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

	/** ---- [ControlViewSource] */

	override val controlName: String
		get() {
			if (StringUtils.isEmpty(model!!.name)) {
				return "$type ($id)"
			}
			return "$type \"${model!!.name}\""
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
		context.g.color = styleProvider.getStyle(StyleType.BACKGROUND).color.textColor
		label.draw(context)
	}

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
			context.g.drawOval(xInt, yInt, SIZE, SIZE)
			context.g.stroke = Themes.get<AntaresTheme>().annotation.stroke
			context.g.drawOval(
				xInt + BORDER_WIDTH, yInt + BORDER_WIDTH,
				SIZE - 2 * BORDER_WIDTH, SIZE - 2 * BORDER_WIDTH)
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
		context.g.fillOval(xInt + BORDER_WIDTH, yInt + BORDER_WIDTH,
			SIZE - 2 * BORDER_WIDTH, SIZE - 2 * BORDER_WIDTH)
		context.g.drawOval(xInt + BORDER_WIDTH, yInt + BORDER_WIDTH,
			SIZE - 2 * BORDER_WIDTH, SIZE - 2 * BORDER_WIDTH)
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
				context.g.fillOval(xInt, yInt, SIZE, SIZE)
			}
		}
		context.g.color = Themes.get<AntaresTheme>().screen.foregroundColor
		context.g.stroke = stroke
		context.g.fillOval(xInt, yInt, SIZE, SIZE)
		drawBulb(context)
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