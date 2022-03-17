package ch.scorpion.jabbah.draw.svg

import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.MainContent
import ch.scorpion.jabbah.draw.graphics.AbstractGraphics2D
import ch.scorpion.jabbah.draw.graphics.Graphics2DJvm
import ch.scorpion.jabbah.draw.module.DrawModule
import org.apache.batik.dom.GenericDOMImplementation
import org.apache.batik.svggen.SVGGraphics2D
import java.awt.Dimension
import java.io.FileWriter
import java.io.PrintWriter
import java.io.StringWriter

object SvgExporter {

	fun export(content: MainContent, path: String) {
		// Get a DOMImplementation
		val domImpl = GenericDOMImplementation.getDOMImplementation()

		// Create an instance of org.w3c.dom.Document
		val svgNS = "http://www.w3.org/2000/svg"
		val document = domImpl.createDocument(svgNS, "svg", null)

		// Create an instance of the SVG Generator
		val svgGenerator = SVGGraphics2D(document)
		svgGenerator.svgCanvasSize = Dimension(content.drawable.boundingBox.widthInt, content.drawable.boundingBox.heightInt)

		// Draw
		val drawContext = DrawModule.drawContextFactory(Graphics2DJvm(svgGenerator), null, null)
		content.drawable.draw(drawContext)

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
