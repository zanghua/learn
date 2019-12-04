package ch1;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

/**
 * @author zangzh
 * @date 2019/11/10 16:27
 */
@Component
@Aspect
public class LogAspect {

    @Pointcut("@annotation(ch1.Action)")
    public void annotationPointCut() {}

    @After("annotationPointCut()")
    public void after(JoinPoint joinPoint) {
        System.out.println("--------after------------");
    }

    @Before("execution(* ch1.FunctionTest2Service.*(..))")
    public void before(JoinPoint joinPoint) {
        System.out.println("--------before------------");
    }

    @Around("execution(* ch1.FunctionTest2Service.print2())")
    public void arount(ProceedingJoinPoint joinPoint) throws Throwable {
        System.out.println("===arount start====");
        try {
            Object object = joinPoint.proceed();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            throw throwable;
        }
        System.out.println("===arount end====");
    }
}
