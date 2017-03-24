import java.util.Comparator;

/**
 * Created by Connor Lewis on 3/23/2017.
 */
public class TimeComparator implements Comparator<TimeStamp> {
    @Override
    public int compare(TimeStamp x, TimeStamp y){
        int xstamp = x.getLogicalClock();
        int ystamp = y.getLogicalClock();

        if(xstamp > ystamp){
            return 1;
        }
        if(xstamp < ystamp){
            return -1;
        }
        return 0;
    }
}