package me.acablade.nmsasm;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.PARAMETER, ElementType.TYPE, ElementType.FIELD})
public @interface NMS {
	
	public String value();
    public CallType callType() default CallType.METHOD;
    public String interfaceName() default "";
    
}
