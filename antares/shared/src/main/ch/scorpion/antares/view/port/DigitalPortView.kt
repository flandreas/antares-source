package ch.scorpion.antares.view.port

import ch.scorpion.antares.model.Logic
import ch.scorpion.antares.model.OutputAnnotation
import ch.scorpion.antares.model.Trigger
import ch.scorpion.antares.model.port.DigitalPort
import ch.scorpion.antares.model.port.DigitalPortImpl
import ch.scorpion.antares.model.signal.*
import ch.scorpion.antares.model.signal.Word
import ch.scorpion.antares.view.inout.DigitalKeyboard
import ch.scorpion.antares.view.net.DigitalEdgeView
import ch.scorpion.antares.view.style.AntaresTheme
import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.geom.*
import ch.scorpion.jabbah.base.geom.Direction.EAST
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.style.StyleType
import ch.scorpion.jabbah.draw.style.Themes
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.execution.actor.ActorInteractionContext
import ch.scorpion.jabbah.graph.GraphApplicationContext
import ch.scorpion.jabbah.graph.GraphApplicationContextHolder
import ch.scorpion.jabbah.graph.container.InternalLabelOrientation
import ch.scorpion.jabbah.graph.model.Port
import ch.scorpion.jabbah.graph.model.PortType
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.net.edge.EdgeViewConnectionGeometry
import ch.scorpion.jabbah.graph.view.net.edge.EdgeViewEndpointType
import ch.scorpion.jabbah.graph.view.port.AbstractPortView
import ch.scorpion.jabbah.graph.view.port.PortLabelPosition
import ch.scorpion.jabbah.graph.view.port.PortView
import ch.scorpion.jabbah.graph.view.style.GraphStyleType
import ch.scorpion.jabbah.graph.view.style.GraphTheme
import ch.scorpion.jabbah.io.Reference
import ch.scorpion.jabbah.io.ReferenceResolver
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import kotlin.math.max

/**
 * A view representation of a [DigitalPort], either input or output.
 */
class DigitalPortView(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
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
	style: DigitalPortViewStyle = DigitalPortViewStyle.Line,
	horizontalExternalLabel: Boolean = false,
	externalPortLabelDistance: ExternalPortLabelDistance = ExternalPortLabelDistance.Small
) : AbstractAntaresPortView<DigitalSignal>(
	styleProvider,
	port,
	x,
	y,
	direction,
	portLabelPosition,
	internalLabelOrientation,
	length ?: style.unconnectedLength,
	customUnconnectedLength,
	horizontalExternalLabel,
	externalPortLabelDistance
), DigitalKeyboard.Target {

	companion object {
		private const val MIN_EDGE_VIEW_LENGTH_FOR_BIT_WIDTH_ANNOTATION = 100

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

	/**
	 * Overrides [showBitWidthAnnotation] with `true` if this [DigitalPortView] is the origin
	 * of the [EdgeView] to which it is connected AND its destination is also connected.
	 * Avoids to display two [BitWidthAnnotation]s very close together.
	 */
	private var hideBitWidthAnnotation: Boolean = false
		set(value) {
			if (field != value) {
				field = value
				invalidate()
				update()
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

	private var bitWidthAnnotation: BitWidthAnnotation? = null

	private var keyboardDigitIndex: Int = 0

	override val hasInternalInputAnnotation: Boolean
		get() = (port as DigitalPort).trigger == Trigger.EDGE

	override val hasInternalOutputAnnotation: Boolean
		get() = (port as DigitalPort).outputAnnotation != OutputAnnotation.NONE

	override val hasExternalAnnotation: Boolean get() =
		showLogicAnnotation && (port as DigitalPort).logic == Logic.NEGATIVE

	init {
		buildBitWidthAnnotation()
	}

	/** ---- [AbstractPortView] */

	override var connectionGeometry: EdgeViewConnectionGeometry?
		get() = super.connectionGeometry
		set(value) {
			if (super.connectionGeometry != value) {
				super.connectionGeometry = value
				buildPortLabel()
				buildBitWidthAnnotation()
			}
		}

	override fun modelChanged() {
		buildBitWidthAnnotation()
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

	override fun setupColor(context: DrawContext) {
		val appContext = context.castedAppContext<GraphApplicationContext>()!!

		context.g.color = if (appContext.showNetState) {
			if (digitalPort.net?.executionError != null) {
				transparent.applyTo(Themes.get<AntaresTheme>().error.foregroundColor)
			} else {
				transparent.applyTo(
					when (port.portType) {
						PortType.INOUT -> drawableInOutSignal.color.foregroundColor
						PortType.INPUT -> digitalPort.getIncomingSignal()!!.color.foregroundColor
						PortType.OUTPUT -> digitalPort.getOutgoingSignal()!!.color.foregroundColor
					}
				)
			}
		} else {
			context.choose(styleProvider.getStyle(GraphStyleType.EDGE).color).foregroundColor
		}
	}

	private val drawableInOutSignal: DigitalSignal get() =
		if (digitalPort.temporarySignal != null) {
			digitalPort.temporarySignal!!
		} else {
			if (digitalPort.net != null) {
				digitalPort.net!!.signal!!
			} else {
				digitalPort.dominantSignal
			}
		}

	override fun setupStroke(context: DrawContext) {
		context.g.stroke = DigitalEdgeView.getStroke(
			Themes.get<GraphTheme>().edge,
			digitalPort.bitWidth,
			context.castedAppContext<GraphApplicationContext>()!!.isExecute)
	}

	override fun drawAboveOwnerImpl(context: DrawContext) {
		portViewStyle.drawLogic(this, context, styleProvider, transparent)

		context.g.color = transparent.applyTo(context.choose(context.styleColor(styleProvider.getStyle(GraphStyleType.VERTICE).color)).foregroundColor)
		if (bitWidthAnnotation != null && showBitWidthAnnotation && !hideBitWidthAnnotation) {
			bitWidthAnnotation!!.draw(context)
		}
		if (hasInternalInputAnnotation) {
			drawInternalInputAnnotation(context)
		}
		if (hasInternalOutputAnnotation) {
			drawInternalOutputAnnotation(context)
		}
	}

	override fun drawAccess(context: DrawContext) {
		if (portViewStyle.isDrawAccess(this)) {
			prepareConnectionDrawContext(context)
			portViewStyle.drawAccess(this, context, styleProvider, transparent)
		}
	}

	override val boundingBox: RectangularShape
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

	override fun buildToolTipContent(): String {
		val content = StringBuilder(super.buildToolTipContent())
		if (digitalPort.bitWidth.width != BitWidth.BW_1.width) {
			content.appendLine()
			content.append("${Translations.getString("${BitWidth.BASE_KEY}.name")}: ${digitalPort.bitWidth.width}")
		}
		return content.toString()
	}

	/** ---- [DigitalKeyboard.Target] */

	override fun consumeKey(key: Int, contextHolder: GraphApplicationContextHolder, graphView: GraphView?) {
		signalRepresentation.digitToWord(BitWidth.of(signalRepresentation.bitCount), key.toChar())?.let { signal ->
			val newSignal = digitalPort.getIncomingSignal()?.let { incomingSignal ->
				signalRepresentation.withDigit(incomingSignal, signal, keyboardDigitIndex)
			}  ?: signal
			digitalPort.setIncomingSignal(newSignal, contextHolder.scheduler)
			if (keyboardDigitIndex == 0) {
				keyboardDigitIndex = signalRepresentation.digitCount(digitalPort.bitWidth) - 1
			} else {
				keyboardDigitIndex -= 1
			}
		}
	}

	override fun clear(contextHolder: GraphApplicationContextHolder) {
		digitalPort.setIncomingSignal(DigitalSignalFactory.of(digitalPort.bitWidth, 0L), contextHolder.scheduler)
	}

	override val keyboardTargetBoundingBox: RectangularShape
		get() = Rectangle2D(boundingBox).moveBy(owner!!.location)

	override val signalRepresentation: DigitalSignalRepresentation get() = digitalPort.signalRepresentation

	/** ---- [PortView] interface */

	override val connectedLength: Int get() = portViewStyle.getConnectedLength(this)

	override val unconnectedLength: Int get() = customUnconnectedLength ?: portViewStyle.unconnectedLength

	override fun handleExecutionClick(context: ActorInteractionContext) {
		with (digitalPort) {
			if (bitWidth == BitWidth.BW_1) {
				val signal = getIncomingSignal()
				val newSignal = if (signal == null || signal.isPartiallyUndefined) {
					Word.trueValue(BitWidth.BW_1)
				} else {
					signal.not()
				}
				setIncomingSignal(newSignal, context.signalHandler)
			} else {
				keyboardDigitIndex = signalRepresentation.digitCount(digitalPort.bitWidth) - 1
				DigitalKeyboard.show(
					this@DigitalPortView,
					context.view as DrawingView<*>,
					context.view.applicationContextHolder as GraphApplicationContextHolder
				)
			}
		}
	}

	override fun handleConnect(edgeView: EdgeView<DigitalSignal>, geometry: EdgeViewConnectionGeometry) {
		super.handleConnect(edgeView, geometry)
		updateHideBitWidthAnnotation(edgeView, geometry)
	}

	override fun handleUnconnect(edgeView: EdgeView<DigitalSignal>?, lockEndpoint: Boolean) {
		super.handleUnconnect(edgeView, lockEndpoint)
		hideBitWidthAnnotation = false
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

	override fun ownerRotationChanged() {
		super.ownerRotationChanged()
		portLabel?.ownerRotation = ownerRotation
		bitWidthAnnotation?.setOwnerRotation(ownerRotation)
	}

	override fun edgeViewUpdated(edgeView: EdgeView<*>, geometry: EdgeViewConnectionGeometry) {
		updateHideBitWidthAnnotation(edgeView, geometry)
	}

	private fun updateHideBitWidthAnnotation(edgeView: EdgeView<*>, geometry: EdgeViewConnectionGeometry) {
		val oppositePortView = edgeView.getOppositeConnection(port)?.portView
		if (oppositePortView != null) {
			// If EdgeView is too short, show bit width annotation at origin and hide at destination
			val narrow = edgeView.polyline.length < MIN_EDGE_VIEW_LENGTH_FOR_BIT_WIDTH_ANNOTATION
			if (edgeView.getConnection(EdgeViewEndpointType.ORIGIN)?.port == port) {
				hideBitWidthAnnotation = false
				(oppositePortView as DigitalPortView).hideBitWidthAnnotation = narrow
			} else {
				hideBitWidthAnnotation = narrow
				(oppositePortView as DigitalPortView).hideBitWidthAnnotation = false
			}
		} else {
			hideBitWidthAnnotation = false
		}
		bitWidthAnnotation?.offsetX = getBitWidthAnnotationOffset(geometry.distance)
	}

	/** ---- [DigitalPortView] */

	val digitalPort: DigitalPort get() = port as DigitalPort

	private fun buildBitWidthAnnotation() {
		bitWidthAnnotation = if (digitalPort.bitWidth.width != BitWidth.BW_1.width) {
			if (centerExternalLabel && portLabelPosition == PortLabelPosition.EXTERNAL) {
				// The external label has priority over BitWithAnnotation
				null
			} else {
				BitWidthAnnotation(digitalPort.bitWidth, direction, centerExternalLabel,
					ownerRotation = ownerRotation, offsetX = getBitWidthAnnotationOffset(0))
			}
		} else {
			null
		}
	}

	private fun getBitWidthAnnotationOffset(edgeViewConnectionGeometryDist: Int): Int {
		val dist = max(BitWidthAnnotation.DIST.toInt(), max(edgeViewConnectionGeometryDist, externalAnnotationSize) + 7)
		return dist - BitWidthAnnotation.DIST.toInt()
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