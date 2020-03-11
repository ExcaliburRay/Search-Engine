/**
 * Copyright (c) 2020, Carnegie Mellon University.  All Rights Reserved.
 */

import java.io.IOException;

/**
 * The SCORE operator for all retrieval models.
 */
public class QrySopScore extends QrySop {

    /**
     *  Document-independent values that should be determined just once.
     *  Some retrieval models have these, some don't.
     */

    /**
     * Indicates whether the query has a match.
     *
     * @param r The retrieval model that determines what is a match
     * @return True if the query matches, otherwise false.
     */
    public boolean docIteratorHasMatch(RetrievalModel r) {
        return this.docIteratorHasMatchFirst(r);
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
        } else if (r instanceof RetrievalModelBM25){
            return this.getScoreBM25(r);
        } else if (r instanceof RetrievalModelIndri){
            return this.getScoreIndri(r);
        }

        else {
            throw new IllegalArgumentException
                    (r.getClass().getName() + " doesn't support the SCORE operator.");
        }
    }


    /**
     * getScore for the Unranked retrieval model.
     *
     * @param r The retrieval model that determines how scores are calculated.
     * @return The document score.
     * @throws IOException Error accessing the Lucene index
     */
    public double getScoreUnrankedBoolean(RetrievalModel r) throws IOException {
        if (!this.docIteratorHasMatchCache()) {
            return 0.0;
        } else {
            return 1.0;
        }
    }

    /**
     * Initialize the query operator (and its arguments), including any
     * internal iterators.  If the query operator is of type QryIop, it
     * is fully evaluated, and the results are stored in an internal
     * inverted list that may be accessed via the internal iterator.
     *
     * @param r A retrieval model that guides initialization
     * @throws IOException Error accessing the Lucene index.
     */
    public void initialize(RetrievalModel r) throws IOException {

        Qry q = this.args.get(0);
        q.initialize(r);
    }

    /**
     * getScore for the Ranked retrieval model.
     * @param r The retrieval model that determines how scores are calculated.
     * @return The document score.
     * @throws IOException Error accessing the Lucene index
     */
    private double getScoreRankedBoolean(RetrievalModel r) throws IOException {
        Qry q = this.getArg(0);
        double score;
        if (!this.docIteratorHasMatchCache()) {
            //get the score base on the term frequency.
            score = 0.0;
        } else {
            //term frequency
            InvList.DocPosting list = ((QryIop) q).docIteratorGetMatchPosting();
            score = list.tf;
        }
        return score;
    }

    private double getScoreBM25(RetrievalModel r) throws IOException {
        Qry q = this.getArg(0);
        if (!this.docIteratorHasMatchCache()) {
            return 0.0;
        }else{
            // number of documents in the data set
            Long N = Idx.getNumDocs();
            //document frequency
            double df = ((QryIop) q).getDf();
            //term frequency
            InvList.DocPosting list = ((QryIop) q).docIteratorGetMatchPosting();
            double tf = list.tf;
            //document id
            int docid = q.docIteratorGetMatch();
            //retrieved document field
            String field = ((QryIop) q).getField();
            //total number of word occurrences in document d
            double doclen = Idx.getFieldLength(field,docid);
            //total number of word occurrences in collection c
            double collection_len = Idx.getSumOfFieldLengths(field);
            //the quantity of documents retrieved in this field
            long document_quantity = Idx.getDocCount(field);
            //the length of average document length
            double avg_doclen = collection_len/document_quantity;
            //query term frequency
            int qtf = 1;
            //parameters in the BM25 Model
            double k1 = ((RetrievalModelBM25)r).getK1();
            double b = ((RetrievalModelBM25)r).getB();
            double k3 = ((RetrievalModelBM25)r).getK3();
            //three part which constitute the formula of BM25,
            //these are RSJ Weight which also means idf, inverted document frequency
            //term frequency weight and user weight
            double RSJ_Weight = Math.log((N-df+0.5)/(df+0.5));
            //handing this situation when retrieved document frequency higher than 1/2
            double modified_RSJ_Weight = Math.max(0,RSJ_Weight);
            double tf_weight = tf/(tf+k1*((1-b)+b*(doclen/avg_doclen)));
            double user_weight = ((k3+1)*qtf)/(k3+qtf);
            //BM 25 formula
            double BM25_Score = modified_RSJ_Weight*tf_weight*user_weight;
            return BM25_Score;
        }

    }


    private double getScoreIndri(RetrievalModel r) throws IOException {

        Qry q = this.getArg(0);
        //two stage smoothing parameters
        double mu = ((RetrievalModelIndri)r).getMu();
        double lambda = ((RetrievalModelIndri)r).getLambda();
        //the term frequency of term q in the document d
        InvList.DocPosting list = ((QryIop) q).docIteratorGetMatchPosting();
        double tf = list.tf;
        //the term frequency of term q in the entire collection
        double ctf = ((QryIop) q).getCtf();
        //retrieved document field
        String field = ((QryIop) q).getField();
        //document id
        int docid = q.docIteratorGetMatch();
        //total number of word occurrences in document d
        double doclen = Idx.getFieldLength(field,docid);
        //total number of word occurrences in collection c
        double collection_len = Idx.getSumOfFieldLengths(field);
        //Maximum likelihood Estimation
        double MLE_collection = ctf/collection_len;
        //Indri Score Formula
        double IndriScore = (1-lambda)*((tf+mu*MLE_collection)/(doclen+mu))+lambda*MLE_collection;
        return IndriScore;

    }

    @Override
    public double getDefaultScore(RetrievalModel r, Long docid) throws IOException {
        Qry q = this.getArg(0);
        //two stage smoothing parameters
        double mu = ((RetrievalModelIndri)r).getMu();
        double lambda = ((RetrievalModelIndri)r).getLambda();
        //the term frequency of term q would be 0
        double tf = 0.0;
        //the term frequency of term q in the entire collection
        double ctf = ((QryIop) q).getCtf();
        ctf = Math.max(0.5,ctf);
        //retrieved document field
        String field = ((QryIop) q).getField();
        //total number of word occurrences in document d
        double doclen = Idx.getFieldLength(field,docid.intValue());
        //total number of word occurrences in collection c
        double collection_len = Idx.getSumOfFieldLengths(field);
        //Maximum likelihood Estimation
        double MLE_collection = ctf/collection_len;
        //Indri Score Formula

        double defaultScore = (1-lambda)*((tf+mu*MLE_collection)/(doclen+mu))+lambda*MLE_collection;
        return defaultScore;
    }

}
