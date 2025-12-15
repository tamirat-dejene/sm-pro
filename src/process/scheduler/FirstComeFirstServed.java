package process.scheduler;

import java.util.List;
import java.util.LinkedList;

public class FirstComeFirstServed extends Scheduler {
  public FirstComeFirstServed(List<PCB> processes, int contextSwitchingTime) {
    super(processes, contextSwitchingTime);
  }

  // Non preemptive by nature
  public LinkedList<String> schedule() {
    var processesCopy = new LinkedList<PCB>();
    for (PCB process : getProcesses())
      processesCopy.add(process.clone()); // Deep copying

    if (processesCopy.isEmpty()) {
      getScheduleTable().addLast("---- Empty Process Block ----");
      return getScheduleTable();
    }

    // Sort the processes based on their arrival time
    processesCopy.sort((p1, p2) -> p1.getArrivalTime() - p2.getArrivalTime());
    var readyQueue = new LinkedList<PCB>();
    readyQueue.add(processesCopy.removeFirst()); // Add the first process to the ready queue

    var timer = 0;
    var totTAT = 0;
    var totWT = 0;

    while (!readyQueue.isEmpty()) {
      var currentProcess = readyQueue.removeFirst();
      if (currentProcess.getArrivalTime() > timer) {
        getScheduleTable().addLast("[" + timer + " <- -- -> " + (currentProcess.getArrivalTime()) + "]");
        timer = currentProcess.getArrivalTime();
      }

      getScheduleTable().addLast(
          "[" + timer + " <- " + currentProcess.getPID() + " -> " + (timer + currentProcess.getBurstTime()) + "]");

      timer += currentProcess.getBurstTime();

      getCompletionTime().put(currentProcess.getPID(), timer);
      getTurnAroundTime().put(currentProcess.getPID(), timer - currentProcess.getArrivalTime());
      getWaitingTime().put(currentProcess.getPID(),
          getTurnAroundTime().get(currentProcess.getPID()) - currentProcess.getBurstTime());

      totTAT += getTurnAroundTime().get(currentProcess.getPID());
      totWT += getWaitingTime().get(currentProcess.getPID());

      if (!processesCopy.isEmpty())
        readyQueue.add(processesCopy.removeFirst());

      // Context Switching Time
      if (getContextSwitchTime() > 0 && !readyQueue.isEmpty())
        timer += getContextSwitchTime();
    }

    setAverageTurnAroundTime((double) totTAT / getProcesses().size());
    setAverageWaitingTime((double) totWT / getProcesses().size());
    calculateExtendedMetrics();

    return getScheduleTable();
  }
  

  public static void main(String[] args) {
    System.out.println("First-Come-First-Served Scheduler Test");

    // Example processes
    List<PCB> processes = List.of(
        new PCB("P1", 0, 5, 1),
        new PCB("P2", 2, 3, 1),
        new PCB("P3", 4, 1, 1)
    );

    FirstComeFirstServed fcfsScheduler = new FirstComeFirstServed(processes, 0);
    LinkedList<String> scheduleTable = fcfsScheduler.schedule();

    // Print the schedule table
    for (String entry : scheduleTable) {
      System.out.println(entry);
    }

    // Print metrics
    System.out.printf("Average Turnaround Time: %.2f ms%n", fcfsScheduler.getAverageTurnAroundTime());
    System.out.printf("Average Waiting Time: %.2f ms%n", fcfsScheduler.getAverageWaitingTime());
  }
}
