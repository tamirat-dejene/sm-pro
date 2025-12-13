package process.scheduler;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class ShortestJobFirst extends Scheduler {
  public ShortestJobFirst(List<PCB> processes) {
    super(processes);
  }

  public LinkedList<String> non_preemptive_schedule() {
    Comparator<PCB> sortComparator = new Comparator<PCB>() {
      @Override
      public int compare(PCB p1, PCB p2) {
        // If two process arrives at the same time, use their burst time as a sorting option
        if (p1.getArrivalTime() - p2.getArrivalTime() == 0)
          return p1.getBurstTime() - p2.getBurstTime();
        return p1.getArrivalTime() - p2.getArrivalTime();
      }
    };
    
    return schedule(sortComparator, (p1, p2) -> p1.getBurstTime() - p2.getBurstTime());
  }

  public void preemptive_schedule() {
    // To be implemented
  }
}
