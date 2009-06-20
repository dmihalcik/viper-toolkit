package edu.umd.cfar.lamp.viper.examples.persontracking;

import java.awt.*;

public class PathPoint{
int Frameno;
Rectangle R;

public PathPoint(int f,Rectangle r) {
   Frameno=f;
   R=r;
}

public int GetFrameno() {return Frameno;}
public Rectangle GetRectangle() {return R;}
public double GetCenterX() {return R.getCenterX();}
public double GetCenterY() {return R.getCenterY();}

}