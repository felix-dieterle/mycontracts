export type FileSummary = {
  id: number
  filename: string
  mime?: string
  size?: number
  checksum?: string
  createdAt?: string
  markers?: string[]
  dueDate?: string | null
  ocrStatus?: string | null
}

export type OcrInfo = {
  id: number
  status: string
  createdAt?: string
  processedAt?: string
  retryCount?: number
  rawJson?: string
}

export type FileDetail = FileSummary & {
  ocr?: OcrInfo | null
  note?: string | null
}

export type MarkerFilter = 'ALL' | 'NEEDS_ATTENTION' | string

export const MARKER_OPTIONS = ['URGENT', 'REVIEW', 'MISSING_INFO', 'INCOMPLETE_OCR', 'FOLLOW_UP']

// Markers that indicate a file needs attention
export const NEEDS_ATTENTION_MARKERS = ['URGENT', 'REVIEW', 'MISSING_INFO']
