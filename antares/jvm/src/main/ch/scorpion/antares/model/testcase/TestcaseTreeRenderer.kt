package ch.scorpion.antares.model.testcase

import ch.scorpion.antares.model.DigitalGraph
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.swing.UiUtil
import ch.scorpion.jabbah.draw.richtext.RichTextLabel
import ch.scorpion.jabbah.edit.model.text.NamableTreeNode
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.ui.MetaGraphIconProvider
import java.awt.Component
import javax.swing.JLabel
import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode

internal class TestcaseTreeRenderer : RichTextLabel() {

    companion object {
        private val testcaseIcon = UiUtil.themedIcon("/img/testcase.png")
    }

    override fun getTreeCellRendererComponent(tree: JTree?, value: Any?, sel: Boolean, expanded: Boolean, leaf: Boolean, row: Int, hasFocus: Boolean): Component {
        val component = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus) as RichTextLabel
        component.richText = null
        component.verticalAlignment = JLabel.CENTER

        when ((value as DefaultMutableTreeNode).userObject) {
            is Testcase -> {
                component.icon = testcaseIcon
                component.disabledIcon = testcaseIcon
            }
            is DigitalGraph -> {
                val icon = (value.userObject as Graph).let {
                    MetaGraphIconProvider.provideIcon(it.type, false, StringUtils.isNotBlank(it.script))
                }
                component.richText = (value as NamableTreeNode).richTextName.value
                component.icon = icon
                component.disabledIcon = icon
            }
        }

        return component
    }
}