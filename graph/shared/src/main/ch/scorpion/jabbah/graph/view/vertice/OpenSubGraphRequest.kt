package ch.scorpion.jabbah.graph.view.vertice

import ch.scorpion.jabbah.base.event.InputEvent

/**
 * A request to open a sub [MetaGraph] of a [SubGraphVerticeView].
 * @property subGraphVerticeView the [SubGraphVerticeView] from where the request originates
 */
class OpenSubGraphRequest(val subGraphVerticeView: SubGraphVerticeView<*>, val quickMode: Boolean) {
}