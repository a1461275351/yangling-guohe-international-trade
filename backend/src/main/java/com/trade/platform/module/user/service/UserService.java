package com.trade.platform.module.user.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.trade.platform.common.BusinessException;
import com.trade.platform.common.Constants;
import com.trade.platform.common.PageResult;
import com.trade.platform.module.tenant.entity.Tenant;
import com.trade.platform.module.tenant.mapper.TenantMapper;
import com.trade.platform.module.user.dto.LoginDTO;
import com.trade.platform.module.user.dto.LoginVO;
import com.trade.platform.module.user.dto.PasswordDTO;
import com.trade.platform.module.user.dto.RegisterDTO;
import com.trade.platform.module.user.dto.ResetPasswordDTO;
import com.trade.platform.module.user.dto.UserApplyQueryDTO;
import com.trade.platform.module.user.dto.UserQueryDTO;
import com.trade.platform.module.user.entity.User;
import com.trade.platform.module.user.entity.UserApply;
import com.trade.platform.module.user.mapper.UserApplyMapper;
import com.trade.platform.module.user.mapper.UserMapper;
import com.trade.platform.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

@Service
public class UserService {

    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final long LOCKOUT_DURATION_MS = 30 * 60 * 1000L;
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=]).{8,32}$");

    private final ConcurrentHashMap<String, int[]> loginAttempts = new ConcurrentHashMap<>();

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserApplyMapper applyMapper;

    @Autowired
    private TenantMapper tenantMapper;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 用户登录
     */
    public LoginVO login(LoginDTO dto) {
        String lockKey = dto.getTenantCode() + ":" + dto.getUsername();
        checkLockout(lockKey);

        Tenant tenant = null;
        User user;

        if ("ADMIN".equals(dto.getTenantCode())) {
            // ADMIN用户使用特殊租户编码登录
            user = userMapper.selectOne(
                    new QueryWrapper<User>()
                            .eq("username", dto.getUsername())
                            .eq("role", Constants.ROLE_ADMIN)
            );
        } else {
            // 查找租户
            tenant = tenantMapper.selectOne(
                    new QueryWrapper<Tenant>().eq("tenant_code", dto.getTenantCode())
            );
            if (tenant == null) {
                throw new BusinessException("租户不存在");
            }
            if (tenant.getStatus() != null && tenant.getStatus() == Constants.STATUS_DISABLED) {
                throw new BusinessException("租户已被禁用，请联系管理员");
            }

            user = userMapper.selectOne(
                    new QueryWrapper<User>()
                            .eq("username", dto.getUsername())
                            .eq("tenant_id", tenant.getId())
            );
        }

        if (user == null) {
            recordFailedLogin(lockKey);
            throw new BusinessException("用户名或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == Constants.STATUS_DISABLED) {
            throw new BusinessException("用户已被禁用，请联系管理员");
        }
        if (!BCrypt.checkpw(dto.getPassword(), user.getPassword())) {
            recordFailedLogin(lockKey);
            throw new BusinessException("用户名或密码错误");
        }

        loginAttempts.remove(lockKey);

        // 生成JWT
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole(), user.getTenantId());

        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setRole(user.getRole());
        vo.setTenantId(user.getTenantId());
        if (tenant != null) {
            vo.setTenantName(tenant.getName());
            vo.setTenantCode(tenant.getTenantCode());
        } else {
            vo.setTenantCode("ADMIN");
            vo.setTenantName("系统管理");
        }

        return vo;
    }

    /**
     * 用户注册（提交申请）
     */
    public void register(RegisterDTO dto) {
        // 查找租户
        Tenant tenant = tenantMapper.selectOne(
                new QueryWrapper<Tenant>().eq("tenant_code", dto.getTenantCode())
        );
        if (tenant == null) {
            throw new BusinessException("租户不存在");
        }

        // 检查用户名唯一性
        Long count = userMapper.selectCount(
                new QueryWrapper<User>()
                        .eq("username", dto.getUsername())
                        .eq("tenant_id", tenant.getId())
        );
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }

        // 检查是否有待审批的申请
        Long applyCount = applyMapper.selectCount(
                new QueryWrapper<UserApply>()
                        .eq("username", dto.getUsername())
                        .eq("tenant_id", tenant.getId())
                        .eq("status", "PENDING")
        );
        if (applyCount > 0) {
            throw new BusinessException("该用户名已有待审批的申请，请等待审批结果");
        }

        validatePasswordComplexity(dto.getPassword());

        UserApply apply = new UserApply();
        apply.setUsername(dto.getUsername());
        apply.setPassword(BCrypt.hashpw(dto.getPassword(), BCrypt.gensalt()));
        apply.setRealName(dto.getRealName());
        apply.setPhone(dto.getPhone());
        apply.setEmail(dto.getEmail());
        apply.setRole(Constants.ROLE_ENTERPRISE);
        apply.setTenantId(tenant.getId());
        apply.setStatus("PENDING");
        apply.setCreateTime(LocalDateTime.now());
        apply.setUpdateTime(LocalDateTime.now());

        applyMapper.insert(apply);
    }

    /**
     * 重置密码申请
     */
    public void resetPasswordRequest(ResetPasswordDTO dto) {
        Tenant tenant = tenantMapper.selectOne(
                new QueryWrapper<Tenant>().eq("tenant_code", dto.getTenantCode())
        );
        if (tenant == null) {
            throw new BusinessException("租户不存在");
        }

        User user = userMapper.selectOne(
                new QueryWrapper<User>()
                        .eq("username", dto.getUsername())
                        .eq("tenant_id", tenant.getId())
        );
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 创建密码重置申请记录
        UserApply apply = new UserApply();
        apply.setUsername(dto.getUsername());
        apply.setTenantId(tenant.getId());
        apply.setRealName(user.getRealName());
        apply.setPhone(user.getPhone());
        apply.setEmail(user.getEmail());
        apply.setRole(user.getRole());
        apply.setStatus("PENDING");
        apply.setCreateTime(LocalDateTime.now());
        apply.setUpdateTime(LocalDateTime.now());

        applyMapper.insert(apply);
    }

    /**
     * 分页查询用户列表
     */
    public PageResult<User> getUserList(UserQueryDTO dto) {
        Page<User> page = new Page<>(dto.getCurrent(), dto.getSize());
        QueryWrapper<User> wrapper = new QueryWrapper<>();

        if (dto.getTenantId() != null) {
            wrapper.eq("tenant_id", dto.getTenantId());
        }
        if (dto.getStatus() != null) {
            wrapper.eq("status", dto.getStatus());
        }
        if (StrUtil.isNotBlank(dto.getKeyword())) {
            wrapper.and(w -> w.like("username", dto.getKeyword())
                    .or().like("real_name", dto.getKeyword()));
        }
        wrapper.orderByDesc("create_time");

        IPage<User> result = userMapper.selectPage(page, wrapper);
        return PageResult.from(result);
    }

    /**
     * 更新用户状态
     */
    public void updateUserStatus(Long id, Integer status) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setStatus(status);
        userMapper.updateById(user);
    }

    /**
     * 重置用户密码为随机密码并返回
     */
    public String resetUserPassword(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        String newPassword = generateRandomPassword(12);
        user.setPassword(BCrypt.hashpw(newPassword, BCrypt.gensalt()));
        userMapper.updateById(user);
        return newPassword;
    }

    private String generateRandomPassword(int length) {
        String chars = "ABCDEFGHJKMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789!@#$%";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    /**
     * 分页查询申请列表
     */
    public PageResult<UserApply> getApplyList(UserApplyQueryDTO dto) {
        Page<UserApply> page = new Page<>(dto.getCurrent(), dto.getSize());
        QueryWrapper<UserApply> wrapper = new QueryWrapper<>();

        if (dto.getTenantId() != null) {
            wrapper.eq("tenant_id", dto.getTenantId());
        }
        if (StrUtil.isNotBlank(dto.getStatus())) {
            wrapper.eq("status", dto.getStatus());
        }
        if (StrUtil.isNotBlank(dto.getStartDate())) {
            wrapper.ge("create_time", dto.getStartDate());
        }
        if (StrUtil.isNotBlank(dto.getEndDate())) {
            wrapper.le("create_time", dto.getEndDate());
        }
        wrapper.orderByDesc("create_time");

        IPage<UserApply> result = applyMapper.selectPage(page, wrapper);
        return PageResult.from(result);
    }

    /**
     * 审批通过
     */
    @Transactional
    public void approveApply(Long id) {
        UserApply apply = applyMapper.selectById(id);
        if (apply == null) {
            throw new BusinessException("申请记录不存在");
        }
        if (!"PENDING".equals(apply.getStatus())) {
            throw new BusinessException("该申请已处理");
        }

        // 更新申请状态
        apply.setStatus("APPROVED");
        apply.setUpdateTime(LocalDateTime.now());
        applyMapper.updateById(apply);

        // 如果有密码说明是注册申请，创建用户记录
        if (StrUtil.isNotBlank(apply.getPassword())) {
            // 再次检查用户名唯一性
            Long count = userMapper.selectCount(
                    new QueryWrapper<User>()
                            .eq("username", apply.getUsername())
                            .eq("tenant_id", apply.getTenantId())
            );
            if (count > 0) {
                throw new BusinessException("用户名已存在，无法创建用户");
            }

            User user = new User();
            user.setUsername(apply.getUsername());
            user.setPassword(apply.getPassword());
            user.setRealName(apply.getRealName());
            user.setPhone(apply.getPhone());
            user.setEmail(apply.getEmail());
            user.setRole(apply.getRole());
            user.setTenantId(apply.getTenantId());
            user.setStatus(Constants.STATUS_ACTIVE);
            userMapper.insert(user);
        } else {
            // 密码重置申请，重置为默认密码
            User user = userMapper.selectOne(
                    new QueryWrapper<User>()
                            .eq("username", apply.getUsername())
                            .eq("tenant_id", apply.getTenantId())
            );
            if (user != null) {
                String newPassword = generateRandomPassword(12);
                user.setPassword(BCrypt.hashpw(newPassword, BCrypt.gensalt()));
                userMapper.updateById(user);
            }
        }
    }

    /**
     * 审批拒绝
     */
    public void rejectApply(Long id, String reason) {
        UserApply apply = applyMapper.selectById(id);
        if (apply == null) {
            throw new BusinessException("申请记录不存在");
        }
        if (!"PENDING".equals(apply.getStatus())) {
            throw new BusinessException("该申请已处理");
        }

        apply.setStatus("REJECTED");
        apply.setRejectReason(reason);
        apply.setUpdateTime(LocalDateTime.now());
        applyMapper.updateById(apply);
    }

    /**
     * 获取用户信息
     */
    public User getUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setPassword(null);
        return user;
    }

    /**
     * 更新用户基本信息
     */
    public void updateUserInfo(Long userId, String realName, String phone, String email) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (realName != null) {
            user.setRealName(realName);
        }
        if (phone != null) {
            user.setPhone(phone);
        }
        if (email != null) {
            user.setEmail(email);
        }
        userMapper.updateById(user);
    }

    /**
     * 修改密码
     */
    public void changePassword(Long userId, PasswordDTO dto) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (StrUtil.isNotBlank(dto.getOldPassword())) {
            if (!BCrypt.checkpw(dto.getOldPassword(), user.getPassword())) {
                throw new BusinessException("原密码错误");
            }
        }
        validatePasswordComplexity(dto.getNewPassword());
        user.setPassword(BCrypt.hashpw(dto.getNewPassword(), BCrypt.gensalt()));
        userMapper.updateById(user);
    }

    /**
     * 批量审批通过
     */
    @Transactional
    public void batchApprove(List<Long> ids) {
        for (Long id : ids) {
            approveApply(id);
        }
    }

    /**
     * 根据租户ID更新所有用户状态
     */
    public void updateUserStatusByTenantId(Long tenantId, Integer status) {
        UpdateWrapper<User> wrapper = new UpdateWrapper<>();
        wrapper.eq("tenant_id", tenantId).set("status", status);
        userMapper.update(null, wrapper);
    }

    private void validatePasswordComplexity(String password) {
        if (password == null || !PASSWORD_PATTERN.matcher(password).matches()) {
            throw new BusinessException("密码需8-32位，包含大小写字母、数字和特殊字符(!@#$%^&*()_+-)");
        }
    }

    private void checkLockout(String key) {
        int[] attempts = loginAttempts.get(key);
        if (attempts != null && attempts[0] >= MAX_LOGIN_ATTEMPTS) {
            long lockedAt = ((long) attempts[1] << 32) | (attempts[2] & 0xFFFFFFFFL);
            if (System.currentTimeMillis() - lockedAt < LOCKOUT_DURATION_MS) {
                long remainMinutes = (LOCKOUT_DURATION_MS - (System.currentTimeMillis() - lockedAt)) / 60000 + 1;
                throw new BusinessException("登录失败次数过多，请" + remainMinutes + "分钟后再试");
            }
            loginAttempts.remove(key);
        }
    }

    private void recordFailedLogin(String key) {
        loginAttempts.compute(key, (k, v) -> {
            if (v == null) {
                long now = System.currentTimeMillis();
                return new int[]{1, (int) (now >> 32), (int) now};
            }
            v[0]++;
            if (v[0] >= MAX_LOGIN_ATTEMPTS) {
                long now = System.currentTimeMillis();
                v[1] = (int) (now >> 32);
                v[2] = (int) now;
            }
            return v;
        });
        int[] attempts = loginAttempts.get(key);
        int remaining = MAX_LOGIN_ATTEMPTS - (attempts != null ? attempts[0] : 0);
        if (remaining > 0 && remaining <= 3) {
            throw new BusinessException("用户名或密码错误，还剩" + remaining + "次尝试机会");
        }
    }
}
