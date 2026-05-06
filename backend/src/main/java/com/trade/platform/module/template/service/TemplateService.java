package com.trade.platform.module.template.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.trade.platform.common.BusinessException;
import com.trade.platform.common.PageResult;
import com.trade.platform.module.template.dto.TemplateQueryDTO;
import com.trade.platform.module.template.entity.Template;
import com.trade.platform.module.template.mapper.TemplateMapper;
import fr.opensagres.poi.xwpf.converter.pdf.PdfConverter;
import fr.opensagres.poi.xwpf.converter.pdf.PdfOptions;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class TemplateService {

    private static final Logger log = LoggerFactory.getLogger(TemplateService.class);

    @Resource
    private TemplateMapper templateMapper;

    @Value("${file.upload-path:./uploads/}")
    private String uploadPath;

    public PageResult<Template> getList(TemplateQueryDTO dto) {
        LambdaQueryWrapper<Template> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(dto.getType())) {
            wrapper.eq(Template::getType, dto.getType());
        }
        if (StringUtils.hasText(dto.getKeyword())) {
            wrapper.and(w -> w.like(Template::getName, dto.getKeyword())
                    .or().like(Template::getDescription, dto.getKeyword()));
        }
        wrapper.orderByDesc(Template::getCreateTime);
        Page<Template> page = new Page<>(dto.getCurrent(), dto.getSize());
        templateMapper.selectPage(page, wrapper);

        PageResult<Template> result = new PageResult<>();
        result.setRecords(page.getRecords());
        result.setTotal(page.getTotal());
        result.setCurrent(page.getCurrent());
        result.setSize(page.getSize());
        return result;
    }

    public Template getById(Long id) {
        Template template = templateMapper.selectById(id);
        if (template == null) throw new BusinessException("模板不存在");
        return template;
    }

    public void create(Template template) {
        templateMapper.insert(template);
    }

    public void update(Template template) {
        Template existing = templateMapper.selectById(template.getId());
        if (existing == null) throw new BusinessException("模板不存在");
        templateMapper.updateById(template);
    }

    public void delete(Long id) {
        Template existing = templateMapper.selectById(id);
        if (existing == null) throw new BusinessException("模板不存在");
        deleteFileQuietly(existing.getFilePath());
        deleteFileQuietly(existing.getPdfPath());
        templateMapper.deleteById(id);
    }

    public List<Template> getByType(String type) {
        LambdaQueryWrapper<Template> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Template::getType, type).orderByDesc(Template::getCreateTime);
        return templateMapper.selectList(wrapper);
    }

    /**
     * 上传模板文件
     * .docx 文件自动转换为 PDF 用于在线预览
     */
    public Template uploadTemplate(MultipartFile file, String type, String name, String description) {
        validateFile(file);
        String storedPath = saveFile(file, "templates");
        String ext = getExtension(file.getOriginalFilename());

        Template template = new Template();
        template.setType(type);
        template.setName(name);
        template.setDescription(description);
        template.setFileName(file.getOriginalFilename());
        template.setFilePath(storedPath);
        template.setFileSize(file.getSize());

        // docx 自动转 PDF 用于预览
        if ("docx".equals(ext)) {
            String pdfPath = convertDocxToPdf(Paths.get(storedPath));
            if (pdfPath != null) {
                template.setPdfPath(pdfPath);
            }
        }

        templateMapper.insert(template);
        return template;
    }

    /**
     * 替换模板文件
     */
    public Template updateTemplateFile(Long id, MultipartFile file) {
        Template existing = templateMapper.selectById(id);
        if (existing == null) throw new BusinessException("模板不存在");
        validateFile(file);

        // 删除旧文件
        deleteFileQuietly(existing.getFilePath());
        deleteFileQuietly(existing.getPdfPath());

        String storedPath = saveFile(file, "templates");
        String ext = getExtension(file.getOriginalFilename());

        existing.setFileName(file.getOriginalFilename());
        existing.setFilePath(storedPath);
        existing.setFileSize(file.getSize());
        existing.setPdfPath(null);

        if ("docx".equals(ext)) {
            String pdfPath = convertDocxToPdf(Paths.get(storedPath));
            if (pdfPath != null) {
                existing.setPdfPath(pdfPath);
            }
        }

        templateMapper.updateById(existing);
        return existing;
    }

    // ==================== 私有方法 ====================

    /**
     * docx 转 PDF（用于预览）
     * 借助 xdocreport 的 PdfConverter
     */
    private String convertDocxToPdf(Path docxPath) {
        try {
            Path pdfPath = Paths.get(docxPath.toString().replaceAll("\\.docx$", ".pdf"));

            try (InputStream is = Files.newInputStream(docxPath);
                 OutputStream os = Files.newOutputStream(pdfPath)) {
                XWPFDocument document = new XWPFDocument(is);
                PdfOptions options = PdfOptions.create();
                PdfConverter.getInstance().convert(document, os, options);
                document.close();
            }

            log.info("docx转PDF成功: {} -> {}", docxPath.getFileName(), pdfPath.getFileName());
            return pdfPath.toString();
        } catch (NoClassDefFoundError e) {
            log.warn("docx转PDF依赖缺失，跳过: {}", e.getMessage());
            return null;
        } catch (Exception e) {
            log.warn("docx转PDF失败，跳过: {}", e.getMessage());
            return null;
        }
    }

    private String saveFile(MultipartFile file, String subDir) {
        try {
            String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            Path dir = Paths.get(uploadPath, subDir, dateDir).toAbsolutePath().normalize();
            Files.createDirectories(dir);

            String ext = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
            String storedName = UUID.randomUUID().toString().replace("-", "") + ext;
            Path target = dir.resolve(storedName);

            Files.copy(file.getInputStream(), target);
            return target.toString();
        } catch (IOException e) {
            throw new BusinessException("文件保存失败: " + e.getMessage());
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new BusinessException("请选择文件");
        if (file.getSize() > 20 * 1024 * 1024) throw new BusinessException("文件大小不能超过20MB");
        String name = file.getOriginalFilename();
        if (name == null) throw new BusinessException("文件名不能为空");
        String ext = getExtension(name);
        if (!("doc".equals(ext) || "docx".equals(ext) || "xls".equals(ext) || "xlsx".equals(ext) || "pdf".equals(ext)))
            throw new BusinessException("仅支持 doc, docx, xls, xlsx, pdf 格式");
    }

    private String getExtension(String filename) {
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    private void deleteFileQuietly(String path) {
        if (StringUtils.hasText(path)) {
            try { Files.deleteIfExists(Paths.get(path)); } catch (IOException ignored) {}
        }
    }
}
