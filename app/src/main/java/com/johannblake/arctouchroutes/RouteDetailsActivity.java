package com.johannblake.arctouchroutes;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.UUID;


public class RouteDetailsActivity extends ActionBarActivity
{
  public static final String INTENT_KEY_ROUTE_ID = "route_id";
  public static final String INTENT_KEY_ROUTE_NAME = "route_name";

  private final String LOGTAG = "RouteDetailsActivity";
  private Context context;

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    try
    {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.route_details);

      // Extract the route id.
      int routeID = getIntent().getIntExtra(INTENT_KEY_ROUTE_ID, 0);

      // Display the route name.
      String routeName = getIntent().getStringExtra(INTENT_KEY_ROUTE_NAME);

      TextView tvRouteName = (TextView) findViewById(R.id.tvRouteName);
      tvRouteName.setText(routeName);

      this.context = this;

      ActionBar actionBar = (ActionBar)getSupportActionBar();
      actionBar.setDisplayHomeAsUpEnabled(true);

      new Thread(null, new GetRouteStopsFromServer(routeID), "GetRouteStopsFromServer_" + UUID.randomUUID()).start();
      new Thread(null, new GetRouteTimetableFromServer(routeID), "GetRouteTimetableFromServer" + UUID.randomUUID()).start();
    }
    catch (Exception ex)
    {
      Log.e(LOGTAG, ".onCreate: " + ex.getMessage());
    }
  }

  private class GetRouteStopsFromServer implements Runnable
  {
    private int routeID;

    public GetRouteStopsFromServer(int routeID)
    {
      this.routeID = routeID;
    }

    @Override
    public void run()
    {
      try
      {
        Thread.currentThread().setName("RouteDetailsActivity.GetRouteStopsFromServer_" + UUID.randomUUID());

        // Set the post body as JSON data.
        Gson gson = new Gson();
        HttpResp httpResp = new HttpResp();

        // Create an object to store the request parameters.
        RouteStopsRequest request = new RouteStopsRequest();
        request.params.routeId = this.routeID;

        // Convert the object to JSON, which gets plugged into the request body.
        String body = gson.toJson(request);
        HashMap<String, String> headers = RouteServiceHelper.createCommonHeaders();

        if (!HTTPConnect.PostJson("https://dashboard.appglu.com/v1/queries/findStopsByRouteId/run", headers, body, httpResp, context))
        {
          return;
        }

        // Deserialize the result.
        final RouteStops stops = gson.fromJson(httpResp.Data, RouteStops.class);

        runOnUiThread(new Runnable()
        {
          public void run()
          {
            try
            {
              Thread.currentThread().setName("RouteDetailsActivity.GetRouteStopsFromServer.run.runOnUiThread.run_" + UUID.randomUUID());
              LinearLayout llStops = (LinearLayout) findViewById(R.id.llStops);

              // Create a layout for each stop.
              for (RouteStop stop : stops.rows)
              {
                LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                RelativeLayout rlStopInfo = (RelativeLayout) vi.inflate(R.layout.stop_info, null);

                TextView tvStopName = (TextView) rlStopInfo.findViewById(R.id.tvStopName);
                tvStopName.setText(stop.name);
                llStops.addView(rlStopInfo);
              }

            }
            catch (Exception ex)
            {
              Log.e(LOGTAG, ".GetRouteStopsFromServer.run.runOnUiThread: " + ex.getMessage());
            }
          }
        });
      }
      catch (Exception ex)
      {
        Log.e(LOGTAG, ".GetRouteStopsFromServer.run: " + ex.getMessage());
      }
    }
  }


  private class GetRouteTimetableFromServer implements Runnable
  {
    private int routeID;

    public GetRouteTimetableFromServer(int routeID)
    {
      this.routeID = routeID;
    }

    @Override
    public void run()
    {
      try
      {
        Thread.currentThread().setName("RouteDetailsActivity.GetRouteTimetableFromServer_" + UUID.randomUUID());

        // Set the post body as JSON data.
        Gson gson = new Gson();
        HttpResp httpResp = new HttpResp();

        // Create an object to store the request parameters.
        RouteTimetableRequest request = new RouteTimetableRequest();
        request.params.routeId = this.routeID;

        // Convert the object to JSON, which gets plugged into the request body.
        String body = gson.toJson(request);
        HashMap<String, String> headers = RouteServiceHelper.createCommonHeaders();

        if (!HTTPConnect.PostJson("https://dashboard.appglu.com/v1/queries/findDeparturesByRouteId/run", headers, body, httpResp, context))
        {
          return;
        }

        // Deserialize the result.
        final Timetable timetable = gson.fromJson(httpResp.Data, Timetable.class);

        runOnUiThread(new Runnable()
        {
          public void run()
          {
            try
            {
              Thread.currentThread().setName("RouteDetailsActivity.GetRouteTimetableFromServer.run.runOnUiThread.run_" + UUID.randomUUID());
              LinearLayout llTimetable = (LinearLayout) findViewById(R.id.llTimetable);

              String weekday = null;

              // Create a layout for each stop.
              for (TimetableItem time : timetable.rows)
              {
                if (!time.calendar.equals(weekday))
                {
                  weekday = time.calendar;

                  // Add a header above the times to indicate which part of the week the time belongs to.
                  LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                  RelativeLayout rlWeekday = (RelativeLayout) vi.inflate(R.layout.timetable_weekday_header, null);

                  TextView tvWeekday = (TextView) rlWeekday.findViewById(R.id.tvWeekday);
                  tvWeekday.setText(time.calendar);
                  llTimetable.addView(rlWeekday);
                }

                LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                RelativeLayout rlTimetable = (RelativeLayout) vi.inflate(R.layout.timetable_info, null);

                TextView tvTime = (TextView) rlTimetable.findViewById(R.id.tvTime);
                tvTime.setText(time.time);
                llTimetable.addView(rlTimetable);
              }

            }
            catch (Exception ex)
            {
              Log.e(LOGTAG, ".GetRouteTimetableFromServer.run.runOnUiThread: " + ex.getMessage());
            }
          }
        });
      }
      catch (Exception ex)
      {
        Log.e(LOGTAG, ".GetRouteTimetableFromServer.run: " + ex.getMessage());
      }
    }
  }


  @Override
  public boolean onCreateOptionsMenu(Menu menu)
  {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_route_details, menu);
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
    else if (id == android.R.id.home)
      finish();

    return super.onOptionsItemSelected(item);
  }
}


class RouteStopsRequest
{
  public RouteStopsRequestParams params = new RouteStopsRequestParams();
}

class RouteStopsRequestParams
{
  public int routeId;
}

class RouteTimetableRequest
{
  public RouteStopsRequestParams params = new RouteStopsRequestParams();
}

class RouteTimetableRequestParams
{
  public int routeId;
}