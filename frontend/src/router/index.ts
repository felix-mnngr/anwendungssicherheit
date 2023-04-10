import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView
    },
    {
      path: '/create',
      name: 'create',
      component: () => import('../views/KontoCreateView.vue')
    },
    {
      path: '/add',
      name: 'add',
      component: () => import('../views/KontoAddBewegungView.vue')
    },
    {
      path: '/konten',
      name: 'konten',
      component: () => import('../views/KontenView.vue')
    },
    {
      path: '/bewegungen',
      name: 'bewegungen',
      component: () => import('../views/BewegungenView.vue')
    }
  ]
})

export default router
