import React from 'react'
import { FileSummary } from '../types'
import { styles } from '../styles/styles'

interface DashboardProps {
  files: FileSummary[]
}

export function Dashboard({ files }: DashboardProps) {
  const now = new Date()
  const in30Days = new Date(Date.now() + 30 * 24 * 60 * 60 * 1000)
  
  const overdue = files.filter(f => f.dueDate && new Date(f.dueDate) < now).length
  const needsAttention = files.filter(f => {
    const markers = f.markers || []
    return markers.includes('URGENT') || markers.includes('REVIEW') || markers.includes('MISSING_INFO') || (f.dueDate && new Date(f.dueDate) < now)
  }).length
  const upcomingDueDates = files.filter(f => f.dueDate && new Date(f.dueDate) < in30Days).length
  const ocrIssues = files.filter(f => f.ocrStatus === 'PENDING' || f.ocrStatus === 'FAILED').length
  const missingInfo = files.filter(f => (f.markers || []).includes('MISSING_INFO')).length
  const needsCategorization = files.filter(f => !(f.markers || []).length && !f.dueDate && !f.note).length
  const urgent = files.filter(f => (f.markers || []).includes('URGENT')).length

  return (
    <section style={styles.dashboardCard}>
      <h2 style={styles.h2}>📊 Optimierungs-Cockpit</h2>
      <div style={styles.dashboardGrid}>
        <div style={styles.insightCard}>
          <div style={styles.insightTitle}>⚠️ Handlungsbedarf</div>
          <div style={styles.insightValue}>{needsAttention}</div>
          <div style={styles.insightLabel}>Verträge benötigen Aufmerksamkeit</div>
        </div>
        <div style={styles.insightCard}>
          <div style={styles.insightTitle}>📅 Fälligkeiten</div>
          <div style={styles.insightValue}>{upcomingDueDates}</div>
          <div style={styles.insightLabel}>In den nächsten 30 Tagen</div>
        </div>
        <div style={styles.insightCard}>
          <div style={styles.insightTitle}>🔍 OCR-Status</div>
          <div style={styles.insightValue}>{ocrIssues}</div>
          <div style={styles.insightLabel}>Benötigen OCR-Überprüfung</div>
        </div>
        <div style={styles.insightCard}>
          <div style={styles.insightTitle}>📋 Gesamt</div>
          <div style={styles.insightValue}>{files.length}</div>
          <div style={styles.insightLabel}>Verträge im System</div>
        </div>
      </div>
      <div style={styles.optimizationTips}>
        <div style={styles.tipsTitle}>💡 Optimierungsempfehlungen</div>
        <ul style={styles.tipsList}>
          {overdue > 0 && (
            <li style={styles.tipItem}>🔴 {overdue} überfällige Verträge prüfen</li>
          )}
          {missingInfo > 0 && (
            <li style={styles.tipItem}>🟣 {missingInfo} Verträge mit unvollständigen Informationen vervollständigen</li>
          )}
          {ocrIssues > 0 && (
            <li style={styles.tipItem}>🔍 {ocrIssues} OCR-Prozesse überprüfen</li>
          )}
          {needsCategorization > 0 && (
            <li style={styles.tipItem}>📝 {needsCategorization} Verträge kategorisieren und Fälligkeiten setzen</li>
          )}
          {overdue === 0 && urgent === 0 && (
            <li style={styles.tipItem}>✅ Alle kritischen Punkte sind bearbeitet</li>
          )}
        </ul>
      </div>
    </section>
  )
}
