package com.trade.platform.module.template.controller;

import com.trade.platform.common.PageResult;
import com.trade.platform.security.RequireRole;
import com.trade.platform.common.Result;
import com.trade.platform.module.template.dto.TemplateQueryDTO;
import com.trade.platform.module.template.entity.Template;
import com.trade.platform.module.template.service.TemplateService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/templates")
@RequireRole("ADMIN")
public class TemplateController {

    @javax.annotation.Resource
    private TemplateService templateService;

    @PostMapping("/list")
    public Result<PageResult<Template>> getList(@RequestBody TemplateQueryDTO dto) {
        return Result.success(templateService.getList(dto));
    }

    @GetMapping("/{id}")
    public Result<Template> getById(@PathVariable Long id) {
        return Result.success(templateService.getById(id));
    }

    @PostMapping
    public Result<Void> create(@RequestBody Template template) {
        templateService.create(template);
        return Result.success();
    }

    @PutMapping
    public Result<Void> update(@RequestBody Template template) {
        templateService.update(template);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        templateService.delete(id);
        return Result.success();
    }

    @GetMapping("/by-type")
    public Result<List<Template>> getByType(@RequestParam String type) {
        return Result.success(templateService.getByType(type));
    }

    /**
     * 上传模板文件（Word/Excel/PDF）
     */
    @PostMapping("/upload")
    public Result<Template> uploadTemplate(
            @RequestParam("file") MultipartFile file,
            @RequestParam("type") String type,
            @RequestParam("name") String name,
            @RequestParam(value = "description", required = false) String description) {
        Template template = templateService.uploadTemplate(file, type, name, description);
        return Result.success(template);
    }

    /**
     * 更新模板文件
     */
    @PostMapping("/upload/{id}")
    public Result<Template> updateTemplateFile(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        Template template = templateService.updateTemplateFile(id, file);
        return Result.success(template);
    }

    /**
     * 下载模板原文件
     */
    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadTemplate(@PathVariable Long id) throws IOException {
        Template template = templateService.getById(id);
        if (template.getFilePath() == null) {
            return ResponseEntity.notFound().build();
        }

        Path filePath = Paths.get(template.getFilePath());
        Resource resource = new UrlResource(filePath.toUri());
        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        String encodedFileName = URLEncoder.encode(template.getFileName(), "UTF-8").replaceAll("\\+", "%20");

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFileName)
                .body(resource);
    }

    /**
     * 预览模板 PDF（浏览器直接展示）
     */
    @GetMapping("/preview/{id}")
    public ResponseEntity<Resource> previewTemplate(@PathVariable Long id) throws IOException {
        Template template = templateService.getById(id);

        // 优先用 PDF 预览文件
        String previewPath = template.getPdfPath();
        if (previewPath == null) {
            // 没有 PDF 则用原文件（浏览器可能支持 PDF 直接预览）
            previewPath = template.getFilePath();
        }
        if (previewPath == null) {
            return ResponseEntity.notFound().build();
        }

        Path filePath = Paths.get(previewPath);
        Resource resource = new UrlResource(filePath.toUri());
        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        // PDF 直接在浏览器内展示（inline），不触发下载
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .body(resource);
    }
}
