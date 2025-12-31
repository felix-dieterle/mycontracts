// Auto-detect API URL: if env var set, use it; else construct from current hostname on port 8080
export const apiBase = import.meta.env.VITE_API_URL || (() => {
  const protocol = window.location.protocol
  const hostname = window.location.hostname
  
  // In Codespaces, hostname looks like: username-abc123-5173.app.github.dev
  // Replace 5173 with 8080 for backend
  if (hostname.includes('app.github.dev')) {
    const backendHost = hostname.replace('-5173.', '-8080.')
    return `${protocol}//${backendHost}`
  }
  
  // For localhost dev (localhost:5173 → localhost:8080)
  return `${protocol}//${hostname}:8080`
})()
