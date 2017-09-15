package org.springframework.core.util.backoff;

import org.springframework.util.backoff.*;
import org.springframework.util.backoff.BackOffExecution;

/**
 * Created by Administrator on 2017/9/15 0015.
 */
public class ExponentialBackOff implements BackOff {

    private static final long DEFAULT_INITIAL_INTERVAL = 2000L;

    public static final double DEFAULT_MULTIPLIER = 1.5;

    public static final long DEFAULT_MAX_INTERVAL = 30000L;

    public static final long DEFAULT_MAX_ELAPSED_TIME = Long.MAX_VALUE;

    private long initialInterval = DEFAULT_INITIAL_INTERVAL;

    private double multiplier = DEFAULT_MULTIPLIER;

    private long maxInterval = DEFAULT_MAX_INTERVAL;

    private long maxElapsedTime = DEFAULT_MAX_ELAPSED_TIME;


    public ExponentialBackOff() {
    }

    public ExponentialBackOff(long initialInterval, double multiplier) {
        checkMulitiplier(multiplier);
        this.initialInterval = initialInterval;
        this.multiplier = multiplier;
    }

    public void setInitialInterval(long initialInterval) {
        this.initialInterval = initialInterval;
    }

    public long getInitialInterval() {
        return initialInterval;
    }

    public void setMulitiplier(double mulitiplier) {
        checkMulitiplier(multiplier);
        this.multiplier = multiplier;
    }

    public double getMultiplier() {
        return multiplier;
    }

    public void setMaxInterval(long maxInterval) {
        this.maxInterval = maxInterval;
    }

    public long getMaxInterval() {
        return maxInterval;
    }

    public void setMaxElapsedTime(long maxElapsedTime) {
        this.maxElapsedTime = maxElapsedTime;
    }

    public long getMaxElapsedTime() {
        return maxElapsedTime;
    }

    public BackOffExecution start() {
        return new ExponentialBackOffExecution();
    }

    private void checkMulitiplier(double mulitiplier) {
        if (mulitiplier < 1) {
            throw new IllegalArgumentException("Invalid mulitiplier'" + mulitiplier + ".Shouble be equal" +
                    "or higher than 1 A mulitiplier of 1 is equivalent to a fixed interval");
        }
    }

    private class ExponentialBackOffExecution implements BackOffExecution {
        private long currentInterval = -1;
        private long currentElapsedTime = 0;

        public long nextBackOff() {
            if (currentElapsedTime >= maxElapsedTime) {
                return STOP;
            }
            long nextInterval = computeNextInterval();
            currentElapsedTime += nextInterval;
            return nextInterval;
        }

        private long computeNextInterval() {
            long maxInterval = getMaxInterval();
            if (this.currentInterval >= maxInterval) {
                return maxInterval;
            } else if (this.currentInterval < 0) {
                long initialInterval = getInitialInterval();
                this.currentInterval = (initialInterval < maxInterval ? initialInterval : maxInterval);
            } else {
                this.currentInterval = multiplyInterval(maxInterval);
            }
            return currentInterval;
        }

        private long multiplyInterval(long maxInterval) {
            long i = this.currentInterval;
            i *= getMultiplier();
            return (i > maxInterval ? maxInterval : i);
        }

        public String toString() {
            String i = (this.currentInterval < 0 ? "n/a" : this.currentInterval + "ms");
            final StringBuilder sb = new StringBuilder("ExponentialBackOff");
            sb.append("currentInterval=").append(i);
            sb.append(",multiplier=").append(getMultiplier());
            sb.append('}');
            return sb.toString();
        }

    }
}
