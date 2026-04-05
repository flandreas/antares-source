package io.antarescircuit.jabbah.graph.model.module

import io.antarescircuit.jabbah.base.AbstractModule
import io.antarescircuit.jabbah.base.Properties
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.edit.model.text.TranslatableText
import io.antarescircuit.jabbah.execution.SignalHandler
import io.antarescircuit.jabbah.execution.module.ExecutionModule
import io.antarescircuit.jabbah.graph.*
import io.antarescircuit.jabbah.graph.dsl.GraphDslModule
import io.antarescircuit.jabbah.graph.model.*
import io.antarescircuit.jabbah.graph.model.graph.GraphImpl
import io.antarescircuit.jabbah.edit.model.image.ImageIdentification
import io.antarescircuit.jabbah.edit.semantic.SemanticRegistry
import io.antarescircuit.jabbah.graph.library.LibraryPreferences
import io.antarescircuit.jabbah.graph.model.graph.GraphPropagationDelayCalculator
import io.antarescircuit.jabbah.graph.model.net.NetImpl
import io.antarescircuit.jabbah.graph.model.net.SignalConflictBehaviour
import io.antarescircuit.jabbah.graph.model.net.SignalConflictBehaviourHolder
import io.antarescircuit.jabbah.graph.model.nonvolatile.NonVolatileService
import io.antarescircuit.jabbah.graph.model.nonvolatile.NonVolatileStorable
import io.antarescircuit.jabbah.graph.model.nonvolatile.UnimplementedNonVolatileService
import io.antarescircuit.jabbah.graph.model.oscilloscope.Oscilloscope
import io.antarescircuit.jabbah.graph.model.oscilloscope.OscilloscopeProbeVertice
import io.antarescircuit.jabbah.graph.model.oscilloscope.SignalHistories
import io.antarescircuit.jabbah.graph.model.param.*
import io.antarescircuit.jabbah.graph.model.port.InconsistentNetError
import io.antarescircuit.jabbah.graph.model.port.PortFactory
import io.antarescircuit.jabbah.graph.model.port.UndefinedPortFactory
import io.antarescircuit.jabbah.graph.model.semantic.GraphSemantic
import io.antarescircuit.jabbah.graph.model.vertice.SubGraphVerticeImpl
import io.antarescircuit.jabbah.graph.model.vertice.SubGraphVerticeRef
import io.antarescircuit.jabbah.graph.model.vertice.SubGraphVerticeRefActivationRecord
import io.antarescircuit.jabbah.graph.model.vertice.SubGraphVerticeRefActivationRecordFactory
import io.antarescircuit.jabbah.graph.repository.RepositoryModule
import io.antarescircuit.jabbah.graph.view.scenario.ScenarioImpl
import io.antarescircuit.jabbah.graph.view.scenario.ScenarioStepImpl
import io.antarescircuit.jabbah.graph.view.scenario.ScenariosImpl
import io.antarescircuit.jabbah.graph.view.usecase.UsecaseImpl
import io.antarescircuit.jabbah.graph.view.usecase.UsecasesImpl
import io.antarescircuit.jabbah.io.IOModule
import io.antarescircuit.jabbah.io.TypeMap

/**
 * Module definitions for the [io.antarescircuit.jabbah.graph.model] module.
 */
object GraphModelModule : AbstractModule() {

	/**
	 * Used for backward compatibility: Before introduction of [GraphType],
	 * [Graph]s without [GraphType] came into existence. Will typically be set
	 * by higher level modules.
	 */
	var defaultGraphType: GraphType = GenericGraphType

	val signalConflictBehaviourHolder by lazy { SignalConflictBehaviourHolder() }

	val graphTypeRegistry = GraphTypeRegistry()

	var nonVolatileService: NonVolatileService = UnimplementedNonVolatileService()

	override fun initialize() {
		BaseModule.require()
		IOModule.require()
		ExecutionModule.require()
		RepositoryModule.require()
		GraphDslModule.require()

		Translations.addBundle("jabbah-graph")

		registerGraphTypes()
		registerGraphParamTypes()
		registerSemantics()

		fillProperties(BaseModule.properties)
		configureTypeMap(IOModule.typeMap)
	}

	override fun resetDependencies() {
		BaseModule.reset()
		IOModule.reset()
		ExecutionModule.reset()
		RepositoryModule.reset()
		GraphDslModule.reset()
	}

	/** Must be specified by higher application layers.*/
	var portFactory: PortFactory = UndefinedPortFactory()

	var graphFactory: GraphFactory = object : GraphFactory {
		override fun create(name: TranslatableText, type: GraphType): Graph = GraphImpl(name, type)
	}

	/** More specific modules can register other implementations for [GraphPort] value type adjustments. */
	var subGraphVerticeRefActivationRecordFactory: SubGraphVerticeRefActivationRecordFactory = { verticeRef: SubGraphVerticeRef, signalHandler: SignalHandler ->
		SubGraphVerticeRefActivationRecord(verticeRef, signalHandler)
	}

	private fun registerGraphParamTypes() {
		GraphParamTypeRegistry.register(LongValueGraphParamType.name) { LongValueGraphParamType }
		GraphParamTypeRegistry.register(StringGraphParamType.name) { StringGraphParamType }
	}

	private fun registerGraphTypes() {
		graphTypeRegistry.register(GenericGraphType, asDefault = true)
	}

	private fun registerSemantics() {
		GraphSemantic.entries.forEach { SemanticRegistry.register(it) }
	}

	private fun configureTypeMap(typeMap: TypeMap) {
		typeMap.register("graph", GraphImpl::class)
		typeMap.register("graphParamDef", GraphParamDefinition::class)
		typeMap.register("graphParamDefs", GraphParamDefinitions::class)
		typeMap.register("graphParam", GraphParamValue::class)
		typeMap.register("graphParams", GraphParamValues::class)
		typeMap.register("metaGraph", MetaGraph::class)
		typeMap.register("metaGraphBundle", MetaGraphBundle::class)
		typeMap.register("graphStorable", GraphStorable::class)
		typeMap.register("net", NetImpl::class)
		typeMap.register("netPortRef", NetImpl.PortRef::class)
		typeMap.register("subGraphVertice", SubGraphVerticeImpl::class)
		typeMap.register("subGraphVerticeRef", SubGraphVerticeRef::class)
		typeMap.register("oscilloscope", Oscilloscope::class)
		typeMap.register("oscilloscopeProbe", OscilloscopeProbeVertice::class)
		typeMap.register("document", Document::class)

		typeMap.register("scenarios", ScenariosImpl::class)
		typeMap.register("scenario", ScenarioImpl::class)
		typeMap.register("scenarioStep", ScenarioStepImpl::class)

		typeMap.register("usecases", UsecasesImpl::class)
		typeMap.register("usecase", UsecaseImpl::class)

		typeMap.register("imageIdentification", ImageIdentification::class)

		typeMap.register("nonVolatile", NonVolatileStorable::class)
		typeMap.register("libraryPrefs", LibraryPreferences::class)
	}

	private fun fillProperties(properties: Properties) {
		properties.set(SignalConflictBehaviour.PROP_SIGNAL_CONFLICT_BEHAVIOUR, SignalConflictBehaviour.IGNORE.customName)
		properties.set(InconsistentNetError.PROP_ALLOWED_DURATION, InconsistentNetError.DEF_ALLOWED_DURATION)
		properties.set(SignalHistories.PROP_BUFFER_SIZE, 50)
		properties.set(GraphPropagationDelayCalculator.PROP_CALCULATE_ON_SAVE, true)
	}
}