import React, { useEffect, useState } from 'react'
import { Plus, Shirt as ShirtIcon } from 'lucide-react'
import { getServices, createService } from '../services/api'

const CATEGORIES = ['WASH', 'DRY_CLEAN', 'IRON', 'FOLD', 'WASH_AND_IRON', 'SPECIAL_CARE']
const UNIT_TYPES = ['KG', 'PIECE', 'LOAD']

export default function Services() {
  const [services, setServices] = useState([])
  const [loading, setLoading] = useState(true)
  const [showModal, setShowModal] = useState(false)
  const [form, setForm] = useState({ name: '', category: 'WASH', pricePerUnit: '', unitType: 'KG' })

  const loadServices = () => {
    setLoading(true)
    getServices()
      .then(setServices)
      .catch(console.error)
      .finally(() => setLoading(false))
  }

  useEffect(() => { loadServices() }, [])

  const handleCreate = async (e) => {
    e.preventDefault()
    await createService({ ...form, pricePerUnit: parseFloat(form.pricePerUnit) })
    setShowModal(false)
    setForm({ name: '', category: 'WASH', pricePerUnit: '', unitType: 'KG' })
    loadServices()
  }

  const formatCurrency = (val) =>
    `₦${Number(val || 0).toLocaleString('en-NG', { minimumFractionDigits: 0 })}`

  return (
    <>
      <div className="page-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
        <div>
          <h2>Services & Pricing</h2>
          <p>Manage your laundry service catalog</p>
        </div>
        <button className="btn btn-primary" onClick={() => setShowModal(true)}><Plus size={18} /> Add Service</button>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(280px, 1fr))', gap: '20px' }}>
        {loading ? (
          <div className="empty-state" style={{ gridColumn: '1/-1' }}><p>Loading services...</p></div>
        ) : services.length === 0 ? (
          <div className="empty-state" style={{ gridColumn: '1/-1' }}>
            <ShirtIcon size={40} /><p>No services yet. Add your first service.</p>
          </div>
        ) : services.map(s => (
          <div key={s.id} className="stat-card" style={{ cursor: 'default' }}>
            <span className={`badge ${s.category === 'WASH' ? 'picked_up' : s.category === 'DRY_CLEAN' ? 'processing' : s.category === 'IRON' ? 'pending' : 'ready'}`}>
              {s.category?.replace('_', ' ')}
            </span>
            <h3 style={{ fontSize: '1.3rem', fontWeight: 700, margin: '12px 0 8px', letterSpacing: '-0.02em' }}>{s.name}</h3>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'baseline' }}>
              <span style={{ fontSize: '1.6rem', fontWeight: 800, color: '#2563eb' }}>{formatCurrency(s.pricePerUnit)}</span>
              <span style={{ color: '#64748b', fontSize: '0.85rem' }}>per {s.unitType}</span>
            </div>
          </div>
        ))}
      </div>

      {showModal && (
        <div className="modal-overlay" onClick={() => setShowModal(false)}>
          <div className="modal" onClick={e => e.stopPropagation()}>
            <div className="modal-header">
              <h3>Add Service</h3>
              <button className="btn btn-sm btn-outline" onClick={() => setShowModal(false)}>✕</button>
            </div>
            <form onSubmit={handleCreate}>
              <div className="modal-body">
                <div className="form-group">
                  <label>Service Name *</label>
                  <input value={form.name} onChange={e => setForm({ ...form, name: e.target.value })} required placeholder="e.g. Wash & Fold" />
                </div>
                <div className="form-row">
                  <div className="form-group">
                    <label>Category *</label>
                    <select value={form.category} onChange={e => setForm({ ...form, category: e.target.value })}>
                      {CATEGORIES.map(c => <option key={c} value={c}>{c.replace('_', ' ')}</option>)}
                    </select>
                  </div>
                  <div className="form-group">
                    <label>Unit Type *</label>
                    <select value={form.unitType} onChange={e => setForm({ ...form, unitType: e.target.value })}>
                      {UNIT_TYPES.map(u => <option key={u} value={u}>{u}</option>)}
                    </select>
                  </div>
                </div>
                <div className="form-group">
                  <label>Price per Unit (₦) *</label>
                  <input type="number" min="0" step="100" value={form.pricePerUnit} onChange={e => setForm({ ...form, pricePerUnit: e.target.value })} required placeholder="1500" />
                </div>
              </div>
              <div className="modal-footer">
                <button type="button" className="btn btn-outline" onClick={() => setShowModal(false)}>Cancel</button>
                <button type="submit" className="btn btn-primary">Add Service</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </>
  )
}
