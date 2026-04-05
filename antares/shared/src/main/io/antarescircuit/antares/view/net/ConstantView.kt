package io.antarescircuit.antares.view.net

import io.antarescircuit.antares.model.net.Constant
import io.antarescircuit.antares.model.port.DigitalPort
import io.antarescircuit.antares.model.signal.*
import io.antarescircuit.jabbah.graph.view.OrientableRectangularVerticeView
import io.antarescircuit.antares.view.port.AbstractAntaresPortView
import io.antarescircuit.antares.view.port.DigitalPortView
import io.antarescircuit.jabbah.base.LongValue
import io.antarescircuit.jabbah.base.PreferencesChangedEvent
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.event.EventHandler
import io.antarescircuit.jabbah.base.geom.Direction
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.drawable.RotationDirection
import io.antarescircuit.jabbah.draw.graphics.DropShadow
import io.antarescircuit.jabbah.draw.graphics.Stroke
import io.antarescircuit.jabbah.draw.style.DrawStyleModule
import io.antarescircuit.jabbah.draw.style.DrawTheme
import io.antarescircuit.jabbah.draw.style.StyleProvider
import io.antarescircuit.jabbah.draw.style.Themes
import io.antarescircuit.jabbah.edit.Component
import io.antarescircuit.jabbah.edit.model.text.Alignment
import io.antarescircuit.jabbah.edit.model.text.HorizontalAlignment
import io.antarescircuit.jabbah.edit.model.text.Label
import io.antarescircuit.jabbah.edit.model.text.VerticalAlignment
import io.antarescircuit.jabbah.graph.view.vertice.AbstractVerticeView
import io.antarescircuit.jabbah.io.Storable
import io.antarescircuit.jabbah.io.StoreReader
import io.antarescircuit.jabbah.io.StoreWriter

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
		context.g.color = transparent.applyTo(context.chooseBackground(backgroundColor))
		context.g.fillRect(xInt, yInt, widthInt, heightInt)
		context.g.stroke = BORDER_STROKE
		context.g.color = transparent.applyTo(context.chooseForeground(foregroundColor))
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