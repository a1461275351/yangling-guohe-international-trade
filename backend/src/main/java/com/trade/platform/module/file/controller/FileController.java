package com.trade.platform.module.file.controller;

import com.trade.platform.common.BusinessException;
import com.trade.platform.common.PageResult;
import com.trade.platform.common.Result;
import com.trade.platform.module.file.dto.FileQueryDTO;
import com.trade.platform.module.file.entity.BizFile;
import com.trade.platform.module.file.service.FileService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

@RestController
@RequestMapping("/api/files")
public class FileController {

    @Resource
    private FileService fileService;

    @Value("${file.upload-path}")
    private String uploadPath;

    @PostMapping("/upload")
    public Result<BizFile> upload(@RequestParam("file") MultipartFile file,
                                  @RequestParam(required = false) String businessType) {
        return Result.success(fileService.upload(file, businessType));
    }

    @PostMapping("/list")
    public Result<PageResult<BizFile>> list(@RequestBody FileQueryDTO dto) {
        return Result.success(fileService.getList(dto));
    }

    @GetMapping("/download/{id}")
    public void download(@PathVariable Long id, HttpServletResponse response) {
        BizFile bizFile = fileService.getById(id);
        if (bizFile == null) {
            throw new BusinessException("文件不存在");
        }
        File file = new File(uploadPath + bizFile.getFilePath());
        if (!file.exists()) {
            throw new BusinessException("文件不存在");
        }
        response.setContentType("application/octet-stream");
        try {
            response.setHeader("Content-Disposition",
                    "attachment;filename=" + URLEncoder.encode(bizFile.getOriginalName(), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            response.setHeader("Content-Disposition",
                    "attachment;filename=" + bizFile.getOriginalName());
        }
        try (InputStream is = new FileInputStream(file);
             OutputStream os = response.getOutputStream()) {
            byte[] buffer = new byte[4096];
            int len;
            while ((len = is.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }
            os.flush();
        } catch (Exception e) {
            throw new BusinessException("文件下载失败");
        }
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        fileService.delete(id);
        return Result.success();
    }

    @DeleteMapping("/batch")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        fileService.batchDelete(ids);
        return Result.success();
    }

    @PutMapping("/{id}/rename")
    public Result<Void> rename(@PathVariable Long id, @RequestParam String name) {
        fileService.rename(id, name);
        return Result.success();
    }
}
