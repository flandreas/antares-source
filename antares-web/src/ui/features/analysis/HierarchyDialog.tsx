// The sub-circuit hierarchy of the open circuit (desktop: `GraphHierarchyViewSwing`), loaded level by level.
// Choosing a node shows that sub-circuit in the editor; the side view button opens it next to the circuit.
import { useState } from 'react'
import { Boxes, ChevronRight, CircuitBoard, PanelRight } from 'lucide-react'
import { DialogFrame, dialogs } from '../../Dialogs'
import { analysisApi, type HierarchyNode } from './api'

export function showHierarchy() {
	const nodes = analysisApi.hierarchy('')
	if (!nodes) return
	const root = analysisApi.hierarchyRootName() ?? 'Circuit'
	return dialogs.custom<null>((done) => <HierarchyDialog root={root} nodes={nodes} onClose={() => done(null)} />)
}

function HierarchyDialog({ root, nodes, onClose }: { root: string; nodes: HierarchyNode[]; onClose: () => void }) {
	const navigate = (path: string) => {
		onClose()
		analysisApi.navigateHierarchy(path)
	}
	return (
		<DialogFrame title="Sub-circuit hierarchy" onCancel={onClose}>
			<div className="hierarchy" role="tree" aria-label="Sub-circuit hierarchy">
				<div className="hierarchy-row root" role="treeitem" aria-expanded="true" aria-level={1}>
					<span className="hierarchy-toggle" />
					<button type="button" className="hierarchy-label" onClick={() => navigate('')} autoFocus>
						<CircuitBoard size={14} />
						<span className="truncate">{root}</span>
					</button>
				</div>
				{nodes.length === 0 ? (
					<div className="empty-small hierarchy-empty">This circuit contains no sub-circuits.</div>
				) : (
					nodes.map((c) => <HierarchyItem key={c.path} node={c} level={2} onNavigate={navigate} onClose={onClose} />)
				)}
			</div>
			<div className="dialog-actions analysis-dialog-footer">
				<span className="subtle">Click a sub-circuit to look inside</span>
				<button type="button" className="btn btn-primary" onClick={onClose}>
					Close
				</button>
			</div>
		</DialogFrame>
	)
}

function HierarchyItem({
	node,
	level,
	onNavigate,
	onClose,
}: {
	node: HierarchyNode
	level: number
	onNavigate: (path: string) => void
	onClose: () => void
}) {
	const [children, setChildren] = useState<HierarchyNode[] | null>(null)
	const expanded = children !== null
	const toggle = () => setChildren(expanded ? null : (analysisApi.hierarchy(node.path) ?? []))
	const showCircuit = node.circuit !== node.name && !node.name.includes(node.circuit)
	return (
		<>
			<div
				className="hierarchy-row"
				role="treeitem"
				aria-level={level}
				aria-expanded={node.hasChildren ? expanded : undefined}
				style={{ paddingLeft: (level - 1) * 16 }}
				onKeyDown={(e) => {
					if (!node.hasChildren) return
					if ((e.key === 'ArrowRight' && !expanded) || (e.key === 'ArrowLeft' && expanded)) {
						e.preventDefault()
						toggle()
					}
				}}
			>
				{node.hasChildren ? (
					<button
						type="button"
						className={`hierarchy-toggle${expanded ? ' expanded' : ''}`}
						onClick={toggle}
						aria-label={expanded ? 'Collapse' : 'Expand'}
						tabIndex={-1}
					>
						<ChevronRight size={13} />
					</button>
				) : (
					<span className="hierarchy-toggle" />
				)}
				<button type="button" className="hierarchy-label" onClick={() => onNavigate(node.path)} title={`Show “${node.name}”`}>
					<Boxes size={14} />
					<span className="truncate">{node.name}</span>
					{showCircuit && <span className="hierarchy-circuit truncate">{node.circuit}</span>}
				</button>
				<button
					type="button"
					className="icon-btn small hierarchy-side"
					title="Open in a side view"
					onClick={() => {
						onClose()
						analysisApi.openHierarchyInSideView(node.path)
					}}
				>
					<PanelRight size={14} />
				</button>
			</div>
			{children?.map((c) => <HierarchyItem key={c.path} node={c} level={level + 1} onNavigate={onNavigate} onClose={onClose} />)}
		</>
	)
}
