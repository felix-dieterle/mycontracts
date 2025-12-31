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

export type NeedsAttention = 'ALL' | 'NEEDS_ATTENTION'

export const MARKER_OPTIONS = ['URGENT', 'REVIEW', 'MISSING_INFO', 'INCOMPLETE_OCR', 'FOLLOW_UP']
