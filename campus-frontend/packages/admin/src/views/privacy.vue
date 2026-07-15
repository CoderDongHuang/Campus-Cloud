<template>
  <div class="page">
    <div class="content">
      <h1>隐私政策</h1>
      <p class="update">最后更新日期：2026 年 7 月 14 日</p>

      <p>校享云（以下简称"我们"）深知个人信息对您的重要性，我们将按照法律法规的规定，保护您的个人信息安全。</p>

      <h2>一、我们收集哪些信息</h2>
      <p><b>账户信息：</b>当您注册账号时，我们需要您提供手机号码、学号/工号以及您设置的密码。这些信息用于身份验证和账号安全。</p>
      <p><b>服务信息：</b>当您使用报修、保洁等服务时，我们会记录您的服务地址（宿舍楼栋、房间号）、预约时间、服务偏好，以便师傅准确上门。</p>
      <p><b>位置信息：</b>为了向您推荐就近的师傅和服务，我们会获取您选择的校区和宿舍楼信息。</p>
      <p><b>设备信息：</b>我们会收集您的设备型号、操作系统版本、IP 地址，用于安全风控和异常登录检测。</p>

      <h2>二、信息如何使用</h2>
      <p>您的信息仅用于以下场景：</p>
      <ul>
        <li>为您匹配最近的师傅并派发工单</li>
        <li>向您推送订单状态变更通知（师傅已接单、服务已完成等）</li>
        <li>用于优惠券的发放与核销记录</li>
        <li>生成运营统计数据（匿名化处理后用于平台优化）</li>
        <li>防范安全风险，检测异常登录行为</li>
      </ul>
      <p>我们<b>不会</b>向任何第三方出售、出租或共享您的个人信息，除非得到您的明确授权或法律要求。</p>

      <h2>三、信息如何存储</h2>
      <p>您的个人信息存储在阿里云/私有服务器上的 MySQL 数据库中，密码采用 BCrypt 单向加密存储，任何人都无法反向破解。关键操作日志（登录、下单、支付）均通过 `t_order_log` 等审计表记录，确保可追溯。</p>

      <h2>四、您的权利</h2>
      <ul>
        <li><b>查阅权：</b>您可以在个人中心查看自己的基本信息和历史订单</li>
        <li><b>更正权：</b>您可以修改手机号、地址等个人信息</li>
        <li><b>删除权：</b>您可以申请注销账号，我们将在 7 个工作日内处理</li>
        <li><b>撤回同意：</b>您可以在设置中关闭个性化推荐和消息推送</li>
      </ul>

      <h2>五、数据安全</h2>
      <p>我们采用以下技术手段保障您的数据安全：</p>
      <ul>
        <li>全站 HTTPS 加密传输，防止中间人攻击</li>
        <li>密码 BCrypt + Salt 哈希存储，不可逆</li>
        <li>JWT 双 Token 机制（Access 2h + Refresh 7d），Token 泄露可快速失效</li>
        <li>登录失败限流（Redis 计数器），防止暴力破解</li>
        <li>Gateway 层 IP 黑白名单 + Sentinel 限流熔断</li>
      </ul>

      <h2>六、联系我们</h2>
      <p>如果您对隐私政策有任何疑问，请联系：<br/>邮箱：privacy@sharecampus.cn<br/>地址：校享云后勤信息中心</p>
    </div>
  </div>
</template>

<style scoped>
.page { min-height:100vh; background:#fafaf8; font-family:'Inter','PingFang SC',sans-serif; display:flex; justify-content:center; padding:48px 24px }
.content { max-width:720px; color:#333; line-height:2 }
h1 { font-size:28px; font-weight:600; margin-bottom:4px }
.update { color:#999; font-size:13px; margin-bottom:32px }
h2 { font-size:18px; font-weight:600; margin:32px 0 12px; color:#1a1a1a }
p, li { font-size:14px; color:#555 }
ul { padding-left:20px }
ul li { margin-bottom:6px }
</style>
