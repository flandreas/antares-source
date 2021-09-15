package ch.scorpion.jabbah.graph.model.module

import ch.scorpion.jabbah.base.AbstractModule
import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.execution.module.ExecutionModule
import ch.scorpion.jabbah.graph.*
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.model.graph.GraphImpl
import ch.scorpion.jabbah.graph.model.GraphPort
import ch.scorpion.jabbah.graph.model.net.NetImpl
import ch.scorpion.jabbah.graph.model.net.SignalConflictBehaviour
import ch.scorpion.jabbah.graph.model.net.SignalConflictBehaviourHolder
import ch.scorpion.jabbah.graph.model.oscilloscope.Oscilloscope
import ch.scorpion.jabbah.graph.model.oscilloscope.OscilloscopeProbeVertice
import ch.scorpion.jabbah.graph.model.port.InconsistentNetError
import ch.scorpion.jabbah.graph.model.port.PortFactory
import ch.scorpion.jabbah.graph.model.port.UndefinedPortFactory
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVerticeImpl
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVerticeRef
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVerticeRefActivationRecord
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVerticeRefActivationRecordFactory
import ch.scorpion.jabbah.graph.repository.RepositoryModule
import ch.scorpion.jabbah.graph.script.ScriptModule
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

	val metaGraphRepository: MetaGraphRepository = CombinedMetaGraphRepository()

	val signalConflictBehaviourHolder by lazy { SignalConflictBehaviourHolder() }

	override fun initialize() {
		BaseModule.require()
		IOModule.require()
		ExecutionModule.require()
		RepositoryModule.require()
		ScriptModule.require()

		Translations.addBundle("jabbah-graph")

		fillProperties(BaseModule.properties)
		configureTypeMap(IOModule.typeMap)
	}

	/** Must be specified by higher application layers.*/
	var portFactory: PortFactory = UndefinedPortFactory()

	var graphFactory: (name: String) -> Graph = { GraphImpl(it) }

	/** More specific modules can register other implementations for [GraphPort] value type adjustments. */
	var subGraphVerticeRefActivationRecordFactory: SubGraphVerticeRefActivationRecordFactory =
		SubGraphVerticeRefActivationRecordFactory { v, s -> SubGraphVerticeRefActivationRecord(v, s)}

	private fun configureTypeMap(typeMap: TypeMap) {
		typeMap.register("graph", GraphImpl::class)
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
		properties.set(Oscilloscope.PROP_BUFFER_SIZE, 50)
	}
}