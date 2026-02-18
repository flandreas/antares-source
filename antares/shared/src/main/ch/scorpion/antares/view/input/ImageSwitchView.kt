package ch.scorpion.antares.view.input

import ch.scorpion.antares.model.input.Switch
import ch.scorpion.jabbah.edit.Look
import ch.scorpion.antares.view.port.AbstractAntaresPortView.Companion.LENGTH
import ch.scorpion.antares.view.port.DigitalPortView
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Direction.*
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.geom.RectangularShape
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.resettableLazy
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.graphics.DropShadow
import ch.scorpion.jabbah.draw.graphics.Image
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.edit.model.image.ImageIdentification
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.graph.GraphApplicationContext
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.model.vertice.VerticeLink
import ch.scorpion.jabbah.graph.view.ControlView
import ch.scorpion.jabbah.graph.view.ControlViewSource
import ch.scorpion.jabbah.graph.view.port.PortView
import ch.scorpion.jabbah.graph.view.vertice.AbstractVerticeView
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter
import kotlin.math.max

/**
 * A switch with an [Image] for "on" and "off" state.
 * Draws a dummy rectangle if one of the two [Image]s is not set.
 */
class ImageSwitchView(
    styleProvider: StyleProvider = DrawStyleModule.styleProvider,
    model: Switch = Switch(),
    onImageUuid: UUID? = null,
    offImageUuid: UUID? = null,
    portDirection: Direction = EAST,
    scale: Double = 0.5,
    private val eventBus: EventBus = BaseModule.eventBus
) : AbstractSwitchView<Switch>(styleProvider, model),
    ControlViewSource<Switch>,
    ControlView<Switch>
{

    companion object {
        private const val TOGGLE_BASE_RESOURCE_KEY = "library.element.ImageToggle"
        private val TOGGLE_TYPE get() = Translations.getString("$TOGGLE_BASE_RESOURCE_KEY.name")
        private val TOGGLE_TYPE_DESC get() = Translations.getOptionalString("$TOGGLE_BASE_RESOURCE_KEY.desc")

        const val PROP_ICON_PATH = "ch.scorpion.antares.view.input.ImageSwitchView.iconPath"
        private const val DEF_SIZE = Look.SCALE * 6
    }

    @Suppress("MemberVisibilityCanBePrivate") // Reflection
    var onImageUuid: UUID? = onImageUuid
        set(value) {
            if (field != value) {
                invalidate()
                field = value
                onImageData.reset()
                updateGeometry()
                validate()
            }
        }

    private val onImageData = resettableLazy {
        this.onImageUuid?.let {
            EditModule.imageRepository.getImage(it)
        }
    }

    @Suppress("MemberVisibilityCanBePrivate") // Reflection
    var offImageUuid: UUID? = offImageUuid
        set(value) {
            if (field != value) {
                invalidate()
                field = value
                offImageData.reset()
                updateGeometry()
                validate()
            }
        }

    private val offImageData = resettableLazy {
        this.offImageUuid?.let {
            EditModule.imageRepository.getImage(it)
        }
    }

    /** The [Direction] in which the output [PortView] faces relative to normal image rotation.*/
    var portDirection: Direction = portDirection
        set(value) {
            if (field != value) {
                invalidate()
                field = value
                updateGeometry()
                validate()
            }
        }

    /** The factor with which the images are scaled when drawn.*/
    var scale: Double = scale
        set(value) {
            invalidate()
            field = value
            updateGeometry()
            update()
        }

    /** ---- UI properties */

    override var toggle: Boolean
        get() = super.toggle
        set(value) {
            if (value != super.toggle) {
                super.toggle = value
                postControlViewSourceChangeEvent(eventBus)
            }
        }

    var minOnTime: Long
		get() = model.minOnTime
		set(value) {
			model.minOnTime = value
		}

    @Suppress("unused") // Reflection
    var onImageId: ImageIdentification?
        get() = onImageData.value?.let { ImageIdentification(uuid = onImageUuid!!, name = it.name) }
        set(value) {
            onImageUuid = value?.uuid
        }

    @Suppress("unused") // Reflection
    var offImageId: ImageIdentification?
        get() = offImageData.value?.let { ImageIdentification(uuid = offImageUuid!!, name = it.name) }
        set(value) {
            offImageUuid = value?.uuid
        }

    init {
        isFocusable = true
        modelExchanged(null)
    }

    override fun modelExchanged(oldModel: Switch?) {
        super.modelExchanged(oldModel)
        val portView = DigitalPortView(
            styleProvider = styleProvider,
            port = model.getOutput(),
            direction = portDirection)
        addPortView(portView)
        updateGeometry()
    }

    override fun drawSelected(context: DrawContext) {
        context.g.color = context.color!!.foregroundColor
        draw(context) {
            super.drawImpl(it)
            context.g.stroke = stroke
            context.g.draw(bounds)
        }
    }

    /** ---- [AbstractSwitchView] */

    override fun updateLabels() {}

    /** ---- [Storable] */

    override fun read(reader: StoreReader) {
        super.read(reader)
        portDirection = Direction.withName(reader.readString("portDirection"))
        scale = reader.readDouble("scale")
        if (reader.hasAttribute("onImageUuid")) {
            onImageUuid = UUID(reader.readString("onImageUuid"))
        }
        if (reader.hasAttribute("offImageUuid")) {
            offImageUuid = UUID(reader.readString("offImageUuid"))
        }
        updateGeometry()
    }

    override fun write(writer: StoreWriter) {
        super.write(writer)
        writer.writeString("portDirection", portDirection.customName)
        writer.writeDouble("scale", scale)
        onImageUuid?.let { writer.writeString("onImageUuid", it.toString()) }
        offImageUuid?.let { writer.writeString("offImageUuid", it.toString()) }
    }

    /** ---- [AbstractVerticeView] */

    override val type: String get() = TOGGLE_TYPE

    override val typeDesc: String? get() = TOGGLE_TYPE_DESC

    override fun drawImpl(context: DrawContext) {
        super.drawImpl(context)

        if (shadow) {
            DropShadow.draw(context, transparency) {
                context.g.fillRect(xInt, yInt, widthInt, heightInt)
            }
        }

        if (onImageData.value != null && offImageData.value != null) {
            drawImage(context)
        } else {
            drawEmpty(context)
        }

        val appContext = context.castedAppContext<GraphApplicationContext>()!!
        if (appContext.isExecute) {
            drawFocus(context)
        }
    }

    private fun drawEmpty(context: DrawContext) {
        context.g.color = context.chooseForeground(transparent.applyTo(foregroundColor))
        context.g.stroke = stroke
        context.g.draw(bounds)
        context.g.drawLine(x, y, x + width, y + height)
        context.g.drawLine(x, y + height, x + width, y)
    }

    private fun drawImage(context: DrawContext) {
        val appContext = context.castedAppContext<GraphApplicationContext>()!!

        val image = if (appContext.isExecute && model.isOn) {
            onImageData.value!!.image
        } else {
            offImageData.value!!.image
        }

        context.translated(bounds.topLeft) {
            it.g.scale(scale, scale)
            it.g.drawImage(image, 0, 0)
            it.g.scale(1.0 / scale, 1.0 / scale)
        }
    }

    /** ---- [ControlViewSource] */

    override val controlId: String get() = "imageSwitch:" + model.id

    override val iconPath: String get() = BaseModule.properties.getString(PROP_ICON_PATH)

    override fun createControlView(): ControlView<Switch> {
        val clone = ImageSwitchView(styleProvider, model, onImageUuid, offImageUuid, portDirection, scale)
        clone.isShowPortViews = false
        clone.location = Point2D.ZERO
        return clone
    }

    private fun copyControlViewProperties(source: ImageSwitchView, dest: ImageSwitchView) {
        dest.toggle = source.toggle
        dest.name = source.name
        dest.onImageId = source.onImageId
        dest.offImageId = source.offImageId
        dest.portDirection = source.portDirection
        dest.scale = source.scale
    }

    /** ---- [ControlView] */

    override var isActiveControlView: Boolean = false

    override val controlName: String
        get() = ControlViewSource.getControlName(type, id, model.name)

    override fun bindControlView(subGraphVerticeView: SubGraphVerticeView<*>, link: VerticeLink, startGraph: Graph) {
        this.model = link.getLinkedObject(startGraph) as Switch
    }

    override fun writeModelProperties(writer: StoreWriter) {}

    override fun readModelProperties(reader: StoreReader) {}

    override fun sourcePropertiesChanged(source: ControlViewSource<Switch>) {
        if (source is ImageSwitchView) {
            copyControlViewProperties(source, this)
        }
    }

    /** ---- [ImageSwitchView] */

    private val effWidth: Double get() =
        if (onImageData.value == null || offImageData.value == null) {
            DEF_SIZE.toDouble()
        } else {
            max(scale * onImageData.value!!.image.width, scale * offImageData.value!!.image.width)
        }

    private val effHeight: Double get() =
        if (onImageData.value == null || offImageData.value == null) {
            DEF_SIZE.toDouble()
        } else {
            max(scale * onImageData.value!!.image.height, scale * offImageData.value!!.image.height)
        }

    private fun updateGeometry() {
        invalidate()
        getPortView(model.getPort())?.let {
            it.direction = portDirection
            it.location = when (portDirection) {
                EAST -> Point2D(-LENGTH, 0)
                NORTH -> Point2D(0, LENGTH)
                WEST -> Point2D(LENGTH, 0)
                SOUTH -> Point2D(0, -LENGTH)
            }
        }
        setBounds(calculateBounds())
        invalidate()
    }

    private fun calculateBounds(): RectangularShape {
        val w = effWidth
        val h = effHeight
        return calculateBoxCorner(w, h).let {
            Rectangle2D(it.x, it.y, w, h)
        }
    }

    private fun calculateBoxCorner(w: Double, h: Double): Point2D {
        return when (portDirection) {
            EAST -> Point2D(-LENGTH.toDouble() - w, -h / 2)
            NORTH -> Point2D(-w / 2, LENGTH.toDouble())
            WEST -> Point2D(LENGTH.toDouble(), -h / 2)
            SOUTH -> Point2D(-w / 2, -LENGTH - h)
        }
    }
}