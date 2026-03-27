import { render, screen, fireEvent } from '@testing-library/react'
import { vi } from 'vitest'
import ConfirmDialog from '../components/ui/ConfirmDialog'

describe('ConfirmDialog', () => {
  const onConfirm = vi.fn()
  const onCancel = vi.fn()

  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('renders the message and action buttons', () => {
    render(
      <ConfirmDialog
        message="Tem certeza que deseja excluir?"
        onConfirm={onConfirm}
        onCancel={onCancel}
      />,
    )

    expect(screen.getByText('Tem certeza que deseja excluir?')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Confirmar' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Cancelar' })).toBeInTheDocument()
  })

  it('calls onConfirm when confirm button is clicked', () => {
    render(
      <ConfirmDialog message="Confirmar?" onConfirm={onConfirm} onCancel={onCancel} />,
    )

    fireEvent.click(screen.getByRole('button', { name: 'Confirmar' }))

    expect(onConfirm).toHaveBeenCalledTimes(1)
    expect(onCancel).not.toHaveBeenCalled()
  })

  it('calls onCancel when cancel button is clicked', () => {
    render(
      <ConfirmDialog message="Confirmar?" onConfirm={onConfirm} onCancel={onCancel} />,
    )

    fireEvent.click(screen.getByRole('button', { name: 'Cancelar' }))

    expect(onCancel).toHaveBeenCalledTimes(1)
    expect(onConfirm).not.toHaveBeenCalled()
  })

  it('disables both buttons when loading is true', () => {
    render(
      <ConfirmDialog
        message="Aguardando..."
        onConfirm={onConfirm}
        onCancel={onCancel}
        loading={true}
      />,
    )

    expect(screen.getByRole('button', { name: 'Aguarde...' })).toBeDisabled()
    expect(screen.getByRole('button', { name: 'Cancelar' })).toBeDisabled()
  })

  it('shows "Confirmar" text when loading is false', () => {
    render(
      <ConfirmDialog
        message="Confirmar?"
        onConfirm={onConfirm}
        onCancel={onCancel}
        loading={false}
      />,
    )

    expect(screen.getByRole('button', { name: 'Confirmar' })).toBeInTheDocument()
  })

  it('has role alertdialog for accessibility', () => {
    render(
      <ConfirmDialog message="Aviso" onConfirm={onConfirm} onCancel={onCancel} />,
    )

    expect(screen.getByRole('alertdialog')).toBeInTheDocument()
  })
})
