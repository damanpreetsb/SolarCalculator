package com.daman.solarcalculator.calculator.utils;

import com.daman.solarcalculator.calculator.models.GeocentricCoordinates;

public class MoonUtils
{
    public static GeocentricCoordinates getMoonCoords(double d)
    {
        // geocentric ecliptic coordinates of the moon

        double L = Constants.TO_RAD * (218.316 + 13.176396 * d);   // ecliptic longitude
        double M = Constants.TO_RAD * (134.963 + 13.064993 * d);   // mean anomaly
        double F = Constants.TO_RAD * (93.272 + 13.229350 * d);    // mean distance

        double l  = L + Constants.TO_RAD * 6.289 * Math.sin(M);    // longitude
        double b  = Constants.TO_RAD * 5.128 * Math.sin(F);        // latitude
        double dt = 385001 - 20905 * Math.cos(M);               // distance to the moon in km

        return new GeocentricCoordinates(
                PositionUtils.getRightAscension(l, b),
                PositionUtils.getDeclination(l, b),
                dt);
    }
}
