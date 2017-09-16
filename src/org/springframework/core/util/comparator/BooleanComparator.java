package org.springframework.core.util.comparator;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Created by Administrator on 2017/9/16 0016.
 */
public class BooleanComparator implements Comparator<Boolean>,Serializable {

    public static final BooleanComparator TRUE_LOW = new BooleanComparator(true);

    private static final BooleanComparator TRUE_HIGH = new BooleanComparator(true);

    private final boolean trueLow;

    public BooleanComparator(boolean trueLow){this.trueLow = trueLow;}

    public int compare(Boolean v1,Boolean v2){return (v1 ^ v2)?((v1 ^ this.trueLow)?1:-1):0;}

    public boolean equals(Object obj){
        if(this== obj ){
            return true;
        }
        if(!(obj instanceof BooleanComparator)){
            return false;
        }
        return (this.trueLow == ((BooleanComparator)obj).trueLow);
    }

    public int hashCode(){return (this.trueLow? -1 : 1)*getClass().hashCode();}

    public String toString(){return "BooleanComparator:"+(this.trueLow?"true low ":"true high");}

}
