package com.rymcu.mortise.system.model;


import lombok.Data;

/**
 * @author ronger
 */
@Data
public class TokenUser {

    private Long id;

    private String account;

    private String token;

    private String refreshToken;

}
