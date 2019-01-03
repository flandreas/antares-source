package ch.scorpion.jabbah.graph.view.vertice

import ch.scorpion.jabbah.graph.MetaGraph

/**
 * A request to open a sub [MetaGraph] of a [SubGraphVerticeView].
 *
 * @property subGraphVerticeView the [SubGraphVerticeView] from where the request originates
 * @property newView `true` if the user wishes that the [MetaGraph] is opened in a new, additional view
 * @property quickMode `true` if the user wishes that the resulting view changes happen quickly, for example
 *      without time-consuming animations.
 */
data class OpenSubGraphRequest(
	val subGraphVerticeView: SubGraphVerticeView<*>,
	val newView: Boolean,
	val quickMode: Boolean)