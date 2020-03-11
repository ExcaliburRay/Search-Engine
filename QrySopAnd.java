import java.io.IOException;

public class QrySopAnd extends QrySop {
    /**
     * Indicates whether the query has a match.
     *
     * @param r The retrieval model that determines what is a match
     * @return True if the query matches, otherwise false.
     */
    public boolean docIteratorHasMatch(RetrievalModel r) {
        if(r instanceof RetrievalModelIndri){
            return this.docIteratorHasMatchMin(r);
        }
        return this.docIteratorHasMatchAll(r);
    }

    /**
     * Get a score for the document that docIteratorHasMatch matched.
     *
     * @param r The retrieval model that determines how scores are calculated.
     * @return The document score.
     * @throws IOException Error accessing the Lucene index
     */
    public double getScore(RetrievalModel r) throws IOException {

        if (r instanceof RetrievalModelUnrankedBoolean) {
            return this.getScoreUnrankedBoolean(r);
        }

        //  STUDENTS::
        //  Add support for other retrieval models here.
        else if (r instanceof RetrievalModelRankedBoolean) {
            return this.getScoreRankedBoolean(r);
        } else if (r instanceof RetrievalModelIndri){
            return this.getScoreIndri(r);
        }else {
            throw new IllegalArgumentException
                    (r.getClass().getName() + " doesn't support the ALL operator.");
        }
    }

    @Override
    public double getDefaultScore(RetrievalModel r, Long docid) throws IOException {
        double score = 1;
        int length = this.args.size();
        for(int i=0;i<length;i++){
            QrySop q = (QrySop) this.args.get(i);
            score*=Math.pow(q.getDefaultScore(r,docid),1.0/length);
        }
        return score;
    }


    /**
     * getScore for the UnrankedBoolean retrieval model.
     *
     * @param r The retrieval model that determines how scores are calculated.
     * @return The document score.
     * @throws IOException Error accessing the Lucene index
     */
    private double getScoreUnrankedBoolean(RetrievalModel r) throws IOException {
        if (!this.docIteratorHasMatchCache()) {
            return 0.0;
        } else {
            return 1.0;
        }
    }

    private double getScoreRankedBoolean(RetrievalModel r) throws IOException {
        double score = Integer.MAX_VALUE;
        int length = this.args.size();
        if (!this.docIteratorHasMatchCache()) {
            score = 0.0;
        } else {
            for(int i=0;i<length;i++){
                QrySop q = (QrySop)this.args.get(i);
                double current_score = q.getScore(r);
                if(current_score<score){
                    score = current_score;
                }
            }
        }
        return score;
    }

    private double getScoreIndri(RetrievalModel r) throws IOException {
        double score = 1;
        long docid = this.docIteratorGetMatch();
        //length of the query
        int length = this.args.size();
        for(int i=0;i<length;i++){
            QrySop q = (QrySop)this.args.get(i);
            if(q.docIteratorHasMatch(r) && q.docIteratorGetMatch() == docid){
                score *= Math.pow(q.getScore(r),1.0/length);
            }else{
                score *= Math.pow(q.getDefaultScore(r,docid),1.0/length);
            }
        }
        return score;


    }

}
