package ch.scorpion.antares.view.port

import ch.scorpion.antares.model.Logic
import ch.scorpion.antares.model.Trigger
import ch.scorpion.antares.model.port.DigitalPort
import ch.scorpion.antares.model.port.DigitalPortImpl
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.view.Look
import ch.scorpion.antares.view.style.AntaresTheme
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.style.StyleType
import ch.scorpion.jabbah.edit.model.text.Label
import ch.scorpion.jabbah.base.geom.*
import ch.scorpion.jabbah.graph.ApplicationMode
import ch.scorpion.jabbah.graph.model.Port
import ch.scorpion.jabbah.graph.model.PortType
import ch.scorpion.jabbah.graph.view.port.AbstractPortView
import ch.scorpion.jabbah.graph.view.port.PortLabelPosition
import ch.scorpion.jabbah.graph.view.style.GraphStyleType
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.Reference
import ch.scorpion.jabbah.io.ReferenceResolver
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.Math
import ch.scorpion.jabbah.draw.style.Themes
import ch.scorpion.jabbah.graph.view.style.GraphTheme


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
        var predefinedConnectedLength: Int? = null
) : AbstractPortView<DigitalSignal>(port, x, y, direction, portLabelPosition, length ?: LENGTH) {

    companion object {
        val DEBUG_GFX = false
        val LENGTH: Int = 2 * Look.SCALE
        val INT_BORDER_DIST = 5
        val EXT_BORDER_DIST = 4
        val LOGIC_SIZE = (2 * Look.SCALE / 1.7f).toInt()
        val INTERNAL_ANNOTATION_SIZE = (LOGIC_SIZE * 1.25).toInt()

        val EDGE_TRIGGER_PATH: Path = System.get().createPath()
            .moveTo(0.0, -INTERNAL_ANNOTATION_SIZE / 2.0)
            .lineTo(-INTERNAL_ANNOTATION_SIZE, 0)
            .lineTo(0, INTERNAL_ANNOTATION_SIZE / 2)
    }

    /** Determines whether this [DigitalPortView] shows an annotation that indicates the [DigitalPort]'s [BitWidth].*/
    var showBitWidthAnnotation: Boolean = true
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

    /**
     * Should be overwritten by subclasses if the have an internal annotation to be drawn.
     * @return `true` if this [DigitalPortView] has an internal annotation to be drawn
     */
    private val hasInternalAnnotation: Boolean
        get() = (port as DigitalPort).trigger == Trigger.EDGE

    init {
        buildPortLabel()
        buildBitWidthAnnotationLabel()
    }

    /** ---- [AbstractPortView] */

    override fun modelChanged() {
        super.modelChanged()
        buildPortLabel()
        buildBitWidthAnnotationLabel()
    }

    override fun getConnectedLength(): Int {
        if (predefinedConnectedLength != null) {
            return predefinedConnectedLength!!
        }
        if (getDigitalPort().logic == Logic.NEGATIVE) {
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
            buildBitWidthAnnotationLabel()
        }
    }

    /** ---- [Drawable] */

    override fun draw(context: DrawContext) {
        val origColor = context.g.color

        if (ApplicationMode.EXECUTE == context.castedAppContext<ApplicationMode>()) {
            if (port.net == null || !port.net!!.isError) {
                context.g.color = when (port.portType) {
                    PortType.INOUT -> getDigitalPort().dominantSignal.getColor().foregroundColor
                    PortType.INPUT -> getDigitalPort().getIncomingSignal()!!.getColor().foregroundColor
                    PortType.OUTPUT -> getDigitalPort().getOutgoingSignal()!!.getColor().foregroundColor
                }
            }
        } else {
            if (context.useContextColors) {
                context.g.color = context.color!!.foregroundColor
            } else {
                context.g.color = styleProvider.getStyle(GraphStyleType.EDGE).color.foregroundColor
            }
        }

        if (!port.isConnected) {
            if (getDigitalPort().bitWidth.width > 1) {
                context.g.stroke = Themes.get<GraphTheme>().edge.busStroke
            } else {
                context.g.stroke = Themes.get<GraphTheme>().edge.stroke
            }

            val connPoint = connectionPoint
            context.g.drawLine(locationX.toInt(), locationY.toInt(), connPoint.x.toInt(), connPoint.y.toInt())
        }

        context.g.translate(locationX, locationY)

        if (bitWidthAnnotation != null && showBitWidthAnnotation) {
            bitWidthAnnotation!!.draw(context)
        }

        drawLogic(context)

        context.g.color = context.choose(styleProvider.getStyle(GraphStyleType.ANNOTATION).color).foregroundColor
        if (hasInternalAnnotation) {
            drawInternalAnnotation(context)
        }

        portLabel?.let {
            if (portLabelPosition == PortLabelPosition.EXTERNAL) {
                context.g.color = context.choose(styleProvider.getStyle(GraphStyleType.EDGE).color).textColor
            } else {
                context.g.color = context.choose(styleProvider.getStyle(GraphStyleType.ANNOTATION).color).textColor
            }
            portLabel?.draw(context)
        }

        context.g.translate(-locationX, -locationY)

        if (DEBUG_GFX) {
            context.g.color = Color.YELLOW
            context.g.draw(boundingBox)
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
            bbox.add(location.toRect(1.0))
            bbox.add(connectionPoint.toRect(1.0))
            return bbox
        }

    override fun contains(x: Double, y: Double): Boolean {
        return boundingBox.contains(x, y)
    }

    override fun getToolTipText(x: Double, y: Double, width: Int?): String? {
        return System.get().buildToolTipText(
            buildToolTipText(),
            StringBuilder()
                .append("<p/>")
                .append("BitWidth: ")
                .append(getDigitalPort().bitWidth.width)
                .toString(),
            width
        )
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

    /** ---- [DigitalPortView] */

    private fun getDigitalPort(): DigitalPort {
        return port as DigitalPort
    }

    private fun buildBitWidthAnnotationLabel() {
        if (getDigitalPort().bitWidth != BitWidth.BW_1) {
            bitWidthAnnotation = BitWidthAnnotation(getDigitalPort().bitWidth, direction)
        } else {
            bitWidthAnnotation = null
        }
    }

    private fun buildPortLabel() {
        portLabel = when(portLabelPosition) {
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
            location = getInternalLabelLocation(direction))
    }

    private fun buildExternalLabel(port: Port<DigitalSignal>): Label {
        val rotation: Rotation = when(direction) {
            Direction.NORTH -> Rotation.R90
            Direction.SOUTH -> Rotation.R90
            else -> Rotation.R0
        }
        return Label(
            horizontalAlignment = getHorizontalExternalLabelAlignment(direction),
            verticalAlignment = Label.VerticalAlignment.BOTTOM,
            font = Look.EXT_PIN_FONT,
            text = port.name,
            location = getExternalLabelLocation(direction),
            rotation = rotation)
    }

    private fun getHorizontalInternalLabelAlignment(direction: Direction): Label.HorizontalAlignment {
        when (direction) {
            Direction.WEST -> return Label.HorizontalAlignment.LEFT
            Direction.EAST -> return Label.HorizontalAlignment.RIGHT
            Direction.NORTH -> return Label.HorizontalAlignment.CENTER
            Direction.SOUTH -> return Label.HorizontalAlignment.CENTER
            else -> throw IllegalStateException("unknown Direction " + direction)
        }
    }

    private fun getHorizontalExternalLabelAlignment(direction: Direction): Label.HorizontalAlignment {
        when (direction) {
            Direction.WEST -> return Label.HorizontalAlignment.RIGHT
            Direction.EAST -> return Label.HorizontalAlignment.LEFT
            Direction.NORTH -> return Label.HorizontalAlignment.LEFT
            Direction.SOUTH -> return Label.HorizontalAlignment.RIGHT
            else -> throw IllegalStateException("unknown Direction " + direction)
        }
    }

    private fun getVerticalInternalLabelAlignment(direction: Direction): Label.VerticalAlignment {
        when (direction) {
            Direction.WEST -> return Label.VerticalAlignment.CENTER
            Direction.EAST -> return Label.VerticalAlignment.CENTER
            Direction.NORTH -> return Label.VerticalAlignment.TOP
            Direction.SOUTH -> return Label.VerticalAlignment.BOTTOM
            else -> throw IllegalStateException("unknown Direction " + direction)
        }
    }

    private fun getInternalLabelLocation(direction: Direction): Point2D {
        val ia = if (hasInternalAnnotation) INTERNAL_ANNOTATION_SIZE else 0
        when (direction) {
            Direction.WEST -> return Point2D(INT_BORDER_DIST + ia, 0)
            Direction.EAST -> return Point2D(-INT_BORDER_DIST - ia, 0)
            Direction.NORTH -> return Point2D(0, INT_BORDER_DIST + ia)
            Direction.SOUTH -> return Point2D(0, -INT_BORDER_DIST - ia)
            else -> throw IllegalStateException("unknown Direction " + direction)
        }
    }

    private fun getExternalLabelLocation(direction: Direction): Point2D {
        when (direction) {
            Direction.WEST -> return Point2D(-EXT_BORDER_DIST, 0)
            Direction.EAST -> return Point2D(EXT_BORDER_DIST, 0)
            Direction.NORTH -> return Point2D(0, -EXT_BORDER_DIST)
            Direction.SOUTH -> return Point2D(0, EXT_BORDER_DIST)
            else -> throw IllegalStateException("unknown Direction " + direction)
        }
    }

    private fun drawLogic(context: DrawContext) {
        if (getDigitalPort().logic == Logic.NEGATIVE) {
            val x1 = 0
            val x2 = LOGIC_SIZE * direction.dx + LOGIC_SIZE * direction.next().dx
            val y1 = 0
            val y2 = LOGIC_SIZE * direction.dy + LOGIC_SIZE * direction.next().dy

            var logicX = Math.min(x1, x2)
            var logicY = Math.min(y1, y2)

            logicX += LOGIC_SIZE * direction.previous().dx / 2
            logicY += LOGIC_SIZE * direction.previous().dy / 2

            context.g.color = context.choose(styleProvider.getStyle(StyleType.BACKGROUND).color).backgroundColor
            context.g.fillOval(logicX, logicY, LOGIC_SIZE, LOGIC_SIZE)

            context.g.color = context.choose(styleProvider.getStyle(GraphStyleType.VERTICE).color).foregroundColor
            context.g.drawOval(logicX, logicY, LOGIC_SIZE, LOGIC_SIZE)
        }
    }

    /**
     * Draws the internal annotation of this [DigitalPortView], if any.
     * This method it automatically called if [hasInternalAnnotation] returns `true`.
     */
    private fun drawInternalAnnotation(context: DrawContext) {
        if (hasInternalAnnotation) {
            val angle = direction.rotation.angle
            context.g.rotate(angle)
            context.g.draw(EDGE_TRIGGER_PATH)
            context.g.rotate(-angle)
        }
    }
}