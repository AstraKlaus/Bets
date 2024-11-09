package ak.spring.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class LoggingAspect {

    @Around("execution(* ak.spring..*(..))")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        log.info("Входной метод: {}", joinPoint.getSignature());
        try {
            Object result = joinPoint.proceed();
            log.info("Выходной метод: {}", joinPoint.getSignature());
            return result;
        } catch (Throwable throwable) {
            log.error("Ошибка в методе: {}", joinPoint.getSignature(), throwable);
            throw throwable;
        }
    }
}

