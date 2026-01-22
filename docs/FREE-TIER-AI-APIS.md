# Free Tier AI APIs for MyContracts

This guide documents how to run MyContracts with free tier AI APIs. The application is designed to work with various AI providers through OpenRouter, but also supports direct API integration.

## Overview

MyContracts uses AI for:
- 💬 **Chat Interface** - Ask questions about your contracts
- 🔍 **Contract Optimization** - Automated analysis with suggestions, risk detection, and improvements
- 📊 **OCR Data Analysis** - Extract structured contract fields from OCR data
- 🌐 **Web Search** - Research providers, products, and market comparisons

## Quick Start: Free Options

### Option 1: OpenRouter (Recommended)

**OpenRouter** aggregates multiple AI providers and offers flexible pricing including free tier options.

#### Free Credits
- ✅ **$1 free credit** on signup (no credit card required)
- ✅ Access to multiple models including GPT-3.5, GPT-4, Claude, and more
- ✅ Pay-as-you-go pricing after credits expire
- ✅ Some models as low as $0.0001 per request

#### Setup

1. **Create Account**
   - Visit [openrouter.ai](https://openrouter.ai)
   - Sign up with email (no credit card required)
   - Verify your email

2. **Generate API Key**
   - Go to [Keys](https://openrouter.ai/keys)
   - Click "Create Key"
   - Copy your API key

3. **Configure MyContracts**
   ```bash
   # In your .env file
   OPENROUTER_API_KEY=sk-or-v1-... # Your API key
   OPENROUTER_BASE_URL=https://openrouter.ai/api/v1
   OPENROUTER_MODEL=openai/gpt-3.5-turbo
   ```

4. **Start the Application**
   ```bash
   docker-compose up
   ```

#### Recommended Free/Low-Cost Models

| Model | Cost (per 1M tokens) | Best For |
|-------|---------------------|----------|
| `meta-llama/llama-3.2-3b-instruct:free` | **FREE** | Chat, basic analysis |
| `google/gemini-flash-1.5` | $0.075 / $0.30 | General purpose |
| `openai/gpt-3.5-turbo` | $0.50 / $1.50 | Contract analysis |
| `anthropic/claude-3-haiku` | $0.25 / $1.25 | Fast responses |
| `mistralai/mistral-7b-instruct:free` | **FREE** | Lightweight tasks |

**Note:** Input/Output costs are listed. FREE models have no cost but may have rate limits.

#### Cost Estimation

With the $1 free credit:
- **GPT-3.5 Turbo**: ~650,000 input tokens or ~200 contract analyses
- **Llama 3.2 (Free)**: Unlimited (subject to rate limits)
- **Gemini Flash**: ~13 million input tokens

Average contract analysis uses ~500 tokens input + ~1000 tokens output.

---

### Option 2: Direct Google Gemini (Free Tier)

Google offers a generous free tier for Gemini API with no credit card required.

#### Free Tier Limits
- ✅ **60 requests per minute** (RPM)
- ✅ **1,500 requests per day** (RPD)
- ✅ **1 million tokens per month** free
- ✅ No credit card required

#### Setup

1. **Get API Key**
   - Visit [Google AI Studio](https://makersuite.google.com/app/apikey)
   - Sign in with Google account
   - Click "Create API Key"
   - Copy your API key

2. **Configure for OpenRouter**
   
   You can use Gemini through OpenRouter:
   ```bash
   OPENROUTER_API_KEY=sk-or-v1-... # Your OpenRouter key
   OPENROUTER_MODEL=google/gemini-flash-1.5
   ```

   Or configure direct access (requires code modification):
   ```bash
   GEMINI_API_KEY=AIza... # Your Gemini API key
   ```

#### Direct Integration (Advanced)

To use Gemini API directly without OpenRouter, you would need to:
1. Create a new configuration class similar to `OpenRouterConfig.java`
2. Modify `AiService.java` to support Gemini's API format
3. Update the WebClient to use `https://generativelanguage.googleapis.com/v1`

Example configuration:
```java
@Configuration
public class GeminiConfig {
    @Value("${gemini.api.key:}")
    private String apiKey;
    
    @Bean
    public WebClient geminiWebClient() {
        return WebClient.builder()
            .baseUrl("https://generativelanguage.googleapis.com/v1")
            .defaultHeader("x-goog-api-key", apiKey)
            .build();
    }
}
```

---

### Option 3: OpenAI Free Trial

OpenAI provides free credits for new accounts.

#### Free Credits
- ✅ **$5 free credit** for new accounts (valid for 3 months)
- ✅ Access to GPT-3.5 Turbo, GPT-4, etc.
- ❌ **Requires credit card** for verification

#### Setup

1. **Create Account**
   - Visit [platform.openai.com](https://platform.openai.com/signup)
   - Sign up and verify phone number
   - Add payment method (charged only after free credit expires)

2. **Generate API Key**
   - Go to [API Keys](https://platform.openai.com/api-keys)
   - Create new secret key
   - Copy the key (shown only once)

3. **Configure through OpenRouter**
   ```bash
   OPENROUTER_API_KEY=sk-or-v1-... # Use OpenRouter
   OPENROUTER_MODEL=openai/gpt-3.5-turbo
   ```
   
   Or direct (requires minor code changes):
   ```bash
   OPENAI_API_KEY=sk-... # Your OpenAI key
   OPENAI_BASE_URL=https://api.openai.com/v1
   ```

#### Cost After Free Credits
- GPT-3.5 Turbo: $0.50/$1.50 per 1M tokens (input/output)
- GPT-4o Mini: $0.15/$0.60 per 1M tokens

---

### Option 4: Anthropic Claude (Trial Credits)

Claude offers high-quality responses with a free trial.

#### Free Credits
- ✅ **$5 free credit** for new accounts
- ✅ Access to Claude 3 Haiku (fast, cheap) and Claude 3.5 Sonnet
- ❌ **Requires credit card**

#### Setup

1. **Create Account**
   - Visit [console.anthropic.com](https://console.anthropic.com/)
   - Sign up and verify email
   - Add payment method

2. **Get API Key**
   - Go to [API Keys](https://console.anthropic.com/settings/keys)
   - Create key

3. **Configure via OpenRouter**
   ```bash
   OPENROUTER_API_KEY=sk-or-v1-...
   OPENROUTER_MODEL=anthropic/claude-3-haiku
   ```

#### Cost After Free Credits
- Claude 3 Haiku: $0.25/$1.25 per 1M tokens (best value)
- Claude 3.5 Sonnet: $3/$15 per 1M tokens (highest quality)

---

## Option 5: Perplexity AI (Free Tier)

Perplexity offers AI with internet access, great for web search features.

#### Free Tier
- ✅ **Limited free requests** (exact limits vary)
- ✅ Internet-connected models for web search
- ❌ May require account

#### Setup via OpenRouter
```bash
OPENROUTER_API_KEY=sk-or-v1-...
OPENROUTER_MODEL=perplexity/llama-3.1-sonar-small-128k-online
```

---

## Completely Free Options (No Credit Card)

### 1. **Meta Llama Models via OpenRouter**

```bash
OPENROUTER_API_KEY=sk-or-v1-...
OPENROUTER_MODEL=meta-llama/llama-3.2-3b-instruct:free
```

- ✅ Completely free
- ✅ Good for basic chat and simple analysis
- ⚠️ May have rate limits
- ⚠️ Lower quality than GPT-3.5/Claude

### 2. **Mistral via OpenRouter**

```bash
OPENROUTER_MODEL=mistralai/mistral-7b-instruct:free
```

- ✅ Completely free
- ✅ Good multilingual support
- ⚠️ Rate limits apply

### 3. **Google Gemini Flash (via OpenRouter with free credits)**

```bash
OPENROUTER_MODEL=google/gemini-flash-1.5
```

- ✅ Very low cost ($0.075/$0.30 per 1M tokens)
- ✅ OpenRouter's $1 credit = ~13M tokens
- ✅ Fast and capable

---

## Recommended Strategy for Free Tier Usage

### Approach 1: Start with OpenRouter (Easiest)

1. **Create OpenRouter account** - Get $1 free credit
2. **Use Llama 3.2 Free** for testing
3. **Switch to Gemini Flash** for production (very cheap with free credit)
4. **Upgrade to GPT-3.5** only when needed for complex analysis

### Approach 2: Maximum Free Tier (Multiple Accounts)

1. **OpenRouter**: $1 free credit → Use for contract optimization
2. **Google Gemini**: 1M tokens/month free → Use for OCR analysis
3. **Rotate models** based on task complexity

### Approach 3: Self-Hosted (Advanced Users)

For completely free unlimited usage:
1. Run **Ollama** locally with Llama 3.2 or Mistral
2. Modify MyContracts to support local API endpoints
3. Trade API convenience for hardware requirements

---

## Configuration Examples

### Minimal Cost Setup (Recommended)

```bash
# .env file
OPENROUTER_API_KEY=sk-or-v1-your-key-here
OPENROUTER_BASE_URL=https://openrouter.ai/api/v1
OPENROUTER_MODEL=google/gemini-flash-1.5
```

**Why**: Gemini Flash is extremely cheap, fast, and the $1 OpenRouter credit lasts very long.

### Completely Free Setup

```bash
# .env file
OPENROUTER_API_KEY=sk-or-v1-your-key-here
OPENROUTER_BASE_URL=https://openrouter.ai/api/v1
OPENROUTER_MODEL=meta-llama/llama-3.2-3b-instruct:free
```

**Why**: No cost at all, suitable for testing and light usage.

### Quality-Focused Setup

```bash
# .env file
OPENROUTER_API_KEY=sk-or-v1-your-key-here
OPENROUTER_MODEL=openai/gpt-3.5-turbo
```

**Why**: Best quality for contract analysis, $1 credit ≈ 200 analyses.

---

## Features Without AI

MyContracts works fully without AI configuration! All these features are available:

✅ File upload and management  
✅ OCR file matching (watcher service)  
✅ Multi-marker system (URGENT, REVIEW, etc.)  
✅ Due dates and notes  
✅ Smart filtering  
✅ REST API  
✅ Health monitoring  

Only these features require AI:
❌ Chat interface  
❌ Contract optimization  
❌ AI-powered OCR field extraction  
❌ Web search integration  

---

## Testing Your Setup

### 1. Verify Configuration

```bash
# Check if API key is configured
curl http://localhost:8080/api/health
```

### 2. Test Chat

```bash
curl -X POST http://localhost:8080/api/ai/chat \
  -H "Content-Type: application/json" \
  -d '{
    "messages": [{"role": "user", "content": "Hello!"}],
    "fileId": 1
  }'
```

### 3. Test Contract Optimization

```bash
curl -X POST http://localhost:8080/api/ai/optimize \
  -H "Content-Type: application/json" \
  -d '{"fileId": 1}'
```

If you get a response, your AI setup is working! 🎉

---

## Cost Monitoring

### Track Usage via OpenRouter Dashboard

1. Visit [openrouter.ai/activity](https://openrouter.ai/activity)
2. View requests, tokens used, and costs
3. Set spending limits if needed

### Expected Usage per Feature

| Feature | Avg Tokens | Cost (GPT-3.5) | Cost (Gemini Flash) |
|---------|------------|----------------|---------------------|
| Chat message | 200 | $0.0003 | $0.000015 |
| Contract optimization | 1,500 | $0.002 | $0.0001 |
| OCR analysis | 1,000 | $0.0015 | $0.000075 |
| Web search | 2,000 | $0.003 | $0.00015 |

**Example**: With $1 credit using Gemini Flash, you could perform:
- ~6,600 contract optimizations, OR
- ~13,000 OCR analyses, OR
- ~66,000 chat messages

---

## Troubleshooting

### "API key not configured" Error

**Solution**: Check your `.env` file and ensure `OPENROUTER_API_KEY` is set correctly.

```bash
# Verify environment variable is loaded
docker-compose config | grep OPENROUTER_API_KEY
```

### "Rate limit exceeded" Error

**Solution**: 
1. Free tier models have rate limits
2. Switch to a paid model or wait for rate limit to reset
3. OpenRouter free models: ~10-20 requests/minute

### "Insufficient credits" Error

**Solution**:
1. Check your balance at [openrouter.ai/credits](https://openrouter.ai/credits)
2. Add more credits or switch to a free model
3. Use `meta-llama/llama-3.2-3b-instruct:free` for unlimited usage

### AI Features Not Working

**Checklist**:
- [ ] Is `OPENROUTER_API_KEY` set in `.env`?
- [ ] Did you restart the backend after changing `.env`?
- [ ] Is the API key valid? (Check OpenRouter dashboard)
- [ ] Is the model name correct? (Check [OpenRouter models](https://openrouter.ai/models))

---

## Migration Guide

### From No AI → OpenRouter

1. Create OpenRouter account
2. Add `OPENROUTER_API_KEY` to `.env`
3. Restart: `docker-compose restart backend`
4. Test chat feature in UI

### From Other Provider → OpenRouter

OpenRouter supports most providers with the same model names:
- OpenAI: `openai/gpt-3.5-turbo`, `openai/gpt-4`
- Anthropic: `anthropic/claude-3-haiku`, `anthropic/claude-3-5-sonnet`
- Google: `google/gemini-flash-1.5`, `google/gemini-pro-1.5`

Simply update your `.env`:
```bash
# Old
OPENAI_API_KEY=sk-...

# New
OPENROUTER_API_KEY=sk-or-v1-...
OPENROUTER_MODEL=openai/gpt-3.5-turbo
```

---

## FAQ

### Q: Can I use MyContracts completely free forever?

**A:** Yes! Use `meta-llama/llama-3.2-3b-instruct:free` via OpenRouter (no cost, rate-limited) or run without AI (all core features work).

### Q: What's the cheapest option for production use?

**A:** Google Gemini Flash via OpenRouter ($0.075/$0.30 per 1M tokens) offers the best value. With average usage, $1 could last months.

### Q: Do I need a credit card?

**A:** No for OpenRouter ($1 free, no card). Google Gemini is also free without a card. OpenAI and Anthropic require a card for verification.

### Q: Can I switch models anytime?

**A:** Yes! Just update `OPENROUTER_MODEL` in `.env` and restart the backend. No code changes needed.

### Q: What if I run out of free credits?

**A:** Switch to a free model (`meta-llama/llama-3.2-3b-instruct:free`) or add credits. Most paid models are very cheap (pennies per analysis).

---

## Summary

**Best Free Option**: OpenRouter with Llama 3.2 Free  
**Best Value Option**: OpenRouter with Gemini Flash  
**Best Quality Option**: OpenRouter with GPT-3.5 Turbo  
**Most Generous Free Tier**: Google Gemini (1M tokens/month)  

Choose based on your needs:
- **Just testing?** → Llama 3.2 Free
- **Light usage?** → Gemini Flash (extremely cheap)
- **Professional use?** → GPT-3.5 Turbo (still cheap)
- **No internet?** → Self-host with Ollama (advanced)

**All options work with the current MyContracts implementation** - just change the environment variables! 🚀
