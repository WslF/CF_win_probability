package wsl_f.cf_win_probability;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import javafx.util.Pair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Wsl_F
 */
public class CodeForcesAPI {

    /**
     *
     * @param contestId codeforces contest id (!!! not equals cf round number)
     * @return rating changes
     */
    public static JSONObject getRatingChanges(int contestId) {
        try {
            JSONObject obj = JsonReader.read("http://codeforces.com/api/contest.ratingChanges?contestId=" + contestId);
            if (obj != null) {
                if (obj.getString("status").equals("OK")) {
                    return obj;
                }
            }
        } catch (IOException | JSONException exception) {
            System.err.println("Failed contestId: " + contestId);
            System.err.println(exception.getMessage());
        }

        return null;
    }

    /**
     *
     * @param division number of division (1 or 2)
     * @param minId minimum number of contest id
     * @param maxId maximum number of contest id
     * @return list of finished contests, id in range
     * [{@code minId},{@code  maxId}]
     */
    public static ArrayList<Integer> getContestsList(int division, int minId, int maxId) {
        ArrayList<Integer> contestsIds = new ArrayList<>();
        String goodEnd = "(Div. " + division + ")";
        try {
            TimeUnit.MILLISECONDS.sleep(250); // 1/4 sec
            JSONObject obj = JsonReader.read("http://codeforces.com/api/contest.list?gym=false");

            if (obj != null && obj.getString("status").equals("OK")) {
                JSONArray array = obj.getJSONArray("result");
                for (int i = array.length() - 1; i >= 0; i--) {
                    JSONObject contest = (JSONObject) array.get(i);
                    if (contest.getString("phase").equals("FINISHED")
                            && contest.getString("type").equals("CF")
                            && contest.getString("name").endsWith(goodEnd)) {
                        int contestId = contest.getInt("id");
                        if (contestId >= minId && contestId <= maxId) {
                            contestsIds.add(contestId);
                        }
                    }
                }
            }

        } catch (InterruptedException | IOException | JSONException ex) {
            System.err.println("Failed get contests list");
            System.err.println(ex.getMessage());
            return new ArrayList<>();
        }

        return contestsIds;
    }

    /**
     * get users rank and rating by contestId.
     *
     * If get any Exception, return empty list.
     *
     * @param contestId contestId (!!!not number of cf round)
     * @return ArrayList<Pair<user rank in contest, user rating before contest>>
     */
    public static ArrayList<Pair<Integer, Integer>> getContestResults(int contestId) {
        ArrayList<Pair<Integer, Integer>> results = new ArrayList<>();

        try {
            JSONObject obj = getRatingChanges(contestId);
            if (obj == null) {
                return results;
            }

            JSONArray array = obj.getJSONArray("result");

            int l = array.length();
            for (int i = 0; i < l; i++) {
                JSONObject user = (JSONObject) array.get(i);
                Pair<Integer, Integer> pair = new Pair<>(user.getInt("rank"), user.getInt("oldRating"));
                results.add(pair);
            }
        } catch (Exception ex) {
            System.err.println("Failed get contest results! contestId: " + contestId);
            System.err.println(ex.getMessage());
            return new ArrayList<>();
        }

        return results;
    }

}