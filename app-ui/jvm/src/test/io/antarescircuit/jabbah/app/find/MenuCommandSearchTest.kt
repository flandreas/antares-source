package io.antarescircuit.jabbah.app.find

import javax.swing.JCheckBoxMenuItem
import javax.swing.JMenu
import javax.swing.JMenuBar
import javax.swing.JMenuItem
import javax.swing.JList
import javax.swing.KeyStroke
import javax.swing.JComponent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MenuCommandSearchTest {
	@Test
	fun shouldFindCommandsByNameMenuPathAndAcronym() {
		val bar = JMenuBar()
		val file = JMenu("File").also { bar.add(it) }
		val export = JMenu("Export").also { file.add(it) }
		val image = JMenuItem("Export Image").also { export.add(it) }
		val save = JMenuItem("Save").also { file.add(it) }
		val commands = MenuCommandSearch.collect(bar)

		assertEquals(listOf(image), MenuCommandSearch.filter(commands, "  FILE image  ").map { it.item })
		assertEquals(listOf(image), MenuCommandSearch.filter(commands, "ei").map { it.item })
		assertEquals(listOf(save), MenuCommandSearch.filter(commands, "save").map { it.item })
		assertEquals("File › Export", commands.first().path)
		assertTrue(MenuCommandSearch.filter(commands, "missing").isEmpty())
	}

	@Test
	fun shouldPrioritizeDirectNamesAndRetainMenuOrderForEqualMatches() {
		val bar = JMenuBar()
		val menu = JMenu("Save options").also { bar.add(it) }
		val unrelated = JMenuItem("Import").also { menu.add(it) }
		val saveAs = JMenuItem("Save As").also { menu.add(it) }
		val save = JMenuItem("Save").also { menu.add(it) }
		assertEquals(listOf(save, saveAs, unrelated), MenuCommandSearch.filter(MenuCommandSearch.collect(bar), "save").map { it.item })
	}

	@Test
	fun shouldUseLiveEnabledStateAndOriginalCheckboxAction() {
		val bar = JMenuBar()
		val menu = JMenu("View").also { bar.add(it) }
		val grid = JCheckBoxMenuItem("Grid").also { menu.add(it) }
		val command = MenuCommandSearch.collect(bar).single()
		var invocations = 0
		grid.addActionListener { invocations++ }
		menu.isEnabled = false
		assertFalse(command.enabled)
		command.execute()
		assertEquals(0, invocations)
		menu.isEnabled = true
		grid.isEnabled = false
		command.execute()
		assertEquals(0, invocations)
		grid.isEnabled = true
		command.execute()
		assertTrue(grid.isSelected)
		assertEquals(1, invocations)
	}

	@Test
	fun shouldSkipSeparatorsAndHiddenMenusAndRefreshDynamicItems() {
		val bar = JMenuBar()
		val menu = JMenu("File").also { bar.add(it) }
		menu.addSeparator()
		menu.add(JMenuItem("Hidden").apply { isVisible = false })
		menu.add(JMenu("Private").apply { isVisible = false; add(JMenuItem("Secret")) })
		assertTrue(MenuCommandSearch.collect(bar).isEmpty())
		menu.add(JMenuItem("Recent project"))
		assertEquals("Recent project", MenuCommandSearch.collect(bar).single().name)
	}
	@Test
	fun shouldExposeCommandContextAndEnabledStateToAssistiveTechnology() {
		val menu = JMenu("View")
		val item = JMenuItem("Show grid").apply { accelerator = KeyStroke.getKeyStroke("ctrl G") }
		val command = MenuCommand(item, listOf(menu))
		val renderer = FindMenuAction.CommandRenderer()
		val list = JList(arrayOf(command))
		val component = renderer.getListCellRendererComponent(list, command, 0, true, true) as JComponent
		assertTrue(component.accessibleContext.accessibleName.contains("Show grid"))
		assertTrue(component.accessibleContext.accessibleName.contains("View"))
		assertTrue(component.accessibleContext.accessibleName.contains("G"))
		assertTrue(component.isEnabled)
		item.isEnabled = false
		assertFalse(renderer.getListCellRendererComponent(list, command, 0, false, false).isEnabled)
	}

}
