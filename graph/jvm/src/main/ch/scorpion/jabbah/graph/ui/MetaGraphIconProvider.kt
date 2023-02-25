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

	private val currentIcons = mutableMapOf<GraphType, Icon>()
	private val nonCurrentIcons = mutableMapOf<GraphType, Icon>()
	private val images = mutableMapOf<GraphType, Image>()

	fun register(type: GraphType, current: Boolean, icon: Icon) {
		if (current) {
			currentIcons[type] = icon
		} else {
			nonCurrentIcons[type] = icon
		}
	}

	fun provideIcon(type: GraphType, current: Boolean): Icon {
		return if (current) {
			currentIcons[type]!!
		} else {
			nonCurrentIcons[type]!!
		}
	}

	fun provideImage(type: GraphType): Image {
		return images.getOrPut(type) {
			val icon = nonCurrentIcons[type]!!
			val image = BufferedImage(icon.iconWidth, icon.iconHeight, Transparency.TRANSLUCENT)
			val g = image.createGraphics()
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
			icon.paintIcon(null, g, 0, 0)
			image
		}
	}
}