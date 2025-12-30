import React, {useEffect, useState} from 'react'

const apiBase = import.meta.env.VITE_API_URL || ''

export default function App(){
  const [status, setStatus] = useState<string>('loading')

  useEffect(()=>{
    fetch(apiBase + '/api/health')
      .then(r=>r.json())
      .then(j=>setStatus(j.status))
      .catch(_=>setStatus('offline'))
  },[])

  return (
    <div style={{fontFamily:'sans-serif',padding:20}}>
      <h1>mycontracts</h1>
      <p>Backend status: <strong>{status}</strong></p>
      <p>Upload, Watcher und Extraction folgen im nächsten Schritt.</p>
    </div>
  )
}
