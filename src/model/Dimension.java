package model;

import java.util.ArrayList;
import java.util.List;
//Under scenario layer second high layer
// groups multiple metrics under a single quality category (e.g. Usability, Reliability)
// example object: new Dimension("Usability", 25)

public class Dimension {
    private final String name;
    private final int coefficient;
    private final List<Metric> metrics;

    public Dimension(String name, int coefficient) {
        this.name = name;
        this.coefficient = coefficient;
        this.metrics = new ArrayList<>();
    }

    public void addMetric(Metric metric) {
        metrics.add(metric);
    }

    
    //Calculates the weighted average score across all metrics in this dimension
    //formula: sum(score * coeff) / sum(coeff)
    
    public double calculateScore() {
        double weightedSum = 0;
        double totalCoeff = 0;
        for (Metric m : metrics) {
            weightedSum += m.calculateScore() * m.getCoefficient();
            totalCoeff += m.getCoefficient();
        }
        return totalCoeff == 0 ? 0 : weightedSum / totalCoeff;
    }

    public String getName(){
        return name;
    
}

    public int getCoefficient(){
        return coefficient;
    }

    public List<Metric> getMetrics(){
        return metrics;
    }
}
