package com.rymcu.mortise.auth.model;

import com.rymcu.mortise.common.model.BaseSearch;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Created on 2025/10/7 16:19.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.auth.model
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class OAuth2ClientConfigSearch extends BaseSearch {

    private String clientId;

    private String registrationId;

    private String provider;

}
