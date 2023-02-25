package ch.scorpion.jabbah.graph.model.module

import ch.scorpion.jabbah.base.AbstractModule
import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.execution.module.ExecutionModule
import ch.scorpion.jabbah.graph.*
import ch.scorpion.jabbah.graph.dsl.GraphDslModule
import ch.scorpion.jabbah.graph.model.*
import ch.scorpion.jabbah.graph.model.graph.GraphImpl
import ch.scorpion.jabbah.graph.model.net.NetImpl
import ch.scorpion.jabbah.graph.model.net.SignalConflictBehaviour
import ch.scorpion.jabbah.graph.model.net.SignalConflictBehaviourHolder
import ch.scorpion.jabbah.graph.model.oscilloscope.Oscilloscope
import ch.scorpion.jabbah.graph.model.oscilloscope.OscilloscopeProbeVertice
import ch.scorpion.jabbah.graph.model.oscilloscope.SignalHistories
import ch.scorpion.jabbah.graph.model.param.GraphParamDefinition
import ch.scorpion.jabbah.graph.model.param.GraphParamDefinitions
import ch.scorpion.jabbah.graph.model.param.GraphParamValue
import ch.scorpion.jabbah.graph.model.param.GraphParamValues
import ch.scorpion.jabbah.graph.model.port.InconsistentNetError
import ch.scorpion.jabbah.graph.model.port.PortFactory
import ch.scorpion.jabbah.graph.model.port.UndefinedPortFactory
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVerticeImpl
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVerticeRef
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVerticeRefActivationRecord
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVerticeRefActivationRecordFactory
import ch.scorpion.jabbah.graph.repository.RepositoryModule
import ch.scorpion.jabbah.graph.view.scenario.ScenarioImpl
import ch.scorpion.jabbah.graph.view.scenario.ScenarioStepImpl
import ch.scorpion.jabbah.graph.view.scenario.ScenariosImpl
import ch.scorpion.jabbah.graph.view.usecase.UsecaseImpl
import ch.scorpion.jabbah.graph.view.usecase.UsecasesImpl
import ch.scorpion.jabbah.io.IOModule
import ch.scorpion.jabbah.io.TypeMap

/**
 * Module definitions for the [ch.scorpion.jabbah.graph.model] module.
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

	override fun initialize() {
		BaseModule.require()
		IOModule.require()
		ExecutionModule.require()
		RepositoryModule.require()
		GraphDslModule.require()

		Translations.addBundle("jabbah-graph")

		registerGraphTypes()
		fillProperties(BaseModule.properties)
		configureTypeMap(IOModule.typeMap)
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

	private fun registerGraphTypes() {
		graphTypeRegistry.register(GenericGraphType, asDefault = true)
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

		typeMap.register("scenarios", ScenariosImpl::class)
		typeMap.register("scenario", ScenarioImpl::class)
		typeMap.register("scenarioStep", ScenarioStepImpl::class)

		typeMap.register("usecases", UsecasesImpl::class)
		typeMap.register("usecase", UsecaseImpl::class)
	}

	private fun fillProperties(properties: Properties) {
		properties.set(SignalConflictBehaviour.PROP_SIGNAL_CONFLICT_BEHAVIOUR, SignalConflictBehaviour.IGNORE.customName)
		properties.set(InconsistentNetError.PROP_ALLOWED_DURATION, 20)
		properties.set(SignalHistories.PROP_BUFFER_SIZE, 50)
	}
}