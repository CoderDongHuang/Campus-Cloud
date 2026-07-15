<script setup lang="ts">
import { ref, onMounted } from 'vue'; import { useRoute } from 'vue-router'; import axios from 'axios'
const route = useRoute(); const messages = ref<any[]>([]); const inputText = ref('')
onMounted(async () => { try { const res = await axios.get(`/api/v1/im/messages/${route.params.sessionId}`); messages.value = res.data.data || [] } catch {} })
function send() { if (!inputText.value) return; messages.value.push({ senderType:'STUDENT', content:inputText.value, createdAt:new Date().toISOString() }); inputText.value = '' }
</script>
<template>
  <div class="page"><van-nav-bar :title="'会话: '+route.params.sessionId" left-arrow @click-left="() => $router.back()" />
    <div class="msg-list"><div v-for="m in messages" :key="m.id" :class="['msg', m.senderType==='STUDENT'?'msg-right':'msg-left']"><div class="msg-bubble" :class="m.senderType==='STUDENT'?'bubble-right':'bubble-left'">{{m.content}}</div></div></div>
    <div class="input-bar"><van-field v-model="inputText" placeholder="输入消息..." @keyup.enter="send" /><van-button size="small" type="primary" @click="send">发送</van-button></div>
  </div>
</template>
<style scoped>.page{padding-bottom:60px}.msg-list{padding:12px 16px;min-height:calc(100vh - 160px)}.msg{display:flex;margin-bottom:12px}.msg-right{justify-content:flex-end}.msg-left{justify-content:flex-start}.msg-bubble{max-width:70%;padding:10px 14px;border-radius:16px;font-size:14px;line-height:1.6}.bubble-right{background:#FF9800;color:#fff;border-bottom-right-radius:4px}.bubble-left{background:#fff;color:#333;border-bottom-left-radius:4px}.input-bar{position:fixed;bottom:0;left:0;right:0;display:flex;align-items:center;gap:8px;padding:8px 12px;background:#fff;border-top:1px solid #f0f0f0}</style>
