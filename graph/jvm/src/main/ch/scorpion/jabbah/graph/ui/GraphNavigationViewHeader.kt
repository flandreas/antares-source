package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.graph.view.GraphView
import javax.swing.JPanel

fun interface GraphNavigationViewHeaderFactory {
    fun createHeader(graphView: GraphView): JPanel?
}