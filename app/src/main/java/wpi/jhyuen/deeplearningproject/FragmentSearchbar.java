package wpi.jhyuen.deeplearningproject;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Objects;

import static android.content.Context.MODE_PRIVATE;

public class FragmentSearchbar extends Fragment
{
    private final String TAG = FragmentSearchbar.class.getSimpleName();

    // Past searches stuff
    private SharedPreferences mPastSearchesPrefs;
    private final String KEY_PASTSEARCHES = "PASTSEARCHES";
    private ArrayList<String> mList_pastSearches;

    // Widget stuff
    private AutoCompleteTextView mAutoComplete_searchbar; // Variable that holds searchbar EditText

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_searchbar, container, false);

        mPastSearchesPrefs = Objects.requireNonNull(getActivity()).getApplicationContext().getSharedPreferences("Prefs_PastSearches", MODE_PRIVATE);
        updatePastSearches(); // Update mList_pastSearches with past searches stored in persistent data
        Log.d(TAG, "onCreateView(): Past Searches stored START:");
        if(mList_pastSearches == null || mList_pastSearches.size() == 0) Log.d(TAG, "No past searches");
        else
        {
            for (String str : mList_pastSearches)
            {
                Log.d(TAG, str);
            }
        }
        Log.d(TAG, "onCreateView(): Past Searches stored END");

        mAutoComplete_searchbar = view.findViewById(R.id.editText_searchbar);
        if(mAutoComplete_searchbar != null)
        {
            mAutoComplete_searchbar.setOnFocusChangeListener(new View.OnFocusChangeListener() // Clear text on search bar on focus
            {
                @Override
                public void onFocusChange(View v, boolean hasFocus)
                {
                    if(hasFocus)
                    {
                        mAutoComplete_searchbar.getText().clear();
                    }
                }
            });

            mAutoComplete_searchbar.setOnEditorActionListener(
                    new TextView.OnEditorActionListener()
                    {
                        @Override
                        public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
                        {
                            if ((actionId == EditorInfo.IME_ACTION_SEARCH) ||
                                (actionId == EditorInfo.IME_ACTION_DONE) ||
                                (event != null && event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER))
                            {
                                if (event == null || !event.isShiftPressed())
                                {
                                    search(); // Search based on search bar text after pressing enter

                                    // Hide virtual keyboard
                                    InputMethodManager in = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                                    in.hideSoftInputFromWindow(mAutoComplete_searchbar.getApplicationWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);

                                    mAutoComplete_searchbar.clearFocus();
                                    return true;
                                }
                            }
                            return false; // pass on to other listeners.
                        }
                    }
            );

            updateSuggestions(); // Add suggestions to the AutoCompleteTextView
        }

        return view;
    }

    // Searches based on content in search bar, storing content into past searches
    // Sends command to FragmentFlowers to display proper flowers
    private void search()
    {
        if(mAutoComplete_searchbar == null) return;

        // Store unique search in persistent storage
        String search = mAutoComplete_searchbar.getText().toString();
        if(!mList_pastSearches.contains(search))
        {
            mList_pastSearches.add(search);
            storePastSearches(); // Store new search to persistent data
            updateSuggestions(); // Update suggestions to the AutoCompleteTextView
        }

        // TODO Do something here that actually searches for flowers to display
    }

    // Update mAutoComplete_searchbar suggestions with current list of past searches
    private void updateSuggestions()
    {
        // Put list into adapter to show past searches suggestions
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.select_dialog_item, mList_pastSearches);
        mAutoComplete_searchbar.setThreshold(1); // Type 1 char before showing suggestions
        mAutoComplete_searchbar.setAdapter(adapter);
    }

    // Stores mList_pastSearches (String list of searches) to persistent data (SharedPreferences)
    private void storePastSearches()
    {
        SharedPreferences.Editor editor = mPastSearchesPrefs.edit();
        JSONArray jsonArray = new JSONArray();
        for (String search : mList_pastSearches) // Fill JSON array with past searches array
        {
            jsonArray.put(search);
        }
        editor.putString(KEY_PASTSEARCHES, jsonArray.toString());
        editor.apply(); // apply() writes data to persistent storage in the background
    }

    // Updates mList_pastSearches (String list of searches) from stored persistent data (SharedPreferences)
    private void updatePastSearches()
    {
        mList_pastSearches = new ArrayList<String>();

        String jsonArrayAsString = mPastSearchesPrefs.getString(KEY_PASTSEARCHES, null);
        if(jsonArrayAsString == null) return;

        try
        {
            JSONArray jsonArray = new JSONArray(jsonArrayAsString);
            Log.d(TAG, "updatePastSearches(): Number of past searches: " + jsonArray.length());
            for (int i = 0; i < jsonArray.length(); i++)
            {
                String pastSearch = jsonArray.optString(i);
                mList_pastSearches.add(pastSearch);
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }
}
