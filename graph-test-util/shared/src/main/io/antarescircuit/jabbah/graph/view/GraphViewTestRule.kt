package io.antarescircuit.jabbah.graph.view

import io.antarescircuit.jabbah.base.System
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.invocation.InvocationHandler
import io.antarescircuit.jabbah.base.invocation.SynchronousInvocationHandler
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.edit.SelectionDrawingStrategy
import io.antarescircuit.jabbah.edit.auth.DesktopUser
import io.antarescircuit.jabbah.edit.auth.DesktopUserHolder
import io.antarescircuit.jabbah.edit.auth.EditAuthModule
import io.antarescircuit.jabbah.edit.select.EditSelectModule
import io.antarescircuit.jabbah.edit.select.selectedColorSelectionModelFactory
import io.antarescircuit.jabbah.graph.container.PortViewComponent
import io.antarescircuit.jabbah.graph.model.TestControlVertice
import io.antarescircuit.jabbah.graph.model.TestVertice
import io.antarescircuit.jabbah.graph.model.module.GraphModelModule
import io.antarescircuit.jabbah.graph.model.nonvolatile.EmptyNonVolatileService
import io.antarescircuit.jabbah.graph.model.param.GraphParamTypeRegistry
import io.antarescircuit.jabbah.graph.model.port.SubGraphPortImpl
import io.antarescircuit.jabbah.graph.model.port.TestPortFactory
import io.antarescircuit.jabbah.graph.model.vertice.GraphInputImpl
import io.antarescircuit.jabbah.graph.model.vertice.GraphOutputImpl
import io.antarescircuit.jabbah.graph.view.module.GraphViewModule
import io.antarescircuit.jabbah.graph.view.oscilloscope.OscilloscopeViewFactoryMockBuilder
import io.antarescircuit.jabbah.graph.view.port.TestPortView
import io.antarescircuit.jabbah.graph.view.port.TestPortViewFactory
import io.antarescircuit.jabbah.graph.view.vertice.TestControlVerticeView
import io.antarescircuit.jabbah.graph.view.vertice.TestVerticeView
import io.antarescircuit.jabbah.io.IOModule

/**
 * Basic setup of unit tests in the [io.antarescircuit.jabbah.graph.view] package.
 */
object GraphViewTestRule {

	fun configure() {
		GraphViewModule.reset()

		BaseModule.require()
		BaseModule.eventBus.clear()

		GraphParamTypeRegistry.clear()
		GraphViewModule.require()

		Translations.withAnyKey()

		InvocationHandler.Companion.implementation = SynchronousInvocationHandler()

		IOModule.typeMap.register("testVertice", TestVertice::class)
		IOModule.typeMap.register("testVerticeView", TestVerticeView::class)
		IOModule.typeMap.register("testControl", TestControlVertice::class)
		IOModule.typeMap.register("testControlView", TestControlVerticeView::class)
		IOModule.typeMap.register("testGraphPortView", TestGraphPortView::class)
		IOModule.typeMap.register("testPortView", TestPortView::class)
		IOModule.typeMap.register("graphInputImpl", GraphInputImpl::class)
		IOModule.typeMap.register("graphOutputImpl", GraphOutputImpl::class)
		IOModule.typeMap.register("portViewComponent", PortViewComponent::class)
		IOModule.typeMap.register("subGraphPortImpl", SubGraphPortImpl::class)

		EditSelectModule.selectionModelFactory.register(SelectionDrawingStrategy.REPLACE, TestVerticeView::class, selectedColorSelectionModelFactory)

		EditAuthModule.userHolder = DesktopUserHolder(DesktopUser.Companion.developer)

		GraphModelModule.portFactory = TestPortFactory()
		GraphModelModule.nonVolatileService = EmptyNonVolatileService()
		GraphViewModule.portViewFactory = TestPortViewFactory()
		GraphViewModule.oscilloscopeViewFactory = OscilloscopeViewFactoryMockBuilder().build()

		System.invoker = { it.invoke() }
	}
}