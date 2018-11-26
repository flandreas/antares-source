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
import ch.scorpion.jabbah.edit.app.CopyPasteUtility
import ch.scorpion.jabbah.edit.app.DrawingService
import ch.scorpion.jabbah.edit.app.DrawingServiceImpl
import ch.scorpion.jabbah.edit.command.CommandManagerImpl
import ch.scorpion.jabbah.edit.editor.EditEditorModule
import ch.scorpion.jabbah.edit.model.DrawingImpl
import ch.scorpion.jabbah.edit.model.curve.EditModuleQuadCurveModule
import ch.scorpion.jabbah.edit.model.curve.QuadCurveComponent
import ch.scorpion.jabbah.edit.model.group.EditModelGroupModule
import ch.scorpion.jabbah.edit.model.group.GroupComponent
import ch.scorpion.jabbah.edit.model.polyline.EditModelPolylineModule
import ch.scorpion.jabbah.edit.model.polyline.PolylineComponent
import ch.scorpion.jabbah.edit.model.rectangle.*
import ch.scorpion.jabbah.edit.model.text.*
import ch.scorpion.jabbah.edit.select.EditSelectModule
import ch.scorpion.jabbah.edit.snap.EditSnapModule
import ch.scorpion.jabbah.edit.style.EditTheme
import ch.scorpion.jabbah.edit.view.AttentionDrawer
import ch.scorpion.jabbah.edit.view.AttentionDrawerImpl
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

    /** Creates an [AttentionDrawer] that produces an animation for drawing the attention of the user to a particular location. */
    val attentionDrawerFactory: () -> AttentionDrawer = { AttentionDrawerImpl() }

    var drawingService: DrawingService = DrawingServiceImpl()

    lateinit var copyPasteUtility: CopyPasteUtility

    override fun initialize() {
        DrawModule.require()
        EditModelRectangleModule.require()
        EditModelPolylineModule.require()
        EditModelTextModule.require()
	    EditModelGroupModule.require()
		EditModuleQuadCurveModule.require()
        EditSnapModule.require()
        EditSelectModule.require()
        EditEditorModule.require()

        Translations.addBundle("jabbah-edit")
        Themes.register(EditTheme())

        configureTypeMap(IOModule.typeMap)
    }

    private fun configureTypeMap(typeMap: TypeMap) {
        typeMap.register("drawing", DrawingImpl::class)
        typeMap.register("rectangle", RectangleComponent::class)
        typeMap.register("ellipse", EllipseComponent::class)
        typeMap.register("roundrect", RoundRectangleComponent::class)
        typeMap.register("polyline", PolylineComponent::class)
        typeMap.register("polylineShape", PolylineShapeImpl::class)
        typeMap.register("label", LabelComponent::class)
        typeMap.register("text", SimpleTextComponent::class)
        typeMap.register("group", GroupComponent::class)
        typeMap.register("quadCurve", QuadCurveComponent::class)
	    typeMap.register("translation", Translation::class)
    }
}