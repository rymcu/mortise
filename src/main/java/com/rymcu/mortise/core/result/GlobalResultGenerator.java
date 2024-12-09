package com.rymcu.mortise.core.result;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created on 2024/4/19 9:15.
 * 全局返回结果生成器
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.core.result
 */
public class GlobalResultGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalResultGenerator.class);

    /**
     * normal
     *
     * @param code   code
     * @param data   data
     * @param message message
     * @param <T>    data
     * @return GlobalResult
     */
    public static <T> GlobalResult<T> genResult(Integer code, T data, String message) {
        GlobalResult<T> result = new GlobalResult<>();
        result.setCode(code);
        result.setData(data);
        result.setMessage(message);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("generate rest result:{}", result);
        }
        return result;
    }

    /**
     * success
     *
     * @param data data
     * @param <T>  data
     * @return GlobalResult
     */
    public static <T> GlobalResult<T> genSuccessResult(T data) {

        return genResult(200, data, null);
    }

    /**
     * error message
     *
     * @param message error message
     * @param <T>     data
     * @return GlobalResult
     */
    public static <T> GlobalResult<T> genErrorResult(String message) {

        return genResult(400, null, message);
    }

    /**
     * error
     *
     * @param error error enum
     * @param <T>   data
     * @return GlobalResult
     */
    public static <T> GlobalResult<T> genErrorResult(ResultCode error) {

        return genResult(error.getCode(), null, error.getMessage());
    }

    /**
     * success no message
     *
     * @return GlobalResult
     */
    public static GlobalResult genSuccessResult() {
        return genSuccessResult(null);
    }

    /**
     * success
     *
     * @param message success message
     * @return GlobalResult
     */
    public static <T> GlobalResult<T> genSuccessResult(String message) {

        return genResult(200, null, message);
    }

}
