package ch.scorpion.jabbah.graph.ui.scenario

import com.l2fprod.common.beans.editor.AbstractPropertyEditor
import com.l2fprod.common.propertysheet.Property
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.swing.PopupMenuButton
import ch.scorpion.jabbah.graph.ui.EditedGraphViewEvent
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.Scenario
import java.awt.event.ActionEvent
import java.beans.PropertyEditor
import javax.swing.*

/**
 * Consists of a [JButton] with a menu that contains all [Scenario]s of the current [GraphView].
 * TODO I18N
 */
class ScenarioPropertyEditor(val graphView: GraphView<*>) : AbstractPropertyEditor() {

    private val label = JLabel()
    private val button = JButton("Select")
    private var scenario: Scenario? = null

    init {
        PopupMenuButton.install(
                button,
                ScenarioMenuBuilder.buildScenarioMenu(
                        graphView, { object : AbstractAction(it.name) {
                            override fun actionPerformed(e: ActionEvent?) {
                                this@ScenarioPropertyEditor.value = it
                            }
                        }
                    }
                )
        )

        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.LINE_AXIS)
        panel.add(label)
        panel.add(button)

        editor = panel
    }

    override fun setValue(value: Any?) {
        scenario = value as Scenario?
        if (scenario != null) {
            label.text = scenario!!.name
        } else {
            label.text = "None"
        }
    }

    override fun getValue(): Any? {
        return scenario
    }
}

/**
 * Listens for [EditedGraphViewEvent]s in order to create new [ScenarioPropertyEditor]
 * that offer the [Scenario]s of the current [GraphView].
 */
class ScenarioPropertyEditorFactory(eventBus: EventBus) : (Property) -> PropertyEditor {
    constructor(): this(BaseModule.eventBus)

    private var graphView: GraphView<*>? = null

    init {
        eventBus.register(EditedGraphViewEvent::class, {
            graphView = it.newGraphView
        })
    }

    override fun invoke(property: Property): PropertyEditor {
        return ScenarioPropertyEditor(graphView!!)
    }
}