package ch.scorpion.jabbah.draw

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.draw.drawable.Locatable
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.FontImpl
import ch.scorpion.jabbah.draw.graphics.FontStyle
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleType

/**
 * Decorates a [View] by displaying [Locatable]s at fixed positions in the [View]'s
 * overlay container, e.g. a static info centered at the upper boundary of the [View].
 *
 * [ViewDecorator] expects the [Locatable]s to have their origin defined in the top-left
 * corner of their bounding box.
 */
class ViewDecorator(private val view: View<*>) {

    companion object {
        private const val INSET = 5.0

        /** Default font for text elements displayed by [ViewDecorator]. */
        val FONT = FontImpl()

        val FONT_ITALIC = FONT.deriveFont(FontStyle.ITALIC)

        /** Default color of text elements displayed by [ViewDecorator]. */
        val TEXT_COLOR: Color get() = DrawStyleModule.styleProvider.getStyle(StyleType.BACKGROUND).color.textColor

        val TEXT_COLOR_SUBTLE: Color get() = TEXT_COLOR.between(
            DrawStyleModule.styleProvider.getStyle(StyleType.BACKGROUND).color.backgroundColor)
    }

    /**
     * The optional [Locatable] displayed horizontally centered at the top
     * border of the [View].
     */
    var topCentered: Locatable? = null
        set(value) {
            updateOverlayContainer(field, value)
            field = value
            update()
        }

    /**
     * The optional [Locatable] displayed right-aligned at the bottom
     * border of the [View].
     */
    var bottomRight: Locatable? = null
        set(value) {
            updateOverlayContainer(field, value)
            field = value
            update()
        }

    init {
        view.addPropertyChangeListener {
            if (it.name == View.PROP_CANVAS) {
                view.canvas.addPropertyChangeListener {
                    if (it.name == Canvas.PROP_DIMENSION) {
                        update()
                    }
                }
            }
        }
    }

    private fun updateOverlayContainer(current: Locatable?, new: Locatable?) {
        current?.let { view.overlayContainer.remove(it) }
        new?.let { view.overlayContainer.add(it) }
    }

    private fun update() {
        if (topCentered != null) {
            updateTopCentered()
        }
        if (bottomRight != null) {
            updateBottonRight()
        }
        view.overlayContainer.validate()
    }

    private fun updateTopCentered() {
        topCentered!!.location = Point2D(view.width / 2 - topCentered!!.boundingBox.width / 2, INSET)
    }

    private fun updateBottonRight() {
        bottomRight!!.location = Point2D(
            view.width - bottomRight!!.boundingBox.width - INSET,
            view.height - bottomRight!!.boundingBox.height - INSET)
    }
}