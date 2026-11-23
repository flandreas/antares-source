package io.antarescircuit.jabbah.draw.rasterimg

import dev.mokkery.answering.returns
import dev.mokkery.answering.calls
import dev.mokkery.answering.throws
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock
import io.antarescircuit.jabbah.base.geom.Dimension2D
import io.antarescircuit.jabbah.base.geom.Margin
import io.antarescircuit.jabbah.base.geom.Rectangle2D
import io.antarescircuit.jabbah.draw.Drawable
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.DrawableContainer
import io.antarescircuit.jabbah.draw.MainContent
import io.antarescircuit.jabbah.draw.drawable.Page
import io.antarescircuit.jabbah.draw.drawable.PageOrientation
import io.antarescircuit.jabbah.draw.drawable.PageSize
import io.antarescircuit.jabbah.draw.drawable.Resolution
import io.antarescircuit.jabbah.draw.graphics.Color
import io.antarescircuit.jabbah.draw.graphics.ImageType
import java.io.ByteArrayInputStream
import java.io.IOException
import java.awt.image.BufferedImage
import java.nio.file.Files
import javax.imageio.ImageIO
import kotlin.test.*

class RasterImageExporterTest {

	@Test
	fun shouldRejectPixelCountsThatOverflowAnIntBeforeAllocating() {
		for ((width, height) in listOf(50_000 to 50_000, Int.MAX_VALUE to Int.MAX_VALUE, Int.MAX_VALUE to 1)) {
			assertFailsWith<RasterImageTooLargeException> {
				RasterImageExporter.checkImageSize(width, height, Long.MAX_VALUE)
			}
		}
	}

	@Test
	fun shouldRejectNonPositiveDimensions() {
		for ((width, height) in listOf(0 to 10, 10 to 0, -1 to 10, -1 to -1)) {
			assertFailsWith<RasterImageTooLargeException> {
				RasterImageExporter.checkImageSize(width, height, Long.MAX_VALUE)
			}
		}
	}

	@Test
	fun shouldReserveMemoryForEncodingAsWellAsThePixelBuffer() {
		// The 100 MiB pixel buffer fits, but the remaining heap cannot accommodate transient copies.
		assertFailsWith<RasterImageTooLargeException> {
			RasterImageExporter.checkImageSize(5120, 5120, 200L * 1024 * 1024)
		}
		RasterImageExporter.checkImageSize(5120, 5120, 512L * 1024 * 1024)
		RasterImageExporter.checkImageSize(800, 600, 64L * 1024 * 1024)
	}

	@Test
	fun shouldExportAReadableImageWithTheRequestedDimensionsAndBackground() {
		val bytes = RasterImageExporter.exportToByteArray(content(), ImageType.PNG, page(120, 80))
		val image = ImageIO.read(ByteArrayInputStream(bytes))
		assertEquals(120, image.width)
		assertEquals(80, image.height)
		assertEquals(java.awt.Color.WHITE.rgb, image.getRGB(0, 0))
	}

	@Test
	fun shouldEnlargeAndCenterContentToUseTheRequestedPixelDimensions() {
		val image = exportRectangle(Rectangle2D(-100, 250, 400, 200), page(2000, 1000).copy(margin = Margin.allOf(20)))
		// The height determines the scale: 960 pixels high and 1920 wide, with the remaining space centered.
		assertContentBounds(image, Rectangle2D(40, 20, 1920, 960))
	}

	@Test
	fun shouldShrinkContentToFitInsideTheImageInset() {
		val image = exportRectangle(Rectangle2D(-300, -150, 1200, 450), page(200, 100).copy(margin = Margin.allOf(20)))
		assertContentBounds(image, Rectangle2D(20, 20, 160, 60))
	}

	@Test
	fun shouldPreserveTheAspectRatioWhenTheOutputHasADifferentShape() {
		val image = exportRectangle(Rectangle2D(0, 0, 400, 200), page(200, 200).copy(margin = Margin.allOf(20)))
		assertContentBounds(image, Rectangle2D(20, 60, 160, 80))
	}

	@Test
	fun shouldCenterWithinAsymmetricMargins() {
		val image = exportRectangle(
			Rectangle2D(100, -200, 160, 70),
			page(200, 100).copy(margin = Margin(top = 10, left = 30, bottom = 20, right = 10)),
		)
		assertContentBounds(image, Rectangle2D(30, 10, 160, 70))
	}

	@Test
	fun shouldConvertPageMarginsToPixelsWhenAResolutionIsProvided() {
		val resolution = Resolution.DPI_96
		val image = exportRectangle(
			Rectangle2D(-100, 50, 140, 100),
			page(100, 80).copy(margin = Margin.allOf(10)),
			resolution,
		)
		// 100 x 80 mm at 96 dpi produces 378 x 303 pixels. The usable area is approximately 302 x 227.
		assertContentBounds(image, Rectangle2D(38, 44, 302, 215))
	}

	@Test
	fun shouldExportOnlyTheBackgroundForAnEmptyDrawing() {
		val drawable = container()
		every { drawable.boundingBox } returns Rectangle2D()
		every { drawable.drawStandalone(any()) } throws IllegalStateException("Empty drawing should not be rendered")
		val bytes = RasterImageExporter.exportToByteArray(MainContent("Empty", drawable, Color.WHITE), ImageType.PNG, page(120, 80))
		val image = ImageIO.read(ByteArrayInputStream(bytes))
		assertEquals(java.awt.Color.WHITE.rgb, image.getRGB(60, 40))
	}

	@Test
	fun shouldExportAReadableFileAtTheRequestedResolution() {
		val directory = Files.createTempDirectory("jabbah-image-export-")
		val path = directory.resolve("image.png")
		try {
			RasterImageExporter.exportToFile(content(), ImageType.PNG, path.toString(), page(100, 80), Resolution.DPI_96)
			val image = ImageIO.read(path.toFile())
			assertEquals(Resolution.DPI_96.millimeterToPixel(100), image.width)
			assertEquals(Resolution.DPI_96.millimeterToPixel(80), image.height)
		} finally {
			Files.deleteIfExists(path)
			Files.delete(directory)
		}
	}

	@Test
	fun shouldCheckTheConvertedPixelDimensionsBeforeTouchingTheOutputFile() {
		val directory = Files.createTempDirectory("jabbah-image-export-")
		val path = directory.resolve("image.png")
		try {
			Files.writeString(path, "Existing file")
			val error = assertFailsWith<RasterImageTooLargeException> {
				RasterImageExporter.exportToFile(content(), ImageType.PNG, path.toString(), page(10_000, 10_000), Resolution.DPI_300)
			}
			assertEquals(Resolution.DPI_300.millimeterToPixel(10_000), error.width)
			assertEquals("Existing file", Files.readString(path))
		} finally {
			Files.deleteIfExists(path)
			Files.delete(directory)
		}
	}

	@Test
	fun shouldReportMemoryFailuresDuringDrawingAsRecoverableExportErrors() {
		val drawable = container()
		val failure = OutOfMemoryError("Java heap space")
		every { drawable.drawStandalone(any()) } throws failure
		val error = assertFailsWith<RasterImageTooLargeException> {
			RasterImageExporter.exportToByteArray(MainContent("Test", drawable, Color.WHITE), ImageType.PNG, page(120, 80))
		}
		assertSame(failure, error.cause)
		assertEquals(120, error.width)
		assertEquals(80, error.height)
	}

	@Test
	fun shouldPropagateFileWritingErrors() {
		val directory = Files.createTempDirectory("jabbah-image-export-")
		try {
			assertFailsWith<IOException> {
				RasterImageExporter.exportToFile(content(), ImageType.PNG, directory.resolve("missing/image.png").toString(), page(120, 80))
			}
		} finally {
			Files.delete(directory)
		}
	}

	private fun container(): DrawableContainer<Drawable> = mock<DrawableContainer<Drawable>>().also {
		every { it.boundingBox } returns Rectangle2D(0, 0, 10, 10)
		every { it.drawStandalone(any()) } returns Unit
	}

	private fun content() = MainContent("Test", container(), Color.WHITE)

	private fun exportRectangle(bounds: Rectangle2D, page: Page, resolution: Resolution? = null): BufferedImage {
		val drawable = container()
		every { drawable.boundingBox } returns bounds
		every { drawable.drawStandalone(any()) } calls {
			val context = it.args[0] as DrawContext
			context.g.color = Color.RED
			context.g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height)
		}
		return ImageIO.read(ByteArrayInputStream(RasterImageExporter.exportToByteArray(
			MainContent("Rectangle", drawable, Color.WHITE), ImageType.PNG, page, resolution,
		)))
	}

	/** Measure the rendered pixels rather than duplicating the exporter's transform calculation. */
	private fun assertContentBounds(image: BufferedImage, expected: Rectangle2D) {
		var minX = image.width
		var minY = image.height
		var maxX = -1
		var maxY = -1
		for (y in 0 until image.height) {
			for (x in 0 until image.width) {
				if (image.getRGB(x, y) != java.awt.Color.WHITE.rgb) {
					minX = minOf(minX, x)
					minY = minOf(minY, y)
					maxX = maxOf(maxX, x)
					maxY = maxOf(maxY, y)
				}
			}
		}
		assertEquals(expected.x, minX.toDouble(), 1.0, "Left edge")
		assertEquals(expected.y, minY.toDouble(), 1.0, "Top edge")
		assertEquals(expected.maxX, (maxX + 1).toDouble(), 1.0, "Right edge")
		assertEquals(expected.maxY, (maxY + 1).toDouble(), 1.0, "Bottom edge")
	}

	private fun page(width: Int, height: Int) = Page(
		PageSize("Test", Dimension2D(width, height)),
		PageOrientation.PORTRAIT,
	)
}
