/**
 *
 * @author Erhan Oztop, 2001-2002 <br>
 * <br>
 * Source code by Erhan Oztop (erhan@atr.co.jp) <br>
 * Copyright August 2002 via <br>
 * University of Southern California Ph.D. publication copyright <br>
 */
 
public class EyeMove extends Thread
{
 private Eye eye;
 private HVCanvas cv;

 public EyeMove(HVCanvas cv,Eye eye)
 {
  this.cv=cv;
  this.eye=eye;
  gaze=new Point3d(0,0,1);
 }

  public void run()
  {
   while(!stoprequested)
   {
    step0();
    try{sleep(120);} catch(InterruptedException e) {}
   }
   System.out.println("Loope exited. Now stopping...");
   stop();
  }

  Point3d gaze=null;
  public void startXrot(double del)
  {
   dxrot=del;
  }
  public void startYrot(double del)
  {
   dyrot=del;
  }
  public void startZrot(double del)
  {
   dzrot=del;
  }
 
 int k=0;
  synchronized public void step0()
  {
    //System.out.println(k++);
    if (dxrot!=0) eye.XrotateViewPlane(dxrot);
    if (dyrot!=0) eye.YrotateViewPlane(dyrot);
    if (dzrot!=0) eye.ZrotateViewPlane(dzrot);
    Mars.project();
    cv.paint(cv.getGraphics());
  }

public void stopSelf()
  {

   //System.out.println("rots:"+dxrot+","+dyrot+","+dzrot);
   System.out.println("Will stop...");
   stoprequested=true;
  }
private boolean stoprequested=false;
public double yrot=0,zrot=0,xrot=0;
public double dyrot,dzrot,dxrot;
int mc=0;
int posdx=3;
}

/*
 *
 * Erhan Oztop, 2000-2002  <br>
 * Source code by Erhan Oztop (erhan@atr.co.jp) <br>
 * Copyright August 2002 under <br>
 * University of Southern California Ph.D. publication copyright <br>
 */
 
