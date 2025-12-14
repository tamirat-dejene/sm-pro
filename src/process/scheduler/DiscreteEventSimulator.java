package process.scheduler;

public class DiscreteEventSimulator {
    public static void main(String[] args) {
        // --- SCENARIO 1: LOW Contention (Baseline) ---
        // Objective: Establish performance where the CPU is mostly idle.
        // IAT (10.0ms) > Burst (8.0ms) -> Low Load
        ExperimentManager.Parameters params1 = new ExperimentManager.Parameters(
            100,  // numJobs: Standard run size
            10.0, // meanInterArrival (lambda): Low contention
            8.0,  // meanBurst (mu): Standard mean burst
            2.0,  // stdDevBurst (sigma): Standard deviation
            10,   // maxPriority: Standard priority range
            5,    // timeQuantum (TQ): Standard TQ
            0,    // contextSwitchTime (CS): Zero overhead for baseline
            100   // replications: Maximize statistical rigor
        );

        // --- SCENARIO 2: HIGH Contention (Stress Test) ---
        // Objective: Maximize queue growth and performance divergence.
        // IAT (2.0ms) << Burst (8.0ms) -> Saturated Load
        ExperimentManager.Parameters params2 = new ExperimentManager.Parameters(
            100,
            2.0,  // meanInterArrival (lambda): High contention/saturated load
            8.0,
            2.0,
            10,
            5,
            0,    // contextSwitchTime (CS): Zero overhead to isolate algorithm efficiency
            100
        );

        // --- SCENARIO 3: REALISTIC Load with Overhead (Trade-off Analysis) ---
        // Objective: Test a moderate load with a real-world context switch penalty.
        // IAT (5.0ms) < Burst (8.0ms) -> High but not saturated load
        ExperimentManager.Parameters params3 = new ExperimentManager.Parameters(
            100,
            5.0,  // meanInterArrival (lambda): Moderate/High load
            8.0,
            2.0,
            10,
            5,    // timeQuantum (TQ): Kept reasonable (5ms)
            1,    // contextSwitchTime (CS): Realistic 1ms overhead for RR
            100
        );


        // --- Run Scenarios (Select which one to run) ---
        System.out.println("Starting SCENARIO 1: Low Contention Study...");
        ExperimentManager manager1 = new ExperimentManager(params1);
        manager1.runComparativeStudy();

        System.out.println("\nStarting SCENARIO 2: High Contention Study...");
        ExperimentManager manager2 = new ExperimentManager(params2);
        manager2.runComparativeStudy();

        System.out.println("\nStarting SCENARIO 3: Realistic Load with Overhead Study...");
        ExperimentManager manager3 = new ExperimentManager(params3);
        manager3.runComparativeStudy();
    }
}