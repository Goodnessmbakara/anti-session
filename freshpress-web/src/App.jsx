import React, { useState } from 'react'
import { LayoutDashboard, ShoppingBag, Users, Shirt, LogOut } from 'lucide-react'
import Dashboard from './pages/Dashboard'
import Orders from './pages/Orders'
import Customers from './pages/Customers'
import Services from './pages/Services'
import Login from './pages/Login'
import { getToken, clearToken } from './services/api'

const navItems = [
  { id: 'dashboard', label: 'Dashboard', icon: LayoutDashboard },
  { id: 'orders', label: 'Orders', icon: ShoppingBag },
  { id: 'customers', label: 'Customers', icon: Users },
  { id: 'services', label: 'Services', icon: Shirt },
]

export default function App() {
  const [page, setPage] = useState('dashboard')
  const [loggedIn, setLoggedIn] = useState(!!getToken())

  if (!loggedIn) {
    return <Login onLogin={() => setLoggedIn(true)} />
  }

  const handleLogout = () => {
    clearToken()
    setLoggedIn(false)
  }

  const renderPage = () => {
    switch (page) {
      case 'dashboard':  return <Dashboard />
      case 'orders':     return <Orders />
      case 'customers':  return <Customers />
      case 'services':   return <Services />
      default:           return <Dashboard />
    }
  }

  return (
    <div className="app-layout">
      <aside className="sidebar">
        <div className="sidebar-brand">
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
            <img src="/logo.png" alt="FreshPress Logo" style={{ width: '32px', height: '32px', borderRadius: '8px', objectFit: 'contain', background: 'white' }} />
            <h1>Fresh<span>Press</span></h1>
          </div>
          <p>Laundry Management</p>
        </div>
        <nav className="sidebar-nav">
          {navItems.map(item => (
            <button
              key={item.id}
              className={`nav-item ${page === item.id ? 'active' : ''}`}
              onClick={() => setPage(item.id)}
            >
              <item.icon size={20} />
              {item.label}
            </button>
          ))}
        </nav>
        <div style={{ padding: '0 12px', marginTop: 'auto' }}>
          <button className="nav-item" onClick={handleLogout}>
            <LogOut size={20} />
            Logout
          </button>
        </div>
      </aside>
      <main className="main-content">
        {renderPage()}
      </main>
    </div>
  )
}
