package ch.scorpion.jabbah.edit.helloedit

import ch.scorpion.jabbah.draw.view.CanvasJvm
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.editor.EditEditorModule
import ch.scorpion.jabbah.edit.module.EditModuleJvm
import ch.scorpion.jabbah.edit.view.DrawingViewImpl
import java.awt.BorderLayout
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.JFrame

/**
 * A sample application showing [ch.scorpion.jabbah.edit] capabilities on the JVM target.
 * Shows some [Component]s that can be selected and manipulated within a [Drawing].
 */
class HelloEditJvm : JFrame() {

    companion object {

        @JvmStatic fun main(args: Array<String>) {
            EditModuleJvm.require()

            val helloEdit = HelloEditJvm()
            helloEdit.addWindowListener(object : WindowAdapter() {
                override fun windowClosing(e: WindowEvent?) {
                    System.exit(0)
                }
            })
            helloEdit.isVisible = true
            helloEdit.canvas.view.navigator.fitMaxNormal()
        }
    }

    private val canvas = CanvasJvm({ DrawingViewImpl(buildDrawing(), it) })

    init {

        EditEditorModule.createEditor(canvas.view as DrawingView<Drawing<Component>>)

        contentPane.layout = BorderLayout()
        contentPane.add(canvas)

        setBounds(100, 100, 800, 600)
        title = "Hello Edit"
    }
}