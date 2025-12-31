import React from 'react'
import { FileSummary, FileDetail, MarkerFilter, MARKER_OPTIONS } from '../types'
import { formatBytes, formatDate } from '../utils'
import { styles, markerBadgeStyle, ocrBadgeStyle } from '../styles/styles'

interface FileListProps {
  files: FileSummary[]
  selectedId: number | null
  listState: 'idle' | 'loading' | 'error'
  markerFilter: MarkerFilter
  ocrFilter: string
  detail: FileDetail | null
  onSelectFile: (id: number) => void
  onMarkerFilterChange: (filter: MarkerFilter) => void
  onOcrFilterChange: (filter: string) => void
}

export function FileList({
  files,
  selectedId,
  listState,
  markerFilter,
  ocrFilter,
  detail,
  onSelectFile,
  onMarkerFilterChange,
  onOcrFilterChange
}: FileListProps) {
  return (
    <section style={styles.card}>
      <div style={styles.sectionHeader}>
        <h2 style={styles.h2}>Dateien</h2>
        {listState === 'loading' && <span style={styles.badge}>lädt...</span>}
        {listState === 'error' && <span style={{ ...styles.badge, background: '#ffe3e3', color: '#b00020' }}>Fehler</span>}
      </div>
      <div style={styles.filterRow}>
        <label style={styles.label}>Marker
          <select value={markerFilter} onChange={e => onMarkerFilterChange(e.target.value as NeedsAttention)} style={styles.select}>
            <option value="ALL">Alle</option>
            <option value="NEEDS_ATTENTION">⚠️ Needs Attention</option>
            {MARKER_OPTIONS.map(m => <option key={m} value={m}>{m}</option>)}
          </select>
        </label>
        <label style={styles.label}>OCR
          <select value={ocrFilter} onChange={e => onOcrFilterChange(e.target.value)} style={styles.select}>
            <option value="ALL">Alle</option>
            <option value="MATCHED">MATCHED</option>
            <option value="PENDING">PENDING</option>
            <option value="FAILED">FAILED</option>
            <option value="NONE">Keine</option>
          </select>
        </label>
      </div>
      {files.length === 0 && <p style={styles.muted}>Keine Dateien für diesen Filter.</p>}
      <ul style={styles.list}>
        {files.map(f => (
          <li
            key={f.id}
            style={{
              ...styles.listItem,
              borderColor: selectedId === f.id ? '#2d6cdf' : '#e5e7eb',
              background: selectedId === f.id ? '#eef4ff' : '#fff'
            }}
            onClick={() => onSelectFile(f.id)}
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
  )
}
