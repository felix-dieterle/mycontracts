import React, { useState } from 'react'
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
  bulkSelection?: number[]
  onBulkSelectionChange?: (ids: number[]) => void
  onBulkAction?: (action: 'markers' | 'dueDate' | 'note') => void
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
  onOcrFilterChange,
  bulkSelection = [],
  onBulkSelectionChange,
  onBulkAction
}: FileListProps) {
  const bulkMode = bulkSelection.length > 0

  const toggleBulkSelection = (id: number) => {
    if (!onBulkSelectionChange) return
    if (bulkSelection.includes(id)) {
      onBulkSelectionChange(bulkSelection.filter(x => x !== id))
    } else {
      onBulkSelectionChange([...bulkSelection, id])
    }
  }

  const selectAll = () => {
    if (!onBulkSelectionChange) return
    onBulkSelectionChange(files.map(f => f.id))
  }

  const clearSelection = () => {
    if (!onBulkSelectionChange) return
    onBulkSelectionChange([])
  }

  return (
    <section style={styles.card}>
      <div style={styles.sectionHeader}>
        <h2 style={styles.h2}>Dateien</h2>
        {listState === 'loading' && <span style={styles.badge}>lädt...</span>}
        {listState === 'error' && <span style={{ ...styles.badge, background: '#ffe3e3', color: '#b00020' }}>Fehler</span>}
      </div>

      {onBulkSelectionChange && (
        <div style={{ marginBottom: '0.75rem', display: 'flex', gap: '0.5rem', flexWrap: 'wrap' }}>
          <button 
            style={{ ...styles.secondaryButton, fontSize: '0.875rem', padding: '0.375rem 0.75rem' }}
            onClick={selectAll}
          >
            ☑️ All
          </button>
          <button 
            style={{ ...styles.secondaryButton, fontSize: '0.875rem', padding: '0.375rem 0.75rem' }}
            onClick={clearSelection}
          >
            ☐ None
          </button>
          {bulkSelection.length > 0 && (
            <>
              <span style={{ ...styles.badge, backgroundColor: '#dbeafe', color: '#1e40af' }}>
                {bulkSelection.length} selected
              </span>
              {onBulkAction && (
                <>
                  <button
                    style={{ ...styles.primaryButton, fontSize: '0.875rem', padding: '0.375rem 0.75rem' }}
                    onClick={() => onBulkAction('markers')}
                  >
                    Set Markers
                  </button>
                  <button
                    style={{ ...styles.primaryButton, fontSize: '0.875rem', padding: '0.375rem 0.75rem' }}
                    onClick={() => onBulkAction('dueDate')}
                  >
                    Set Due Date
                  </button>
                </>
              )}
            </>
          )}
        </div>
      )}

      <div style={styles.filterRow}>
        <label style={styles.label}>Marker
          <select value={markerFilter} onChange={e => onMarkerFilterChange(e.target.value as MarkerFilter)} style={styles.select}>
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
              background: bulkSelection.includes(f.id) ? '#fef3c7' : (selectedId === f.id ? '#eef4ff' : '#fff')
            }}
            onClick={() => onSelectFile(f.id)}
          >
            {onBulkSelectionChange && (
              <input
                type="checkbox"
                checked={bulkSelection.includes(f.id)}
                onChange={(e) => {
                  e.stopPropagation()
                  toggleBulkSelection(f.id)
                }}
                style={{ marginRight: '0.5rem', cursor: 'pointer' }}
                onClick={(e) => e.stopPropagation()}
              />
            )}
            <div style={{ flex: 1 }}>
              <div style={styles.listTitle}>{f.filename}</div>
              <div style={{ display: 'flex', alignItems: 'center', gap: 6, flexWrap: 'wrap' }}>
                <span style={styles.listMeta}>{formatBytes(f.size)} · {formatDate(f.createdAt)}</span>
                {(f.markers || []).map(m => <span key={m} style={{ ...styles.badge, ...markerBadgeStyle(m) }}>{m}</span>)}
                {f.dueDate && <span style={{ ...styles.badge, background: '#cffafe', color: '#164e63', borderColor: '#a5f3fc' }}>📅 {formatDate(f.dueDate)}</span>}
                {f.ocrStatus && <span style={{ ...styles.badge, ...ocrBadgeStyle(f.ocrStatus) }}>OCR {f.ocrStatus}</span>}
                {detail?.id === f.id && detail.note && <span style={{ ...styles.badge, background: '#f3e8ff', color: '#6b21a8', borderColor: '#e9d5ff' }}>📝 Note</span>}
              </div>
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
