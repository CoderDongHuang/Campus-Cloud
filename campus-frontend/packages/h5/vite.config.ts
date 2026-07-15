import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import Components from 'unplugin-vue-components/vite'
import { VantResolver } from '@vant/auto-import-resolver'
import path from 'path'

export default defineConfig({
  plugins: [vue(), Components({ resolvers: [VantResolver()] })],
  resolve: { alias: { '@': path.resolve(__dirname, 'src'), '@shared': path.resolve(__dirname, '../../shared') } },
  server: { port: 3001, proxy: { '/api': { target: 'http://localhost:8080', changeOrigin: true } } },
})
