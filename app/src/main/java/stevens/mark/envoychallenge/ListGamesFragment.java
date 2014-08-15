package stevens.mark.envoychallenge;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;

import com.squareup.picasso.Picasso;

/**
 * A fragment representing a list of Items.
 * <p />
 * <p />
 * Activities containing this fragment MUST implement the HandlerProvider
 * interface.
 */
public class ListGamesFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor>, RatingBar.OnRatingBarChangeListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final int LOADER_GAME_LIST = 1;
    private Uri GAMES_URI = GamesProvider.Endpoint.GAME.buildUpon().build();

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private Handler mActivityHandler;

    // TODO: Rename and change types of parameters
    public static ListGamesFragment newInstance(String param1, String param2) {
        ListGamesFragment fragment = new ListGamesFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ListGamesFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        getLoaderManager().initLoader(LOADER_GAME_LIST, new Bundle(), this );

        if ("ratings".equals(mParam1))
            setListAdapter(prepareRatingsAdapter());
        else
            setListAdapter(prepareListingsAdapter());

        setHasOptionsMenu(true);
        ActionBar actionBar = getActivity().getActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(R.string.title_activity_list_games);
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof HandlerProvider)
            mActivityHandler = ((HandlerProvider) activity).getFragmentMessageHandler();
        else
            throw new ClassCastException(activity.toString()
                    + " must implement HandlerProvider");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mActivityHandler = null;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.list_games_options, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()==R.id.action_add_item){
//            ContentValues values = new ContentValues();
//            values.put(GameEntry.COLUMN.game.name(),  "new game");
//            Uri newGameUri = getActivity().getContentResolver().insert(GAMES_URI, values);
            Fragment fragment = EditGameFragment.newInstance(null,null);
            Message msg = mActivityHandler.obtainMessage(ListGamesActivity.LIST_GAMES_MSG_EDIT_GAME,  fragment );
            mActivityHandler.sendMessage(msg);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        if (null != mActivityHandler) {
            Uri gameUri = ContentUris.withAppendedId(GAMES_URI, id);
            // get the current values and push them to the edit fragment
            Cursor c = ((CursorAdapter)getListAdapter()).getCursor();
            ContentValues values = getContentValues(c, position);
            // ask the activity to play this fragment in my place
            Fragment fragment = EditGameFragment.newInstance(gameUri,values);
            Message msg = mActivityHandler.obtainMessage(ListGamesActivity.LIST_GAMES_MSG_EDIT_GAME,  fragment );
            mActivityHandler.sendMessage(msg);
        }
    }

    private CursorAdapter prepareListingsAdapter(){
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                getActivity(),
                R.layout.list_item_game_data,
                null,
                new String[]{
                        GameEntry.COLUMN.game.name(),
                        GameEntry.COLUMN.console.name(),
                        GameEntry.COLUMN.image_url.name(),
                        GameEntry.COLUMN.finished.name()
                },
                new int[]{
                        android.R.id.text1,
                        R.id.game_console,
                        R.id.imageView,
                        R.id.game_finished
                },
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER );


        adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                if (columnIndex==GameEntry.COLUMN.image_url.ordinal()){
                    String imageUrl = cursor.getString(GameEntry.COLUMN.image_url.ordinal());
                    if (TextUtils.isEmpty(imageUrl)){
                        Picasso.with(getActivity())
                                .load(android.R.drawable.ic_menu_gallery)
                                .into((ImageView)view);
                    } else {
                        Picasso.with(getActivity())
                                .load(Uri.parse(imageUrl))
                                .resize(96,96)
                                .into((ImageView) view);
                    }
                    return true;
                } else
                if (columnIndex==GameEntry.COLUMN.finished.ordinal()){
                    // setup the update uri now
                    Uri gameUri = ContentUris.withAppendedId(GAMES_URI, cursor.getLong(GameEntry.COLUMN._id.ordinal()));
                    view.setTag(gameUri);
                    ((CheckedTextView)view).setChecked(cursor.getInt(columnIndex) == 1);
                    ((CheckedTextView)view).setOnClickListener(checkFinishedClickListener);
                    return true;
                }
                return false;
            }
        });
        return adapter;
    }

    private View.OnClickListener checkFinishedClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Uri gameUri = (Uri)v.getTag();
            Checkable checkable = (Checkable)v;
            checkable.toggle();
            ContentValues values = new ContentValues();
            values.put(GameEntry.COLUMN.finished.name(), checkable.isChecked() );
            getActivity().getContentResolver().update(gameUri, values, null, null);

        }
    };

    private CursorAdapter prepareRatingsAdapter(){

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                getActivity(),
                R.layout.list_item_game_rating,
                null,
                new String[]{
                        GameEntry.COLUMN.game.name(),
                        GameEntry.COLUMN.console.name(),
                        GameEntry.COLUMN.image_url.name(),
                        GameEntry.COLUMN.rating.name()

                },
                new int[]{
                        android.R.id.text1,
                        R.id.game_console,
                        R.id.imageView,
                        R.id.ratingBar
                },
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER );

        adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                if (columnIndex==GameEntry.COLUMN.image_url.ordinal()){
                    String imageUrl = cursor.getString(GameEntry.COLUMN.image_url.ordinal());
                    if (TextUtils.isEmpty(imageUrl)){
                        Picasso.with(getActivity())
                                .load(android.R.drawable.ic_menu_gallery)
                                .into((ImageView)view);
                    } else {
                        Picasso.with(getActivity())
                                .load(Uri.parse(imageUrl))
                                .resize(96,96)
                                .into((ImageView) view);
                    }
                    return true;
                } else
                if (columnIndex==GameEntry.COLUMN.rating.ordinal()){
                    // setup the update uri now
                    Uri gameUri = ContentUris.withAppendedId(GAMES_URI, cursor.getLong(GameEntry.COLUMN._id.ordinal()));
                    view.setTag(gameUri);
                    ((RatingBar)view).setRating(cursor.getFloat(columnIndex));
                    ((RatingBar)view).setOnRatingBarChangeListener(ListGamesFragment.this);
                    return true;
                }
                return false;
            }
        });
        return adapter;
    }

    @Override
    public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
        Uri gameUri = (Uri)ratingBar.getTag();
        ContentValues values = new ContentValues();
        values.put(GameEntry.COLUMN.rating.name(), rating);
        getActivity().getContentResolver().update(gameUri, values, null,null);

    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id){
            case LOADER_GAME_LIST:
                return new CursorLoader(
                        getActivity(),   // Parent activity context
                        GamesProvider.Endpoint.GAME.buildUpon().build(),        // Table to query
                        GameEntry.PROJECTION,     // Projection to return
                        null,            // No selection clause
                        null,            // No selection arguments
                        null             // Default sort order
                );
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        switch (loader.getId()){
            case LOADER_GAME_LIST:
                ((SimpleCursorAdapter)getListAdapter()).changeCursor(data);

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private ContentValues getContentValues(Cursor c, int position){
        ContentValues values = new ContentValues();
        if (c!=null && c.getCount()>0){
            c.moveToPosition(position);
            if (!c.isAfterLast()) {
                values.put(GameEntry.COLUMN.game.name(), c.getString(GameEntry.COLUMN.game.ordinal()));
                values.put(GameEntry.COLUMN.console.name(), c.getString(GameEntry.COLUMN.console.ordinal()));
                values.put(GameEntry.COLUMN.image_url.name(), c.getString(GameEntry.COLUMN.image_url.ordinal()));
                values.put(GameEntry.COLUMN.rating.name(), c.getFloat(GameEntry.COLUMN.rating.ordinal()));
                values.put(GameEntry.COLUMN.finished.name(), c.getInt(GameEntry.COLUMN.finished.ordinal()) != 0);
            }
        }
        return values;
    }


}
