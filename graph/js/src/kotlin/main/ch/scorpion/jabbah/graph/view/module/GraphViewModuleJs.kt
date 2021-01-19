package ch.scorpion.jabbah.graph.view.module

import ch.scorpion.jabbah.base.AbstractModule
import ch.scorpion.jabbah.io.module.IOModuleJs
import ch.scorpion.jabbah.module.DrawModuleJs

object GraphViewModuleJs : AbstractModule() {

	override fun initialize() {
		IOModuleJs.require()
		DrawModuleJs.require()

		GraphViewModule.require()
	}
}