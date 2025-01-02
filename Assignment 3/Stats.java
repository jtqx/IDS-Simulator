public class Stats {
    private double mean;
    private double std;

    public Stats() {}

    public Stats(double mean, double std) {
        this.mean = mean;
        this.std = std;
    }

    public double getMean() {
        return mean;
    }

    public void setMean(double mean) {
        this.mean = mean;
    }
    
    public double getStd() {
        return std;
    }

    public void setStd(double std) {
        this.std = std;
    }
}
