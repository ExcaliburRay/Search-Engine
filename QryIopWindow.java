import java.awt.event.MouseAdapter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QryIopWindow extends QryIop {

    private int distance;

    public QryIopWindow(String distance) {
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
        //iterate the doc


        while (this.docIteratorHasMatchAll(null)) {
            QryIop query = this.getArg(0);
            int id = query.docIteratorGetMatch();
            //iterate the location
            boolean exitsMatch = true;
            List<Integer> storage = new ArrayList<>();
            while(exitsMatch){

                int max = Integer.MIN_VALUE;
                int min = Integer.MAX_VALUE;
                int minLoc = -1;
                int maxLoc = -1;
                List<Integer> positions = new ArrayList<>();
                //find out the final match result, only store the right location
                //iterate the arguments
                for (int i = 0; i < this.args.size(); i++) {
                    QryIop q = this.getArg(i);
                    if(q.locIteratorHasMatch()){
                        positions.add(q.locIteratorGetMatch());
                    }else{
                        exitsMatch = false;
                        break;
                    }
                }
                if(exitsMatch && positions.size()== this.args.size()) {
                    max = Collections.max(positions);
                    min = Collections.min(positions);
                    minLoc = positions.indexOf(min);
                    maxLoc = positions.indexOf(max);
                    if (max - min + 1 <= getDistance()) {
                        //insert the selected location
                        storage.add(maxLoc);
                        //advanced location pos
                        for (int i = 0; i < this.args.size(); i++) {
                            QryIop q = (QryIop) this.args.get(i);
                            int curLocID = q.locIteratorGetMatch();
                            q.locIteratorAdvancePast(curLocID);
                        }
                    } else {
                        //only advanced the min location pos
                        QryIop q = (QryIop) this.args.get(minLoc);
                        int minID = q.locIteratorGetMatch();
                        q.locIteratorAdvancePast(minID);
                    }
                }
                positions.clear();
            }
            if(storage.size()>0) this.invertedList.appendPosting(id,storage);
            this.getArg(0).docIteratorAdvancePast(id);
        }

    }
}
