package com.johannblake.arctouchroutes;

import android.content.Context;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * Basic support for HTTP requests.
 */
public class HTTPConnect
{

  /**
   * Issues an HTTP request using a post and JSON data in the body. The response is returned.
   *
   * @param url          The url that the data will be posted to.
   * @param postBody     The body of the http request. Must be formatted as Json data.
   * @param headers
   *                     Any headers to include in the request. Name/value pairs.
   * @param httpResponse If the method succeeds, the data returned from the server will be returned in this parameter.
   * @param context      the context
   * @return Returns true if no exceptions were generated and data was retrieved from the server.
   */
  public static boolean PostJson(String url, HashMap<String, String> headers, String postBody, HttpResp httpResponse, Context context)
  {
    try
    {
      URL u = new URL(url);

      HttpURLConnection connection = (HttpURLConnection) u.openConnection();
      connection.setRequestMethod("POST");
      connection.setDoOutput(true);

      for (Map.Entry<String, String> entry : headers.entrySet())
      {
        connection.setRequestProperty(entry.getKey(), entry.getValue());
      }

      connection.setRequestProperty("Content-Type", "application/json");
      connection.setRequestProperty("Content-Length", Integer.toString(postBody.length()));
      connection.setUseCaches(false);

      byte[] postData = postBody.getBytes(Charset.forName("UTF-8"));

      DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
      wr.write(postData);

      InputStream content = (InputStream) connection.getInputStream();
      BufferedReader in = new BufferedReader(new InputStreamReader(content));

      String line;
      StringBuilder sb = new StringBuilder();

      while ((line = in.readLine()) != null)
      {
        sb.append(line);
      }

      httpResponse.Data = sb.toString();

      in.close();
      wr.close();

      return true;
    }
    catch (Exception ex)
    {
      return false;
    }
  }
}
