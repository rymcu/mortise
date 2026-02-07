package com.rymcu.mortise.member.service.impl;

import com.mybatisflex.core.util.UpdateEntity;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.rymcu.mortise.common.exception.ServiceException;
import com.rymcu.mortise.member.entity.*;
import com.rymcu.mortise.member.enums.TargetType;
import com.rymcu.mortise.member.mapper.ProductMapper;
import com.rymcu.mortise.member.model.*;
import com.rymcu.mortise.member.service.*;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 商品基础服务实现
 * <p>
 * 仅提供 MyBatis-Flex 的基础 CRUD 实现，不包含业务逻辑
 * <p>
 * 业务模块（edu、api）应继承此类，扩展自己的业务方法
 *
 * @author ronger
 */
@Service
@Primary
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    @Resource
    private ProductSkuService productSkuService;
    @Resource
    private ProductSkuTargetService productSkuTargetService;
    @Resource
    private CourseScheduleService courseScheduleService;
    @Resource
    private CourseService courseService;
    @Resource
    private CourseModuleService courseModuleService;
    @Resource
    private OndemandLessonService ondemandLessonService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean incrementViewCount(Long id) {
        Product product = getById(id);
        if (product == null) {
            throw new ServiceException("产品不存在");
        }
        Product updateProduct = UpdateEntity.of(Product.class, id);
        updateProduct.setViewCount((product.getViewCount() != null ? product.getViewCount() : 0) + 1);
        return updateById(updateProduct);
    }

    @Override
    public ProductCourseOutlineInfo getProductCourseOutline(Long productId) {
        // 获取产品信息
        Product product = getById(productId);
        if (product == null) {
            throw new ServiceException("产品不存在");
        }

        // 获取产品所有SKU
        List<ProductSku> skus = productSkuService.findByProductId(productId);
        if (skus == null || skus.isEmpty()) {
            throw new ServiceException("该产品暂未配置SKU");
        }

        // 获取所有SKU的ID列表
        List<Long> skuIds = skus.stream()
                .map(ProductSku::getId)
                .collect(Collectors.toList());

        return buildCourseOutline(product, skuIds);
    }

    @Override
    public ProductCourseOutlineInfo getProductCourseOutlineBySku(Long productId, Long skuId) {
        // 获取产品信息
        Product product = getById(productId);
        if (product == null) {
            throw new ServiceException("产品不存在");
        }

        // 验证SKU属于该产品
        ProductSku sku = productSkuService.getById(skuId);
        if (sku == null || !sku.getProductId().equals(productId)) {
            throw new ServiceException("SKU不存在或不属于该产品");
        }

        return buildCourseOutline(product, Collections.singletonList(skuId));
    }

    /**
     * 构建课程大纲
     *
     * @param product 产品信息
     * @param skuIds SKU ID列表
     * @return 课程大纲
     */
    private ProductCourseOutlineInfo buildCourseOutline(Product product, List<Long> skuIds) {
        ProductCourseOutlineInfo outline = new ProductCourseOutlineInfo();
        outline.setProductId(product.getId());
        outline.setProductTitle(product.getTitle());

        // 获取所有SKU关联的目标
        List<ProductSkuTarget> targets = productSkuTargetService.findBySkuIds(skuIds);
        if (targets == null || targets.isEmpty()) {
            outline.setCourses(Collections.emptyList());
            return outline;
        }

        // 按目标类型分组
        Map<String, List<ProductSkuTarget>> targetsByType = targets.stream()
                .collect(Collectors.groupingBy(ProductSkuTarget::getTargetType));

        // 收集所有课程ID

        // 处理直接关联的课程
        List<ProductSkuTarget> courseTargets = targetsByType.getOrDefault(TargetType.COURSE.getCode(), Collections.emptyList());
        Map<Long, ProductSkuTarget> courseTargetMap = courseTargets.stream()
                .collect(Collectors.toMap(ProductSkuTarget::getTargetId, t -> t, (a, b) -> a));
        Set<Long> courseIds = new HashSet<>(courseTargetMap.keySet());

        // 处理通过排期关联的课程
        List<ProductSkuTarget> scheduleTargets = targetsByType.getOrDefault(TargetType.COURSE_SCHEDULE.getCode(), Collections.emptyList());
        Map<Long, Long> courseToScheduleMap = new HashMap<>();

        if (!scheduleTargets.isEmpty()) {
            List<Long> scheduleIds = scheduleTargets.stream()
                    .map(ProductSkuTarget::getTargetId)
                    .collect(Collectors.toList());

            // 查询排期信息
            List<CourseSchedule> schedules = courseScheduleService.listByIds(scheduleIds);
            for (CourseSchedule schedule : schedules) {
                courseIds.add(schedule.getCourseId());
                // 使用 putIfAbsent 只保留第一个遇到的排期
                courseToScheduleMap.putIfAbsent(schedule.getCourseId(), schedule.getId());
            }
        }

        // 查询所有课程
        if (courseIds.isEmpty()) {
            outline.setCourses(Collections.emptyList());
            return outline;
        }

        List<Course> courses = courseService.listByIds(new ArrayList<>(courseIds));
        List<CourseOutlineInfo> courseOutlines = new ArrayList<>();

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (Course course : courses) {
            CourseOutlineInfo courseInfo = new CourseOutlineInfo();
            courseInfo.setCourseId(course.getId());
            courseInfo.setCourseTitle(course.getTitle());
            courseInfo.setCourseType(course.getType());
            courseInfo.setDescription(course.getDescription());
            courseInfo.setDifficultyLevel(course.getDifficultyLevel());

            // 判断关联来源
            if (courseTargetMap.containsKey(course.getId())) {
                courseInfo.setSourceType(TargetType.COURSE.getCode());
                courseInfo.setSourceId(course.getId());
            } else {
                // 找到对应的排期
                Long scheduleId = courseToScheduleMap.get(course.getId());

                if (scheduleId != null) {
                    courseInfo.setSourceType(TargetType.COURSE_SCHEDULE.getCode());
                    courseInfo.setSourceId(scheduleId);

                    // 添加排期信息
                    CourseSchedule schedule = courseScheduleService.getById(scheduleId);
                    if (schedule != null) {
                        ScheduleInfo scheduleInfo = new ScheduleInfo();
                        scheduleInfo.setScheduleId(schedule.getId());
                        scheduleInfo.setName(schedule.getName());
                        scheduleInfo.setStartDate(schedule.getStartDate() != null ? schedule.getStartDate().format(dateFormatter) : null);
                        scheduleInfo.setEndDate(schedule.getEndDate() != null ? schedule.getEndDate().format(dateFormatter) : null);
                        scheduleInfo.setCurrentStudents(schedule.getCurrentStudents());
                        scheduleInfo.setMaxStudents(schedule.getMaxStudents());
                        scheduleInfo.setStatus(schedule.getStatus());
                        courseInfo.setScheduleInfo(scheduleInfo);
                    }
                }
            }

            // 获取课程模块和课时
            List<CourseModule> modules = courseModuleService.findModulesByCourseId(course.getId());
            if (modules != null && !modules.isEmpty()) {
                courseInfo.setTotalModules(modules.size());

                List<ModuleOutlineInfo> moduleOutlines = new ArrayList<>();
                int totalLessons = 0;
                int totalDuration = 0;

                for (CourseModule module : modules) {
                    ModuleOutlineInfo moduleInfo = new ModuleOutlineInfo();
                    moduleInfo.setModuleId(module.getId());
                    moduleInfo.setTitle(module.getTitle());
                    moduleInfo.setDescription(module.getDescription());
                    moduleInfo.setSortNo(module.getSortNo());
                    moduleInfo.setIsFree(module.getIsFree());

                    // 获取模块下的课时
                    List<OndemandLesson> lessons = ondemandLessonService.findLessonsByModuleId(module.getId());
                    if (lessons != null && !lessons.isEmpty()) {
                        moduleInfo.setLessonCount(lessons.size());
                        totalLessons += lessons.size();

                        List<LessonOutlineInfo> lessonOutlines = new ArrayList<>();
                        int moduleDuration = 0;

                        for (OndemandLesson lesson : lessons) {
                            LessonOutlineInfo lessonInfo = getLessonOutlineInfo(lesson);

                            if (lesson.getDurationSeconds() != null) {
                                moduleDuration += lesson.getDurationSeconds();
                            }

                            lessonOutlines.add(lessonInfo);
                        }

                        moduleInfo.setLessons(lessonOutlines);
                        moduleInfo.setDurationMinutes(moduleDuration / 60);
                        totalDuration += moduleDuration;
                    } else {
                        moduleInfo.setLessonCount(0);
                        moduleInfo.setLessons(Collections.emptyList());
                    }

                    moduleOutlines.add(moduleInfo);
                }

                courseInfo.setModules(moduleOutlines);
                courseInfo.setTotalLessons(totalLessons);
                courseInfo.setTotalDurationMinutes(totalDuration / 60);
            } else {
                courseInfo.setTotalModules(0);
                courseInfo.setTotalLessons(0);
                courseInfo.setTotalDurationMinutes(0);
                courseInfo.setModules(Collections.emptyList());
            }

            courseOutlines.add(courseInfo);
        }

        outline.setCourses(courseOutlines);
        return outline;
    }

    private static LessonOutlineInfo getLessonOutlineInfo(OndemandLesson lesson) {
        LessonOutlineInfo lessonInfo = new LessonOutlineInfo();
        lessonInfo.setLessonId(lesson.getId());
        lessonInfo.setTitle(lesson.getTitle());
        lessonInfo.setContentType(lesson.getContentType());
        lessonInfo.setDurationSeconds(lesson.getDurationSeconds());
        lessonInfo.setSortNo(lesson.getSortNo());
        lessonInfo.setIsFree(lesson.getIsFree());
        lessonInfo.setReleaseStrategy(lesson.getReleaseStrategy());
        return lessonInfo;
    }

}
