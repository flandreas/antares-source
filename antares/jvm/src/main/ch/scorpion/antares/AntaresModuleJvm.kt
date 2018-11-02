package ch.scorpion.antares

import ch.scorpion.antares.model.*
import ch.scorpion.antares.model.net.BranchCount
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
import ch.scorpion.jabbah.edit.*
import ch.scorpion.jabbah.edit.model.text.TextComponentJvm
import ch.scorpion.jabbah.edit.module.EditModuleJvm
import ch.scorpion.jabbah.edit.view.DrawingViewImpl
import ch.scorpion.jabbah.graph.module.GraphModuleJvm
import ch.scorpion.jabbah.graph.container.ContainerDrawing
import ch.scorpion.jabbah.graph.container.ContainerEditor
import ch.scorpion.jabbah.graph.library.*
import ch.scorpion.jabbah.graph.project.FileProjectService
import ch.scorpion.jabbah.graph.project.ProjectModule
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.io.IOModule
import ch.scorpion.jabbah.io.TypeMap

/**
 * Module definitions for the [ch.scorpion.antares] module on the JVM target.
 */
class AntaresModuleJvm(private val app: Antares) : AbstractModule() {

	override fun initialize() {

		GraphViewModule.containerEditorFactory = { createContainerEditor(it) }

		GraphModuleJvm.containerTreeViewFactory = { DigitalContainerTreeView() }
		GraphModuleJvm.graphNavigationPanelFactory = AntaresGraphNavigationPanelFactory()

		GraphModuleJvm.require()
		AntaresViewModule.require()

		LibraryModule.libraryPersistenceService = FileLibraryPersistenceService(
			directoryPath = app.libraryDirectoryPath.toString(),
			metaGraphFileExtension = app.fileExtension,
			libraryFileName = app.libraryFileName
		)
		LibraryModule.libraryFactory = AntaresLibraryFactory()
		LibraryModule.libraryHolder = LibraryHolder(LibraryModule.libraryFactory.createEmptyLibrary("Standard"))

		ProjectModule.projectLibraryPersistenceService = FileLibraryPersistenceService(
			directoryPath = app.projectsDirectoryPath.toString(),
			metaGraphFileExtension = app.fileExtension,
			libraryFileName = app.libraryFileName
		)
		ProjectModule.projectService = FileProjectService(
			directoryPath = app.projectsDirectoryPath.toString(),
			newMetaGraphNameTranslationKey = "graph.name.unknown"
		)

		LibraryModule.libraryDictionary = FileLibraryDictionary(
			directoryPath = app.libraryDirectoryPath.toString()
		)
		LibraryModule.libraryDictionary.load()

		LibraryModule.libraryManagementService = FileLibraryManagementService(
			defaultLibraryName = "Standard",
			directoryPath = app.libraryDirectoryPath.toString(),
			projectService = ProjectModule.projectService
		)


		configureTypeMap(IOModule.typeMap)
		configurePropertyRenderer(EditModuleJvm.propertyRendererRegistry)
		configurePropertyEditors(EditModuleJvm.propertyEditorRegistry)
	}

	private fun createContainerEditor(eventBus: EventBus): ContainerEditor {
		val containerCanvas = CanvasJvm {
			val drawingView = DrawingViewImpl<Drawing<Component>>(ContainerDrawing(), it)
			drawingView.addDrawableDrawer(DigitalComponentViewDrawer())
			drawingView
		}
		return DigitalContainerEditor(containerCanvas.view as DrawingView<Drawing<Component>>, eventBus)
	}

	private fun configureTypeMap(typeMap: TypeMap) {
		typeMap.register("text", TextComponentJvm::class)
	}

	private fun configurePropertyRenderer(registry: PropertyRendererRegistry) {
		registry.registerRenderer(ch.scorpion.antares.view.output.LightColor::class.java, LightColorRenderer::class.java)
		registry.registerRenderer(InputCount::class.java, EnumRenderer::class.java)
		registry.registerRenderer(InputPortNumber::class.java, EnumRenderer::class.java)
		registry.registerRenderer(Handedness::class.java, EnumRenderer::class.java)
		registry.registerRenderer(Logic::class.java, EnumRenderer::class.java)
		registry.registerRenderer(Trigger::class.java, EnumRenderer::class.java)
		registry.registerRenderer(BitWidth::class.java, EnumRenderer::class.java)
		registry.registerRenderer(DigitalSignalRepresentation::class.java, EnumRenderer::class.java)
		registry.registerRenderer(SevenSegmentDisplayScheme::class.java, EnumRenderer::class.java)
		registry.registerRenderer(OutputAnnotation::class.java, EnumRenderer::class.java)
	}

	private fun configurePropertyEditors(registry: DynamicPropertyEditorRegistry) {
		registry.registerEditor(LightColor::class.java, LightColorEditor::class.java)
		registry.register(InputCount::class.java) { InputCountEditor((it as PropertyImpl<InputCount>).filter) }
		registry.register(InputPortNumber::class.java) { InputPortNumberEditor((it as PropertyImpl<InputPortNumber>).filter) }
		registry.registerEditor(Handedness::class.java, HandednessEditor::class.java)
		registry.registerEditor(Logic::class.java, LogicEditor::class.java)
		registry.registerEditor(Trigger::class.java, TriggerEditor::class.java)
		registry.registerEditor(BitWidth::class.java, BitWidthEditor::class.java)
		registry.register(BranchCount::class.java) { BranchCountEditor((it as PropertyImpl<BranchCount>).filter) }
		registry.registerEditor(DigitalSignalRepresentation::class.java, DigitalSignalRepresentationEditor::class.java)
		registry.registerEditor(SevenSegmentDisplayScheme::class.java, SevenSegmentDisplaySchemeEditor::class.java)
		registry.registerEditor(OutputAnnotation::class.java, OutputAnnotationEditor::class.java)
	}
}