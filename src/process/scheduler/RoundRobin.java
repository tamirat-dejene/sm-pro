package process.scheduler;

import java.util.LinkedList;
import java.util.List;

public class RoundRobin extends Scheduler {
  private int timeQuantum;

  public RoundRobin(List<PCB> processes, int timeQuantum, int contextSwitchTime) {
    super(processes, contextSwitchTime);
    this.timeQuantum = timeQuantum;
  }

  // preemptive schedule
  public LinkedList<String> schedule() {
    // Reset all metrics
    clearMetrics();
    
    var processesCopy = new LinkedList<PCB>();
    for (PCB process : getProcesses())
      processesCopy.add(process.clone());

    if (processesCopy.size() == 0) {
      getScheduleTable().addLast("---- Empty Process Block ----");
      return getScheduleTable();
    }

    // Sort the processes based on their arrival time
    processesCopy.sort((p1, p2) -> p1.getArrivalTime() - p2.getArrivalTime());

    var readyQueue = new LinkedList<PCB>();
    readyQueue.add(processesCopy.removeFirst());

    int timer = 0;
    String previousPID = "";

    while (!readyQueue.isEmpty()) {
      var currentProcess = readyQueue.removeFirst();

      if (currentProcess.getArrivalTime() > timer) {
        getScheduleTable().addLast("[" + timer + " <- -- -> " + (currentProcess.getArrivalTime()) + "]");
        timer = currentProcess.getArrivalTime();
      } // The process hasn't arrived yet

      if (!currentProcess.getPID().equals(previousPID) && !currentProcess.getPID().isEmpty()) {
        timer += getContextSwitchTime();
      }
      previousPID = currentProcess.getPID();


      if (currentProcess.getBurstTime() <= timeQuantum) {
        getScheduleTable().addLast(
            "[" + timer + " <- " + currentProcess.getPID() + " -> " + (timer + currentProcess.getBurstTime()) + "]");

        timer += currentProcess.getBurstTime();
        getCompletionTime().put(currentProcess.getPID(), timer);
        currentProcess = null; // Done
      } else {
        getScheduleTable().addLast(
            "[" + timer + " <- " + currentProcess.getPID() + " -> " + (timer + timeQuantum) + "]");
        currentProcess.setBurstTime(currentProcess.getBurstTime() - timeQuantum);

        timer += timeQuantum;
        getCompletionTime().put(currentProcess.getPID(), timer);
      }

      while (!processesCopy.isEmpty() && processesCopy.getFirst().getArrivalTime() <= timer)
        readyQueue.addLast(processesCopy.removeFirst());
      if (currentProcess != null)
        readyQueue.addLast(currentProcess);

      if (!processesCopy.isEmpty() && readyQueue.isEmpty())
        readyQueue.add(processesCopy.removeFirst());
    }

    int totalWaitingTime = 0, totalTurnAroundTime = 0;
    for (PCB process : getProcesses()) {
      int completionT = getCompletionTime().get(process.getPID());
      int originalBurstT = process.getBurstTime();

      int turnaroundT = completionT - process.getArrivalTime();
      int waitingT = turnaroundT - originalBurstT;

      getTurnAroundTime().put(process.getPID(), turnaroundT);
      totalTurnAroundTime += turnaroundT;
      getWaitingTime().put(process.getPID(), waitingT);
      totalWaitingTime += waitingT;
    }

    setAverageTurnAroundTime((double) totalTurnAroundTime / getProcesses().size());
    setAverageWaitingTime((double) totalWaitingTime / getProcesses().size());
    calculateExtendedMetrics();
    
    return getScheduleTable();
  }
}
