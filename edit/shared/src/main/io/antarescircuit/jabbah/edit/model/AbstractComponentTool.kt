package io.antarescircuit.jabbah.edit.model

import io.antarescircuit.jabbah.edit.Tool
import io.antarescircuit.jabbah.edit.Drawing
import io.antarescircuit.jabbah.edit.Component
import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.app.DrawingAppService
import io.antarescircuit.jabbah.edit.tool.ToolAdapter

/**
 * Base class of a [Tool] for interactively adding a [Component] to a [Drawing].
 *
 * @property factory creates the [Component]
 * @property adder a functions that returns the [Component] to be added to the [Drawing] instead of the created
 * [Component], which allows wrapping of a [Component].
 */
abstract class AbstractComponentTool<T: Component> (
    editor: Editor,
    private val service: DrawingAppService,
    private val factory: () -> T,
    private val adder: (T) -> Component = { it }
) : ToolAdapter(editor) {

    protected fun createComponent(): T {
        return factory.invoke()
    }

    protected fun getAddedComponent(component: T): Component {
        return adder.invoke(component)
    }

	protected fun addComponent(component: Component) {
		editor.drawing.remove(component)
		val addedComponent = service.add(component, editor.view)
		editor.view.selectionManager.deselectAll()
		editor.view.selectionManager.select(addedComponent)
	}
}