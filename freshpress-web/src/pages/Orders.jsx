import React, { useEffect, useState } from 'react'
import { Plus, Package } from 'lucide-react'
import { getOrders, createOrder, updateOrderStatus, getCustomers, getServices } from '../services/api'

const STATUSES = ['PENDING', 'PICKED_UP', 'PROCESSING', 'READY', 'DELIVERED', 'CANCELLED']

export default function Orders() {
  const [orders, setOrders] = useState([])
  const [loading, setLoading] = useState(true)
  const [showModal, setShowModal] = useState(false)
  const [customers, setCustomers] = useState([])
  const [services, setServices] = useState([])
  const [filterStatus, setFilterStatus] = useState('')
  const [form, setForm] = useState({ customerId: '', notes: '', items: [{ serviceItemId: '', quantity: 1 }] })

  const loadOrders = () => {
    setLoading(true)
    getOrders(0, 50, filterStatus)
      .then(data => setOrders(data.content || []))
      .catch(console.error)
      .finally(() => setLoading(false))
  }

  useEffect(() => { loadOrders() }, [filterStatus])

  const openModal = async () => {
    const [c, s] = await Promise.all([getCustomers(), getServices()])
    setCustomers(c.content || [])
    setServices(s || [])
    setForm({ customerId: '', notes: '', items: [{ serviceItemId: '', quantity: 1 }] })
    setShowModal(true)
  }

  const addItem = () => setForm({ ...form, items: [...form.items, { serviceItemId: '', quantity: 1 }] })

  const updateItem = (i, field, val) => {
    const items = [...form.items]
    items[i][field] = field === 'quantity' ? parseInt(val) || 1 : val
    setForm({ ...form, items })
  }

  const handleCreate = async (e) => {
    e.preventDefault()
    await createOrder({
      customerId: parseInt(form.customerId),
      notes: form.notes,
      items: form.items.map(i => ({ serviceItemId: parseInt(i.serviceItemId), quantity: i.quantity }))
    })
    setShowModal(false)
    loadOrders()
  }

  const handleStatusChange = async (id, newStatus) => {
    await updateOrderStatus(id, newStatus)
    loadOrders()
  }

  const formatCurrency = (val) =>
    `₦${Number(val || 0).toLocaleString('en-NG', { minimumFractionDigits: 0 })}`

  return (
    <>
      <div className="page-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
        <div>
          <h2>Orders</h2>
          <p>Manage laundry orders and track their progress</p>
        </div>
        <button className="btn btn-primary" onClick={openModal}><Plus size={18} /> New Order</button>
      </div>

      <div style={{ display: 'flex', gap: '8px', marginBottom: '24px', flexWrap: 'wrap' }}>
        <button className={`btn btn-sm ${!filterStatus ? 'btn-primary' : 'btn-outline'}`} onClick={() => setFilterStatus('')}>All</button>
        {STATUSES.map(s => (
          <button key={s} className={`btn btn-sm ${filterStatus === s ? 'btn-primary' : 'btn-outline'}`} onClick={() => setFilterStatus(s)}>
            {s.replace('_', ' ')}
          </button>
        ))}
      </div>

      <div className="card">
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>ID</th>
                <th>Customer</th>
                <th>Items</th>
                <th>Total</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr><td colSpan={6} style={{ textAlign: 'center', padding: '40px' }}>Loading...</td></tr>
              ) : orders.length === 0 ? (
                <tr><td colSpan={6}>
                  <div className="empty-state"><Package size={40} /><p>No orders found</p></div>
                </td></tr>
              ) : orders.map(order => (
                <tr key={order.id}>
                  <td style={{ fontWeight: 700 }}>#{order.id}</td>
                  <td>{order.customer?.name || '—'}</td>
                  <td>{order.items?.length || 0} items</td>
                  <td style={{ fontWeight: 600 }}>{formatCurrency(order.totalAmount)}</td>
                  <td><span className={`badge ${order.status?.toLowerCase()}`}>{order.status?.replace('_', ' ')}</span></td>
                  <td>
                    <select
                      className="btn btn-sm btn-outline"
                      value={order.status}
                      onChange={(e) => handleStatusChange(order.id, e.target.value)}
                      style={{ fontSize: '0.75rem' }}
                    >
                      {STATUSES.map(s => <option key={s} value={s}>{s.replace('_', ' ')}</option>)}
                    </select>
                  </td>
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
              <h3>New Order</h3>
              <button className="btn btn-sm btn-outline" onClick={() => setShowModal(false)}>✕</button>
            </div>
            <form onSubmit={handleCreate}>
              <div className="modal-body">
                <div className="form-group">
                  <label>Customer</label>
                  <select value={form.customerId} onChange={e => setForm({ ...form, customerId: e.target.value })} required>
                    <option value="">Select customer...</option>
                    {customers.map(c => <option key={c.id} value={c.id}>{c.name} — {c.phone}</option>)}
                  </select>
                </div>
                <div className="form-group">
                  <label>Notes</label>
                  <textarea rows={2} value={form.notes} onChange={e => setForm({ ...form, notes: e.target.value })} placeholder="Special instructions..." />
                </div>
                <label style={{ fontWeight: 600, fontSize: '0.85rem', marginBottom: '8px', display: 'block' }}>Service Items</label>
                {form.items.map((item, i) => (
                  <div key={i} className="form-row" style={{ marginBottom: '12px' }}>
                    <div className="form-group" style={{ marginBottom: 0 }}>
                      <select value={item.serviceItemId} onChange={e => updateItem(i, 'serviceItemId', e.target.value)} required>
                        <option value="">Select service...</option>
                        {services.map(s => <option key={s.id} value={s.id}>{s.name} — ₦{s.pricePerUnit}/{s.unitType}</option>)}
                      </select>
                    </div>
                    <div className="form-group" style={{ marginBottom: 0 }}>
                      <input type="number" min="1" value={item.quantity} onChange={e => updateItem(i, 'quantity', e.target.value)} placeholder="Qty" />
                    </div>
                  </div>
                ))}
                <button type="button" className="btn btn-sm btn-outline" onClick={addItem}>+ Add Item</button>
              </div>
              <div className="modal-footer">
                <button type="button" className="btn btn-outline" onClick={() => setShowModal(false)}>Cancel</button>
                <button type="submit" className="btn btn-primary">Create Order</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </>
  )
}
