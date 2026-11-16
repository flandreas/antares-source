import { useEffect, useState } from 'react'
import { BookOpen, FilePlus2, FolderOpen, Trash2, Upload } from 'lucide-react'
import { engine, type SampleInfo } from '../core/engine'
import { AntaresMark } from './TopBar'
import { DialogFrame } from './Dialogs'
import { deleteProject, importProject, newProject, openSample } from './projectActions'
import { useEngineVersion } from './hooks'

/** Lists the user's projects and the bundled examples. Shown on first start and via "Open project…". */
export function ProjectsScreen({ onClose, closable }: { onClose: () => void; closable: boolean }) {
	useEngineVersion()
	const projects = engine.projects
	const current = engine.state.project?.uuid
	const [samples, setSamples] = useState<SampleInfo[] | null>(null)
	const [busy, setBusy] = useState<string | null>(null)

	useEffect(() => {
		engine.samples().then(setSamples, () => setSamples([]))
	}, [])

	const run = async (key: string, action: () => Promise<void> | void) => {
		setBusy(key)
		try {
			await action()
		} finally {
			setBusy(null)
		}
		if (engine.state.project) onClose()
	}

	return (
		<DialogFrame title="Projects" onCancel={() => closable && onClose()} wide>
			<div className="projects">
				<div className="projects-hero">
					<AntaresMark size={34} />
					<div>
						<h1>Antares circuit editor</h1>
						<p>Design digital and analog circuits, then bring them to life in the simulator. Your projects are stored in this browser.</p>
					</div>
				</div>

				<div className="projects-actions">
					<button type="button" className="btn btn-primary" onClick={() => void run('new', newProject)}>
						<FilePlus2 size={15} /> New project
					</button>
					<button type="button" className="btn" onClick={() => void run('import', importProject)}>
						<Upload size={15} /> Import project file
					</button>
				</div>

				<div className="projects-columns">
					<section>
						<h3>Your projects</h3>
						{projects.length === 0 ? (
							<p className="empty-small">No projects yet. Create one, or start from an example.</p>
						) : (
							<ul className="project-list">
								{projects.map((p) => (
									<li key={p.uuid} className={p.uuid === current ? 'current' : ''}>
										<button
											type="button"
											className="project-open"
											onClick={() =>
												void run(p.uuid, () => {
													if (p.uuid !== current) engine.openProject(p.uuid)
												})
											}
										>
											<FolderOpen size={16} />
											<span className="truncate">{p.name}</span>
											{p.uuid === current && <span className="pill">Open</span>}
										</button>
										<button
											type="button"
											className="icon-btn small"
											title={`Delete “${p.name}”`}
											onClick={() => void deleteProject(p.uuid, p.name)}
										>
											<Trash2 size={14} />
										</button>
									</li>
								))}
							</ul>
						)}
					</section>
					<section>
						<h3>Examples</h3>
						{samples === null ? (
							<p className="empty-small">Loading examples…</p>
						) : (
							<ul className="sample-list">
								{samples.map((s) => (
									<li key={s.uuid}>
										<button
											type="button"
											className="sample"
											disabled={busy !== null}
											onClick={() => void run(s.uuid, () => openSample(s.uuid))}
										>
											<BookOpen size={16} />
											<span className="sample-text">
												<strong>{s.name}</strong>
												{s.description && <span className="subtle clamp-2">{s.description}</span>}
											</span>
											{busy === s.uuid && <span className="spinner" />}
										</button>
									</li>
								))}
							</ul>
						)}
					</section>
				</div>
			</div>
		</DialogFrame>
	)
}
