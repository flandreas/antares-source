package ch.scorpion.jabbah.edit.helloedit

import ch.scorpion.jabbah.base.module.BaseModuleJs
import ch.scorpion.jabbah.draw.style.StyleRepository
import ch.scorpion.jabbah.draw.view.CanvasJs
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.editor.EditEditorModule
import ch.scorpion.jabbah.edit.module.EditModuleJs
import ch.scorpion.jabbah.edit.view.DrawingViewImpl


fun hello() {

    EditModuleJs.require()

    val drawing = buildDrawing()

    val canvas = CanvasJs("kotlinCanvas", { DrawingViewImpl<Drawing<Component>>(drawing, it)}, StyleRepository.INSTANCE )
    val editor = EditEditorModule.createEditor(canvas.view as DrawingView<Drawing<Component>>)

    canvas.repaint()
}
