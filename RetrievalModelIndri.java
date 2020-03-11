import java.util.Map;

public class RetrievalModelIndri extends RetrievalModel {
    private double mu;
    private double lambda;

    public double getMu() {
        return mu;
    }

    public double getLambda() {
        return lambda;
    }

    public void setParameters(Map<String, String> parameters){
        mu = Double.valueOf(parameters.get("Indri:mu"));
        lambda = Double.valueOf(parameters.get("Indri:lambda"));
    }

    @Override
    public String defaultQrySopName() {
        return "#and";
    }
}
