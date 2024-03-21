package ch.scorpion.jabbah.draw

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.draw.drawable.Locatable
import ch.scorpion.jabbah.draw.drawable.RectangularDrawable
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.FontImpl
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleType

/**
 * Decorates a [View] by displaying [RectangularDrawable]s at fixed positions in the [View]'s
 * overlay container, e.g. a static info centered at the upper boundary of the [View].
 */
class ViewDecorator(private val view: View<*>) {

    companion object {
        private const val INSET = 5.0

        /** Default font for text elements displayed by [ViewDecorator]. */
        val FONT = FontImpl()

        /** Default color of text elements displayed by [ViewDecorator]. */
        val TEXT_COLOR: Color get() = DrawStyleModule.styleProvider.getStyle(StyleType.BACKGROUND).color.textColor
    }

    /**
     * The optional [RectangularDrawable] displayed horizontally centered at the top
     * border of the [View].
     */
    var topCentered: Locatable? = null
        set(value) {
            if (field != null) {
                view.overlayContainer.remove(field!!)
            }
            if (value != null) {
                view.overlayContainer.add(value)
            }
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

    private fun update() {
        if (topCentered != null) {
            updateTopCentered()
        }
        view.overlayContainer.validate()
    }

    private fun updateTopCentered() {
        topCentered!!.location = Point2D(view.width / 2 - topCentered!!.boundingBox.width / 2, INSET)
    }
}