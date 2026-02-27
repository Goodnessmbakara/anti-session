import React, { useEffect, useState } from 'react'
import { Plus, Search, Users as UsersIcon } from 'lucide-react'
import { getCustomers, createCustomer } from '../services/api'

export default function Customers() {
  const [customers, setCustomers] = useState([])
  const [loading, setLoading] = useState(true)
  const [search, setSearch] = useState('')
  const [showModal, setShowModal] = useState(false)
  const [form, setForm] = useState({ name: '', phone: '', email: '', address: '' })

  const loadCustomers = (q = '') => {
    setLoading(true)
    getCustomers(0, 50, q)
      .then(data => setCustomers(data.content || []))
      .catch(console.error)
      .finally(() => setLoading(false))
  }

  useEffect(() => { loadCustomers() }, [])

  const handleSearch = (e) => {
    setSearch(e.target.value)
    loadCustomers(e.target.value)
  }

  const handleCreate = async (e) => {
    e.preventDefault()
    await createCustomer(form)
    setShowModal(false)
    setForm({ name: '', phone: '', email: '', address: '' })
    loadCustomers()
  }

  return (
    <>
      <div className="page-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
        <div>
          <h2>Customers</h2>
          <p>Manage your customer relationships</p>
        </div>
        <button className="btn btn-primary" onClick={() => setShowModal(true)}><Plus size={18} /> Add Customer</button>
      </div>

      <div style={{ marginBottom: '24px', position: 'relative' }}>
        <Search size={18} style={{ position: 'absolute', left: '14px', top: '12px', color: '#94a3b8' }} />
        <input
          type="text"
          placeholder="Search customers by name..."
          value={search}
          onChange={handleSearch}
          style={{
            width: '100%', maxWidth: '400px', padding: '10px 14px 10px 40px',
            border: '1px solid var(--border)', borderRadius: '8px',
            fontFamily: 'var(--font)', fontSize: '0.9rem', outline: 'none'
          }}
        />
      </div>

      <div className="card">
        <div className="table-wrap">
          <table>
            <thead>
              <tr><th>Name</th><th>Phone</th><th>Email</th><th>Address</th></tr>
            </thead>
            <tbody>
              {loading ? (
                <tr><td colSpan={4} style={{ textAlign: 'center', padding: '40px' }}>Loading...</td></tr>
              ) : customers.length === 0 ? (
                <tr><td colSpan={4}>
                  <div className="empty-state"><UsersIcon size={40} /><p>No customers found</p></div>
                </td></tr>
              ) : customers.map(c => (
                <tr key={c.id}>
                  <td style={{ fontWeight: 600 }}>{c.name}</td>
                  <td>{c.phone}</td>
                  <td>{c.email || '—'}</td>
                  <td style={{ color: '#64748b' }}>{c.address || '—'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {showModal && (
        <div className="modal-overlay" onClick={() => setShowModal(false)}>
          <div className="modal" onClick={e => e.stopPropagation()}>
            <div className="modal-header">
              <h3>Add Customer</h3>
              <button className="btn btn-sm btn-outline" onClick={() => setShowModal(false)}>✕</button>
            </div>
            <form onSubmit={handleCreate}>
              <div className="modal-body">
                <div className="form-row">
                  <div className="form-group">
                    <label>Full Name *</label>
                    <input value={form.name} onChange={e => setForm({ ...form, name: e.target.value })} required placeholder="Amara Okafor" />
                  </div>
                  <div className="form-group">
                    <label>Phone *</label>
                    <input value={form.phone} onChange={e => setForm({ ...form, phone: e.target.value })} required placeholder="+234 801 234 5678" />
                  </div>
                </div>
                <div className="form-group">
                  <label>Email</label>
                  <input type="email" value={form.email} onChange={e => setForm({ ...form, email: e.target.value })} placeholder="customer@email.com" />
                </div>
                <div className="form-group">
                  <label>Address</label>
                  <textarea rows={2} value={form.address} onChange={e => setForm({ ...form, address: e.target.value })} placeholder="12 Aba Road, Uyo" />
                </div>
              </div>
              <div className="modal-footer">
                <button type="button" className="btn btn-outline" onClick={() => setShowModal(false)}>Cancel</button>
                <button type="submit" className="btn btn-primary">Add Customer</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </>
  )
}
