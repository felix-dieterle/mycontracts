import React from 'react'

export const styles: Record<string, React.CSSProperties> = {
  page: { fontFamily: '"Inter", system-ui, -apple-system, sans-serif', background: '#f7f9fc', minHeight: '100vh', padding: '24px', color: '#0f172a' },
  header: { display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 },
  title: { margin: 0, fontSize: 24, color: '#0f172a' },
  muted: { color: '#6b7280', margin: 0 },
  status: { padding: '6px 10px', borderRadius: 8, fontSize: 13, border: '1px solid #e5e7eb' },
  card: { background: '#fff', border: '1px solid #e5e7eb', borderRadius: 12, padding: 16, boxShadow: '0 4px 18px rgba(15,23,42,0.04)', display: 'flex', flexDirection: 'column', gap: 8 },
  dashboardCard: { background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)', border: '1px solid #e5e7eb', borderRadius: 12, padding: 20, boxShadow: '0 4px 18px rgba(15,23,42,0.12)', display: 'flex', flexDirection: 'column', gap: 16, color: '#fff' },
  dashboardGrid: { display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: 12 },
  insightCard: { background: 'rgba(255,255,255,0.15)', backdropFilter: 'blur(10px)', borderRadius: 10, padding: 16, border: '1px solid rgba(255,255,255,0.2)', display: 'flex', flexDirection: 'column', gap: 6 },
  insightTitle: { fontSize: 13, fontWeight: 600, opacity: 0.9 },
  insightValue: { fontSize: 32, fontWeight: 700 },
  insightLabel: { fontSize: 12, opacity: 0.8 },
  optimizationTips: { background: 'rgba(255,255,255,0.1)', borderRadius: 10, padding: 16, border: '1px solid rgba(255,255,255,0.2)' },
  tipsTitle: { fontSize: 15, fontWeight: 600, marginBottom: 12 },
  tipsList: { margin: 0, paddingLeft: 20, display: 'flex', flexDirection: 'column', gap: 6 },
  tipItem: { fontSize: 14, lineHeight: '1.5' },
  uploadRow: { display: 'flex', gap: 12, alignItems: 'center', flexWrap: 'wrap' },
  label: { fontSize: 13, fontWeight: 600, color: '#475569' },
  fileInput: { marginTop: 6 },
  primaryButton: { background: '#2d6cdf', color: '#fff', border: 'none', padding: '10px 16px', borderRadius: 10, cursor: 'pointer' },
  button: { background: '#2d6cdf', color: '#fff', border: 'none', padding: '10px 16px', borderRadius: 10, cursor: 'pointer' },
  error: { background: '#ffe3e3', color: '#b00020', padding: '8px 12px', borderRadius: 8, border: '1px solid #f5c2c7' },
  grid: { display: 'grid', gridTemplateColumns: '320px 1fr', gap: 16, alignItems: 'start', marginTop: 16 },
  panel: { background: '#fff', border: '1px solid #e5e7eb', borderRadius: 12, boxShadow: '0 4px 18px rgba(15,23,42,0.04)' },
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

export function markerBadgeStyle(marker: string): React.CSSProperties {
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

export function ocrBadgeStyle(status: string): React.CSSProperties {
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
