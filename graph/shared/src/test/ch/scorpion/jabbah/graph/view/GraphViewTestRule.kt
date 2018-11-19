package ch.scorpion.jabbah.graph.view

import ch.scorpion.jabbah.app.module.AppModule
import ch.scorpion.jabbah.app.user.User
import ch.scorpion.jabbah.base.invocation.InvocationHandler
import ch.scorpion.jabbah.base.invocation.SynchronousInvocationHandler
import ch.scorpion.jabbah.base.module.BaseModuleJvm
import ch.scorpion.jabbah.base.swing.UiUtil
import ch.scorpion.jabbah.graph.container.PortViewComponent
import ch.scorpion.jabbah.graph.model.TestControlVertice
import ch.scorpion.jabbah.graph.model.TestVertice
import ch.scorpion.jabbah.graph.model.port.SubGraphPortImpl
import ch.scorpion.jabbah.graph.model.vertice.GraphInputImpl
import ch.scorpion.jabbah.graph.model.vertice.GraphOutputImpl
import ch.scorpion.jabbah.graph.view.module.GraphViewModuleJvm
import ch.scorpion.jabbah.graph.view.port.TestPortView
import ch.scorpion.jabbah.graph.view.vertice.TestControlVerticeView
import ch.scorpion.jabbah.graph.view.vertice.TestVerticeView
import ch.scorpion.jabbah.io.IOModule
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

/**
 * Basic setup of unit tests in the [ch.scorpion.jabbah.graph.view] package.
 */
class GraphViewTestRule : TestRule {

    override fun apply(statement: Statement?, p1: Description?): Statement {
        return object : Statement() {
            override fun evaluate() {
                configure()
                try {
                    statement!!.evaluate()
                } finally {
                    // empty
                }
            }
        }
    }

    fun configure() {
        BaseModuleJvm.require()
        GraphViewModuleJvm.require()

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

	    InvocationHandler.implementation = SynchronousInvocationHandler()
	    UiUtil.eventQueueInvoker = {
		    false
	    }

	    AppModule.userHolder.u = User.developer()
    }
}