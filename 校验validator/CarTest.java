package com.grd.example.validate;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.validation.*;
import java.util.Iterator;
import java.util.Set;

public class CarTest {
    // Validator对象是线程安全的
    private static Validator validator;

    @Before
    public void setUp(){
        // 获取validator实例方法
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    // 校验对象
    public void manufacturerIsNull(){
        Car car = new Car(null,"京GU1122",4);
        Set<ConstraintViolation<Object>> constraintValidators = validator.validate(car);
        printError(constraintValidators);
    }

    @Test
    // 校验属性
    public void validateProperties(){
        Car car = new Car(null,"京GU1122",4);
        Set<ConstraintViolation<Object>> constraintValidators = validator.validateProperty(car,"manufacturer");
        printError(constraintValidators);
    }

    @Test
    // 校验自定义的注解
    public void validateOther(){
        Car car = new Car("fot","京Gu1122",4);
        Set<ConstraintViolation<Object>> constraintValidators = validator.validate(car);
        printError(constraintValidators);
    }

    public void printError(Set<ConstraintViolation<Object>> constraintViolations){
        Iterator<ConstraintViolation<Object>> iter = constraintViolations.iterator();
        while (iter.hasNext()){
            ConstraintViolation constraintViolation = iter.next();
            System.out.println(constraintViolation.getPropertyPath()+" 属性的值 "+constraintViolation.getInvalidValue()+" 报错信息为:"+constraintViolation.getMessage());
        }
    }
}
