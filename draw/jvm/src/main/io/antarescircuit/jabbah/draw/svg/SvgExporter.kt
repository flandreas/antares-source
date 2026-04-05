package io.antarescircuit.jabbah.draw.svg

import io.antarescircuit.jabbah.draw.Drawable
import io.antarescircuit.jabbah.draw.DrawableContainer
import io.antarescircuit.jabbah.draw.MainContent
import io.antarescircuit.jabbah.draw.graphics.AbstractGraphics2D
import io.antarescircuit.jabbah.draw.graphics.Graphics2DJvm
import io.antarescircuit.jabbah.draw.module.DrawModule
import org.apache.batik.dom.GenericDOMImplementation
import org.apache.batik.svggen.SVGGraphics2D
import java.awt.Dimension
import java.awt.RenderingHints
import java.io.FileWriter
import java.io.PrintWriter
import java.io.StringWriter

object SvgExporter {

	fun export(content: MainContent, path: String) {
		require(content.drawable is DrawableContainer<*>)

		// Get a DOMImplementation
		val domImpl = GenericDOMImplementation.getDOMImplementation()

		// Create an instance of org.w3c.dom.Document
		val svgNS = "http://www.w3.org/2000/svg"
		val document = domImpl.createDocument(svgNS, "svg", null)

		// Create an instance of the SVG Generator
		val svgGenerator = SVGGraphics2D(document)
		svgGenerator.svgCanvasSize = Dimension(content.drawable.boundingBox.widthInt, content.drawable.boundingBox.heightInt)

		// Draw
		val g = Graphics2DJvm(svgGenerator)

		// Remove rendering hints not supported for rendering raster images
		g.antialiasing = false
		g.g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE)

		val drawContext = DrawModule.drawContextFactory(g, null, null)
		content.drawable.drawStandalone(drawContext)

		// Getting the root clears the contents of the SVGGenerator
		val root = svgGenerator.root
		root.setAttributeNS(null, "viewBox", getViewBox(content.drawable))

		// Export
		val useCss = true
		var writer = StringWriter()
		writer.use {
				PrintWriter(it).use {
					svgGenerator.stream(root, it, useCss, false)
				}
				it.flush()
			}

		var data = writer.toString()
		val rgba = AbstractGraphics2D.toJsColor(content.background)

		// Despite trying hard, didn't find the right way to set a style property on the root element
		data = data.replaceFirst("<svg style=\"", "<svg style=\"background-color:$rgba; ")

		FileWriter(path).use {
			it.write(data)
		}
	}

	private fun getViewBox(drawable: Drawable): String {
		val bbox = drawable.boundingBox
		return "${bbox.x.toInt()} ${bbox.y.toInt()} ${bbox.width.toInt()} ${bbox.height.toInt()}"
	}
}
