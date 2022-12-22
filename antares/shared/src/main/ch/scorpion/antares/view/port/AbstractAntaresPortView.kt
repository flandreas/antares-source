package ch.scorpion.antares.view.port

import ch.scorpion.antares.view.Look
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Direction.*
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.geom.Rotation
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.drawable.Transparent
import ch.scorpion.jabbah.draw.drawable.TransparentImpl
import ch.scorpion.jabbah.draw.module.DrawModule
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.style.StyleType
import ch.scorpion.jabbah.edit.model.text.HorizontalAlignment
import ch.scorpion.jabbah.edit.model.text.HorizontalAlignment.*
import ch.scorpion.jabbah.edit.model.text.Label
import ch.scorpion.jabbah.edit.model.text.RotationDisplayStrategy
import ch.scorpion.jabbah.edit.model.text.VerticalAlignment
import ch.scorpion.jabbah.graph.container.InternalLabelOrientation
import ch.scorpion.jabbah.graph.model.Port
import ch.scorpion.jabbah.graph.model.PortType
import ch.scorpion.jabbah.graph.view.port.PortView
import ch.scorpion.jabbah.graph.view.port.AbstractPortView
import ch.scorpion.jabbah.graph.view.port.PortLabelPosition
import ch.scorpion.jabbah.graph.view.style.GraphStyleType

/**
 * @param T the type of signal
 */
abstract class AbstractAntaresPortView<T: Any>(
	protected val styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	port: Port<T>,
	x: Int = 0,
	y: Int = 0,
	direction: Direction = EAST,
	portLabelPosition: PortLabelPosition = PortLabelPosition.INTERNAL,
	internalLabelOrientation: InternalLabelOrientation = InternalLabelOrientation.Horizontal,
	length: Int,
	customUnconnectedLength: Int? = null,
	) : AbstractPortView<T>(port, x, y, direction, portLabelPosition, internalLabelOrientation, length) {

	companion object {
		const val LENGTH: Int = 2 * Look.SCALE
		private const val INT_BORDER_DIST = 5
		const val LOGIC_SIZE = (2 * Look.SCALE / 1.7f).toInt()
		private const val SMALL_EXT_BORDER_DIST = 4
		private const val LARGE_EXT_BORDER_DIST = SMALL_EXT_BORDER_DIST + LOGIC_SIZE
		const val INTERNAL_ANNOTATION_SIZE = (LOGIC_SIZE * 1.25).toInt()
	}

	var largeExternalPortLabelDistance: Boolean = false
		set(value) {
			if (field != value) {
				invalidate()
				field = value
				buildPortLabel()
				invalidate()
				validate()
			}
		}

	protected var portLabel: Label? = null
		private set

	init {
		buildPortLabel()
	}

	/** ---- [PortView] interface */

	override val customUnconnectedLength: Int? = customUnconnectedLength

	override fun prepareConnectionDrawContext(context: DrawContext) {
		setupColor(context)
		setupStroke(context)
	}

	override val minSegmentLength: Int get() = LENGTH

	/** ---- [Transparent] interface */

	protected val transparent = TransparentImpl(this)

	override var transparency: Int
		get() = transparent.transparency
		set(value) {
			transparent.transparency = value
		}

	/** ---- [Drawable] */

	override val boundingBox: Rectangle2D
		get() {
			val bbox: Rectangle2D = if (portLabel != null) {
				val lb = portLabel!!.boundingBox
				Rectangle2D(locationX + lb.x, locationY + lb.y, lb.width, lb.height)
			} else {
				Rectangle2D(locationX, locationY, 0.0, 0.0)
			}
			bbox.add(Rectangle2D(location.xInt, location.yInt, 0, 0))

			bbox.add(location.toRect(1.0))
			bbox.add(connectionPoint.toRect(1.0))
			return bbox
		}

	override fun contains(x: Double, y: Double): Boolean = boundingBox.contains(x, y)

	override fun draw(context: DrawContext) {
		drawBelowOwner(context)
		drawAboveOwner(context)
	}

	protected abstract fun setupColor(context: DrawContext)

	protected abstract fun setupStroke(context: DrawContext)

	protected open fun drawAboveOwnerImpl(context: DrawContext) {}

	protected open fun drawAccess(context: DrawContext) {
		val connPoint = connectionPoint
		context.g.drawLine(locationX.toInt(), locationY.toInt(), connPoint.x.toInt(), connPoint.y.toInt())
	}

	override fun drawAboveOwner(context: DrawContext) {
		val origColor = context.g.color

		setupColor(context)
		context.g.translate(locationX, locationY)
		drawAboveOwnerImpl(context)

		portLabel?.let {
			context.g.color = transparent.applyTo(if (portLabelPosition == PortLabelPosition.EXTERNAL) {
				context.choose(styleProvider.getStyle(GraphStyleType.EDGE).color).textColor
			} else {
				context.choose(context.styleColor(styleProvider.getStyle(StyleType.FIGURE).color).deriveTextTowardsBackgroundColor()).textColor
			})
			portLabel?.draw(context)
		}

		context.g.translate(-locationX, -locationY)
		DrawModule.drawDebugBoundingBox(this, context.g, DrawModule.DEBUG_BBOX_COLOR_SECONDARY)
		context.g.color = origColor
	}

	override fun drawBelowOwner(context: DrawContext) {
		val origColor = context.g.color
		drawAccess(context)
		context.g.color = origColor
	}

	/** ---- [AbstractPortView] */

	override fun modelChanged() {
		buildPortLabel()
		updateLength()
		super.modelChanged()
	}

	/** ---- [AbstractAntaresPortView] */

	/** Determines whether this [DigitalPortView] has an internal input annotation to be drawn.*/
	protected open val hasInternalInputAnnotation: Boolean get() = false

	/** Determines whether this [DigitalPortView] has an internal output annotation to be drawn.*/
	protected open val hasInternalOutputAnnotation: Boolean get() = false

	protected open val hasExternalAnnotation: Boolean get() = false

	protected val centerExternalLabel: Boolean get() = port.isConnected && edgeViewWidth > Look.EXT_PIN_FONT.size

	protected fun buildPortLabel() {
		portLabel = when (portLabelPosition) {
			PortLabelPosition.INTERNAL -> buildInternalLabel(port)
			PortLabelPosition.EXTERNAL -> buildExternalLabel(port)
			PortLabelPosition.HIDE -> null
		}
	}

	private fun buildInternalLabel(port: Port<T>): Label {
		val rotation = when (internalLabelOrientation) {
			InternalLabelOrientation.Horizontal -> Rotation.R0
			InternalLabelOrientation.Aligned -> getLabelRotation()
		}
		return Label(
			horizontalAlignment = getInternalLabelHorizontalAlignment(direction),
			verticalAlignment = getInternalLabelVerticalAlignment(direction),
			font = Look.INT_PIN_FONT,
			text = port.name,
			location = getInternalLabelLocation(direction),
			rotationDisplayStrategy = RotationDisplayStrategy.ROTATE_HALF,
			rotation = rotation,
			ownerRotation = ownerRotation)
	}

	private fun buildExternalLabel(port: Port<T>): Label {
		return Label(
			horizontalAlignment = getExternalLabelHorizontalAlignment(direction),
			verticalAlignment = getExternalLabelVerticalAlignment(),
			font = Look.EXT_PIN_FONT,
			text = port.name,
			location = getExternalLabelLocation(direction),
			rotationDisplayStrategy = RotationDisplayStrategy.ROTATE_HALF,
			rotation = getLabelRotation(),
			ownerRotation = ownerRotation)
	}

	private fun getLabelRotation(): Rotation =
		when (direction) {
			NORTH -> Rotation.R90
			SOUTH -> Rotation.R90
			else -> Rotation.R0
		}

	private fun getInternalLabelHorizontalAlignment(direction: Direction): HorizontalAlignment =
		when (internalLabelOrientation) {
			InternalLabelOrientation.Horizontal -> when (direction) {
				WEST -> LEFT
				EAST -> RIGHT
				NORTH, SOUTH -> CENTER
			}
			InternalLabelOrientation.Aligned -> when (direction) {
				WEST -> LEFT
				EAST -> RIGHT
				NORTH -> RIGHT
				SOUTH -> LEFT
			}
		}

	private fun getExternalLabelHorizontalAlignment(direction: Direction): HorizontalAlignment =
		when (direction) {
			WEST -> RIGHT
			EAST -> LEFT
			NORTH -> LEFT
			SOUTH -> RIGHT
		}

	private fun getInternalLabelVerticalAlignment(direction: Direction): VerticalAlignment =
		when (internalLabelOrientation) {
			InternalLabelOrientation.Horizontal -> when (direction) {
				WEST -> VerticalAlignment.CENTER
				EAST -> VerticalAlignment.CENTER
				NORTH -> VerticalAlignment.TOP
				SOUTH -> VerticalAlignment.BOTTOM
			}
			InternalLabelOrientation.Aligned -> when (direction) {
				WEST -> VerticalAlignment.CENTER
				EAST -> VerticalAlignment.CENTER
				NORTH -> VerticalAlignment.CENTER
				SOUTH -> VerticalAlignment.CENTER
			}
		}

	private fun getExternalLabelVerticalAlignment(): VerticalAlignment =
		if (centerExternalLabel)
			VerticalAlignment.CENTER
		else
			VerticalAlignment.BOTTOM

	private fun getInternalLabelLocation(direction: Direction): Point2D {
		val ia = when (port.portType) {
			PortType.INOUT -> 0
			PortType.INPUT -> if (hasInternalInputAnnotation) INTERNAL_ANNOTATION_SIZE else 0
			PortType.OUTPUT -> if (hasInternalOutputAnnotation) INTERNAL_ANNOTATION_SIZE else 0
		}

		return when (direction) {
			WEST -> Point2D(INT_BORDER_DIST + ia, 0)
			EAST -> Point2D(-INT_BORDER_DIST - ia, 0)
			NORTH -> Point2D(0, INT_BORDER_DIST + ia)
			SOUTH -> Point2D(0, -INT_BORDER_DIST - ia)
		}
	}

	private fun getExternalLabelLocation(direction: Direction): Point2D {
		val ea = if (hasExternalAnnotation) LOGIC_SIZE else 0
		val dist = if (largeExternalPortLabelDistance) LARGE_EXT_BORDER_DIST else SMALL_EXT_BORDER_DIST
		return when (direction) {
			WEST -> Point2D(-dist - ea, -1)
			EAST -> Point2D(dist + ea, -1)
			NORTH -> Point2D(0, -dist - ea)
			SOUTH -> Point2D(0, dist + ea)
		}
	}
}