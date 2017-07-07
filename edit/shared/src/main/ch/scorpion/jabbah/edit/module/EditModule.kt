package ch.scorpion.jabbah.edit.module

import ch.scorpion.jabbah.base.AbstractModule
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.exception.UnsupportedOperationException
import ch.scorpion.jabbah.draw.Canvas
import ch.scorpion.jabbah.draw.module.DrawModule
import ch.scorpion.jabbah.draw.polyline.PolylineShapeImpl
import ch.scorpion.jabbah.draw.style.Themes
import ch.scorpion.jabbah.edit.CommandManager
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.command.CommandManagerImpl
import ch.scorpion.jabbah.edit.editor.EditEditorModule
import ch.scorpion.jabbah.edit.model.DrawingImpl
import ch.scorpion.jabbah.edit.model.polyline.EditModelPolylineModule
import ch.scorpion.jabbah.edit.model.polyline.PolylineComponent
import ch.scorpion.jabbah.edit.model.rectangle.EditModelRectangleModule
import ch.scorpion.jabbah.edit.model.rectangle.RectangularComponent
import ch.scorpion.jabbah.edit.model.text.LabelComponent
import ch.scorpion.jabbah.edit.model.text.TextComponentFactory
import ch.scorpion.jabbah.edit.select.EditSelectModule
import ch.scorpion.jabbah.edit.snap.EditSnapModule
import ch.scorpion.jabbah.edit.style.EditTheme
import ch.scorpion.jabbah.edit.view.DrawingViewImpl
import ch.scorpion.jabbah.io.IOModule
import ch.scorpion.jabbah.io.TypeMap

/**
 * Module definitions for the [ch.scorpion.jabbah.edit] module.
 */
object EditModule : AbstractModule() {

    var commandManager: CommandManager = CommandManagerImpl()

    var textComponentFactory: () -> TextComponentFactory = { throw UnsupportedOperationException() }

    var drawingViewFactory: (Drawing<Component>, Canvas) -> DrawingView<Drawing<Component>> = { drawing, canvas -> DrawingViewImpl<Drawing<Component>>(drawing, canvas) }

    override fun initialize() {
        DrawModule.require()
        EditModelRectangleModule.require()
        EditModelPolylineModule.require()
        EditSnapModule.require()
        EditSelectModule.require()
        EditEditorModule.require()

        configureTypeMap(IOModule.typeMap)

        Translations.addBundle("jabbah-edit")
        Themes.register(EditTheme())
    }

    private fun configureTypeMap(typeMap: TypeMap) {
        typeMap.register("drawing", DrawingImpl::class)
        typeMap.register("rectangle", RectangularComponent::class)
        typeMap.register("polyline", PolylineComponent::class)
        typeMap.register("polylineShape", PolylineShapeImpl::class)
        typeMap.register("label", LabelComponent::class)
    }
}