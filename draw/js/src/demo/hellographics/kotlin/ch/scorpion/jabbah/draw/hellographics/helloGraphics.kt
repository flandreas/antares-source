package ch.scorpion.jabbah.draw.hellographics

import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.geom.AffineTransformImpl
import ch.scorpion.jabbah.base.geom.Dimension2D
import ch.scorpion.jabbah.base.module.BaseModuleJs
import ch.scorpion.jabbah.draw.module.DrawModule
import ch.scorpion.jabbah.draw.style.StyleRepository
import ch.scorpion.jabbah.draw.view.CanvasJs
import ch.scorpion.jabbah.draw.view.ViewImpl
import ch.scorpion.jabbah.draw.view.ZoomPanController

fun hello() {

    BaseModuleJs.require()
    DrawModule.require()

    val model = Model(Dimension2D(800.0, 600.0), 50)

    val canvas = CanvasJs("kotlinCanvas", { ViewImpl(it, { AffineTransformImpl() }) }, StyleRepository.INSTANCE)
    canvas.view.addDrawable(model.container)
    canvas.view.navigator.setZoomFactor(1.0)

    Controller(canvas, model)

    canvas.paint()

    val timer = System.SYSTEM!!.createTimer()
    timer.initialize(20, { model.animateBall(canvas)} )
    timer.start()
}
