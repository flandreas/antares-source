package ch.scorpion.jabbah.edit.model

import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.tool.ToolAdapter

/**
 * Base class of a [Tool] for interactively adding a [Component] to a [Drawing].
 *
 * @property factory creates the [Component]
 * @property adder a functions that returns the [Component] to be added to the [Drawing] instead of the created
 * [Component], which allows wrapping of a [Component].
 */
abstract class AbstractComponentTool<T: Component> (
    editor: Editor,
    private val factory: () -> T,
    private val adder: (T) -> Component
) : ToolAdapter(editor) {

    constructor(
        editor: Editor,
        factory: () -> T
    ): this(editor, factory, { it })

    protected fun createComponent(): T {
        return factory.invoke()
    }

    protected fun getAddedComponent(component: T): Component {
        return adder.invoke(component)
    }
}