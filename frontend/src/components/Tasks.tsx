import React from 'react'
import { FileSummary } from '../types'
import { styles } from '../styles/styles'

interface TasksProps {
  tasks: FileSummary[]
  selectedId: number | null
  onSelect: (id: number) => void
}

export function Tasks({ tasks, selectedId, onSelect }: TasksProps) {
  const now = new Date()

  const isOverdue = (dueDate: string | null) => {
    if (!dueDate) return false
    return new Date(dueDate) < now
  }

  const formatDueDate = (dueDate: string | null) => {
    if (!dueDate) return ''
    const date = new Date(dueDate)
    const diffDays = Math.ceil((date.getTime() - now.getTime()) / (1000 * 60 * 60 * 24))
    
    if (diffDays < 0) {
      return `OVERDUE (${Math.abs(diffDays)} days ago)`
    } else if (diffDays === 0) {
      return 'TODAY'
    } else if (diffDays === 1) {
      return 'Tomorrow'
    } else if (diffDays <= 7) {
      return `In ${diffDays} days`
    } else {
      return date.toLocaleDateString()
    }
  }

  return (
    <div style={styles.fileList}>
      <div style={{ ...styles.h2, marginBottom: '1rem' }}>
        📅 Tasks & Reminders
      </div>

      {tasks.length === 0 && (
        <div style={{ padding: '1rem', color: '#666' }}>
          No tasks with due dates found. Add due dates to files to see them here.
        </div>
      )}

      {tasks.map(task => {
        const overdue = isOverdue(task.dueDate)
        const selected = task.id === selectedId

        return (
          <div
            key={task.id}
            onClick={() => onSelect(task.id)}
            style={{
              ...styles.fileItem,
              ...(selected ? styles.fileItemSelected : {}),
              borderLeft: overdue ? '4px solid #ef4444' : '4px solid #10b981'
            }}
          >
            <div style={{ fontWeight: 500, marginBottom: '0.25rem' }}>
              {task.filename}
            </div>
            
            <div style={{ fontSize: '0.875rem', color: '#666', marginBottom: '0.5rem' }}>
              {task.size} bytes · {new Date(task.createdAt).toLocaleDateString()}
            </div>

            <div style={{ 
              fontSize: '0.875rem', 
              color: overdue ? '#ef4444' : '#10b981',
              fontWeight: 600,
              marginBottom: '0.5rem'
            }}>
              📅 {formatDueDate(task.dueDate)}
            </div>

            {task.markers && task.markers.length > 0 && (
              <div style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap', marginBottom: '0.5rem' }}>
                {task.markers.map(m => (
                  <span key={m} style={styles.marker}>
                    {m}
                  </span>
                ))}
              </div>
            )}

            {task.ocrStatus && (
              <span style={{
                ...styles.marker,
                backgroundColor: task.ocrStatus === 'MATCHED' ? '#10b981' :
                                 task.ocrStatus === 'PENDING' ? '#f59e0b' : '#ef4444',
                color: 'white'
              }}>
                OCR {task.ocrStatus}
              </span>
            )}
          </div>
        )
      })}
    </div>
  )
}
