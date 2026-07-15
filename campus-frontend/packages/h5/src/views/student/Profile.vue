<script setup lang="ts">
</script>
<template>
  <div class="page">
    <div class="profile-header"><van-image round width="64" height="64" src="https://picsum.photos/100"/><div class="ph-name">同学你好</div><div class="ph-id">ID: 2073005678401105922</div></div>
    <van-cell-group inset><van-cell title="我的地址" is-link/><van-cell title="优惠券" is-link to="/student/coupon"/><van-cell title="我的积分" is-link/><van-cell title="设置" is-link/></van-cell-group>
  </div>
</template>
<style scoped>.profile-header{text-align:center;padding:32px 0;background:#fff;margin-bottom:12px}.ph-name{font-size:18px;font-weight:600;margin-top:8px}.ph-id{font-size:12px;color:#999}</style>
