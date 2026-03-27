import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import Sidebar from '../components/layout/Sidebar'

function renderSidebar(initialPath = '/dashboard') {
  return render(
    <MemoryRouter initialEntries={[initialPath]}>
      <Sidebar />
    </MemoryRouter>,
  )
}

describe('Sidebar', () => {
  it('renders all navigation links', () => {
    renderSidebar()

    expect(screen.getByText('Dashboard')).toBeInTheDocument()
    expect(screen.getByText('Clientes')).toBeInTheDocument()
    expect(screen.getByText('Produtos')).toBeInTheDocument()
    expect(screen.getByText('Ordens de Serviço')).toBeInTheDocument()
    expect(screen.getByText('Financeiro')).toBeInTheDocument()
  })

  it('links point to correct paths', () => {
    renderSidebar()

    expect(screen.getByRole('link', { name: /Dashboard/ })).toHaveAttribute('href', '/dashboard')
    expect(screen.getByRole('link', { name: /Clientes/ })).toHaveAttribute('href', '/clients')
    expect(screen.getByRole('link', { name: /Produtos/ })).toHaveAttribute('href', '/products')
    expect(screen.getByRole('link', { name: /Ordens de Serviço/ })).toHaveAttribute(
      'href',
      '/service-orders',
    )
    expect(screen.getByRole('link', { name: /Financeiro/ })).toHaveAttribute('href', '/financial')
  })

  it('renders inside an aside element', () => {
    renderSidebar()
    expect(document.querySelector('aside')).toBeInTheDocument()
  })

  it('renders icons alongside labels', () => {
    renderSidebar()
    expect(screen.getByText('📊')).toBeInTheDocument()
    expect(screen.getByText('👥')).toBeInTheDocument()
    expect(screen.getByText('📦')).toBeInTheDocument()
    expect(screen.getByText('🔧')).toBeInTheDocument()
    expect(screen.getByText('💰')).toBeInTheDocument()
  })
})
