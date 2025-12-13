package process.scheduler;

public class PCB implements Cloneable {
  private String pID;
  private int burstTime;
  private int arrivalTime = 0;
  private int priority = 0;

  /**
   * PCB : Process Contorl Block
   * 
   * @param pID         pID - Represents the current running process uniquely
   * @param burstTime   Burst Time} burstTime - The cpu time a process require to
   *                    execute
   * @param arrivalTime Arrival Time} arrivalTime - The time at which the process
   *                    got
   * @param priority    the priority attached to the process
   */
  public PCB(String pID, int burstTime, int arrivalTime, int priority) {
    this.pID = pID;
    this.burstTime = burstTime;
    this.arrivalTime = arrivalTime;
    this.priority = priority;
  }

  /**
   * PCB : Process Contorl Block
   * 
   * @param pID         pID - Represents the current running process uniquely
   * @param burstTime   Burst Time} burstTime - The cpu time a process require to
   *                    execute
   * @param arrivalTime Arrival Time} arrivalTime - The time at which the process
   *                    got
   */
  public PCB(String pID, int burstTime, int arrivalTime) {
    this.pID = pID;
    this.burstTime = burstTime;
    this.arrivalTime = arrivalTime;
    this.priority = 0;
  }

  /**
   * PCB : Process Contorl Block
   * 
   * @param burstTime   Burst Time} burstTime - The cpu time a process require to
   *                    execute
   * @param priority    the priority attached to the process
   * @param pID         pID - Represents the current running process uniquely
   */
  
  public PCB(int burstTime, int priority, String pID) {
    this.pID = pID;
    this.burstTime = burstTime;
    this.priority = priority;
  }

  /**
   * PCB : Process Contorl Block
   * 
   * @param pID       pID - Represents the current running process uniquely
   * @param burstTime Burst Time - The cpu time a process require to execute
   */
  public PCB(String pID, int burstTime) {
    this.pID = pID;
    this.burstTime = burstTime;
    this.arrivalTime = 0;
    this.priority = 0;
  }

  public String getPID() {
    return pID;
  }

  public void setPID(String pID) {
    this.pID = pID;
  }

  public int getBurstTime() {
    return burstTime;
  }

  public void setBurstTime(int burstTime) {
    this.burstTime = burstTime;
  }

  public int getArrivalTime() {
    return arrivalTime;
  }

  public void setArrivalTime(int arrivalTime) {
    this.arrivalTime = arrivalTime;
  }

  public int getPriority() {
    return priority;
  }

  public void setPriority(int priority) {
    this.priority = priority;
  }

  @Override
  public String toString() {
    return "PCB [PID=" + pID + ", burstTime=" + burstTime + ", arrivalTime=" + arrivalTime + ", priority=" + priority
        + "]";
  }

  @Override
  public PCB clone() {
    return new PCB(pID, burstTime, arrivalTime, priority);
  }
}