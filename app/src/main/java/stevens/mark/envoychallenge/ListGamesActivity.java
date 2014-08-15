package stevens.mark.envoychallenge;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

public class ListGamesActivity extends Activity implements
        Handler.Callback,
        HandlerProvider{

    private Fragment mFragment;
    private Handler mHandler;

    public static final int LIST_GAMES_MSG_LIST_GAMES = 100;
    public static final int LIST_GAMES_MSG_EDIT_GAME = 101;


    public static Intent prepareIntent() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.addCategory(IntentConstant.CATEGORY_GAME_LIST);

        return intent;
    }
    // easier to deal with multiple fragment updates
    @Override
    public Handler getFragmentMessageHandler() {
        return mHandler;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_games);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        mHandler = new Handler(this);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, ListGamesFragment.newInstance("", ""))
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.list_games, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
//        if (id == android.R.id.home) {
//            return true;
//        }
        return super.onOptionsItemSelected(item);
    }

//    @Override
//    public void onFragmentInteraction(Uri uri) {
//
//        // change out listFragment for EditGameFragment
//    }

    @Override
    public boolean handleMessage(Message msg) {
        if (msg.what== LIST_GAMES_MSG_EDIT_GAME){
            mFragment = (Fragment) msg.obj;
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(R.id.container, mFragment);
            ft.show(mFragment);
            ft.commit();
        } else
        if (msg.what == LIST_GAMES_MSG_LIST_GAMES){
            mFragment = new ListGamesFragment();
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(R.id.container, mFragment);
            ft.show(mFragment);
            ft.commit();
            ActionBar actionBar = getActionBar();
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setIcon(this.getApplicationInfo().icon);
            actionBar.setTitle(this.getTitle());
        }
        return false;
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_list_games, container, false);
            return rootView;
        }
    }
}
