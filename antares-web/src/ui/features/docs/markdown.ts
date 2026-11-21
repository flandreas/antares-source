// Renders Markdown of circuit documentation and AI answers (the desktop renders it with a Markdown viewer).
// Documentation comes from imported project files, so it is untrusted: marked parses GitHub-flavored Markdown and
// DOMPurify removes everything that could run code or restyle the editor from the resulting HTML.
import DOMPurify from 'dompurify'
import { Marked } from 'marked'

const marked = new Marked({ gfm: true, async: false })

// Links open in a new tab without access to the editor; in-page anchors stay as they are
DOMPurify.addHook('afterSanitizeAttributes', (node) => {
	if (node.tagName === 'A' && !node.getAttribute('href')?.startsWith('#')) {
		node.setAttribute('target', '_blank')
		node.setAttribute('rel', 'noopener noreferrer')
	}
})

/** Converts Markdown to sanitized HTML. */
export function markdownToHtml(markdown: string): string {
	return DOMPurify.sanitize(marked.parse(markdown) as string, {
		// Inline styles and forms could imitate or cover the editor's UI
		FORBID_TAGS: ['style', 'form'],
		FORBID_ATTR: ['style'],
	})
}
