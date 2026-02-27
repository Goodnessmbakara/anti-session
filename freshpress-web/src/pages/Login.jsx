import React, { useState } from 'react'
import { CheckCircle2 } from 'lucide-react'
import { login, register, setToken } from '../services/api'

export default function Login({ onLogin }) {
  const [isRegister, setIsRegister] = useState(false)
  const [form, setForm] = useState({ email: '', password: '', fullName: '' })
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      const res = isRegister ? await register(form) : await login(form)
      setToken(res.token)
      onLogin()
    } catch (err) {
      setError(err.message || 'Authentication failed')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div style={{ display: 'flex', minHeight: '100vh', background: '#fff' }}>
      
      {/* LEFT SIDE - BRANDING */}
      <div style={{
        flex: 1,
        background: 'linear-gradient(135deg, #0f172a 0%, #1e293b 100%)',
        color: 'white',
        display: 'flex',
        flexDirection: 'column',
        justifyContent: 'center',
        padding: '0 8%',
        position: 'relative',
        overflow: 'hidden'
      }}>
        {/* Decorative background shapes */}
        <div style={{
          position: 'absolute', top: '-10%', left: '-10%', width: '300px', height: '300px',
          background: 'rgba(56, 189, 248, 0.15)', borderRadius: '50%', filter: 'blur(80px)'
        }}></div>
        <div style={{
          position: 'absolute', bottom: '-10%', right: '-10%', width: '400px', height: '400px',
          background: 'rgba(249, 115, 22, 0.15)', borderRadius: '50%', filter: 'blur(100px)'
        }}></div>

        <div style={{ position: 'relative', zIndex: 10, maxWidth: '500px' }}>
          <h1 style={{ fontSize: '3.5rem', fontWeight: 900, letterSpacing: '-0.04em', lineHeight: 1.1, marginBottom: '24px' }}>
            Run your laundry business <span style={{ color: '#f97316' }}>seamlessly.</span>
          </h1>
          <p style={{ fontSize: '1.2rem', color: '#94a3b8', marginBottom: '40px', lineHeight: 1.6 }}>
            FreshPress is the all-in-one operating system for modern laundry and dry cleaning businesses.
          </p>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
            {[
              'Track orders from drop-off to delivery',
              'Manage customer relationships effortlessly',
              'Automate pricing and staff workflows'
            ].map((feature, i) => (
              <div key={i} style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
                <div style={{ background: 'rgba(249, 115, 22, 0.2)', padding: '6px', borderRadius: '50%', display: 'flex' }}>
                  <CheckCircle2 size={20} color="#f97316" />
                </div>
                <span style={{ fontSize: '1.05rem', fontWeight: 500, color: '#e2e8f0' }}>{feature}</span>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* RIGHT SIDE - FORM */}
      <div style={{
        flex: 1,
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        padding: '40px',
        position: 'relative'
      }}>
        <div style={{ width: '100%', maxWidth: '400px' }}>
          <div style={{ marginBottom: '40px', display: 'flex', flexDirection: 'column' }}>
            <img src="/logo.png" alt="Logo" style={{ width: '48px', height: '48px', borderRadius: '12px', objectFit: 'contain', background: '#0f172a', marginBottom: '16px' }} />
            <h2 style={{ fontSize: '2rem', fontWeight: 800, letterSpacing: '-0.03em', color: '#0f172a' }}>
              Fresh<span style={{ color: '#f97316' }}>Press</span>
            </h2>
            <p style={{ color: '#64748b', marginTop: '8px', fontSize: '1rem' }}>
              {isRegister ? 'Create an account to get started' : 'Welcome back to your dashboard'}
            </p>
          </div>

          {error && (
            <div style={{
              background: '#fee2e2', color: '#991b1b', padding: '14px 16px',
              borderRadius: '10px', fontSize: '0.9rem', marginBottom: '24px',
              fontWeight: 500
            }}>{error}</div>
          )}

          <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
            {isRegister && (
              <div className="form-group" style={{ marginBottom: 0 }}>
                <label style={{ color: '#475569' }}>Full Name</label>
                <input
                  type="text"
                  placeholder="e.g. Goodness Mbakara"
                  value={form.fullName}
                  onChange={e => setForm({ ...form, fullName: e.target.value })}
                  required
                  style={{ background: '#f8fafc', border: '1px solid #e2e8f0', padding: '12px 16px' }}
                />
              </div>
            )}
            <div className="form-group" style={{ marginBottom: 0 }}>
              <label style={{ color: '#475569' }}>Email address</label>
              <input
                type="email"
                placeholder="admin@freshpress.com"
                value={form.email}
                onChange={e => setForm({ ...form, email: e.target.value })}
                required
                style={{ background: '#f8fafc', border: '1px solid #e2e8f0', padding: '12px 16px' }}
              />
            </div>
            <div className="form-group" style={{ marginBottom: 0 }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '6px' }}>
                <label style={{ margin: 0, color: '#475569' }}>Password</label>
                {!isRegister && <span style={{ fontSize: '0.8rem', color: '#2563eb', cursor: 'pointer', fontWeight: 500 }}>Forgot password?</span>}
              </div>
              <input
                type="password"
                placeholder="••••••••"
                value={form.password}
                onChange={e => setForm({ ...form, password: e.target.value })}
                required
                style={{ background: '#f8fafc', border: '1px solid #e2e8f0', padding: '12px 16px' }}
              />
            </div>

            <button
              type="submit"
              className="btn btn-primary"
              disabled={loading}
              style={{
                width: '100%', justifyContent: 'center', padding: '14px',
                fontSize: '1rem', marginTop: '8px', borderRadius: '10px',
                background: '#0f172a', /* Dark button instead of blue */
              }}
            >
              {loading ? 'Authenticating...' : isRegister ? 'Create Account' : 'Sign In'}
            </button>
          </form>

          <p style={{ textAlign: 'center', marginTop: '32px', fontSize: '0.9rem', color: '#64748b' }}>
            {isRegister ? 'Already have an account?' : "Don't have an account?"}{' '}
            <button
              onClick={() => setIsRegister(!isRegister)}
              style={{
                background: 'none', border: 'none', color: '#0f172a',
                cursor: 'pointer', fontWeight: 700, fontFamily: 'inherit'
              }}
            >
              {isRegister ? 'Sign in instead' : 'Create one now'}
            </button>
          </p>

          {!isRegister && (
            <div style={{
              marginTop: '40px', padding: '16px', background: '#f8fafc',
              borderRadius: '10px', border: '1px dashed #cbd5e1', textAlign: 'center'
            }}>
              <p style={{ fontSize: '0.75rem', color: '#64748b', fontWeight: 600, textTransform: 'uppercase', letterSpacing: '0.05em', marginBottom: '4px' }}>
                Demo Credentials
              </p>
              <p style={{ fontSize: '0.85rem', color: '#0f172a', fontWeight: 500 }}>
                admin@freshpress.com<br/>password123
              </p>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
