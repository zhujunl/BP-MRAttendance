package com.miaxis.aspect;

/**
 * @author Tank
 * @date 2021/6/15 12:23 上午
 * @des
 * @updateAuthor
 * @updateDes
 */
//@Aspect
public class AspectMxCamera {

    final String TAG = AspectMxCamera.class.getSimpleName();

//     @Before("execution(* *..MXCamera+.open(..))")
//     public void before(JoinPoint joinPoint) throws Throwable {
//            MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
//            String className = joinPoint.getThis().getClass().getSimpleName();
//            Timbere(TAG, "before class:" + className);
//            Timbere(TAG, "before method:" + methodSignature.getName());
//            int zzCamControl = mr990Driver.zzCamControl(1);
//            Timbere(TAG, "before zzCamControl:" + zzCamControl);
//            SystemClock.sleep(600);
//    }


    //    @Around("execution(* *..MainActivity+.test**(..))")
    //    public void around(JoinPoint joinPoint) throws Throwable {
    //        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
    //        String className = joinPoint.getThis().getClass().getSimpleName();
    //
    //        Timber.e(TAG, "around class:" + className);
    //        Timber.e(TAG, "around method:" + methodSignature.getName());
    //
    ////        Object[] args = joinPoint.getArgs();
    ////
    ////        Timber.e(TAG, "around args:" + Arrays.toString(args));
    //    }

    //    @After("execution(* *..MainActivity+.on**(..))")
    //    public void after(JoinPoint joinPoint) throws Throwable {
    //        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
    //        String className = joinPoint.getThis().getClass().getSimpleName();
    //        Timber.e(TAG, "After class:" + className);
    //        Timber.e(TAG, "After method:" + methodSignature.getName());
    //    }

    //    @AfterReturning("execution(* *..MainActivity+.on**(..))")
    //    public void afterReturning(JoinPoint joinPoint) throws Throwable {
    //        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
    //        String className = joinPoint.getThis().getClass().getSimpleName();
    //
    //
    //        Timber.e(TAG, "AfterReturning class:" + className);
    //        Timber.e(TAG, "AfterReturning method:" + methodSignature.getName());
    //
    ////        Class returnType = methodSignature.getReturnType();
    ////        Timber.e(TAG, "AfterReturning returnType:" + returnType);
    //
    //    }
    //
    //    @AfterThrowing("execution(* *..MainActivity+.on**(..))")
    //    public void afterThrowing(JoinPoint joinPoint) throws Throwable {
    //        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
    //        String className = joinPoint.getThis().getClass().getSimpleName();
    //
    //        Timber.e(TAG, "AfterThrowing class:" + className);
    //        Timber.e(TAG, "AfterThrowing method:" + methodSignature.getName());
    //    }

}
