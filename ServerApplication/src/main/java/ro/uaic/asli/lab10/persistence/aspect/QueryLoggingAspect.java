package ro.uaic.asli.lab10.persistence.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Logs execution time and failures for Spring Data repository calls (JPQL / derived queries).
 */
@Aspect
@Component
@Order(10)
public class QueryLoggingAspect {

    private static final Logger QUERY_LOG = LoggerFactory.getLogger("lab11.jpql");

    @Around("execution(* ro.uaic.asli.lab10.persistence.repository..*(..))")
    public Object aroundRepository(ProceedingJoinPoint pjp) throws Throwable {
        long startNs = System.nanoTime();
        try {
            Object result = pjp.proceed();
            long ms = (System.nanoTime() - startNs) / 1_000_000L;
            QUERY_LOG.info(
                    "repository OK | {} ms | {} | args={}",
                    ms,
                    pjp.getSignature().toShortString(),
                    safeArgs(pjp.getArgs())
            );
            return result;
        } catch (Throwable ex) {
            long ms = (System.nanoTime() - startNs) / 1_000_000L;
            QUERY_LOG.error(
                    "repository FAIL | {} ms | {} | args={} | message={}",
                    ms,
                    pjp.getSignature().toShortString(),
                    safeArgs(pjp.getArgs()),
                    ex.getMessage(),
                    ex
            );
            throw ex;
        }
    }

    private static String safeArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return "[]";
        }
        return "[" + Arrays.stream(args)
                .map(a -> a == null ? "null" : String.valueOf(a))
                .collect(Collectors.joining(", ")) + "]";
    }
}
