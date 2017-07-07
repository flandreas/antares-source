package ch.scorpion.jabbah.edit.select

import ch.scorpion.jabbah.draw.drawable.Unzoomable
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.SelectionModel

/**
 * Represents a [SelectionModel] that is [Unzoomable].
 */
interface UnzoomableSelectionModel<T : Component> : SelectionModel<T>, Unzoomable