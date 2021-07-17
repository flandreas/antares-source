package ch.scorpion.jabbah.graph.view.module

import ch.scorpion.jabbah.base.AbstractModule
import ch.scorpion.jabbah.edit.module.EditModuleJs
import ch.scorpion.jabbah.edit.properties.PropertyPageRendererRegistry
import ch.scorpion.jabbah.graph.ui.property.EdgeViewPropertyPage
import ch.scorpion.jabbah.graph.view.net.edge.EdgeViewImpl
import ch.scorpion.jabbah.io.module.IOModuleJs
import ch.scorpion.jabbah.module.DrawModuleJs

object GraphViewModuleJs : AbstractModule() {

	override fun initialize() {
		IOModuleJs.require()
		DrawModuleJs.require()

		GraphViewModule.require()

		registerPropertyRenderers(EditModuleJs.propertyPageRendererRegistry)
	}

	private fun registerPropertyRenderers(registry: PropertyPageRendererRegistry) {
		registry.register(EdgeViewImpl::class, EdgeViewPropertyPage())
	}
}