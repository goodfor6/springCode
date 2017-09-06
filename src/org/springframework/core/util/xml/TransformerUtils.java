package org.springframework.core.util.xml;

import org.springframework.core.util.Assert;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;

/**
 * Created by Administrator on 2017/9/6 0006.
 */
public abstract class TransformerUtils {

    public static final int DEFAULT_INDENT_AMOUNT = 2;

    public static void enableIndenting(Transformer transformer) {enableIndenting(transformer,DEFAULT_INDENT_AMOUNT);}

    public static void enableIndenting(Transformer transformer, int indentAmount){
        Assert.notNull(transformer,"Transformer must not be null");
        Assert.isTrue(indentAmount > -1,"The indent amount cannot be less than zero:got"+indentAmount);
        transformer.setOutputProperty(OutputKeys.INDENT,"yes");
        try{
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount",String.valueOf(indentAmount));
        }
        catch(IllegalArgumentException ignored){}
    }

    public static void disableIndenting(Transformer transformer){
        Assert.notNull(transformer,"Transfomer must not be null");
        transformer.setOutputProperty(OutputKeys.INDENT,"no");
    }

}
