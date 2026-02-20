import React from 'react'

/**
 * Mobile breakpoint in pixels
 */
export const MOBILE_BREAKPOINT = 768

/**
 * Detect if the current device is mobile based on screen width
 * Note: This detection happens at render time and won't automatically update on window resize.
 * For dynamic updates on resize, components should use React state with a resize event listener.
 */
export const isMobileScreen = (): boolean => {
  return typeof window !== 'undefined' && window.innerWidth < MOBILE_BREAKPOINT
}

/**
 * Get responsive styles based on screen size
 * Note: Styles are calculated at render time. For automatic updates on window resize,
 * components should trigger re-renders using React state and resize event listeners.
 */
export const getResponsiveStyles = (): Record<string, React.CSSProperties> => {
  const isMobile = isMobileScreen()
  
  return {
    page: { 
      fontFamily: '"Inter", system-ui, -apple-system, sans-serif', 
      background: '#f7f9fc', 
      minHeight: '100vh', 
      paddingTop: isMobile ? 'max(12px, env(safe-area-inset-top))' : '24px',
      paddingBottom: isMobile ? 'max(12px, env(safe-area-inset-bottom))' : '24px',
      paddingLeft: isMobile ? 'max(12px, env(safe-area-inset-left))' : '24px',
      paddingRight: isMobile ? 'max(12px, env(safe-area-inset-right))' : '24px',
      color: '#0f172a',
      width: '100%',
      maxWidth: '100vw',
      overflowX: 'hidden'
    },
    header: { 
      display: 'flex', 
      justifyContent: 'space-between', 
      alignItems: isMobile ? 'flex-start' : 'center', 
      marginBottom: 16,
      flexDirection: isMobile ? 'column' : 'row',
      gap: isMobile ? '12px' : '0'
    },
    title: { 
      margin: 0, 
      fontSize: isMobile ? 20 : 24, 
      color: '#0f172a' 
    },
    muted: { 
      color: '#6b7280', 
      margin: 0,
      fontSize: isMobile ? 12 : 14
    },
    status: { 
      padding: '6px 10px', 
      borderRadius: 8, 
      fontSize: isMobile ? 11 : 13, 
      border: '1px solid #e5e7eb',
      whiteSpace: 'nowrap'
    },
    card: { 
      background: '#fff', 
      border: '1px solid #e5e7eb', 
      borderRadius: 12, 
      padding: isMobile ? 12 : 16, 
      boxShadow: '0 4px 18px rgba(15,23,42,0.04)', 
      display: 'flex', 
      flexDirection: 'column', 
      gap: 8 
    },
    dashboardCard: { 
      background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)', 
      border: '1px solid #e5e7eb', 
      borderRadius: 12, 
      padding: isMobile ? 16 : 20, 
      boxShadow: '0 4px 18px rgba(15,23,42,0.12)', 
      display: 'flex', 
      flexDirection: 'column', 
      gap: 16, 
      color: '#fff' 
    },
    dashboardGrid: { 
      display: 'grid', 
      gridTemplateColumns: isMobile ? '1fr' : 'repeat(auto-fit, minmax(200px, 1fr))', 
      gap: 12 
    },
    insightCard: { 
      background: 'rgba(255,255,255,0.15)', 
      backdropFilter: 'blur(10px)', 
      borderRadius: 10, 
      padding: isMobile ? 12 : 16, 
      border: '1px solid rgba(255,255,255,0.2)', 
      display: 'flex', 
      flexDirection: 'column', 
      gap: 6 
    },
    insightTitle: { 
      fontSize: isMobile ? 11 : 13, 
      fontWeight: 600, 
      opacity: 0.9 
    },
    insightValue: { 
      fontSize: isMobile ? 24 : 32, 
      fontWeight: 700 
    },
    insightLabel: { 
      fontSize: isMobile ? 11 : 12, 
      opacity: 0.8 
    },
    optimizationTips: { 
      background: 'rgba(255,255,255,0.1)', 
      borderRadius: 10, 
      padding: isMobile ? 12 : 16, 
      border: '1px solid rgba(255,255,255,0.2)' 
    },
    tipsTitle: { 
      fontSize: isMobile ? 13 : 15, 
      fontWeight: 600, 
      marginBottom: 12 
    },
    tipsList: { 
      margin: 0, 
      paddingLeft: isMobile ? 16 : 20, 
      display: 'flex', 
      flexDirection: 'column', 
      gap: 6 
    },
    tipItem: { 
      fontSize: isMobile ? 13 : 14, 
      lineHeight: '1.5' 
    },
    uploadRow: { 
      display: 'flex', 
      gap: 12, 
      alignItems: 'center', 
      flexWrap: 'wrap' 
    },
    label: { 
      fontSize: isMobile ? 12 : 13, 
      fontWeight: 600, 
      color: '#475569' 
    },
    fileInput: { marginTop: 6 },
    primaryButton: { 
      background: '#2d6cdf', 
      color: '#fff', 
      border: 'none', 
      padding: isMobile ? '8px 12px' : '10px 16px', 
      borderRadius: 10, 
      cursor: 'pointer',
      fontSize: isMobile ? 13 : 14,
      whiteSpace: 'nowrap'
    },
    button: { 
      background: '#2d6cdf', 
      color: '#fff', 
      border: 'none', 
      padding: isMobile ? '8px 12px' : '10px 16px', 
      borderRadius: 10, 
      cursor: 'pointer',
      fontSize: isMobile ? 13 : 14
    },
    error: { 
      background: '#ffe3e3', 
      color: '#b00020', 
      padding: '8px 12px', 
      borderRadius: 8, 
      border: '1px solid #f5c2c7',
      fontSize: isMobile ? 12 : 14
    },
    grid: { 
      display: 'grid', 
      gridTemplateColumns: isMobile ? '1fr' : '320px 1fr', 
      gap: isMobile ? 12 : 16, 
      alignItems: 'start', 
      marginTop: 16,
      width: '100%'
    },
    panel: { 
      background: '#fff', 
      border: '1px solid #e5e7eb', 
      borderRadius: 12, 
      boxShadow: '0 4px 18px rgba(15,23,42,0.04)',
      width: '100%'
    },
    sectionHeader: { 
      display: 'flex', 
      alignItems: 'center', 
      gap: 8,
      flexWrap: 'wrap'
    },
    h2: { 
      margin: 0, 
      fontSize: isMobile ? 16 : 18 
    },
    badge: { 
      fontSize: isMobile ? 11 : 12, 
      padding: '4px 8px', 
      borderRadius: 8, 
      background: '#eef2ff', 
      color: '#3730a3',
      whiteSpace: 'nowrap'
    },
    list: { 
      listStyle: 'none', 
      padding: 0, 
      margin: 0, 
      display: 'flex', 
      flexDirection: 'column', 
      gap: 8 
    },
    listItem: { 
      border: '1px solid #e5e7eb', 
      borderRadius: 10, 
      padding: isMobile ? 8 : 10, 
      cursor: 'pointer' 
    },
    listTitle: { 
      fontWeight: 600, 
      color: '#0f172a',
      fontSize: isMobile ? 14 : 16
    },
    listMeta: { 
      fontSize: isMobile ? 11 : 12, 
      color: '#6b7280' 
    },
    link: { 
      color: '#2d6cdf', 
      textDecoration: 'none', 
      fontWeight: 600 
    },
    pre: { 
      background: '#0f172a', 
      color: '#e2e8f0', 
      padding: isMobile ? 8 : 12, 
      borderRadius: 8, 
      maxHeight: isMobile ? 180 : 260, 
      overflow: 'auto', 
      fontSize: isMobile ? 11 : 12,
      maxWidth: '100%'
    },
    code: { 
      fontFamily: 'monospace', 
      fontSize: isMobile ? 11 : 12, 
      color: '#0f172a', 
      background: '#f1f5f9', 
      padding: '4px 6px', 
      borderRadius: 6,
      wordBreak: 'break-all'
    },
    ocrBox: { 
      display: 'flex', 
      flexDirection: 'column', 
      gap: 6 
    },
    select: { 
      padding: isMobile ? '6px 8px' : '8px 10px', 
      borderRadius: 8, 
      border: '1px solid #cbd5e1', 
      background: '#fff',
      fontSize: isMobile ? 13 : 14
    },
    filterRow: { 
      display: 'flex', 
      gap: isMobile ? 8 : 12, 
      flexWrap: 'wrap', 
      margin: '8px 0' 
    },
    legendRow: { 
      display: 'flex', 
      gap: isMobile ? 6 : 8, 
      flexWrap: 'wrap', 
      marginTop: 8 
    },
    textarea: { 
      width: '100%', 
      padding: isMobile ? 8 : 10, 
      borderRadius: 10, 
      border: '1px solid #cbd5e1', 
      fontFamily: 'inherit', 
      fontSize: isMobile ? 13 : 14, 
      minHeight: isMobile ? 80 : 100,
      maxWidth: '100%'
    },
    secondaryButton: { 
      marginTop: 8, 
      background: '#f8fafc', 
      color: '#0f172a', 
      border: '1px solid #cbd5e1', 
      padding: isMobile ? '6px 10px' : '8px 12px', 
      borderRadius: 8, 
      cursor: 'pointer',
      fontSize: isMobile ? 12 : 14
    },
  }
}

/**
 * Static styles export computed at module load time.
 * 
 * WARNING: This export uses the screen size at the time the module is loaded
 * and will NOT update on window resize. This is intentional for performance
 * and works well for components that don't need to respond to size changes.
 * 
 * For components that need fresh styles on each render (e.g., top-level layouts),
 * call getResponsiveStyles() directly or use useMemo as shown in App.tsx.
 */
export const styles = getResponsiveStyles()

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
