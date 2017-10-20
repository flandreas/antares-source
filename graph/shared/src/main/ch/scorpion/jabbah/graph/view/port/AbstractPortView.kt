package ch.scorpion.jabbah.graph.view.port

import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.collection.EmptyIterator
import ch.scorpion.jabbah.base.event.PropertyChangeEvent
import ch.scorpion.jabbah.base.event.PropertyChangeListener
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.drawable.AbstractDrawable
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rotation
import ch.scorpion.jabbah.graph.model.*
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.VerticeView
import ch.scorpion.jabbah.graph.view.port.PortView.Companion.PROP_SENSITIVE_AREA
import ch.scorpion.jabbah.io.*
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.SnappableX
import ch.scorpion.jabbah.edit.SnappableY


/**
 * A [PortView] that draws a line that points to one of the four [Direction]s.
 * @param T the type of signal that this [PortView]'s [Port] can consume or produce.
 */
abstract class AbstractPortView<T: Any>(
    port: Port<T>,
    x: Int,
    y: Int,
    direction: Direction,
    open var portLabelPosition: PortLabelPosition = PortLabelPosition.INTERNAL,
    unconnectedLength: Int,
    override val connectable: Boolean = true
) : AbstractDrawable(), PortView<T>, Storable {

    companion object {
        private val LOG by logger(AbstractPortView::class)
    }

    override var location: Point2D = Point2D(x, y)
        set(value) {
            invalidate()
            field = value
            invalidate()
            update()
        }

    override var length: Int = unconnectedLength
        set(value) {
            field = value
            LOG.debug("AbstractPortView: setting length to '$value'")
        }

    override var unconnectedLength: Int = unconnectedLength
        set(value) {
            invalidate()
            field = value
            invalidate()
            update()
        }

    override var owner: VerticeView<*>? = null

    private var _port: Port<T> = port
    override var port: Port<T>
        get() = _port
        set(value) {
            invalidate()
            _port = value
            if (_port.isConnected) {
                length = getConnectedLength()
            }
            modelChanged()
            invalidate()
            update()
        }

    override var direction: Direction = direction
        set(value) {
            invalidate()
            field = value
            modelChanged()
            invalidate()
            update()
        }

    override var ownerRotation: Rotation
        get() = owner?.rotation ?: Rotation.R0
        set(value) {
            invalidate()
            modelChanged()
            invalidate()
            update()
        }

    override val connectionPoint: Point2D get() = Point2D(connectionPointX, connectionPointY)

    override val unconnectedConnectionPoint: Point2D
        get() = Point2D(
            location.x + unconnectedLength * direction.dx,
            location.y + unconnectedLength * direction.dy)

    override var edgeViewWidth: Int = 1

    /** Listens for property changes of [port] and updates this [PortView] accordingly.*/
    private val portListener = PortListener()

    private val connectionPointX: Double get() = location.x + length * direction.dx

    private val connectionPointY: Double get() = location.y + length * direction.dy

    init {
        port.addPropertyChangeListener(portListener)
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

    override fun getExecutionToolTipText(x: Double, y: Double, width: Int?): String? {
        return when(port.portType) {
            PortType.INPUT -> (port as InputPort<*>).getIncomingSignal().toString()
            PortType.OUTPUT -> (port as OutputPort<*>).getOutgoingSignal().toString()
            PortType.INOUT -> {
                val p = port as BidirectionalPort<*>
                return buildToolTipText("Port", "I:${p.getIncomingSignal()}, O:${p.getOutgoingSignal()}", width)
            }
        }
    }

    override fun setPortName(name: String) {
        invalidate()
        port.name = name
        invalidate()
        update()
    }

    override fun containsConnectionPoint(x: Double, y: Double): Boolean {
        val sensArea = BaseModule.properties.getInt(PROP_SENSITIVE_AREA)
        // TODO Use Point#isNear()
        return x >= connectionPointX - sensArea &&
                x <= connectionPointX + sensArea &&
                y >= connectionPointY - sensArea &&
                y <= connectionPointY + sensArea
    }

    override fun handleConnect(edgeView: EdgeView<T>) {
        invalidate()
        edgeViewWidth = edgeView.width
        length = getConnectedLength()
        invalidate()
        update()
    }

    override fun handleUnconnect(edgeView: EdgeView<T>?) {
        invalidate()
        edgeViewWidth = 0
        length = unconnectedLength
        if (edgeView != null) {
            val connectionPoint = owner!!.getPortConnectionPoint(port)
            if (edgeView.originPort === port) {
                edgeView.moveOriginEndPoint(connectionPoint.x, connectionPoint.y)
            } else if (edgeView.destinationPort === port) {
                edgeView.moveDestinationEndPoint(connectionPoint.x, connectionPoint.y)
            }
        }
        invalidate()
        update()
    }

    /** ---- [SnappableX] interface */

    /** Delegate to owner to apply translation and rotation.*/
    override val x: Double get() = owner!!.getPortConnectionPoint(port).x

    override fun accept(other: SnappableX): Boolean {
        return other is PortView<*>
                && relativeDirection.isVertical()
                && relativeDirection == other.relativeDirection.opposite()
                && port.portType.isCompatibleWith(other.port.portType)
    }

    /** ---- [SnappableY] interface */

    /** Delegate to owner to apply translation and rotation.*/
    override val y: Double get() = owner!!.getPortConnectionPoint(port).y

    override fun accept(other: SnappableY): Boolean {
        return other is PortView<*>
                && relativeDirection.isHorizontal()
                && relativeDirection == other.relativeDirection.opposite()
                && port.portType.isCompatibleWith(other.port.portType)
    }

    /** ---- [Any] */

    override fun toString(): String {
        return "${super.toString()} for Port $port"
    }

    /** ---- [Drawable] interface */

    override val canMirror: Boolean get() = true

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

    override fun getToolTipText(x: Double, y: Double, width: Int?): String? {
        return System.get().buildToolTipText(
                buildToolTipTitle(),
                buildToolTipContent(),
                width)
    }

    /** ---- [Storable] interface */

    override var storableId: Int = 0

    override fun write(writer: StoreWriter) {
        writer.writeDouble("x", location.x)
        writer.writeDouble("y", location.y)
        writer.writeString("dir", direction.customName)
        writer.writeString("textPos", portLabelPosition.customName)
        if (port is Storable) {
            writer.writeInt("portId", writer.provideIdentity(port as Storable))
        }
    }

    override fun read(reader: StoreReader) {
        location = Point2D(reader.readDouble("x"), reader.readDouble("y"))
        direction = Direction.withName(reader.readString("dir"))
        // There was a bug in the older version that wrote enum names "INTERNAL" to physical files.
        // Use toLower() in order to be able to read these files as well
        portLabelPosition = PortLabelPosition.withName(reader.readString("textPos").toLowerCase())
        if (reader.hasAttribute("portId")) {
            val portId = reader.readInt("portId")
            reader.requestResolution(this, Reference(name = "portRef", referenceId = portId))
        }
    }

    override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) {
        if (reference.name == "portRef") {
            port.removePropertyChangeListener(portListener)
            _port = referenceResolver.getStorable(reference.referenceId) as Port<T>
            port.addPropertyChangeListener(portListener)
        }
    }

    override fun getStorableChildren(): Iterator<Storable> = EmptyIterator()

    /** ---- [AbstractPortView] */

    /** Returns the length of the line to be used if the [Port] is connected. */
    protected abstract fun getConnectedLength(): Int

    /**
     * This method is automatically called by [AbstractPortView] whenever a property of the underlying
     * [Port] model has changed.
     *
     * Subclasses can override this method to update their state if it depends on [Port] properties. Invalidation
     * and validation is handled by the calling method in [PortView]. This implementation does nothing.
     */
    protected open fun modelChanged() {
        // empty
    }

    protected fun buildToolTipTitle(): String {
        return "${port.portType.toString()} '${port.name}'"
    }

    protected open fun buildToolTipContent(): String {
        return port.description!!
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