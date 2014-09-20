
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
 

//************************* REACH ***********************
//make a Gesture class and derive all the gestures from it!
public class Reach extends Thread
{
  private boolean stopRequested=false;
  private Object3d obj;
  public String name="unnamed";
  Point3d target;
  Trajectory tr;
  String com;

  private int SLP;

  public Reach(Object3d o,  Trajectory t,String com)
  {
   obj=o;
   tr=t;
   this.com=com;
   SLP=5;
   if (obj.search_mode==obj.BABBLE) SLP=1;
   else if (obj.search_mode==obj.VISUAL_SEARCH) SLP=1;
   else if (obj.search_mode==obj.SILENT_SEARCH) SLP=1;
   else if (obj.search_mode==obj.EXECUTE) SLP=5; 
   else if (obj.search_mode==obj.PDFGRASP) SLP=15;
   reset();
  }

public void reset()
 {
 }

public void run()
 {
   while (!stopRequested)
   {
    if (obj.search_mode==obj.VISUAL_SEARCH) 
      {
       
       obj.tickVisual(this); 
       HV.cv.refreshDisplay();
      } else
      if (obj.search_mode==obj.SILENT_SEARCH)
      {
        obj.tickSilent(this); 
      } else
      if (obj.search_mode==obj.EXECUTE)
      {
        obj.tickReachGesture(this,tr);
       HV.cv.refreshDisplay();
       } else
	   if (obj.search_mode==obj.BABBLE) {
	       obj.tickBabble();
	       //HV.cv.refreshDisplay();
	   }
	   else
	       if (obj.search_mode==obj.PDFGRASP) {
		   obj.tickPDFgrasp();
		   //HV.cv.refreshDisplay();
	   }
    try{sleep(SLP);} catch(InterruptedException e) {}
   }
   System.out.println("Reach "+name+" stopped.");
   HV.cv.refreshDisplay();
   obj.enablePanels();
   HV.self.setTrace(false);
   obj.finalizeReach(com);
   stop();
  }

public void stopRGest()
 { stopRequested=true;}
}

/*
 *
 * Erhan Oztop, 2000-2002  <br>
 * Source code by Erhan Oztop (erhan@atr.co.jp) <br>
 * Copyright August 2002 under <br>
 * University of Southern California Ph.D. publication copyright <br>
 */
 
