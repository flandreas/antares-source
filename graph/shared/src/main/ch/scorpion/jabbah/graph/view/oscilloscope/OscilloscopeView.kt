package ch.scorpion.jabbah.graph.view.oscilloscope

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.geom.Dimension2D
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.geom.RectangularShape
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.draw.*
import ch.scorpion.jabbah.draw.container.DrawableContainerImpl
import ch.scorpion.jabbah.draw.drawable.IconButton
import ch.scorpion.jabbah.draw.graphics.Icon
import ch.scorpion.jabbah.edit.EditInputEventContext
import ch.scorpion.jabbah.edit.model.rectangle.RectangularComponent

class OscilloscopeView() : RectangularComponent(shape = Rectangle2D(0, 0, DEF_WIDTH, DEF_HEIGHT)) {

    companion object {
        private val LOG by logger(OscilloscopeView::class)
        private const val DEF_WIDTH = 400
        private const val DEF_HEIGHT = 200
    }

    private val container = DrawableContainerImpl<Drawable>(useLocation = true)

    init {
        container.add(IconButton(RemoveIcon(Dimension2D(20, 20)), Point2D(10, 50)))
        container.add(IconButton(RemoveIcon(Dimension2D(20, 20)), Point2D(10, 80)))
        container.add(IconButton(RemoveIcon(Dimension2D(20, 20)), Point2D(10, 110)))
        container.add(IconButton(AddIcon(Dimension2D(20, 20)), Point2D(10, 140)))
        DrawableOwner(this, container)
    }

    /** ---- [Component] interface */

    override val type: String? get() = Translations.getString("graph.component.oscilloscope")

    override fun <T : InputEventContext> getInputEventHandler(context: T): InputEventHandler<T> {
        return container.getInputEventHandler(context)
    }

    /** ---- [Drawable] */

    override fun draw(context: DrawContext) {
        super.draw(context)
        context.g.drawString("Oscilloscope", x.toInt() + 10, y.toInt() + 20)
        container.draw(context)
    }

    /** ---- [RectangularComponent] */

    override fun setFrame(x: Double, y: Double, width: Double, height: Double) {
        super.setFrame(x, y, width, height)
        container.location = location
    }
}

private class AddIcon(override val dim: Dimension2D) : Icon {

    override fun draw(context: DrawContext, location: Point2D) {
        context.g.drawOval(location.x, location.y, dim.width, dim.height)
        context.g.drawLine(
                location.x + dim.width / 3, location.y + dim.height / 2,
                location.x + 2 * dim.width / 3, location.y + dim.height / 2)
        context.g.drawLine(
                location.x + dim.width / 2, location.y + dim.height / 3,
                location.x + dim.width / 2, location.y + 2 * dim.height / 3)
    }
}

private class RemoveIcon(override val dim: Dimension2D) : Icon {

    override fun draw(context: DrawContext, location: Point2D) {
        context.g.drawOval(location.x, location.y, dim.width, dim.height)
        context.g.drawLine(
                location.x + dim.width / 3, location.y + dim.height / 2,
                location.x + 2 * dim.width / 3, location.y + dim.height / 2)
    }
}