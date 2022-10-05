package task1.decorators.pizza;

public class Pesto extends Pizza {
    private final int cost = 2700;
    public Pesto(String description) {
        this.description = description;
    }

    @Override
    public int getCost() {
        return cost;
    }
}
