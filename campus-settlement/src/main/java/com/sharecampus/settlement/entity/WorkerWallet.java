package com.sharecampus.settlement.entity;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
@Data @TableName("worker_wallet")
public class WorkerWallet {
    @TableId
    private Long userId; private BigDecimal pendingAmount;
    private BigDecimal availableAmount; private BigDecimal frozenAmount;
    private BigDecimal withdrawnAmount; private BigDecimal totalEarned;
    private Integer version;
}
