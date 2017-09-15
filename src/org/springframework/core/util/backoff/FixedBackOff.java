package org.springframework.core.util.backoff;

/**
 * Created by Administrator on 2017/9/15 0015.
 */
public class FixedBackOff implements BackOff{

    public static final long DEFAULT_INTERVAL = 5000;

    public static final long UNLIMITED_ATTEMPTS = Long.MAX_VALUE;

    private long interval = DEFAULT_INTERVAL;

    private long maxAttermpts = UNLIMITED_ATTEMPTS;

    public FixedBackOff(){}

    public FixedBackOff(long interval ,long maxAttermpts){
        this.interval = interval;
        this.maxAttermpts = maxAttermpts;
    }

    public void setInterval(long interval){ this.interval = interval;}

    public long getInterval(){return interval;}

    public long getMaxAttermpts() {
        return maxAttermpts;
    }

    public void setMaxAttermpts(long maxAttermpts) {
        this.maxAttermpts = maxAttermpts;
    }

    public BackOffExecution start(){return new FixedBackOffExecution();}

    private class FixedBackOffExecution implements BackOffExecution{

        private long currentAttempts = 0;

        public long nextBackOff(){
            this.currentAttempts++;
            if(this.currentAttempts < = getMaxAttermpts()){
                return getInterval();
            }
            else{
                return STOP;
            }
        }

        public String toString(){
            final StringBuilder sb = new StringBuilder("FixedBackOff{");
            sb.append("interval=").append(FixedBackOff.this.interval);
            String attemptValue = (FixedBackOff.this.maxAttermpts == Long.MAX_VALUE?"unlimited":String.valueOf(FixedBackOff.this.maxAttermpts));
            sb.append(",currentAttempts=").append(this.currentAttempts);
            sb.append(",maxAttempts=").append(attemptValue);
            sb.append('}');
            return sb.toString();
        }

    }
}
