package ch.scorpion.jabbah.graph.ui.scenario

import com.l2fprod.common.beans.editor.AbstractPropertyEditor
import com.l2fprod.common.propertysheet.Property
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.swing.PopupMenuButton
import ch.scorpion.jabbah.graph.ui.EditedGraphViewEvent
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.ScenarioStep
import java.awt.event.ActionEvent
import java.beans.PropertyEditor
import javax.swing.*

/**
 * Consists of a [JButton] with a menu that contains all [ScenarioStep]s of the current [GraphView].
 * TODO I18N
 */
class ScenarioStepPropertyEditor(val graphView: GraphView<*>) : AbstractPropertyEditor() {

    private val label = JLabel()
    private val button = JButton("Select")
    private var scenarioStep: ScenarioStep? = null

    init {
        PopupMenuButton.install(
            button,
            ScenarioMenuBuilder.buildScenarioStepMenu(
                graphView, { object : AbstractAction(it.name) {
                        override fun actionPerformed(e: ActionEvent?) {
                            this@ScenarioStepPropertyEditor.value = it
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
        scenarioStep = value as ScenarioStep?
        if (scenarioStep != null) {
            label.text = scenarioStep!!.name
        } else {
            label.text = "None"
        }
    }

    override fun getValue(): Any? {
        return scenarioStep
    }
}

/**
 * Listens for [EditedGraphViewEvent]s in order to create new [ScenarioStepPropertyEditor]
 * that offer the [ScenarioStep]s of the current [GraphView].
 */
class ScenarioStepPropertyEditorFactory(eventBus: EventBus) : (Property) -> PropertyEditor {
    constructor(): this(BaseModule.eventBus)

    private var graphView: GraphView<*>? = null

    init {
        eventBus.register(EditedGraphViewEvent::class, {
            graphView = it.graphView
        })
    }

    override fun invoke(property: Property): PropertyEditor {
        return ScenarioStepPropertyEditor(graphView!!)
    }
}