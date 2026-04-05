package io.antarescircuit.jabbah.graph.ui

import io.antarescircuit.jabbah.graph.view.GraphView
import javax.swing.JPanel

fun interface GraphNavigationViewHeaderFactory {
    fun createHeader(graphView: GraphView): JPanel?
}