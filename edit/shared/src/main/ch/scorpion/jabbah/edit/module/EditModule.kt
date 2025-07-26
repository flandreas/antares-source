package ch.scorpion.jabbah.edit.module

import ch.scorpion.jabbah.base.AbstractModule
import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.module.DrawModule
import ch.scorpion.jabbah.draw.style.Themes
import ch.scorpion.jabbah.edit.CommandManager
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.DrawingViewFactory
import ch.scorpion.jabbah.edit.app.DrawingAppService
import ch.scorpion.jabbah.edit.app.DrawingAppServiceImpl
import ch.scorpion.jabbah.edit.auth.EditAuthModule
import ch.scorpion.jabbah.edit.command.SourcingCommandManager
import ch.scorpion.jabbah.edit.drag.EditDragModule
import ch.scorpion.jabbah.edit.editor.EditEditorModule
import ch.scorpion.jabbah.edit.find.DrawingViewSearch
import ch.scorpion.jabbah.edit.model.CopyPasteService
import ch.scorpion.jabbah.edit.model.DrawingImpl
import ch.scorpion.jabbah.edit.model.DrawingService
import ch.scorpion.jabbah.edit.model.DrawingServiceImpl
import ch.scorpion.jabbah.edit.model.curve.CubicCurveComponent
import ch.scorpion.jabbah.edit.model.curve.EditModuleCurveModule
import ch.scorpion.jabbah.edit.model.curve.QuadCurveComponent
import ch.scorpion.jabbah.edit.model.group.EditModelGroupModule
import ch.scorpion.jabbah.edit.model.group.GroupComponent
import ch.scorpion.jabbah.edit.model.image.ImageComponent
import ch.scorpion.jabbah.edit.model.image.ImageData
import ch.scorpion.jabbah.edit.model.image.ImageIdentification
import ch.scorpion.jabbah.edit.model.image.ImageRepository
import ch.scorpion.jabbah.edit.model.polyline.EditModelPolylineModule
import ch.scorpion.jabbah.edit.model.polyline.PolylineComponent
import ch.scorpion.jabbah.edit.model.rectangle.EditModelRectangleModule
import ch.scorpion.jabbah.edit.model.rectangle.EllipseComponent
import ch.scorpion.jabbah.edit.model.rectangle.RectangleComponent
import ch.scorpion.jabbah.edit.model.rectangle.RoundRectangleComponent
import ch.scorpion.jabbah.edit.model.text.EditModelTextModule
import ch.scorpion.jabbah.edit.model.text.LabelComponent
import ch.scorpion.jabbah.edit.model.text.SimpleTextComponent
import ch.scorpion.jabbah.edit.model.text.Translation
import ch.scorpion.jabbah.edit.properties.ComponentPropertyPanelController
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

    var commandManager: CommandManager = SourcingCommandManager()

	var drawingViewFactory: DrawingViewFactory<Drawing<Component>> = DrawingViewFactory { drawing, contextHolder, displayGlobalMessages, name ->
		DrawingViewImpl(drawing, applicationContextHolder = contextHolder, displayGlobalMessages = displayGlobalMessages, name = name)
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