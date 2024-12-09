package com.rymcu.mortise.model;

import lombok.Getter;
import lombok.Setter;

/**
 * Created on 2024/4/19 8:46.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.model
 */
@Getter
@Setter
public class UserSearch extends BaseSearch {

    private String email;

    private String account;

}
