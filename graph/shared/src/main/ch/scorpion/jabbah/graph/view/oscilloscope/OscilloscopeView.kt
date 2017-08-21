package ch.scorpion.jabbah.graph.view.oscilloscope

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.module.DrawModule
import ch.scorpion.jabbah.edit.model.rectangle.RectangularComponent

class OscilloscopeView() : RectangularComponent(shape = Rectangle2D(0, 0, DEF_WIDTH, DEF_HEIGHT)) {

    companion object {
        private const val DEF_WIDTH = 400
        private const val DEF_HEIGHT = 200
    }

    private val image = DrawModule.imageLoader.invoke("/img/pointer.gif")

    /** ---- [Component] interface */

    override val type: String? get() = Translations.getString("graph.component.oscilloscope")

    /** ---- [RectangularComponent] */

    override fun draw(context: DrawContext) {
        super.draw(context)
        context.g.drawString("Oscilloscope", x.toInt() + 10, y.toInt() + 20)

        // TEST BEGIN
        context.g.drawImage(image, x.toInt() + 20,y.toInt() + 50)
        // TEST END
    }
}