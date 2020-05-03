package ch.scorpion.jabbah.edit.model

import ch.scorpion.jabbah.edit.Tool
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.app.DrawingAppService
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