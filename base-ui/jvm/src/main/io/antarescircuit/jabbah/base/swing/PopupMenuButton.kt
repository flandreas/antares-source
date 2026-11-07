package io.antarescircuit.jabbah.base.swing

import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.AbstractButton
import javax.swing.JButton
import javax.swing.JMenuItem
import javax.swing.JPopupMenu


/**
 * Enriches a [JButton] with a [JPopupMenu] that is shown when the user presses the [JButton].
 */
object PopupMenuButton {

    /**
     * Installs a [JPopupMenu] on the specified [JButton].
     * @param button the [JButton] that should show a [JPopupMenu] containing the specified [JMenuItem]s.
     * @param menuItems the [JMenuItem] to show.
     */
    fun install(button: AbstractButton, menuItems: List<JMenuItem>) {
        if (menuItems.isEmpty()) {
            return
        }
        val popupMenu = JPopupMenu()
        for (menuItem in menuItems) {
            popupMenu.add(menuItem)
        }
        button.addMouseListener(object : MouseAdapter() {
            override fun mousePressed(e: MouseEvent) {
                popupMenu.show(e.component, e.x, e.y)
            }
        })
    }
}