import { FileSummary, MarkerFilter, NEEDS_ATTENTION_MARKERS } from '../types'

export function formatBytes(size?: number) {
  if (size == null) return 'n/a'
  if (size < 1024) return `${size} B`
  const kb = size / 1024
  if (kb < 1024) return `${kb.toFixed(1)} KB`
  return `${(kb / 1024).toFixed(1)} MB`
}

export function formatDate(iso?: string) {
  if (!iso) return 'n/a'
  const d = new Date(iso)
  return d.toLocaleString()
}

export function getFiltered(files: FileSummary[], filter: MarkerFilter, ocr: string) {
  return files.filter(f => {
    let markerOk: boolean
    if (filter === 'NEEDS_ATTENTION') {
      const markers = f.markers || []
      markerOk = markers.some(m => NEEDS_ATTENTION_MARKERS.includes(m)) || (f.dueDate && new Date(f.dueDate) < new Date())
    } else if (filter === 'ALL') {
      markerOk = true
    } else {
      // Filter by specific marker
      const markers = f.markers || []
      markerOk = markers.includes(filter)
    }
    const ocrValue = f.ocrStatus || 'NONE'
    const ocrOk = ocr === 'ALL' || ocrValue === ocr
    return markerOk && ocrOk
  })
}

export function prettyJson(raw?: string) {
  if (!raw) return '–'
  try {
    const parsed = JSON.parse(raw)
    return JSON.stringify(parsed, null, 2)
  } catch {
    return raw
  }
}
