import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class QrySopWSum extends QrySop {

    public List<Double> weightList = new ArrayList<>();

    public void AddIntoWeightList(double weight) {
        weightList.add(weight);
    }

    public double sumOfWeight(List<Double> list){
        double sum = 0;
        for(int i=0;i<list.size();i++){
            sum+=list.get(i);
        }
        return sum;
    }

    @Override
    public double getScore(RetrievalModel r) throws IOException {
        if(r instanceof RetrievalModelIndri){
            return this.getScoreIndri(r);
        }else {
            throw new IllegalArgumentException
                    (r.getClass().getName() + " doesn't support the ALL operator.");
        }
    }

    private double getScoreIndri(RetrievalModel r) throws IOException {
        double score = 0;
        long docid = this.docIteratorGetMatch();
        //length of the query
        int length = this.args.size();
        for (int i = 0; i < length; i++) {
            QrySop q = (QrySop) this.args.get(i);
            double weight = weightList.get(i);
            if (q.docIteratorHasMatch(r) && q.docIteratorGetMatch() == docid) {
                score += weight / sumOfWeight(weightList) * q.getScore(r);
            } else {
                score += weight / sumOfWeight(weightList) * q.getDefaultScore(r, docid);
            }
        }
        return score;
    }

    @Override
    public double getDefaultScore(RetrievalModel r, Long docid) throws IOException {
        double score = 0;
        int length = this.args.size();
        for(int i=0;i<length;i++){
            double weight = weightList.get(i);
            QrySop q = (QrySop) this.args.get(i);
            score+=weight/sumOfWeight(weightList)*q.getDefaultScore(r,docid);
        }
        return score;

    }

    @Override
    public boolean docIteratorHasMatch(RetrievalModel r) {
        return this.docIteratorHasMatchMin(r);
    }
}
