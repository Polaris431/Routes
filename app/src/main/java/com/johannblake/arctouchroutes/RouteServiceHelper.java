package com.johannblake.arctouchroutes;

import android.util.Log;

import java.util.HashMap;

/**
 * Common stuff needed when communicating with the backend service.
 */
public class RouteServiceHelper
{
  private final static String LOGTAG = "RouteServiceHelper";

  /**
   * Returns a list of common headers required by the route service.
   */
  public static HashMap<String, String> createCommonHeaders()
  {
    try
    {
      HashMap<String, String> hmHeaders = new HashMap<String, String>();
      hmHeaders.put("Authorization", "Basic V0tENE43WU1BMXVpTThWOkR0ZFR0ek1MUWxBMGhrMkMxWWk1cEx5VklsQVE2OA==");
      hmHeaders.put("X-AppGlu-Environment", "staging");

      return hmHeaders;
    }
    catch(Exception ex)
    {
      Log.e(LOGTAG, "createCommonHeaders: " + ex.getMessage());
      return null;
    }
  }
}
