package ch.scorpion.jabbah.module

import ch.scorpion.jabbah.base.AbstractModule
import ch.scorpion.jabbah.base.module.BaseModuleJs
import ch.scorpion.jabbah.draw.module.DrawModule
import org.w3c.dom.HTMLCanvasElement
import kotlinx.browser.document

/**
 * Module definitions for the [ch.scorpion.jabbah.draw] package for the JavaScript target.
 */
object DrawModuleJs : AbstractModule() {

	private val canvas = document.createElement("canvas") as HTMLCanvasElement

	override fun initialize() {
		BaseModuleJs.require()

		DrawModule.require()
	}
}