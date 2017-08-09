package org.springframework.core.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.springframework.util.StringUtils;

/**
 * Created by Administrator on 2017/8/7 0007.
 */
public abstract class NumberUtils {

    private static final BigInteger LONG_MIN = BigInteger.valueOf(-9223372036854775808L);
    private static final BigInteger LONG_MAX = BigInteger.valueOf(9223372036854775807L);
    public static final Set<Class<?>> STANDARD_NUMBER_TYPES;
    public NumberUtils() {
    }

    public static <T extends Number> Number convertNumberToTargetClass(Number number,Class<T>targetClass) 
    		throws IllegalArgumentException{
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
    
    private static void raiseOverflowException(Number number, Class<?> targetClass){
    	throw new IllegalArgumentException("Could not convert number ["+number+"] of type ["+number.getClass().getName()+"] to target class ["
    			+targetClass.getName()+" ]: overflow");
    }

    @SuppressWarnings("unchecked")
	public static <T extends Number>T parseNumber(String text,Class<T>targetClass){
    	Assert.notNull(text,"Text must be null");
    	Assert.notNull(targetClass,"Target class not be null ");
    	String trimmed = StringUtils.trimAllWhitespace(text);
    	
    	if(targetClass.equals(Byte.class)){
    		return (T)(isHexNumber(trimmed) ? Byte.decode(trimmed):Byte.valueOf(trimmed));
    	}
    	else if(targetClass.equals(Short.class)){
    		return (T)(isHexNumber(trimmed) ? Short.decode(trimmed) : Short.valueOf(trimmed) );
    	}
    	else if(targetClass.equals(Integer.class)){
    		return (T)(isHexNumber(trimmed) ? Integer.decode(trimmed) : Integer.valueOf(trimmed));
    	}
    	else if(targetClass.equals(Long.class)){
    		return (T) (isHexNumber(trimmed) ? Long.decode(trimmed) : Long.valueOf(trimmed));
    	}
    	else if(targetClass.equals(BigInteger.class)){
    		return (T) (isHexNumber(trimmed) ? decodeBigInteger(trimmed) : new BigInteger(trimmed));
    	}
    	else if(targetClass.equals(Float.class)){
    		return (T) Float.valueOf(trimmed);
    	}
    	else if(targetClass.equals(Double.class)){
    		return (T) Double.valueOf(trimmed);
    	}
    	else if(targetClass.equals(BigDecimal.class) || targetClass.equals(Number.class)){
    		return (T) new BigDecimal(trimmed);
    	}
    	else{
    		throw new IllegalArgumentException(
    			"Count convert String ["+text+"] to target class ["+targetClass.getName()+"]");
    		}
     }
    
    public static <T extends Number> Number parseNumber(String text,Class<T>targetClass,NumberFormat numberFormat){
    	if(numberFormat != null){
    		Assert.notNull(text,"Text must not be null");
    		Assert.notNull(targetClass,"Target class must not be null ");
    		DecimalFormat decimalFormat = null;
    		boolean resetBigDecimal = false;
    		if(numberFormat instanceof DecimalFormat){
    			decimalFormat = (DecimalFormat) numberFormat;
    			if(BigDecimal.class.equals(targetClass) && !decimalFormat.isParseBigDecimal()){
    				decimalFormat.setParseBigDecimal(true);
    				resetBigDecimal = true;
    			}
    		}
    		try{
    			Number number = numberFormat.parse(StringUtils.trimAllWhitespace(text));
    			return convertNumberToTargetClass(number,targetClass);
    		}
    		catch(ParseException ex){
    			throw new IllegalArgumentException("Could not parse number: "+ex.getMessage());
    		}
    		finally{
    			if(resetBigDecimal){
    				decimalFormat.setParseBigDecimal(false);
    			}
    		}
    	}
    	else {
    		return parseNumber(text,targetClass);
    	}
    }
    
    private static boolean isHexNumber(String value){
    	int index = (value.startsWith("-") ? 1 : 0);
    	return (value.startsWith("0x",index)||value.startsWith("0X",index)|| value.startsWith("#",index));
    }
    
    
    private static BigInteger decodeBigInteger(String value ){
    	int index = 0;
    	int radix = 10;
    	boolean negative = false;
    	
    	if(value.startsWith("-")){
    		negative = true;
    		index++;
    	}
    	
    	if(value.startsWith("0x",index)|| value.startsWith("0X",index)){
    		index += 2;
    		radix = 16;
    	}
    	
    	else if (value.startsWith("#",index)){
    		index++;
    		radix = 16;
    	}
    	else if(value.startsWith("0",index)&& value.length() > 1 + index){
    		index++;
    		radix = 8;
    	}
    	
    	BigInteger result = new BigInteger(value.substring(index), radix);
    	return (negative ? result.negate() : result);
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
