package io.antarescircuit.antares.web

import io.antarescircuit.antares.view.theme.AntaresThemes
import io.antarescircuit.jabbah.base.geom.Margin
import io.antarescircuit.jabbah.base.logger
import io.antarescircuit.jabbah.base.time.SystemSpeed
import io.antarescircuit.jabbah.draw.drawable.Page
import io.antarescircuit.jabbah.draw.drawable.PageOrientation
import io.antarescircuit.jabbah.draw.drawable.PageSize
import io.antarescircuit.jabbah.draw.drawable.Resolution
import io.antarescircuit.jabbah.draw.graphics.Graphics2DJs
import io.antarescircuit.jabbah.draw.module.DrawModule
import io.antarescircuit.jabbah.draw.style.DrawTheme
import io.antarescircuit.jabbah.draw.style.Theme
import io.antarescircuit.jabbah.draw.style.Themes
import io.antarescircuit.jabbah.execution.speed.CurrentSystemSpeedCategory
import io.antarescircuit.jabbah.graph.GraphApplicationContext
import io.antarescircuit.jabbah.graph.library.ContainerLibraryElement
import io.antarescircuit.jabbah.graph.poster.PosterElement
import io.antarescircuit.jabbah.graph.poster.PosterPacker
import kotlinx.browser.document
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement

/**
 * A printable overview of the circuits of the open project (desktop: `CreatePosterAction`, `PosterViewerSwing`).
 * The circuits are packed onto one page with the desktop's [PosterPacker], all at the same zoom, and rendered
 * into a PNG image.
 */
@JsExport
class WebPoster internal constructor(private val ctx: WebEditorContext) {

	companion object {
		private val LOG by logger(WebPoster::class)

		// The same layout as the desktop's PosterViewerController (in millimeter)
		private const val GAP = 2
		private val PAGE_MARGIN = Margin.allOf(5)
		private val ELEM_MARGIN = Margin(5, 2, 2, 2)
	}

	private val appContext = GraphApplicationContext(CurrentSystemSpeedCategory(SystemSpeed()))

	/**
	 * Describes the poster options as JSON: page sizes, orientations, resolutions (dpi) and the circuits of the
	 * project (uuid, name), or `null` without project.
	 */
	fun options(): String = ctx.guarded("null") {
		val project = ctx.project ?: return@guarded "null"
		val circuits = circuitsOf().map { element ->
			val uuid = element.uuid.id
			val name = element.name.value
			obj {
				this.uuid = uuid
				this.name = name
			}
		}
		val sizes = PageSize.PREDEFINED.map { it.customName }
		val orientations = PageOrientation.entries.map { orientation ->
			val id = orientation.customName
			val label = orientation.toString()
			obj {
				this.id = id
				this.label = label
			}
		}
		val resolutions = Resolution.PREDEFINED.map { it.dpi }
		val projectName = project.name.value
		toJson(obj {
			this.project = projectName
			this.circuits = circuits.toJsArray()
			this.sizes = sizes.toJsArray()
			this.orientations = orientations.toJsArray()
			this.resolutions = resolutions.toJsArray()
		})
	}

	/**
	 * Renders the circuits with the given [uuids] onto a page of [size] (e.g. "A4") and [orientation]
	 * ("portrait" or "landscape") with [pixelsPerMm] and returns a PNG data URL, or `null` on failure.
	 * Posters are meant for printing, so they always use the light theme.
	 */
	fun render(uuids: Array<String>, size: String, orientation: String, borders: Boolean, pixelsPerMm: Double): String? =
		ctx.guarded(null) {
			val project = ctx.project ?: return@guarded null
			val page = Page(PageSize.predefinedWithName(size), PageOrientation.withName(orientation), PAGE_MARGIN)
			val selected = uuids.toSet()
			val elements = circuitsOf().filter { it.uuid.id in selected }.map {
				PosterElement(project.libraryService.getMetaGraph(project, it), drawBorder = borders, margin = ELEM_MARGIN)
			}
			if (elements.isNotEmpty()) PosterPacker(page, elements, GAP).pack()

			val theme = Themes.get<Theme>()
			if (theme.dark) AntaresThemes.install(AntaresWebEditor.LIGHT_THEME)
			try {
				draw(page, elements, pixelsPerMm)
			} finally {
				if (theme.dark) AntaresThemes.install(theme.name)
			}
		}

	private fun draw(page: Page, elements: List<PosterElement>, pixelsPerMm: Double): String {
		val canvas = document.createElement("canvas") as HTMLCanvasElement
		canvas.width = (page.width * pixelsPerMm).toInt()
		canvas.height = (page.height * pixelsPerMm).toInt()
		val g = Graphics2DJs(canvas.getContext("2d") as CanvasRenderingContext2D)
		g.color = Themes.get<DrawTheme>().background.color.backgroundColor
		g.fillRect(0.0, 0.0, canvas.width.toDouble(), canvas.height.toDouble())
		g.scale(pixelsPerMm, pixelsPerMm)
		val context = DrawModule.drawContextFactory(g, null, appContext)
		elements.forEach { element ->
			try {
				element.draw(context)
			} catch (t: Throwable) {
				LOG.error("Cannot draw ${element.metaGraph.name} on the poster: ${t.message}")
			}
		}
		return canvas.toDataURL("image/png")
	}

	private fun circuitsOf(): List<ContainerLibraryElement> =
		ctx.project?.allLocalItems { it is ContainerLibraryElement }?.map { it as ContainerLibraryElement }.orEmpty()
}
