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

export type ContractInfo = {
  id: number
  title: string
}

export type Contract = {
  id: number
  title: string
  createdAt?: string
}

export type FileDetail = FileSummary & {
  ocr?: OcrInfo | null
  note?: string | null
  contract?: ContractInfo | null
}

export type MarkerFilter = 'ALL' | 'NEEDS_ATTENTION' | string

export const MARKER_OPTIONS = ['URGENT', 'REVIEW', 'MISSING_INFO', 'INCOMPLETE_OCR', 'FOLLOW_UP']

// Markers that indicate a file needs attention
export const NEEDS_ATTENTION_MARKERS = ['URGENT', 'REVIEW', 'MISSING_INFO']

// Rate limit information
export type RateLimitInfo = {
  limit: number | null
  remaining: number | null
  resetAt: number | null
  apiName: string
}

// AI Chat types
export type ChatMessage = {
  role: 'user' | 'assistant' | 'error'
  content: string
}

export type ChatRequest = {
  messages: Array<{ role: 'user' | 'assistant'; content: string }>
  fileId?: number | null
}

export type ChatResponse = {
  message: string
  role: string
  error: boolean
  rateLimit?: RateLimitInfo | null
}

export type ContractOptimizationResponse = {
  suggestions: string[]
  risks: string[]
  improvements: string[]
  summary: string
}
