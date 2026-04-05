package io.antarescircuit.jabbah.base.swing

import java.awt.*

/** A convenience wrapper for [GridBagLayout].*/
class EGBL {

	companion object {
		val BOTH = GridBagConstraints.BOTH
		val CENTER = GridBagConstraints.CENTER
		val EAST = GridBagConstraints.EAST
		val HORIZONTAL = GridBagConstraints.HORIZONTAL
		val NONE = GridBagConstraints.NONE
		val NORTH = GridBagConstraints.NORTH
		val NORTHEAST = GridBagConstraints.NORTHEAST
		val NORTHWEST = GridBagConstraints.NORTHWEST
		val RELATIVE = GridBagConstraints.RELATIVE
		val REMAINDER = GridBagConstraints.REMAINDER
		val SOUTH = GridBagConstraints.SOUTH
		val SOUTHEAST = GridBagConstraints.SOUTHEAST
		val SOUTHWEST = GridBagConstraints.SOUTHWEST
		val VERTICAL = GridBagConstraints.VERTICAL
		val WEST = GridBagConstraints.WEST

		private val defIns = Insets(0, 0, 0, 0)
		private val gbc = GridBagConstraints()

		/**
		 * Gets a new instance of the default [LayoutManager] to be used with this
		 * convenience methods [GridBagLayout].
		 */
		fun getLayout(): GridBagLayout {
			return GridBagLayout()
		}

		/**
		 * Adds a [Component] to a [Container]. See [GridBagLayout] and
		 * the [GridBagConstraints] for the meaning of the parameters.
		 */
		fun add(cont: Container, comp: Component, gridx: Int,
		        gridy: Int, gridwidth: Int, gridheight: Int,
		        weightx: Double, weighty: Double, anchor: Int,
		        fill: Int) {
			add(cont, comp, gridx, gridy, gridwidth, gridheight, weightx,
				weighty, anchor, fill, defIns, -1)
		}

		/**
		 * Adds a [Component] to a [Container]. See [GridBagLayout] and
		 * the [GridBagConstraints] for the meaning of the parameters.
		 * This is the version where the index of the component can be specified.
		 */
		fun add(cont: Container, comp: Component, gridx: Int,
		        gridy: Int, gridwidth: Int, gridheight: Int,
		        weightx: Double, weighty: Double, anchor: Int,
		        fill: Int, index: Int) {
			add(cont, comp, gridx, gridy, gridwidth, gridheight, weightx,
				weighty, anchor, fill, defIns, index)
		}

		/**
		 * Adds a [Component] to a [Container]. See [GridBagLayout] and
		 * the [GridBagConstraints] for the meaning of the parameters.
		 */
		fun add(cont: Container, comp: Component, gridx: Int,
		        gridy: Int, gridwidth: Int, gridheight: Int,
		        weightx: Double, weighty: Double, anchor: Int,
		        fill: Int, insTop: Int, insLeft: Int, insBottom: Int,
		        insRight: Int) {
			add(cont, comp, gridx, gridy, gridwidth, gridheight, weightx,
				weighty, anchor, fill,
				Insets(insTop, insLeft, insBottom, insRight), -1)
		}

		private fun add(cont: Container, comp: Component, gridx: Int,
		                gridy: Int, gridwidth: Int, gridheight: Int,
		                weightx: Double, weighty: Double, anchor: Int,
		                fill: Int, ins: Insets, index: Int) {
			gbc.gridx = gridx
			gbc.gridy = gridy
			gbc.gridwidth = gridwidth
			gbc.gridheight = gridheight
			gbc.weightx = weightx
			gbc.weighty = weighty

			if (anchor >= 0) {
				gbc.anchor = anchor
			} else {
				gbc.anchor = CENTER
			}

			if (fill >= 0) {
				gbc.fill = fill
			} else {
				gbc.fill = NONE
			}

			gbc.insets = ins
			(cont.getLayout() as GridBagLayout).setConstraints(comp, gbc)
			cont.add(comp, index)
		}
	}
}