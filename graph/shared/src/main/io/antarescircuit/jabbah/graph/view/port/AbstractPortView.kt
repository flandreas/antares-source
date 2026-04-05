package io.antarescircuit.jabbah.graph.view.port

import io.antarescircuit.jabbah.base.StringUtils
import io.antarescircuit.jabbah.base.Tooltip
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.event.PropertyChangeEvent
import io.antarescircuit.jabbah.base.event.PropertyChangeListener
import io.antarescircuit.jabbah.base.geom.Direction
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.geom.Rectangle2D
import io.antarescircuit.jabbah.base.geom.Rotation
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.base.resettableLazy
import io.antarescircuit.jabbah.draw.Drawable
import io.antarescircuit.jabbah.draw.InputEventContext
import io.antarescircuit.jabbah.draw.drawable.AbstractDrawable
import io.antarescircuit.jabbah.draw.drawable.Mirrorable
import io.antarescircuit.jabbah.edit.Cloneable
import io.antarescircuit.jabbah.edit.SnappableX
import io.antarescircuit.jabbah.edit.SnappableY
import io.antarescircuit.jabbah.graph.container.InternalLabelOrientation
import io.antarescircuit.jabbah.graph.model.*
import io.antarescircuit.jabbah.graph.view.EdgeView
import io.antarescircuit.jabbah.graph.view.VerticeView
import io.antarescircuit.jabbah.graph.view.net.edge.EdgeViewConnectionGeometry
import io.antarescircuit.jabbah.graph.view.port.PortView.Companion.PROP_SENSITIVE_AREA
import io.antarescircuit.jabbah.io.*
import kotlin.math.sign

/**
 * A [PortView] that draws a line that points to one of the four [Direction]s.
 * @param T the type of signal that this [PortView]'s [Port] can consume or produce.
 */
abstract class AbstractPortView<T : Any>(
	port: Port<T>,
	x: Int,
	y: Int,
	direction: Direction,
	open var portLabelPosition: PortLabelPosition = PortLabelPosition.INTERNAL,
	open var internalLabelOrientation: InternalLabelOrientation = InternalLabelOrientation.Horizontal,
	length: Int,
	override val connectable: Boolean = true
) : AbstractDrawable(), PortView<T>, Storable, Mirrorable {

	override var location: Point2D = Point2D(x, y)
		set(value) {
			invalidate()
			field = value
			invalidate()
			update()
		}

	override var length: Int = length

	override var owner: VerticeView<*>? = null
		set(value) {
			if (field != value) {
				field = value
				ownerRotationChanged()
			}
		}

	private var _port: Port<T> = port
	override var port: Port<T>
		get() = _port
		set(value) {
			invalidate()
			_port.removePropertyChangeListener(portListener)
			_port = value
			_port.addPropertyChangeListener(portListener)
			if (_port.isConnected) {
				length = connectedLength
			}
			modelChanged()
			invalidate()
			update()
		}

	override var direction: Direction = direction
		set(value) {
			if (field != value) {
				invalidate()
				field = value
				modelChanged()
				invalidate()
				update()
			}
		}

	@Suppress("UNUSED_PARAMETER")
	override var ownerRotation: Rotation
		get() = owner?.rotation ?: Rotation.R0
		set(value) {
			invalidate()
			ownerRotationChanged()
			invalidate()
			update()
		}

	override val connectionPoint: Point2D get() = Point2D(connectionPointX, connectionPointY)

	override val unconnectedConnectionPoint: Point2D
		get() = Point2D(
			location.x + unconnectedLength * direction.dx,
			location.y + unconnectedLength * direction.dy)

	override var connectionGeometry: EdgeViewConnectionGeometry? = null

	override var coincidenceWarning: Boolean = false

	/** Listens for property changes of [port] and updates this [PortView] accordingly.*/
	private val portListener = PortListener()

	private val connectionPointX: Double get() = location.x + length * direction.dx

	private val connectionPointY: Double get() = location.y + length * direction.dy

	/** Caches the (static) [Tooltip] (if any) created by [getTooltip]. */
	private val tooltip = resettableLazy {
		io.antarescircuit.jabbah.draw.view.buildToolTipText(
			buildToolTipTitle(),
			buildToolTipContent(),
			null
		)?.let {
			Tooltip(it, Rectangle2D.pointLike(owner!!.getPortConnectionPoint(this.port)))
		}
	}

	init {
		port.addPropertyChangeListener(portListener)
	}

	/** ---- [Cloneable] */

	override fun doClone(): PortView<T> {
		return StorableCloner.clone(PortViewStorable(this)).portView!!
	}

	/** ---- [PortView] interface */

	override fun reuseFrom(portView: PortView<*>) {
		length = portView.length
	}

	override fun dispose() {
		port.removePropertyChangeListener(portListener)
	}

	override fun setLocation(x: Double, y: Double) {
		location = Point2D(x, y)
	}

	override fun <T: InputEventContext> getExecutionTooltip(context: T): Tooltip? {
		val valueText = when (port.portType) {
			PortType.INPUT -> (port as InputPort<*>).incomingSignalDescription
			PortType.OUTPUT -> (port as OutputPort<*>).outgoingSignalDescription
			PortType.INOUT -> {
				val p = port as BidirectionalPort<*>
				"${Translations.getString("graph.property.portType.input")}: ${p.incomingSignalDescription ?: ""}, ${Translations.getString("graph.property.portType.output")}: ${p.outgoingSignalDescription ?: ""}"
			}
		}
		val content = StringBuilder(buildToolTipContent())
		if (StringUtils.isNotEmpty(valueText)) {
			content.appendLine()
			content.append("${Translations.getString("graph.currentValue.name")}: $valueText")
		}
		return io.antarescircuit.jabbah.draw.view.buildToolTipText(buildToolTipTitle(), content.toString(), null)?.let {
			Tooltip(it, Rectangle2D.pointLike(owner!!.getPortConnectionPoint(port)))
		}
	}

	override fun setPortName(name: String) {
		invalidate()
		port.name = name
		invalidate()
		update()
	}

	override fun containsConnectionPoint(x: Double, y: Double): Boolean
		= connectionPoint.isNear(x, y, BaseModule.properties.getInt(PROP_SENSITIVE_AREA))

	override fun handleConnect(edgeView: EdgeView<T>, geometry: EdgeViewConnectionGeometry) {
		invalidate()
		connectionGeometry = geometry
		length = connectedLength
		invalidate()
		update()
	}

	override fun handleUnconnect(edgeView: EdgeView<T>?, lockEndpoint: Boolean) {
		invalidate()
		connectionGeometry = null
		length = unconnectedLength
		if (edgeView != null && !lockEndpoint) {
			val connectionPoint = owner!!.getPortConnectionPoint(port)
			if (edgeView.origin?.port === port) {
				edgeView.moveOriginEndPoint(connectionPoint.x, connectionPoint.y)
			} else if (edgeView.destination?.port === port) {
				edgeView.moveDestinationEndPoint(connectionPoint.x, connectionPoint.y)
			}
		}
		invalidate()
		update()
	}

	protected fun updateLength() {
		val newLength = if (_port.isConnected) {
			connectedLength
		} else {
			unconnectedLength
		}
		if (newLength != length) {
			invalidate()
			length = newLength
			invalidate()
			update()
		}
	}

	override fun edgeViewUpdated(edgeView: EdgeView<*>, geometry: EdgeViewConnectionGeometry) { }

	/** ---- [SnappableX] interface */

	/** Delegate to owner to apply translation and rotation.*/
	override val x: Double get() = owner!!.getUnconnectedPortConnectionPoint(port).x

	override fun accept(other: SnappableX): Boolean =
		other is PortView<*>
			&& (isVerticallyPointingTowardsCompatible(other) || isVerticallyAlignedWithAlike(other))

	private fun isVerticallyPointingTowardsCompatible(other: PortView<*>): Boolean =
		relativeDirection.isVertical()
			&& port.portType.isCompatibleWith(other.port.portType)
			&& relativeDirection == other.relativeDirection.opposite()
			&& sign(other.y - y).toInt() == relativeDirection.dy

	private fun isVerticallyAlignedWithAlike(other: PortView<*>): Boolean =
		relativeDirection.isHorizontal()
			&& relativeDirection == other.relativeDirection
			&& x == other.x

	/** ---- [SnappableY] interface */

	/** Delegate to owner to apply translation and rotation.*/
	override val y: Double get() = owner!!.getUnconnectedPortConnectionPoint(port).y

	override fun accept(other: SnappableY): Boolean =
		other is PortView<*>
			&& (isHorizontallyPointingTowardsCompatible(other) || isHorizontallyAlignedWithAlike(other))

	private fun isHorizontallyPointingTowardsCompatible(other: PortView<*>): Boolean =
		relativeDirection.isHorizontal()
			&& port.portType.isCompatibleWith(other.port.portType)
			&& relativeDirection == other.relativeDirection.opposite()
			&& sign(other.x - x).toInt() == relativeDirection.dx

	private fun isHorizontallyAlignedWithAlike(other: PortView<*>): Boolean =
		relativeDirection.isVertical()
			&& relativeDirection == other.relativeDirection
			&& y == other.y

	/** ---- [Any] */

	override fun toString(): String = "${super.toString()} for Port $port"

	/** ---- [Drawable] interface */

	override fun <T: InputEventContext> getTooltip(context: T): Tooltip? =
		if (coincidenceWarning) {
			Tooltip("Warning: Pin coincides with other pin without\nconnection", Rectangle2D.pointLike(owner!!.getPortConnectionPoint(port)))
		} else {
			tooltip.value?.also { it.sourceRect = Rectangle2D.pointLike(owner!!.getPortConnectionPoint(port)) }
		}

	/** ---- [Mirrorable] interface */

	override fun mirrorHorizontally(x: Double) {
		invalidate()
		location = location.mirrorHorizontally(x)
		direction = direction.mirrorHorizontally()
		modelChanged()
		invalidate()
		update()
	}

	override fun mirrorVertically(y: Double) {
		invalidate()
		location = location.mirrorVertically(y)
		direction = direction.mirrorVertically()
		modelChanged()
		invalidate()
		update()
	}

	/** ---- [Storable] interface */

	override var isReading: Boolean = false

	override fun write(writer: StoreWriter) {
		writer.writeDouble("x", location.x)
		writer.writeDouble("y", location.y)
		writer.writeString("dir", direction.customName)
		writer.writeString("textPos", portLabelPosition.customName)
		if (internalLabelOrientation != InternalLabelOrientation.Horizontal) {
			writer.writeString("intLabelOrient", internalLabelOrientation.customName)
		}
		if (port is Storable) {
			writer.writeInt("portId", writer.provideIdentity(port as Storable))
		}
	}

	override fun read(reader: StoreReader) {
		location = Point2D(reader.readDouble("x"), reader.readDouble("y"))
		direction = Direction.withName(reader.readString("dir"))
		// There was a bug in the older version that wrote enum names "INTERNAL" to physical files.
		// Use toLower() in order to be able to read these files as well
		portLabelPosition = PortLabelPosition.withName(reader.readString("textPos").lowercase())
		if (reader.hasAttribute("intLabelOrient")) {
			internalLabelOrientation = InternalLabelOrientation.withName(reader.readString("intLabelOrient"))
		}
		if (reader.hasAttribute("portId")) {
			val portId = reader.readInt("portId")
			reader.requestResolution(this, Reference(name = "portRef", referenceId = portId))
		}
	}

	override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) {
		if (reference.name == "portRef") {
			port.removePropertyChangeListener(portListener)
			_port = referenceResolver.getStorable<Storable>(reference.referenceId)!! as Port<T>
			port.addPropertyChangeListener(portListener)
		}
	}

	/** ---- [AbstractPortView] */

	/**
	 * This method is automatically called by [AbstractPortView] whenever a property of the underlying
	 * [Port] model has changed.
	 *
	 * Subclasses can override this method to update their state if it depends on [Port] properties. Invalidation
	 * and validation is handled by the calling method in [PortView]. This implementation does nothing.
	 */
	protected open fun modelChanged() {
		tooltip.reset()
	}

	protected open fun ownerRotationChanged() {
		// empty
	}

	private fun buildToolTipTitle(): String =
		if (StringUtils.isBlank(port.name)) {
			port.portType.richTextName
		} else {
			"${port.portType.richTextName} '${port.name!!}'"
		}

	protected open fun buildToolTipContent(): String {
		val content = StringBuilder(StringUtils.orEmpty(port.description.value))
		content.appendLine()
		content.append("${Translations.getString("graph.property.PortId.name")}: ${port.portId}")
		return content.toString()
	}

	private inner class PortListener : PropertyChangeListener<Any> {
		override fun propertyChanged(e: PropertyChangeEvent<Any>) {
			invalidate()
			modelChanged()
			invalidate()
			update()
		}
	}
}