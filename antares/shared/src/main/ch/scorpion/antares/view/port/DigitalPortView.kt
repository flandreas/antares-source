package ch.scorpion.antares.view.port

import ch.scorpion.antares.model.Logic
import ch.scorpion.antares.model.OutputAnnotation
import ch.scorpion.antares.model.Trigger
import ch.scorpion.antares.model.port.DigitalPort
import ch.scorpion.antares.model.port.DigitalPortImpl
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.Word
import ch.scorpion.antares.view.Look
import ch.scorpion.antares.view.net.DigitalEdgeView
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.geom.*
import ch.scorpion.jabbah.base.geom.Direction.*
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
import ch.scorpion.jabbah.edit.model.text.HorizontalAlignment.*
import ch.scorpion.jabbah.edit.model.text.Label
import ch.scorpion.jabbah.edit.model.text.RotationDisplayStrategy
import ch.scorpion.jabbah.edit.model.text.VerticalAlignment
import ch.scorpion.jabbah.execution.actor.ActorInteractionContext
import ch.scorpion.jabbah.graph.GraphApplicationContext
import ch.scorpion.jabbah.graph.container.InternalLabelOrientation
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

/**
 * A view representation of a [DigitalPort], either input or output.
 */
class DigitalPortView(
	private val styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	port: Port<DigitalSignal> = DigitalPortImpl.createInput(),
	x: Int = 0,
	y: Int = 0,
	direction: Direction = EAST,
	portLabelPosition: PortLabelPosition = PortLabelPosition.INTERNAL,
	internalLabelOrientation: InternalLabelOrientation = InternalLabelOrientation.Horizontal,
	length: Int? = null,
	customUnconnectedLength: Int? = null,
	showBitWidthAnnotation: Boolean = true,
	showLogicAnnotation: Boolean = true,
	style: DigitalPortViewStyle = DigitalPortViewStyle.Line
) : AbstractPortView<DigitalSignal>(port, x, y, direction, portLabelPosition, internalLabelOrientation, length ?: style.unconnectedLength) {

	companion object {
		const val LENGTH: Int = 2 * Look.SCALE
		private const val INT_BORDER_DIST = 5
		const val LOGIC_SIZE = (2 * Look.SCALE / 1.7f).toInt()
		private const val SMALL_EXT_BORDER_DIST = 4
		private const val LARGE_EXT_BORDER_DIST = SMALL_EXT_BORDER_DIST + LOGIC_SIZE
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

	var portViewStyle: DigitalPortViewStyle = style
		set(value) {
			if (value != field) {
				invalidate()
				field = value
				length = unconnectedLength
				invalidate()
				validate()
			}
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
		buildPortLabel()
		buildBitWidthAnnotation()
		updateLength()
		super.modelChanged()
	}

	/** ---- [Storable] */

	override fun read(reader: StoreReader) {
		super.read(reader)
		reader.requestResolution(this, Reference(name = ""))
	}

	override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) {
		super.resolve(reference, referenceResolver)
		if (reference.name == "portRef") {
			length = unconnectedLength
			buildPortLabel()
			buildBitWidthAnnotation()
		}
	}

	/** ---- [Drawable] */

	private fun setupColor(context: DrawContext) {
		val appContext = context.castedAppContext<GraphApplicationContext>()!!

		if (appContext.showNetState) {
			context.g.color = transparent.applyTo(when (port.portType) {
				PortType.INOUT -> drawableInOutSignal.color.foregroundColor
				PortType.INPUT -> getDigitalPort().getIncomingSignal()!!.color.foregroundColor
				PortType.OUTPUT -> getDigitalPort().getOutgoingSignal()!!.color.foregroundColor
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
		context.g.stroke = DigitalEdgeView.getStroke(
			Themes.get<GraphTheme>().edge,
			getDigitalPort().bitWidth,
			context.castedAppContext<GraphApplicationContext>()!!.isExecute)
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

		portViewStyle.drawLogic(this, context, styleProvider, transparent)

		context.g.color = transparent.applyTo(context.choose(context.styleColor(styleProvider.getStyle(GraphStyleType.VERTICE).color)).foregroundColor)
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

		if (portViewStyle.isDrawAccess(this)) {
			prepareConnectionDrawContext(context)
			portViewStyle.drawAccess(this, context, styleProvider, transparent)
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
			bbox.add(portViewStyle.createBasicBoundingBox(this))

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
		if (StringUtils.isEmpty(port.name)) {
			content.appendLine()
			content.append("${Translations.getString("graph.property.PortId.name")}: ${port.portId}")
		}

		if (getDigitalPort().bitWidth.width != BitWidth.BW_1.width) {
			content.appendLine()
			content.append("${Translations.getString("${BitWidth.BASE_KEY}.name")}: ${getDigitalPort().bitWidth.width}")
		}
		return content.toString()
	}

	/** ---- [PortView] interface */

	override val connectedLength: Int get() = portViewStyle.getConnectedLength(this)

	override val unconnectedLength: Int get() = customUnconnectedLength ?: portViewStyle.unconnectedLength

	override val customUnconnectedLength: Int? = customUnconnectedLength

	override fun prepareConnectionDrawContext(context: DrawContext) {
		setupColor(context)
		setupStroke(context)
	}

	override fun handleExecutionClick(context: ActorInteractionContext) {
		with (getDigitalPort()) {
			if (bitWidth == BitWidth.BW_1) {
				val signal = getIncomingSignal()
				val newSignal = if (signal == null || signal.isPartiallyUndefined) {
					Word.trueValue(BitWidth.BW_1)
				} else {
					signal.not()
				}
				setIncomingSignal(newSignal, context.signalHandler)
			}
		}
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

	override var internalLabelOrientation: InternalLabelOrientation
		get() = super.internalLabelOrientation
		set(value) {
			super.internalLabelOrientation = value
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

	fun getDigitalPort(): DigitalPort = port as DigitalPort

	private fun buildBitWidthAnnotation() {
		bitWidthAnnotation = if (getDigitalPort().bitWidth.width != BitWidth.BW_1.width) {
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

	private fun buildExternalLabel(port: Port<DigitalSignal>): Label {
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

	private val centerExternalLabel: Boolean get() = port.isConnected && edgeViewWidth > Look.EXT_PIN_FONT.size

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