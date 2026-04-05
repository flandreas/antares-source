package io.antarescircuit.jabbah.edit.select

import io.antarescircuit.jabbah.draw.drawable.Unzoomable
import io.antarescircuit.jabbah.edit.Component
import io.antarescircuit.jabbah.edit.SelectionModel

/**
 * Represents a [SelectionModel] that is [Unzoomable].
 */
interface UnzoomableSelectionModel<T : Component> : SelectionModel<T>, Unzoomable