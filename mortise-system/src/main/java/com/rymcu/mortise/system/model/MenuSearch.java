package com.rymcu.mortise.system.model;

import com.rymcu.mortise.common.model.BaseSearch;
import lombok.Getter;
import lombok.Setter;

/**
 * Created on 2024/4/30 15:21.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.model
 */
@Getter
@Setter
public class MenuSearch extends BaseSearch {

    private Long parentId;

    private String label;

    private boolean showButton;

}
