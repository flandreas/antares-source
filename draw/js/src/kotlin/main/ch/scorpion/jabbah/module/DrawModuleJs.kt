package ch.scorpion.jabbah.module

import ch.scorpion.jabbah.base.AbstractModule
import ch.scorpion.jabbah.base.module.BaseModuleJs
import ch.scorpion.jabbah.draw.Canvas
import ch.scorpion.jabbah.draw.CanvasFactory
import ch.scorpion.jabbah.draw.InputEventContext
import ch.scorpion.jabbah.draw.ViewFactory
import ch.scorpion.jabbah.draw.module.DrawModule
import ch.scorpion.jabbah.draw.view.CanvasJs
import org.w3c.dom.HTMLCanvasElement
import kotlin.browser.document

/**
 * Module definitions for the [ch.scorpion.jabbah.draw] package for the JavaScript target.
 */
object DrawModuleJs : AbstractModule() {

	private val canvas = document.createElement("canvas") as HTMLCanvasElement

	override fun initialize() {
		BaseModuleJs.require()

		DrawModule.require()

		DrawModule.canvasFactory = object : CanvasFactory<InputEventContext> {
			override fun create(id: String, viewFactory: ViewFactory<InputEventContext>): Canvas {
				return CanvasJs(id, viewFactory)
			}
		}
	}
}