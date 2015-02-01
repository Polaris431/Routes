package com.johannblake.arctouchroutes;

/**
 * Used to hold information returned from the server about the timetable for a specific route.
 */
public class Timetable
{
  public TimetableItem[] rows;
  public int rowsAffected = 0;
}

class TimetableItem
{
  public int id;
  public String calendar;
  public String time;
}