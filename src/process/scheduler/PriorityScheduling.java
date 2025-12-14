package process.scheduler;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class PriorityScheduling extends Scheduler {
  public PriorityScheduling(List<PCB> processes, int contextSwitchingTime) {
    super(processes, contextSwitchingTime);
  }

  public LinkedList<String> non_preemptive_schedule() {
    // Priority Queue of the procces: with the provided comparator
    // it is always guaranteed to get the process with the smallest burst time at
    // the top/front of ppq;
    Comparator<PCB> proccessQueueSorter = new Comparator<PCB>() {
      @Override
      public int compare(PCB p1, PCB p2) {
        if (p1.getArrivalTime() - p2.getArrivalTime() == 0)
          return p1.getPriority() - p2.getPriority();
        return p1.getArrivalTime() - p2.getArrivalTime();
      }
    };

    // The lower the number, theearlier it gets executed
    return schedule(proccessQueueSorter, (p1, p2) -> p1.getPriority() - p2.getPriority());
  }
}
