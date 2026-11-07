package io.antarescircuit.jabbah.draw.graphics

import com.github.weisj.jsvg.parser.SVGLoader
import java.io.FileInputStream

/** Adds SVG support to the core JVM raster image loader. */
class ImageLoaderUiJvm : ImageLoaderJvm() {

	override fun loadVectorImage(path: String): Image {
		FileInputStream(path).use { input ->
			val document = requireNotNull(SVGLoader().load(input)) { "Unable to load SVG image '$path'" }
			return SvgImageJvm(document)
		}
	}
}
