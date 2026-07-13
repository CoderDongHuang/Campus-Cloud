<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const router = useRouter()
const mode = ref<'signin' | 'signup'>('signin')
const activeRole = ref('student')
const account = ref('')
const password = ref('')
const loading = ref(false)
const errorMsg = ref('')
const captchaCode = ref('')
const captchaInput = ref('')
const regPhone = ref('')
const regPassword = ref('')
const regConfirmPwd = ref('')
const regCaptchaInput = ref('')
const agreeTerms = ref(false)

const roles = [
  { key: 'student', label: '学生', sub: '学号登录 · 校园生活', ph: '手机号或学号', btn: '进入校园' },
  { key: 'worker', label: '师傅', sub: '工号登录 · 工单设备', ph: '手机号或工号', btn: '开始接单' },
  { key: 'admin', label: '校方', sub: '手机号登录 · 数据统筹', ph: '手机号', btn: '进入后台' },
]
const currentRole = ref(roles[0])

function generateCaptcha() {
  const canvas = document.getElementById('cpc') as HTMLCanvasElement
  if (!canvas) return
  const ctx = canvas.getContext('2d')!, w = 90, h = 38
  ctx.clearRect(0, 0, w, h)
  ctx.fillStyle = '#fafaf9'; ctx.fillRect(0, 0, w, h)
  ctx.strokeStyle = '#e8e4e0'; ctx.lineWidth = 1
  for (let i = 0; i < 4; i++) { ctx.beginPath(); ctx.moveTo(Math.random()*w, Math.random()*h); ctx.lineTo(Math.random()*w, Math.random()*h); ctx.stroke() }
  const chars = '23456789ABCDEFGHJKLMNPQRSTUVWXYZ'
  let code = ''
  for (let i = 0; i < 4; i++) {
    code += chars[Math.floor(Math.random() * chars.length)]
    ctx.font = 'bold 22px "SF Mono","Menlo",monospace'; ctx.textAlign = 'center'; ctx.textBaseline = 'middle'
    ctx.fillStyle = `hsl(${Math.random()*360},8%,${30+Math.random()*30}%)`
    ctx.fillText(code[i], 14 + i * 20, 22 + Math.random() * 6)
  }
  captchaCode.value = code
}

async function login() {
  errorMsg.value = ''
  if (!account.value || !password.value) { errorMsg.value = '请输入账号和密码'; return }
  if (captchaInput.value.toUpperCase() !== captchaCode.value) { errorMsg.value = '验证码错误'; generateCaptcha(); captchaInput.value = ''; return }
  loading.value = true
  try {
    const res = await axios.post('/api/v1/auth/login', { phone: account.value, password: password.value })
    const d = res.data.data
    localStorage.setItem('accessToken', d.accessToken); localStorage.setItem('refreshToken', d.refreshToken); localStorage.setItem('userType', d.userType || 'STUDENT')
    const ut = d.userType || 'STUDENT'
    if (ut === 'STUDENT' || ut === 'WORKER') {
      const target = ut === 'STUDENT' ? 'http://localhost:3001/student' : 'http://localhost:3001/worker'
      window.location.href = target + '?token=' + encodeURIComponent(d.accessToken)
    } else { setTimeout(() => router.push('/dashboard'), 300) }
  } catch (e: any) {
    const code = e?.response?.data?.code
    const map: Record<number,string> = { 1001:'用户不存在，请检查账号', 1002:'密码错误', 1006:'账号已被禁用' }
    errorMsg.value = map[code] || '登录失败，请重试'
    loading.value = false
  }
}

const pwdChecks = computed(() => {
  const p = regPassword.value
  return {
    len: p.length >= 8 && p.length <= 16,
    upper: /[A-Z]/.test(p),
    lower: /[a-z]/.test(p),
    digit: /[0-9]/.test(p),
    special: /[!@#$%^&*()_+\-=\[\]{}|;:,.<>?]/.test(p),
  }
})

function validatePwd(p: string): string|null {
  if (!p) return '请输入密码'
  if (p.length < 8 || p.length > 16) return '密码需8-16位'
  if (!/[A-Z]/.test(p)) return '密码需包含大写字母'
  if (!/[a-z]/.test(p)) return '密码需包含小写字母'
  if (!/[0-9]/.test(p)) return '密码需包含数字'
  if (!/[!@#$%^&*()_+\-=\[\]{}|;:,.<>?]/.test(p)) return '密码需包含特殊字符'
  return null
}

async function register() {
  errorMsg.value = ''
  if (!regPhone.value) { errorMsg.value = '请输入手机号'; return }
  const pwdErr = validatePwd(regPassword.value)
  if (pwdErr) { errorMsg.value = pwdErr; return }
  if (regPassword.value !== regConfirmPwd.value) { errorMsg.value = '两次密码不一致'; return }
  if (!agreeTerms.value) { errorMsg.value = '请同意用户协议'; return }
  if (regCaptchaInput.value.toUpperCase() !== captchaCode.value) { errorMsg.value = '验证码错误'; generateCaptcha(); regCaptchaInput.value = ''; return }
  loading.value = true
  try {
    const res = await axios.post('/api/v1/auth/register', { phone: regPhone.value, password: regPassword.value, code: '' })
    if (res.data.code === 200) { mode.value = 'signin'; account.value = regPhone.value; errorMsg.value = ''; generateCaptcha() }
    else { errorMsg.value = res.data.message || '注册失败' }
  } catch (e: any) { errorMsg.value = e?.response?.data?.message || '注册失败，手机号可能已注册' }
  loading.value = false
}

onMounted(() => generateCaptcha())
</script>

<template>
  <div class="root">
    <header class="top">
      <div class="top-brand">
        <div class="logo-mark">
          <span class="lg-a">X</span>
          <span class="lg-b">C</span>
          <span class="lg-c">Y</span>
        </div>
        <div>
          <span class="top-logo">校享云</span>
          <p class="top-desc">高校智慧后勤平台 · 报修、保洁、洗衣，动动手指就有人上门</p>
        </div>
      </div>
      <span class="top-ver">v2</span>
    </header>

    <div class="center">
      <!-- 标题区 -->
      <h1 v-if="mode === 'signin'" class="heading">登录</h1>
      <h1 v-else class="heading">创建账号</h1>

      <!-- 模式切换 -->
      <div class="switcher">
        <button :class="{ on: mode === 'signin' }" @click="mode = 'signin'; errorMsg = ''; generateCaptcha()">登录</button>
        <span class="sw-divider">/</span>
        <button :class="{ on: mode === 'signup' }" @click="mode = 'signup'; errorMsg = ''; generateCaptcha()">注册</button>
      </div>

      <!-- ====== 登录 ====== -->
      <form v-if="mode === 'signin'" @submit.prevent="login" class="form">
        <!-- 角色选择 -->
        <div class="role-row">
          <button v-for="r in roles" :key="r.key" type="button" :class="['role-chip', { active: activeRole === r.key }]" @click="activeRole = r.key; currentRole = r">{{ r.label }}</button>
        </div>
        <p class="role-desc">{{ currentRole.sub }}</p>

        <input v-model="account" class="inp" :placeholder="currentRole.ph" />
        <input v-model="password" type="password" class="inp" placeholder="密码" />
        <div class="captcha-line">
          <input v-model="captchaInput" class="inp captcha-inp" placeholder="验证码" maxlength="4" />
          <canvas id="cpc" width="90" height="38" class="captcha-img" @click="generateCaptcha" title="点我换一个" />
        </div>
        <p v-if="errorMsg" class="err">{{ errorMsg }}</p>
        <button type="submit" class="btn" :disabled="loading">{{ loading ? '...' : currentRole.btn }}</button>
        <p class="tip">13800000000 / admin123</p>
      </form>

      <!-- ====== 注册 ====== -->
      <form v-else @submit.prevent="register" class="form">
        <div class="role-row">
          <button v-for="r in roles" :key="r.key" type="button" :class="['role-chip', { active: activeRole === r.key }]" @click="activeRole = r.key; currentRole = r">{{ r.label }}</button>
        </div>
        <template v-if="activeRole !== 'admin'">
          <input v-model="regPhone" class="inp" :placeholder="activeRole==='student'?'手机号或学号':activeRole==='worker'?'手机号或工号':'手机号'" />
          <div class="captcha-line">
            <input v-model="regCaptchaInput" class="inp captcha-inp" placeholder="验证码" maxlength="4" />
            <canvas id="cpc" width="90" height="38" class="captcha-img" @click="generateCaptcha" title="点我换一个" />
          </div>
          <input v-model="regPassword" type="password" class="inp" placeholder="密码" />
          <div class="pwd-checks" v-if="regPassword">
            <span :class="pwdChecks.len?'ok':'fail'">{{ pwdChecks.len ? '✓' : '✗' }} 8-16位</span>
            <span :class="pwdChecks.upper?'ok':'fail'">{{ pwdChecks.upper ? '✓' : '✗' }} 大写字母</span>
            <span :class="pwdChecks.lower?'ok':'fail'">{{ pwdChecks.lower ? '✓' : '✗' }} 小写字母</span>
            <span :class="pwdChecks.digit?'ok':'fail'">{{ pwdChecks.digit ? '✓' : '✗' }} 数字</span>
            <span :class="pwdChecks.special?'ok':'fail'">{{ pwdChecks.special ? '✓' : '✗' }} 特殊字符</span>
          </div>
          <input v-model="regConfirmPwd" type="password" class="inp" placeholder="确认密码" />
          <label class="terms"><input type="checkbox" v-model="agreeTerms" /> 同意用户协议</label>
        </template>
        <p v-else class="locked">校方账号由管理员开通</p>
        <p v-if="errorMsg" class="err">{{ errorMsg }}</p>
        <button v-if="activeRole !== 'admin'" type="submit" class="btn" :disabled="loading">{{ loading ? '...' : '注册' }}</button>
      </form>
    </div>

    <footer class="foot">
      <span>15 微服务</span><span>/</span><span>ShardingSphere</span><span>/</span><span>Canal + ES</span><span>/</span><span>SSL</span>
    </footer>
  </div>
</template>

<style>
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap');
</style>

<style scoped>
.root {
  min-height: 100vh; display: flex; flex-direction: column; align-items: center;
  font-family: 'Inter', -apple-system, sans-serif; background: #fafaf8; color: #1a1a1a;
}
.top { width: 100%; display: flex; justify-content: space-between; padding: 20px 28px; }
.top-brand { display:flex; align-items:center; gap:14px }
.logo-mark { width:52px; height:52px; background:#1a1a1a; border-radius:10px; position:relative; overflow:hidden; flex-shrink:0 }
.lg-a { position:absolute; top:-3px; left:4px; font-size:30px; font-weight:800; color:rgba(255,255,255,.9); font-family:'SF Mono','Menlo',monospace; line-height:1 }
.lg-b { position:absolute; top:2px; right:-4px; font-size:20px; font-weight:700; color:rgba(255,255,255,.45); font-family:'SF Mono','Menlo',monospace; line-height:1 }
.lg-c { position:absolute; bottom:-3px; right:5px; font-size:24px; font-weight:800; color:rgba(255,255,255,.7); font-family:'SF Mono','Menlo',monospace; line-height:1 }
.top-logo { font-size:24px; font-weight:600; letter-spacing:2px }
.top-desc { font-size:14px; color:#999; margin-top:4px }
.top-ver { font-size: 11px; color: #bbb; }

.center { flex: 1; display: flex; flex-direction: column; align-items: center; justify-content: center; width: 100%; max-width: 360px; padding: 0 24px; }
.heading { font-size: 32px; font-weight: 300; letter-spacing: 4px; margin: 0 0 24px; }

.switcher { display: flex; gap: 6px; align-items: center; margin-bottom: 36px; }
.switcher button { background: none; border: none; font-size: 14px; color: #bbb; cursor: pointer; padding: 0; font-family: inherit; }
.switcher button.on { color: #1a1a1a; font-weight: 600; }
.sw-divider { color: #ddd; font-size: 14px; }

.role-row { display: flex; gap: 2px; margin-bottom: 12px; }
.role-chip { background: none; border: none; padding: 6px 16px; font-size: 13px; color: #aaa; cursor: pointer; border-radius: 20px; font-family: inherit; transition: .2s; }
.role-chip:hover { color: #666; }
.role-chip.active { background: #1a1a1a; color: #fff; }
.role-desc { font-size: 13px; color: #bbb; margin: 0 0 20px; }

.form { width: 100%; display: flex; flex-direction: column; gap: 12px; }
.inp { width: 100%; padding: 12px 0; border: none; border-bottom: 1px solid #e8e4e0; font-size: 15px; outline: none; background: transparent; font-family: inherit; color: #1a1a1a; transition: border-color .2s; }
.inp:focus { border-bottom-color: #1a1a1a; }
.inp::placeholder { color: #d0ccc8; }

.captcha-line { display: flex; gap: 12px; align-items: flex-end; }
.captcha-inp { flex: 1; }
.captcha-img { border-radius: 4px; cursor: pointer; height: 38px; border: 1px solid #e8e4e0; }

.terms { display: flex; align-items: center; gap: 8px; font-size: 13px; color: #999; cursor: pointer; }
.terms input { accent-color: #1a1a1a; }

.err { color: #d44; font-size: 13px; }
.pwd-checks { display:flex; flex-wrap:wrap; gap:4px 10px; margin:-4px 0 4px }
.pwd-checks span { font-size:11px }
.pwd-checks .ok { color:#4CAF50 }
.pwd-checks .fail { color:#ccc }

.btn { padding: 14px 0; border: 1px solid #1a1a1a; background: #1a1a1a; color: #fff; font-size: 14px; font-weight: 500; cursor: pointer; border-radius: 2px; font-family: inherit; letter-spacing: 1px; transition: .2s; }
.btn:hover { background: #333; }
.btn:disabled { opacity: .4; }

.locked { text-align: center; color: #ccc; padding: 20px 0; }
.tip { text-align: center; font-size: 12px; color: #ddd; }
.foot { padding: 20px; font-size: 11px; color: #ddd; display: flex; gap: 8px; justify-content: center; }
</style>
