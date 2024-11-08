package ch.scorpion.jabbah.draw.graphics

import com.github.weisj.jsvg.SVGDocument
import com.github.weisj.jsvg.attributes.ViewBox
import java.awt.Graphics2D
import java.awt.RenderingHints.*

/** Renders an [SVGDocument] using a [Graphics2D]. */
class SvgImageJvm(
    private val svgDocument: SVGDocument
) : Image {

    /** ---- [Image] */

    override val width: Int = svgDocument.size().width.toInt()

    override val height: Int = svgDocument.size().height.toInt()

    /** ---- [SvgImageJvm] */

    private val viewBox = ViewBox(0f, 0f, width.toFloat(), height.toFloat())

    fun draw(g: Graphics2D) {
        val oldAntialiasing = g.getRenderingHint(KEY_ANTIALIASING)
        val oldKeyStrokeControl = g.getRenderingHint(KEY_STROKE_CONTROL)

        g.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON)
        g.setRenderingHint(KEY_STROKE_CONTROL, VALUE_STROKE_PURE)

        svgDocument.render(null, g, viewBox)

        g.setRenderingHint(KEY_ANTIALIASING, oldAntialiasing)
        g.setRenderingHint(KEY_STROKE_CONTROL, oldKeyStrokeControl)
    }
}