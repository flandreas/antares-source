package io.antarescircuit.antares.web

import io.antarescircuit.antares.model.oscilloscope.DigitalTrace
import io.antarescircuit.antares.model.oscilloscope.VcdExport
import io.antarescircuit.antares.model.signal.DigitalSignal
import io.antarescircuit.antares.view.DigitalGraphView
import io.antarescircuit.antares.view.theme.AntaresThemes
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.UUID
import io.antarescircuit.jabbah.base.richtext.RichText
import io.antarescircuit.jabbah.draw.DrawableContainer
import io.antarescircuit.jabbah.draw.MainContent
import io.antarescircuit.jabbah.draw.View
import io.antarescircuit.jabbah.draw.graphics.Graphics2DJs
import io.antarescircuit.jabbah.draw.graphics.SvgGraphics2D
import io.antarescircuit.jabbah.draw.module.DrawModule
import io.antarescircuit.jabbah.draw.style.Themes
import io.antarescircuit.jabbah.draw.view.DrawViewModule
import io.antarescircuit.jabbah.graph.library.LibraryModule
import io.antarescircuit.jabbah.graph.library.MetaGraphBundleImportResultType
import io.antarescircuit.jabbah.graph.model.oscilloscope.SignalHistoriesType
import io.antarescircuit.jabbah.graph.model.oscilloscope.SignalHistoryEntry
import io.antarescircuit.jabbah.graph.model.vertice.BrokenReferenceException
import io.antarescircuit.jabbah.graph.view.oscilloscope.OscilloscopeView
import io.antarescircuit.jabbah.io.StorableCloner
import kotlinx.browser.document
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import kotlin.math.ceil
import kotlin.math.min

/**
 * Exports of the open circuit: SVG and raster images (desktop: `ExportSvgAction`, `ExportRasterImageAction`),
 * oscilloscope waveforms as VCD (`ExportVcdAction`) and single circuits as bundles (`ExportMetaGraphAction`,
 * `ImportMetaGraphAction`).
 */
@JsExport
class WebExport internal constructor(private val ctx: WebEditorContext) {

	companion object {
		/** Space around the drawing, in drawing units (desktop raster export: `RasterImageExporter.IMAGE_INSET`). */
		private const val INSET = 20.0

		/** Raster images larger than this (in pixels per side) are refused, browsers fail on huge canvases. */
		private const val MAX_IMAGE_SIZE = 16_384
	}

	/** The view shown in the editor: the circuit, the symbol or a focused side view. */
	private val view: View<*> get() = DrawViewModule.viewManager.activeView?.view ?: ctx.editor.view

	/** Returns the drawing's [MainContent], or `null` if there is nothing to export. */
	private fun content(): MainContent? =
		view.mainContent.takeIf { it.drawable is DrawableContainer<*> && !it.drawable.boundingBox.isEmpty }

	/**
	 * Draws the content with [theme] (a theme name, or empty for the current one) and returns [block]'s result.
	 * Other themes are activated only while drawing, the editor repaints later and doesn't notice the switch.
	 */
	private fun <T> withTheme(theme: String, block: (MainContent) -> T): T? {
		val current = Themes.get<io.antarescircuit.jabbah.draw.style.Theme>().name
		val switch = theme.isNotEmpty() && theme != current && Themes.exists(theme)
		if (switch) {
			Themes.store(theme)
			AntaresThemes.install()
		}
		try {
			return content()?.let(block)
		} finally {
			if (switch) {
				Themes.store(current)
				AntaresThemes.install()
			}
		}
	}

	/** Returns the selectable themes as JSON: `[{id, dark}]` with the current one first. */
	fun themes(): String = ctx.guarded("[]") {
		val current = Themes.get<io.antarescircuit.jabbah.draw.style.Theme>().name
		val all = mutableListOf<io.antarescircuit.jabbah.draw.style.Theme>()
		Themes.allThemes().forEach { all.add(it) }
		toJson(all.sortedBy { if (it.name == current) 0 else 1 }.map { t ->
			val id = t.name
			val dark = t.dark
			obj {
				this.id = id
				this.dark = dark
			}
		}.toJsArray())
	}

	/** Returns the size of the drawing (including a margin) as JSON `{width, height}`, or `null` if it is empty. */
	fun drawingSize(): String = ctx.guarded("null") {
		val box = content()?.drawable?.boundingBox ?: return@guarded "null"
		val width = ceil(box.width + 2 * INSET).toInt()
		val height = ceil(box.height + 2 * INSET).toInt()
		toJson(obj {
			this.width = width
			this.height = height
		})
	}

	/**
	 * Returns the drawing as SVG document drawn with [theme] (empty for the current one),
	 * with the theme's background unless [transparent]. Returns an empty string if there is nothing to export.
	 */
	fun svg(theme: String, transparent: Boolean): String = ctx.guarded("") {
		withTheme(theme) { content ->
			val g = SvgGraphics2D()
			(content.drawable as DrawableContainer<*>).drawStandalone(DrawModule.drawContextFactory(g, null, view.applicationContext))
			val box = content.drawable.boundingBox
			g.toSvg(box.x - INSET, box.y - INSET, box.width + 2 * INSET, box.height + 2 * INSET, content.background.takeUnless { transparent })
		} ?: ""
	}

	/**
	 * Renders the drawing into a [width] x [height] pixel image of [type] (e.g. "image/png"), like the desktop's
	 * `RasterImageExporter`: scaled to fit (never enlarged beyond [maxScale]) and centered.
	 * Returns the image as `data:` URL, or an empty string if there is nothing to export.
	 */
	fun rasterImage(width: Int, height: Int, maxScale: Double, theme: String, transparent: Boolean, type: String): String = ctx.guarded("") {
		require(width in 1..MAX_IMAGE_SIZE && height in 1..MAX_IMAGE_SIZE) { "The image must be between 1 and $MAX_IMAGE_SIZE pixels wide and high" }
		withTheme(theme) { content ->
			val box = content.drawable.boundingBox
			val zoom = min(maxScale, min((width - 2 * INSET) / box.width, (height - 2 * INSET) / box.height))
			val dx = (width - zoom * box.width) / 2
			val dy = (height - zoom * box.height) / 2
			val canvas = document.createElement("canvas") as HTMLCanvasElement
			canvas.width = width
			canvas.height = height
			val c2d = canvas.getContext("2d") as CanvasRenderingContext2D
			if (!transparent || type == "image/jpeg") {
				c2d.fillStyle = io.antarescircuit.jabbah.draw.graphics.AbstractGraphics2D.toJsColor(content.background)
				c2d.fillRect(0.0, 0.0, width.toDouble(), height.toDouble())
			}
			val g = Graphics2DJs(c2d)
			g.translate(dx, dy)
			g.scale(zoom, zoom)
			g.translate(-box.x, -box.y)
			(content.drawable as DrawableContainer<*>).drawStandalone(DrawModule.drawContextFactory(g, null, view.applicationContext))
			canvas.toDataURL(type)
		} ?: ""
	}

	/**
	 * Returns the waveforms recorded by the oscilloscope of the open digital circuit as Value Change Dump
	 * in JSON `{vcd}`, or `{error}` with a translated message if nothing has been recorded.
	 */
	fun vcd(): String = ctx.guarded("null") {
		val drawing = view.mainContent.drawable as? DigitalGraphView
			?: ctx.editor.view.mainContent.drawable as? DigitalGraphView
		val scope = (drawing?.getDrawable { it is OscilloscopeView } as? OscilloscopeView)?.model
		val traces = scope?.getPorts()?.mapNotNull { port ->
			val probeName = port.name ?: return@mapNotNull null
			val entries = scope.getSignalHistory(probeName)?.getEntriesSince(0)?.asSequence()?.mapNotNull { entry ->
				(entry.signal as? DigitalSignal)?.let { SignalHistoryEntry(it, entry.time) }
			}?.toList().orEmpty()
			if (entries.isEmpty()) null else DigitalTrace(probeName, entries)
		}.orEmpty()
		if (traces.isEmpty()) {
			val error = Translations.getString("antares.action.exportVcd.empty")
			return@guarded toJson(obj { this.error = error })
		}
		val endTime = scope!!.maxTime
		val clocked = scope.mode == SignalHistoriesType.Clocked
		val text = buildString { VcdExport.write(traces, this, endTime, clocked) }
		toJson(obj { this.vcd = text })
	}

	/**
	 * Returns the circuit [uuid] and all its sub-circuits that are not part of a system library as bundle file
	 * contents (the XML to be zipped, like the desktop's `ExportMetaGraphAction`) in JSON `{xml, name}`,
	 * or `{error}` if a sub-circuit is missing.
	 */
	fun circuitBundle(uuid: String): String = ctx.guarded("null") {
		ctx.saveNow()
		val element = LibraryModule.libraryHolder.getContainerLibraryElement(UUID(uuid)) ?: return@guarded "null"
		val library = element.library ?: return@guarded "null"
		try {
			val metaGraph = library.libraryService.getMetaGraph(library, element)
			val name = RichText.stripToPlainText(metaGraph.name)
			val xml = StorableCloner.serialize(LibraryModule.libraryHolder.createBundle(metaGraph))
			toJson(obj {
				this.xml = xml
				this.name = name
			})
		} catch (_: BrokenReferenceException) {
			val error = Translations.getString("library.action.exportMetaGraph.brokenRef.msg")
			toJson(obj { this.error = error })
		}
	}

	/**
	 * Imports a circuit bundle ([xml] unpacked from the ZIP file) into a new folder of the open project,
	 * like the desktop's `ImportMetaGraphAction`. Returns JSON `{result, message}` with result `success`,
	 * `conflict` (circuits already exist; call again with [replace] to replace them) or `error`.
	 */
	fun importCircuitBundle(xml: String, bundleName: String, replace: Boolean): String = ctx.guarded("null") {
		val project = ctx.project ?: return@guarded "null"
		ctx.saveNow()
		val result = project.libraryService.importMetaGraphBundle(xml, bundleName, project, replace, LibraryModule.libraryHolder)
		val type = when (result.type) {
			MetaGraphBundleImportResultType.Success -> "success"
			MetaGraphBundleImportResultType.UuidAlreadyExists -> "conflict"
			else -> "error"
		}
		val message = when (result.type) {
			MetaGraphBundleImportResultType.Success -> Translations.getString("library.action.importMetaGraph.success.msg")
			MetaGraphBundleImportResultType.Invalid -> Translations.getString("library.action.importMetaGraph.invalid.msg", bundleName)
			MetaGraphBundleImportResultType.StaleLibraryReference -> Translations.getString("library.action.importMetaGraph.staleLibraryReference.msg", bundleName)
			MetaGraphBundleImportResultType.UuidAlreadyExists -> Translations.getString("library.action.importMetaGraph.uuidAlreadyExists.msg")
			MetaGraphBundleImportResultType.ApplicationTooOld -> result.param ?: ""
		}
		ctx.notifyChanged()
		toJson(obj {
			this.result = type
			this.message = message
		})
	}
}
