package com.trade.platform.module.file.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.trade.platform.common.BusinessException;
import com.trade.platform.common.PageResult;
import com.trade.platform.module.file.dto.FileQueryDTO;
import com.trade.platform.module.file.entity.BizFile;
import com.trade.platform.module.file.mapper.FileMapper;
import com.trade.platform.security.UserContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class FileService {

    @Resource
    private FileMapper fileMapper;

    @Value("${file.upload-path}")
    private String uploadPath;

    public BizFile upload(MultipartFile file, String businessType) {
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new BusinessException("文件大小不能超过5MB");
        }

        String originalName = file.getOriginalFilename();
        String ext = originalName.substring(originalName.lastIndexOf("."));
        String fileName = UUID.randomUUID().toString().replace("-", "") + ext;

        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        // 将相对路径转为绝对路径，避免MultipartFile.transferTo使用临时目录
        Path dir = Paths.get(uploadPath, datePath).toAbsolutePath().normalize();
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            throw new BusinessException("创建目录失败");
        }

        String filePath = datePath + "/" + fileName;
        try {
            Path target = dir.resolve(fileName);
            Files.copy(file.getInputStream(), target);
        } catch (IOException e) {
            throw new BusinessException("文件上传失败: " + e.getMessage());
        }

        BizFile bizFile = new BizFile();
        bizFile.setTenantId(UserContext.getTenantId());
        bizFile.setFileName(fileName);
        bizFile.setOriginalName(originalName);
        bizFile.setFilePath(filePath);
        bizFile.setFileSize(file.getSize());
        bizFile.setFileType(file.getContentType());
        bizFile.setBusinessType(businessType);
        bizFile.setCreateTime(LocalDateTime.now());
        fileMapper.insert(bizFile);

        return bizFile;
    }

    public PageResult<BizFile> getList(FileQueryDTO dto) {
        LambdaQueryWrapper<BizFile> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(dto.getFileName())) {
            wrapper.like(BizFile::getOriginalName, dto.getFileName());
        }
        if (StringUtils.hasText(dto.getBusinessType())) {
            wrapper.eq(BizFile::getBusinessType, dto.getBusinessType());
        }
        if (StringUtils.hasText(dto.getStartDate())) {
            wrapper.ge(BizFile::getCreateTime, LocalDate.parse(dto.getStartDate()).atStartOfDay());
        }
        if (StringUtils.hasText(dto.getEndDate())) {
            wrapper.le(BizFile::getCreateTime, LocalDate.parse(dto.getEndDate()).atTime(23, 59, 59));
        }
        wrapper.orderByDesc(BizFile::getCreateTime);

        Page<BizFile> page = new Page<>(dto.getCurrent(), dto.getSize());
        fileMapper.selectPage(page, wrapper);

        PageResult<BizFile> result = new PageResult<>();
        result.setRecords(page.getRecords());
        result.setTotal(page.getTotal());
        result.setCurrent(page.getCurrent());
        result.setSize(page.getSize());
        return result;
    }

    public void delete(Long id) {
        fileMapper.deleteById(id);
    }

    public void batchDelete(List<Long> ids) {
        fileMapper.deleteBatchIds(ids);
    }

    public BizFile getById(Long id) {
        return fileMapper.selectById(id);
    }

    public void rename(Long id, String newName) {
        BizFile file = fileMapper.selectById(id);
        if (file == null) {
            throw new BusinessException("文件不存在");
        }
        file.setOriginalName(newName);
        fileMapper.updateById(file);
    }
}
