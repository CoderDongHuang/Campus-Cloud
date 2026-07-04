package com.sharecampus.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.sharecampus.common.core.exception.BizException;
import com.sharecampus.common.core.exception.ErrorCode;
import com.sharecampus.user.entity.UserAddress;
import com.sharecampus.user.entity.WorkerCertification;
import com.sharecampus.user.mapper.UserAddressMapper;
import com.sharecampus.user.mapper.WorkerCertificationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserAddressMapper addressMapper;
    private final WorkerCertificationMapper certMapper;

    // ==================== 地址管理 ====================

    public List<UserAddress> listAddresses(Long userId) {
        return addressMapper.selectList(
                new LambdaQueryWrapper<UserAddress>()
                        .eq(UserAddress::getUserId, userId)
                        .orderByDesc(UserAddress::getIsDefault));
    }

    @Transactional
    public void addAddress(UserAddress address, Long userId) {
        address.setUserId(userId);
        if (address.getIsDefault() == 1) clearDefaultAddress(userId);
        addressMapper.insert(address);
    }

    @Transactional
    public void updateAddress(UserAddress address, Long userId) {
        if (address.getIsDefault() == 1) clearDefaultAddress(userId);
        addressMapper.updateById(address);
    }

    public void deleteAddress(Long id) { addressMapper.deleteById(id); }

    @Transactional
    public void setDefault(Long id, Long userId) {
        clearDefaultAddress(userId);
        UserAddress a = new UserAddress(); a.setId(id); a.setIsDefault(1);
        addressMapper.updateById(a);
    }

    private void clearDefaultAddress(Long userId) {
        LambdaUpdateWrapper<UserAddress> w = new LambdaUpdateWrapper<>();
        w.eq(UserAddress::getUserId, userId).set(UserAddress::getIsDefault, 0);
        addressMapper.update(w);
    }

    // ==================== 师傅认证 ====================

    public void submitCert(WorkerCertification cert, Long userId) {
        cert.setUserId(userId);
        Long count = certMapper.selectCount(
                new LambdaQueryWrapper<WorkerCertification>()
                        .eq(WorkerCertification::getUserId, userId)
                        .eq(WorkerCertification::getStatus, 0));
        if (count > 0) throw new BizException(ErrorCode.RATE_LIMIT.getCode(), "已有待审核的认证");
        cert.setStatus(0);
        certMapper.insert(cert);
    }

    public WorkerCertification getCertStatus(Long userId) {
        return certMapper.selectOne(
                new LambdaQueryWrapper<WorkerCertification>()
                        .eq(WorkerCertification::getUserId, userId)
                        .orderByDesc(WorkerCertification::getCreateTime)
                        .last("LIMIT 1"));
    }
}
