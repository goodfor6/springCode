package org.springframework.core.util;

import java.text.NumberFormat;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Administrator on 2017/8/16 0016.
 */
public class StopWatch {
    private final String id;
    private boolean keepTaskList = true;
    private final List<TaskInfo> taskList = new LinkedList();
    private long startTimeMillis;
    private boolean running;
    private String currentTaskName;
    private StopWatch.TaskInfo lastTaskInfo;
    private int taskCount;
    private long totalTimeMillis;
    public StopWatch(){this.id = "";}
    public StopWatch(String id){this.id = id;}
    public void setKeepTaskList(boolean keepTaskList){this.keepTaskList = keepTaskList;}
    public void start() throws IllegalStateException{this.start("");}
    public void start(String taskName)throws IllegalStateException{
        if(this.running){
            throw new IllegalStateException("Can't start StopWatch: it's already running");
        }
        else{
            this.startTimeMillis = System.currentTimeMillis();
            this.running = true;
            this.currentTaskName = taskName;
        }
    }

    public void stop() throws IllegalStateException{
        if(!this.running){
            throw new IllegalStateException("Can't stop StopWatch : it's not running");
        }
        else{
            long lastTime = System.currentTimeMillis() - this.startTimeMillis;
            this.totalTimeMillis += lastTime;
            this.lastTaskInfo = new StopWatch.TaskInfo(this.currentTaskName,lastTime);
            if(this.keepTaskList){
                this.taskList.add(this.lastTaskInfo);
            }

            ++this.taskCount;
            this.running = false;
            this.currentTaskName = null;
        }
    }


    public boolean isRunning() {return this.running;}

    public long getLastTaskTimeMillis() throws IllegalStateException{
        if(this.lastTaskInfo == null){
            throw new IllegalStateException("No tasks run: can't get last task inteval");
        }
        else{
            return this.lastTaskInfo.getTimeMillis();
        }
    }


    public String getLastTaskName()throws IllegalStateException{
        if(this.lastTaskInfo == null){
            throw new IllegalStateException("No tasks run: can't get last task name  ");
        }
        else{
            return this.lastTaskInfo.getTaskName();
        }
    }


    public StopWatch.TaskInfo getLastTaskInfo() throws IllegalStateException{
        if(this.lastTaskInfo == null){
            throw new IllegalStateException("No tasks run: can't get last task info");
        }
        else{
            return this.lastTaskInfo;
        }
    }

    public long getTotalTimeMillis(){return this.totalTimeMillis;}

    public double getTotalTimeSeconds(){return (double)this.totalTimeMillis/1000.00;}

    public int getTaskCount(){return this.taskCount;}

    public StopWatch.TaskInfo[] getTaskInfo(){
        if(!this.keepTaskList){
            throw new UnsupportedOperationException("Task info is not being kept!");
        }
        else {
            return (StopWatch.TaskInfo[])this.taskList.toArray(new StopWatch.TaskInfo[this.taskList.size()]);
        }
    }

    public String shortSummary(){
        return "StopWatch '"+this.id+"': running time (millis) = "+this.getTotalTimeMillis();
    }

    public String prettyPrint(){
        StringBuilder sb = new StringBuilder(this.shortSummary());
        sb.append('\n');
        if(!this.keepTaskList){
            sb.append("No task info kept");
        }
        else {
            sb.append("------------------------------\n");
            sb.append("ms      %         Task name\n");
            sb.append("------------------------------\n");
            NumberFormat nf = NumberFormat.getPercentInstance();
            nf.setMinimumIntegerDigits(5);
            nf.setGroupingUsed(false);
            NumberFormat pf = NumberFormat.getPercentInstance();
            pf.setMinimumIntegerDigits(3);
            pf.setGroupingUsed(false);
            StopWatch.TaskInfo[] var4 = this.getTaskInfo();
            int var5 = var4.length;

            for(int var6 = 0; var6 < var5;++var6){
                StopWatch.TaskInfo task = var4[var6];
                sb.append(nf.format(task.getTimeMillis())).append(" ");
                sb.append(pf.format(task.getTimeSeconds() / this.getTotalTimeSeconds())).append("  ");
                sb.append(task.getTaskName()).append("\n");
            }
        }
        return sb.toString();
    }

    public String toString(){
        StringBuilder sb = new StringBuilder(this.shortSummary());
        if(this.keepTaskList){
            StopWatch.TaskInfo[] var2 = this.getTaskInfo();
            int var3 = var2.length;

            for(int var4 = 0;var4 < var3; ++var4){
                StopWatch.TaskInfo task = var2[var4];
                sb.append("; [").append(task.getTaskName()).append("] took ").append(task.getTimeMillis());
                long percent = Math.round(100.0D * task.getTimeSeconds() / this.getTotalTimeSeconds());
                sb.append(" = ").append(percent).append("%");
            }
        }else{
            sb.append(";no task info kept ");
        }
        return sb.toString();
    }

    public static final class TaskInfo{
        private final String taskName;
        private final long timeMillis;

        TaskInfo(String taskName,long timeMillis){
            this.taskName = taskName;
            this.timeMillis = timeMillis;
        }
        public String getTaskName(){return this.taskName;}
        public long getTimeMillis(){return this.timeMillis;}

        public double getTimeSeconds(){return (double)this.timeMillis/1000.0D;}
    }



}
