// A small Markdown renderer for circuit documentation (the desktop renders it with a Markdown viewer).
// Supports headings, paragraphs, emphasis, inline code, code blocks, block quotes, lists, tables, rules, links
// and images. All text is escaped; only http(s), mailto and data:image URLs are linked.

const escape = (s: string) =>
	s.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;')

function safeUrl(url: string, image = false): string | null {
	const u = url.trim()
	if (/^(https?:|mailto:)/i.test(u) && !image) return u
	if (/^https?:/i.test(u) || /^data:image\/(png|jpe?g|gif|svg\+xml);/i.test(u)) return u
	if (/^#/.test(u) && !image) return u
	return null
}

/** Renders inline Markdown of an already block-split line of text. */
function inline(text: string): string {
	// Code spans first, so their contents aren't formatted
	const codes: string[] = []
	let s = text.replace(/(`+)([\s\S]*?[^`])\1(?!`)/g, (_, __, code: string) => {
		codes.push(`<code>${escape(code.trim())}</code>`)
		return `\u0000${codes.length - 1}\u0000`
	})
	s = escape(s)
	// URLs may contain one level of balanced parentheses, e.g. Wikipedia links
	s = s.replace(/!\[([^\]]*)\]\(((?:[^()\s]|\([^()\s]*\))+)(?:\s+&quot;[^&]*&quot;)?\)/g, (m, alt: string, url: string) => {
		const safe = safeUrl(url.replace(/&amp;/g, '&'), true)
		return safe ? `<img src="${escape(safe)}" alt="${alt}">` : m
	})
	s = s.replace(/\[([^\]]+)\]\(((?:[^()\s]|\([^()\s]*\))+)(?:\s+&quot;[^&]*&quot;)?\)/g, (m, label: string, url: string) => {
		const safe = safeUrl(url.replace(/&amp;/g, '&'))
		return safe ? `<a href="${escape(safe)}" target="_blank" rel="noopener noreferrer">${label}</a>` : m
	})
	s = s.replace(/&lt;(https?:\/\/[^\s&]+)&gt;/g, '<a href="$1" target="_blank" rel="noopener noreferrer">$1</a>')
	s = s.replace(/(\*\*|__)(?=\S)([\s\S]*?\S)\1/g, '<strong>$2</strong>')
	s = s.replace(/(^|[^\w*])\*(?=\S)([^*]*?\S)\*/g, '$1<em>$2</em>')
	s = s.replace(/(^|[^\w])_(?=\S)([^_]*?\S)_(?!\w)/g, '$1<em>$2</em>')
	s = s.replace(/~~(?=\S)([\s\S]*?\S)~~/g, '<del>$1</del>')
	s = s.replace(/ {2,}\n|\\\n/g, '<br>')
	return s.replace(/\u0000(\d+)\u0000/g, (_, i: string) => codes[Number(i)])
}

const isRule = (line: string) => /^ {0,3}([-*_])( *\1){2,} *$/.test(line)
const listItem = /^( {0,3})([-*+]|\d{1,9}[.)])\s+(.*)$/
const tableSeparator = /^\s*\|?\s*:?-+:?\s*(\|\s*:?-+:?\s*)*\|?\s*$/

function cells(row: string): string[] {
	return row.trim().replace(/^\|/, '').replace(/\|$/, '').split(/(?<!\\)\|/).map((c) => c.trim().replace(/\\\|/g, '|'))
}

/** Converts Markdown to sanitized HTML. */
export function markdownToHtml(markdown: string): string {
	const lines = markdown.replace(/\r\n?/g, '\n').replace(/\t/g, '    ').split('\n')
	const out: string[] = []
	let i = 0
	const isBlockStart = (line: string) =>
		/^ {0,3}(#{1,6}\s|>|```|~~~)/.test(line) || isRule(line) || listItem.test(line)

	while (i < lines.length) {
		const line = lines[i]
		if (!line.trim()) {
			i++
			continue
		}
		// Fenced code
		const fence = /^ {0,3}(```|~~~)/.exec(line)
		if (fence) {
			const body: string[] = []
			i++
			while (i < lines.length && !lines[i].trimStart().startsWith(fence[1])) body.push(lines[i++])
			i++
			out.push(`<pre><code>${escape(body.join('\n'))}</code></pre>`)
			continue
		}
		// Headings
		const heading = /^ {0,3}(#{1,6})\s+(.*?)\s*#*\s*$/.exec(line)
		if (heading) {
			out.push(`<h${heading[1].length}>${inline(heading[2])}</h${heading[1].length}>`)
			i++
			continue
		}
		if (isRule(line)) {
			out.push('<hr>')
			i++
			continue
		}
		// Block quotes (rendered recursively)
		if (/^ {0,3}>/.test(line)) {
			const body: string[] = []
			while (i < lines.length && lines[i].trim() && /^ {0,3}>/.test(lines[i])) body.push(lines[i++].replace(/^ {0,3}> ?/, ''))
			out.push(`<blockquote>${markdownToHtml(body.join('\n'))}</blockquote>`)
			continue
		}
		// Lists (items may continue on indented lines; nested lists are rendered recursively)
		const first = listItem.exec(line)
		if (first) {
			const ordered = /\d/.test(first[2])
			const items: string[][] = []
			const indent = first[1].length
			while (i < lines.length) {
				const m = listItem.exec(lines[i])
				if (m && m[1].length <= indent + 1 && /\d/.test(m[2]) === ordered) {
					items.push([m[3]])
					i++
				} else if (items.length > 0 && lines[i].trim() && (/^\s{2,}/.test(lines[i]) || !isBlockStart(lines[i]))) {
					items[items.length - 1].push(lines[i].replace(/^ {2,4}/, ''))
					i++
				} else if (!lines[i].trim() && i + 1 < lines.length && /^\s{2,}\S/.test(lines[i + 1])) {
					i++
				} else break
			}
			const start = ordered && parseInt(first[2]) !== 1 ? ` start="${parseInt(first[2])}"` : ''
			const tag = ordered ? 'ol' : 'ul'
			const html = items.map((item) => {
				const [text, ...rest] = item
				const task = /^\[([ xX])\]\s+(.*)$/.exec(text)
				const head = task
					? `<input type="checkbox" disabled${task[1] !== ' ' ? ' checked' : ''}> ${inline(task[2])}`
					: inline(text)
				const nested = rest.length > 0 ? markdownToHtml(rest.join('\n')) : ''
				return `<li>${head}${nested.replace(/^<p>([\s\S]*)<\/p>$/, ' $1')}</li>`
			})
			out.push(`<${tag}${start}>${html.join('')}</${tag}>`)
			continue
		}
		// Tables
		if (line.includes('|') && i + 1 < lines.length && tableSeparator.test(lines[i + 1])) {
			const header = cells(line)
			const aligns = cells(lines[i + 1]).map((c) =>
				c.startsWith(':') && c.endsWith(':') ? 'center' : c.endsWith(':') ? 'right' : c.startsWith(':') ? 'left' : '',
			)
			const cell = (tag: string, text: string, col: number) =>
				`<${tag}${aligns[col] ? ` style="text-align:${aligns[col]}"` : ''}>${inline(text)}</${tag}>`
			i += 2
			const rows: string[] = []
			while (i < lines.length && lines[i].trim() && lines[i].includes('|')) {
				rows.push(`<tr>${cells(lines[i++]).map((c, col) => cell('td', c, col)).join('')}</tr>`)
			}
			out.push(
				`<table><thead><tr>${header.map((c, col) => cell('th', c, col)).join('')}</tr></thead><tbody>${rows.join('')}</tbody></table>`,
			)
			continue
		}
		// Paragraph, up to the next blank line or block
		const body: string[] = [line]
		i++
		while (i < lines.length && lines[i].trim() && !isBlockStart(lines[i])) {
			// Setext headings
			if (/^ {0,3}(=+|-+)\s*$/.test(lines[i])) break
			body.push(lines[i++])
		}
		const setext = i < lines.length ? /^ {0,3}(=+|-+)\s*$/.exec(lines[i]) : null
		if (setext) {
			const level = setext[1].startsWith('=') ? 1 : 2
			out.push(`<h${level}>${inline(body.join('\n'))}</h${level}>`)
			i++
		} else {
			out.push(`<p>${inline(body.join('\n'))}</p>`)
		}
	}
	return out.join('\n')
}
