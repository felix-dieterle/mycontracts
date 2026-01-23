import React, { useState } from 'react'
import { FileSummary } from '../types'
import { styles, markerBadgeStyle, ocrBadgeStyle } from '../styles/styles'

interface TasksProps {
  tasks: FileSummary[]
  selectedId: number | null
  onSelect: (id: number) => void
}

type TaskFilter = 'ALL' | 'OVERDUE' | 'THIS_WEEK' | 'THIS_MONTH'

export function Tasks({ tasks, selectedId, onSelect }: TasksProps) {
  const [filter, setFilter] = useState<TaskFilter>('ALL')
  const now = new Date()

  const isOverdue = (dueDate: string | null) => {
    if (!dueDate) return false
    return new Date(dueDate) < now
  }

  const isThisWeek = (dueDate: string | null) => {
    if (!dueDate) return false
    const date = new Date(dueDate)
    const weekFromNow = new Date(now.getTime() + 7 * 24 * 60 * 60 * 1000)
    return date >= now && date <= weekFromNow
  }

  const isThisMonth = (dueDate: string | null) => {
    if (!dueDate) return false
    const date = new Date(dueDate)
    const monthEnd = new Date(now.getFullYear(), now.getMonth() + 1, 0)
    return date >= now && date <= monthEnd
  }

  const getFilteredTasks = () => {
    let filtered = tasks
    
    switch (filter) {
      case 'OVERDUE':
        filtered = tasks.filter(t => isOverdue(t.dueDate))
        break
      case 'THIS_WEEK':
        filtered = tasks.filter(t => isThisWeek(t.dueDate))
        break
      case 'THIS_MONTH':
        filtered = tasks.filter(t => isThisMonth(t.dueDate))
        break
    }
    
    // Sort: overdue first, then by due date ascending, URGENT at top within each group
    return filtered.sort((a, b) => {
      const aOverdue = isOverdue(a.dueDate)
      const bOverdue = isOverdue(b.dueDate)
      const aUrgent = a.markers?.includes('URGENT')
      const bUrgent = b.markers?.includes('URGENT')
      
      // Overdue tasks first
      if (aOverdue && !bOverdue) return -1
      if (!aOverdue && bOverdue) return 1
      
      // Within same overdue status, URGENT first
      if (aUrgent && !bUrgent) return -1
      if (!aUrgent && bUrgent) return 1
      
      // Then by due date
      if (!a.dueDate || !b.dueDate) return 0
      return new Date(a.dueDate).getTime() - new Date(b.dueDate).getTime()
    })
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

  const filteredTasks = getFilteredTasks()
  const overdueCount = tasks.filter(t => isOverdue(t.dueDate)).length
  const thisWeekCount = tasks.filter(t => isThisWeek(t.dueDate)).length

  return (
    <section style={styles.card}>
      <div style={{ ...styles.h2, marginBottom: '1rem' }}>
        📅 Tasks & Reminders
      </div>

      <div style={{ marginBottom: '1rem', display: 'flex', gap: '0.5rem', flexWrap: 'wrap' }}>
        <button
          onClick={() => setFilter('ALL')}
          style={{
            ...styles.secondaryButton,
            backgroundColor: filter === 'ALL' ? '#2d6cdf' : '#fff',
            color: filter === 'ALL' ? '#fff' : '#333',
            border: filter === 'ALL' ? 'none' : '1px solid #ddd',
          }}
        >
          All ({tasks.length})
        </button>
        <button
          onClick={() => setFilter('OVERDUE')}
          style={{
            ...styles.secondaryButton,
            backgroundColor: filter === 'OVERDUE' ? '#ef4444' : '#fff',
            color: filter === 'OVERDUE' ? '#fff' : '#333',
            border: filter === 'OVERDUE' ? 'none' : '1px solid #ddd',
          }}
        >
          🔴 Overdue ({overdueCount})
        </button>
        <button
          onClick={() => setFilter('THIS_WEEK')}
          style={{
            ...styles.secondaryButton,
            backgroundColor: filter === 'THIS_WEEK' ? '#10b981' : '#fff',
            color: filter === 'THIS_WEEK' ? '#fff' : '#333',
            border: filter === 'THIS_WEEK' ? 'none' : '1px solid #ddd',
          }}
        >
          This Week ({thisWeekCount})
        </button>
        <button
          onClick={() => setFilter('THIS_MONTH')}
          style={{
            ...styles.secondaryButton,
            backgroundColor: filter === 'THIS_MONTH' ? '#3b82f6' : '#fff',
            color: filter === 'THIS_MONTH' ? '#fff' : '#333',
            border: filter === 'THIS_MONTH' ? 'none' : '1px solid #ddd',
          }}
        >
          This Month
        </button>
      </div>

      {filteredTasks.length === 0 && (
        <div style={{ padding: '1rem', color: '#666' }}>
          {filter === 'ALL' 
            ? 'No tasks with due dates found. Add due dates to files to see them here.'
            : `No ${filter.toLowerCase().replace('_', ' ')} tasks.`
          }
        </div>
      )}

      <div style={styles.list}>
        {filteredTasks.map(task => {
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
