package io.antarescircuit.antares.web

import io.antarescircuit.antares.view.DigitalComponentViewDrawer
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.logger
import io.antarescircuit.jabbah.base.time.SystemSpeed
import io.antarescircuit.jabbah.draw.drawable.DefaultDrawableDrawer
import io.antarescircuit.jabbah.draw.graphics.Graphics2DJs
import io.antarescircuit.jabbah.draw.module.DrawModule
import io.antarescircuit.jabbah.edit.Component
import io.antarescircuit.jabbah.execution.speed.CurrentSystemSpeedCategory
import io.antarescircuit.jabbah.graph.GraphApplicationContext
import io.antarescircuit.jabbah.graph.library.LibraryElement
import io.antarescircuit.jabbah.graph.model.GraphElement
import kotlinx.browser.document
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import kotlin.math.min

/**
 * Renders small preview images of library elements for the component palette,
 * the same way the desktop's library preview draws them.
 */
internal class WebThumbnails {

	companion object {
		private val LOG by logger(WebThumbnails::class)
		private const val PADDING = 2.0
	}

	private val canvas = document.createElement("canvas") as HTMLCanvasElement
	private val ctx = canvas.getContext("2d") as CanvasRenderingContext2D
	private val drawer = DigitalComponentViewDrawer(DefaultDrawableDrawer())
	private val appContext = GraphApplicationContext(CurrentSystemSpeedCategory(SystemSpeed()))

	/** Returns a PNG data URL of [element] fitted into [width] x [height] CSS pixels, or `null` on failure. */
	fun render(element: LibraryElement, width: Int, height: Int, pixelRatio: Double): String? =
		try {
			val component: Component = element.getNewInstance<GraphElement>()
			component.location = Point2D(0, 0)
			val box = component.boundingBox
			val scale = min(
				min((width - 2 * PADDING) / box.width, (height - 2 * PADDING) / box.height),
				1.0
			)
			canvas.width = (width * pixelRatio).toInt()
			canvas.height = (height * pixelRatio).toInt()
			ctx.setTransform(1.0, 0.0, 0.0, 1.0, 0.0, 0.0)
			ctx.clearRect(0.0, 0.0, canvas.width.toDouble(), canvas.height.toDouble())
			val s = scale * pixelRatio
			val dx = (width - box.width * scale) / 2 - box.x * scale
			val dy = (height - box.height * scale) / 2 - box.y * scale
			// Graphics2DJs keeps its own transform and applies it to the context when drawing
			val g = Graphics2DJs(ctx)
			g.translate(dx * pixelRatio, dy * pixelRatio)
			g.scale(s, s)
			drawer.process(DrawModule.drawContextFactory(g, null, appContext), component)
			canvas.toDataURL("image/png")
		} catch (t: Throwable) {
			LOG.debug("Cannot render thumbnail of ${element.name.value}: ${t.message}")
			null
		}
}
