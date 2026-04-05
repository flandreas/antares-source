package io.antarescircuit.jabbah.edit.figure

import io.antarescircuit.jabbah.draw.drawable.Mirrorable
import io.antarescircuit.jabbah.edit.Component
import io.antarescircuit.jabbah.edit.Drawing

/**
 * A [Figure] is a predefined graphical [Component] the user can drag into his [Drawing].
 */
interface Figure : Component, Mirrorable