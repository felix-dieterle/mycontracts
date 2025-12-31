// Auto-detect API URL: if env var set, use it; else construct from current hostname on port 8080
export const apiBase = import.meta.env.VITE_API_URL || (() => {
  const protocol = window.location.protocol
  const hostname = window.location.hostname
  const port = window.location.port
  
  // In Codespaces, hostname looks like: username-abc123-5173.app.github.dev
  // Replace the frontend port with 8080 for backend
  if (hostname.includes('app.github.dev')) {
    // Extract frontend port from hostname and replace with 8080
    const backendHost = hostname.replace(new RegExp(`-${port}\\.`), '-8080.')
    const backendUrl = `${protocol}//${backendHost}`
    console.log(`[API Config] Codespaces detected - Frontend: ${window.location.href}, Backend: ${backendUrl}`)
    return backendUrl
  }
  
  // For localhost dev (localhost:5173 → localhost:8080)
  const backendUrl = `${protocol}//${hostname}:8080`
  console.log(`[API Config] Local dev detected - Frontend: ${window.location.href}, Backend: ${backendUrl}`)
  return backendUrl
})()
