package io.antarescircuit.jabbah.graph.module

import io.antarescircuit.jabbah.app.health.SystemHealthChecker
import io.antarescircuit.jabbah.app.module.AppModuleJvm
import io.antarescircuit.jabbah.base.AbstractModule
import io.antarescircuit.jabbah.base.DataLocation
import io.antarescircuit.jabbah.base.LongValue
import io.antarescircuit.jabbah.base.Properties
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.base.module.BaseModuleJvm
import io.antarescircuit.jabbah.base.preferences.*
import io.antarescircuit.jabbah.base.swing.ToStringRenderer
import io.antarescircuit.jabbah.draw.module.DrawModuleJvm
import io.antarescircuit.jabbah.edit.BeanProvider
import io.antarescircuit.jabbah.edit.DrawingView
import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.auth.EditAuthModule
import io.antarescircuit.jabbah.edit.module.EditModuleJvm
import io.antarescircuit.jabbah.edit.properties.AbstractReflectionPropertySwing
import io.antarescircuit.jabbah.edit.properties.CommandPropertySwing
import io.antarescircuit.jabbah.edit.properties.DynamicPropertyEditorRegistry
import io.antarescircuit.jabbah.edit.view.DynamicPropertyRendererRegistry
import io.antarescircuit.jabbah.execution.ExecutionModuleJvm
import io.antarescircuit.jabbah.graph.container.ContainerDrawingLayouter
import io.antarescircuit.jabbah.graph.container.ContainerTreeView
import io.antarescircuit.jabbah.graph.health.GraphViewConsistencyCheck
import io.antarescircuit.jabbah.graph.health.PortViewCoincidenceCheck
import io.antarescircuit.jabbah.graph.library.*
import io.antarescircuit.jabbah.graph.login.LoginService
import io.antarescircuit.jabbah.graph.login.LoginServiceJvm
import io.antarescircuit.jabbah.graph.model.param.*
import io.antarescircuit.jabbah.graph.model.port.InconsistentNetError
import io.antarescircuit.jabbah.graph.project.ProjectAkrabClientService
import io.antarescircuit.jabbah.graph.ui.GraphNavigationViewHeaderFactory
import io.antarescircuit.jabbah.graph.view.GraphElementView
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.connect.ConnectMethod
import io.antarescircuit.jabbah.graph.view.module.GraphViewModuleJvm
import io.antarescircuit.jabbah.graph.view.net.edge.OrthoEdgeViewLayouter
import java.net.URI

/**
 * Module definitions for the [io.antarescircuit.jabbah.graph] module on the JVM platform.
 */
object GraphModuleJvm : AbstractModule() {

	val supportWeb: Boolean get() = true

	var containerTreeViewFactory: (DrawingView<GraphElementView<*>, GraphView>) -> ContainerTreeView = { ContainerTreeView(it) }

	var projectAkrabClientService: () -> ProjectAkrabClientService = { throw UnsupportedOperationException() }

	// Tried a function interface, but the Java obfuscator didn't like it
	var libraryTreeViewActionsProvider: (LibraryTreeViewActionsParams) -> LibraryTreeViewActionsSwing =
		{ params -> LibraryTreeViewActionsSwing(params.controller, params.type, params.application) }

	var metaGraphHistoryService: FileMetaGraphHistoryService = UnimplementedFileMetaGraphHistoryService()

	val loginService: LoginService by lazy {
		LoginServiceJvm(URI(BaseModule.properties.getString(DataLocation.PROP_SERVER_URL)).toURL())
	}

	var graphNavigationViewHeaderFactory: GraphNavigationViewHeaderFactory = GraphNavigationViewHeaderFactory { null }

	override fun initialize() {
		BaseModuleJvm.require()
		DrawModuleJvm.require()
		ExecutionModuleJvm.require()
		AppModuleJvm.require()
		GraphViewModuleJvm.require()

		configurePropertyRenderer(EditModuleJvm.propertyRendererRegistry)
		configurePropertyEditors(EditModuleJvm.propertyEditorRegistry)
		configureGraphParamValueProperties()
		configureGraphParamValueEditors()

		fillProperties(BaseModule.properties)

		buildPreferencesTree(BaseModuleJvm.preferencesTree)

		if (EditAuthModule.userHolder.user.isDeveloper || AppModuleJvm.remoteControlService.getBoolean(GraphViewConsistencyCheck.REMOTE_PROP_CONSISTENCY_CHECK)) {
			SystemHealthChecker.register(GraphViewConsistencyCheck)
		}
		SystemHealthChecker.register(PortViewCoincidenceCheck)
	}

	override fun resetDependencies() {
		BaseModuleJvm.reset()
		DrawModuleJvm.reset()
		ExecutionModuleJvm.reset()
		AppModuleJvm.reset()
		GraphViewModuleJvm.reset()
	}

	private fun configurePropertyRenderer(registry: DynamicPropertyRendererRegistry) {
		registry.register(GraphParamDefinitions::class.java) { GraphParamDefinitionsPropertyRenderer() }
		registry.registerRenderer(LongValue::class.java, ToStringRenderer::class.java)
	}

	@Suppress("UNCHECKED_CAST")
	private fun configurePropertyEditors(registry: DynamicPropertyEditorRegistry) {
		registry.register(GraphParamDefinitions::class.java) {
			GraphParamDefinitionsPropertyEditor(
				propertyName = (it as CommandPropertySwing<GraphParamDefinitions>).displayName,
				editable = it.editable,
				graph = (it.editor!!.drawing as GraphView).graph!!
			)
		}
		registry.register(LongValue::class.java) { prop ->
			LongValueEditor(
				propertyName = prop.displayName,
				editable = (prop as ExpressionPropertySwing<LongValue>).editable,
				graphEditor = prop.editor,
				errorCallback = { prop.dslError = it }
			)
		}
	}

	private fun configureGraphParamValueProperties() {
		GraphParamValuePropertyFactoryRegistry.register(
			LongValueGraphParamType,
			object : GraphParamValuePropertyFactory {
				override fun create(
					def: GraphParamDefinition<*>,
					editor: Editor,
					beanProvider: BeanProvider
				): AbstractReflectionPropertySwing<*> {

					@Suppress("UNCHECKED_CAST")
					return GraphParamValuePropertySwing(
						def as GraphParamDefinition<LongValue>,
						"LongValue", // only used for logging
						LongValue::class.java,
						beanProvider
					)
				}
			}
		)

		GraphParamValuePropertyFactoryRegistry.register(
			StringGraphParamType,
			object : GraphParamValuePropertyFactory {
				override fun create(
					def: GraphParamDefinition<*>,
					editor: Editor,
					beanProvider: BeanProvider
				): AbstractReflectionPropertySwing<*> {

					@Suppress("UNCHECKED_CAST")
					return GraphParamValuePropertySwing(
						paramDefinition = def as GraphParamDefinition<String>,
						propertyName = "<notUsed>",
						baseKey = if (def.hasSemantic) "graph.paramDefs.genericSemanticParameter" else "graph.paramDefs.genericParameter",
						baseKeyParams = if (def.hasSemantic) arrayOf(def.name, def.semantic!!.translatedName) else arrayOf(def.name),
						valueClass = String::class.java,
						beanProvider = beanProvider,
					)
				}
			}
		)
	}

	private fun configureGraphParamValueEditors() {
		GraphParamValueEditorRegistry.register(LongValueGraphParamType) { LongValueGraphParamValueEditor() }
		GraphParamValueEditorRegistry.register(StringGraphParamType) { StringGraphParamValueEditor() }
	}

	private fun fillProperties(properties: Properties) {
		properties.set(FileMetaGraphHistoryService.PREF_META_GRAPH_HISTORY, true)
		properties.set(ContainerDrawingLayouter.PROP_CONTAINER_DRAWING_LAYOUTER, ContainerDrawingLayouter.Narrow.customName)
		properties.set(AbstractLibraryImportProcess.PROP_PROJECT_FILE_EXTENSION, "jgp") // Jabbah Graph Project
		properties.set(AbstractLibraryImportProcess.PROP_LIBRARY_FILE_EXTENSION, "jgl") // Jabbah Graph Library
	}

	private fun buildPreferencesTree(root: PreferenceGroup) {

		if (supportWeb) {
			root.getGroup(BaseModuleJvm.PREF_TREE_GENERAL).add(EnumPreference(
				id = DataLocation.PROP_DATA_LOCATION,
				nameKey = "base.preferences.dataLocation",
				values = DataLocation.entries.toTypedArray(),
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
			nameKey = "graph.preferences.InconsistentNetError.allowedDuration",
			minValue = 0,
			maxValue = 1_000_000
		))

		root.getGroup(EditModuleJvm.PREF_TREE_EDITOR).add(BooleanPreference(
			id = FileMetaGraphHistoryService.PREF_META_GRAPH_HISTORY,
			nameKey = "graph.history.preference.name"
		))

		root.getGroup(EditModuleJvm.PREF_TREE_EDITOR).add(BooleanPreference(
			id = OrthoEdgeViewLayouter.PROP_ADVANCED_LAYOUT,
			nameKey = "graph.edgeView.advancedLayout.name",
			needsRestart = true
		))

		root.getGroup(EditModuleJvm.PREF_TREE_EDITOR).add(EnumPreference(
			id = ConnectMethod.PROP_CONNECT_METHOD,
			nameKey = "graph.connectMethod",
			values = ConnectMethod.entries.toTypedArray(),
			withName = ConnectMethod::withName,
			needsRestart = false
		))
	}
}