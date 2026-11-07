package io.antarescircuit.antares.model.testcase

import io.antarescircuit.antares.model.DigitalGraph
import io.antarescircuit.jabbah.base.StringUtils
import io.antarescircuit.jabbah.base.swing.UiUtil
import io.antarescircuit.jabbah.draw.richtext.RichTextLabel
import io.antarescircuit.jabbah.edit.model.text.NamableTreeNode
import io.antarescircuit.jabbah.graph.model.Graph
import io.antarescircuit.jabbah.graph.ui.MetaGraphIconProvider
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