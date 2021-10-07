package ch.scorpion.jabbah.edit.model

import ch.scorpion.jabbah.draw.View
import ch.scorpion.jabbah.edit.Component

/**
 * A text message being sent by a [Component] that should be temporarily displayed by the [View] that
 * displays the sending [Component].
 *
 * @property type the [ComponentMessageType] that might influence how the message is rendered
 * @property source the [Component] that sent the message. If `null`, this [ComponentMessage]
 * is more like a system message and will be displayed in the [View]'s overlay container.
 * @property messageKey the translation key of the message text to be displayed
 */
data class ComponentMessage(
    val type: ComponentMessageType = ComponentMessageType.Info,
    val source: Component?,
    val messageKey: String,
	val messageParam: Any? = null)

enum class ComponentMessageType {
    Info,
    Error
}