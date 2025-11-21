package ch.scorpion.jabbah.graph.view.connect

import ch.scorpion.jabbah.base.EnumProperty
import ch.scorpion.jabbah.base.PreferencesChangedEvent
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.MouseEvent
import ch.scorpion.jabbah.base.module.BaseModule

enum class ConnectMethod(
    override val customName: String,
    private val baseKey: String
): EnumProperty<ConnectMethod> {

    AutoLayout("autoLayout", "graph.connectMethod.autoLayout"),

    SetPoints("setPoints", "graph.connectMethod.setPoints");

    companion object {
        const val PROP_CONNECT_METHOD = "graph.connectMethod"

        fun withName(customName: String): ConnectMethod =
            entries.firstOrNull { it.customName == customName }
                ?: throw IllegalArgumentException("unknown ConnectMethod '$customName'")
    }

    val description: String = Translations.getOptionalString("$baseKey.desc") ?: ""

    override fun toString(): String = Translations.getString("$baseKey.name")
}

object CurrentConnectMethod {

    private val eventBus: EventBus = BaseModule.eventBus

    var defaultMethod: ConnectMethod = connectMethodFromProperties

    init {
        eventBus.register(PreferencesChangedEvent::class) {
            defaultMethod = connectMethodFromProperties
        }
    }

    fun isAutoLayout(event: MouseEvent): Boolean =
        if (event.isAltDown) {
            defaultMethod === ConnectMethod.SetPoints
        } else {
            defaultMethod === ConnectMethod.AutoLayout
        }


    fun isSetPointsMethod(event: MouseEvent): Boolean =
        if (event.isAltDown) {
            defaultMethod === ConnectMethod.AutoLayout
        } else {
            defaultMethod === ConnectMethod.SetPoints
        }

    private val connectMethodFromProperties: ConnectMethod get() =
        ConnectMethod.withName(BaseModule.properties.getString(ConnectMethod.PROP_CONNECT_METHOD))
}