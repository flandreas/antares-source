package ch.scorpion.jabbah.draw.hellographics

import java.awt.BorderLayout
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.JFrame
import ch.scorpion.jabbah.draw.module.DrawModule
import ch.scorpion.jabbah.draw.style.StyleRepository
import ch.scorpion.jabbah.draw.view.ViewImpl
import ch.scorpion.jabbah.base.geom.Dimension2D
import ch.scorpion.jabbah.base.geom.AffineTransformJvm
import ch.scorpion.jabbah.draw.view.CanvasJvm
import ch.scorpion.jabbah.base.module.BaseModuleJvm

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
                    val timer = ch.scorpion.jabbah.base.System.SYSTEM!!.createTimer()
                    timer.initialize(10, { model.moveBall(canvas)})
                    timer.start()
                }
                override fun windowClosing(e: WindowEvent?) {
                    System.exit(0)
                }
            })
            helloGraphics.isVisible = true
        }
    }

    init {
        model = Model(Dimension2D(800.0, 600.0), 100)
        canvas = CanvasJvm({ ViewImpl(it, { AffineTransformJvm() }) }, StyleRepository.INSTANCE)
        canvas.view.addDrawable(model.container)
        canvas.view.navigator.setZoomFactor(1.0)

        Controller(canvas, model)

        contentPane.layout = BorderLayout()
        contentPane.add(canvas)
        setBounds(100, 100, 800, 600)
        title = "Hello Graphics"
    }
}