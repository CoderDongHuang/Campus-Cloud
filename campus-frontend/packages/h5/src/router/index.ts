import { createRouter, createWebHistory } from 'vue-router'
const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: '/student' },
    { path: '/student', component: () => import('@/views/student/Home.vue'), meta: { title: '校享云' } },
    { path: '/student/service/:id', component: () => import('@/views/student/ServiceDetail.vue'), meta: { title: '服务详情' } },
    { path: '/student/order/confirm', component: () => import('@/views/student/OrderConfirm.vue'), meta: { title: '确认订单' } },
    { path: '/student/orders', component: () => import('@/views/student/Orders.vue'), meta: { title: '我的订单' } },
    { path: '/student/coupon', component: () => import('@/views/student/Coupon.vue'), meta: { title: '优惠券' } },
    { path: '/student/im/:sessionId', component: () => import('@/views/student/Chat.vue'), meta: { title: '消息' } },
    { path: '/student/profile', component: () => import('@/views/student/Profile.vue'), meta: { title: '我的' } },
    { path: '/worker', component: () => import('@/views/worker/Home.vue'), meta: { title: '师傅端' } },
    { path: '/worker/orders', component: () => import('@/views/worker/Orders.vue'), meta: { title: '工单' } },
    { path: '/worker/income', component: () => import('@/views/worker/Income.vue'), meta: { title: '收入' } },
  ],
})
router.beforeEach((to, _from, next) => {
  // 从URL参数收Token（跨端口跳转时Admin传过来）
  const urlToken = to.query.token as string
  if (urlToken) {
    localStorage.setItem('accessToken', urlToken)
  }
  const token = localStorage.getItem('accessToken')
  if (!token) {
    window.location.href = 'http://localhost:3000/login'
  } else {
    next()
  }
})
export default router
