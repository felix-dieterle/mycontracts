import { describe, it, expect, beforeEach, afterEach } from 'vitest'
import { getResponsiveStyles } from './styles'

describe('getResponsiveStyles', () => {
  let originalInnerWidth: number

  beforeEach(() => {
    originalInnerWidth = window.innerWidth
  })

  afterEach(() => {
    Object.defineProperty(window, 'innerWidth', {
      writable: true,
      configurable: true,
      value: originalInnerWidth,
    })
  })

  it('should return mobile styles when window width is less than 768px', () => {
    Object.defineProperty(window, 'innerWidth', {
      writable: true,
      configurable: true,
      value: 375, // Mobile width
    })

    const styles = getResponsiveStyles()

    // Check page padding is reduced for mobile
    expect(styles.page.padding).toBe('12px')
    
    // Check grid uses single column for mobile
    expect(styles.grid.gridTemplateColumns).toBe('1fr')
    
    // Check title font size is smaller for mobile
    expect(styles.title.fontSize).toBe(20)
    
    // Check header is column direction for mobile
    expect(styles.header.flexDirection).toBe('column')
  })

  it('should return desktop styles when window width is 768px or more', () => {
    Object.defineProperty(window, 'innerWidth', {
      writable: true,
      configurable: true,
      value: 1024, // Desktop width
    })

    const styles = getResponsiveStyles()

    // Check page padding is normal for desktop
    expect(styles.page.padding).toBe('24px')
    
    // Check grid uses two columns for desktop
    expect(styles.grid.gridTemplateColumns).toBe('320px 1fr')
    
    // Check title font size is normal for desktop
    expect(styles.title.fontSize).toBe(24)
    
    // Check header is row direction for desktop
    expect(styles.header.flexDirection).toBe('row')
  })

  it('should include overflow prevention in page styles', () => {
    const styles = getResponsiveStyles()

    expect(styles.page.overflowX).toBe('hidden')
    expect(styles.page.width).toBe('100%')
    expect(styles.page.maxWidth).toBe('100vw')
  })

  it('should make text elements smaller on mobile', () => {
    Object.defineProperty(window, 'innerWidth', {
      writable: true,
      configurable: true,
      value: 375,
    })

    const styles = getResponsiveStyles()

    expect(styles.h2.fontSize).toBe(16) // Smaller than desktop
    expect(styles.badge.fontSize).toBe(11)
    expect(styles.listTitle.fontSize).toBe(14)
  })

  it('should reduce padding on mobile elements', () => {
    Object.defineProperty(window, 'innerWidth', {
      writable: true,
      configurable: true,
      value: 375,
    })

    const styles = getResponsiveStyles()

    expect(styles.primaryButton.padding).toBe('8px 12px')
    expect(styles.card.padding).toBe(12)
  })
})
