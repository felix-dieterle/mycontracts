import React from 'react'
import { FileDetail as FileDetailType, MARKER_OPTIONS } from '../types'
import { formatBytes, formatDate, prettyJson } from '../utils'
import { styles, markerBadgeStyle } from '../styles/styles'
import { apiBase } from '../utils/apiConfig'
import { isMobile, shareFile } from '../utils/mobile'

interface FileDetailProps {
  detail: FileDetailType | null
  detailState: 'idle' | 'loading' | 'error'
  selectedMarkersForDetail: string[]
  noteDraft: string
  dueDateDraft: string
  savingMarkers: boolean
  savingNote: boolean
  savingDueDate: boolean
  onMarkersChange: (markers: string[]) => void
  onNoteDraftChange: (note: string) => void
  onDueDateDraftChange: (date: string) => void
  onSaveMarkers: () => void
  onSaveNote: () => void
  onSaveDueDate: () => void
  onDelete?: () => void
}

export function FileDetail({
  detail,
  detailState,
  selectedMarkersForDetail,
  noteDraft,
  dueDateDraft,
  savingMarkers,
  savingNote,
  savingDueDate,
  onMarkersChange,
  onNoteDraftChange,
  onDueDateDraftChange,
  onSaveMarkers,
  onSaveNote,
  onSaveDueDate,
  onDelete
}: FileDetailProps) {
  return (
    <section style={styles.card}>
      <div style={styles.sectionHeader}>
        <h2 style={styles.h2}>Detail</h2>
        {detailState === 'loading' && <span style={styles.badge}>lädt...</span>}
        {detailState === 'error' && <span style={{ ...styles.badge, background: '#ffe3e3', color: '#b00020' }}>Fehler</span>}
      </div>
      {!detail && <p style={styles.muted}>Wähle eine Datei aus.</p>}
      {detail && (
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
                        onMarkersChange([...selectedMarkersForDetail, m])
                      } else {
                        onMarkersChange(selectedMarkersForDetail.filter(x => x !== m))
                      }
                    }}
                  />
                  <span style={{ ...styles.badge, ...markerBadgeStyle(m) }}>{m}</span>
                </label>
              ))}
            </div>
            <button style={styles.secondaryButton} onClick={onSaveMarkers} disabled={savingMarkers}>
              {savingMarkers ? 'Speichere...' : 'Marker speichern'}
            </button>
          </div>
          <div>
            <div style={styles.label}>Due Date (Fälligkeitsdatum)</div>
            <input
              type="date"
              value={dueDateDraft}
              onChange={e => onDueDateDraftChange(e.target.value)}
              style={styles.select}
            />
            <button style={styles.secondaryButton} onClick={onSaveDueDate} disabled={savingDueDate}>
              {savingDueDate ? 'Speichere...' : 'Fälligkeitsdatum speichern'}
            </button>
          </div>
          <div>
            <div style={styles.label}>Notiz</div>
            <textarea
              value={noteDraft}
              onChange={e => onNoteDraftChange(e.target.value)}
              placeholder="Kurze Notiz (Review, Risiken, ToDos)"
              rows={4}
              style={styles.textarea}
            />
            <button style={styles.secondaryButton} onClick={onSaveNote} disabled={savingNote}>
              {savingNote ? 'Speichere...' : 'Notiz speichern'}
            </button>
          </div>
          <div>
            <div style={styles.label}>Vertrag</div>
            {!detail.contract && <div style={styles.muted}>Nicht mit einem Vertrag verknüpft.</div>}
            {detail.contract && (
              <div>
                <div style={styles.badge}>{detail.contract.title}</div>
                <div style={{ ...styles.listMeta, marginTop: '0.5rem' }}>Contract ID: {detail.contract.id}</div>
              </div>
            )}
          </div>
          <div>
            <a href={`${apiBase}/api/files/${detail.id}/download`} style={styles.link}>Download</a>
            {isMobile() && (
              <>
                {' '}·{' '}
                <button
                  onClick={() => shareFile(`${apiBase}/api/files/${detail.id}/download`, detail.filename)}
                  style={{ ...styles.link, background: 'none', border: 'none', cursor: 'pointer', padding: 0 }}
                >
                  📤 Share
                </button>
              </>
            )}
          </div>
          {onDelete && (
            <div>
              <button 
                style={{ ...styles.secondaryButton, backgroundColor: '#fee', color: '#c00', border: '1px solid #fcc' }}
                onClick={() => {
                  if (window.confirm(`Datei "${detail.filename}" wirklich löschen?`)) {
                    onDelete()
                  }
                }}
              >
                🗑️ Datei löschen
              </button>
            </div>
          )}
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
  )
}
