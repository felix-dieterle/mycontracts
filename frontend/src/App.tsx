import React, { useEffect, useState } from 'react'
import { Routes, Route, Navigate, useNavigate, useParams, useLocation } from 'react-router-dom'
import { FileSummary, FileDetail, MarkerFilter } from './types'
import { apiBase } from './utils/apiConfig'
import { getFiltered } from './utils'
import { getResponsiveStyles, isMobileScreen } from './styles/styles'
import { Dashboard } from './components/Dashboard'
import { FileList } from './components/FileList'
import { FileDetail as FileDetailComponent } from './components/FileDetail'
import { Chat } from './components/Chat'
import { Tasks } from './components/Tasks'
import MobileFileUpload from './components/MobileFileUpload'
import { isMobile } from './utils/mobile'

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<Navigate to="/files" replace />} />
      <Route path="/files" element={<FilesShell />} />
      <Route path="/files/:id" element={<FilesShell />} />
      <Route path="/tasks" element={<TasksShell />} />
      <Route path="/tasks/:id" element={<TasksShell />} />
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
  const [uploadStatus, setUploadStatus] = useState<'idle' | 'uploading' | 'success' | 'error'>('idle')
  const [error, setError] = useState<string | null>(null)
  const [markerFilter, setMarkerFilter] = useState<MarkerFilter>('ALL')
  const [ocrFilter, setOcrFilter] = useState<string>('ALL')
  const [noteDraft, setNoteDraft] = useState<string>('')
  const [savingNote, setSavingNote] = useState(false)
  const [dueDateDraft, setDueDateDraft] = useState<string>('')
  const [savingDueDate, setSavingDueDate] = useState(false)
  const [selectedMarkersForDetail, setSelectedMarkersForDetail] = useState<string[]>([])
  const [savingMarkers, setSavingMarkers] = useState(false)
  const [bulkSelection, setBulkSelection] = useState<number[]>([])

  // Get responsive styles
  const styles = getResponsiveStyles()
  const isMobileView = isMobileScreen()

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

  async function deleteFile() {
    if (!selectedId) return
    setError(null)
    try {
      const res = await fetch(apiBase + `/api/files/${selectedId}`, { method: 'DELETE' })
      if (!res.ok) throw new Error('Delete failed')
      await refreshList()
      // Navigate to first file or files list
      if (files.length > 1) {
        const remaining = files.filter(f => f.id !== selectedId)
        if (remaining.length > 0) {
          navigate(`/files/${remaining[0].id}`)
        } else {
          navigate('/files')
        }
      } else {
        navigate('/files')
      }
    } catch (e) {
      setError((e as Error).message)
    }
  }

  async function handleBulkAction(action: 'markers' | 'dueDate' | 'note') {
    if (bulkSelection.length === 0) return
    
    if (action === 'markers') {
      const markers = window.prompt('Enter markers (comma-separated): URGENT,REVIEW,MISSING_INFO,INCOMPLETE_OCR,FOLLOW_UP')
      if (markers === null) return
      const markerList = markers.split(',').map(m => m.trim()).filter(m => m)
      
      setError(null)
      try {
        const res = await fetch(apiBase + '/api/files/bulk/markers', {
          method: 'PATCH',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ fileIds: bulkSelection, markers: markerList })
        })
        if (!res.ok) throw new Error('Bulk markers update failed')
        await refreshList()
        setBulkSelection([])
      } catch (e) {
        setError((e as Error).message)
      }
    } else if (action === 'dueDate') {
      const dateStr = window.prompt('Enter due date (YYYY-MM-DD):')
      if (dateStr === null) return
      const dueDate = dateStr ? new Date(dateStr + 'T00:00:00Z').toISOString() : null
      
      setError(null)
      try {
        const res = await fetch(apiBase + '/api/files/bulk/due-date', {
          method: 'PATCH',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ fileIds: bulkSelection, dueDate })
        })
        if (!res.ok) throw new Error('Bulk due date update failed')
        await refreshList()
        setBulkSelection([])
      } catch (e) {
        setError((e as Error).message)
      }
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
    setUploadStatus('uploading')
    setError(null)
    try {
      const res = await fetch(apiBase + '/api/files/upload', { method: 'POST', body })
      if (!res.ok) throw new Error('Upload failed')
      const created: FileSummary = await res.json()
      await refreshList()
      navigate(`/files/${created.id}`)
      input.value = ''
      setUploadStatus('success')
      setTimeout(() => setUploadStatus('idle'), 3000)
    } catch (e) {
      setError((e as Error).message)
      setUploadStatus('error')
      setTimeout(() => setUploadStatus('idle'), 5000)
    } finally {
      setUploading(false)
    }
  }

  async function handleMobileUpload(files: FileList | null) {
    if (!files || !files[0]) return
    const file = files[0]
    const body = new FormData()
    body.append('file', file)
    setUploading(true)
    setUploadStatus('uploading')
    setError(null)
    try {
      const res = await fetch(apiBase + '/api/files/upload', { method: 'POST', body })
      if (!res.ok) throw new Error('Upload failed')
      const created: FileSummary = await res.json()
      await refreshList()
      navigate(`/files/${created.id}`)
      setUploadStatus('success')
      setTimeout(() => setUploadStatus('idle'), 3000)
    } catch (e) {
      setError((e as Error).message)
      setUploadStatus('error')
      setTimeout(() => setUploadStatus('idle'), 5000)
    } finally {
      setUploading(false)
    }
  }

  const visibleFiles = getFiltered(files, markerFilter, ocrFilter)

  return (
    <div style={styles.page}>
      <header style={styles.header}>
        <div>
          <h1 style={styles.title}>⚙️ Vertrags-Cockpit</h1>
          <p style={styles.muted}>Vertragsoptimierung & Zukunftsplanung</p>
        </div>
        <div style={{ display: 'flex', gap: isMobileView ? '0.5rem' : '1rem', alignItems: 'center', flexWrap: 'wrap' }}>
          <button 
            onClick={() => navigate('/files')} 
            style={styles.primaryButton}
          >
            📄 Files
          </button>
          <button 
            onClick={() => navigate('/tasks')} 
            style={styles.primaryButton}
          >
            📅 Tasks
          </button>
          <span style={{ ...styles.status, backgroundColor: health === 'ok' ? '#c5f6c5' : '#ffe3e3' }}>
            Backend: {health}
          </span>
        </div>
      </header>

      <section style={styles.card}>
        {isMobile() ? (
          <MobileFileUpload 
            onFilesSelected={handleMobileUpload}
            uploadStatus={uploadStatus}
          />
        ) : (
          <form onSubmit={handleUpload} style={styles.uploadRow}>
            <label style={styles.label}>
              Datei hochladen
              <input type="file" name="file" required style={styles.fileInput} />
            </label>
            <button type="submit" disabled={uploading} style={styles.primaryButton}>
              {uploading ? 'Lade...' : 'Upload'}
            </button>
          </form>
        )}
        {error && <div style={styles.error}>{error}</div>}
      </section>

      <Dashboard files={files} />

      <div style={{ 
        ...styles.grid, 
        gridTemplateColumns: isMobileView ? '1fr' : '320px 1fr 400px'
      }}>
        <FileList
          files={visibleFiles}
          selectedId={selectedId}
          listState={listState}
          markerFilter={markerFilter}
          ocrFilter={ocrFilter}
          detail={detail}
          onSelectFile={(id) => navigate(`/files/${id}`)}
          onMarkerFilterChange={setMarkerFilter}
          onOcrFilterChange={setOcrFilter}
          bulkSelection={bulkSelection}
          onBulkSelectionChange={setBulkSelection}
          onBulkAction={handleBulkAction}
        />

        <FileDetailComponent
          detail={detail}
          detailState={detailState}
          selectedMarkersForDetail={selectedMarkersForDetail}
          noteDraft={noteDraft}
          dueDateDraft={dueDateDraft}
          savingMarkers={savingMarkers}
          savingNote={savingNote}
          savingDueDate={savingDueDate}
          onMarkersChange={setSelectedMarkersForDetail}
          onNoteDraftChange={setNoteDraft}
          onDueDateDraftChange={setDueDateDraft}
          onSaveMarkers={saveMarkers}
          onSaveNote={saveNote}
          onSaveDueDate={saveDueDate}
          onDelete={deleteFile}
        />

        <Chat fileId={selectedId} filename={detail?.filename} />
      </div>
    </div>
  )
}

function TasksShell() {
  const params = useParams()
  const navigate = useNavigate()
  const selectedId = params.id ? Number(params.id) : null

  const [health, setHealth] = useState<'loading' | 'ok' | 'offline'>('loading')
  const [tasks, setTasks] = useState<FileSummary[]>([])
  const [listState, setListState] = useState<'idle' | 'loading' | 'error'>('idle')
  const [detail, setDetail] = useState<FileDetail | null>(null)
  const [detailState, setDetailState] = useState<'idle' | 'loading' | 'error'>('idle')
  const [error, setError] = useState<string | null>(null)
  const [noteDraft, setNoteDraft] = useState<string>('')
  const [savingNote, setSavingNote] = useState(false)
  const [dueDateDraft, setDueDateDraft] = useState<string>('')
  const [savingDueDate, setSavingDueDate] = useState(false)
  const [selectedMarkersForDetail, setSelectedMarkersForDetail] = useState<string[]>([])
  const [savingMarkers, setSavingMarkers] = useState(false)

  // Get responsive styles
  const styles = getResponsiveStyles()
  const isMobileView = isMobileScreen()

  useEffect(() => {
    fetch(apiBase + '/api/health')
      .then(r => r.json())
      .then(j => setHealth(j.status === 'UP' ? 'ok' : 'offline'))
      .catch(() => setHealth('offline'))
  }, [])

  useEffect(() => {
    refreshTasks()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  useEffect(() => {
    if (selectedId != null) {
      loadDetail(selectedId)
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [selectedId])

  async function refreshTasks() {
    setListState('loading')
    setError(null)
    try {
      const res = await fetch(apiBase + '/api/files/tasks')
      if (!res.ok) throw new Error('Failed to load tasks')
      const data: FileSummary[] = await res.json()
      setTasks(data)

      const currentExists = selectedId && data.some(f => f.id === selectedId)
      if (data.length && (!selectedId || !currentExists)) {
        navigate(`/tasks/${data[0].id}`, { replace: true })
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
      setDueDateDraft(data.dueDate ? new Date(data.dueDate).toISOString().split('T')[0] : '')
      setSelectedMarkersForDetail(data.markers || [])
      setDetailState('idle')
    } catch (e) {
      setDetailState('error')
      setError((e as Error).message)
    }
  }

  async function saveMarkers() {
    if (!selectedId) return
    setSavingMarkers(true)
    try {
      const res = await fetch(apiBase + `/api/files/${selectedId}/markers`, {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ markers: selectedMarkersForDetail })
      })
      if (!res.ok) throw new Error('Failed to save markers')
      await refreshTasks()
      await loadDetail(selectedId)
    } catch (e) {
      setError((e as Error).message)
    } finally {
      setSavingMarkers(false)
    }
  }

  async function saveDueDate() {
    if (!selectedId) return
    setSavingDueDate(true)
    try {
      const body = dueDateDraft
        ? JSON.stringify({ dueDate: new Date(dueDateDraft).toISOString() })
        : JSON.stringify({ dueDate: null })
      const res = await fetch(apiBase + `/api/files/${selectedId}/due-date`, {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json' },
        body
      })
      if (!res.ok) throw new Error('Failed to save due date')
      await refreshTasks()
      await loadDetail(selectedId)
    } catch (e) {
      setError((e as Error).message)
    } finally {
      setSavingDueDate(false)
    }
  }

  async function saveNote() {
    if (!selectedId) return
    setSavingNote(true)
    try {
      const res = await fetch(apiBase + `/api/files/${selectedId}/note`, {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ note: noteDraft })
      })
      if (!res.ok) throw new Error('Failed to save note')
      await refreshTasks()
      await loadDetail(selectedId)
    } catch (e) {
      setError((e as Error).message)
    } finally {
      setSavingNote(false)
    }
  }

  return (
    <div style={styles.page}>
      <header style={styles.header}>
        <div>
          <h1 style={styles.title}>⚙️ Vertrags-Cockpit</h1>
          <p style={styles.muted}>Vertragsoptimierung & Zukunftsplanung</p>
        </div>
        <div style={{ display: 'flex', gap: isMobileView ? '0.5rem' : '1rem', alignItems: 'center', flexWrap: 'wrap' }}>
          <button 
            onClick={() => navigate('/files')} 
            style={styles.primaryButton}
          >
            📄 Files
          </button>
          <button 
            onClick={() => navigate('/tasks')} 
            style={styles.primaryButton}
          >
            📅 Tasks
          </button>
          <span style={{ ...styles.status, backgroundColor: health === 'ok' ? '#c5f6c5' : '#ffe3e3' }}>
            Backend: {health}
          </span>
        </div>
      </header>

      {error && (
        <section style={styles.card}>
          <div style={styles.error}>{error}</div>
        </section>
      )}

      <div style={{ 
        ...styles.grid, 
        gridTemplateColumns: isMobileView ? '1fr' : '320px 1fr 400px'
      }}>
        <Tasks
          tasks={tasks}
          selectedId={selectedId}
          onSelect={(id) => navigate(`/tasks/${id}`)}
        />

        <FileDetailComponent
          detail={detail}
          detailState={detailState}
          selectedMarkersForDetail={selectedMarkersForDetail}
          noteDraft={noteDraft}
          dueDateDraft={dueDateDraft}
          savingMarkers={savingMarkers}
          savingNote={savingNote}
          savingDueDate={savingDueDate}
          onMarkersChange={setSelectedMarkersForDetail}
          onNoteDraftChange={setNoteDraft}
          onDueDateDraftChange={setDueDateDraft}
          onSaveMarkers={saveMarkers}
          onSaveNote={saveNote}
          onSaveDueDate={saveDueDate}
        />

        <Chat fileId={selectedId} filename={detail?.filename} />
      </div>
    </div>
  )
}
