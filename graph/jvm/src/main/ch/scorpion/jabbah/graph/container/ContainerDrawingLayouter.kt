package ch.scorpion.jabbah.graph.container

import ch.scorpion.jabbah.graph.view.GraphView

/**
 * Updates the layout of a [ContainerDrawing] after relevant elements
 * have been added, removed or changed.
 * Might support various styles of layouts in the future.
 */
object ContainerDrawingLayouter {

	fun layout(graphView: GraphView, containerDrawing: ContainerDrawing, addLabel: Boolean = false) {
		ContainerDrawingFiller(graphView, containerDrawing, addLabel).fill()
	}
}