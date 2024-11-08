package ch.scorpion.jabbah.graph

import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.draw.view.find.SearchRequest
import ch.scorpion.jabbah.draw.View

/**
 * A request for executing a [SearchRequest] in a [View] that displays the
 * [MetaGraph] with the specified [UUID].
 */
data class SearchInMetaGraphRequest(
    val metaGraphId: UUID,
    val searchRequest: SearchRequest
)