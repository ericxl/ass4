public class Clock {
    int time;
    public Clock(){
        time = 1;
    }
    public int getValue(){
        return time;
    }
    public void incrementClock(){
        time = time + 1;
    }

    public void action(int value){
        if(time < value){
            time = value + 1;
        }
        else {
            incrementClock();
        }
    }
}