package io.antarescircuit.jabbah.edit

/**
 * Defines the available strategies of how a [Component] likes to render its selection state.
 */
enum class SelectionDrawingStrategy {

    /** The selection is rendered above the selected [Component].*/
    ABOVE,

    /** The selection is rendered instead of the selected [Component].*/
    REPLACE,

    /** The selection is rendered below the selected [Component].*/
    BELOW;
}