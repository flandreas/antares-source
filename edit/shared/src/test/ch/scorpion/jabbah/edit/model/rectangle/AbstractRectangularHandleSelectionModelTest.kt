package ch.scorpion.jabbah.edit.model.rectangle

import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.edit.AbstractEditIntegrationTest
import ch.scorpion.jabbah.edit.SelectionDrawingStrategy
import ch.scorpion.jabbah.edit.model.image.ImageComponent
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.edit.select.EditSelectModule
import ch.scorpion.jabbah.edit.select.SelectionModelFactoryImpl
import ch.scorpion.jabbah.edit.select.SimpleSelectionModelProvider

abstract class AbstractRectangularHandleSelectionModelTest : AbstractEditIntegrationTest() {

    override fun createEnvironment() {
        EditSelectModule.selectionModelFactory = SelectionModelFactoryImpl()
        EditSelectModule.selectionModelProvider = SimpleSelectionModelProvider(EditSelectModule.selectionModelFactory)

        EditSelectModule.selectionModelFactory.register(
            SelectionDrawingStrategy.REPLACE,
            RectangleComponent::class
        ) { RectangularReplaceSelectionModel(it as AbstractRectangularComponent) }

        EditSelectModule.selectionModelFactory.register(SelectionDrawingStrategy.REPLACE, ImageComponent::class) { RectangularReplaceSelectionModel(it as AbstractRectangularComponent) }

        super.createEnvironment()
    }

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