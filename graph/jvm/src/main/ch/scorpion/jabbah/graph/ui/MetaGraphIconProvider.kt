package ch.scorpion.jabbah.graph.ui

import javax.swing.Icon
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.model.GraphType
import java.awt.Image
import java.awt.RenderingHints
import java.awt.Transparency
import java.awt.image.BufferedImage


/** Provides [Icon] for [MetaGraph] depending on [GraphType] on the JVM platform. */
object MetaGraphIconProvider {

	private val icons = mutableMapOf<IconKey, Icon>()

	private val images = mutableMapOf<GraphType, Image>()

	fun register(type: GraphType, current: Boolean, scripted: Boolean, icon: Icon) {
		icons[IconKey(type, current, scripted)] = icon
	}

	fun provideIcon(type: GraphType, current: Boolean, scripted: Boolean): Icon {
		return icons[IconKey(type, current, scripted)]!!
	}

	fun provideImage(type: GraphType): Image {
		return images.getOrPut(type) {
			val icon = icons[IconKey(type, current = false, scripted = false)]!!
			val image = BufferedImage(icon.iconWidth, icon.iconHeight, Transparency.TRANSLUCENT)
			val g = image.createGraphics()
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
			icon.paintIcon(null, g, 0, 0)
			image
		}
	}

	private data class IconKey(
		val type: GraphType,
		val current: Boolean,
		val scripted: Boolean
	)
}