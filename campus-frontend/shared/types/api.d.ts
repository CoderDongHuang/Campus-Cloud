// 统一 API 响应体
export interface ApiResult<T = unknown> {
  code: number
  message: string
  data: T
  timestamp: number
}

// 登录响应
export interface LoginResult {
  accessToken: string
  refreshToken: string
  userId: string
  nickname: string
  userType: 'STUDENT' | 'WORKER' | 'ADMIN' | 'SUPER_ADMIN'
}

// 分页参数
export interface PageQuery {
  page: number
  size: number
}

// 商品 SPU
export interface ProductSpu {
  id: number
  name: string
  description: string
  categoryId: number
  status: number
  salesCount: number
  mainImage?: string
}

// 订单
export interface Order {
  orderId: number
  orderNo: string
  userId: number
  spuId: number
  status: string
  actualAmount: number
  createTime: string
  workerId?: number
}

// 优惠券
export interface UserCoupon {
  id: number
  templateId: number
  userId: number
  status: 'UNUSED' | 'LOCKED' | 'USED' | 'EXPIRED'
  discountValue: number
  useThreshold: number
  expireTime: string
}

// 师傅钱包
export interface WorkerWallet {
  userId: number
  pendingAmount: number
  availableAmount: number
  totalEarned: number
}
