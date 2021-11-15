package cz.muni.ics.oidc.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class ExecutionTimeLoggingAspect {

    @Around("@annotation(LogTimes) && execution(* cz.muni.ics.oidc.server.connectors..* (..))")
    public Object logExecutionTimeForConnectorsWithParams(ProceedingJoinPoint pjp) throws Throwable {
        return LoggingUtils.logExecutionTimes(log, pjp);
    }

}
