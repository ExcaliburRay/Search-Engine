import java.io.IOException;

public class QrySopSum extends QrySop{


    @Override
    public double getScore(RetrievalModel r) throws IOException {
        if(r instanceof RetrievalModelBM25){
            return this.getScoreBM25(r);
        }else {
            throw new IllegalArgumentException
                    (r.getClass().getName() + " doesn't support the ALL operator.");
        }
    }

    @Override
    public double getDefaultScore(RetrievalModel r, Long docid) throws IOException {
        return 0;
    }

    @Override
    public boolean docIteratorHasMatch(RetrievalModel r) {
        return this.docIteratorHasMatchMin(r);
    }

    private double getScoreBM25(RetrievalModel r) throws IOException {
        double score = 0;
        int length = this.args.size();
        int docid =this.docIteratorGetMatch();
        if (!this.docIteratorHasMatchCache()) {
            score = 0.0;
        } else {
            for (int i = 0; i < length; i++) {
                QrySop q = (QrySop) this.args.get(i);
                if(q.docIteratorHasMatch(r) && q.docIteratorGetMatch() == docid){
                    score += q.getScore(r);
                }
            }
        }
        return score;
    }
}
