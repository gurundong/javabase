package com.grd.example.validate;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * 验证器
 */
public class CheckCaseValidator implements ConstraintValidator<CheckCase,String> {
    private CaseMode caseMode;

    @Override
    public void initialize(CheckCase constraintAnnotation) {
        this.caseMode = constraintAnnotation.value();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if(value == null){
            return true;
        }
        boolean isValid;
        if(CaseMode.UPPER == caseMode){
            isValid =  value.equals(value.toUpperCase());
        }
        else{
            isValid =  value.equals(value.toLowerCase());
        }
        if(!isValid){
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("{com.example.CheckCase.message}").addConstraintViolation();
        }
        return isValid;
    }
}
