#!/bin/bash
# Start both backend and frontend services

set -e

echo "🚀 Starting mycontracts application..."
echo ""

# Kill any existing processes
echo "1️⃣ Cleaning up old processes..."
pkill -9 -f "java.*spring-boot" 2>/dev/null || true
pkill -9 -f "npm run dev" 2>/dev/null || true
pkill -9 -f "vite" 2>/dev/null || true
sleep 2
echo "   ✅ Cleanup complete"
echo ""

# Start backend
echo "2️⃣ Starting backend on port 8080..."
cd /workspaces/mycontracts/backend
nohup mvn spring-boot:run -DskipTests > /tmp/backend.log 2>&1 &
BACKEND_PID=$!
echo "   Backend PID: $BACKEND_PID"
echo "   Log: /tmp/backend.log"

# Wait for backend
echo "   Waiting for backend to start..."
for i in {1..30}; do
  if curl -s http://localhost:8080/api/health > /dev/null 2>&1; then
    echo "   ✅ Backend ready at http://localhost:8080"
    break
  fi
  sleep 1
  if [ $i -eq 30 ]; then
    echo "   ⚠️  Backend startup timeout - check /tmp/backend.log"
  fi
done
echo ""

# Start frontend
echo "3️⃣ Starting frontend on port 5173..."
cd /workspaces/mycontracts/frontend
nohup npm run dev -- --host --port 5173 > /tmp/frontend.log 2>&1 &
FRONTEND_PID=$!
echo "   Frontend PID: $FRONTEND_PID"
echo "   Log: /tmp/frontend.log"

# Wait for frontend
echo "   Waiting for frontend to start..."
for i in {1..30}; do
  if curl -s http://localhost:5173 > /dev/null 2>&1; then
    echo "   ✅ Frontend ready at http://localhost:5173"
    break
  fi
  sleep 1
  if [ $i -eq 30 ]; then
    echo "   ⚠️  Frontend startup timeout - check /tmp/frontend.log"
  fi
done
echo ""

echo "╔════════════════════════════════════════════════════════════════════════════╗"
echo "║                                                                            ║"
echo "║                    ✅ APPLICATION STARTED                                 ║"
echo "║                                                                            ║"
echo "╚════════════════════════════════════════════════════════════════════════════╝"
echo ""
echo "📍 URLs:"
echo "   Frontend:     http://localhost:5173"
echo "   Backend API:  http://localhost:8080"
echo "   Health Check: http://localhost:8080/api/health"
echo ""
echo "📊 In Codespaces, use the forwarded HTTPS URLs from the Ports tab"
echo ""
echo "📝 Logs:"
echo "   Backend:  tail -f /tmp/backend.log"
echo "   Frontend: tail -f /tmp/frontend.log"
echo ""
echo "🛑 To stop:"
echo "   pkill -f 'java.*spring-boot'"
echo "   pkill -f 'npm run dev'"
echo ""
echo "✅ Both services are running in background!"
