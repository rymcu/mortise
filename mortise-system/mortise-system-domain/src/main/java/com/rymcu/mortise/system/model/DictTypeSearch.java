package com.rymcu.mortise.system.model;

import com.rymcu.mortise.common.model.BaseSearch;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Created on 2024/9/22 19:56.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.model
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DictTypeSearch extends BaseSearch {

    private String typeCode;

}
