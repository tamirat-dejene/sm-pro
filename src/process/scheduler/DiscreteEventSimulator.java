package process.scheduler;

public class DiscreteEventSimulator {
    public static void main(String[] args) {
        ExperimentManager.HyperParameters realisticParams = new ExperimentManager.HyperParameters(
                10,
                5.0,
                8.0,                        
                2.0,
                5,
                3,
                1,                         
                30
        );
        ExperimentManager manager = new ExperimentManager(realisticParams);
        manager.runComparativeStudy();
    }
}