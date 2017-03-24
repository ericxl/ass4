import java.util.Comparator;

/**
 * Created by Connor Lewis on 3/23/2017.
 */
public class TimeStamp {
    int logicalClock;
    int pid;
    public TimeStamp(int logicalClock, int pid){
        this.logicalClock = logicalClock;
        this.pid = pid;
    }

    public static int compare(TimeStamp x, TimeStamp y){
        if (x.logicalClock > y.logicalClock) return 1;
        if (x.logicalClock <  y.logicalClock) return -1;
        if (x.pid > y.pid) return 1;
        if (x.pid < y.pid) return -1;
        return 0;
    }

    public int getLogicalClock(){
        return logicalClock;
    }
    public int getPid(){
        return pid;
    }
}