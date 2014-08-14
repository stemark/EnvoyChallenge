package stevens.mark.envoychallenge;

/**
 * Created by mark.stevens on 8/13/14.
 */
public class GameEntry {
    public static String TABLE_NAME="games";

    public enum COLUMN {
        _id,
        game,
        image_url,
        console,
        finished
    }

    public static final String[] PROJECTION = new String[ COLUMN.values().length];
    static {
        for (COLUMN e : COLUMN.values()){PROJECTION[e.ordinal()]=e.name();}
    }
}
