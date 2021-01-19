package ch.scorpion.jabbah.draw.hellographics

import java.awt.BorderLayout
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.JFrame
import ch.scorpion.jabbah.draw.module.DrawModule
import ch.scorpion.jabbah.draw.style.StyleRepository
import ch.scorpion.jabbah.draw.view.ViewImpl
import ch.scorpion.jabbah.base.geom.AffineTransformJvm
import ch.scorpion.jabbah.draw.view.CanvasJvm
import ch.scorpion.jabbah.base.module.BaseModuleJvm
import ch.scorpion.jabbah.draw.InputEventContext
import kotlin.system.exitProcess

/**
 * A sample application showing [ch.scorpion.jabbah.draw] capabilities on the JVM target.
 */
class HelloGraphicsJvm : JFrame() {

    companion object {
        lateinit var model: Model
        lateinit var canvas: CanvasJvm

        @JvmStatic fun main(args: Array<String>) {
            BaseModuleJvm.require()
            DrawModule.require()
            val helloGraphics = HelloGraphicsJvm()

            helloGraphics.addWindowListener(object : WindowAdapter() {
                override fun windowOpened(e: WindowEvent?) {
                    val timer = ch.scorpion.jabbah.base.System.createTimer()
                    timer.initialize(10) { model.animateBall(canvas)}
	                timer.start()
                }
                override fun windowClosing(e: WindowEvent?) {
                    exitProcess(0)
                }
            })
            helloGraphics.isVisible = true
        }
    }

    init {
        model = Model()
        canvas = CanvasJvm(
	        ViewImpl<InputEventContext>({ AffineTransformJvm() }, null),
	        StyleRepository.INSTANCE)
        canvas.view.addDrawable(model.container)
        canvas.view.navigator.setZoomFactor(1.0)

        Controller(canvas, model)

        contentPane.layout = BorderLayout()
        contentPane.add(canvas)
        setBounds(100, 100, 800, 600)
        title = "Hello Swing Graphics"
    }
}