package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.checkState
import ch.scorpion.jabbah.graph.model.GraphElement
import ch.scorpion.jabbah.graph.view.GraphElementView
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StorableCreator
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter
import kotlin.reflect.KClass

/**
 * A [LibraryElement] that represents a basic, non-composed [GraphElementView].
 * [LibraryElement] supports two different ways to decide which [GraphElementView] will be instantiated:
 * - Provide a [StorableCreator] to be used for instantiation, and the class of the [GraphElementView] to be instantiated
 * - Provide a supplier that knows how to instantiate the [GraphElementView]. Use this method if you need to
 * change the created [GraphElementView] after instantiation.
 * The supplier takes precedence over the [StorableCreator].
 */
class BaseLibraryElement(
    override val name: String,
    private val translationKey: String,
    iconPath: String?,
    private val storableCreator: StorableCreator? = null,
    private val clazz: KClass<out GraphElementView<*>>?,
    private val supplier: (() -> GraphElementView<out GraphElement>)? = null
) : LibraryElement(iconPath) {

    init {
        checkState((storableCreator != null && clazz != null) || supplier != null, "either StorableCreator/clazz or supplier must be provided")
    }

    override val isFixed: Boolean get() = true

    /** ---- [Any] */

    override fun toString(): String {
        return Translations.getString("$translationKey.name")
    }

    /** ---- [Storable] interface */

    override fun write(writer: StoreWriter) {
        // empty
    }

    override fun read(reader: StoreReader) {
        // empty
    }

    /** ---- [LibraryElement] */

    override fun handleRemoved() {
        // empty
    }

    override fun <T : GraphElement> getNewInstance(): GraphElementView<T> {
        if (supplier != null) {
            return supplier.invoke() as GraphElementView<T>
        }
        return storableCreator!!.create(clazz!!) as GraphElementView<T>
    }
}