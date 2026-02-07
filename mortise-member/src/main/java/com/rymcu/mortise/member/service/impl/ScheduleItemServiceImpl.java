package com.rymcu.mortise.member.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.rymcu.mortise.member.entity.ScheduleItem;
import com.rymcu.mortise.member.mapper.ScheduleItemMapper;
import com.rymcu.mortise.member.service.ScheduleItemService;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * Created on 2026/1/18 23:52.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.member.service.impl
 */
@Service
@Primary
public class ScheduleItemServiceImpl extends ServiceImpl<ScheduleItemMapper, ScheduleItem> implements ScheduleItemService {
}
