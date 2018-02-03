package ch.scorpion.jabbah.edit.helloedit

import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.SystemJvm
import ch.scorpion.jabbah.base.geom.AffineTransformFx
import ch.scorpion.jabbah.draw.view.CanvasFx
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.editor.EditEditorModule
import ch.scorpion.jabbah.edit.module.EditModuleJvm
import ch.scorpion.jabbah.edit.view.DrawingViewImpl
import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.layout.StackPane
import javafx.stage.Stage

class HelloEditFx : Application() {

    companion object {
        @JvmStatic fun main(args: Array<String>) {
            EditModuleJvm.require()
            System.SYSTEM = SystemJvm(useJavaFX = true)

            launch(HelloEditFx::class.java)
        }
    }

    override fun start(primaryStage: Stage?) {
        val canvas = Canvas()
        val canvasFx = CanvasFx(canvas, { DrawingViewImpl(buildDrawing(), it, { AffineTransformFx() }) })

        val pane = StackPane()
        pane.children.add(canvas)

        canvas.widthProperty().bind(pane.widthProperty())
        canvas.heightProperty().bind(pane.heightProperty())

        EditEditorModule.createEditor(canvasFx.view as DrawingView<Drawing<Component>>)

        primaryStage?.let {
            it.title = "Hello JavaFX Edit"
            it.scene = Scene(pane, 800.0, 600.0)
            it.show()
        }
    }
}