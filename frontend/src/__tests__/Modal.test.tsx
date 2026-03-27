import { render, screen, fireEvent } from '@testing-library/react'
import { vi } from 'vitest'
import Modal from '../components/ui/Modal'

describe('Modal', () => {
  const onClose = vi.fn()

  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('renders the modal with title and children', () => {
    render(
      <Modal title="Título do Modal" onClose={onClose}>
        <p>Conteúdo do modal</p>
      </Modal>,
    )

    expect(screen.getByRole('dialog')).toBeInTheDocument()
    expect(screen.getByText('Título do Modal')).toBeInTheDocument()
    expect(screen.getByText('Conteúdo do modal')).toBeInTheDocument()
  })

  it('calls onClose when the close button is clicked', () => {
    render(
      <Modal title="Modal" onClose={onClose}>
        <span>content</span>
      </Modal>,
    )

    fireEvent.click(screen.getByRole('button', { name: 'Fechar' }))

    expect(onClose).toHaveBeenCalledTimes(1)
  })

  it('calls onClose when Escape key is pressed', () => {
    render(
      <Modal title="Modal" onClose={onClose}>
        <span>content</span>
      </Modal>,
    )

    fireEvent.keyDown(document, { key: 'Escape' })

    expect(onClose).toHaveBeenCalledTimes(1)
  })

  it('does not call onClose on other key presses', () => {
    render(
      <Modal title="Modal" onClose={onClose}>
        <span>content</span>
      </Modal>,
    )

    fireEvent.keyDown(document, { key: 'Enter' })
    fireEvent.keyDown(document, { key: 'Tab' })

    expect(onClose).not.toHaveBeenCalled()
  })

  it('has aria-modal attribute set to true', () => {
    render(
      <Modal title="Acessível" onClose={onClose}>
        <span>content</span>
      </Modal>,
    )

    expect(screen.getByRole('dialog')).toHaveAttribute('aria-modal', 'true')
  })

  it('removes keydown listener on unmount', () => {
    const { unmount } = render(
      <Modal title="Modal" onClose={onClose}>
        <span>content</span>
      </Modal>,
    )

    unmount()
    fireEvent.keyDown(document, { key: 'Escape' })

    expect(onClose).not.toHaveBeenCalled()
  })
})
