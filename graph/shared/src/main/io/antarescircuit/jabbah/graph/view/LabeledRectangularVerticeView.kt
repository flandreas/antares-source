package io.antarescircuit.jabbah.graph.view

import io.antarescircuit.jabbah.base.StringUtils
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.geom.*
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.graphics.Font
import io.antarescircuit.jabbah.draw.style.StyleProvider
import io.antarescircuit.jabbah.draw.style.StyleType
import io.antarescircuit.jabbah.edit.Look
import io.antarescircuit.jabbah.edit.model.text.HorizontalAlignment
import io.antarescircuit.jabbah.edit.model.text.HorizontalLabel
import io.antarescircuit.jabbah.edit.model.text.Label
import io.antarescircuit.jabbah.edit.model.text.Labeled
import io.antarescircuit.jabbah.edit.model.text.RotationDisplayStrategy
import io.antarescircuit.jabbah.edit.model.text.VerticalAlignment
import io.antarescircuit.jabbah.graph.model.Vertice
import io.antarescircuit.jabbah.graph.view.vertice.AbstractRectangularVerticeView
import io.antarescircuit.jabbah.graph.view.vertice.AbstractVerticeView
import io.antarescircuit.jabbah.io.Storable
import io.antarescircuit.jabbah.io.StoreReader
import io.antarescircuit.jabbah.io.StoreWriter

/**
 * Base class for implementing [VerticeView]s with an external label and an optional internal label.
 *
 * This class reacts to [rotationChanged] in order to update the external label's location and to
 * always keep it horizontal, which is implemented with the help of [HorizontalLabel]. The internal
 * label is not rotated so that it always fits into the [VerticeView]'s geometry, except for
 * a 180 degree's rotation to prevent it from being upside down.
 */
abstract class LabeledRectangularVerticeView<T : Vertice>(
    styleProvider: StyleProvider,
    model: T,
    rectangle: MutableRectangularShape = Rectangle2D(),
    internalLabelText: String? = null,
    private val eventBus: EventBus = BaseModule.eventBus
) : OrientableRectangularVerticeView<T>(styleProvider, model, rectangle), Labeled, InternallyLabeled {

    companion object {
        protected const val LABEL_DIST = Look.SCALE
    }

    /** UI property for editing the model's name, which is displayed as external label.*/
    var name: String?
        get() = model.name
        set(value) {
            if (value != name) {
                model.name = value
                updateLabels()
                if (this is ControlViewSource<*>) {
                    postControlViewSourceChangeEvent(eventBus)
                }
            }
        }

    override fun modelExchanged(oldModel: T?) {
        super.modelExchanged(oldModel)
        updateLabels()
    }

    /** ---- Common label management */

    protected fun updateLabels() {
        invalidate()

        updateExternalLabel()
        updateInternalLabel()

        invalidate()
        update()
    }

    /** ---- [Labeled] interface and external label management */

    protected lateinit var externalLabel: HorizontalLabel

    override val label: Label get() = externalLabel.label

    /**
     * The factor by which the label gets scaled up or down when drawn.
     * Used for implementing extensions of this class with varying sizes.
     */
    open val labelScale: Float = 1f

    /**
     * Returns the location of the [externalLabel] relative to this [VerticeView]'s location.
     * Must be re-evaluated every time this [VerticeView] changes its geometry, but NOT if it is only rotated.
     */
    protected abstract val relativeExternalLabelLocation: Point2D

    /** UI property for showing/hiding the external label. Primarily used for [ControlView]s. */
    var showExternalLabel: Boolean = true
        set(value) {
            if (field != value) {
                invalidate()
                field = value
                invalidate()
                update()
            }
        }

    /** Must be called by the subclass constructor to initialize the instance of external [HorizontalLabel]. */
    protected fun initExternalLabel(
        orientation: Direction? = Direction.EAST,
        text: String? = null
    ) {
        externalLabel = HorizontalLabel(this, relativeExternalLabelLocation, orientation, text, font)
    }

    private fun updateExternalLabel() {
        invalidate()
        externalLabel.text = StringUtils.orEmpty(name)
        externalLabel.update()
        invalidate()
        update()
    }

    /** ---- [InternallyLabeled] and internal label management */

    var internalLabelStyle: InternalLabelStyle? = if (StringUtils.isNotEmpty(internalLabelText)) InternalLabelStyle.LARGE_CENTERED else null
        set(value) {
            if (field == value) {
                return
            }
            invalidate()
            field = value
            field?.updateLabel(this)
            invalidate()
            validate()
        }

    var internalLabelText: String?
        get() = internalLabel?.text
        set(value) {
            internalLabel?.let {
                invalidate()
                internalLabel!!.text = value ?: ""
                invalidate()
                validate()
            }
        }

    /** The text displayed inside the box representing the fixed text inside the [VerticeView]. */
    override val internalLabel: Label? = if (StringUtils.isNotEmpty(internalLabelText)) {
        Label(
            text = internalLabelText,
            font = font,
            horizontalAlignment = HorizontalAlignment.CENTER,
            verticalAlignment = VerticalAlignment.CENTER,
            location = Point2D.Companion.ZERO,
            rotationDisplayStrategy = RotationDisplayStrategy.ROTATE_HALF
        )
    } else {
        null
    }

    override val internalLabelFont: Font get() = font

    protected open fun updateInternalLabel() {
        // By default, the internal label is static and doesn't need to be updated and doesn't change the geometry.
        // Subclasses whose internal label displays the name can override this method
    }

    /** ---- [LabeledRectangularVerticeView] */

    /**
     * Called by the subclass when its geometry has changed, and [relativeExternalLabelLocation] must be re-evaluated
     * to adjust the external [HorizontalLabel]'s position to the new geometry.
     */
    protected open fun updateGeometry() {
        externalLabel.relLocation = relativeExternalLabelLocation
    }

    /** ---- [AbstractVerticeView] */

    override val boundingBox: RectangularShape
        get() {
            if (showExternalLabel) {
                val bb = Rectangle2D(super.boundingBox)
                val lbb = Rectangle2D(externalLabel.boundingBox).moveBy(location)
                bb.add(lbb)
                return bb
            } else {
                return super.boundingBox
            }
        }

    override fun draw(context: DrawContext) {
        super.draw(context)
        if (showExternalLabel) {
            drawExternalLabel(context)
        }
    }

    protected open fun drawExternalLabel(context: DrawContext) {
        context.g.color = styleProvider.getStyle(StyleType.BACKGROUND).color.textColor
        externalLabel.draw(context)
    }

    open fun drawInternalLabel(context: DrawContext, text: String? = null) {
        internalLabel?.let { label ->
            if (text != null) {
                if (label.text != text) {
                    label.text = text
                }
            }
            label.draw(context)
        }
    }

    /** ---- [AbstractRectangularVerticeView] */

    override fun rotationChanged(newRotation: Rotation) {
        super.rotationChanged(newRotation)
        externalLabel.update()
        internalLabelStyle?.updateLabel(this)
    }

    override fun drawSelected(context: DrawContext) {
        if (showExternalLabel) {
            externalLabel.draw(context)
        }
    }

    /** ---- [Storable] interface */

    override fun read(reader: StoreReader) {
        super.read(reader)
        if (reader.hasAttribute("showExternalLabel")) {
            showExternalLabel = reader.readBoolean("showExternalLabel")
        }
    }

    override fun write(writer: StoreWriter) {
        super.write(writer)
        if (!showExternalLabel) {
            writer.writeBoolean("showExternalLabel", showExternalLabel)
        }
    }
}