package io.antarescircuit.jabbah.graph

import io.antarescircuit.jabbah.graph.library.*
import io.antarescircuit.jabbah.graph.model.module.GraphModelModule
import io.antarescircuit.jabbah.graph.model.param.GraphParamValue
import io.antarescircuit.jabbah.graph.model.port.PortFactory
import io.antarescircuit.jabbah.graph.model.vertice.SubGraphVerticeRef
import io.antarescircuit.jabbah.graph.view.CompositeTestGraphViewBuilder
import io.antarescircuit.jabbah.graph.view.module.GraphViewModule
import io.antarescircuit.jabbah.graph.view.port.PortViewFactory
import io.antarescircuit.jabbah.graph.view.vertice.SubGraphVerticeViewImpl

/** Builds and fills a [Library] using components from [CompositeTestGraphViewBuilder] used for integration testing.*/
class TestLibraryBuilder(
	private val portFactory: PortFactory = GraphModelModule.portFactory,
	private val portViewFactory: PortViewFactory = GraphViewModule.portViewFactory,
	private val libraryService: LibraryService = LibraryModule.libraryService
) {

	companion object {
		const val INNER_CUSTOM_COMP = "InnerCustomComp"
		const val OUTER_CUSTOM_COMP = "OuterCustomComp"
	}

	/**
	 * Adds a custom component (as of [CompositeTestGraphViewBuilder.buildInnerCustomComponent] to the specified [Library].
	 * @return the created [MetaGraph] that contains the created custom component
	 */
	fun addInnerCustomComponent(
		library: Library,
		label: String? = null,
		inputName: String = "I",
		outputName: String = "O"
	): MetaGraph {
		val builder = CompositeTestGraphViewBuilder(INNER_CUSTOM_COMP, portFactory, portViewFactory)
		return libraryService.addContainerLibraryElement(
			library,
			builder.buildMetaGraph(builder.buildInnerCustomComponent(inputName, outputName), label),
			library
		).storable!!
	}

	fun addOuterCustomComponent(
		library: Library,
		label: String? = null,
		innerLibrary: Library = library,
		paramValue: GraphParamValue<*>? = null
	): MetaGraph {
		val builder = CompositeTestGraphViewBuilder(OUTER_CUSTOM_COMP, portFactory, portViewFactory)
		return libraryService.addContainerLibraryElement(
			library,
			builder.buildMetaGraph(
				builder.buildOuterCustomComponent(createSubGraphVerticeView(INNER_CUSTOM_COMP, innerLibrary, paramValue)),
				label),
			library
		).storable!!
	}

	private fun createSubGraphVerticeView(
		name: String,
		libraryDirectory: LibraryDirectory,
		paramValue: GraphParamValue<*>? = null
	): SubGraphVerticeViewImpl {
		val vv = (libraryDirectory.get(name) as LibraryElement).getNewInstance<SubGraphVerticeRef>() as SubGraphVerticeViewImpl
		paramValue?.let {
			vv.model.setParamValue(it)
		}
		return vv
	}
}