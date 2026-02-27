import React, { useEffect, useState } from 'react'
import { ShoppingBag, Users, DollarSign, Clock } from 'lucide-react'
import { getStats } from '../services/api'

export default function Dashboard() {
  const [stats, setStats] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    getStats().then(setStats).catch(console.error).finally(() => setLoading(false))
  }, [])

  if (loading) return <div className="empty-state"><p>Loading dashboard...</p></div>
  if (!stats) return <div className="empty-state"><p>Could not load stats. Is the backend running?</p></div>

  const formatCurrency = (val) =>
    `₦${Number(val || 0).toLocaleString('en-NG', { minimumFractionDigits: 0 })}`

  return (
    <>
      <div className="page-header">
        <h2>Dashboard</h2>
        <p>Overview of your laundry business performance</p>
      </div>

      <div className="stats-grid">
        <div className="stat-card">
          <div className="stat-icon blue"><ShoppingBag size={22} /></div>
          <div className="stat-value">{stats.totalOrders}</div>
          <div className="stat-label">Total Orders</div>
        </div>
        <div className="stat-card">
          <div className="stat-icon green"><DollarSign size={22} /></div>
          <div className="stat-value">{formatCurrency(stats.totalRevenue)}</div>
          <div className="stat-label">Total Revenue</div>
        </div>
        <div className="stat-card">
          <div className="stat-icon orange"><Users size={22} /></div>
          <div className="stat-value">{stats.totalCustomers}</div>
          <div className="stat-label">Customers</div>
        </div>
        <div className="stat-card">
          <div className="stat-icon purple"><Clock size={22} /></div>
          <div className="stat-value">{stats.pendingOrders}</div>
          <div className="stat-label">Pending Orders</div>
        </div>
      </div>

      <div className="card">
        <div className="card-header">
          <h3>Order Status Breakdown</h3>
        </div>
        <div style={{ padding: '24px' }}>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '16px' }}>
            {stats.statusBreakdown && Object.entries(stats.statusBreakdown).map(([status, count]) => (
              <div key={status} style={{
                display: 'flex', justifyContent: 'space-between', alignItems: 'center',
                padding: '14px 18px', background: '#f8fafc', borderRadius: '8px'
              }}>
                <span className={`badge ${status.toLowerCase()}`}>{status.replace('_', ' ')}</span>
                <span style={{ fontWeight: 700, fontSize: '1.2rem' }}>{count}</span>
              </div>
            ))}
          </div>
        </div>
      </div>
    </>
  )
}
