package ch.scorpion.jabbah.module

import ch.scorpion.jabbah.base.AbstractModule
import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.module.BaseModuleJs
import ch.scorpion.jabbah.draw.module.DrawModule
import ch.scorpion.jabbah.draw.view.DrawViewModule
import ch.scorpion.jabbah.draw.view.TooltipHandler
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

		fillProperties(BaseModule.properties)
	}

	private fun fillProperties(properties: Properties) {
		// Disable textual HTML tooltips in JS until HTML text boxed can be rendered
		properties.set(TooltipHandler.PROP_TOOLTIPS_ENABLED, false)
	}
}