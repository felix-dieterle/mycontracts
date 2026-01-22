import React from 'react'
import { FileSummary } from '../types'
import { styles, markerBadgeStyle, ocrBadgeStyle } from '../styles/styles'

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
    <section style={styles.card}>
      <div style={{ ...styles.h2, marginBottom: '1rem' }}>
        📅 Tasks & Reminders
      </div>

      {tasks.length === 0 && (
        <div style={{ padding: '1rem', color: '#666' }}>
          No tasks with due dates found. Add due dates to files to see them here.
        </div>
      )}

      <div style={styles.list}>
        {tasks.map(task => {
          const overdue = isOverdue(task.dueDate)
          const selected = task.id === selectedId

          return (
            <div
              key={task.id}
              onClick={() => onSelect(task.id)}
              style={{
                ...styles.listItem,
                backgroundColor: selected ? '#eff6ff' : '#fff',
                borderLeft: overdue ? '4px solid #ef4444' : '4px solid #10b981',
                borderColor: selected ? '#2d6cdf' : (overdue ? '#ef4444' : '#10b981')
              }}
            >
              <div style={styles.listTitle}>
                {task.filename}
              </div>
              
              <div style={{ ...styles.listMeta, marginBottom: '0.5rem' }}>
                {task.size} bytes · {task.createdAt ? new Date(task.createdAt).toLocaleDateString() : 'n/a'}
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
                    <span key={m} style={{ ...styles.badge, ...markerBadgeStyle(m) }}>
                      {m}
                    </span>
                  ))}
                </div>
              )}

              {task.ocrStatus && (
                <span style={{ ...styles.badge, ...ocrBadgeStyle(task.ocrStatus) }}>
                  OCR {task.ocrStatus}
                </span>
              )}
            </div>
          )
        })}
      </div>
    </section>
  )
}
