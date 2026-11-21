import { useState } from 'react'
import { engine } from '../../../core/engine'
import { DialogFrame, dialogs } from '../../Dialogs'
import { libraryApi, type LibraryProperties, type PropertiesUpdate, type Translations } from './api'
import { LanguageSwitch, type LanguageCode } from './fields'

const plural = (n: number, word: string) => `${n} ${word}${n === 1 ? '' : 's'}`

/** Shows the properties of the open project or library (desktop: `ProjectPropertiesAction`, `LibraryPropertiesAction`). */
export async function showProperties(): Promise<void> {
	const properties = libraryApi.properties()
	if (!properties) return
	const editable = engine.state.mode === 'edit'
	const update = await dialogs.custom<PropertiesUpdate>((done) => (
		<PropertiesDialog properties={properties} editable={editable} done={done} />
	))
	if (update) libraryApi.setProperties(update)
}

type Tab = 'properties' | 'preferences'

/** Like the desktop's `LibraryPropertiesPanel`: translated name and description, visibility, and preferences. */
function PropertiesDialog({
	properties,
	editable,
	done,
}: {
	properties: LibraryProperties
	editable: boolean
	done: (update: PropertiesUpdate | null) => void
}) {
	const noun = properties.kind === 'project' ? 'Project' : 'Library'
	const [tab, setTab] = useState<Tab>('properties')
	const [language, setLanguage] = useState<LanguageCode>('en')
	const [name, setName] = useState<Translations>(properties.name)
	const [description, setDescription] = useState<Translations>(properties.description)
	const [visibility, setVisibility] = useState(properties.visibility)
	const [preferences, setPreferences] = useState<Record<string, string | number>>(() =>
		Object.fromEntries(properties.preferences.filter((p) => p.value !== null).map((p) => [p.key, p.value as string | number])),
	)

	const nameMissing = !Object.values(name).some((v) => v.trim())
	const rangeError = properties.preferences.find((p) => {
		const v = preferences[p.key]
		return p.type === 'integer' && (typeof v !== 'number' || Number.isNaN(v) || (p.min !== null && v < p.min) || (p.max !== null && v > p.max))
	})
	const invalid = nameMissing || !!rangeError
	const selectedVisibility = properties.visibilities.find((v) => v.id === visibility)

	const submit = () => {
		if (!invalid) done({ name, description, visibility, preferences })
	}

	return (
		<DialogFrame title={`${noun} properties`} onCancel={() => done(null)}>
			<form
				onSubmit={(e) => {
					e.preventDefault()
					submit()
				}}
			>
				<div className="tabs library-tabs" role="tablist">
					{(['properties', 'preferences'] as Tab[]).map((t) => (
						<button
							key={t}
							type="button"
							role="tab"
							aria-selected={tab === t}
							className={tab === t ? 'selected' : ''}
							onClick={() => setTab(t)}
						>
							{t === 'properties' ? 'General' : 'Simulation'}
						</button>
					))}
				</div>
				{tab === 'properties' ? (
					<div className="dialog-body library-form">
						<div className="library-language-row">
							<span className="subtle">Texts are stored in English and German.</span>
							<LanguageSwitch value={language} onChange={setLanguage} />
						</div>
						<label className="field">
							<span className="field-label">Name</span>
							<input
								className="input"
								value={name[language] ?? ''}
								readOnly={!editable}
								autoFocus
								onChange={(e) => setName({ ...name, [language]: e.target.value })}
							/>
							{nameMissing && <span className="field-error">Please enter a name.</span>}
						</label>
						<label className="field">
							<span className="field-label">Description</span>
							<textarea
								className="input"
								rows={4}
								value={description[language] ?? ''}
								readOnly={!editable}
								placeholder={`What is this ${noun.toLowerCase()} about?`}
								onChange={(e) => setDescription({ ...description, [language]: e.target.value })}
							/>
						</label>
						<label className="field">
							<span className="field-label">Visibility</span>
							<select
								className="input select"
								value={visibility}
								disabled={!editable}
								onChange={(e) => setVisibility(e.target.value)}
							>
								{properties.visibilities.map((v) => (
									<option key={v.id} value={v.id}>
										{v.label}
									</option>
								))}
							</select>
							{selectedVisibility && <span className="field-hint">{selectedVisibility.description}</span>}
						</label>
						<dl className="library-facts">
							<dt>Contents</dt>
							<dd>
								{plural(properties.circuits, 'circuit')}, {plural(properties.items, 'other item')},{' '}
								{plural(properties.folders, 'folder')}
							</dd>
							<dt>Libraries</dt>
							<dd>{properties.imports.length > 0 ? properties.imports.join(', ') : 'None'}</dd>
						</dl>
					</div>
				) : (
					<div className="dialog-body library-form">
						<p className="subtle library-intro">
							These settings are stored with the {noun.toLowerCase()} and override your own preferences, so the
							circuits simulate the same everywhere.
						</p>
						{properties.preferences.map((p) => (
							<label key={p.key} className="field">
								<span className="field-label">{p.label}</span>
								{p.type === 'choice' ? (
									<select
										className="input select"
										value={String(preferences[p.key] ?? '')}
										disabled={!editable}
										onChange={(e) => setPreferences({ ...preferences, [p.key]: e.target.value })}
									>
										{p.choices?.map((c) => (
											<option key={c.id} value={c.id}>
												{c.label}
											</option>
										))}
									</select>
								) : (
									<input
										className="input"
										type="number"
										min={p.min ?? undefined}
										max={p.max ?? undefined}
										value={Number.isNaN(preferences[p.key]) ? '' : (preferences[p.key] ?? '')}
										readOnly={!editable}
										onChange={(e) => setPreferences({ ...preferences, [p.key]: e.target.valueAsNumber })}
									/>
								)}
								{rangeError?.key === p.key && (
									<span className="field-error">
										Enter a number from {p.min} to {p.max}.
									</span>
								)}
							</label>
						))}
					</div>
				)}
				<div className="dialog-actions">
					{!editable && <span className="subtle dialog-actions-note">Stop the simulation to make changes.</span>}
					<button type="button" className="btn" onClick={() => done(null)}>
						Cancel
					</button>
					<button type="submit" className="btn btn-primary" disabled={!editable || invalid}>
						Save
					</button>
				</div>
			</form>
		</DialogFrame>
	)
}
