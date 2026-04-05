package io.antarescircuit.jabbah.graph

import io.antarescircuit.jabbah.base.UUID
import io.antarescircuit.jabbah.draw.view.find.SearchRequest
import io.antarescircuit.jabbah.draw.View

/**
 * A request for executing a [SearchRequest] in a [View] that displays the
 * [MetaGraph] with the specified [UUID].
 */
data class SearchInMetaGraphRequest(
    val metaGraphId: UUID,
    val searchRequest: SearchRequest
)