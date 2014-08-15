package stevens.mark.envoychallenge;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link EditGameFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link EditGameFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class EditGameFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_GAME_URI = "uri";
    private static final String ARG_PARAM2 = "param2";
    private Uri GAMES_URI = GamesProvider.Endpoint.GAME.buildUpon().build();

    // TODO: Rename and change types of parameters
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    private Handler mActivityHandler;

    private EditText game_label;
    private EditText game_console;
    private CheckBox is_finished;

    private Uri gameUri;
    private ContentValues gameData;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param Uri GameUri
     * @param ContentValues values
     * @return A new instance of fragment EditGameFragment.
     */
    public static EditGameFragment newInstance(Uri gameUri, ContentValues values) {
        EditGameFragment fragment = new EditGameFragment();
        Bundle args = new Bundle();
        if (gameUri!=null) {
            args.putParcelable(ARG_GAME_URI, gameUri);
        }
        if (values!=null){
            args.putParcelable(ARG_PARAM2, values);
        }
        fragment.setArguments(args);
        return fragment;
    }
    public EditGameFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            if (getArguments().containsKey(ARG_GAME_URI))
                gameUri = getArguments().getParcelable(ARG_GAME_URI);
            if (getArguments().containsKey(ARG_PARAM2))
                gameData = getArguments().getParcelable(ARG_PARAM2);
        }
        setHasOptionsMenu(true);
        ActionBar actionBar = getActivity().getActionBar();
        actionBar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setIcon(R.drawable.ic_menu_done_holo_light);
        actionBar.setTitle(R.string.action_done);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_edit_game, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        game_label = (EditText)view.findViewById(R.id.game_label);
        game_console = (EditText)view.findViewById(R.id.game_console_label);
        is_finished = (CheckBox)view.findViewById(R.id.game_finished);

        if (gameData!=null){
            game_label.setText(gameData.getAsString(GameEntry.COLUMN.game.name()));
            game_console.setText(gameData.getAsString(GameEntry.COLUMN.console.name()));
            is_finished.setChecked(gameData.getAsBoolean(GameEntry.COLUMN.finished.name()));
        }

        super.onViewCreated(view, savedInstanceState);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
//        try {
//            mListener = (OnFragmentInteractionListener) activity;
//        } catch (ClassCastException e) {
//            throw new ClassCastException(activity.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
        if (activity instanceof HandlerProvider)
            mActivityHandler = ((HandlerProvider) activity).getFragmentMessageHandler();
        else
            throw new ClassCastException(activity.toString()
                    + " must implement HandlerProvider");

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_edit_game, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            // save and return to list
            if (gameUri==null){
                getActivity().getContentResolver().insert(GAMES_URI, getContentValues());
            } else {
                getActivity().getContentResolver().update(gameUri, getContentValues(), GameEntry.COLUMN._id + "=?", new String[]{gameUri.getLastPathSegment()});
            }

            Message msg = mActivityHandler.obtainMessage(ListGamesActivity.LIST_GAMES_MSG_LIST_GAMES);
            mActivityHandler.sendMessage(msg);
            return true;

        } else if (itemId == R.id.action_reset) {
            Message msg = mActivityHandler.obtainMessage(ListGamesActivity.LIST_GAMES_MSG_LIST_GAMES);
            mActivityHandler.sendMessage(msg);

        }
        return true;
    }

    private ContentValues getContentValues(){
        ContentValues values = new ContentValues();
        values.put(GameEntry.COLUMN.game.name(), game_label.getText().toString());
        values.put(GameEntry.COLUMN.console.name(), game_console.getText().toString());
        values.put(GameEntry.COLUMN.finished.name(), is_finished.isChecked());
        return values;
    }
}
