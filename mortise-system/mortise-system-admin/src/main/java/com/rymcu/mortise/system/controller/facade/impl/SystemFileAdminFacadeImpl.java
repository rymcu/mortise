package com.rymcu.mortise.system.controller.facade.impl;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.query.QueryWrapper;
import com.rymcu.mortise.core.result.GlobalResult;
import com.rymcu.mortise.file.entity.FileDetail;
import com.rymcu.mortise.file.mapper.FileDetailMapper;
import com.rymcu.mortise.file.service.FileDetailService;
import com.rymcu.mortise.system.controller.assembler.FileDetailAdminAssembler;
import com.rymcu.mortise.system.controller.facade.SystemFileAdminFacade;
import com.rymcu.mortise.system.controller.vo.FileDetailVO;
import lombok.extern.slf4j.Slf4j;
import org.dromara.x.file.storage.core.FileInfo;
import org.dromara.x.file.storage.core.FileStorageService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Component
public class SystemFileAdminFacadeImpl implements SystemFileAdminFacade {

    private final FileStorageService fileStorageService;
    private final FileDetailMapper fileDetailMapper;
    private final FileDetailService fileDetailService;

    public SystemFileAdminFacadeImpl(FileStorageService fileStorageService,
                                     FileDetailMapper fileDetailMapper,
                                     FileDetailService fileDetailService) {
        this.fileStorageService = fileStorageService;
        this.fileDetailMapper = fileDetailMapper;
        this.fileDetailService = fileDetailService;
    }

    @Override
    public GlobalResult<Page<FileDetailVO>> listFiles(int pageNumber, int pageSize, String keyword) {
        QueryWrapper queryWrapper = QueryWrapper.create();
        if (StringUtils.hasText(keyword)) {
            QueryColumn colOriginalFilename = new QueryColumn("original_filename");
            queryWrapper.and(colOriginalFilename.like(keyword));
        }
        queryWrapper.orderBy(new QueryColumn(FileDetail.COL_ID), false);
        Page<FileDetail> page = fileDetailMapper.paginate(new Page<>(pageNumber, pageSize), queryWrapper);

        Page<FileDetailVO> voPage = new Page<>(page.getPageNumber(), page.getPageSize());
        voPage.setTotalRow(page.getTotalRow());
        voPage.setRecords(page.getRecords().stream()
                .map(FileDetailAdminAssembler::toVO)
                .toList());
        return GlobalResult.success(voPage);
    }

    @Override
    public GlobalResult<Boolean> deleteFile(Long id) {
        FileDetail detail = fileDetailMapper.selectOneById(id);
        if (detail == null) {
            return GlobalResult.error("文件不存在");
        }
        try {
            FileInfo fileInfo = fileDetailService.getByUrl(detail.getUrl());
            if (fileInfo != null) {
                fileStorageService.delete(fileInfo);
            } else {
                fileDetailMapper.deleteById(id);
            }
            return GlobalResult.success(true);
        } catch (Exception e) {
            log.error("删除文件失败, id={}", id, e);
            return GlobalResult.error("删除文件失败: " + e.getMessage());
        }
    }

    @Override
    public GlobalResult<FileInfo> upload(MultipartFile file) {
        if (file.isEmpty()) {
            return GlobalResult.error("文件不能为空");
        }
        try {
            FileInfo fileInfo = fileStorageService.of(file).upload();
            return GlobalResult.success(fileInfo);
        } catch (Exception e) {
            log.error("文件上传失败", e);
            return GlobalResult.error("文件上传失败: " + e.getMessage());
        }
    }
}
