import java.util.Map;

public class RetrievalModelBM25 extends RetrievalModel {

    private double k1,b,k3;

    public double getK1() {
        return k1;
    }

    public double getB() {
        return b;
    }

    public double getK3() {
        return k3;
    }

    public void setParameters(Map<String, String> parameters) {
        k1 = Double.valueOf(parameters.get("BM25:k_1"));
        b = Double.valueOf(parameters.get("BM25:b"));
        k3 = Double.valueOf(parameters.get("BM25:k_3"));
    }

    @Override
    public String defaultQrySopName() {
        return ("#sum");
    }
}
