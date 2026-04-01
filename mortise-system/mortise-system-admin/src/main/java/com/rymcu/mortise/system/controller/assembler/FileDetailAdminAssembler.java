package com.rymcu.mortise.system.controller.assembler;

import com.rymcu.mortise.file.entity.FileDetail;
import com.rymcu.mortise.system.controller.vo.FileDetailVO;

public final class FileDetailAdminAssembler {

    private FileDetailAdminAssembler() {
    }

    public static FileDetailVO toVO(FileDetail detail) {
        if (detail == null) {
            return null;
        }
        FileDetailVO vo = new FileDetailVO();
        vo.setId(detail.getId());
        vo.setUrl(detail.getUrl());
        vo.setSize(detail.getSize());
        vo.setFilename(detail.getFilename());
        vo.setOriginalFilename(detail.getOriginalFilename());
        vo.setBasePath(detail.getBasePath());
        vo.setPath(detail.getPath());
        vo.setExt(detail.getExt());
        vo.setContentType(detail.getContentType());
        vo.setPlatform(detail.getPlatform());
        vo.setThUrl(detail.getThUrl());
        vo.setThFilename(detail.getThFilename());
        vo.setThSize(detail.getThSize());
        vo.setThContentType(detail.getThContentType());
        vo.setObjectId(detail.getObjectId());
        vo.setObjectType(detail.getObjectType());
        vo.setMetadata(detail.getMetadata());
        vo.setUserMetadata(detail.getUserMetadata());
        vo.setThMetadata(detail.getThMetadata());
        vo.setThUserMetadata(detail.getThUserMetadata());
        vo.setAttr(detail.getAttr());
        vo.setFileAcl(detail.getFileAcl());
        vo.setThFileAcl(detail.getThFileAcl());
        vo.setHashInfo(detail.getHashInfo());
        vo.setUploadId(detail.getUploadId());
        vo.setUploadStatus(detail.getUploadStatus());
        vo.setCreateTime(detail.getCreateTime());
        return vo;
    }
}
