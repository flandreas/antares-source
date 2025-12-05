package ch.scorpion.antares.view.net

import ch.scorpion.antares.model.net.Constant
import ch.scorpion.antares.model.port.DigitalPort
import ch.scorpion.antares.model.signal.*
import ch.scorpion.antares.view.OrientableRectangularVerticeView
import ch.scorpion.antares.view.port.AbstractAntaresPortView
import ch.scorpion.antares.view.port.DigitalPortView
import ch.scorpion.jabbah.base.LongValue
import ch.scorpion.jabbah.base.PreferencesChangedEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.drawable.RotationDirection
import ch.scorpion.jabbah.draw.graphics.DropShadow
import ch.scorpion.jabbah.draw.graphics.Stroke
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.DrawTheme
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.style.Themes
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.model.text.Alignment
import ch.scorpion.jabbah.edit.model.text.HorizontalAlignment
import ch.scorpion.jabbah.edit.model.text.Label
import ch.scorpion.jabbah.edit.model.text.VerticalAlignment
import ch.scorpion.jabbah.graph.view.vertice.AbstractVerticeView
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

class ConstantView(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	model: Constant = Constant(),
	private val eventBus: EventBus = BaseModule.eventBus
) : OrientableRectangularVerticeView<Constant>(styleProvider, model), DigitalSignalRepresenter {

	companion object {
		private const val VERTICAL_INSET = 2
		private const val HORIZONTAL_INSET = 5
		private val BORDER_STROKE = Stroke(width = 0.5f)
	}

	constructor(
		value: LongValue,
		styleProvider: StyleProvider = DrawStyleModule.styleProvider
	) : this(styleProvider, Constant(value))

	override var orientation: Direction = Direction.EAST
		set(value) {
			if (value != field) {
				field = value
				updateView()
			}
		}

	override var signalRepresentation: DigitalSignalRepresentation = DigitalSignalRepresentation.BINARY
		set(value) {
			if (value != field) {
				field = value
				(model.getPort<DigitalSignal>() as DigitalPort).signalRepresentation = value
				updateView()
			}
		}

	private val label = Label(
		text = "",
		font = font,
		horizontalAlignment = HorizontalAlignment.RIGHT,
		verticalAlignment = VerticalAlignment.CENTER,
		location = Point2D(-AbstractAntaresPortView.LENGTH - HORIZONTAL_INSET, 0)
	)

	private val preferencesChangedHandler: EventHandler<PreferencesChangedEvent> = {
		updateView()
	}

	init {
		modelExchanged(null)
		eventBus.register(PreferencesChangedEvent::class, preferencesChangedHandler)
	}

	override fun dispose() {
		eventBus.unregister(preferencesChangedHandler)
	}

	/** ---- [AbstractVerticeView] */

	override fun modelExchanged(oldModel: Constant?) {
		super.modelExchanged(oldModel)
		val portView = DigitalPortView(
			styleProvider = styleProvider,
			port = model.getOutput(),
			direction = Direction.EAST)
		portView.setLocation(-AbstractAntaresPortView.LENGTH, 0)
		addPortView(portView)
		(model.getPort<DigitalSignal>() as DigitalPort).signalRepresentation = signalRepresentation
		updateView()
	}

	override fun drawImpl(context: DrawContext) {
		super.drawImpl(context)

		if (shadow) {
			DropShadow.draw(context, transparency) {
				context.g.fillRect(xInt, yInt, widthInt, heightInt)
			}
		}

		// Draw background
		val color = Themes.get<DrawTheme>().background.color.deriveBackgroundTowardsTextColor()
		context.g.color = transparent.applyTo(context.choose(color).backgroundColor)
		context.g.fillRect(xInt, yInt, widthInt, heightInt)
		context.g.stroke = BORDER_STROKE
		context.g.color = transparent.applyTo(context.choose(color).foregroundColor)
		context.g.drawRect(xInt, yInt, widthInt, heightInt)

		label.color = transparent.applyTo(Themes.get<DrawTheme>().figure.color.textColor)
		label.draw(context)
	}

	/** ---- UI properties */

	var value: LongValue
		get() = model.value
		set(newValue) {
			invalidate()
			model.value = newValue
			updateView()
		}

	var bitWidth: BitWidth
		get() = model.bitWidth
		set(value) {
			invalidate()
			model.bitWidth = value
			updateView()
		}

	/** ---- [Storable] interface */

	override fun write(writer: StoreWriter) {
		super.write(writer)
		writer.writeString("representation", signalRepresentation.customName)
		writer.writeString("orientation", orientation.customName)
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		signalRepresentation = DigitalSignalRepresentation.withName(reader.readString("representation"))
		if (reader.hasAttribute("orientation")) {
			orientation = Direction.withName(reader.readString("orientation"))
		}
	}

	/** ---- [Component] */

	override fun rotate(direction: RotationDirection, pivot: Point2D?) {
		orientation = when (direction) {
			RotationDirection.Clockwise -> Direction.of(orientation.rotation.previous())
			RotationDirection.CounterClockwise -> Direction.of(orientation.rotation.next())
		}
		pivot?.let {
			location = direction.rotation.rotatePointAround(it, location)
		}
	}

	/** ---- [ConstantView] */

	private val upperLeftBoundsEdge: Point2D
		get() {
			val labelBounds = label.boundingBox
			return when (orientation) {
				Direction.EAST -> Point2D(
					-AbstractAntaresPortView.LENGTH - 2 * HORIZONTAL_INSET - labelBounds.width,
					-labelBounds.height / 2 - VERTICAL_INSET)
				Direction.NORTH -> Point2D(
					-labelBounds.width / 2 - HORIZONTAL_INSET,
					AbstractAntaresPortView.LENGTH.toDouble())
				Direction.WEST -> Point2D(
					AbstractAntaresPortView.LENGTH.toDouble(),
					-labelBounds.height / 2 - VERTICAL_INSET)
				Direction.SOUTH -> Point2D(
					-labelBounds.width / 2 - HORIZONTAL_INSET,
					-AbstractAntaresPortView.LENGTH - labelBounds.height - 2 * VERTICAL_INSET)
			}
		}

	private val labelLocation: Point2D
		get() = when (orientation) {
			Direction.EAST -> Point2D(-AbstractAntaresPortView.LENGTH - HORIZONTAL_INSET, 1)
			Direction.NORTH -> Point2D(0, AbstractAntaresPortView.LENGTH + VERTICAL_INSET)
			Direction.WEST -> Point2D(AbstractAntaresPortView.LENGTH + HORIZONTAL_INSET, 1)
			Direction.SOUTH -> Point2D(0, -AbstractAntaresPortView.LENGTH - VERTICAL_INSET)
		}

	private fun updateView() {
		updateLabel()

		val labelBounds = label.boundingBox
		val orig = upperLeftBoundsEdge

		setBounds(
			orig.x, orig.y,
			2 * HORIZONTAL_INSET + labelBounds.width, 2 * VERTICAL_INSET + labelBounds.height)

		getOutput().direction = orientation
		getOutput().setLocation(
			getOutput().unconnectedLength * -getOutput().direction.dx,
			getOutput().unconnectedLength * -getOutput().direction.dy)

		invalidate()
		update()
		validate()
	}

	private fun updateLabel() {
		label.text = CurrentDigitalSignalNotation.notation.notate(model.valueSignal, signalRepresentation)
		label.alignment = Alignment.forOrientation(orientation)
		label.location = labelLocation
	}
}