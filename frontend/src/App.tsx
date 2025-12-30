import React, { useEffect, useState } from 'react'
import { Routes, Route, Navigate, useNavigate, useParams } from 'react-router-dom'

type FileSummary = {
  id: number
  filename: string
  mime?: string
  size?: number
  checksum?: string
  createdAt?: string
  markers?: string[]
  dueDate?: string | null
  ocrStatus?: string | null
}

type OcrInfo = {
  id: number
  status: string
  createdAt?: string
  processedAt?: string
  retryCount?: number
  rawJson?: string
}

type FileDetail = FileSummary & {
  ocr?: OcrInfo | null
  note?: string | null
}

type NeedsAttention = 'ALL' | 'NEEDS_ATTENTION'
const MARKER_OPTIONS = ['URGENT', 'REVIEW', 'MISSING_INFO', 'INCOMPLETE_OCR', 'FOLLOW_UP']

const apiBase = import.meta.env.VITE_API_URL || ''

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<Navigate to="/files" replace />} />
      <Route path="/files" element={<FilesShell />} />
      <Route path="/files/:id" element={<FilesShell />} />
      <Route path="*" element={<Navigate to="/files" replace />} />
    </Routes>
  )
}

function FilesShell() {
  const params = useParams()
  const navigate = useNavigate()
  const selectedId = params.id ? Number(params.id) : null

  const [health, setHealth] = useState<'loading' | 'ok' | 'offline'>('loading')
  const [files, setFiles] = useState<FileSummary[]>([])
  const [listState, setListState] = useState<'idle' | 'loading' | 'error'>('idle')
  const [detail, setDetail] = useState<FileDetail | null>(null)
  const [detailState, setDetailState] = useState<'idle' | 'loading' | 'error'>('idle')
  const [uploading, setUploading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [markerFilter, setMarkerFilter] = useState<NeedsAttention>('ALL')
  const [ocrFilter, setOcrFilter] = useState<string>('ALL')
  const [noteDraft, setNoteDraft] = useState<string>('')
  const [savingNote, setSavingNote] = useState(false)
  const [dueDateDraft, setDueDateDraft] = useState<string>('')
  const [savingDueDate, setSavingDueDate] = useState(false)
  const [selectedMarkersForDetail, setSelectedMarkersForDetail] = useState<string[]>([])
  const [savingMarkers, setSavingMarkers] = useState(false)

  useEffect(() => {
    fetch(apiBase + '/api/health')
      .then(r => r.json())
      .then(j => setHealth(j.status === 'UP' ? 'ok' : 'offline'))
      .catch(() => setHealth('offline'))
  }, [])

  useEffect(() => {
    refreshList()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  useEffect(() => {
    if (selectedId != null) {
      loadDetail(selectedId)
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [selectedId])

  useEffect(() => {
    // If current selection is filtered out, jump to first in filtered list
    const filtered = getFiltered(files, markerFilter, ocrFilter)
    const inFiltered = selectedId && filtered.some(f => f.id === selectedId)
    if (!inFiltered && filtered.length > 0) {
      navigate(`/files/${filtered[0].id}`, { replace: true })
    }
  }, [files, markerFilter, ocrFilter, selectedId, navigate])

  async function refreshList() {
    setListState('loading')
    setError(null)
    try {
      const res = await fetch(apiBase + '/api/files')
      if (!res.ok) throw new Error('List failed')
      const data: FileSummary[] = await res.json()
      setFiles(data)

      const currentExists = selectedId && data.some(f => f.id === selectedId)
      if (data.length && (!selectedId || !currentExists)) {
        navigate(`/files/${data[0].id}`, { replace: true })
      }
      setListState('idle')
    } catch (e) {
      setListState('error')
      setError((e as Error).message)
    }
  }

  async function loadDetail(id: number) {
    setDetailState('loading')
    setError(null)
    try {
      const res = await fetch(apiBase + `/api/files/${id}`)
      if (!res.ok) throw new Error('Detail failed')
      const data: FileDetail = await res.json()
      setDetail(data)
      setNoteDraft(data.note || '')
      setSelectedMarkersForDetail(data.markers || [])
      setDueDateDraft(data.dueDate ? new Date(data.dueDate).toISOString().split('T')[0] : '')
      setDetailState('idle')
    } catch (e) {
      setDetailState('error')
      setError((e as Error).message)
    }
  }

  async function saveMarkers() {
    if (!selectedId) return
    setSavingMarkers(true)
    setError(null)
    try {
      const res = await fetch(apiBase + `/api/files/${selectedId}/markers`, {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ markers: selectedMarkersForDetail })
      })
      if (!res.ok) throw new Error('Markers update failed')
      await Promise.all([refreshList(), loadDetail(selectedId)])
    } catch (e) {
      setError((e as Error).message)
    } finally {
      setSavingMarkers(false)
    }
  }

  async function saveDueDate() {
    if (!selectedId) return
    setSavingDueDate(true)
    setError(null)
    try {
      const dueDate = dueDateDraft ? new Date(dueDateDraft + 'T00:00:00Z').toISOString() : null
      const res = await fetch(apiBase + `/api/files/${selectedId}/due-date`, {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ dueDate })
      })
      if (!res.ok) throw new Error('Due date update failed')
      await Promise.all([refreshList(), loadDetail(selectedId)])
    } catch (e) {
      setError((e as Error).message)
    } finally {
      setSavingDueDate(false)
    }
  }

  async function saveNote() {
    if (!selectedId) return
    setSavingNote(true)
    setError(null)
    try {
      const res = await fetch(apiBase + `/api/files/${selectedId}/note`, {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ note: noteDraft })
      })
      if (!res.ok) throw new Error('Note update failed')
      await loadDetail(selectedId)
    } catch (e) {
      setError((e as Error).message)
    } finally {
      setSavingNote(false)
    }
  }

  async function handleUpload(ev: React.FormEvent<HTMLFormElement>) {
    ev.preventDefault()
    const input = ev.currentTarget.elements.namedItem('file') as HTMLInputElement
    if (!input?.files || !input.files[0]) return
    const file = input.files[0]
    const body = new FormData()
    body.append('file', file)
    setUploading(true)
    setError(null)
    try {
      const res = await fetch(apiBase + '/api/files/upload', { method: 'POST', body })
      if (!res.ok) throw new Error('Upload failed')
      const created: FileSummary = await res.json()
      await refreshList()
      navigate(`/files/${created.id}`)
      input.value = ''
    } catch (e) {
      setError((e as Error).message)
    } finally {
      setUploading(false)
    }
  }

  const selectedFile = selectedId ? files.find(f => f.id === selectedId) || null : null
  const visibleFiles = getFiltered(files, markerFilter, ocrFilter)

  return (
    <div style={styles.page}>
      <header style={styles.header}>
        <div>
          <h1 style={styles.title}>mycontracts</h1>
          <p style={styles.muted}>Upload, Liste und Detail inkl. OCR</p>
        </div>
        <span style={{ ...styles.status, backgroundColor: health === 'ok' ? '#c5f6c5' : '#ffe3e3' }}>
          Backend: {health}
        </span>
      </header>

      <section style={styles.card}>
        <form onSubmit={handleUpload} style={styles.uploadRow}>
          <label style={styles.label}>
            Datei hochladen
            <input type="file" name="file" required style={styles.fileInput} />
          </label>
          <button type="submit" disabled={uploading} style={styles.primaryButton}>
            {uploading ? 'Lade...' : 'Upload'}
          </button>
        </form>
        {error && <div style={styles.error}>{error}</div>}
      </section>

      <div style={styles.grid}>
        <section style={styles.card}>
          <div style={styles.sectionHeader}>
            <h2 style={styles.h2}>Dateien</h2>
            {listState === 'loading' && <span style={styles.badge}>lädt...</span>}
            {listState === 'error' && <span style={{ ...styles.badge, background: '#ffe3e3', color: '#b00020' }}>Fehler</span>}
          </div>
          <div style={styles.filterRow}>
            <label style={styles.label}>Marker
              <select value={markerFilter} onChange={e => setMarkerFilter(e.target.value as NeedsAttention)} style={styles.select}>
                <option value="ALL">Alle</option>
                <option value="NEEDS_ATTENTION">⚠️ Needs Attention</option>
                {MARKER_OPTIONS.map(m => <option key={m} value={m}>{m}</option>)}
              </select>
            </label>
            <label style={styles.label}>OCR
              <select value={ocrFilter} onChange={e => setOcrFilter(e.target.value)} style={styles.select}>
                <option value="ALL">Alle</option>
                <option value="MATCHED">MATCHED</option>
                <option value="PENDING">PENDING</option>
                <option value="FAILED">FAILED</option>
                <option value="NONE">Keine</option>
              </select>
            </label>
          </div>
          {visibleFiles.length === 0 && <p style={styles.muted}>Keine Dateien für diesen Filter.</p>}
          <ul style={styles.list}>
            {visibleFiles.map(f => (
              <li
                key={f.id}
                style={{
                  ...styles.listItem,
                  borderColor: selectedId === f.id ? '#2d6cdf' : '#e5e7eb',
                  background: selectedId === f.id ? '#eef4ff' : '#fff'
                }}
                onClick={() => navigate(`/files/${f.id}`)}
              >
                <div style={styles.listTitle}>{f.filename}</div>
                <div style={{ display: 'flex', alignItems: 'center', gap: 6, flexWrap: 'wrap' }}>
                  <span style={styles.listMeta}>{formatBytes(f.size)} · {formatDate(f.createdAt)}</span>
                  {(f.markers || []).map(m => <span key={m} style={{ ...styles.badge, ...markerBadgeStyle(m) }}>{m}</span>)}
                  {f.dueDate && <span style={{ ...styles.badge, background: '#cffafe', color: '#164e63', borderColor: '#a5f3fc' }}>📅 {formatDate(f.dueDate)}</span>}
                  {f.ocrStatus && <span style={{ ...styles.badge, ...ocrBadgeStyle(f.ocrStatus) }}>OCR {f.ocrStatus}</span>}
                  {detail?.id === f.id && detail.note && <span style={{ ...styles.badge, background: '#f3e8ff', color: '#6b21a8', borderColor: '#e9d5ff' }}>📝 Note</span>}
                </div>
              </li>
            ))}
          </ul>
          <div style={styles.legendRow}>
            {MARKER_OPTIONS.map(m => <span key={m} style={{ ...styles.badge, ...markerBadgeStyle(m) }}>{m}</span>)}
            <span style={{ ...styles.badge, ...ocrBadgeStyle('MATCHED') }}>OCR MATCHED</span>
            <span style={{ ...styles.badge, ...ocrBadgeStyle('PENDING') }}>OCR PENDING</span>
            <span style={{ ...styles.badge, ...ocrBadgeStyle('FAILED') }}>OCR FAILED</span>
          </div>
        </section>

        <section style={styles.card}>
          <div style={styles.sectionHeader}>
            <h2 style={styles.h2}>Detail</h2>
            {detailState === 'loading' && <span style={styles.badge}>lädt...</span>}
            {detailState === 'error' && <span style={{ ...styles.badge, background: '#ffe3e3', color: '#b00020' }}>Fehler</span>}
          </div>
          {!selectedFile && <p style={styles.muted}>Wähle eine Datei aus.</p>}
          {selectedFile && detail && (
            <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
              <div>
                <div style={styles.label}>Datei</div>
                <div>{detail.filename}</div>
                <div style={styles.listMeta}>{formatBytes(detail.size)} · {detail.mime || 'unbekannt'} · {formatDate(detail.createdAt)}</div>
              </div>
              <div>
                <div style={styles.label}>Checksumme</div>
                <div style={styles.code}>{detail.checksum || 'n/a'}</div>
              </div>
              <div>
                <div style={styles.label}>Marker (Multiple)</div>
                <div style={{ display: 'flex', flexDirection: 'column', gap: 6 }}>
                  {MARKER_OPTIONS.map(m => (
                    <label key={m} style={{ display: 'flex', alignItems: 'center', gap: 8, cursor: 'pointer' }}>
                      <input
                        type="checkbox"
                        checked={selectedMarkersForDetail.includes(m)}
                        onChange={e => {
                          if (e.target.checked) {
                            setSelectedMarkersForDetail([...selectedMarkersForDetail, m])
                          } else {
                            setSelectedMarkersForDetail(selectedMarkersForDetail.filter(x => x !== m))
                          }
                        }}
                      />
                      <span style={{ ...styles.badge, ...markerBadgeStyle(m) }}>{m}</span>
                    </label>
                  ))}
                </div>
                <button style={styles.secondaryButton} onClick={saveMarkers} disabled={savingMarkers}>
                  {savingMarkers ? 'Speichere...' : 'Marker speichern'}
                </button>
              </div>
              <div>
                <div style={styles.label}>Due Date (Fälligkeitsdatum)</div>
                <input
                  type="date"
                  value={dueDateDraft}
                  onChange={e => setDueDateDraft(e.target.value)}
                  style={styles.select}
                />
                <button style={styles.secondaryButton} onClick={saveDueDate} disabled={savingDueDate}>
                  {savingDueDate ? 'Speichere...' : 'Fälligkeitsdatum speichern'}
                </button>
              </div>
              <div>
                <div style={styles.label}>Notiz</div>
                <textarea
                  value={noteDraft}
                  onChange={e => setNoteDraft(e.target.value)}
                  placeholder="Kurze Notiz (Review, Risiken, ToDos)"
                  rows={4}
                  style={styles.textarea}
                />
                <button style={styles.secondaryButton} onClick={saveNote} disabled={savingNote}>
                  {savingNote ? 'Speichere...' : 'Notiz speichern'}
                </button>
              </div>
              <div>
                <a href={`${apiBase}/api/files/${detail.id}/download`} style={styles.link}>Download</a>
              </div>
              <div>
                <div style={styles.label}>OCR</div>
                {!detail.ocr && <div style={styles.muted}>Keine OCR verknüpft.</div>}
                {detail.ocr && (
                  <div style={styles.ocrBox}>
                    <div style={styles.listMeta}>Status: {detail.ocr.status} · Versuche: {detail.ocr.retryCount ?? 0}</div>
                    <pre style={styles.pre}>{prettyJson(detail.ocr.rawJson)}</pre>
                  </div>
                )}
              </div>
            </div>
          )}
        </section>
      </div>
    </div>
  )
}

function formatBytes(size?: number) {
  if (size == null) return 'n/a'
  if (size < 1024) return `${size} B`
  const kb = size / 1024
  if (kb < 1024) return `${kb.toFixed(1)} KB`
  return `${(kb / 1024).toFixed(1)} MB`
}

function formatDate(iso?: string) {
  if (!iso) return 'n/a'
  const d = new Date(iso)
  return d.toLocaleString()
}

function getFiltered(files: FileSummary[], filter: NeedsAttention, ocr: string) {
  return files.filter(f => {
    let markerOk: boolean
    if (filter === 'NEEDS_ATTENTION') {
      const markers = f.markers || []
      markerOk = markers.some(m => m === 'URGENT' || m === 'REVIEW' || m === 'MISSING_INFO') || (f.dueDate && new Date(f.dueDate) < new Date())
    } else {
      markerOk = filter === 'ALL'
    }
    const ocrValue = f.ocrStatus || 'NONE'
    const ocrOk = ocr === 'ALL' || ocrValue === ocr
    return markerOk && ocrOk
  })
}

function prettyJson(raw?: string) {
  if (!raw) return '–'
  try {
    const parsed = JSON.parse(raw)
    return JSON.stringify(parsed, null, 2)
  } catch {
    return raw
  }
}

const styles: Record<string, React.CSSProperties> = {
  page: { fontFamily: '"Inter", system-ui, -apple-system, sans-serif', background: '#f7f9fc', minHeight: '100vh', padding: '24px', color: '#0f172a' },
  header: { display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 },
  title: { margin: 0, fontSize: 24, color: '#0f172a' },
  muted: { color: '#6b7280', margin: 0 },
  status: { padding: '6px 10px', borderRadius: 8, fontSize: 13, border: '1px solid #e5e7eb' },
  card: { background: '#fff', border: '1px solid #e5e7eb', borderRadius: 12, padding: 16, boxShadow: '0 4px 18px rgba(15,23,42,0.04)', display: 'flex', flexDirection: 'column', gap: 8 },
  uploadRow: { display: 'flex', gap: 12, alignItems: 'center', flexWrap: 'wrap' },
  label: { fontSize: 13, fontWeight: 600, color: '#475569' },
  fileInput: { marginTop: 6 },
  primaryButton: { background: '#2d6cdf', color: '#fff', border: 'none', padding: '10px 16px', borderRadius: 10, cursor: 'pointer' },
  error: { background: '#ffe3e3', color: '#b00020', padding: '8px 12px', borderRadius: 8, border: '1px solid #f5c2c7' },
  grid: { display: 'grid', gridTemplateColumns: '320px 1fr', gap: 16, alignItems: 'start', marginTop: 16 },
  sectionHeader: { display: 'flex', alignItems: 'center', gap: 8 },
  h2: { margin: 0, fontSize: 18 },
  badge: { fontSize: 12, padding: '4px 8px', borderRadius: 8, background: '#eef2ff', color: '#3730a3' },
  list: { listStyle: 'none', padding: 0, margin: 0, display: 'flex', flexDirection: 'column', gap: 8 },
  listItem: { border: '1px solid #e5e7eb', borderRadius: 10, padding: 10, cursor: 'pointer' },
  listTitle: { fontWeight: 600, color: '#0f172a' },
  listMeta: { fontSize: 12, color: '#6b7280' },
  link: { color: '#2d6cdf', textDecoration: 'none', fontWeight: 600 },
  pre: { background: '#0f172a', color: '#e2e8f0', padding: 12, borderRadius: 8, maxHeight: 260, overflow: 'auto', fontSize: 12 },
  code: { fontFamily: 'monospace', fontSize: 12, color: '#0f172a', background: '#f1f5f9', padding: '4px 6px', borderRadius: 6 },
  ocrBox: { display: 'flex', flexDirection: 'column', gap: 6 },
  select: { padding: '8px 10px', borderRadius: 8, border: '1px solid #cbd5e1', background: '#fff' },
  filterRow: { display: 'flex', gap: 12, flexWrap: 'wrap', margin: '8px 0' },
  legendRow: { display: 'flex', gap: 8, flexWrap: 'wrap', marginTop: 8 },
  textarea: { width: '100%', padding: 10, borderRadius: 10, border: '1px solid #cbd5e1', fontFamily: 'inherit', fontSize: 14, minHeight: 100 },
  secondaryButton: { marginTop: 8, background: '#f8fafc', color: '#0f172a', border: '1px solid #cbd5e1', padding: '8px 12px', borderRadius: 8, cursor: 'pointer' },
}

function markerBadgeStyle(marker: string): React.CSSProperties {
  const base = { border: '1px solid transparent' }
  switch (marker) {
    case 'URGENT':
      return { ...base, background: '#fee2e2', color: '#b91c1c', borderColor: '#fecaca' }
    case 'REVIEW':
      return { ...base, background: '#fef9c3', color: '#854d0e', borderColor: '#fde68a' }
    case 'MISSING_INFO':
      return { ...base, background: '#e0d5ff', color: '#6d28d9', borderColor: '#d8b4fe' }
    case 'INCOMPLETE_OCR':
      return { ...base, background: '#fce7f3', color: '#831843', borderColor: '#fbcfe8' }
    case 'FOLLOW_UP':
      return { ...base, background: '#dcfce7', color: '#166534', borderColor: '#bbf7d0' }
    default:
      return { ...base, background: '#e2e8f0', color: '#475569', borderColor: '#cbd5e1' }
  }
}

function ocrBadgeStyle(status: string): React.CSSProperties {
  const base = { border: '1px solid transparent' }
  switch (status) {
    case 'MATCHED':
      return { ...base, background: '#dcfce7', color: '#166534', borderColor: '#bbf7d0' }
    case 'PENDING':
      return { ...base, background: '#fef9c3', color: '#854d0e', borderColor: '#fde68a' }
    case 'FAILED':
      return { ...base, background: '#fee2e2', color: '#b91c1c', borderColor: '#fecaca' }
    default:
      return { ...base, background: '#e0f2fe', color: '#075985', borderColor: '#bae6fd' }
  }
}
