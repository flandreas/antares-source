package ch.scorpion.jabbah.graph.view

import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.SelectionDrawingStrategy
import ch.scorpion.jabbah.edit.auth.DesktopUser
import ch.scorpion.jabbah.edit.auth.DesktopUserHolder
import ch.scorpion.jabbah.edit.auth.EditAuthModule
import ch.scorpion.jabbah.edit.select.EditSelectModule
import ch.scorpion.jabbah.edit.select.SelectedColorSelectionModel
import ch.scorpion.jabbah.graph.container.PortViewComponent
import ch.scorpion.jabbah.graph.model.TestControlVertice
import ch.scorpion.jabbah.graph.model.TestVertice
import ch.scorpion.jabbah.graph.model.module.GraphModelModule
import ch.scorpion.jabbah.graph.model.param.GraphParamTypeRegistry
import ch.scorpion.jabbah.graph.model.port.SubGraphPortImpl
import ch.scorpion.jabbah.graph.model.port.TestPortFactory
import ch.scorpion.jabbah.graph.model.vertice.GraphInputImpl
import ch.scorpion.jabbah.graph.model.vertice.GraphOutputImpl
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.graph.view.oscilloscope.OscilloscopeViewFactoryMockBuilder
import ch.scorpion.jabbah.graph.view.port.TestPortView
import ch.scorpion.jabbah.graph.view.port.TestPortViewFactory
import ch.scorpion.jabbah.graph.view.vertice.TestControlVerticeView
import ch.scorpion.jabbah.graph.view.vertice.TestVerticeView
import ch.scorpion.jabbah.io.IOModule

/**
 * Basic setup of unit tests in the [ch.scorpion.jabbah.graph.view] package.
 */
object GraphViewTestRule {

	fun configure() {
		BaseModule.require()
		BaseModule.eventBus.clear()

		GraphParamTypeRegistry.clear()
		GraphViewModule.require()

		Translations.withAnyKey()

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

		EditSelectModule.selectionModelFactory.register(SelectionDrawingStrategy.REPLACE, TestVerticeView::class) { SelectedColorSelectionModel(it) }

		EditAuthModule.userHolder = DesktopUserHolder(DesktopUser.developer)

		GraphModelModule.portFactory = TestPortFactory()
		GraphViewModule.portViewFactory = TestPortViewFactory()
		GraphViewModule.oscilloscopeViewFactory = OscilloscopeViewFactoryMockBuilder().build()

		System.invoker = { it.invoke() }
	}
}