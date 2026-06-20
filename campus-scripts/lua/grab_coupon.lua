--[[
  抢券 Lua 脚本 — 原子操作：去重 + 验库存 + 扣减 + 标记已抢

  KEYS[1]: 库存 key     coupon:stock:{templateId}
  KEYS[2]: 已抢集合 key  coupon:grabbed:{templateId}
  ARGV[1]: userId
  ARGV[2]: count（默认 1）

  返回值:
    1   = 抢券成功
    -1  = 库存不足
    -2  = 已抢过（去重）
]]

-- 1. 去重：检查是否已抢
if redis.call('sismember', KEYS[2], ARGV[1]) == 1 then
    return -2
end

-- 2. 验库存
local stock = tonumber(redis.call('get', KEYS[1]) or '0')
if stock <= 0 then
    return -1
end

-- 3. 扣库存 + 4. 标记已抢
redis.call('decrby', KEYS[1], tonumber(ARGV[2]))
redis.call('sadd', KEYS[2], ARGV[1])

return 1
