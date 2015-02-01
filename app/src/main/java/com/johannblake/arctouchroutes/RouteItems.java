package com.johannblake.arctouchroutes;

/**
 * Used to hold the response data for routes returned from the server.
 */
public class RouteItems
{
  public RouteItem[] rows;
  public int rowsAffected = 0;
}

class RouteItem
{
  public int id;
  public String shortName;
  public String longName;
  public String lastModifiedDate;
  public int agencyId;
}
