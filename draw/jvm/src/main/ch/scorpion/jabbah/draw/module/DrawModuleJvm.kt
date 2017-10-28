package ch.scorpion.jabbah.draw.module

import ch.scorpion.jabbah.base.AbstractModule
import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.module.BaseModuleJvm
import ch.scorpion.jabbah.draw.graphics.*
import ch.scorpion.jabbah.draw.polyline.PolylineShapeJvm
import ch.scorpion.jabbah.draw.view.AbstractViewAction
import java.awt.font.FontRenderContext

/**
 * Setup of the [ch.scorpion.jabbah.draw] module for the JVM target.
 */
object DrawModuleJvm : AbstractModule() {

    override fun initialize() {
        BaseModuleJvm.require()

        DrawModule.polylineShapeFactory = ::PolylineShapeJvm

        DrawModule.textRenderInfoFactory = TextRenderInfoFactoryJvm()

        DrawModule.imageLoader = { ImageJvm(it) }

        DrawModule.require()

        fillProperties(DrawModule.properties)
    }

    private fun fillProperties(properties: Properties) {
        properties.set(AbstractViewAction.PROP_ZOOM_STEP, 1.5f)
    }
}

private class TextRenderInfoFactoryJvm : TextRenderInfoFactory {

    override fun measureSingleLineText(text: String, font: Font): TextRenderInfo {
        val awtFont = java.awt.Font(font.family.javaName, Graphics2DJvm.fromFontStyle(font), font.size)
        val context = FontRenderContext(awtFont.transform, true, true)
        val rect = awtFont.getStringBounds(text, context)
        val lm = awtFont.getLineMetrics(text, context)
        return TextRenderInfo(Rectangle2D(rect.x, rect.y, rect.width, rect.height), lm.ascent.toDouble())
    }

    override fun measureHtmlText(text: String, font: Font, width: Int): TextRenderInfo {
        return Graphics2DJvm.measureHtmlText(text, Graphics2DJvm.toAwtFont(font), width)
    }
}