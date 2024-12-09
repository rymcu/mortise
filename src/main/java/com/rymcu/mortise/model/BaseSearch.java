package com.rymcu.mortise.model;

import lombok.Getter;
import lombok.Setter;

/**
 * Created on 2024/4/19 8:45.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.model
 */
@Getter
@Setter
public class BaseSearch {

    private String query;

    private String sort;

    private String order;

    private Integer pageNum = 1;

    private Integer pageSize = 10;

    private String startDate;

    private String endDate;

    private Integer status;

}
