package com.founder.ark.ids.util;

/**
 * Created by cheng.ly on 2018/4/9.
 */

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface NotCheckToken {
}
