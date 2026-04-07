package com.rymcu.mortise.system.controller.facade;

import com.mybatisflex.core.paginate.Page;
import com.rymcu.mortise.core.model.PageResult;
import com.rymcu.mortise.core.result.GlobalResult;
import com.rymcu.mortise.system.controller.vo.FileDetailVO;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.dromara.x.file.storage.core.FileInfo;
import org.springframework.web.multipart.MultipartFile;

public interface SystemFileAdminFacade {

    GlobalResult<PageResult<FileDetailVO>> listFiles(@Min(1) int pageNumber, @Min(1) int pageSize, String keyword);

    GlobalResult<Boolean> deleteFile(Long id);

    GlobalResult<FileInfo> upload(@NotNull MultipartFile file);
}
