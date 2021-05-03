package ch.scorpion.antares.view.port

import ch.scorpion.antares.model.Logic
import ch.scorpion.antares.model.OutputAnnotation
import ch.scorpion.antares.model.Trigger
import ch.scorpion.antares.model.port.DigitalPort
import ch.scorpion.antares.model.port.DigitalPortImpl
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.view.Look
import ch.scorpion.antares.view.style.AntaresTheme
import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.geom.*
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.drawable.Transparent
import ch.scorpion.jabbah.draw.drawable.TransparentImpl
import ch.scorpion.jabbah.draw.module.DrawModule
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.style.StyleType
import ch.scorpion.jabbah.draw.style.Themes
import ch.scorpion.jabbah.edit.model.text.HorizontalAlignment
import ch.scorpion.jabbah.edit.model.text.Label
import ch.scorpion.jabbah.edit.model.text.VerticalAlignment
import ch.scorpion.jabbah.execution.speed.SystemSpeedCategory
import ch.scorpion.jabbah.graph.GraphApplicationContext
import ch.scorpion.jabbah.graph.model.Port
import ch.scorpion.jabbah.graph.model.PortType
import ch.scorpion.jabbah.graph.view.port.AbstractPortView
import ch.scorpion.jabbah.graph.view.port.PortLabelPosition
import ch.scorpion.jabbah.graph.view.port.PortView
import ch.scorpion.jabbah.graph.view.style.GraphStyleType
import ch.scorpion.jabbah.graph.view.style.GraphTheme
import ch.scorpion.jabbah.io.Reference
import ch.scorpion.jabbah.io.ReferenceResolver
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import kotlin.math.min

/**
 * A view representation of a [DigitalPort], either input or output.
 */
class DigitalPortView(
	private val styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	port: Port<DigitalSignal> = DigitalPortImpl.createInput(),
	x: Int = 0,
	y: Int = 0,
	direction: Direction = Direction.EAST,
	portLabelPosition: PortLabelPosition = PortLabelPosition.INTERNAL,
	length: Int? = null,
	unconnectedLength: Int = length ?: LENGTH,
	var predefinedConnectedLength: Int? = null,
	showBitWidthAnnotation: Boolean = true,
	showLogicAnnotation: Boolean = true
) : AbstractPortView<DigitalSignal>(port, x, y, direction, portLabelPosition, unconnectedLength, length ?: LENGTH) {

	companion object {
		const val LENGTH: Int = 2 * Look.SCALE
		const val INT_BORDER_DIST = 5
		const val EXT_BORDER_DIST = 4
		const val LOGIC_SIZE = (2 * Look.SCALE / 1.7f).toInt()
		const val INTERNAL_ANNOTATION_SIZE = (LOGIC_SIZE * 1.25).toInt()

		val EDGE_TRIGGER_PATH: Path = System.createPath()
			.moveTo(0.0, -INTERNAL_ANNOTATION_SIZE / 2.0)
			.lineTo(-INTERNAL_ANNOTATION_SIZE, 0)
			.lineTo(0, INTERNAL_ANNOTATION_SIZE / 2)

		val MASTER_SLAVE_PATH: Path = System.createPath()
			.moveTo(-INTERNAL_ANNOTATION_SIZE, -INTERNAL_ANNOTATION_SIZE / 2 + 1)
			.lineTo(-INTERNAL_ANNOTATION_SIZE / 2, -INTERNAL_ANNOTATION_SIZE / 2 + 1)
			.lineTo(-INTERNAL_ANNOTATION_SIZE / 2, INTERNAL_ANNOTATION_SIZE / 2 - 1)

		val TRI_STATE_PATH: Path = System.createPath()
			.moveTo(-INTERNAL_ANNOTATION_SIZE, -INTERNAL_ANNOTATION_SIZE / 2 + 1)
			.lineTo(-3, -INTERNAL_ANNOTATION_SIZE / 2 + 1)
			.lineTo(-INTERNAL_ANNOTATION_SIZE / 2 - 1.5, INTERNAL_ANNOTATION_SIZE / 2 - 1.0)
			.close()
	}

	/** Determines whether this [DigitalPortView] shows an annotation that indicates the [DigitalPort]'s [BitWidth].*/
	var showBitWidthAnnotation: Boolean = showBitWidthAnnotation
		set(value) {
			if (value != field) {
				invalidate()
				field = value
				invalidate()
				validate()
			}
		}

	var showLogicAnnotation: Boolean = showLogicAnnotation
		set(value) {
			if (value != field) {
				invalidate()
				field = value
				invalidate()
				validate()
			}
		}

	private var portLabel: Label? = null

	private var bitWidthAnnotation: BitWidthAnnotation? = null

	/** Determines whether this [DigitalPortView] has an internal input annotation to be drawn.*/
	private val hasInternalInputAnnotation: Boolean
		get() = (port as DigitalPort).trigger == Trigger.EDGE

	/** Determines whether this [DigitalPortView] has an internal output annotation to be drawn.*/
	private val hasInternalOutputAnnotation: Boolean
		get() = (port as DigitalPort).outputAnnotation != OutputAnnotation.NONE

	private val hasExternalAnnotation: Boolean get() =
		showLogicAnnotation && (port as DigitalPort).logic == Logic.NEGATIVE

	init {
		buildPortLabel()
		buildBitWidthAnnotation()
	}

	/** ---- [Transparent] interface */

	private val transparent = TransparentImpl(this)

	override var transparency: Int
		get() = transparent.transparency
		set(value) {
			transparent.transparency = value
		}

	/** ---- [AbstractPortView] */

	override var edgeViewWidth: Int
		get() = super.edgeViewWidth
		set(value) {
			if (super.edgeViewWidth != value) {
				super.edgeViewWidth = value
				buildPortLabel()
				buildBitWidthAnnotation()
			}
		}

	override fun modelChanged() {
		super.modelChanged()
		buildPortLabel()
		buildBitWidthAnnotation()
	}

	override fun getConnectedLength(): Int {
		if (predefinedConnectedLength != null) {
			return predefinedConnectedLength!!
		}
		if (showLogicAnnotation && getDigitalPort().logic == Logic.NEGATIVE) {
			return LOGIC_SIZE
		}
		return 0
	}

	/** ---- [Storable] */

	override fun read(reader: StoreReader) {
		super.read(reader)
		reader.requestResolution(this, Reference(name = ""))
	}

	override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) {
		super.resolve(reference, referenceResolver)
		if (reference.name == "portRef") {
			unconnectedLength = LENGTH
			length = LENGTH
			predefinedConnectedLength = null
			buildPortLabel()
			buildBitWidthAnnotation()
		}
	}

	/** ---- [Drawable] */

	private fun setupColor(context: DrawContext) {
		val appContext = context.castedAppContext<GraphApplicationContext>()!!

		if (appContext.isExecute && showNetState(appContext.systemSpeedCategory.systemSpeedCategory)) {
			context.g.color = transparent.applyTo(when (port.portType) {
				PortType.INOUT -> drawableInOutSignal.getColor().foregroundColor
				PortType.INPUT -> getDigitalPort().getIncomingSignal()!!.getColor().foregroundColor
				PortType.OUTPUT -> getDigitalPort().getOutgoingSignal()!!.getColor().foregroundColor
			})
		} else {
			context.g.color = context.choose(styleProvider.getStyle(GraphStyleType.EDGE).color).foregroundColor
		}
	}

	private val drawableInOutSignal: DigitalSignal get() {
		return if (getDigitalPort().net != null) {
			getDigitalPort().net!!.signal!!
		} else {
			getDigitalPort().dominantSignal
		}
	}

	private fun setupStroke(context: DrawContext) {
		if (getDigitalPort().bitWidth.width > 1) {
			context.g.stroke = Themes.get<GraphTheme>().edge.busStroke
		} else {
			context.g.stroke = if (context.castedAppContext<GraphApplicationContext>()!!.isExecute) {
				Themes.get<GraphTheme>().edge.executionStroke
			} else {
				Themes.get<GraphTheme>().edge.stroke
			}
		}
	}

	override fun draw(context: DrawContext) {
		drawBelowOwner(context)
		drawAboveOwner(context)
	}

	override fun drawAboveOwner(context: DrawContext) {
		val origColor = context.g.color

		setupColor(context)

		context.g.translate(locationX, locationY)

		if (bitWidthAnnotation != null && showBitWidthAnnotation) {
			bitWidthAnnotation!!.draw(context)
		}

		drawLogic(context)

		context.g.color = transparent.applyTo(context.choose(styleProvider.getStyle(StyleType.ANNOTATION).color).foregroundColor)
		if (hasInternalInputAnnotation) {
			drawInternalInputAnnotation(context)
		}
		if (hasInternalOutputAnnotation) {
			drawInternalOutputAnnotation(context)
		}

		portLabel?.let {
			context.g.color = transparent.applyTo(if (portLabelPosition == PortLabelPosition.EXTERNAL) {
				context.choose(styleProvider.getStyle(GraphStyleType.EDGE).color).textColor
			} else {
				context.choose(context.styleColor(styleProvider.getStyle(StyleType.FIGURE).color)).textColor
			})
			portLabel?.draw(context)
		}

		context.g.translate(-locationX, -locationY)

		DrawModule.drawDebugBoundingBox(this, context.g, DrawModule.DEBUG_BBOX_COLOR_SECONDARY)

		context.g.color = origColor
	}

	override fun drawBelowOwner(context: DrawContext) {
		val origColor = context.g.color

		if (!port.isConnected) {
			prepareConnectionDrawContext(context)

			val connPoint = connectionPoint
			context.g.drawLine(locationX.toInt(), locationY.toInt(), connPoint.x.toInt(), connPoint.y.toInt())
		}

		context.g.color = origColor
	}

	override val boundingBox: Rectangle2D
		get() {
			val bbox: Rectangle2D = if (portLabel != null) {
				val lb = portLabel!!.boundingBox
				Rectangle2D(locationX + lb.x, locationY + lb.y, lb.width, lb.height)
			} else {
				Rectangle2D(locationX, locationY, 0.0, 0.0)
			}
			if (bitWidthAnnotation != null) {
				val bb = bitWidthAnnotation!!.boundingBox
				bbox.add(Rectangle2D(locationX + bb.x, locationY + bb.y, bb.width, bb.height))
			}
			if (hasInternalInputAnnotation) {
				bbox.add(getInternalInputAnnotationBox()!!)
			}
			if (hasInternalOutputAnnotation) {
				bbox.add(getInternalOutputAnnotationBox()!!)
			}
			bbox.add(location.toRect(1.0))
			bbox.add(connectionPoint.toRect(1.0))
			return bbox
		}

	override fun contains(x: Double, y: Double): Boolean {
		return boundingBox.contains(x, y)
	}

	override fun buildToolTipContent(): String {
		val content = StringBuilder(super.buildToolTipContent())
		if (getDigitalPort().bitWidth != BitWidth.BW_1) {
			content.append("<p/><b>${Translations.getString("element.property.bitWidth.name")}</b>: ${getDigitalPort().bitWidth.width}")
		}
		return content.toString()
	}

	/** ---- [PortView] interface */

	override fun prepareConnectionDrawContext(context: DrawContext) {
		setupColor(context)
		setupStroke(context)
	}

	/** ---- [AbstractPortView] */

	override fun setPortName(name: String) {
		portLabel?.text = name
		super.setPortName(name)
	}

	override var portLabelPosition: PortLabelPosition
		get() = super.portLabelPosition
		set(value) {
			super.portLabelPosition = value
			invalidate()
			buildPortLabel()
			invalidate()
			validate()
		}

	override val minSegmentLength: Int get() = LENGTH

	override fun ownerRotationChanged() {
		super.ownerRotationChanged()
		portLabel?.ownerRotation = ownerRotation
		bitWidthAnnotation?.setOwnerRotation(ownerRotation)
	}

	/** ---- [DigitalPortView] */

	// TODO Refactor (DRY): Same logic as in [AbstractNetViewElement]
	private fun showNetState(systemSpeedCategory: SystemSpeedCategory): Boolean {
		return systemSpeedCategory > SystemSpeedCategory.Use
	}

	private fun getDigitalPort(): DigitalPort {
		return port as DigitalPort
	}

	private fun buildBitWidthAnnotation() {
		bitWidthAnnotation = if (getDigitalPort().bitWidth != BitWidth.BW_1) {
			if (centerExternalLabel && portLabelPosition == PortLabelPosition.EXTERNAL) {
				// The external label has priority over BitWithAnnotation
				null
			} else {
				BitWidthAnnotation(getDigitalPort().bitWidth, direction, centerExternalLabel, ownerRotation = ownerRotation)
			}
		} else {
			null
		}
	}

	private fun buildPortLabel() {
		portLabel = when (portLabelPosition) {
			PortLabelPosition.INTERNAL -> buildInternalLabel(port)
			PortLabelPosition.EXTERNAL -> buildExternalLabel(port)
			PortLabelPosition.HIDE -> null
		}
	}

	private fun buildInternalLabel(port: Port<DigitalSignal>): Label {
		return Label(
			horizontalAlignment = getHorizontalInternalLabelAlignment(direction),
			verticalAlignment = getVerticalInternalLabelAlignment(direction),
			font = Look.INT_PIN_FONT,
			text = port.name,
			location = getInternalLabelLocation(direction),
			rotationDisplayStrategy = Label.RotationDisplayStrategy.ROTATE_HALF,
			ownerRotation = ownerRotation)
	}

	private fun buildExternalLabel(port: Port<DigitalSignal>): Label {
		val rotation: Rotation = when (direction) {
			Direction.NORTH -> Rotation.R90
			Direction.SOUTH -> Rotation.R90
			else -> Rotation.R0
		}
		return Label(
			horizontalAlignment = getHorizontalExternalLabelAlignment(direction),
			verticalAlignment = getVerticalExternalLabelAlignment(),
			font = Look.EXT_PIN_FONT,
			text = port.name,
			location = getExternalLabelLocation(direction),
			rotationDisplayStrategy = Label.RotationDisplayStrategy.ROTATE_HALF,
			rotation = rotation,
			ownerRotation = ownerRotation)
	}

	private fun getHorizontalInternalLabelAlignment(direction: Direction): HorizontalAlignment =
		when (direction) {
			Direction.WEST -> HorizontalAlignment.LEFT
			Direction.EAST -> HorizontalAlignment.RIGHT
			Direction.NORTH -> HorizontalAlignment.CENTER
			Direction.SOUTH -> HorizontalAlignment.CENTER
		}

	private fun getHorizontalExternalLabelAlignment(direction: Direction): HorizontalAlignment =
		when (direction) {
			Direction.WEST -> HorizontalAlignment.RIGHT
			Direction.EAST -> HorizontalAlignment.LEFT
			Direction.NORTH -> HorizontalAlignment.LEFT
			Direction.SOUTH -> HorizontalAlignment.RIGHT
		}

	private fun getVerticalInternalLabelAlignment(direction: Direction): VerticalAlignment =
		when (direction) {
			Direction.WEST -> VerticalAlignment.CENTER
			Direction.EAST -> VerticalAlignment.CENTER
			Direction.NORTH -> VerticalAlignment.TOP
			Direction.SOUTH -> VerticalAlignment.BOTTOM
		}

	private val centerExternalLabel: Boolean get() = port.isConnected && edgeViewWidth > Look.EXT_PIN_FONT.size

	private fun getVerticalExternalLabelAlignment(): VerticalAlignment =
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
			Direction.WEST -> Point2D(INT_BORDER_DIST + ia, 0)
			Direction.EAST -> Point2D(-INT_BORDER_DIST - ia, 0)
			Direction.NORTH -> Point2D(0, INT_BORDER_DIST + ia)
			Direction.SOUTH -> Point2D(0, -INT_BORDER_DIST - ia)
		}
	}

	private fun getExternalLabelLocation(direction: Direction): Point2D {
		val ea = if (hasExternalAnnotation) LOGIC_SIZE else 0
		return when (direction) {
			Direction.WEST -> Point2D(-EXT_BORDER_DIST - ea, -1)
			Direction.EAST -> Point2D(EXT_BORDER_DIST + ea, -1)
			Direction.NORTH -> Point2D(0, -EXT_BORDER_DIST - ea)
			Direction.SOUTH -> Point2D(0, EXT_BORDER_DIST + ea)
		}
	}

	private fun drawLogic(context: DrawContext) {
		if (showLogicAnnotation && getDigitalPort().logic == Logic.NEGATIVE) {
			val x1 = 0
			val x2 = LOGIC_SIZE * direction.dx + LOGIC_SIZE * direction.next().dx
			val y1 = 0
			val y2 = LOGIC_SIZE * direction.dy + LOGIC_SIZE * direction.next().dy

			var logicX = min(x1, x2)
			var logicY = min(y1, y2)

			logicX += LOGIC_SIZE * direction.previous().dx / 2
			logicY += LOGIC_SIZE * direction.previous().dy / 2

			val fillColor = if (Look.FILL_BASIC_COMPONENTS) {
				context.styleColor(styleProvider.getStyle(StyleType.BACKGROUND).color)
			} else {
				styleProvider.getStyle(StyleType.BACKGROUND).color
			}
			context.g.color = transparent.applyTo(context.choose(fillColor).backgroundColor)
			context.g.fillOval(logicX, logicY, LOGIC_SIZE, LOGIC_SIZE)

			context.g.stroke = Themes.get<AntaresTheme>().figure.stroke
			context.g.color = transparent.applyTo(context.choose(context.styleColor(styleProvider.getStyle(GraphStyleType.VERTICE).color)).foregroundColor)
			context.g.drawOval(logicX, logicY, LOGIC_SIZE, LOGIC_SIZE)
		}
	}

	/**
	 * Draws the internal input annotation of this [DigitalPortView], if any.
	 * This method it automatically called if [hasInternalInputAnnotation] returns `true`.
	 */
	private fun drawInternalInputAnnotation(context: DrawContext) {
		if (hasInternalInputAnnotation) {
			val angle = direction.rotation.angle
			context.g.stroke = styleProvider.getStyle(StyleType.ANNOTATION).stroke
			context.g.rotate(angle)
			context.g.draw(EDGE_TRIGGER_PATH)
			context.g.rotate(-angle)
		}
	}

	private fun getInternalInputAnnotationBox(): RectangularShape? {
		if (hasInternalInputAnnotation) {
			val bb = direction.rotation.rotateRectangleAround(Point2D.ZERO, EDGE_TRIGGER_PATH.boundingBox)
			return Rectangle2D(locationX + bb.x, locationY + bb.y, bb.width, bb.height)
		}
		return null
	}

	private fun drawInternalOutputAnnotation(context: DrawContext) {
		if (hasInternalOutputAnnotation) {
			val angle = direction.rotation.angle
			context.g.stroke = styleProvider.getStyle(StyleType.ANNOTATION).stroke
			context.g.rotate(angle)
			context.g.draw(getOutputAnnotationPath()!!)
			context.g.rotate(-angle)
		}
	}

	private fun getInternalOutputAnnotationBox(): RectangularShape? {
		if (hasInternalOutputAnnotation) {
			val bb = direction.rotation.rotateRectangleAround(Point2D.ZERO, getOutputAnnotationPath()!!.boundingBox)
			return Rectangle2D(locationX + bb.x, locationY + bb.y, bb.width, bb.height)
		}
		return null
	}

	private fun getOutputAnnotationPath(): Path? =
		when ((port as DigitalPort).outputAnnotation) {
			OutputAnnotation.TRI_STATE -> TRI_STATE_PATH
			OutputAnnotation.MASTER_SLAVE -> MASTER_SLAVE_PATH
			else -> null
		}
}