package ch.scorpion.jabbah.edit.select

import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.Component
/**
 * Posted by [SelectionTool] when [Component]s are being dragged by the user.
 */
data class DragEvent(val editor: Editor, val components: Collection<Component>)