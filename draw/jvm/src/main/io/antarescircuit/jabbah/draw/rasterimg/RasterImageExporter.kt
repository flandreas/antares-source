package io.antarescircuit.jabbah.draw.rasterimg

import io.antarescircuit.jabbah.base.geom.Dimension2D
import io.antarescircuit.jabbah.base.geom.Margin
import io.antarescircuit.jabbah.base.logger
import io.antarescircuit.jabbah.draw.DrawableContainer
import io.antarescircuit.jabbah.draw.MainContent
import io.antarescircuit.jabbah.draw.drawable.Page
import io.antarescircuit.jabbah.draw.drawable.PageOrientation
import io.antarescircuit.jabbah.draw.drawable.PageSize
import io.antarescircuit.jabbah.draw.drawable.Resolution
import io.antarescircuit.jabbah.draw.graphics.Graphics2DJvm
import io.antarescircuit.jabbah.draw.graphics.ImageType
import io.antarescircuit.jabbah.draw.module.DrawModule
import java.awt.RenderingHints
import java.awt.RenderingHints.KEY_ANTIALIASING
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.nio.file.Path
import javax.imageio.ImageIO
import kotlin.math.min

object RasterImageExporter {

	private val LOG by logger(RasterImageExporter::class)

	const val IMAGE_INSET = 20
	private const val DEF_WIDTH = 800
	private const val DEF_HEIGHT = 800

	private val DEF_PAGE = Page(
		PageSize("any", Dimension2D(DEF_WIDTH, DEF_HEIGHT)),
		PageOrientation.LANDSCAPE,
		Margin.allOf(IMAGE_INSET),
	)

	fun exportToFile(
		content: MainContent,
		imageType: ImageType,
		path: String,
		page: Page = DEF_PAGE,
		resolution: Resolution? = null,
	) {
		export(content, imageType, page, resolution) { saveToFile(it, imageType, path) }
	}

	@Suppress("unused") // Used by Akrab
	fun exportToByteArray(
		content: MainContent,
		imageType: ImageType,
		page: Page = DEF_PAGE,
		resolution: Resolution? = null,
	): ByteArray {
		return export(content, imageType, page, resolution) { saveToByteArray(it, imageType) }
	}

	private fun <T> export(
		content: MainContent,
		imageType: ImageType,
		page: Page,
		resolution: Resolution?,
		write: (BufferedImage) -> T,
	): T {
		require(content.drawable is DrawableContainer<*>)
		val width = resolution?.millimeterToPixel(page.width) ?: page.width
		val height = resolution?.millimeterToPixel(page.height) ?: page.height
		val runtime = Runtime.getRuntime()
		val maxHeap = runtime.maxMemory()
		val committedHeap = runtime.totalMemory()
		val usedHeap = committedHeap - runtime.freeMemory()
		LOG.info("Export raster image: ${width}x$height pixels, type=$imageType, " +
			"dpi=${resolution?.dpi ?: "none"}, heap bytes: max=$maxHeap, committed=$committedHeap, used=$usedHeap")

		try {
			checkImageSize(width, height, maxHeap - usedHeap)
			val image = drawToImage(content, page, resolution, width, height)
			try {
				return write(image)
			} finally {
				image.flush()
			}
		} catch (e: RasterImageTooLargeException) {
			LOG.warn("Rejected raster image export: ${e.message}")
			throw e
		} catch (e: OutOfMemoryError) {
			// Scope recovery to this user-requested operation; the preflight is only an estimate.
			LOG.error("Insufficient memory exporting ${width}x$height pixels; max heap bytes=$maxHeap", e)
			throw RasterImageTooLargeException(width, height, e)
		}
	}

	/** Reject impossible arrays and leave headroom for rendering, encoding and transient copies. */
	internal fun checkImageSize(width: Int, height: Int, availableHeapBytes: Long) {
		val pixels = width.toLong() * height.toLong()
		// Check before multiplying by bytes/pixel, which could overflow Long for arbitrary Int dimensions.
		if (width <= 0 || height <= 0 || pixels > Int.MAX_VALUE - 8L) {
			throw RasterImageTooLargeException(width, height)
		}
		// TYPE_INT_RGB uses four bytes/pixel. Three buffers are a conservative estimate, not a guarantee.
		val estimatedBytes = pixels * Int.SIZE_BYTES * 3L
		if (estimatedBytes > availableHeapBytes) {
			throw RasterImageTooLargeException(width, height)
		}
	}

	private fun drawToImage(
		content: MainContent,
		page: Page,
		resolution: Resolution?,
		widthInPixel: Int,
		heightInPixel: Int,
	): BufferedImage {
		val drawable = content.drawable as DrawableContainer<*>

		val pixelsPerUnit = resolution?.let { it.dpi / Resolution.MM_PER_INCH.toDouble() } ?: 1.0
		val leftInset = page.margin.left * pixelsPerUnit
		val topInset = page.margin.top * pixelsPerUnit
		val usableWidth = widthInPixel - leftInset - page.margin.right * pixelsPerUnit
		val usableHeight = heightInPixel - topInset - page.margin.bottom * pixelsPerUnit
		val bbox = drawable.boundingBox
		val image = BufferedImage(widthInPixel, heightInPixel, BufferedImage.TYPE_INT_RGB)

		val g2 = image.createGraphics()
		try {
			val context = DrawModule.drawContextFactory(Graphics2DJvm(g2), null, null)
			g2.setRenderingHint(KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

			// Draw background
			g2.color = Graphics2DJvm.toAwtColor(content.background)
			g2.fillRect(0, 0, widthInPixel, heightInPixel)

			// An empty drawing or a page consumed by its margins has only a background.
			if (usableWidth <= 0 || usableHeight <= 0 || (bbox.width <= 0 && bbox.height <= 0)) {
				return image
			}

			// Fit in both directions, including enlargement, without changing the aspect ratio.
			val zoomFactor = min(
				if (bbox.width > 0) usableWidth / bbox.width else Double.POSITIVE_INFINITY,
				if (bbox.height > 0) usableHeight / bbox.height else Double.POSITIVE_INFINITY,
			)
			g2.translate(
				leftInset + (usableWidth - zoomFactor * bbox.width) / 2 - zoomFactor * bbox.x,
				topInset + (usableHeight - zoomFactor * bbox.height) / 2 - zoomFactor * bbox.y,
			)
			g2.scale(zoomFactor, zoomFactor)
			drawable.drawStandalone(context)
		} finally {
			g2.dispose()
		}

		return image
	}

	private fun saveToFile(image: BufferedImage, imageType: ImageType, path: String) {
		if (!ImageIO.write(
			image,
			imageType.customName.lowercase(),
			Path.of(path).toFile()
		)) {
			throw IOException("No image writer for $imageType")
		}
	}

	private fun saveToByteArray(image: BufferedImage, imageType: ImageType): ByteArray {
		return ByteArrayOutputStream().use {
			if (!ImageIO.write(
				image,
				imageType.customName.lowercase(),
				it
			)) {
				throw IOException("No image writer for $imageType")
			}
			it.toByteArray()
		}
	}
}
