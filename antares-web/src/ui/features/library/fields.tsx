import type { Translations } from './api'

export type LanguageCode = 'en' | 'de'

const LANGUAGES: Array<{ code: LanguageCode; label: string }> = [
	{ code: 'en', label: 'English' },
	{ code: 'de', label: 'Deutsch' },
]

/**
 * Switches the language of translated fields (names and descriptions are stored in English and German,
 * like the desktop's `TranslatablePropertyEditor`). Languages with text are marked.
 */
export function LanguageSwitch({
	value,
	onChange,
	filled,
}: {
	value: LanguageCode
	onChange: (code: LanguageCode) => void
	filled?: Translations
}) {
	return (
		<div className="segmented small language-switch" role="tablist" aria-label="Language">
			{LANGUAGES.map((l) => (
				<button
					key={l.code}
					type="button"
					role="tab"
					aria-selected={value === l.code}
					className={value === l.code ? 'selected' : ''}
					onClick={() => onChange(l.code)}
					title={filled && !filled[l.code]?.trim() ? `${l.label} (empty)` : l.label}
				>
					{l.code.toUpperCase()}
					{filled && !filled[l.code]?.trim() && <span className="language-empty" aria-hidden="true" />}
				</button>
			))}
		</div>
	)
}
