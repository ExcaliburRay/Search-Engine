/**
 * Copyright (c) 2020, Carnegie Mellon University.  All Rights Reserved.
 */

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * The NEAR operator for all retrieval models.
 */
public class QryIopNear extends QryIop {

    private int distance;

    public QryIopNear(String distance) {
        this.distance = Integer.valueOf(distance);
    }

    public int getDistance() {
        return distance;
    }

    /**
     * Evaluate the query operator; the result is an internal inverted
     * list that may be accessed via the internal iterators.
     *
     * @throws IOException Error accessing the Lucene index.
     */
    protected void evaluate() throws IOException {

        //  Create an empty inverted list.  If there are no query arguments,
        //  this is the final result.

        this.invertedList = new InvList(this.getField());

        if (args.size() == 0) {
            return;
        }

        while (this.docIteratorHasMatchAll(null)) {
            //compare with others docs location information
            QryIop query = this.getArg(0);
            int init_id = query.docIteratorGetMatch();
            List<Integer> init_loc = query.docIteratorGetMatchPosting().positions;
            //find out the final match result, only store the right location
            for (int i = 1; i < this.args.size(); i++) {
                QryIop remain_i = this.getArg(i);
                List<Integer> remain_i_loc = remain_i.docIteratorGetMatchPosting().positions;
                List<Integer> loc_match = greedy(init_loc, remain_i_loc);
                init_loc = loc_match;
            }
            if(init_loc.size()>0)this.invertedList.appendPosting(init_id, init_loc);
            this.getArg(0).docIteratorAdvancePast(init_id);
        }
    }


    public List<Integer> greedy(List<Integer> init_loc, List<Integer> remain_i_loc){
        List<Integer> result = new ArrayList<>();
        int init_locCursor = 0;
        int remain_iCursor = 0;
        while(init_locCursor<init_loc.size() && remain_iCursor<remain_i_loc.size()) {
            int locOne = init_loc.get(init_locCursor);
            int locTwo = remain_i_loc.get(remain_iCursor);
            if (locOne < locTwo && locTwo - locOne <= getDistance()) {
                result.add(remain_i_loc.get(remain_iCursor));
                init_locCursor++;
                remain_iCursor++;
            } else if (locOne > locTwo) {
                remain_iCursor++;
            } else {
                init_locCursor++;
            }
        }
        return result;
    }
}


