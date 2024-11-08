package ch.scorpion.jabbah.draw.view

import ch.scorpion.jabbah.base.EnumProperty
import ch.scorpion.jabbah.base.PreferencesChangedEvent
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.KeyEvent
import ch.scorpion.jabbah.base.event.MouseEvent
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.View

/** The input method used by the user to pan a [View].*/
enum class PanMethod(override val customName: String): EnumProperty<PanMethod> {

	MiddleMouseButton("middleMouseButton") {
		override val description: String
			get() = Translations.getString("draw.panMethod.middleMouseButton.desc")

		override fun isActivatedByPressed(event: MouseEvent, pressedKeyCode: Int?): Boolean =
			event.isMiddleButtonDown
	},

	AltLeftMouseButton("altLeftMouseButton") {
		override val description: String
			get() = Translations.getString("draw.panMethod.altLeftMouseButton.desc")

		override fun isActivatedByPressed(event: MouseEvent, pressedKeyCode: Int?): Boolean =
			event.isLeftButtonDown && event.isAltDown
	},

	SpaceLeftMouseButton("spaceLeftMouseButton") {
		override val description: String
			get() = Translations.getString("draw.panMethod.spaceLeftMouseButton.desc")

		override fun isActivatedByPressed(event: MouseEvent, pressedKeyCode: Int?): Boolean =
			event.isLeftButtonDown && pressedKeyCode == KeyEvent.VK_SPACE
	};

	companion object {
		const val PROP_PAN_METHOD = "draw.panMethod"

		fun withName(customName: String): PanMethod =
			entries.firstOrNull { it.customName == customName }
				?: throw IllegalArgumentException("unknown PanMethod '$customName'")
	}

	abstract val description: String

	abstract fun isActivatedByPressed(event: MouseEvent, pressedKeyCode: Int? = null): Boolean

	override fun toString(): String =
		when (this) {
			MiddleMouseButton -> Translations.getString("draw.panMethod.middleMouseButton.name")
			AltLeftMouseButton -> Translations.getString("draw.panMethod.altLeftMouseButton.name")
			SpaceLeftMouseButton -> Translations.getString("draw.panMethod.spaceLeftMouseButton.name")
		}
}

object CurrentPanMethod {

	private val eventBus: EventBus = BaseModule.eventBus

	var panMethod = panMethodFromProperties

	init {
		eventBus.register(PreferencesChangedEvent::class) {
			panMethod = panMethodFromProperties
		}
	}

	private val panMethodFromProperties: PanMethod get() =
		PanMethod.withName(BaseModule.properties.getString(PanMethod.PROP_PAN_METHOD))
}