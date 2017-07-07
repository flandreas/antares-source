package ch.scorpion.jabbah.module

import ch.scorpion.jabbah.base.AbstractModule
import ch.scorpion.jabbah.base.module.BaseModuleJs
import ch.scorpion.jabbah.draw.module.DrawModule
import ch.scorpion.jabbah.draw.polyline.PolylineShapeImpl

/**
 * Module definitions for the [ch.scorpion.jabbah.draw] package for the JavaScript target.
 */
object DrawModuleJs : AbstractModule() {

    override fun initialize() {
        BaseModuleJs.require()

        DrawModule.polylineShapeFactory = ::PolylineShapeImpl
        DrawModule.require()
    }
}