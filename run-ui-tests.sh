#!/bin/bash
# Run UI Tests - Complete automation script

set -e

echo "🚀 Starting UI Test Automation..."
echo ""

# Install Playwright browsers (required)
echo "0️⃣ Installing Playwright browsers..."
cd /workspaces/mycontracts/frontend
echo "   Running: npx playwright install --with-deps"
npx playwright install --with-deps
if [ $? -ne 0 ]; then
  echo "   ❌ Browser installation failed!"
  exit 1
fi
echo "   ✅ Browsers installed successfully"
echo ""

# Kill any stuck processes
echo "1️⃣ Cleaning up old processes..."
pkill -9 -f "java.*spring-boot" 2>/dev/null || true
pkill -9 -f "npm run dev" 2>/dev/null || true
pkill -9 -f "vite" 2>/dev/null || true
pkill -9 -f playwright 2>/dev/null || true
sleep 2

# Start backend
echo "2️⃣ Starting backend on port 8080..."
cd /workspaces/mycontracts/backend
nohup mvn spring-boot:run -DskipTests > /tmp/backend.log 2>&1 &
BACKEND_PID=$!
echo "   Backend PID: $BACKEND_PID"

# Wait for backend
echo "   Waiting for backend to start..."
for i in {1..30}; do
  if curl -s http://localhost:8080/api/health > /dev/null 2>&1; then
    echo "   ✅ Backend ready!"
    break
  fi
  sleep 1
done

# Start frontend
echo "3️⃣ Starting frontend on port 5173..."
cd /workspaces/mycontracts/frontend
nohup npm run dev -- --host --port 5173 > /tmp/frontend.log 2>&1 &
FRONTEND_PID=$!
echo "   Frontend PID: $FRONTEND_PID"

# Wait for frontend
echo "   Waiting for frontend to start..."
for i in {1..30}; do
  if curl -s http://localhost:5173 > /dev/null 2>&1; then
    echo "   ✅ Frontend ready!"
    break
  fi
  sleep 1
done

# Run Playwright tests (without interactive report server)
echo "4️⃣ Running Playwright tests..."
echo "   This may take 2-3 minutes..."
cd /workspaces/mycontracts/frontend
npx playwright test --reporter=list 2>&1 | tee /tmp/test.log

# Check if tests passed
if [ ${PIPESTATUS[0]} -eq 0 ]; then
  echo ""
  echo "✅ Tests passed!"
  echo ""
  echo "📸 Screenshots generated:"
  ls -lh /workspaces/mycontracts/frontend/screenshots/ 2>/dev/null || echo "   No screenshots found"
  echo ""
  echo "📊 Test report available at:"
  echo "   /workspaces/mycontracts/frontend/test-results/index.html"
  echo ""
  echo "   To view: npx playwright show-report"
else
  echo ""
  echo "❌ Tests failed or incomplete. Check logs:"
  echo "   /tmp/test.log"
fi

echo ""
echo "✅ Done!"
