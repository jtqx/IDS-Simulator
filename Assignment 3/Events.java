import java.util.Random;

public class Events {
    private String name;
    private char type;
    private double min;
    private double max;
    private double weight;
    private Stats stats;

    public Events(String name, char type, double min, double max, double weight) {
        this.name = name;
        this.type = type;
        this.min = min;
        this.max = max;
        this.weight = weight;
        this.stats = new Stats();
    }

    public Events(String name, char type, double min, double max, double weight, Stats stats) {
        this.name = name;
        this.type = type;
        this.min = min;
        this.max = max;
        this.weight = weight;
        this.stats = stats;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public char getType() {
        return type;
    }

    public void setType(char type) {
        this.type = type;
    }

    public double getMin() {
        return min;
    }

    public void setMin(double min) {
        this.min = min;
    }

    public double getMax() {
        return max;
    }

    public void setMax(double max) {
        this.max = max;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public Stats getStats() {
        return stats;
    }

    public void setStats(Stats stats) {
        this.stats = stats;
    }

    public double getRandomize(){
        Random rand = new Random();
        boolean correctValue = false;
        double value = 0 ;

        while (!correctValue) {
            value =  stats.getMean() + stats.getStd() * rand.nextGaussian();
            
            if (type == 'D')
                value = Math.round(value);
            
            if (value >= min && value <= max) {
                correctValue = true;
            }
        }
        return value;
    }
}
