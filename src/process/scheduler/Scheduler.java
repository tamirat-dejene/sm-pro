package process.scheduler;

import java.util.Comparator;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;
import java.util.PriorityQueue;

public class Scheduler {
  private List<PCB> processes;
  private Map<String, Integer> turnAroundTime;
  private Map<String, Integer> waitingTime;
  private Map<String, Integer> completionTime;
  private LinkedList<String> scheduleTable;
  private double averageTurnAroundTime;
  private double averageWaitingTime;
  private double cpuUtilization;
  private double throughput;

  public static class SimulationResult {
    public double avgWaitingTime;
    public double avgTurnAroundTime;
    public double cpuUtilization;
    public double throughput;
  }

  public Scheduler(List<PCB> processes) {
    this.processes = processes;
    this.turnAroundTime = new HashMap<>();
    this.completionTime = new HashMap<>();
    this.waitingTime = new HashMap<>();
    this.scheduleTable = new LinkedList<>();
    this.averageTurnAroundTime = 0D;
    this.averageWaitingTime = 0D;
    this.cpuUtilization = 0D;
    this.throughput = 0D;
  }

  public List<PCB> getProcesses() {
    return processes;
  }

  public void setProcesses(List<PCB> processes) {
    this.processes = processes;
  }

  public Map<String, Integer> getTurnAroundTime() {
    return turnAroundTime;
  }

  public void setTurnAroundTime(Map<String, Integer> turnAroundTime) {
    this.turnAroundTime = turnAroundTime;
  }

  public Map<String, Integer> getWaitingTime() {
    return waitingTime;
  }

  public void setWaitingTime(Map<String, Integer> waitingTime) {
    this.waitingTime = waitingTime;
  }

  public Map<String, Integer> getCompletionTime() {
    return completionTime;
  }

  public void setCompletionTime(Map<String, Integer> completionTime) {
    this.completionTime = completionTime;
  }

  public LinkedList<String> getScheduleTable() {
    return this.scheduleTable;
  }

  public void setScheduleTable(LinkedList<String> scheduleTable) {
    this.scheduleTable = scheduleTable;
  }
  
  public double getAverageTurnAroundTime() {
    return averageTurnAroundTime;
  }

  public void setAverageTurnAroundTime(double averageTurnAroundTime) {
    this.averageTurnAroundTime = averageTurnAroundTime;
  }

  public double getAverageWaitingTime() {
    return averageWaitingTime;
  }

  public void setAverageWaitingTime(double averageWaitingTime) {
    this.averageWaitingTime = averageWaitingTime;
  }

  public double getCpuUtilization() {
    return cpuUtilization;
  }

  public double getThroughput() {
    return throughput;
  }

  public static void print(LinkedList<String> scheduleTable) {
    System.out.println(" -------- Process Execution Schedule -------- ");
    for (var row : scheduleTable)
      System.out.println("  " + row);
    System.out.println(" -------- ------- --------- -------- -------- ");
  }

  public static void print(List<PCB> processes) {
    System.out.println(" ----------------- PROCESSES ---------------- ");
    System.out.println("PID   BurstT(ms)     ArrivalT(ms)     Priority");
    for (var procces : processes) {
      System.out.println(
          procces.getPID() + "    " + procces.getBurstTime() + "              " + procces.getArrivalTime()
              + "                "
              + procces.getPriority());
    }
    System.out.println(" -------------------------------------------- ");
  }

  public static void print(String header, Map<String, Integer> mapData) {
    System.out.println(" ------ " + header + " ------ ");
    for (var entry : mapData.entrySet())
      System.out.println("   " + entry.getKey() + " : " + entry.getValue() + "ms");
    System.out.println(" ------ ---------- ----- ------ ");
  }

  public static void print(String header, double timeUnits) {
    System.out.println(" - " + header + ": " + timeUnits + "ms");
  }

  public void printSimulationResults() {
    System.out.println("  Average Waiting Time:    " + String.format("%.2f", averageWaitingTime) + " ms");
    System.out.println("  Average Turnaround Time: " + String.format("%.2f", averageTurnAroundTime) + " ms");
    System.out.println("  CPU Utilization:         " + String.format("%.2f", cpuUtilization) + "%");
    System.out.println("  Throughput:              " + String.format("%.4f", throughput) + " jobs/ms");
}
  
  /**
   * Calculate Extended Metrics: CPU Utilization and Throughput
   */
  public void calculateExtendedMetrics() {
    if (processes.isEmpty() || completionTime.isEmpty())
      return;

    // 1. Find the simulation "Makespan" (End Time - Start Time)
    // Start time is assumed to be the arrival time of the first process (0)
    // End time is the maximum completion time
    int minArrival = processes.stream()
        .mapToInt(PCB::getArrivalTime)
        .min()
        .orElse(0);
    int maxCompletion = completionTime.values().stream()
        .mapToInt(Integer::intValue)
        .max()
        .orElse(0);
    int totlaSimulationTime = maxCompletion - minArrival;

    if (totlaSimulationTime == 0)
      return;

    // 2. Calculate CPU Busy Time (Sum of all Burst Times)
    int cpuBusyTime = processes.stream()
        .mapToInt(PCB::getBurstTime)
        .sum();
      
    // 3. Calculate CPU Utilization (%)
    this.cpuUtilization = ((double) cpuBusyTime / totlaSimulationTime) * 100.0;

    // 4. Calculate Throughput (Processes per Time Unit(miliseconds))
    this.throughput = (double) processes.size() / totlaSimulationTime;
  }
  
  /**
   * @param listComparator Comparator to sort the proccess list
   * @param pqComparator   the priority queue sorting comparator
   */

  public LinkedList<String> schedule(Comparator<PCB> listComparator, Comparator<PCB> pqComparator) {
    // Reset previous metrics
    clearMetrics();
  
    var processesCopy = new LinkedList<PCB>();
    for (PCB process : processes)
      processesCopy.add(process.clone()); // Deep copying

    if (processesCopy.size() == 0) {
      scheduleTable.addLast("---- Empty Process Block ----");
      return scheduleTable;
    }

    processesCopy.sort(listComparator);

    var ppq = new PriorityQueue<PCB>(pqComparator);
    ppq.add(processesCopy.removeFirst());
    var timer = 0;
    var totTAT = 0;
    var totWT = 0;

    while (!ppq.isEmpty()) {
      var currentProcess = ppq.poll();

      if (currentProcess.getArrivalTime() > timer) {
        scheduleTable.addLast("[" + timer + " <- -- -> " + (currentProcess.getArrivalTime()) + "]");
        timer = currentProcess.getArrivalTime();
      } // the process not arrived yet

      scheduleTable.addLast(
          "[" + timer + " <- " + currentProcess.getPID() + " -> " + (timer + currentProcess.getBurstTime()) + "]");
      timer += currentProcess.getBurstTime();
      completionTime.put(currentProcess.getPID(), timer);
      turnAroundTime.put(currentProcess.getPID(), timer - currentProcess.getArrivalTime());
      waitingTime.put(currentProcess.getPID(),
          turnAroundTime.get(currentProcess.getPID()) - currentProcess.getBurstTime());

      totTAT += turnAroundTime.get(currentProcess.getPID());
      totWT += waitingTime.get(currentProcess.getPID());

      while (!processesCopy.isEmpty() && processesCopy.getFirst().getArrivalTime() <= timer)
        ppq.add(processesCopy.removeFirst());

      // If the priority queue is empty but there are still processes left to schedule,
      // advance the timer to the arrival time of the next process and add it to the queue
      if (ppq.isEmpty() && !processesCopy.isEmpty())
        ppq.add(processesCopy.removeFirst());
    }

    this.averageTurnAroundTime = (double) totTAT / processes.size();
    this.averageWaitingTime = (double) totWT / processes.size();
    this.calculateExtendedMetrics();

    return this.scheduleTable;
  }
  
  public SimulationResult CollectMetrics() {
    SimulationResult result = new SimulationResult();
    result.avgWaitingTime = this.averageWaitingTime;
    result.avgTurnAroundTime = this.averageTurnAroundTime;
    result.cpuUtilization = this.cpuUtilization;
    result.throughput = this.throughput;
    return result;
  }

  public void clearMetrics() {
    this.turnAroundTime.clear();
    this.completionTime.clear();
    this.waitingTime.clear();
    this.scheduleTable.clear();
    this.averageTurnAroundTime = 0D;
    this.averageWaitingTime = 0D;
  }

}
