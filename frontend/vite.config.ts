import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
  build: {
    // Output to backend static resources directory so the JAR serves the frontend.
    // To build locally without integration, set VITE_OUT_DIR=dist.
    outDir: '../backend/src/main/resources/static',
    emptyOutDir: true,
  },
})
