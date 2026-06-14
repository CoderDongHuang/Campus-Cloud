package com.sharecampus.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.sharecampus.common.core.exception.BizException;
import com.sharecampus.common.core.exception.ErrorCode;
import com.sharecampus.common.security.UserContext;
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

    public List<UserAddress> listAddresses() {
        Long userId = UserContext.getUserId();
        return addressMapper.selectList(
                new LambdaQueryWrapper<UserAddress>()
                        .eq(UserAddress::getUserId, userId)
                        .orderByDesc(UserAddress::getIsDefault));
    }

    @Transactional
    public void addAddress(UserAddress address) {
        address.setUserId(UserContext.getUserId());
        // 如果设为默认，先清除旧默认
        if (address.getIsDefault() == 1) {
            clearDefaultAddress();
        }
        addressMapper.insert(address);
    }

    @Transactional
    public void updateAddress(UserAddress address) {
        if (address.getIsDefault() == 1) {
            clearDefaultAddress();
        }
        addressMapper.updateById(address);
    }

    public void deleteAddress(Long id) {
        addressMapper.deleteById(id);
    }

    @Transactional
    public void setDefault(Long id) {
        clearDefaultAddress();
        UserAddress address = new UserAddress();
        address.setId(id);
        address.setIsDefault(1);
        addressMapper.updateById(address);
    }

    private void clearDefaultAddress() {
        LambdaUpdateWrapper<UserAddress> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(UserAddress::getUserId, UserContext.getUserId())
               .set(UserAddress::getIsDefault, 0);
        addressMapper.update(wrapper);
    }

    // ==================== 师傅认证 ====================

    public void submitCert(WorkerCertification cert) {
        cert.setUserId(UserContext.getUserId());
        // 检查是否已有待审核的认证
        Long count = certMapper.selectCount(
                new LambdaQueryWrapper<WorkerCertification>()
                        .eq(WorkerCertification::getUserId, UserContext.getUserId())
                        .eq(WorkerCertification::getStatus, 0));
        if (count > 0) {
            throw new BizException(ErrorCode.RATE_LIMIT.getCode(), "已有待审核的认证，请耐心等待");
        }
        cert.setStatus(0);
        certMapper.insert(cert);
    }

    public WorkerCertification getCertStatus() {
        return certMapper.selectOne(
                new LambdaQueryWrapper<WorkerCertification>()
                        .eq(WorkerCertification::getUserId, UserContext.getUserId())
                        .orderByDesc(WorkerCertification::getCreateTime)
                        .last("LIMIT 1"));
    }
}
