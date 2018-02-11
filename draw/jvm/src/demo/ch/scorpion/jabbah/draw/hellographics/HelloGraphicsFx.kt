package ch.scorpion.jabbah.draw.hellographics

import ch.scorpion.jabbah.base.geom.AffineTransformFx
import ch.scorpion.jabbah.base.module.BaseModuleJvm
import ch.scorpion.jabbah.draw.module.DrawModule
import ch.scorpion.jabbah.draw.style.StyleRepository
import ch.scorpion.jabbah.draw.view.CanvasFx
import ch.scorpion.jabbah.draw.view.ViewImpl
import javafx.application.Application
import javafx.application.Platform
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.layout.StackPane
import javafx.stage.Stage

/**
 * A sample application showing [ch.scorpion.jabbah.draw] capabilities on the JVM target using JavaFX.
 */
class HelloGraphicsFx : Application() {

    companion object {
        @JvmStatic fun main(args: Array<String>) {
            BaseModuleJvm.require()
            DrawModule.require()

            launch(HelloGraphicsFx::class.java)
        }
    }

    override fun start(primaryStage: Stage?) {
        val model = Model()
        val canvas = Canvas()
        val canvasFx = CanvasFx(canvas, { ViewImpl(it, { AffineTransformFx() }) })

        canvasFx.view.addDrawable(model.container)
        canvasFx.view.navigator.setZoomFactor(1.0)

        Controller(canvasFx, model)

        val pane = StackPane()
        pane.children.add(canvas)
        pane.style = "-fx-border-color: black;"

        canvas.widthProperty().bind(pane.widthProperty())
        canvas.heightProperty().bind(pane.heightProperty())

        primaryStage?.let {
            it.title = "Hello JavaFX Graphics"
            it.scene = Scene(pane, 800.0, 600.0)
            it.show()
        }

        val timer = ch.scorpion.jabbah.base.System.SYSTEM!!.createTimer()
        timer.initialize(10, { Platform.runLater { model.animateBall(canvasFx)} })
        timer.start()

        primaryStage!!.setOnCloseRequest { timer.stop() }
    }
}