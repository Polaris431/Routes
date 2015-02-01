package com.johannblake.arctouchroutes;

/**
 * Used to hold information returned from the server about the stops along a route.
 */
public class RouteStops
{
  public RouteStop[] rows;
  public int rowsAffected = 0;
}

class RouteStop
{
  public int id;
  public String name;
  public int sequence;
  public int route_Id;
}
