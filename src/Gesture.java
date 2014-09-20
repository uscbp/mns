 
import java.awt.*;
import java.util.*;
import java.awt.*;
import java.lang.*;
import java.util.Vector;
import java.util.Enumeration;
import java.io.*;
/**
 *
 * @author Erhan Oztop, 2001-2002 <br>
 * <br>
 * Source code by Erhan Oztop (erhan@atr.co.jp) <br>
 * Copyright August 2002 via <br>
 * University of Southern California Ph.D. publication copyright <br>
 */

class Gesture extends Thread
{
  private boolean stopRequested=false;
  private Object3d obj;
  private double speed;
  private Spline[] jointpath;
  
  String name="unnamed";

  public Gesture(Object3d o,Spline[] tr, double sp,String nm)
  {
   name=nm;
   obj=o;
   speed=sp;
   jointpath=tr;
   reset();
  }

public void reset()
 {
   for (int i=0;i<obj.segc;i++)
    if (jointpath[i]!=null) obj.setJointAngle(obj.seg[i],jointpath[i].eval(0));
 }

public void run()
 {
   while (!stopRequested)
   {
    obj.tickGesture(this,jointpath,speed);
    HV.cv.refreshDisplay();
    try{sleep(50);} catch(InterruptedException e) {}
   }
   System.out.println("Gesture "+name+" stopped.");
   HV.self.setTrace(false);
   stop();

  }

public void stopGest()
 { stopRequested=true;}
}

/*
 *
 * Erhan Oztop, 2000-2002  <br>
 * Source code by Erhan Oztop (erhan@atr.co.jp) <br>
 * Copyright August 2002 under <br>
 * University of Southern California Ph.D. publication copyright <br>
 */
 
