package ch.scorpion.jabbah.graph.model.module

import ch.scorpion.jabbah.base.AbstractModule
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.exception.UnsupportedOperationException
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.execution.module.ExecutionModule
import ch.scorpion.jabbah.graph.CombinedMetaGraphRepository
import ch.scorpion.jabbah.graph.GraphStorable
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.MetaGraphRepository
import ch.scorpion.jabbah.graph.library.LibraryModule
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.model.graph.GraphImpl
import ch.scorpion.jabbah.graph.model.net.NetImpl
import ch.scorpion.jabbah.graph.model.oscilloscope.Oscilloscope
import ch.scorpion.jabbah.graph.model.oscilloscope.OscilloscopeProbeVertice
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVerticeImpl
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVerticeRef
import ch.scorpion.jabbah.graph.project.ProjectModule
import ch.scorpion.jabbah.graph.script.ScriptModule
import ch.scorpion.jabbah.graph.view.scenario.ScenarioImpl
import ch.scorpion.jabbah.graph.view.scenario.ScenarioStepImpl
import ch.scorpion.jabbah.graph.view.scenario.ScenariosImpl
import ch.scorpion.jabbah.io.IOModule
import ch.scorpion.jabbah.io.TypeMap

/**
 * Module definitions for the [ch.scorpion.jabbah.graph.model] module.
 */
object GraphModelModule : AbstractModule() {

    val metaGraphRepository:  MetaGraphRepository = CombinedMetaGraphRepository()

    override fun initialize() {
        BaseModule.require()
        IOModule.require()
        ExecutionModule.require()
        LibraryModule.require()
        ProjectModule.require()
        ScriptModule.require()

        configureTypeMap(IOModule.typeMap)

        Translations.addBundle("jabbah-graph")
    }

    var graphFactory: () -> Graph = { GraphImpl() }

    private fun configureTypeMap(typeMap: TypeMap) {
	    typeMap.register("graph", GraphImpl::class)
        typeMap.register("metaGraph", MetaGraph::class)
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

    }
}