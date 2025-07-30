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
import ch.scorpion.jabbah.base.Bean
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
 * Thrown by [PropertyPanel] if setting a new value on the current [Bean] lead to a validation failure.
 *
 * @property msg displayed in the error field of [PropertyPanel]
 * @property event the event to be posted on the system's [EventBus] to initiate further actions, such
 * as displaying a dialog
 */
class PropertyValueException(
	msg: String,
	val event: Any
) : IllegalArgumentException(msg)

/**
 * Controls displaying and editing the properties of arbitrary objects.
 * @param editor the [Editor] used for creating undoable [Command]s when changing properties
 */
abstract class AbstractPropertyPanelController<T: PropertyPanel>(
	editor: Editor,
	protected val eventBus: EventBus = BaseModule.eventBus,
	private val currentEditorEventFilter: ((CurrentEditorEvent) -> Boolean)? = null,
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

	private var activeEditorListener: PropertyChangeListener<Any> =
		PropertyChangeListener { e ->
			if (e.name == Editor.PROP_ACTIVE) {
				// Invoke later in order to give the View time to update its enabledness,
				// because enabledness of the property editors depend on it
				System.invokeLater {
					bean = defaultBean
				}
			}
		}

	private var drawingListener: PropertyChangeListener<Any> =
		PropertyChangeListener { e ->
			if (e.name == DrawingView.PROP_DRAWING) {
				bean = defaultBean
			}
		}

	private val currentEditorHandler: EventHandler<CurrentEditorEvent> = { event ->
		currentEditorEventFilter?.let { filter ->
			if (filter(event)) {
				updateEditor(event.editor)
			}
		}
	}

	init {
		if (currentEditorEventFilter != null) {
			eventBus.register(CurrentEditorEvent::class, currentEditorHandler)
		}
	}

	private fun updateEditor(e: Editor) {
		e.removePropertyChangeListener(activeEditorListener)
		e.removePropertyChangeListener(drawingListener)

		this.editor = e

		editor.addPropertyChangeListener(activeEditorListener)
		editor.addPropertyChangeListener(drawingListener)
	}

	override fun onViewInitialized() {
		super.onViewInitialized()
		editor.addPropertyChangeListener(activeEditorListener)
		editor.addPropertyChangeListener(drawingListener)
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
}