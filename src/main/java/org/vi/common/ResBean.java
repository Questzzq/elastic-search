package org.vi.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Eric Tseng
 * @description ResBean
 * @since 2022/2/19 15:53
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResBean {
    private long code;
    private String message;
    private Object data;

    /**
     * 成功返回结果
     * @return
     */
    public static ResBean success() {
        return new ResBean(ResBeanEnum.SUCCESS.getCode(),
                ResBeanEnum.SUCCESS.getMessage(), null);
    }

    public static ResBean success(Object data) {
        return new ResBean(ResBeanEnum.SUCCESS.getCode(),
                ResBeanEnum.SUCCESS.getMessage(), data);
    }

    public static ResBean error(ResBeanEnum resBeanEnum) {
        return new ResBean(resBeanEnum.getCode(),
                resBeanEnum.getMessage(), null);
    }

    public static ResBean error(ResBeanEnum resBeanEnum, Object data) {
        return new ResBean(resBeanEnum.getCode(),
                resBeanEnum.getMessage(), data);
    }

}
