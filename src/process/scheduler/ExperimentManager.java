package process.scheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.io.FileWriter;
import java.io.IOException;

public class ExperimentManager {
    // --- Experiment Parameters ---
    private Parameters parameters;
    public static class Parameters {
        public int numJobs;
        public double meanInterArrival;
        public double meanBurst;
        public double stdDevBurst;
        public int maxPriority;
        public int timeQuantum;
        public int contextSwitchTime;
        public int replications;

        public Parameters(int numJobs, double meanInterArrival, double meanBurst, double stdDevBurst,
                int maxPriority, int timeQuantum, int contextSwitchTime, int replications) {
            this.numJobs = numJobs;
            this.meanInterArrival = meanInterArrival;
            this.meanBurst = meanBurst;
            this.stdDevBurst = stdDevBurst;
            this.maxPriority = maxPriority;
            this.timeQuantum = timeQuantum;
            this.contextSwitchTime = contextSwitchTime;
            this.replications = replications;
        }
    }
    // --- Data Storage for Python Export ---
    private Map<String, List<Double>> waitingTimes = new HashMap<>();
    private Map<String, List<Double>> turnaroundTimes = new HashMap<>();
    private Map<String, List<Double>> cpuUtilization = new HashMap<>();
    private Map<String, List<Double>> throughputs = new HashMap<>();
    private List<String> algorithmNames = List.of("FCFS", "Priority-NP", "SJF-NP", "RoundRobin");

    // CSV Output File
    private static final String OUTPUT_FILE = "simulation_results.csv";
    

    public ExperimentManager(Parameters parameters) {
        this.parameters = parameters;
    }

    public void runComparativeStudy() {
        for (String alg : algorithmNames) {
            waitingTimes.put(alg, new ArrayList<>());
            turnaroundTimes.put(alg, new ArrayList<>());
            cpuUtilization.put(alg, new ArrayList<>());
            throughputs.put(alg, new ArrayList<>());
        }

        for (int i = 0; i < parameters.replications; i++) {
            // 1. GENERATE STOCHASTIC WORKLOAD (New random inputs for each replication)
            JobGenerator generator = new JobGenerator();
            List<PCB> workload = generator.generateWorkload(
                parameters.numJobs, parameters.meanInterArrival, parameters.meanBurst, parameters.stdDevBurst, parameters.maxPriority
            );
            
            // 2. RUN ALL ALGORITHMS
            runSingleReplication(workload);
        }

        // 3. STATISTICAL ANALYSIS & REPORTING
        System.out.println();
        System.out.println();
        System.out.println("=== Statistical Analysis (Based on " + parameters.replications + " Replications) ===");
        analyzeAndReportResults("Average Waiting Time (ms)", waitingTimes);
        analyzeAndReportResults("Average Turnaround Time (ms)", turnaroundTimes);
        analyzeAndReportResults("CPU Utilization (%)", cpuUtilization);
        analyzeAndReportResults("Throughput (jobs/sec)", throughputs);

        // 4. DATA EXPORT
        exportToCSV();
    }
    
    private void runSingleReplication(List<PCB> originalWorkload) {
        // --- FCFS ---
        FirstComeFirstServed fcfs = new FirstComeFirstServed(deepCopy(originalWorkload), parameters.contextSwitchTime);
        fcfs.schedule();
        Scheduler.SimulationResult resFCFS = fcfs.CollectMetrics();
        waitingTimes.get("FCFS").add(resFCFS.avgWaitingTime);
        turnaroundTimes.get("FCFS").add(resFCFS.avgTurnAroundTime);
        cpuUtilization.get("FCFS").add(resFCFS.cpuUtilization);
        throughputs.get("FCFS").add(resFCFS.throughput);

        // --- Priority (Non-Preemptive) ---
        PriorityScheduling priority = new PriorityScheduling(deepCopy(originalWorkload), parameters.contextSwitchTime);
        priority.non_preemptive_schedule();
        Scheduler.SimulationResult resPri = priority.CollectMetrics();
        waitingTimes.get("Priority-NP").add(resPri.avgWaitingTime);
        turnaroundTimes.get("Priority-NP").add(resPri.avgTurnAroundTime);
        cpuUtilization.get("Priority-NP").add(resPri.cpuUtilization);
        throughputs.get("Priority-NP").add(resPri.throughput);

        // --- SJF (Non-Preemptive) ---
        ShortestJobFirst sjf = new ShortestJobFirst(deepCopy(originalWorkload), parameters.contextSwitchTime);
        sjf.non_preemptive_schedule();
        Scheduler.SimulationResult resSJF = sjf.CollectMetrics();
        waitingTimes.get("SJF-NP").add(resSJF.avgWaitingTime);
        turnaroundTimes.get("SJF-NP").add(resSJF.avgTurnAroundTime);
        cpuUtilization.get("SJF-NP").add(resSJF.cpuUtilization);
        throughputs.get("SJF-NP").add(resSJF.throughput);
        
        // --- Round Robin ---
        RoundRobin rr = new RoundRobin(deepCopy(originalWorkload), parameters.timeQuantum, parameters.contextSwitchTime);
        rr.schedule();
        Scheduler.SimulationResult resRR = rr.CollectMetrics();
        waitingTimes.get("RoundRobin").add(resRR.avgWaitingTime);
        turnaroundTimes.get("RoundRobin").add(resRR.avgTurnAroundTime);
        cpuUtilization.get("RoundRobin").add(resRR.cpuUtilization);
        throughputs.get("RoundRobin").add(resRR.throughput);
    }

    // --- STATISTICAL METHODS ---
    private void analyzeAndReportResults(String metricName, Map<String, List<Double>> results) {
        System.out.println("\n--- Metric: " + metricName + " ---");
        for (Map.Entry<String, List<Double>> entry : results.entrySet()) {
            String alg = entry.getKey();
            List<Double> values = entry.getValue();
            
            double mean = values.stream().mapToDouble(val -> val).average().orElse(0.0);
            double sumOfSquares = 0.0;
            for (double num : values) sumOfSquares += Math.pow(num - mean, 2);
            double stdDev = Math.sqrt(sumOfSquares / (values.size() - 1)); // Sample StdDev

            // 95% Confidence Interval (t-distribution critical value for N=30 is approx 2.045)
            // For large N, we use Z=1.96, but t is more accurate for N=30.
            double tValue = 2.045; // Critical value for 29 degrees of freedom
            double marginOfError = tValue * (stdDev / Math.sqrt(values.size()));

            System.out.println("  " + alg + ": " + String.format("%.2f", mean) + " ± " + String.format("%.2f", marginOfError) + " (95% CI)");
        }
    }

    // --- DATA EXPORT METHOD (The Python Bridge) ---
    private void exportToCSV() {
        try (FileWriter writer = new FileWriter(OUTPUT_FILE)) {
            // Write Header
            writer.write("Replication,Algorithm,Metric,Value\n");

            // Write Data
            for (int i = 0; i < parameters.replications; i++) {
                for (String alg : algorithmNames) {
                    writer.write(String.format("%d,%s,%s,%.2f\n", i, alg, "AvgWaitingTime", waitingTimes.get(alg).get(i)));
                    writer.write(String.format("%d,%s,%s,%.2f\n", i, alg, "AvgTurnaroundTime", turnaroundTimes.get(alg).get(i)));
                    writer.write(String.format("%d,%s,%s,%.2f\n", i, alg, "CPUUtilization", cpuUtilization.get(alg).get(i)));
                    writer.write(String.format("%d,%s,%s,%.2f\n", i, alg, "Throughput", throughputs.get(alg).get(i)));
                }
            }
            System.out.println("\nData successfully exported to " + OUTPUT_FILE + " for Python analysis.");

        } catch (IOException e) {
            System.err.println("Error writing CSV file: " + e.getMessage());
        }
    }

    // Helper for deep copying
    private static List<PCB> deepCopy(List<PCB> original) {
        List<PCB> copy = new ArrayList<>();
        for (PCB p : original) {
            copy.add(p.clone());
        }
        return copy;
    }
}