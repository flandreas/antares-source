import { useState } from 'react'
import { DialogFrame } from '../../Dialogs'
import type { FolderInfo, Translations } from './api'
import { LanguageSwitch, type LanguageCode } from './fields'

const plural = (n: number, word: string) => `${n} ${word}${n === 1 ? '' : 's'}`

/** Edits the translated name of a folder, like the desktop's `LibraryFolderPropertiesPanel`. */
export function FolderPropertiesDialog({
	info,
	editable,
	done,
}: {
	info: FolderInfo
	editable: boolean
	done: (name: Translations | null) => void
}) {
	const [name, setName] = useState<Translations>(info.name)
	const [language, setLanguage] = useState<LanguageCode>('en')
	const missing = !Object.values(name).some((v) => v.trim())
	const contents = [
		info.circuits > 0 && plural(info.circuits, 'circuit'),
		info.items > 0 && plural(info.items, info.circuits > 0 ? 'other item' : 'item'),
		info.folders > 0 && plural(info.folders, 'sub-folder'),
	].filter(Boolean)
	return (
		<DialogFrame title="Folder properties" onCancel={() => done(null)}>
			<form
				onSubmit={(e) => {
					e.preventDefault()
					if (!missing) done(name)
				}}
			>
				<div className="dialog-body library-form">
					<div className="field">
						<div className="field-label-row">
							<span className="field-label">Name</span>
							<LanguageSwitch value={language} onChange={setLanguage} filled={name} />
						</div>
						<input
							className="input"
							value={name[language] ?? ''}
							readOnly={!editable}
							autoFocus
							aria-label={`Name (${language})`}
							onChange={(e) => setName({ ...name, [language]: e.target.value })}
						/>
						{missing && <div className="field-error">Please enter a name.</div>}
					</div>
					<p className="subtle library-stats">{contents.length > 0 ? `Contains ${contents.join(', ')}.` : 'This folder is empty.'}</p>
				</div>
				<div className="dialog-actions">
					<button type="button" className="btn" onClick={() => done(null)}>
						Cancel
					</button>
					<button type="submit" className="btn btn-primary" disabled={!editable || missing}>
						Save
					</button>
				</div>
			</form>
		</DialogFrame>
	)
}
