package com.johannblake.arctouchroutes;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.UUID;


public class MainActivity extends ActionBarActivity
{
  private final String LOGTAG = "MainActivity";
  private ListView lvRoutes;
  private AdapterRoutes adapterRoutes;
  private Context context;
  private RouteItems routeItems;

  private final int INTENT_REQUEST_CODE_SHOW_MAP = 1;

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    try
    {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);

      this.context = this;

      this.lvRoutes = (ListView) findViewById(R.id.lvRoutes);
      this.lvRoutes.setItemsCanFocus(true);


      this.lvRoutes.setOnItemClickListener(new AdapterView.OnItemClickListener()
      {
        @Override
        public void onItemClick(AdapterView<?> parent, View v, int position, long id)
        {
          try
          {
            // Display the route details when the user clicks on a list item.
            RouteItem routeItem = (RouteItem) v.getTag();

            Intent intent = new Intent(context, RouteDetailsActivity.class);
            intent.putExtra(RouteDetailsActivity.INTENT_KEY_ROUTE_ID, routeItem.id);
            intent.putExtra(RouteDetailsActivity.INTENT_KEY_ROUTE_NAME, routeItem.longName);
            startActivity(intent);
          }
          catch (Exception ex)
          {
            Log.e(LOGTAG, ".onItemClick: " + ex.getMessage());
          }
        }
      });

      EditText etSearch = (EditText) findViewById(R.id.etSearch);

      // Handle the user tapping on the Search button on the keyboard.
      etSearch.setOnEditorActionListener(new TextView.OnEditorActionListener()
      {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
        {
          try
          {
            if (actionId == EditorInfo.IME_ACTION_SEARCH)
            {
              searchRoutes();
              return true;
            }
            return false;
          }
          catch(Exception ex)
          {
            Log.e(LOGTAG, ".onEditorAction: " + ex.getMessage());
            return false;
          }
        }
      });

    }
    catch (Exception ex)
    {
      Log.e(LOGTAG, ".onCreate: " + ex.getMessage());
    }
  }


  /**
   * Start a thread that handles downloading the routes from the server.
   */
  public void searchRoutes()
  {
    try
    {
      hideKeyboard();

      Button btnSearch = (Button) findViewById(R.id.btnSearch);
      btnSearch.setEnabled(false);

      EditText etSearch = (EditText) findViewById(R.id.etSearch);

      ProgressBar pbarLoading = (ProgressBar) findViewById(R.id.pbarLoading);
      pbarLoading.setVisibility(View.VISIBLE);

      String query = etSearch.getText().toString();
      new Thread(null, new GetRoutesFromServer(query), "GetRoutesFromServer_" + UUID.randomUUID()).start();
    }
    catch (Exception ex)
    {
      Log.e(LOGTAG, ".searchRoutes: " + ex.getMessage());
    }
  }

  public void searchRoutes(View view)
  {
    try
    {
      searchRoutes();
    }
    catch (Exception ex)
    {
      Log.e(LOGTAG, ".searchRoutes: " + ex.getMessage());
    }
  }

  /**
   * The array adapter that the listview uses for displaying street names.
   */
  private class AdapterRoutes extends ArrayAdapter<RouteItem>
  {
    @SuppressWarnings("unchecked")
    public AdapterRoutes(Context context, int textViewResourceId, @SuppressWarnings("rawtypes") RouteItem[] items)
    {
      super(context, textViewResourceId, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
      try
      {
        View v = convertView;

        if (v == null)
        {
          LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
          v = vi.inflate(R.layout.routes_list_row, null);
        }

        RouteItem routeItem = routeItems.rows[position];
        v.setTag(routeItem);

        // Display the route's long name.
        TextView tvLongName = (TextView) v.findViewById(R.id.tvLongName);
        tvLongName.setText(routeItem.longName);

        return v;
      }
      catch (Exception ex)
      {
        Log.e(LOGTAG, ".AdapterRoutes.getView: " + ex.getMessage());
        return convertView;
      }
    }
  }

  /**
   * Used to retrieve the routes (street names) from the backend server.
   */
  private class GetRoutesFromServer implements Runnable
  {
    private String query;

    public GetRoutesFromServer(String query)
    {
      this.query = query;
    }

    @Override
    public void run()
    {
      try
      {
        Thread.currentThread().setName("MainActivity.GetRoutesFromServer_" + UUID.randomUUID());

        // Set the post body as JSON data.
        Gson gson = new Gson();
        HttpResp httpResp = new HttpResp();

        // Create an object to store the request parameters.
        RouteRequest request = new RouteRequest();
        request.params.stopName = "%" + this.query + "%";

        // Convert the object to JSON, which gets plugged into the request body.
        String body = gson.toJson(request);
        HashMap<String, String> headers = RouteServiceHelper.createCommonHeaders();

        if (!HTTPConnect.PostJson("https://dashboard.appglu.com/v1/queries/findRoutesByStopName/run", headers, body, httpResp, context))
        {
          return;
        }

        // Deserialize the result.
        routeItems = gson.fromJson(httpResp.Data, RouteItems.class);


        runOnUiThread(new Runnable()
        {
          public void run()
          {
            try
            {
              Thread.currentThread().setName("MainActivity.GetRoutesFromServer.run.runOnUiThread.run_" + UUID.randomUUID());

              // Display the routes.
              adapterRoutes = new AdapterRoutes(context, R.layout.routes_list_row, routeItems.rows);
              lvRoutes.setAdapter(adapterRoutes);
              lvRoutes.requestFocus();
            }
            catch (Exception ex)
            {
              Log.e(LOGTAG, ".GetRoutesFromServer.run.runOnUiThread: " + ex.getMessage());
            }
            finally
            {
              // Re-enable the Search button.
              Button btnSearch = (Button) findViewById(R.id.btnSearch);
              btnSearch.setEnabled(true);

              ProgressBar pbarLoading = (ProgressBar) findViewById(R.id.pbarLoading);
              pbarLoading.setVisibility(View.GONE);
            }
          }
        });
      }
      catch (Exception ex)
      {
        Log.e(LOGTAG, ".GetRoutesFromServer.run: " + ex.getMessage());
      }
    }
  }

  // Used to hide the keyboard.
  private void hideKeyboard()
  {
    // Check if no view has focus:
    View view = this.getCurrentFocus();

    if (view != null)
    {
      InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
      inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }
  }


  @Override
  public boolean onCreateOptionsMenu(Menu menu)
  {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item)
  {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    if (id == R.id.action_settings)
    {
      return true;
    }
    else if (id == R.id.mnuShowMap)
    {
      Intent intentShowMap = new Intent(context, MapsActivity.class);
      startActivityForResult(intentShowMap, INTENT_REQUEST_CODE_SHOW_MAP);
    }

    return super.onOptionsItemSelected(item);
  }


  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent intent)
  {
    try
    {
      super.onActivityResult(requestCode, resultCode, intent);

      if (resultCode == Activity.RESULT_CANCELED)
        return;

      if (requestCode == INTENT_REQUEST_CODE_SHOW_MAP)
      {
        String streetName = intent.getStringExtra(MapsActivity.INTENT_KEY_SELECTED_STREET_NAME);

        // Display the street name and then query the server for routes.
        EditText etSearch = (EditText) findViewById(R.id.etSearch);
        etSearch.setText(streetName);

        searchRoutes();
      }
    }
    catch (Exception ex)
    {
    }
  }
}


class RouteRequest
{
  public RouteRequestParams params = new RouteRequestParams();
}

class RouteRequestParams
{
  public String stopName;
}

