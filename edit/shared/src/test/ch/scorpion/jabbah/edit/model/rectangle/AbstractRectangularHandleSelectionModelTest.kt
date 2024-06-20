package ch.scorpion.jabbah.edit.model.rectangle

import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.edit.AbstractEditIntegrationTest
import ch.scorpion.jabbah.edit.EditorToolDriver
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.edit.select.EditSelectModule
import ch.scorpion.jabbah.edit.select.SelectionModelFactoryImpl
import ch.scorpion.jabbah.edit.select.SimpleSelectionModelProvider

abstract class AbstractRectangularHandleSelectionModelTest : AbstractEditIntegrationTest() {

    companion object {
        init {
            EditSelectModule.selectionModelFactory = SelectionModelFactoryImpl()
            EditSelectModule.selectionModelProvider = SimpleSelectionModelProvider(EditSelectModule.selectionModelFactory)

            EditModelRectangleModule.reset()
            EditModelRectangleModule.require()
        }
    }

    protected val driver = EditorToolDriver(editor)

    protected fun addSelectedRect(x: Int, y: Int, width: Int, height: Int): RectangleComponent {
        val rect = EditModule.drawingAppService.add(
            createRect(x, y, width, height),
            editor.view
        ) as RectangleComponent
        editor.view.selectionManager.select(rect)
        return rect
    }

    protected open fun createRect(x: Int, y: Int, width: Int, height: Int): RectangleComponent =
        RectangleComponent(shape = Rectangle2D(x, y, width, height))
}