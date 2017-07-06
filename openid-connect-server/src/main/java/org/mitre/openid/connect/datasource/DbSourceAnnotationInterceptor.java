package org.mitre.openid.connect.datasource;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;


/**
 * Created by Vishwanathan.D on 6/7/17.
 */
@Aspect
@Component
public class DbSourceAnnotationInterceptor implements Ordered {

    private static final Logger log = LoggerFactory.getLogger(DbSourceAnnotationInterceptor.class);

    private int order;

    @Value("20")
    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public int getOrder() {
        return order;
    }

    @Pointcut(value="execution(public * *(..))")
    public void anyPublicMethod() { }


    @Around("anyPublicMethod() && @annotation(dbSource)")
    public Object processByMethod(ProceedingJoinPoint pjp, DbSource dbSource) throws Throwable {
       return processAnnotation(pjp, dbSource);
    }

    @Around("anyPublicMethod() && @within(dbSource)")
    public Object processByClass(ProceedingJoinPoint pjp, DbSource dbSource) throws Throwable {
       return processAnnotation(pjp, dbSource);
    }

    private Object processAnnotation(ProceedingJoinPoint pjp, DbSource dbSource) throws Throwable {
        try {
            log.debug("Setting db source to {} ", dbSource.value());
            DbContextHolder.setDbType(dbSource.value());
            Object result = pjp.proceed();
            DbContextHolder.clearDbType();
            return result;
        } finally {
            // restore state
            DbContextHolder.clearDbType();
        }
    }


}
