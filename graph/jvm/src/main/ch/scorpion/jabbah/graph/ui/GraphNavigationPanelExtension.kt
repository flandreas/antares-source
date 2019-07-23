package ch.scorpion.jabbah.graph.ui

/** An extension point for extending the functionality of [GraphNavigationPanel] without the need for subclassing.*/
interface GraphNavigationPanelExtension {

	/** Called by the owning [GraphNavigationPanel] when it is being disposed.*/
	fun dispose(panel: GraphNavigationPanel)

}

open class EmptyGraphNavigationPanelExtension(
	@Suppress("UNUSED_PARAMETER") panel: GraphNavigationPanel
) : GraphNavigationPanelExtension {

	override fun dispose(panel: GraphNavigationPanel) {
		// empty
	}
}