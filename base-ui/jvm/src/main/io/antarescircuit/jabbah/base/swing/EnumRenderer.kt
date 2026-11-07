package io.antarescircuit.jabbah.base.swing

import javax.swing.table.TableCellRenderer

/**
 * A [TableCellRenderer] implementation for rendering enum values as a list.
 */
open class EnumRenderer<T: Any>(nullText: String = "") : ToStringRenderer<T>(nullText)