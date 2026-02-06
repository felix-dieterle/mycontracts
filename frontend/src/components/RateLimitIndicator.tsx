import React from 'react'
import { RateLimitInfo } from '../types'

type RateLimitIndicatorProps = {
  rateLimit: RateLimitInfo | null | undefined
}

/**
 * Small colored badge showing API rate limit status
 * - Green: 0-69% usage
 * - Yellow: 70-89% usage
 * - Red: 90-100% usage
 * - Gray: No data available
 */
export function RateLimitIndicator({ rateLimit }: RateLimitIndicatorProps) {
  if (!rateLimit || rateLimit.limit === null || rateLimit.remaining === null) {
    return null
  }

  const used = rateLimit.limit - rateLimit.remaining
  const usagePercentage = Math.round((used / rateLimit.limit) * 100)
  
  let color: string
  let textColor: string
  
  if (usagePercentage < 70) {
    color = '#c5f6c5' // green
    textColor = '#2d5a2d'
  } else if (usagePercentage < 90) {
    color = '#fff3cd' // yellow
    textColor = '#856404'
  } else {
    color = '#ffe3e3' // red
    textColor = '#721c24'
  }

  const indicatorStyle: React.CSSProperties = {
    display: 'inline-flex',
    alignItems: 'center',
    gap: '4px',
    fontSize: '11px',
    fontWeight: 500,
    padding: '2px 6px',
    borderRadius: '3px',
    backgroundColor: color,
    color: textColor,
    whiteSpace: 'nowrap'
  }

  const dotStyle: React.CSSProperties = {
    width: '6px',
    height: '6px',
    borderRadius: '50%',
    backgroundColor: textColor
  }

  return (
    <span style={indicatorStyle} title={`${rateLimit.apiName}: ${rateLimit.remaining}/${rateLimit.limit} remaining`}>
      <span style={dotStyle}></span>
      {rateLimit.remaining}/{rateLimit.limit}
    </span>
  )
}
