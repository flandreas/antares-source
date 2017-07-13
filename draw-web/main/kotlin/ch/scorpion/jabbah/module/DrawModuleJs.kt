package ch.scorpion.jabbah.module

import ch.scorpion.jabbah.base.AbstractModule
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModuleJs
import ch.scorpion.jabbah.draw.Canvas
import ch.scorpion.jabbah.draw.graphics.Graphics2DJs
import ch.scorpion.jabbah.draw.graphics.TextRenderInfo
import ch.scorpion.jabbah.draw.module.DrawModule
import ch.scorpion.jabbah.draw.polyline.PolylineShapeImpl
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import kotlin.browser.document

/**
 * Module definitions for the [ch.scorpion.jabbah.draw] package for the JavaScript target.
 */
object DrawModuleJs : AbstractModule() {

    private val canvas = document.createElement("canvas") as HTMLCanvasElement

    override fun initialize() {
        BaseModuleJs.require()

        DrawModule.polylineShapeFactory = ::PolylineShapeImpl

        DrawModule.textRenderInfoFactory = { text, font ->
            val context = canvas.getContext("2d") as CanvasRenderingContext2D
            context.font = Graphics2DJs.toJsFont(font)
            var metrics = context.measureText(text)

            // Note: The properties of TextMetrics other than 'width' are not yet supported
            // in most of the browsers. Therefore we use a heuristic instead.

            val ascent = 7.0 / 8 * font.size
            val descent = 1.0 / 8 * font.size

            TextRenderInfo(
                    Rectangle2D(
                            0.0,
                            -ascent,
                            metrics.width,
                            ascent + descent),
                    ascent)
        }

        DrawModule.require()
    }
}