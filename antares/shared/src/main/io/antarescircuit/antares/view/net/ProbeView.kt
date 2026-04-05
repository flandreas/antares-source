package io.antarescircuit.antares.view.net

import io.antarescircuit.antares.model.net.Probe
import io.antarescircuit.antares.model.signal.*
import io.antarescircuit.jabbah.edit.Look
import io.antarescircuit.antares.view.port.AbstractAntaresPortView
import io.antarescircuit.antares.view.port.DigitalPortView
import io.antarescircuit.antares.view.signal.AbstractNumberViewComponent
import io.antarescircuit.antares.view.signal.DigitalSignalSourceControlView
import io.antarescircuit.jabbah.base.StringUtils
import io.antarescircuit.jabbah.base.System
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.geom.Direction
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.geom.Rectangle2D
import io.antarescircuit.jabbah.base.geom.RectangularShape
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.drawable.AbstractDrawable
import io.antarescircuit.jabbah.draw.drawable.RotationDirection
import io.antarescircuit.jabbah.draw.graphics.Color
import io.antarescircuit.jabbah.draw.graphics.DropShadow
import io.antarescircuit.jabbah.draw.style.DrawStyleModule
import io.antarescircuit.jabbah.draw.style.StyleProvider
import io.antarescircuit.jabbah.draw.style.StyleType
import io.antarescircuit.jabbah.edit.Component
import io.antarescircuit.jabbah.edit.model.text.*
import io.antarescircuit.jabbah.graph.GraphApplicationContext
import io.antarescircuit.jabbah.graph.model.GraphElementEvent
import io.antarescircuit.jabbah.graph.view.ControlView
import io.antarescircuit.jabbah.graph.view.ControlViewSource


/**
 * A view of a [Probe].
 */
class ProbeView(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	probe: Probe = Probe(),
	private val eventBus: EventBus = BaseModule.eventBus
) : AbstractNumberViewComponent<Probe>(styleProvider, probe, Direction.EAST), ControlViewSource<Probe>, Labeled {

	companion object {
		const val PROP_ICON_PATH = "io.antarescircuit.antares.view.net.ProbeView.iconPath"
		private const val LABEL_DIST = Look.SCALE
		private val TRIANGLE_PATH = System.createPath()
			.moveTo(0, 0)
			.lineTo(0, 5)
			.lineTo(8, 0)
			.lineTo(0, -5)
			.close()
	}

	override val label = Label(
		font = font,
		text = probe.name)

	init {
		modelExchanged(null)
	}

	override fun modelExchanged(oldModel: Probe?) {
		super.modelExchanged(oldModel)
		createInnerView()
		updatePortViews()
		updateView()
		updateLabel()
	}

	private fun updatePortViews() {
		clearPortViews()

		val inputPortView = DigitalPortView(
			styleProvider = styleProvider,
			port = model.getInput(),
			direction = Direction.WEST)
		addPortView(inputPortView)

		if (hasOutput) {
			val outputPort = DigitalPortView(
				styleProvider = styleProvider,
				port = model.getOutput(),
				direction = Direction.EAST)
			addPortView(outputPort)
		}
	}

	override fun handleStateChanged(event: GraphElementEvent) {
		invalidate()
		if (event.signalHandler == null) {
			if (model.bitWidth != numberView?.bitWidth) {
				updateBitWidth(model.bitWidth)
			}
			updateBoxes()
			updateLabel()
		}
		super.handleStateChanged(event)
	}

	/** ---- UI properties */

	@Suppress("MemberVisibilityCanBePrivate") // Reflection
	var hasOutput: Boolean
		get() = model.hasOutput
		set(value) {
			if (value == hasOutput) {
				return
			}

			invalidate()
			model.hasOutput = value
			createInnerView()
			updatePortViews()
			updateView()
		}

	var name: String?
		get() = model.name
		set(value) {
			if (model.name != value) {
				model.name = value
				postControlViewSourceChangeEvent(eventBus)
			}
		}

	@Suppress("MemberVisibilityCanBePrivate", "unused") // Reflection
	var isLogging: Boolean
		get() = model.isLogging
		set(value) {
			if (value == isLogging) {
				return
			}
			model.isLogging = value
		}

	@Suppress("MemberVisibilityCanBePrivate", "unused") // Reflection
	var fixedPointFractionSize: Int?
		get() = model.fixedPointFractionSize
		set(value) {
			if (model.fixedPointFractionSize != value) {
				invalidate()
				model.fixedPointFractionSize = value
				createInnerView()
				updateView()
				invalidate()
				update()
			}
		}

	@Suppress("MemberVisibilityCanBePrivate", "unused") // Reflection
	var fixedPointSigned: Boolean?
		get() = model.fixedPointSigned
		set(value) {
			if (model.fixedPointSigned != value) {
				invalidate()
				model.fixedPointSigned = value
				createInnerView()
				updateView()
				invalidate()
				update()
			}
		}

	/** ---- [AbstractDrawable] */

	override val boundingBox: RectangularShape
		get() {
			val bb = Rectangle2D(super.boundingBox)
			val lbb = Rectangle2D(label.boundingBox).moveBy(location)
			bb.add(lbb)
			return bb
		}

	/** ---- [Component] */

	override val useRotation: Boolean get() = true

	override fun rotate(direction: RotationDirection, pivot: Point2D?) {
		orientation = when (direction) {
			RotationDirection.Clockwise -> Direction.of(orientation.rotation.previous())
			RotationDirection.CounterClockwise -> Direction.of(orientation.rotation.next())
		}
		pivot?.let {
			location = direction.rotation.rotatePointAround(it, location)
		}
	}

	/** ---- [AbstractNumberViewComponent] */

	override var bitWidth: BitWidth
		get() = model.bitWidth
		set(value) {
			if (value != bitWidth) {
				updateBitWidth(value)
			}
		}

	private fun updateBitWidth(newBitWidth: BitWidth) {
		clear()
		model.bitWidth = newBitWidth
		createInnerView()
		updateView()
		postControlViewSourceChangeEvent(eventBus)
	}

	override var signalRepresentation: DigitalSignalRepresentation
		get() = super.signalRepresentation
		set(value) {
			if (super.signalRepresentation != value) {
				if (value == DigitalSignalRepresentation.FIXED_POINT) {
					if (model.fixedPointConfig == null) {
						model.fixedPointConfig = FixedPointConfig(0, false)
					}
				}
				super.signalRepresentation = value
				postControlViewSourceChangeEvent(eventBus)
			}
		}

	override val signal: DigitalSignal get() = model.signal!!

	override val upperLeftBoundsEdge: Point2D
		get() = when (orientation) {
			Direction.EAST -> Point2D(AbstractAntaresPortView.LENGTH.toDouble(), -innerBounds.height / 2 - insets)
			Direction.NORTH -> Point2D(-innerBounds.width / 2 - insets, -AbstractAntaresPortView.LENGTH - innerBounds.height - 2 * insets)
			Direction.SOUTH -> Point2D(-innerBounds.width / 2 - insets, AbstractAntaresPortView.LENGTH.toDouble())
			Direction.WEST -> Point2D(-AbstractAntaresPortView.LENGTH - innerBounds.width - 2 * insets, -innerBounds.height / 2 - insets)
		}

	override fun updateViewImpl() {
		getInput().direction = orientation.opposite()
		getInput().setLocation(AbstractAntaresPortView.LENGTH * orientation.dx, AbstractAntaresPortView.LENGTH * orientation.dy)
		if (hasOutput) {
			getOutput().direction = orientation
			when (orientation) {
				Direction.EAST -> getOutput().setLocation(2 * AbstractAntaresPortView.LENGTH + innerBounds.width, 0.0)
				Direction.NORTH -> getOutput().setLocation(0.0, -2 * AbstractAntaresPortView.LENGTH - innerBounds.height)
				Direction.WEST -> getOutput().setLocation(-2 * AbstractAntaresPortView.LENGTH - innerBounds.width, 0.0)
				Direction.SOUTH -> getOutput().setLocation(0.0, 2 * AbstractAntaresPortView.LENGTH + innerBounds.height)
			}
		}
		updateLabelPosition()
	}

	/** ---- [ControlViewSource] */

	override val controlId: String get() = "probe:$id"

	override val controlName: String get() = if (StringUtils.isBlank(name)) "$type (ID:$id)" else "$type $name"

	override val iconPath: String get() = BaseModule.properties.getString(PROP_ICON_PATH)

	override fun createControlView(): ControlView<Probe> {
		val controlView = DigitalSignalSourceControlView(styleProvider, controlId, signalRepresentation, model)
		controlView.location = Point2D(0, 0)
		return controlView
	}

	/** ---- [AbstractDrawable] */

	override fun drawImpl(context: DrawContext) {
		if (shadow) {
			DropShadow.draw(context, transparency) {
				context.g.fillRoundRect(xInt, yInt, width.toInt(), height.toInt(), 10, 10)
			}
		}
		super.drawImpl(context)
		if (context.castedAppContext<GraphApplicationContext>()!!.isExecute) {
			drawSimulated(context)
		} else {
			drawEdited(context)
		}
	}

	/** ---- [ProbeView] */

	private fun drawSimulated(context: DrawContext) {
		drawEdited(context)
		drawInnerView(context, true)
	}

	private fun drawEdited(context: DrawContext) {
		if (context.useContextColors) {
			drawEdited(context, context.color!!.foregroundColor, context.color!!.backgroundColor, context.color!!.textColor)
		} else {
			drawEdited(context, foregroundColor, propertiesBackgroundColor, styleProvider.getStyle(StyleType.BACKGROUND).color.textColor)
		}
	}

	private fun drawEdited(context: DrawContext, lineColor: Color, fillColor: Color?, textColor: Color) {
		val oldStroke = context.g.stroke
		val oldColor = context.g.color

		if (fillColor != null) {
			context.g.color = transparent.applyTo(fillColor)
			context.g.fillRoundRect(xInt, yInt, width.toInt(), height.toInt(), 10, 10)
		}
		context.g.stroke = stroke
		context.g.color = transparent.applyTo(lineColor)
		context.g.drawRoundRect(xInt, yInt, width.toInt(), height.toInt(), 10, 10)

		if (hasOutput) {
			context.translatedAndRotated(getOutput().location, orientation.rotation.angle) {
				it.g.fill(TRIANGLE_PATH)
			}
		}

		context.g.color = textColor
		label.draw(context)

		context.g.color = oldColor
		context.g.stroke = oldStroke
	}

	private fun updateLabel() {
		label.text = StringUtils.orEmpty(name)
		updateLabelPosition()
	}

	private fun updateLabelPosition() {
		if (model.hasOutput) {
			updateLabelPositionWithOutput()
		} else {
			updateLabelPositionWithoutOutput()
		}
	}

	private fun updateLabelPositionWithoutOutput() {
		label.location = when (orientation) {
			Direction.EAST -> Point2D(AbstractAntaresPortView.LENGTH + width + LABEL_DIST, 0.0)
			Direction.NORTH -> Point2D(0.0, -(AbstractAntaresPortView.LENGTH + height + LABEL_DIST))
			Direction.WEST -> Point2D(-(AbstractAntaresPortView.LENGTH + width + LABEL_DIST), 0.0)
			Direction.SOUTH -> Point2D(0.0, AbstractAntaresPortView.LENGTH + height + LABEL_DIST)
		}
		label.alignment = Alignment.forOrientation(orientation.opposite())
	}

	private fun updateLabelPositionWithOutput() {
		if (orientation.isHorizontal()) {
			label.location = Point2D(rectangle.centerX, rectangle.minY - LABEL_DIST)
			label.alignment = Alignment(HorizontalAlignment.CENTER, VerticalAlignment.BOTTOM)
		} else {
			label.location = Point2D(rectangle.maxX + LABEL_DIST, rectangle.centerY)
			label.alignment = Alignment(HorizontalAlignment.LEFT, VerticalAlignment.CENTER)
		}
	}
}