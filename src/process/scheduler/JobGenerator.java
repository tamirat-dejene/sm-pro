package process.scheduler;

import java.util.List;
import java.util.Random;

public class JobGenerator {
    private Random random;

    public JobGenerator() {
        this.random = new Random();
    }

    /**
     * Generates a synthetic workload of processes based on probability distributions.
     * * @param numJobs Number of jobs to generate
     * @param meanInterArrival Mean time between arrivals (exponential distribution)
     * @param meanBurst Mean CPU burst time (normal distribution)
     * @param burstStdDev Standard deviation for CPU burst time
     * @param maxPriority Maximum priority value (Uniform distribution from 1 to maxPriority)
     * @return List of stochastic processes (PCBs)
     */
    public java.util.List<PCB> generateWorkload(int numJobs, double meanInterArrival, double meanBurst,
            double burstStdDev, int maxPriority) {
        
        List<PCB> workload = new java.util.ArrayList<>();
        int currentArrivalTime = 0;

        for (int i = 1; i <= numJobs; i++) {
            // 1. Inter-arrival Time: Exponential Distribution
            // Formula: Time = -Mean * ln(1 - u) where u is a uniform(0,1) random number
            double u = random.nextDouble();
            int interArrivalTime = (int) (-meanInterArrival * Math.log(1 - u));
            // Ensure at least 0 arrival time spacing
            if (interArrivalTime < 0)
                interArrivalTime = 0;
            
            currentArrivalTime += interArrivalTime;

            // 2. Burst Time: Normal Distribution
            // Uses Box-Muller transform internally via nextGaussian()
            int burstTime = (int) (random.nextGaussian() * burstStdDev + meanBurst);
            if (burstTime < 1) // CPU burst time must be at least 1
                burstTime = 1;

            // 3. Priority: Uniform Distribution
            int priority = random.nextInt(maxPriority) + 1;

            // Create PCB and add to workload
            String pid = "P" + i;
            workload.add(new PCB(pid, burstTime, currentArrivalTime, priority));
        }

        return workload;

    }
    
}
