// Editing tools: the drawing tools strip on the canvas, grid and snap options in the View menu, and editing actions
// in the Edit menu and the canvas context menu (gate types, select next/previous, stacking order, extracting
// sub-circuits, sub-circuit symbols, port renaming), mirroring the desktop's menus and toolbars.
import { Code, Combine, PenTool, RotateCcw, TextCursorInput } from 'lucide-react'
import { engine } from '../../../core/engine'
import type { EditorState } from '../../../core/types'
import { dialogs } from '../../Dialogs'
import type { MenuEntry } from '../../Menu'
import { appMenuContributions, canvasContextActions, canvasOverlays, register, shortcutDocs, type DocumentEditorProps } from '../registry'
import { toolsApi, type ToolsState } from './api'
import { editSubCircuitSymbol, renamePorts, showEmbedCode } from './dialogs'
import { toolInfo, ToolStrip, useToolShortcuts } from './ToolStrip'
import './tools.css'

/** The tools state while a circuit is edited (not while simulating, in documents or in opened sub-circuits). */
function editingTools(state: EditorState): ToolsState | null {
	if (!state.circuit || state.document || state.mode !== 'edit') return null
	return toolsApi.state()
}

/** ---- Extracting a sub-circuit */

async function extractSubCircuit() {
	const problem = toolsApi.extractProblem()
	if (problem) {
		engine.message('info', 'Extract as sub-circuit', problem)
		return
	}
	const confirmed = await dialogs.confirm({
		title: 'Extract as sub-circuit',
		message: (
			<>
				<p>The selected parts are moved into a new sub-circuit, which replaces them in this circuit.</p>
				<p className="tools-note">This can’t be undone, and the circuit is saved right away.</p>
			</>
		),
		confirmLabel: 'Continue',
	})
	if (!confirmed) return
	const name = await dialogs.prompt({
		title: 'Extract as sub-circuit',
		label: 'Name of the new sub-circuit',
		confirmLabel: 'Extract',
		validate: (value) => (value.trim() ? null : 'Enter a name'),
	})
	if (name === null) return
	toolsApi.extract(name.trim())
}

/** ---- Menu entries */

/** "Change gate type" entries for the selected gates (desktop: a submenu of the context menu). */
function gateTypeEntries(tools: ToolsState): MenuEntry[] {
	if (tools.gateTypes.length === 0) return []
	return [
		{ heading: 'Change gate type' },
		...tools.gateTypes.map(
			(t): MenuEntry => ({ label: t.label, checked: t.current, onSelect: () => toolsApi.changeGateType(t.id) }),
		),
	]
}

function subCircuitEntries(tools: ToolsState): MenuEntry[] {
	const entries: MenuEntry[] = []
	if (tools.editSymbol) {
		entries.push({ label: 'Edit symbol of this use…', icon: <PenTool size={15} />, onSelect: () => void editSubCircuitSymbol() })
		entries.push({
			label: 'Reset symbol',
			icon: <RotateCcw size={15} />,
			disabled: !tools.resetSymbol,
			onSelect: () => toolsApi.resetSymbol(),
		})
	}
	return entries
}

const extractEntry = (tools: ToolsState): MenuEntry => ({
	label: 'Extract as sub-circuit…',
	icon: <Combine size={15} />,
	disabled: !tools.extract,
	onSelect: () => void extractSubCircuit(),
})

const stackingEntries = (tools: ToolsState): MenuEntry[] => [
	{ label: 'Bring forward', disabled: !tools.oneUp, onSelect: () => toolsApi.perform('oneUp') },
	{ label: 'Send backward', disabled: !tools.oneDown, onSelect: () => toolsApi.perform('oneDown') },
]

register(appMenuContributions.edit, (c) => c.id, {
	id: 'tools',
	entries: (state) => {
		const tools = editingTools(state)
		if (!tools) return []
		return [
			{ label: 'Select next', shortcut: 'Mod+→', disabled: !tools.selectNext, onSelect: () => toolsApi.perform('selectNext') },
			{
				label: 'Select previous',
				shortcut: 'Mod+←',
				disabled: !tools.selectPrevious,
				onSelect: () => toolsApi.perform('selectPrevious'),
			},
			...stackingEntries(tools),
			'separator',
			extractEntry(tools),
			...subCircuitEntries(tools),
			{
				label: 'Rename inputs and outputs…',
				icon: <TextCursorInput size={15} />,
				disabled: !tools.renamePorts || tools.target !== 'circuit',
				onSelect: () => void renamePorts(),
			},
			...(gateTypeEntries(tools).length > 0 ? (['separator', ...gateTypeEntries(tools)] as MenuEntry[]) : []),
		]
	},
})

register(appMenuContributions.view, (c) => c.id, {
	id: 'tools',
	entries: (state) => {
		const tools = editingTools(state)
		if (!tools) return []
		const gates = state.circuit?.type === 'Digital' && tools.target === 'circuit'
		return [
			{ label: 'Grid', checked: tools.grid, disabled: !tools.toolsEnabled, onSelect: () => toolsApi.toggleGrid() },
			{ label: 'Snap to grid', checked: tools.gridSnap, disabled: !tools.toolsEnabled, onSelect: () => toolsApi.toggleGridSnap() },
			{
				label: 'Snap to components',
				checked: tools.componentSnap,
				disabled: !tools.toolsEnabled,
				onSelect: () => toolsApi.toggleComponentSnap(),
			},
			...(gates
				? [{ label: 'Gate mnemonics', checked: tools.gateMnemonic, onSelect: () => toolsApi.toggleGateMnemonic() }]
				: []),
		]
	},
})

register(appMenuContributions.tools, (c) => c.id, {
	id: 'tools',
	entries: (state) => {
		const tools = editingTools(state)
		if (!tools?.embed) return []
		return [{ label: 'Embed in a web page…', icon: <Code size={15} />, onSelect: () => void showEmbedCode() }]
	},
})

register(canvasContextActions, (c) => c.id, {
	id: 'tools',
	entries: (state) => {
		const tools = editingTools(state)
		if (!tools || !tools.toolsEnabled) return []
		const entries: MenuEntry[] = [...stackingEntries(tools)]
		if (tools.target === 'circuit') entries.push('separator', ...subCircuitEntries(tools), extractEntry(tools))
		return [...entries, 'separator', ...gateTypeEntries(tools)]
	},
})

/** ---- Canvas overlay */

function ToolsOverlay({ state }: DocumentEditorProps) {
	const tools = editingTools(state)
	const shown = !!tools && state.breadcrumbs.length <= 1 && tools.target !== 'instance'
	useToolShortcuts(shown)
	if (!shown || !tools) return null
	return (
		<div className="canvas-overlay tools-overlay">
			<ToolStrip tools={tools} />
		</div>
	)
}

register(canvasOverlays, (o) => o.id, { id: 'tools', component: ToolsOverlay })

register(
	shortcutDocs,
	(d) => `${d.group}/${d.label}`,
	...Object.entries(toolInfo)
		.filter(([id]) => id !== 'label')
		.map(([id, t]) => ({ group: 'Drawing tools', label: id === 'text' ? 'Text (label in symbols)' : t.label, keys: t.shortcut })),
	{ group: 'Drawing tools', label: 'Back to selection', keys: 'Escape' },
	{ group: 'Edit', label: 'Select next', keys: 'Mod+→' },
	{ group: 'Edit', label: 'Select previous', keys: 'Mod+←' },
)
