package ch.scorpion.jabbah.edit.properties

import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.event.PropertyChangeListener
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.ui.AbstractUIController
import ch.scorpion.jabbah.base.ui.UIView
import ch.scorpion.jabbah.edit.Command
import ch.scorpion.jabbah.edit.CurrentEditorEvent
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.Editor

/** A [UIView] for displaying and editing the properties of arbitrary objects.*/
interface PropertyPanel : UIView {

	/** Called by [AbstractPropertyPanelController] when [AbstractPropertyPanelController.bean] has been replaced.*/
	fun handleBeanReplaced()
}

/**
 * Controls displaying and editing the properties of arbitrary objects.
 * @param editor the [Editor] used for creating undoable [Command]s when changing properties
 */
abstract class AbstractPropertyPanelController<T: PropertyPanel>(
	editor: Editor,
	private val eventBus: EventBus = BaseModule.eventBus
) : AbstractUIController<T>() {

	var editor: Editor = editor
		private set

	/** The translated text describing the selected bean to be used as title in [PropertyPanel].*/
	var title: String = Translations.getString("edit.property.bean.none")
		protected set

	var bean: Any? = null
		protected set(value) {
			if (value !== field) {
				val oldValue = field
				field = value
				handleBeanChanged(oldValue)
			}
		}

	/** A displayable description of the currently selected bean.*/
	protected abstract val description: String?

	/** The bean to be displayed if no selection exists, such as an entire drawing. */
	protected open val defaultBean: Any? = null

	private lateinit var activeEditorListener: PropertyChangeListener<Any>

	private lateinit var drawingListener: PropertyChangeListener<Any>

	private val currentEditorHandler: EventHandler<CurrentEditorEvent> = { updateEditor(it.editor) }

	init {
		eventBus.register(CurrentEditorEvent::class, currentEditorHandler)
	}

	private fun updateEditor(editor: Editor) {
		this.editor = editor
	}

	override fun onViewInitialized() {
		super.onViewInitialized()
		activeEditorListener = setupActiveEditorListener()
		drawingListener = setupDrawingListener()
	}

	override fun dispose() {
		super.dispose()
		eventBus.unregister(currentEditorHandler)
		editor.removePropertyChangeListener(activeEditorListener)
		editor.removePropertyChangeListener(drawingListener)
	}

	fun refresh() {
		view.handleBeanReplaced()
	}

	protected fun handleBeanChanged(oldValue: Any?) {
		updateTitle()
		view.handleBeanReplaced()
		handleBeanChangedHandler(oldValue)
	}

	protected open fun handleBeanChangedHandler(oldValue: Any?) {
		// empty, can be implemented by subclasses
	}

	protected open fun updateTitle() {
		title = if (bean == null) {
			Translations.getString("edit.property.bean.none")
		} else {
			val beanDescription = if (StringUtils.isEmpty(description)) {
				Translations.getString("edit.property.bean.undefined")
			} else {
				description!!
			}
			getDefinedDescription(beanDescription)
		}
	}

	protected open fun getDefinedDescription(description: String): String =
		Translations.getString("edit.property.bean", description)

	private fun setupActiveEditorListener(): PropertyChangeListener<Any> = editor.addPropertyChangeListener { event ->
		if (event.name == Editor.PROP_ACTIVE) {
			// Invoke later in order to give the View time to update its enabledness,
			// because enabledness of the property editors depend on it
			System.invokeLater {
				bean = defaultBean
			}
		}
	}

	private fun setupDrawingListener(): PropertyChangeListener<Any> = editor.view.addPropertyChangeListener { event ->
		if (event.name == DrawingView.PROP_DRAWING) {
			bean = defaultBean
		}
	}
}