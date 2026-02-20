import React, { useEffect, useState } from 'react'
import { EmailAccount, BankAccount, BankTransaction } from '../types'
import { apiBase } from '../utils/apiConfig'
import { styles } from '../styles/styles'

type Tab = 'email' | 'bank'

export function Accounts() {
  const [activeTab, setActiveTab] = useState<Tab>('email')

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
      <div style={{ display: 'flex', gap: 8 }}>
        <button
          onClick={() => setActiveTab('email')}
          style={{
            ...styles.primaryButton,
            opacity: activeTab === 'email' ? 1 : 0.5,
          }}
        >
          ✉️ E-Mail Konten
        </button>
        <button
          onClick={() => setActiveTab('bank')}
          style={{
            ...styles.primaryButton,
            opacity: activeTab === 'bank' ? 1 : 0.5,
          }}
        >
          🏦 Bank Konten
        </button>
      </div>

      {activeTab === 'email' && <EmailAccounts />}
      {activeTab === 'bank' && <BankAccounts />}
    </div>
  )
}

function EmailAccounts() {
  const [accounts, setAccounts] = useState<EmailAccount[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [showForm, setShowForm] = useState(false)
  const [form, setForm] = useState({ name: '', host: '', port: '993', protocol: 'IMAP', username: '', password: '' })
  const [saving, setSaving] = useState(false)

  useEffect(() => {
    loadAccounts()
  }, [])

  async function loadAccounts() {
    setLoading(true)
    setError(null)
    try {
      const res = await fetch(apiBase + '/api/email-accounts')
      if (!res.ok) throw new Error('Fehler beim Laden der E-Mail Konten')
      setAccounts(await res.json())
    } catch (e) {
      setError((e as Error).message)
    } finally {
      setLoading(false)
    }
  }

  async function handleCreate(ev: React.FormEvent) {
    ev.preventDefault()
    setSaving(true)
    setError(null)
    try {
      const res = await fetch(apiBase + '/api/email-accounts', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ ...form, port: Number(form.port) }),
      })
      if (!res.ok) throw new Error('Fehler beim Speichern')
      setForm({ name: '', host: '', port: '993', protocol: 'IMAP', username: '', password: '' })
      setShowForm(false)
      await loadAccounts()
    } catch (e) {
      setError((e as Error).message)
    } finally {
      setSaving(false)
    }
  }

  async function handleDelete(id: number) {
    if (!window.confirm('E-Mail Konto wirklich löschen?')) return
    setError(null)
    try {
      const res = await fetch(apiBase + `/api/email-accounts/${id}`, { method: 'DELETE' })
      if (!res.ok) throw new Error('Fehler beim Löschen')
      await loadAccounts()
    } catch (e) {
      setError((e as Error).message)
    }
  }

  return (
    <div style={styles.panel}>
      <div style={{ padding: 16, display: 'flex', flexDirection: 'column', gap: 12 }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <h2 style={styles.h2}>✉️ E-Mail Konten</h2>
          <button style={styles.primaryButton} onClick={() => setShowForm(!showForm)}>
            {showForm ? 'Abbrechen' : '+ Konto hinzufügen'}
          </button>
        </div>

        {error && <div style={styles.error}>{error}</div>}

        {showForm && (
          <form onSubmit={handleCreate} style={{ display: 'flex', flexDirection: 'column', gap: 8, background: '#f8fafc', padding: 12, borderRadius: 10, border: '1px solid #e5e7eb' }}>
            <label style={styles.label}>
              Name *
              <input
                style={{ ...styles.textarea, minHeight: 'unset', padding: '8px 10px' }}
                value={form.name}
                onChange={e => setForm(f => ({ ...f, name: e.target.value }))}
                placeholder="z.B. Geschäftliches E-Mail"
                required
              />
            </label>
            <label style={styles.label}>
              Host *
              <input
                style={{ ...styles.textarea, minHeight: 'unset', padding: '8px 10px' }}
                value={form.host}
                onChange={e => setForm(f => ({ ...f, host: e.target.value }))}
                placeholder="z.B. imap.gmail.com"
                required
              />
            </label>
            <div style={{ display: 'flex', gap: 8 }}>
              <label style={{ ...styles.label, flex: 1 }}>
                Port
                <input
                  type="number"
                  style={{ ...styles.textarea, minHeight: 'unset', padding: '8px 10px' }}
                  value={form.port}
                  onChange={e => setForm(f => ({ ...f, port: e.target.value }))}
                />
              </label>
              <label style={{ ...styles.label, flex: 1 }}>
                Protokoll
                <select
                  style={styles.select}
                  value={form.protocol}
                  onChange={e => setForm(f => ({ ...f, protocol: e.target.value }))}
                >
                  <option value="IMAP">IMAP</option>
                  <option value="POP3">POP3</option>
                </select>
              </label>
            </div>
            <label style={styles.label}>
              Benutzername *
              <input
                style={{ ...styles.textarea, minHeight: 'unset', padding: '8px 10px' }}
                value={form.username}
                onChange={e => setForm(f => ({ ...f, username: e.target.value }))}
                placeholder="E-Mail Adresse"
                required
              />
            </label>
            <label style={styles.label}>
              Passwort
              <input
                type="password"
                style={{ ...styles.textarea, minHeight: 'unset', padding: '8px 10px' }}
                value={form.password}
                onChange={e => setForm(f => ({ ...f, password: e.target.value }))}
                placeholder="Passwort"
              />
            </label>
            <button type="submit" disabled={saving} style={styles.primaryButton}>
              {saving ? 'Speichern...' : 'Speichern'}
            </button>
          </form>
        )}

        {loading && <div style={styles.muted}>Lade...</div>}
        {!loading && accounts.length === 0 && (
          <div style={styles.muted}>Noch keine E-Mail Konten hinterlegt.</div>
        )}
        <ul style={styles.list}>
          {accounts.map(acc => (
            <li key={acc.id} style={styles.listItem}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                <div>
                  <div style={styles.listTitle}>✉️ {acc.name}</div>
                  <div style={styles.listMeta}>{acc.protocol} · {acc.host}:{acc.port} · {acc.username}</div>
                  {acc.lastSync && <div style={styles.listMeta}>Letzter Sync: {new Date(acc.lastSync).toLocaleString('de-DE')}</div>}
                </div>
                <button
                  onClick={() => handleDelete(acc.id)}
                  style={{ ...styles.secondaryButton, marginTop: 0, color: '#b00020' }}
                >
                  🗑️
                </button>
              </div>
            </li>
          ))}
        </ul>
      </div>
    </div>
  )
}

function BankAccounts() {
  const [accounts, setAccounts] = useState<BankAccount[]>([])
  const [selectedAccount, setSelectedAccount] = useState<BankAccount | null>(null)
  const [transactions, setTransactions] = useState<BankTransaction[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [showForm, setShowForm] = useState(false)
  const [form, setForm] = useState({ name: '', iban: '', bankName: '', apiProvider: '', apiKey: '' })
  const [saving, setSaving] = useState(false)
  const [showTxForm, setShowTxForm] = useState(false)
  const [txForm, setTxForm] = useState({ date: '', amount: '', counterparty: '', description: '', category: '', reference: '' })
  const [savingTx, setSavingTx] = useState(false)

  useEffect(() => {
    loadAccounts()
  }, [])

  useEffect(() => {
    if (selectedAccount) loadTransactions(selectedAccount.id)
  }, [selectedAccount])

  async function loadAccounts() {
    setLoading(true)
    setError(null)
    try {
      const res = await fetch(apiBase + '/api/bank-accounts')
      if (!res.ok) throw new Error('Fehler beim Laden der Bank Konten')
      const data: BankAccount[] = await res.json()
      setAccounts(data)
      if (data.length && !selectedAccount) setSelectedAccount(data[0])
    } catch (e) {
      setError((e as Error).message)
    } finally {
      setLoading(false)
    }
  }

  async function loadTransactions(accountId: number) {
    try {
      const res = await fetch(apiBase + `/api/bank-accounts/${accountId}/transactions`)
      if (!res.ok) throw new Error('Fehler beim Laden der Transaktionen')
      setTransactions(await res.json())
    } catch (e) {
      setError((e as Error).message)
    }
  }

  async function handleCreate(ev: React.FormEvent) {
    ev.preventDefault()
    setSaving(true)
    setError(null)
    try {
      const res = await fetch(apiBase + '/api/bank-accounts', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(form),
      })
      if (!res.ok) throw new Error('Fehler beim Speichern')
      setForm({ name: '', iban: '', bankName: '', apiProvider: '', apiKey: '' })
      setShowForm(false)
      await loadAccounts()
    } catch (e) {
      setError((e as Error).message)
    } finally {
      setSaving(false)
    }
  }

  async function handleDelete(id: number) {
    if (!window.confirm('Bank Konto wirklich löschen? Alle Transaktionen werden ebenfalls gelöscht.')) return
    setError(null)
    try {
      const res = await fetch(apiBase + `/api/bank-accounts/${id}`, { method: 'DELETE' })
      if (!res.ok) throw new Error('Fehler beim Löschen')
      if (selectedAccount?.id === id) setSelectedAccount(null)
      await loadAccounts()
    } catch (e) {
      setError((e as Error).message)
    }
  }

  async function handleAddTransaction(ev: React.FormEvent) {
    ev.preventDefault()
    if (!selectedAccount) return
    setSavingTx(true)
    setError(null)
    try {
      const res = await fetch(apiBase + `/api/bank-accounts/${selectedAccount.id}/transactions`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          ...txForm,
          amount: txForm.amount ? Number(txForm.amount) : null,
          date: txForm.date || null,
        }),
      })
      if (!res.ok) throw new Error('Fehler beim Speichern')
      setTxForm({ date: '', amount: '', counterparty: '', description: '', category: '', reference: '' })
      setShowTxForm(false)
      await loadTransactions(selectedAccount.id)
    } catch (e) {
      setError((e as Error).message)
    } finally {
      setSavingTx(false)
    }
  }

  async function handleUpdateCategory(txId: number, category: string) {
    try {
      const res = await fetch(apiBase + `/api/bank-accounts/transactions/${txId}/category`, {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ category }),
      })
      if (!res.ok) throw new Error('Fehler beim Aktualisieren')
      if (selectedAccount) await loadTransactions(selectedAccount.id)
    } catch (e) {
      setError((e as Error).message)
    }
  }

  async function handleDeleteTransaction(txId: number) {
    if (!window.confirm('Transaktion löschen?')) return
    try {
      const res = await fetch(apiBase + `/api/bank-accounts/transactions/${txId}`, { method: 'DELETE' })
      if (!res.ok) throw new Error('Fehler beim Löschen')
      if (selectedAccount) await loadTransactions(selectedAccount.id)
    } catch (e) {
      setError((e as Error).message)
    }
  }

  const CATEGORIES = ['Miete', 'Lebensmittel', 'Transport', 'Unterhaltung', 'Gesundheit', 'Versicherung', 'Telekommunikation', 'Abonnement', 'Shopping', 'Sonstiges']

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
      <div style={styles.panel}>
        <div style={{ padding: 16, display: 'flex', flexDirection: 'column', gap: 12 }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <h2 style={styles.h2}>🏦 Bank Konten</h2>
            <button style={styles.primaryButton} onClick={() => setShowForm(!showForm)}>
              {showForm ? 'Abbrechen' : '+ Konto hinzufügen'}
            </button>
          </div>

          {error && <div style={styles.error}>{error}</div>}

          {showForm && (
            <form onSubmit={handleCreate} style={{ display: 'flex', flexDirection: 'column', gap: 8, background: '#f8fafc', padding: 12, borderRadius: 10, border: '1px solid #e5e7eb' }}>
              <label style={styles.label}>
                Name *
                <input
                  style={{ ...styles.textarea, minHeight: 'unset', padding: '8px 10px' }}
                  value={form.name}
                  onChange={e => setForm(f => ({ ...f, name: e.target.value }))}
                  placeholder="z.B. Girokonto"
                  required
                />
              </label>
              <label style={styles.label}>
                IBAN
                <input
                  style={{ ...styles.textarea, minHeight: 'unset', padding: '8px 10px' }}
                  value={form.iban}
                  onChange={e => setForm(f => ({ ...f, iban: e.target.value }))}
                  placeholder="z.B. DE89370400440532013000"
                />
              </label>
              <label style={styles.label}>
                Bankname
                <input
                  style={{ ...styles.textarea, minHeight: 'unset', padding: '8px 10px' }}
                  value={form.bankName}
                  onChange={e => setForm(f => ({ ...f, bankName: e.target.value }))}
                  placeholder="z.B. Deutsche Bank"
                />
              </label>
              <label style={styles.label}>
                API Anbieter
                <input
                  style={{ ...styles.textarea, minHeight: 'unset', padding: '8px 10px' }}
                  value={form.apiProvider}
                  onChange={e => setForm(f => ({ ...f, apiProvider: e.target.value }))}
                  placeholder="z.B. FinTS, GoCardless, Plaid"
                />
              </label>
              <label style={styles.label}>
                API Schlüssel
                <input
                  type="password"
                  style={{ ...styles.textarea, minHeight: 'unset', padding: '8px 10px' }}
                  value={form.apiKey}
                  onChange={e => setForm(f => ({ ...f, apiKey: e.target.value }))}
                  placeholder="API Key (wird verschlüsselt gespeichert)"
                />
              </label>
              <button type="submit" disabled={saving} style={styles.primaryButton}>
                {saving ? 'Speichern...' : 'Speichern'}
              </button>
            </form>
          )}

          {loading && <div style={styles.muted}>Lade...</div>}
          {!loading && accounts.length === 0 && (
            <div style={styles.muted}>Noch keine Bank Konten hinterlegt.</div>
          )}
          <ul style={styles.list}>
            {accounts.map(acc => (
              <li
                key={acc.id}
                style={{ ...styles.listItem, background: selectedAccount?.id === acc.id ? '#eef2ff' : undefined }}
                onClick={() => setSelectedAccount(acc)}
              >
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                  <div>
                    <div style={styles.listTitle}>🏦 {acc.name}</div>
                    <div style={styles.listMeta}>
                      {acc.bankName && <span>{acc.bankName} · </span>}
                      {acc.iban && <span>IBAN: {acc.iban} · </span>}
                      {acc.apiProvider && <span>API: {acc.apiProvider}</span>}
                    </div>
                    {acc.lastSync && <div style={styles.listMeta}>Letzter Sync: {new Date(acc.lastSync).toLocaleString('de-DE')}</div>}
                  </div>
                  <button
                    onClick={e => { e.stopPropagation(); handleDelete(acc.id) }}
                    style={{ ...styles.secondaryButton, marginTop: 0, color: '#b00020' }}
                  >
                    🗑️
                  </button>
                </div>
              </li>
            ))}
          </ul>
        </div>
      </div>

      {selectedAccount && (
        <div style={styles.panel}>
          <div style={{ padding: 16, display: 'flex', flexDirection: 'column', gap: 12 }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <h2 style={styles.h2}>📊 Transaktionen – {selectedAccount.name}</h2>
              <button style={styles.primaryButton} onClick={() => setShowTxForm(!showTxForm)}>
                {showTxForm ? 'Abbrechen' : '+ Transaktion hinzufügen'}
              </button>
            </div>

            {showTxForm && (
              <form onSubmit={handleAddTransaction} style={{ display: 'flex', flexDirection: 'column', gap: 8, background: '#f8fafc', padding: 12, borderRadius: 10, border: '1px solid #e5e7eb' }}>
                <div style={{ display: 'flex', gap: 8 }}>
                  <label style={{ ...styles.label, flex: 1 }}>
                    Datum
                    <input
                      type="date"
                      style={{ ...styles.textarea, minHeight: 'unset', padding: '8px 10px' }}
                      value={txForm.date}
                      onChange={e => setTxForm(f => ({ ...f, date: e.target.value }))}
                    />
                  </label>
                  <label style={{ ...styles.label, flex: 1 }}>
                    Betrag (€)
                    <input
                      type="number"
                      step="0.01"
                      style={{ ...styles.textarea, minHeight: 'unset', padding: '8px 10px' }}
                      value={txForm.amount}
                      onChange={e => setTxForm(f => ({ ...f, amount: e.target.value }))}
                      placeholder="-42.50"
                    />
                  </label>
                </div>
                <label style={styles.label}>
                  Gegenpartei
                  <input
                    style={{ ...styles.textarea, minHeight: 'unset', padding: '8px 10px' }}
                    value={txForm.counterparty}
                    onChange={e => setTxForm(f => ({ ...f, counterparty: e.target.value }))}
                    placeholder="z.B. Amazon, Spotify"
                  />
                </label>
                <label style={styles.label}>
                  Verwendungszweck
                  <input
                    style={{ ...styles.textarea, minHeight: 'unset', padding: '8px 10px' }}
                    value={txForm.description}
                    onChange={e => setTxForm(f => ({ ...f, description: e.target.value }))}
                    placeholder="Beschreibung des Buchungspostens"
                  />
                </label>
                <label style={styles.label}>
                  Kategorie
                  <select
                    style={styles.select}
                    value={txForm.category}
                    onChange={e => setTxForm(f => ({ ...f, category: e.target.value }))}
                  >
                    <option value="">– Keine –</option>
                    {CATEGORIES.map(c => <option key={c} value={c}>{c}</option>)}
                  </select>
                </label>
                <label style={styles.label}>
                  Referenz
                  <input
                    style={{ ...styles.textarea, minHeight: 'unset', padding: '8px 10px' }}
                    value={txForm.reference}
                    onChange={e => setTxForm(f => ({ ...f, reference: e.target.value }))}
                    placeholder="Referenznummer"
                  />
                </label>
                <button type="submit" disabled={savingTx} style={styles.primaryButton}>
                  {savingTx ? 'Speichern...' : 'Speichern'}
                </button>
              </form>
            )}

            {transactions.length === 0 && (
              <div style={styles.muted}>Noch keine Transaktionen für dieses Konto.</div>
            )}
            <ul style={styles.list}>
              {transactions.map(tx => (
                <li key={tx.id} style={styles.listItem}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                    <div style={{ flex: 1 }}>
                      <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
                        <span style={{
                          fontWeight: 700,
                          color: tx.amount != null && tx.amount < 0 ? '#b00020' : '#166534',
                          fontSize: 15,
                        }}>
                          {tx.amount != null ? (tx.amount > 0 ? '+' : '') + tx.amount.toFixed(2) + ' €' : '–'}
                        </span>
                        <span style={styles.listMeta}>{tx.date || '–'}</span>
                        {tx.counterparty && <span style={styles.listTitle}>{tx.counterparty}</span>}
                      </div>
                      {tx.description && <div style={styles.listMeta}>{tx.description}</div>}
                      <div style={{ display: 'flex', gap: 8, alignItems: 'center', marginTop: 4 }}>
                        <select
                          style={{ ...styles.select, padding: '4px 6px', fontSize: 12 }}
                          value={tx.category || ''}
                          onChange={e => handleUpdateCategory(tx.id, e.target.value)}
                        >
                          <option value="">– Kategorie –</option>
                          {CATEGORIES.map(c => <option key={c} value={c}>{c}</option>)}
                        </select>
                      </div>
                    </div>
                    <button
                      onClick={() => handleDeleteTransaction(tx.id)}
                      style={{ ...styles.secondaryButton, marginTop: 0, color: '#b00020' }}
                    >
                      🗑️
                    </button>
                  </div>
                </li>
              ))}
            </ul>
          </div>
        </div>
      )}
    </div>
  )
}
