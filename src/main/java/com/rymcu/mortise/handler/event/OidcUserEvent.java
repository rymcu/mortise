package com.rymcu.mortise.handler.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

/**
 * Created on 2025/2/25 11:00.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.handler.event
 */
@Data
@AllArgsConstructor
public class OidcUserEvent {

    private OidcUser user;

}
