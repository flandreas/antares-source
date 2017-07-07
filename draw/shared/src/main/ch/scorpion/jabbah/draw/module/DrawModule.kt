package ch.scorpion.jabbah.draw.module

import ch.scorpion.jabbah.animation.AnimationModule
import ch.scorpion.jabbah.base.AbstractModule
import ch.scorpion.jabbah.base.exception.UnsupportedOperationException
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.DrawProperties
import ch.scorpion.jabbah.draw.graphics.DrawGraphicsModule
import ch.scorpion.jabbah.draw.graphics.Font
import ch.scorpion.jabbah.draw.graphics.TextRenderInfo
import ch.scorpion.jabbah.draw.polyline.PolylineShape
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.view.DrawViewModule

/**
 * Module definitions for the [ch.scorpion.jabbah.draw] package.
 */
object DrawModule : AbstractModule() {

    var properties: DrawProperties = DrawProperties()

    /**
     * Creates a [PolylineShape] for the specified [Point2D]s. Must be implemented platform-specifically.
     */
    var polylineShapeFactory: (List<Point2D>?) -> PolylineShape = { throw UnsupportedOperationException() }

    /**
     * Creates a [TextRenderInfo] for a particular text [String] and the [Font] to be used for rendering.
     * Must be implemented platform-specifically.
     */
    var textRenderInfoFactory: (String, Font) -> TextRenderInfo = { _, _ -> throw UnsupportedOperationException() }

    override fun initialize() {
        BaseModule.properties = properties

        BaseModule.require()
        DrawGraphicsModule.require()
        DrawStyleModule.require()
        DrawViewModule.require()
        AnimationModule.require()
    }
}