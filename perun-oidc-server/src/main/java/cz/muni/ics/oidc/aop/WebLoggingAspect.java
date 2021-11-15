package cz.muni.ics.oidc.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class WebLoggingAspect {

    @AfterReturning(value = "execution(* cz.muni.ics.oidc.web..* (..))", returning = "result")
    public Object logAroundMethodWithParams(JoinPoint jp, Object result) {
        return LoggingUtils.logExecutionEnd(log, jp, result);
    }

    @AfterThrowing(value = "execution(* cz.muni.ics.oidc.web..* (..))", throwing = "t")
    public void logAroundMethodWithParams(JoinPoint jp, Throwable t) throws Throwable {
        LoggingUtils.logExecutionException(log, jp, t);
    }

}
