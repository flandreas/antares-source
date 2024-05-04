package ch.scorpion.antares.view.input

import ch.scorpion.antares.model.input.Switch
import ch.scorpion.antares.view.Look
import ch.scorpion.antares.view.port.AbstractAntaresPortView
import ch.scorpion.antares.view.port.DigitalPortView
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.geom.RectangularShape
import ch.scorpion.jabbah.base.resettableLazy
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.edit.model.image.ImageIdentification
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.graph.GraphApplicationContext
import ch.scorpion.jabbah.graph.view.port.PortView
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

class ImageSwitchView(
    styleProvider: StyleProvider = DrawStyleModule.styleProvider,
    model: Switch = Switch(),
    onImageUuid: UUID? = null,
    offImageUuid: UUID? = null
) : AbstractSwitchView<Switch>(styleProvider, model) {

    companion object {
        private const val TOGGLE_BASE_RESOURCE_KEY = "library.element.ImageToggle"
        private val TOGGLE_TYPE get() = Translations.getString("$TOGGLE_BASE_RESOURCE_KEY.name")
        private val TOGGLE_TYPE_DESC get() = Translations.getOptionalString("$TOGGLE_BASE_RESOURCE_KEY.desc")

        const val PROP_ICON_PATH = "ch.scorpion.antares.view.input.ImageSwitchView.iconPath"
        private const val DEF_SIZE = Look.SCALE * 6
    }

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
    var portDirection: Direction = Direction.EAST

    /** The factor with which the images are scaled when drawn.*/
    var scale: Double = 0.5
        set(value) {
            invalidate()
            field = value
            updateGeometry()
            update()
        }

    /** ---- UI properties */

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
        modelExchanged(null)
        updateGeometry()
    }

    override fun modelExchanged(oldModel: Switch?) {
        super.modelExchanged(oldModel)
        val portView = DigitalPortView(
            styleProvider = styleProvider,
            port = model.getOutput(),
            direction = portDirection)
        portView.setLocation(-portView.length.toDouble(), 0.0)
        addPortView(portView)
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

    override fun drawImpl(context: DrawContext) {
        super.drawImpl(context)
        if (onImageData.value != null) {
            drawImage(context)
        } else {
            drawEmpty(context)
        }
    }

    override val type: String get() = TOGGLE_TYPE

    override val typeDesc: String? get() = TOGGLE_TYPE_DESC

    private fun drawEmpty(context: DrawContext) {
        context.g.color = context.chooseForeground(transparent.applyTo(color.foregroundColor))
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

        context.g.translate(bounds.x, bounds.y)
        context.g.scale(scale, scale)
        context.g.drawImage(image, 0, 0)
        context.g.scale(1.0 / scale, 1.0 / scale)
        context.g.translate(-bounds.x, -bounds.y)
    }

    /** ---- [ImageSwitchView] */

    private fun updateGeometry() {
        invalidate()
        setBounds(calculateBounds())
        invalidate()
    }

    private fun calculateBounds(): RectangularShape {
        return if (onImageData.value != null) {
            val width = scale * onImageData.value!!.image.width
            val height = scale * onImageData.value!!.image.height
            Rectangle2D(
                -AbstractAntaresPortView.LENGTH - width,
                -height / 2,
                width,
                height
            )
        } else {
            Rectangle2D(-AbstractAntaresPortView.LENGTH - DEF_SIZE, -DEF_SIZE / 2, DEF_SIZE, DEF_SIZE)
        }
    }
}