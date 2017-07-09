package ch.scorpion.antares

import ch.scorpion.antares.model.InputCount
import ch.scorpion.antares.model.Logic
import ch.scorpion.antares.model.Trigger
import ch.scorpion.antares.model.output.SevenSegmentDisplayScheme
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignalRepresentation
import ch.scorpion.antares.view.*
import ch.scorpion.antares.view.container.DigitalContainerEditor
import ch.scorpion.antares.view.container.DigitalContainerTreeView
import ch.scorpion.antares.view.module.AntaresViewModule
import ch.scorpion.antares.view.output.LightColor
import com.l2fprod.common.propertysheet.PropertyRendererRegistry
import ch.scorpion.jabbah.base.AbstractModule
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.swing.EnumRenderer
import ch.scorpion.jabbah.draw.view.CanvasJvm
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.DynamicPropertyEditorRegistry
import ch.scorpion.jabbah.edit.module.EditModuleJvm
import ch.scorpion.jabbah.edit.view.DrawingViewImpl
import ch.scorpion.jabbah.graph.library.FileLibraryService
import ch.scorpion.jabbah.graph.library.LibraryHolder
import ch.scorpion.jabbah.graph.library.LibraryImpl
import ch.scorpion.jabbah.graph.library.LibraryModule
import ch.scorpion.jabbah.graph.module.GraphModuleJvm
import ch.scorpion.jabbah.graph.view.container.ContainerDrawing
import ch.scorpion.jabbah.graph.view.container.ContainerEditor
import ch.scorpion.jabbah.graph.view.module.GraphViewModule


/**
 * Module definitions for the [ch.scorpion.antares] module on the JVM target.
 */
class AntaresModuleJvm(val app: Antares) : AbstractModule() {

    override fun initialize() {

        GraphViewModule.containerEditorFactory = { createContainerEditor(it) }

        GraphModuleJvm.containerTreeViewFactory = { DigitalContainerTreeView() }
        GraphModuleJvm.graphNavigationPanelFactory = AntaresGraphNavigationPanelFactory()

        AntaresViewModule.require()
        GraphModuleJvm.require()

        LibraryModule.libraryService = FileLibraryService(app.getLibraryDirectoryPath().toString())
        LibraryModule.libraryFactory = {
            LibraryImpl(app.getLibraryFileName(), app.getLibraryDirectoryPath().toString())
        }
        LibraryModule.libraryHolder = LibraryHolder(LibraryModule.libraryFactory.invoke())

        configurePropertyRenderer(EditModuleJvm.propertyRendererRegistry)
        configurePropertyEditors(EditModuleJvm.propertyEditorRegistry)
    }

    private fun createContainerEditor(eventBus: EventBus): ContainerEditor {
        val containerCanvas = CanvasJvm({
            val drawingView = DrawingViewImpl<Drawing<Component>>(ContainerDrawing(), it)
            drawingView.addDrawableDrawer(DigitalComponentViewDrawer())
            drawingView
        })
        return DigitalContainerEditor(containerCanvas.view as DrawingView<Drawing<Component>>, eventBus)
    }

    private fun configurePropertyRenderer(registry: PropertyRendererRegistry) {
        registry.registerRenderer(ch.scorpion.antares.view.output.LightColor::class.java, LightColorRenderer::class.java)
        registry.registerRenderer(InputCount::class.java, EnumRenderer::class.java)
        registry.registerRenderer(Handedness::class.java, EnumRenderer::class.java)
        registry.registerRenderer(Logic::class.java, EnumRenderer::class.java)
        registry.registerRenderer(Trigger::class.java, EnumRenderer::class.java)
        registry.registerRenderer(BitWidth::class.java, EnumRenderer::class.java)
        registry.registerRenderer(DigitalSignalRepresentation::class.java, EnumRenderer::class.java)
        registry.registerRenderer(SevenSegmentDisplayScheme::class.java, EnumRenderer::class.java)
    }

    private fun configurePropertyEditors(registry: DynamicPropertyEditorRegistry) {
        registry.registerEditor(LightColor::class.java, LightColorEditor::class.java)
        registry.registerEditor(InputCount::class.java, InputCountEditor::class.java)
        registry.registerEditor(Handedness::class.java, HandednessEditor::class.java)
        registry.registerEditor(Logic::class.java, LogicEditor::class.java)
        registry.registerEditor(Trigger::class.java, TriggerEditor::class.java)
        registry.registerEditor(BitWidth::class.java, BitWidthEditor::class.java)
        registry.registerEditor(DigitalSignalRepresentation::class.java, DigitalSignalRepresentationEditor::class.java)
        registry.registerEditor(SevenSegmentDisplayScheme::class.java, SevenSegmentDisplaySchemeEditor::class.java)
    }
}