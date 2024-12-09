package com.rymcu.mortise.model;

import lombok.Data;

import java.util.Set;

/**
 * Created on 2024/4/17 9:08.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.model
 */
@Data
public class Tooltip {

    private String text;

    private Set<String> shortcuts;

}
