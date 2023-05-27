package ch.scorpion.jabbah.edit.figure

import ch.scorpion.jabbah.draw.drawable.Mirrorable
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing

/**
 * A [Figure] is a predefined graphical [Component] the user can drag into his [Drawing].
 */
interface Figure : Component, Mirrorable