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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

/**
 * A fragment representing a list of Items.
 * <p />
 * <p />
 * Activities containing this fragment MUST implement the HandlerProvider
 * interface.
 */
public class ListGamesFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final int LOADER_GAME_LIST = 1;
    private Uri GAMES_URI = GamesProvider.Endpoint.GAME.buildUpon().build();

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
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
        // TODO: Change Adapter to display your content
        setListAdapter(new SimpleCursorAdapter(
                getActivity(),
                R.layout.list_item_game_data,
                null,
                new String[]{
                        GameEntry.COLUMN.game.name(),
                        GameEntry.COLUMN.console.name()//,
//                        GameEntry.COLUMN.image_url.name()
                },
                new int[]{
                        android.R.id.text1,
                        R.id.game_console,
//                        R.id.imageView
                },
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER ));

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

//        try {
//            mListener = (OnFragmentInteractionListener) activity;
//        } catch (ClassCastException e) {
//            throw new ClassCastException(activity.toString()
//                + " must implement OnFragmentInteractionListener");
//        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.list_games_options, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()==R.id.action_add_item){
            ContentValues values = new ContentValues();
            values.put(GameEntry.COLUMN.game.name(),  "new game");
            Uri newGameUri = getActivity().getContentResolver().insert(GAMES_URI, values);
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
                values.put(GameEntry.COLUMN.finished.name(), c.getInt(GameEntry.COLUMN.game.ordinal()) != 0);
            }
        }
        return values;
    }
    /**
    * This interface must be implemented by activities that contain this
    * fragment to allow an interaction in this fragment to be communicated
    * to the activity and potentially other fragments contained in that
    * activity.
    * <p>
    * See the Android Training lesson <a href=
    * "http://developer.android.com/training/basics/fragments/communicating.html"
    * >Communicating with Other Fragments</a> for more information.
    */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

}
