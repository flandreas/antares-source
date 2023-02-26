package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.HierarchyVisitor
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.graph.model.GraphElement
import ch.scorpion.jabbah.graph.model.GraphType
import ch.scorpion.jabbah.graph.view.GraphElementView
import ch.scorpion.jabbah.io.Reference
import ch.scorpion.jabbah.io.ReferenceResolver
import ch.scorpion.jabbah.io.Storable

/**
 * A [LibraryElement] is a leaf [LibraryItem] containing a template of a [GraphElementView] that
 * can be instantiated.
 */
abstract class LibraryElement(
	initialName: TranslatableText = TranslatableText(),
	iconPath: String? = null
) : AbstractLibraryItem(initialName, iconPath) {

	abstract val graphType: GraphType

    /** ---- [LibraryItem] */

    override fun accept(visitor: HierarchyVisitor): Boolean {
        return visitor.visit(this)
    }

    /** ---- [Storable] */

    override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) { }

    /** ---- [LibraryElement] */

    /** Creates a new instance of the [GraphElementView] of this [LibraryElement].*/
    abstract fun <T: GraphElement> getNewInstance(): GraphElementView<T>
}
