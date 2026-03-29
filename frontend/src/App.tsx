import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { Suspense, lazy } from 'react'
import LoginPage from './pages/LoginPage'
import AppLayout from './pages/AppLayout'
import PageLoader from './components/ui/PageLoader'
import { useAuthStore } from './store/authStore'

const Dashboard = lazy(() => import('./components/dashboard/Dashboard'))
const ClientsPage = lazy(() => import('./pages/ClientsPage'))
const ProductsPage = lazy(() => import('./pages/ProductsPage'))
const ServiceOrdersPage = lazy(() => import('./pages/ServiceOrdersPage'))
const FinancialPage = lazy(() => import('./pages/FinancialPage'))

function PrivateRoute({ children }: { children: React.ReactNode }) {
  const { token } = useAuthStore()
  return token ? <>{children}</> : <Navigate to="/login" replace />
}

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route
          path="/"
          element={
            <PrivateRoute>
              <AppLayout />
            </PrivateRoute>
          }
        >
          <Route index element={<Navigate to="/dashboard" replace />} />
          <Route
            path="dashboard"
            element={
              <Suspense fallback={<PageLoader />}>
                <Dashboard />
              </Suspense>
            }
          />
          <Route
            path="clients"
            element={
              <Suspense fallback={<PageLoader />}>
                <ClientsPage />
              </Suspense>
            }
          />
          <Route
            path="products"
            element={
              <Suspense fallback={<PageLoader />}>
                <ProductsPage />
              </Suspense>
            }
          />
          <Route
            path="service-orders"
            element={
              <Suspense fallback={<PageLoader />}>
                <ServiceOrdersPage />
              </Suspense>
            }
          />
          <Route
            path="financial"
            element={
              <Suspense fallback={<PageLoader />}>
                <FinancialPage />
              </Suspense>
            }
          />
        </Route>
        <Route path="*" element={<Navigate to="/dashboard" replace />} />
      </Routes>
    </BrowserRouter>
  )
}

export default App
