import React, { useState, useRef, useEffect } from 'react'
import { ChatMessage, ChatRequest, ChatResponse, ContractOptimizationResponse } from '../types'
import { apiBase } from '../utils/apiConfig'
import { styles } from '../styles/styles'

type ChatProps = {
  fileId?: number | null
  filename?: string
}

export function Chat({ fileId, filename }: ChatProps) {
  const [messages, setMessages] = useState<ChatMessage[]>([])
  const [input, setInput] = useState('')
  const [loading, setLoading] = useState(false)
  const [optimizing, setOptimizing] = useState(false)
  const [optimization, setOptimization] = useState<ContractOptimizationResponse | null>(null)
  const messagesEndRef = useRef<HTMLDivElement>(null)

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' })
  }

  useEffect(() => {
    scrollToBottom()
  }, [messages])

  const sendMessage = async () => {
    if (!input.trim()) return

    const userMessage: ChatMessage = { role: 'user', content: input }
    setMessages(prev => [...prev, userMessage])
    setInput('')
    setLoading(true)

    try {
      const request: ChatRequest = {
        messages: [...messages, userMessage].map(m => ({ role: m.role, content: m.content })),
        fileId: fileId || null
      }

      const res = await fetch(`${apiBase}/api/ai/chat`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(request)
      })

      const data: ChatResponse = await res.json()
      
      const assistantMessage: ChatMessage = {
        role: data.error ? 'error' : 'assistant',
        content: data.message
      }
      
      setMessages(prev => [...prev, assistantMessage])
    } catch (error) {
      const errorMessage: ChatMessage = {
        role: 'error',
        content: 'Failed to send message: ' + (error as Error).message
      }
      setMessages(prev => [...prev, errorMessage])
    } finally {
      setLoading(false)
    }
  }

  const optimizeContract = async () => {
    if (!fileId) return

    setOptimizing(true)
    try {
      const res = await fetch(`${apiBase}/api/ai/optimize`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ fileId })
      })

      const data: ContractOptimizationResponse = await res.json()
      setOptimization(data)
      
      // Also add to chat
      const optimizationMessage: ChatMessage = {
        role: 'assistant',
        content: `📊 **Contract Optimization Analysis**\n\n${data.summary}`
      }
      setMessages(prev => [...prev, optimizationMessage])
    } catch (error) {
      const errorMessage: ChatMessage = {
        role: 'error',
        content: 'Failed to optimize contract: ' + (error as Error).message
      }
      setMessages(prev => [...prev, errorMessage])
    } finally {
      setOptimizing(false)
    }
  }

  const handleKeyPress = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault()
      sendMessage()
    }
  }

  return (
    <div style={{ ...styles.panel, display: 'flex', flexDirection: 'column', height: '100%' }}>
      <div style={{ padding: '12px', borderBottom: '1px solid #ddd', backgroundColor: '#f8f9fa' }}>
        <h3 style={{ margin: '0 0 8px 0', fontSize: '16px', fontWeight: 600 }}>
          💬 AI Assistant
        </h3>
        {filename && (
          <div style={{ fontSize: '13px', color: '#666' }}>
            Context: {filename}
          </div>
        )}
        {fileId && (
          <button
            onClick={optimizeContract}
            disabled={optimizing}
            style={{
              ...styles.button,
              marginTop: '8px',
              fontSize: '12px',
              padding: '6px 12px'
            }}
          >
            {optimizing ? '🔄 Analyzing...' : '🔍 Optimize Contract'}
          </button>
        )}
      </div>

      <div style={{
        flex: 1,
        overflowY: 'auto',
        padding: '16px',
        display: 'flex',
        flexDirection: 'column',
        gap: '12px'
      }}>
        {messages.length === 0 && (
          <div style={{ color: '#666', fontSize: '14px', textAlign: 'center', marginTop: '20px' }}>
            👋 Ask me anything about {filename || 'your contracts'}!
            <br />
            <br />
            Try asking:
            <ul style={{ textAlign: 'left', display: 'inline-block', marginTop: '8px' }}>
              <li>What are the key terms?</li>
              <li>What risks should I be aware of?</li>
              <li>How can I optimize this contract?</li>
            </ul>
          </div>
        )}
        
        {messages.map((msg, idx) => (
          <div
            key={idx}
            style={{
              padding: '10px 14px',
              borderRadius: '8px',
              backgroundColor: msg.role === 'user' 
                ? '#007bff' 
                : msg.role === 'error'
                ? '#f8d7da'
                : '#e9ecef',
              color: msg.role === 'user' ? 'white' : msg.role === 'error' ? '#721c24' : '#333',
              alignSelf: msg.role === 'user' ? 'flex-end' : 'flex-start',
              maxWidth: '80%',
              fontSize: '14px',
              whiteSpace: 'pre-wrap',
              wordBreak: 'break-word'
            }}
          >
            {msg.content}
          </div>
        ))}
        
        {loading && (
          <div style={{
            padding: '10px 14px',
            borderRadius: '8px',
            backgroundColor: '#e9ecef',
            color: '#666',
            alignSelf: 'flex-start',
            maxWidth: '80%',
            fontSize: '14px'
          }}>
            💭 Thinking...
          </div>
        )}
        
        <div ref={messagesEndRef} />
      </div>

      {optimization && (
        <div style={{
          padding: '12px',
          borderTop: '1px solid #ddd',
          borderBottom: '1px solid #ddd',
          backgroundColor: '#f8f9fa',
          fontSize: '13px',
          maxHeight: '200px',
          overflowY: 'auto'
        }}>
          <strong>📊 Optimization Summary:</strong>
          <div style={{ marginTop: '8px' }}>
            {optimization.suggestions.length > 0 && (
              <div style={{ marginBottom: '8px' }}>
                <strong>💡 Suggestions:</strong>
                <ul style={{ margin: '4px 0', paddingLeft: '20px' }}>
                  {optimization.suggestions.map((s, i) => <li key={i}>{s}</li>)}
                </ul>
              </div>
            )}
            {optimization.risks.length > 0 && (
              <div style={{ marginBottom: '8px' }}>
                <strong>⚠️ Risks:</strong>
                <ul style={{ margin: '4px 0', paddingLeft: '20px' }}>
                  {optimization.risks.map((r, i) => <li key={i}>{r}</li>)}
                </ul>
              </div>
            )}
            {optimization.improvements.length > 0 && (
              <div>
                <strong>🔧 Improvements:</strong>
                <ul style={{ margin: '4px 0', paddingLeft: '20px' }}>
                  {optimization.improvements.map((imp, i) => <li key={i}>{imp}</li>)}
                </ul>
              </div>
            )}
          </div>
        </div>
      )}

      <div style={{ padding: '12px', borderTop: '1px solid #ddd', backgroundColor: '#fff' }}>
        <div style={{ display: 'flex', gap: '8px' }}>
          <input
            type="text"
            value={input}
            onChange={e => setInput(e.target.value)}
            onKeyPress={handleKeyPress}
            placeholder="Type your message..."
            disabled={loading}
            style={{
              flex: 1,
              padding: '8px 12px',
              border: '1px solid #ccc',
              borderRadius: '4px',
              fontSize: '14px'
            }}
          />
          <button
            onClick={sendMessage}
            disabled={loading || !input.trim()}
            style={{
              ...styles.button,
              padding: '8px 16px',
              fontSize: '14px',
              opacity: (loading || !input.trim()) ? 0.5 : 1
            }}
          >
            Send
          </button>
        </div>
      </div>
    </div>
  )
}
