package ch.scorpion.jabbah.graph.view

import ch.scorpion.jabbah.app.module.AppModule
import ch.scorpion.jabbah.app.user.User
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.graph.container.PortViewComponent
import ch.scorpion.jabbah.graph.model.TestControlVertice
import ch.scorpion.jabbah.graph.model.TestVertice
import ch.scorpion.jabbah.graph.model.port.SubGraphPortImpl
import ch.scorpion.jabbah.graph.model.vertice.GraphInputImpl
import ch.scorpion.jabbah.graph.model.vertice.GraphOutputImpl
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.graph.view.port.TestPortView
import ch.scorpion.jabbah.graph.view.vertice.TestControlVerticeView
import ch.scorpion.jabbah.graph.view.vertice.TestVerticeView
import ch.scorpion.jabbah.io.IOModule

/**
 * Basic setup of unit tests in the [ch.scorpion.jabbah.graph.view] package.
 */
object GraphViewTestRule {

	fun configure() {
		BaseModule.require()
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

		AppModule.userHolder.u = User.developer()
	}
}