package ch.scorpion.jabbah.graph.module

import ch.scorpion.jabbah.app.module.AppModuleJvm
import ch.scorpion.jabbah.base.AbstractModule
import ch.scorpion.jabbah.base.DataLocation
import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.module.BaseModuleJvm
import ch.scorpion.jabbah.base.preferences.*
import ch.scorpion.jabbah.draw.module.DrawModuleJvm
import ch.scorpion.jabbah.edit.BeanProvider
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.auth.EditAuthModule
import ch.scorpion.jabbah.edit.module.EditModuleJvm
import ch.scorpion.jabbah.edit.properties.AbstractReflectionPropertySwing
import ch.scorpion.jabbah.edit.properties.CommandPropertySwing
import ch.scorpion.jabbah.edit.properties.DynamicPropertyEditorRegistry
import ch.scorpion.jabbah.edit.view.DynamicPropertyRendererRegistry
import ch.scorpion.jabbah.execution.ExecutionModuleJvm
import ch.scorpion.jabbah.graph.container.ContainerDrawingLayouter
import ch.scorpion.jabbah.graph.container.ContainerTreeView
import ch.scorpion.jabbah.graph.library.FileMetaGraphHistoryService
import ch.scorpion.jabbah.graph.library.LibraryTreeViewActionsParams
import ch.scorpion.jabbah.graph.library.LibraryTreeViewActionsSwing
import ch.scorpion.jabbah.graph.library.UnimplementedFileMetaGraphHistoryService
import ch.scorpion.jabbah.graph.model.graph.LongGraphParamType
import ch.scorpion.jabbah.graph.model.graph.StringGraphParamType
import ch.scorpion.jabbah.graph.model.param.*
import ch.scorpion.jabbah.graph.model.port.InconsistentNetError
import ch.scorpion.jabbah.graph.project.ProjectAkrabClientServiceJvm
import ch.scorpion.jabbah.graph.ui.GraphContextMenuProvider
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.module.GraphViewModuleJvm

/**
 * Module definitions for the [ch.scorpion.jabbah.graph] module on the JVM platform.
 */
object GraphModuleJvm : AbstractModule() {

	val supportWeb: Boolean get() = EditAuthModule.userHolder.user.isDeveloper

	var containerTreeViewFactory: () -> ContainerTreeView = { ContainerTreeView() }

	var projectAkrabClientServiceJvm: () -> ProjectAkrabClientServiceJvm = { throw UnsupportedOperationException() }

	// Tried a function interface, but the Java obfuscator didn't like it
	var libraryTreeViewActionsProvider: (LibraryTreeViewActionsParams) -> LibraryTreeViewActionsSwing =
		{ params -> LibraryTreeViewActionsSwing(params.controller, params.type, params.application) }

	var metaGraphHistoryService: FileMetaGraphHistoryService = UnimplementedFileMetaGraphHistoryService()

	override fun initialize() {
		BaseModuleJvm.require()
		DrawModuleJvm.require()
		ExecutionModuleJvm.require()
		AppModuleJvm.require()
		GraphViewModuleJvm.require()

		DrawModuleJvm.contextMenuProvider = GraphContextMenuProvider()

		configurePropertyRenderer(EditModuleJvm.propertyRendererRegistry)
		configurePropertyEditors(EditModuleJvm.propertyEditorRegistry)
		configureGraphParamValueProperties()

		fillProperties(BaseModule.properties)

		buildPreferencesTree(BaseModuleJvm.preferencesTree)
	}

	private fun configurePropertyRenderer(registry: DynamicPropertyRendererRegistry) {
		registry.register(GraphParamDefinitions::class.java) { GraphParamDefinitionsPropertyRenderer() }
	}

	private fun configurePropertyEditors(registry: DynamicPropertyEditorRegistry) {
		registry.register(GraphParamDefinitions::class.java) {
			GraphParamDefinitionsPropertyEditor(
				propertyName = (it as CommandPropertySwing<GraphParamDefinitions>).displayName,
				editable = it.editable,
				graph = (it.editor!!.drawing as GraphView).graph!!
			)
		}
	}

	private fun configureGraphParamValueProperties() {
		GraphParamValuePropertyFactoryRegistry.register(
			StringGraphParamType,
			object : GraphParamValuePropertyFactory {
				override fun create(
					def: GraphParamDefinition<*>,
					editor: Editor,
					beanProvider: BeanProvider
				): AbstractReflectionPropertySwing<*> {
					return GraphParamValuePropertySwing(
						paramDefinition = def as GraphParamDefinition<String>,
						propertyName = "<notUsed>",
						baseKey ="element.property.bitWidth", // TODO: This must be a dynamic name and not a resource key
						valueClass = String::class.java,
						beanProvider
					)
				}
			}
		)

		GraphParamValuePropertyFactoryRegistry.register(
			LongGraphParamType,
			object : GraphParamValuePropertyFactory {
				override fun create(
					def: GraphParamDefinition<*>,
					editor: Editor,
					beanProvider: BeanProvider
				): AbstractReflectionPropertySwing<*> {
					return GraphParamValuePropertySwing(
						paramDefinition = def as GraphParamDefinition<Long>,
						propertyName = "<notUsed>",
						baseKey ="element.property.bitWidth", // TODO: This must be a dynamic name and not a resource key
						valueClass = Long::class.java,
						beanProvider
					)
				}
			}
		)
	}

	private fun fillProperties(properties: Properties) {
		properties.set(FileMetaGraphHistoryService.PREF_META_GRAPH_HISTORY, true)
		properties.set(ContainerDrawingLayouter.PROP_CONTAINER_DRAWING_LAYOUTER, ContainerDrawingLayouter.Narrow.customName)
	}

	private fun buildPreferencesTree(root: PreferenceGroup) {

		if (supportWeb) {
			root.getGroup(BaseModuleJvm.PREF_TREE_GENERAL).add(EnumPreference(
				id = DataLocation.PROP_DATA_LOCATION,
				nameKey = "base.preferences.dataLocation",
				values = DataLocation.values(),
				withName = DataLocation::withName,
				needsRestart = true
			))
			root.getGroup(BaseModuleJvm.PREF_TREE_GENERAL).add(
				StringPreference(
				id = DataLocation.PROP_SERVER_URL,
				nameKey = "base.preferences.serverUrl",
				columns = 15
			))
		}

		root.getGroup(ExecutionModuleJvm.PREF_TREE_EXECUTION).add(IntPreference(
			id = InconsistentNetError.PROP_ALLOWED_DURATION,
			nameKey = "graph.preferences.InconsistentNetError.allowedDuration"
		))

		root.getGroup(EditModuleJvm.PREF_TREE_EDITOR).add(BooleanPreference(
			id = FileMetaGraphHistoryService.PREF_META_GRAPH_HISTORY,
			nameKey = "graph.history.preference.name"
		))
	}
}