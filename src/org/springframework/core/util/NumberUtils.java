package org.springframework.core.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Administrator on 2017/8/7 0007.
 */
public abstract class NumberUtils {

    private static final BigInteger LONG_MIN = BigInteger.valueOf(-9223372036854775808L);
    private static final BigInteger LONG_MAX = BigInteger.valueOf(9223372036854775807L);
    public static final Set<Class<?>> STANDARD_NUMBER_TYPES;
    public NumberUtils() {
    }

    public static <T extends Number> T convertNumberToTargetClass(Number number,Class<T>targetClass) throws IllegalArgumentException{
        Assert.notNull(number,"Number must not be null");
        Assert.notNull(targetClass,"Target Class must not be null");
        if(targetClass.isInstance(number)){
            return number;
        }else{
            long value;
            if(targetClass.equals(Byte.class)){
                value = number.longValue();
                if(value < -128 || value > 127L){
                    raiseOverflowException(number,targetClass);
                }

                return new Byte(number.byteValue());
            }else if(targetClass.equals(Short.class)){
                value = number.longValue();
                if(value < -32768L || value > 32767L ){
                    raiseOverflowException(number,targetClass);
                }
                return new Short(number.shortValue());
            }else if (targetClass.equals(Integer.class)){
                value = number.longValue();
                if(value < -2147483648L || value > 2147483647L ){
                    raiseOverflowException(number,targetClass);
                }
                return new Integer(number.intValue());
            }else if(targetClass.equals(Long.class)){
                BigInteger bigInt = null;
                if(number instanceof BigInteger){
                    bigInt = (BigInteger)number;
                }else if(number instanceof BigDecimal){
                    bigInt = ((BigDecimal) number).toBigInteger();
                }

                if(bigInt != null && (bigInt.compareTo(LONG_MIN) < 0 || bigInt.compareTo(LONG_MAX) > 0)){
                    raiseOverflowException(number,targetClass);
                }

                return new Long(number.longValue());

            }else if (targetClass.equals(BigInteger.class)){
                return number instanceof BigDecimal? ((BigDecimal)number).toBigInteger(): BigInteger.valueOf(number.longValue());
            }else if( targetClass.equals(Float.class)){
                return new Float(number.floatValue());
            }else if(targetClass.equals(Double.class)){
                return new Double(number.doubleValue());
            }else if(targetClass.equals(BigDecimal.class)){
                return new BigDecimal(number.toString());
            }else {
                throw new IllegalArgumentException("Could not convert number["+number+"] of type ["+number.getClass().getName()+" ]to unknown target class ["+targetClass.getName()+"]");
            }
        }
    }


    static {
        Set<Class<?>> numberTypes = new HashSet(8);
        numberTypes.add(Byte.class);
        numberTypes.add(Short.class);
        numberTypes.add(Integer.class);
        numberTypes.add(Long.class);
        numberTypes.add(BigInteger.class);
        numberTypes.add(Float.class);
        numberTypes.add(Double.class);
        numberTypes.add(BigDecimal.class);
        STANDARD_NUMBER_TYPES = Collections.unmodifiableSet(numberTypes);
    }

}
