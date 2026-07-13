import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/login/index.vue'),
      meta: { title: '登录' },
    },
    {
      path: '/',
      component: () => import('@/layouts/DefaultLayout.vue'),
      redirect: '/dashboard',
      children: [
        {
          path: 'dashboard',
          name: 'Dashboard',
          component: () => import('@/views/dashboard/index.vue'),
          meta: { title: '数据大盘', icon: 'DataAnalysis' },
        },
        {
          path: 'product',
          name: 'Product',
          component: () => import('@/views/product/index.vue'),
          meta: { title: '商品管理', icon: 'Goods' },
        },
        {
          path: 'order',
          name: 'Order',
          component: () => import('@/views/order/index.vue'),
          meta: { title: '订单管理', icon: 'Document' },
        },
        {
          path: 'coupon',
          name: 'Coupon',
          component: () => import('@/views/coupon/index.vue'),
          meta: { title: '优惠券管理', icon: 'Ticket' },
        },
        {
          path: 'user',
          name: 'User',
          component: () => import('@/views/user/index.vue'),
          meta: { title: '用户管理', icon: 'User' },
        },
        {
          path: 'tenant',
          name: 'Tenant',
          component: () => import('@/views/tenant/index.vue'),
          meta: { title: '租户管理', icon: 'OfficeBuilding' },
        },
        {
          path: 'settlement',
          name: 'Settlement',
          component: () => import('@/views/settlement/index.vue'),
          meta: { title: '结算管理', icon: 'Money' },
        },
        {
          path: 'review',
          name: 'Review',
          component: () => import('@/views/review/index.vue'),
          meta: { title: '评价管理', icon: 'Star' },
        },
        {
          path: 'notify',
          name: 'Notify',
          component: () => import('@/views/notify/index.vue'),
          meta: { title: '通知管理', icon: 'Bell' },
        },
      ],
    },
  ],
})

// 路由守卫：未登录跳转到登录页
router.beforeEach((to, _from, next) => {
  const token = localStorage.getItem('accessToken')
  if (to.path !== '/login' && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router
