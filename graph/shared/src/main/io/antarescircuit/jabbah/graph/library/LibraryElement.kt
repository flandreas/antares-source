package io.antarescircuit.jabbah.graph.library

import io.antarescircuit.jabbah.base.HierarchyVisitor
import io.antarescircuit.jabbah.edit.model.text.TranslatableText
import io.antarescircuit.jabbah.graph.model.GraphElement
import io.antarescircuit.jabbah.graph.model.GraphType
import io.antarescircuit.jabbah.graph.view.GraphElementView
import io.antarescircuit.jabbah.io.Reference
import io.antarescircuit.jabbah.io.ReferenceResolver
import io.antarescircuit.jabbah.io.Storable

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
