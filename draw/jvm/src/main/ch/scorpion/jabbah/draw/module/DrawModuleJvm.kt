package ch.scorpion.jabbah.draw.module

import ch.scorpion.jabbah.base.AbstractModule
import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.module.BaseModuleJvm
import ch.scorpion.jabbah.draw.graphics.Graphics2DJvm
import ch.scorpion.jabbah.draw.graphics.TextRenderInfo
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

        DrawModule.textRenderInfoFactory = { text, font ->
            val awtFont = java.awt.Font(font.family.javaName, Graphics2DJvm.fromFontStyle(font), font.size)
            val context = FontRenderContext(awtFont.transform, true, true)
            val rect = awtFont.getStringBounds(text, context)
            val lm = awtFont.getLineMetrics(text, context)
            TextRenderInfo(Rectangle2D(rect.x, rect.y, rect.width, rect.height), lm.ascent.toDouble())
        }

        DrawModule.require()

        fillProperties(DrawModule.properties)
    }

    private fun fillProperties(properties: Properties) {
        properties.predefine(AbstractViewAction.PROP_ZOOM_STEP, 1.5f)
    }
}