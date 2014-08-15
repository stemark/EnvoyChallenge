package stevens.mark.envoychallenge;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.text.TextUtils;

import java.util.List;

public class GamesProvider extends ContentProvider {
    private static String authority= null;
    private final UriMatcher dataUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);


    public static enum Endpoint {
        GAME           ("vnd.stevens.mark.game"),    // content://authority/game
        GAME_ID        ("vnd.stevens.mark.game");    // content://authority/game/#

        private String mimeSubType;
        private boolean hasId;
        Endpoint(String mimeSubtype){
            this.mimeSubType = mimeSubtype;
            this.hasId = name().endsWith("_ID");
        }

        private boolean hasId() { return hasId; }
        public String asPath(){
            // translate ENUM name to the lowercase Uri path, mapping _ID,_ANY,_ to #,*,/ punctuation marks
            return name().replaceAll("_ID", "/#").replaceAll("_ANY","/*").replaceAll("_","/").toLowerCase();
        }
        public Uri.Builder buildUpon() {
            String path = name().replaceAll("_ID", "").replaceAll("_ANY","").replaceAll("_","/").toLowerCase();
            return new Uri.Builder().scheme("content").authority(authority).path(path);
        }
        public String getMimeSubType(){
            return mimeSubType;
        }
        public String getMimeType(){
            String prefix = hasId ? ContentResolver.CURSOR_ITEM_BASE_TYPE : ContentResolver.CURSOR_DIR_BASE_TYPE;
            return prefix + "/" + getMimeSubType();
        }
        static private Endpoint get(int match){
            return match >=0 ? values()[match] : null;
        }
    }

    protected SQLiteOpenHelper mDatabase = null;



    public GamesProvider() {
    }

    @Override
    public void attachInfo(Context context, ProviderInfo info) {
        super.attachInfo(context, info);
        authority = info.authority;

        for (Endpoint endpoint : Endpoint.values()){
            dataUriMatcher.addURI(info.authority, endpoint.asPath(), endpoint.ordinal());
        }
        mDatabase = new GameDataDBHelper(context );
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int deleted = 0;

        int match = dataUriMatcher.match(uri);
        Endpoint endpoint = Endpoint.get(match);
        if (endpoint== null)
            throw new IllegalArgumentException("Invalid URI: " + uri);

        notifyChange(uri);  // let ContentResolver know that the update is finished
        return deleted;
    }

    @Override
    public String getType(Uri uri) {
        int match = dataUriMatcher.match(uri);
        Endpoint endpoint = Endpoint.get(match);
        if (endpoint== null)
            return null;

        // Combine the android standard type with our specific data subtype.
        return endpoint.getMimeType();
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int match = dataUriMatcher.match(uri);
        Endpoint endpoint = Endpoint.get(match);
        if (endpoint== null || endpoint.hasId())
            throw new IllegalArgumentException("Invalid URI: " + uri);

        String tableName = null;
        switch (endpoint) {

            case GAME:
            case GAME_ID:
                tableName = GameEntry.TABLE_NAME;
                break;
            default:
                break;
        }
        SQLiteDatabase db = mDatabase.getWritableDatabase();
        long rowId = db.insert(tableName, "null", values);
        notifyChange(uri);
        return Uri.withAppendedPath(uri, Long.toString(rowId));
    }

    @Override
    public boolean onCreate() {
        // done in attach where we have a context passed in
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
        int match = dataUriMatcher.match(uri);
        Endpoint endpoint = Endpoint.get(match);
        if (endpoint== null)
            throw new IllegalArgumentException("Invalid URI: " + uri);

        if (endpoint.hasId()) {
            selection = whereWithId(uri, selection);
        }

        List<String> segs = uri.getPathSegments();
        int id =1;
        String tableName = null;
        switch (endpoint) {

            case GAME:
            case GAME_ID:
                tableName = GameEntry.TABLE_NAME;
                break;
            default:
                break;
        }

        // Run the query.
        Cursor cursor = mDatabase.getReadableDatabase().query(tableName, projection, selection, selectionArgs, null, null,
                sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
            String[] selectionArgs) {
        int updated = 0;

        int match = dataUriMatcher.match(uri);
        Endpoint endpoint = Endpoint.get(match);
        if (endpoint == null)
            throw new IllegalArgumentException("Invalid URI: " + uri);

        if (endpoint.hasId()) {
            selection = whereWithId(uri, selection);
        }

        List<String> segs = uri.getPathSegments();

        String tableName = null;
        switch (endpoint) {
            case GAME:
            case GAME_ID:
                tableName = GameEntry.TABLE_NAME;
                break;
            default:
                break;
        }

        // Update the item(s) and broadcast a change notification.
        SQLiteDatabase db = mDatabase.getWritableDatabase();
        updated = db.update(tableName, values, selection, selectionArgs);
        if (updated>0) {
            notifyChange(uri);
        }
        return updated;
    }

    protected final String whereWithId(Uri uri, String selection) {
        String id = uri.getPathSegments().get(1);
        StringBuilder where = new StringBuilder("_id=");
        where.append(id);
        if (!TextUtils.isEmpty(selection)) {
            where.append(" AND (");
            where.append(selection);
            where.append(')');
        }
        return where.toString();
    }

    protected final void notifyChange(Uri uri) {
        getContext().getContentResolver().notifyChange(uri, null,false);
    }

    // because we'll often need to update more than one Uri
    protected final void notifyChanges(Uri[] uris) {
        ContentResolver cr = getContext().getContentResolver();
        for (Uri uri : uris) {
            cr.notifyChange(uri, null,false);
        }
    }
}
