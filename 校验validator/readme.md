## validation bean
* validation bean是基于JSR-303标准开发出来的，使用注解方式实现，及其方便。是这只是一个接口，没有具体实现
* Hibernate Validator是一个hibernate独立的包，可以直接引用，他实现了validation bean同时有做了扩展，比较强大
* 示意图
![示意图](https://note.youdao.com/yws/api/personal/file/AE493CE107674D64B091C301443AD1AB?method=download&shareKey=01196353b3d1338508cbd28f9bc8ca9e)
* 常用注解辨析：@NotEmpty @NotBlank @NotNull
   * @NotEmpty用于数组与集合
   * @NotBlank用于字符串String对象，不能用于基本数据类型
   * @NotNull用于基本数据类型
* 自动使用hibernate-validator的原理：Java Service Provider实现
   * Java提供的Service Provider机制其实就是一种DI，在实现时只考虑接口（也就是规范），由其他第三方去实现具体的功能。这个跟Spring的依赖注入概念上差不多，Spring是框架级别的依赖注入，SPI则是语言自身提供的，不依赖于任何框架。
   * 使用方法：1. 假设系统中实现一个接口，例如com.test.DemoInterface
   * 2. 在生成的jar包里，在META-INF/services目录下，创建一个UTF-8编码的文件，名称为com.test.DemoInterface，然后内容只需要一行，就是实现类的全路径 (com.test.thirdparty.DemoInterfaceImpl)
   * 3. 系统代码中调用：
```java
ServiceLoader<DemoInterface> di = ServiceLoader.load(DemoInterface.class);
```
   * 4. 如果有多个第三方jar都提供了实现，并且都有META-INF/services的文件,顺序为jar包加载顺序->再按照包字母排序顺序
   * JSR-303标准（接口为javax.validation.spi.ValidationProvider），hibernate实现方式（实现为org.hibernate.validator.HibernateValidator）
![示意图](https://note.youdao.com/yws/api/personal/file/7C4ECCC42B2040D38E181321D4EC6264?method=download&shareKey=aa47bd8d166a8968220ab169259bb69e)
![示意图](https://note.youdao.com/yws/api/personal/file/CDD953F8F3BB4D78830BE4057B128D77?method=download&shareKey=7aaceafa2e218cdfa6b39615a979c11e)
## bean注解校验与获取错误
* 注解校验bean示例：Car.java
* 校验测试：
   * 校验对象-CarTest.manufacturerIsNull
   * 校验某一个属性-CarTest.validateProperties
* 获取验证器：
```java
// 对一个实体对象验证之前首先需要有个Validator对象, 而这个对象是需要通过Validation 类和 ValidatorFactory来创建的. 
// 最简单的方法是调用Validation.buildDefaultValidatorFactory() 这个静态方法.
ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
Validator validator = factory.getValidator();
```
* 校验方法：
```java
// 校验对象    
validator.validate(object);
// 校验属性
validator.validateProperty;
```
* 获取错误：
   * ConstraintViolation为校验失败的对象描述
   * constraintViolation.getPropertyPath()获取校验失败属性
   * constraintViolation.getInvalidValue()获取校验失败属性值
   * constraintViolation.getMessage()获取校验失败属性信息
```java
// 打印错误
public void printError(Set<ConstraintViolation<Object>> constraintViolations){
        Iterator<ConstraintViolation<Object>> iter = constraintViolations.iterator();
        while (iter.hasNext()){
            ConstraintViolation constraintViolation = iter.next();
            System.out.println(constraintViolation.getPropertyPath()+" 属性的值 "+constraintViolation.getInvalidValue()+" 报错信息为:"+constraintViolation.getMessage());
        }
    }
```

## 自定义一个校验注解+逻辑
* 创建一个自定义逻辑分为3步：实现例子，判断字母的大小写
   * 创建约束标注
   * 实现一个验证器
   * 定义默认的验证错误信息
* 创建注解： 枚举类型CaseMode, 来表示大写或小写模式.
   * message属性，这个属性被用来定义默认得消息模版
   * groups属性，用于指定这个约束条件属于哪(些)个校验组，默认值必须是Class<?>类型到空到数组
   * payload 属性，指定约束条件指定严重级别
   * value在annotation的定义中比较特殊, 如果只有这个属性被赋值了的话, 那么, 在使用此annotation到时候可以忽略此属性名称, 即@CheckCase(CaseMode.UPPER).
   * @Target({ METHOD, FIELD, ANNOTATION_TYPE }): 表示@CheckCase 可以被用在方法, 字段或者annotation声明上.
   * @Retention(RUNTIME): 表示这个标注信息是在运行期通过反射被读取的.
   * @Constraint(validatedBy = CheckCaseValidator.class): 指明使用那个校验器(类) 去校验使用了此标注的元素.
```java
// 设置常量（枚举类）
public enum CaseMode {
    UPPER, 
    LOWER;
}
```
```java
// 创建注解
@Target( { METHOD, FIELD, ANNOTATION_TYPE })
@Retention(RUNTIME)
@Constraint(validatedBy = CheckCaseValidator.class)
@Documented
public @interface CheckCase {

    String message() default "{com.mycompany.constraints.checkcase}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
    
    CaseMode value();

}
```
* 创建校验器
   * ConstraintValidator定义了两个泛型参数: 第一个是这个校验器所服务到标注类型(即注解CheckCase)，第二个是支持到被校验元素到类型 (即String)
   * initialize() 方法传进来一个所要验证的注解类型的实例，本例子用注解来校验规则value（UPPER或LOWER）
   * isValid()是实现真正的校验逻辑的地方，第一个参数String value代表真正待验证的值(实际待验证业务值)，
   第二个参数ConstraintValidatorContext constraintContext为约束上下文，可用于自定义消息模板
```java
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
```
* 消息模板
   * 默认获取classpath下的ValidationMessages.properties（或对应Locale语种的_zh_CN.properties文件），如果classpath下找不到，会读取org.hibernate.validator:hibernate-validator包下的ValidationMessages.properties
   * 示例自定义模板：com.example.CheckCase.message=Case mode must be {value}，{value}代表实际业务值

## 参考文档：https://docs.jboss.org/hibernate/validator/4.2/reference/zh-CN/html_single/
       


