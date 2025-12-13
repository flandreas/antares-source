package ch.scorpion.antares.view

import ch.scorpion.antares.view.gate.BoxGateView
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rotation
import ch.scorpion.jabbah.base.geom.Rotation.R0
import ch.scorpion.jabbah.base.geom.Rotation.R180
import ch.scorpion.jabbah.base.geom.Rotation.R270
import ch.scorpion.jabbah.base.geom.Rotation.R90
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.graphics.Font
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.model.text.HorizontalAlignment
import ch.scorpion.jabbah.edit.model.text.Label
import ch.scorpion.jabbah.edit.model.text.RotationDisplayStrategy.KEEP_HORIZONTAL
import ch.scorpion.jabbah.edit.model.text.VerticalAlignment
import ch.scorpion.jabbah.graph.model.Vertice

open class OrientableLabeledRectangularVerticeView<T: Vertice>(
    styleProvider: StyleProvider,
    text: String,
    model: T,
) : OrientableRectangularVerticeView<T>(styleProvider, model) {

    /** Represents the supported styles for the [Label] of an [OrientableLabeledRectangularVerticeView].*/
    enum class LabelStyle {

        /**
         * Positions the [Label] within the box by centering it horizontally and placing it at one-third of the
         * height.
         */
        LARGE_CENTERED {
            override fun updateLabel(box: OrientableLabeledRectangularVerticeView<*>) {
                box.internalLabel?.let {
                    it.font = box.symbolFont
                    it.ownerRotation = box.rotation
                    it.horizontalAlignment = HorizontalAlignment.CENTER
                    it.verticalAlignment = VerticalAlignment.CENTER
                    it.location = when (box.rotation) {
                        R0 -> Point2D(box.x + box.width / 2, box.y + box.height / 3)
                        R180 -> Point2D(box.x + box.width / 2, box.y + 2 * box.height / 3)
                        R90 -> Point2D(box.x + 2 * box.width / 3, box.y + box.height / 2)
                        R270 -> Point2D(box.x + box.width / 3, box.y + box.height / 2)
                    }
                }
            }
        },

        SMALL_UPPER_LEFT {
            override fun updateLabel(box: OrientableLabeledRectangularVerticeView<*>) {
                box.internalLabel?.let {
                    it.font = deriveFont(box)
                    it.ownerRotation = box.rotation
                    it.horizontalAlignment = HorizontalAlignment.RIGHT
                    it.verticalAlignment = VerticalAlignment.TOP
                    it.location = Point2D(box.bounds.maxX - SMALL_LABEL_INSET, box.bounds.minY + SMALL_LABEL_INSET)
                }
            }
        };

        companion object {
            const val SMALL_LABEL_INSET = 3
            const val FONT_SIZE_FACTOR = 0.6
        }

        abstract fun updateLabel(box: OrientableLabeledRectangularVerticeView<*>)

        fun deriveFont(box: OrientableLabeledRectangularVerticeView<*>): Font =
            box.font.deriveFont((box.font.size * FONT_SIZE_FACTOR).toInt())
    }

    /** Holds the current [LabelStyle] used for positioning and sizing the label of this [BoxGateView].*/
    var labelStyle: LabelStyle = LabelStyle.LARGE_CENTERED
        set(value) {
            if (field == value) {
                return
            }
            invalidate()
            field = value
            field.updateLabel(this)
            invalidate()
            validate()
        }

    var labelText: String?
        get() = internalLabel?.text
        set(value) {
            internalLabel?.let {
                invalidate()
                internalLabel.text = value ?: ""
                invalidate()
                validate()
            }
        }

    /** The text displayed inside the box representing the name of the [Vertice]. */
    private val internalLabel: Label? = if (StringUtils.isNotEmpty(text)) {
        Label(
            text = text,
            font = font,
            horizontalAlignment = HorizontalAlignment.CENTER,
            verticalAlignment = VerticalAlignment.CENTER,
            location = Point2D.ZERO,
            rotationDisplayStrategy = KEEP_HORIZONTAL
        )
    } else {
        null
    }

    open val symbolFont: Font get() = font

    open val scale: Float = 1f

    /** ---- [Component] */

    override var rotation: Rotation
        get() = super.rotation
        set(value) {
            super.rotation = value
            labelStyle.updateLabel(this)
        }

    /** ---- [OrientableLabeledRectangularVerticeView] */

    fun drawLabelText(context: DrawContext, text: String? = null) {
        if (internalLabel != null) {
            if (text != null) {
                if (internalLabel.text != text) {
                    internalLabel.text = text
                }
            }
            internalLabel.draw(context)
        }
    }
}