package com.trade.platform.module.template.controller;

import com.trade.platform.common.BusinessException;
import com.trade.platform.common.Result;
import com.trade.platform.config.OnlyOfficeProperties;
import com.trade.platform.module.template.entity.Template;
import com.trade.platform.module.template.service.TemplateService;
import com.trade.platform.security.RequireRole;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

@RestController
@RequestMapping("/api/templates/onlyoffice")
public class OnlyOfficeController {

    private static final Logger log = LoggerFactory.getLogger(OnlyOfficeController.class);

    @Resource
    private TemplateService templateService;

    @Resource
    private OnlyOfficeProperties onlyOfficeProps;

    @Value("${file.upload-path:./uploads/}")
    private String uploadPath;

    /**
     * 获取 ONLYOFFICE 编辑器配置
     */
    @GetMapping("/config/{id}")
    @RequireRole("ADMIN")
    public Result<Map<String, Object>> getEditorConfig(
            @PathVariable Long id,
            @RequestParam(value = "mode", defaultValue = "edit") String mode) {
        Template template = templateService.getById(id);
        if (!StringUtils.hasText(template.getFilePath())) {
            throw new BusinessException("该模板没有上传文件，无法使用Office编辑");
        }

        String fileName = template.getFileName();
        String fileExt = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();

        // 从文件绝对路径提取 uploads/ 后面的相对部分
        String absFilePath = template.getFilePath().replace("\\", "/");
        String relativePath;
        int uploadsIdx = absFilePath.indexOf("/uploads/");
        if (uploadsIdx >= 0) {
            // 取 uploads/ 之后的部分: templates/2026/04/13/xxx.docx
            relativePath = absFilePath.substring(uploadsIdx + "/uploads/".length());
        } else {
            // 兜底：用文件名
            relativePath = "templates/" + template.getFileName();
        }

        String fileUrl = onlyOfficeProps.getCallbackHost() + "/uploads/" + relativePath;
        String callbackUrl = onlyOfficeProps.getCallbackHost() + "/api/templates/onlyoffice/callback?id=" + id;

        // 文档key：ID + 文件修改时间，确保文件变化后key不同
        long lastModified = 0;
        try { lastModified = Files.getLastModifiedTime(Paths.get(template.getFilePath())).toMillis(); } catch (Exception ignored) {}
        String docKey = id + "_" + lastModified;

        // 构建配置
        Map<String, Object> document = new LinkedHashMap<>();
        document.put("fileType", fileExt);
        document.put("key", docKey);
        document.put("title", fileName);
        document.put("url", fileUrl);

        Map<String, Object> editorConfig = new LinkedHashMap<>();
        if ("edit".equals(mode)) {
            editorConfig.put("callbackUrl", callbackUrl);
            editorConfig.put("mode", "edit");
        } else {
            editorConfig.put("mode", "view");
        }
        editorConfig.put("lang", "zh");

        Map<String, Object> config = new LinkedHashMap<>();
        config.put("document", document);
        config.put("documentType", getDocumentType(fileExt));
        config.put("editorConfig", editorConfig);

        // JWT签名
        if (StringUtils.hasText(onlyOfficeProps.getJwtSecret())) {
            String token = Jwts.builder()
                    .setClaims(config)
                    .signWith(SignatureAlgorithm.HS256, onlyOfficeProps.getJwtSecret())
                    .compact();
            config.put("token", token);
        }

        // 返回前端需要的完整信息
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("config", config);
        result.put("documentServerUrl", onlyOfficeProps.getDocumentServer());

        return Result.success(result);
    }

    /**
     * ONLYOFFICE 文档保存回调
     * 由ONLYOFFICE Document Server直接调用，通过 JWT secret 验证来源合法性
     */
    @PostMapping("/callback")
    public Map<String, Object> callback(
            @RequestParam("id") Long id,
            @RequestBody Map<String, Object> body,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            HttpServletResponse response) {

        if (StringUtils.hasText(onlyOfficeProps.getJwtSecret())) {
            String token = (String) body.get("token");
            if (!StringUtils.hasText(token) && StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
            }
            if (!StringUtils.hasText(token)) {
                log.warn("ONLYOFFICE callback rejected: missing token, templateId={}", id);
                Map<String, Object> err = new HashMap<>();
                err.put("error", 1);
                return err;
            }
            try {
                Jwts.parser().setSigningKey(onlyOfficeProps.getJwtSecret()).parseClaimsJws(token);
            } catch (Exception e) {
                log.warn("ONLYOFFICE callback rejected: invalid token, templateId={}", id);
                Map<String, Object> err = new HashMap<>();
                err.put("error", 1);
                return err;
            }
        }

        int status = (int) body.getOrDefault("status", 0);
        log.info("ONLYOFFICE callback: templateId={}, status={}", id, status);

        // status 2=文档已保存(所有人关闭), 6=强制保存
        if (status == 2 || status == 6) {
            String downloadUrl = (String) body.get("url");
            if (StringUtils.hasText(downloadUrl)) {
                try {
                    downloadAndReplace(id, downloadUrl);
                    log.info("Template {} file updated from ONLYOFFICE", id);
                } catch (Exception e) {
                    log.error("Failed to save ONLYOFFICE callback file: {}", e.getMessage(), e);
                }
            }
        }

        // ONLYOFFICE 要求返回 {"error": 0}
        Map<String, Object> result = new HashMap<>();
        result.put("error", 0);
        return result;
    }

    /**
     * 从ONLYOFFICE下载编辑后的文件并替换原文件
     */
    private void downloadAndReplace(Long id, String downloadUrl) throws Exception {
        Template template = templateService.getById(id);
        Path filePath = Paths.get(template.getFilePath());

        URL url = new URL(downloadUrl);
        String host = url.getHost();
        if ("localhost".equalsIgnoreCase(host) || "127.0.0.1".equals(host)
                || host.startsWith("10.") || host.startsWith("172.") || host.startsWith("169.254.")) {
            if (!StringUtils.hasText(onlyOfficeProps.getDocumentServer())
                    || !downloadUrl.startsWith(onlyOfficeProps.getDocumentServer())) {
                throw new BusinessException("不允许下载内网地址文件");
            }
        }
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(30000);

        try (InputStream is = conn.getInputStream()) {
            Files.copy(is, filePath, StandardCopyOption.REPLACE_EXISTING);
        }

        // 更新文件大小
        long newSize = Files.size(filePath);
        Template update = new Template();
        update.setId(id);
        update.setFileSize(newSize);
        templateService.update(update);
    }

    private String getDocumentType(String ext) {
        switch (ext) {
            case "doc": case "docx": case "odt": case "rtf": case "txt": return "word";
            case "xls": case "xlsx": case "ods": case "csv": return "cell";
            case "ppt": case "pptx": case "odp": return "slide";
            case "pdf": return "word";
            default: return "word";
        }
    }
}
