package io.antarescircuit.jabbah.graph.ui

/** An extension point for extending the functionality of [GraphNavigationViewController] without the need for subclassing.*/
interface GraphNavigationViewControllerExtension {

	/** Called by the owning [GraphNavigationViewController] when it is being disposed.*/
	fun dispose(controller: GraphNavigationViewController)
}

class EmptyGraphNavigationControllerExtension : GraphNavigationViewControllerExtension {
	override fun dispose(controller: GraphNavigationViewController) {
		// empty
	}
}