// Analysis and navigation: issues and probe log (bottom panel over the canvas), unconnected ports, statistics,
// propagation delay, global tunnels and the sub-circuit hierarchy (dialogs from the Tools menu).
import { ChartColumn, ListTree, ScrollText, Timer, TriangleAlert, Unplug, Waypoints } from 'lucide-react'
import type { EditorState } from '../../../core/types'
import { appMenuContributions, canvasOverlays, register } from '../registry'
import { BottomPanel } from './BottomPanel'
import { showFindUnconnected, showPropagationDelay, showStatistics } from './dialogs'
import { showHierarchy } from './HierarchyDialog'
import { bottomPanel } from './store'
import { showGlobalTunnels } from './TunnelsDialog'
import './analysis.css'

/** Shows the issues panel, e.g. from the issue count in the status bar. */
export const showIssues = () => bottomPanel.show('issues')

const circuitShown = (s: EditorState) => !!s.circuit && !s.document
const editing = (s: EditorState) => circuitShown(s) && s.mode === 'edit'

register(canvasOverlays, (o) => o.id, { id: 'analysis', component: BottomPanel })

register(appMenuContributions.simulate, (c) => c.id, {
	id: 'analysis',
	entries: (state) => [
		{ label: 'Issues', icon: <TriangleAlert size={15} />, disabled: !circuitShown(state), onSelect: () => bottomPanel.toggle('issues') },
		{ label: 'Probe log', icon: <ScrollText size={15} />, disabled: !circuitShown(state), onSelect: () => bottomPanel.toggle('log') },
	],
})

register(appMenuContributions.tools, (c) => c.id, {
	id: 'analysis',
	entries: (state) => [
		{ heading: 'Analyze' },
		{ label: 'Find unconnected ports…', icon: <Unplug size={15} />, disabled: !state.project || state.mode !== 'edit', onSelect: () => void showFindUnconnected() },
		{ label: 'Circuit statistics', icon: <ChartColumn size={15} />, disabled: !circuitShown(state), onSelect: () => void showStatistics() },
		{ label: 'Propagation delay', icon: <Timer size={15} />, disabled: !editing(state), onSelect: () => void showPropagationDelay() },
		{ heading: 'Navigate' },
		{ label: 'Sub-circuit hierarchy', icon: <ListTree size={15} />, disabled: !circuitShown(state), onSelect: () => void showHierarchy() },
		{ label: 'Global tunnels', icon: <Waypoints size={15} />, disabled: !state.project, onSelect: () => void showGlobalTunnels() },
	],
})
