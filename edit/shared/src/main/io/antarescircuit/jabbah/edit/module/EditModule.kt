package io.antarescircuit.jabbah.edit.module

import io.antarescircuit.jabbah.base.AbstractModule
import io.antarescircuit.jabbah.base.Properties
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.UUID
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.draw.ApplicationContextHolder
import io.antarescircuit.jabbah.draw.graphics.Color
import io.antarescircuit.jabbah.draw.module.DrawModule
import io.antarescircuit.jabbah.draw.style.Themes
import io.antarescircuit.jabbah.edit.CommandManager
import io.antarescircuit.jabbah.edit.Component
import io.antarescircuit.jabbah.edit.Drawing
import io.antarescircuit.jabbah.edit.DrawingView
import io.antarescircuit.jabbah.edit.DrawingViewFactory
import io.antarescircuit.jabbah.edit.app.DrawingAppService
import io.antarescircuit.jabbah.edit.app.DrawingAppServiceImpl
import io.antarescircuit.jabbah.edit.auth.EditAuthModule
import io.antarescircuit.jabbah.edit.command.SourcingCommandManager
import io.antarescircuit.jabbah.edit.drag.EditDragModule
import io.antarescircuit.jabbah.edit.editor.EditEditorModule
import io.antarescircuit.jabbah.edit.find.DrawingViewSearch
import io.antarescircuit.jabbah.edit.model.CopyPasteService
import io.antarescircuit.jabbah.edit.model.DrawingImpl
import io.antarescircuit.jabbah.edit.model.DrawingService
import io.antarescircuit.jabbah.edit.model.DrawingServiceImpl
import io.antarescircuit.jabbah.edit.model.curve.CubicCurveComponent
import io.antarescircuit.jabbah.edit.model.curve.EditModuleCurveModule
import io.antarescircuit.jabbah.edit.model.curve.QuadCurveComponent
import io.antarescircuit.jabbah.edit.model.group.EditModelGroupModule
import io.antarescircuit.jabbah.edit.model.group.GroupComponent
import io.antarescircuit.jabbah.edit.model.image.ImageComponent
import io.antarescircuit.jabbah.edit.model.image.ImageData
import io.antarescircuit.jabbah.edit.model.image.ImageIdentification
import io.antarescircuit.jabbah.edit.model.image.ImageRepository
import io.antarescircuit.jabbah.edit.model.polyline.EditModelPolylineModule
import io.antarescircuit.jabbah.edit.model.polyline.PolylineComponent
import io.antarescircuit.jabbah.edit.model.rectangle.EditModelRectangleModule
import io.antarescircuit.jabbah.edit.model.rectangle.EllipseComponent
import io.antarescircuit.jabbah.edit.model.rectangle.RectangleComponent
import io.antarescircuit.jabbah.edit.model.rectangle.RoundRectangleComponent
import io.antarescircuit.jabbah.edit.model.text.EditModelTextModule
import io.antarescircuit.jabbah.edit.model.text.LabelComponent
import io.antarescircuit.jabbah.edit.model.text.SimpleTextComponent
import io.antarescircuit.jabbah.edit.model.text.Translation
import io.antarescircuit.jabbah.edit.properties.ComponentPropertyPanelController
import io.antarescircuit.jabbah.edit.select.EditSelectModule
import io.antarescircuit.jabbah.edit.snap.EditSnapModule
import io.antarescircuit.jabbah.edit.style.EditTheme
import io.antarescircuit.jabbah.edit.view.AttentionDrawer
import io.antarescircuit.jabbah.edit.view.AttentionDrawerImpl
import io.antarescircuit.jabbah.edit.view.DrawingViewImpl
import io.antarescircuit.jabbah.io.IOModule
import io.antarescircuit.jabbah.io.TypeMap

/**
 * Module definitions for the [io.antarescircuit.jabbah.edit] module.
 */
object EditModule : AbstractModule() {

    var commandManager: CommandManager = SourcingCommandManager()

    var drawingViewFactory: DrawingViewFactory = object : DrawingViewFactory {
        override fun <C : Component, T : Drawing<C>> create(
            drawing: T,
            contextHolder: ApplicationContextHolder?,
            displayGlobalMessages: Boolean,
            name: String
        ): DrawingView<C, T> {
            return DrawingViewImpl(
                drawing,
                applicationContextHolder = contextHolder,
                displayGlobalMessages = displayGlobalMessages,
                name = name
            )
        }
    }

	var drawingViewSearchFactory: () -> DrawingViewSearch = { DrawingViewSearch() }

    /**
     * Creates an [AttentionDrawer] that produces an animation for drawing the attention
     * of the user to a particular location. The optional argument represents the  object requiring attention.
     */
    var attentionDrawerFactory: (Any?) -> AttentionDrawer = { AttentionDrawerImpl() }

	var copyPasteService = CopyPasteService()

    var drawingService: DrawingService = DrawingServiceImpl()

    var drawingAppService: DrawingAppService = DrawingAppServiceImpl()

    var imageRepository: ImageRepository = object : ImageRepository {
        override fun getImage(uuid: UUID): ImageData? = null
        override fun getAllImageIds(): List<ImageIdentification> = emptyList()
    }

    override fun initialize() {
        DrawModule.require()
        EditModelRectangleModule.require()
        EditModelPolylineModule.require()
        EditModelTextModule.require()
	    EditModelGroupModule.require()
		EditModuleCurveModule.require()
        EditSnapModule.require()
	    EditDragModule.require()
        EditSelectModule.require()
        EditEditorModule.require()
	    EditAuthModule.require()

        Translations.addBundle("jabbah-edit")
        Themes.register(EditTheme())

        configureTypeMap(IOModule.typeMap)
	    fillProperties(BaseModule.properties)
    }

    override fun resetDependencies() {
        DrawModule.reset()
        EditModelRectangleModule.reset()
        EditModelPolylineModule.reset()
        EditModelTextModule.reset()
        EditModelGroupModule.reset()
        EditModuleCurveModule.reset()
        EditSnapModule.reset()
        EditDragModule.reset()
        EditSelectModule.reset()
        EditEditorModule.reset()
        EditAuthModule.reset()
    }

    private fun configureTypeMap(typeMap: TypeMap) {
        typeMap.register("drawing", DrawingImpl::class)
        typeMap.register("rectangle", RectangleComponent::class)
        typeMap.register("ellipse", EllipseComponent::class)
        typeMap.register("roundrect", RoundRectangleComponent::class)
        typeMap.register("polyline", PolylineComponent::class)
        //typeMap.register("polylineShape", PolylineShapeImpl::class)
        typeMap.register("label", LabelComponent::class)
        typeMap.register("text", SimpleTextComponent::class)
        typeMap.register("group", GroupComponent::class)
        typeMap.register("quadCurve", QuadCurveComponent::class)
        typeMap.register("cubicCurve", CubicCurveComponent::class)
	    typeMap.register("translation", Translation::class)
        typeMap.register("image", ImageComponent::class)
    }

	private fun fillProperties(properties: Properties) {
		properties.set(AttentionDrawer.PROP_COLOR, Color.ORANGE)
		properties.set(AttentionDrawerImpl.PROP_DURATION, 500.0f)
		properties.set(AttentionDrawerImpl.PROP_MAX_RADIUS, 30.0f)
		properties.set(SourcingCommandManager.PROP_MAX_COMMAND_COUNT_PER_SNAPSHOT, 10)
		properties.set(ComponentPropertyPanelController.PROP_MAX_MULTI_SELECT_COUNT, 8)
	}
}