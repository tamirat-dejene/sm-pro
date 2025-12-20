# Discrete-Event CPU Scheduling Simulator (SMP-Implementation)

This project implements a discrete-event simulator to study and compare classical CPU scheduling algorithms under stochastic workloads. It generates synthetic process workloads, runs multiple scheduling strategies, and reports core performance metrics along with CSV outputs suitable for further analysis in Jupyter notebooks and LaTeX reports.

## Overview
- **Workload generation:** Synthetic jobs are produced using statistical distributions (exponential inter-arrival, normal burst, uniform priority) via [src/process/scheduler/JobGenerator.java](src/process/scheduler/JobGenerator.java).
- **Schedulers implemented:**
	- FCFS: [src/process/scheduler/FirstComeFirstServed.java](src/process/scheduler/FirstComeFirstServed.java)
	- Priority (Non-Preemptive): [src/process/scheduler/PriorityScheduling.java](src/process/scheduler/PriorityScheduling.java)
	- SJF (Non-Preemptive): [src/process/scheduler/ShortestJobFirst.java](src/process/scheduler/ShortestJobFirst.java)
	- Round Robin (Preemptive): [src/process/scheduler/RoundRobin.java](src/process/scheduler/RoundRobin.java)
- **Experiment orchestration & metrics:** Comparative runs and statistical aggregation happen in [src/process/scheduler/ExperimentManager.java](src/process/scheduler/ExperimentManager.java).
- **Scenarios:** Predefined parameter sets for different contention levels live in [src/process/scheduler/DiscreteEventSimulator.java](src/process/scheduler/DiscreteEventSimulator.java) and run back-to-back.
- **Outputs:** CSV files ([simulation_results.csv](simulation_results.csv), [generated_workloads.csv](generated_workloads.csv)) feed notebooks like [analysis.ipynb](analysis.ipynb) and [sim.ipynb](sim.ipynb) and the LaTeX report in [docs/Report.tex](docs/Report.tex).

## Repository Layout
- [src/process/scheduler/](src/process/scheduler/)
	- `Scheduler.java`: Base class that manages common data structures (waiting/turnaround/completion times, schedule table) and computes extended metrics (CPU utilization, throughput).
	- `PCB.java`: Process Control Block defining `PID`, `burstTime`, `arrivalTime`, and `priority`, with cloning support for independent runs.
	- `JobGenerator.java`: Creates workloads using stochastic distributions and appends them to [generated_workloads.csv](generated_workloads.csv).
	- `FirstComeFirstServed.java`: Non-preemptive FCFS implementation.
	- `PriorityScheduling.java`: Non-preemptive priority-based scheduling; lower numbers indicate higher priority.
	- `ShortestJobFirst.java`: Non-preemptive SJF; breaks ties by arrival time, then burst time.
	- `RoundRobin.java`: Preemptive RR with configurable time quantum and context-switch overhead.
	- `ExperimentManager.java`: Runs replications per algorithm, aggregates statistics, and writes [simulation_results.csv](simulation_results.csv).
	- `DiscreteEventSimulator.java`: Entry point defining three scenarios and invoking comparative studies.
- Notebooks & data
	- [analysis.ipynb](analysis.ipynb), [sim.ipynb](sim.ipynb), [sim_script.ipynb](sim_script.ipynb): Analysis and visualization of CSV outputs.
	- [simulation_results.csv](simulation_results.csv): Metrics per replication per algorithm.
	- [generated_workloads.csv](generated_workloads.csv): Raw workload data per replication.
	- [descriptive_stats.csv](descriptive_stats.csv): Example aggregated statistics produced by notebooks.
- Documentation: [docs/Report.tex](docs/Report.tex) and generated LaTeX artifacts in [docs/tex_out/](docs/tex_out/).

## Simulation Scenarios
Defined in [src/process/scheduler/DiscreteEventSimulator.java](src/process/scheduler/DiscreteEventSimulator.java):
- **Scenario 1 — Low Contention (Baseline):** `meanInterArrival=10ms`, `meanBurst=8ms`, `stdDevBurst=2ms`, `timeQuantum=5ms`, `contextSwitch=0ms`, `replications=100`.
- **Scenario 2 — High Contention (Stress Test):** `meanInterArrival=2ms` (saturated load), `meanBurst=8ms`, `stdDevBurst=2ms`, `timeQuantum=5ms`, `contextSwitch=0ms`, `replications=100`.
- **Scenario 3 — Realistic Load (Overhead Analysis):** `meanInterArrival=5ms`, `meanBurst=8ms`, `stdDevBurst=2ms`, `timeQuantum=5ms`, `contextSwitch=1ms`, `replications=100`.

Each scenario runs all algorithms and produces aggregate statistics with 95% confidence intervals.

## How It Works
1. **Workload generation:** [JobGenerator](src/process/scheduler/JobGenerator.java) creates `numJobs` PCBs with stochastic arrival, burst, and priority. Replication ID is used to structure CSV headers.
2. **Algorithm runs:** For each replication, the same workload is deep-copied and passed to each scheduler to ensure independence.
3. **Metrics collected:**
	 - Average Waiting Time (ms)
	 - Average Turnaround Time (ms)
	 - CPU Utilization (%) — based on CPU busy time over makespan
	 - Throughput (jobs/ms)
4. **Statistical reporting:** Means and 95% CIs are printed per metric and algorithm.
5. **CSV export:** [ExperimentManager](src/process/scheduler/ExperimentManager.java) writes [simulation_results.csv](simulation_results.csv) in long format (`Replication,Algorithm,Metric,Value`). [JobGenerator](src/process/scheduler/JobGenerator.java) writes [generated_workloads.csv](generated_workloads.csv).

## Build & Run
Prerequisites: Linux, JDK 11+ (any modern JDK should work). No Maven/Gradle required.

Compile all sources to an `out` directory and run the simulator:

```bash
# From repo root
javac -d out src/process/scheduler/*.java
java -cp out process.scheduler.DiscreteEventSimulator
```

You can also run the FCFS class’s inline example:

```bash
java -cp out process.scheduler.FirstComeFirstServed
```

### Adjusting Parameters
Edit [DiscreteEventSimulator.java](src/process/scheduler/DiscreteEventSimulator.java) to tweak `ExperimentManager.Parameters` for scenarios (e.g., number of jobs, time quantum, context-switch time, replications).

## Outputs
- [simulation_results.csv](simulation_results.csv)
	- Columns: `Replication, Algorithm, Metric, Value`
	- Metrics: `AvgWaitingTime`, `AvgTurnaroundTime`, `CPUUtilization`, `Throughput`
- [generated_workloads.csv](generated_workloads.csv)
	- Columns: `ReplicationID, JobID, ArrivalTime, BurstTime, Priority`
- Optional: [descriptive_stats.csv](descriptive_stats.csv) generated by notebooks, not by Java code.

## Analysis Notebooks
- Open [analysis.ipynb](analysis.ipynb) or [sim.ipynb](sim.ipynb) to visualize distributions, compare algorithms across scenarios, and render tables/plots suitable for the LaTeX report.
- Typical workflow:
	1. Run the Java simulator to regenerate CSVs.
	2. Load CSVs in notebooks and compute descriptive statistics (means, CIs, violin plots, etc.).
	3. Export figures/tables used by [docs/Report.tex](docs/Report.tex).

## Design Notes
- **Isolation of runs:** Each algorithm receives a deep copy of the workload ([Scheduler.schedule](src/process/scheduler/Scheduler.java#L120-L168)) to avoid shared-state artifacts.
- **Context switching:** RR and other algorithms can include context-switch overhead via `contextSwitchTime` to reflect realistic preemption costs.
- **Extensibility:** To add an algorithm:
	1. Create a class extending `Scheduler`.
	2. Implement `schedule()` (preemptive) or call `Scheduler.schedule(...)` with appropriate comparators (non-preemptive) like in Priority/SJF.
	3. Add it to `algorithmNames` in [ExperimentManager](src/process/scheduler/ExperimentManager.java) and integrate it into `runSingleReplication(...)`.

## Reproducibility
- Randomness uses Java’s `Random` without a fixed seed; results vary across runs. For exact reproducibility, add a fixed seed to `JobGenerator` or parameterize it.
- Replications (`Parameters.replications`) reduce variance; CIs use a t-critical value (~2.045 around N≈30). With `N=100`, CI width narrows.

## Troubleshooting
- If `java` cannot find classes, ensure you compiled with `-d out` and run with `-cp out`.
- CSV files are written to the repo root; ensure you have write permissions.
- Large replications increase runtime; start with smaller `numJobs` or `replications` when iterating.

## License & Attribution
This academic simulator is intended for study and experimentation with scheduling policies. No explicit license is provided; please request permission for broader use if needed.

