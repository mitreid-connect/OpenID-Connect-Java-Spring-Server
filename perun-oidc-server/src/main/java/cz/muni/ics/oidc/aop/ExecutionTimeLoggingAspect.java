package cz.muni.ics.oidc.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ExecutionTimeLoggingAspect {

    public static final Logger log = LoggerFactory.getLogger(ExecutionTimeLoggingAspect.class);

    @Around("@annotation(LogTimes) && execution(* cz.muni.ics.oidc.server.connectors..* (..))")
    public Object logExecutionTimeForConnectorsWithParams(ProceedingJoinPoint pjp) throws Throwable {
        return LoggingUtils.logExecutionTimes(log, pjp);
    }

}
