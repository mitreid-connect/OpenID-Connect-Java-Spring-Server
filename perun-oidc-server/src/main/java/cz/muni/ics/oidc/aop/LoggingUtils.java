package cz.muni.ics.oidc.aop;

import java.sql.Timestamp;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.Logger;

/**
 * Utility class that takes care of the logging for AOP.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
public class LoggingUtils {

    /**
     * Log at TRACE level end of method.
     * @param log Logger object.
     * @param jp Join point.
     * @return Value returned by the methods.
     */
    public static Object logExecutionEnd(Logger log, JoinPoint jp, Object result) {
        String className = jp.getTarget().getClass().getName();
        String methodName = jp.getSignature().getName();
        Object[] args = jp.getArgs();
        log.trace("{}.{}({}) returns: {}", className, methodName, args.length > 0 ? args : "", result);
        return result;
    }

    /**
     * Log at TRACE level end of method.
     * @param log Logger object.
     * @param jp Join point.
     * @throws Throwable thrown exception by the method execution.
     */
    public static void logExecutionException(Logger log, JoinPoint jp, Throwable t) throws Throwable {
        String className = jp.getTarget().getClass().getName();
        String methodName = jp.getSignature().getName();
        Object[] args = jp.getArgs();
        log.warn("{}.{}({}) has thrown {}", className, methodName, args.length > 0 ? args : "", t.getClass(), t);
        throw t;
    }

    /**
     * Log at TRACE level times of start and end of method execution.
     * @param log Logger object.
     * @param pjp proceeding join point.
     * @return Value returned by the methods.
     * @throws Throwable throw exception by the method execution.
     */
    public static Object logExecutionTimes(Logger log, ProceedingJoinPoint pjp) throws Throwable {
        String className = pjp.getTarget().getClass().getName();
        String methodName = pjp.getSignature().getName();
        Object[] args = pjp.getArgs();
        long start = System.currentTimeMillis();

        log.trace("Execution of {}.{}({}) started at {}",
                className, methodName, args.length > 0 ? args : "", new Timestamp(start));
        try {
            Object result = pjp.proceed();
            long finish = System.currentTimeMillis();
            log.trace("Execution of {}.{}({}) finished successfully at {}, execution took {}ms",
                    className, methodName, args.length > 0 ? args : "", new Timestamp(finish), finish - start);
            return result;
        } catch (Throwable e) {
            long finish = System.currentTimeMillis();
            log.trace("Execution of {}.{}({}) finished by exception being thrown at {}, execution took {}ms",
                    className, methodName, args.length > 0 ? args : "", new Timestamp(finish), finish - start);
            throw e;
        }
    }

}
