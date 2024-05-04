package ch.scorpion.jabbah.edit.model.image

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.base.resettableLazy
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.drawable.Rotatable
import ch.scorpion.jabbah.draw.graphics.Image
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.model.rectangle.RectangleComponent
import ch.scorpion.jabbah.edit.model.text.description.Name
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreWriter

/**
 * A rectangular [Image] whose size can be proportionally changed by the user.
 * If the [Image] is (not yet) set, draws itself as a simple [RectangleComponent] with a cross.
 *
 * @property uuid the [UUID] in [ImageRepository]
 */
class ImageComponent(
    uuid: UUID? = null
) : RectangleComponent() {

    companion object {
        private const val DEF_WIDTH = 100.0
        private const val DEF_HEIGHT = 50.0
    }

    var uuid: UUID? = uuid
        set(value) {
            if (field != value) {
                invalidate()

                field = value
                imageData.reset()
                updateGeometry()

                invalidate()
                update()
            }
        }

    private val imageData = resettableLazy {
        this.uuid?.let {
            EditModule.imageRepository.getImage(it)
        }
    }

    val name: Name? get() = imageData.value?.name

    init {
        initializeGeometry()
    }

    /** ---- [Rotatable] */

    override val useRotation: Boolean get() = true

    /** ---- [Drawable] */

    override val type: String by lazy { Translations.getString("edit.component.image") }

    /** ---- [Storable] */

    override fun read(reader: StoreReader) {
        super.read(reader)
        uuid = UUID(reader.readString("uuid"))
    }

    override fun write(writer: StoreWriter) {
        super.write(writer)
        writer.writeString("uuid", uuid.toString())
    }

    /** ---- [RectangleComponent] */

    override val maintainAspectRation: Boolean get() = true

    override fun drawShape(context: DrawContext, strokeColor: Color?, fillColor: Color?) {
        if (imageData.value != null) {
            drawImage(context)
        } else {
            super.drawShape(context, strokeColor, fillColor)
            if (strokeColor != null) {
                context.g.color = strokeColor
                context.g.stroke = stroke
                context.g.drawLine(x, y, x + width, y + height)
                context.g.drawLine(x, y + height, x + width, y)
            }
        }
    }

    /** Used when [ImageComponent] is drawn within a selected outer [Component]*/
    fun drawSelected(context: DrawContext) {
        context.g.color = context.color!!.foregroundColor
        super.drawShape(context, context.color!!.foregroundColor, null)
    }

    private fun drawImage(context: DrawContext) {
        val scaleX = width / imageData.value!!.image.width
        val scaleY = height / imageData.value!!.image.height

        context.g.translate(x, y)
        context.g.scale(scaleX, scaleY)
        context.g.drawImage(imageData.value!!.image, 0, 0)
        context.g.scale(1 / scaleX, 1 / scaleY)
        context.g.translate(-x, -y)
    }

    private fun initializeGeometry() {
        if (imageData.value != null) {
            setFrame(x, y, imageData.value!!.image.width.toDouble(), imageData.value!!.image.height.toDouble())
        } else {
            setFrame(x, y, DEF_WIDTH, DEF_HEIGHT)
        }
    }

    private fun updateGeometry() {
        if (imageData.value != null) {
            setFrame(x, y, width, height)
        } else {
            setFrame(x, y, DEF_WIDTH, DEF_HEIGHT)
        }
    }
}