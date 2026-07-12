// IM消息TTL索引 — 90天后自动清理历史消息
db = db.getSiblingDB("campus_im");
db.im_message.createIndex(
  { "createdAt": 1 },
  { expireAfterSeconds: 7776000, name: "idx_ttl_90d" }
);
print("IM TTL index created: 90-day auto-cleanup");
