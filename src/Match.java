 
import java.io.*;
import java.net.*;
import java.applet.*;
import java.util.*;
import java.awt.*;
import java.lang.*;
import java.util.Vector;
import java.util.Enumeration;
/**
 *
 * @author Erhan Oztop, 2001-2002 <br>
 * <br>
 * Source code by Erhan Oztop (erhan@atr.co.jp) <br>
 * Copyright August 2002 via <br>
 * University of Southern California Ph.D. publication copyright <br>
 */

class Match extends Thread
{
  private HVCanvas cv;
  private Hand hand;
  private Feature real,model;
  private boolean stopRequest=false;

  public Match(HVCanvas c, Feature f, Hand h)
  {
   cv=c;
   hand=h;
   real=f;
   model=new Feature(hand);
   stopRequest=false;
  }

  public Match(HVCanvas c, String s, Hand h)
  {
   cv=c;
   hand=h;
   real=new Feature(s);
   double ang=real.Imex[0][0];
   double palmlen=real.Imex[0][1];
   System.out.println("Match real: ang:"+ang+"   palmlen:"+palmlen);
   hand.makeFist();
   double knucle_dist=Mars.eye.projectDist(hand.pinky.joint_pos,
                                           hand.index.joint_pos);
   System.out.println("Project model Knuckle dist:"+knucle_dist);
   real.setScale(0.9*knucle_dist/palmlen); // update Feat scale 
   real.extract(0); //extract all types regardless of parameter.
   ang=real.getAngle();
    System.out.println("Angle:"+180*ang/Math.PI);
   hand.arm.rotateJoint(ang-Math.PI/2);
   model=new Feature(hand);
   stopRequest=false;
  }
  public Match(HVCanvas c, Hand fakereal, Hand h)
  {
   cv=c;
   hand=h;
   real=new Feature(fakereal);
   model=new Feature(hand);
   hand.project(cv.eye);
   fakereal.project(cv.eye);
   real.extract(0);
   real.extract(1);
   real.extract(2); // store the features of real
   model.extract(0);
   model.extract(1);
   model.extract(2); // store the features of real
   stopRequest=false;
  }

  public Match(HVCanvas c, Hand h)
  {
   cv=c; 
   hand=h;
   model=new Feature(hand);
   real=new Feature();
   real.extract(0);
   real.extract(1);
   real.extract(2);
   stopRequest=false;
  }

 public void updateReal(int f)
 { 
  real.extract(f);
 }
 public void updateModel(int f)
 { 
  model.extract(f);
 }

 public void resetMatching()
 { 
  mode=0; 
  it=0;
 }
 public void resetMatching(int f)
 {
  resetMatching();
  if (f==Feature.ftype_PALM) mode=0;
  if (f==Feature.ftype_LIMBS) mode=3; 
  if (f==Feature.ftype_ALL) mode=1; 
  System.out.println("MODE setto:"+mode);
 }
 public void run()
  {
   System.out.println("Match started.");
   while(!stopRequest)
   {
   //hand.makeFist();
    hand.project(cv.eye);
    match(1);
    if (real.type==Feature.type_IMAGE) cv.showfeat=real;
    cv.paint(cv.getGraphics());
    cv.showfeat=null;
    try{sleep(20);} catch(InterruptedException e) {}
   }
   System.out.println("Match stopped.");
   stop();
  }

int count=0;
int mode=0;
int conc=0;
int goodc=0;
int badc=0;
int it=0;
double r=1;
int f;
int actj=3;
double actang=Math.PI/2/20;
double actend=Math.PI/2/actang;
int actc=0;
int best=0;
double abest=0;
double bestdiff=100000;
public void match(int N)
{
 double ang,olddiff,diff=0;
 Segment pick;

 if (mode==0) f=Feature.ftype_PALM;
 else if (mode==1 || mode==3) f=Feature.ftype_LIMBS;
 else f=Feature.ftype_ALL;
 model.extract(f);
 olddiff=model.difference(real,f);
 for (int k=0;k< N; k++)
 {
  int j=0;

  if (mode==3)
  {
          mode=2; if (mode==2) continue;
  //if (mode==3) {mode=1; System.out.println("***mode:"+mode); continue;}
  //System.out.println("MODE3: freejoint:"+actj+"  current angle:"+hand.free[actj].beta*180/Math.PI);
  System.out.println("Working on finger:"+hand.free[actj].parent.label);
  hand.project(cv.eye);
  model.extract(f);
  diff=model.difference(real,f);
  if (diff<bestdiff) {bestdiff=diff; best=actc; abest=hand.free[actj].beta;}
  hand.constrainedRotate(hand.free[actj],actang);
  actc++;
  if (actc>=actend)
  {
   hand.constrainedRotate(hand.free[actj],-hand.free[actj].beta);
   hand.constrainedRotate(hand.free[actj],best*actang);
   System.out.println("+++++++++++"+hand.free[actj].parent.label+" best angle:"+best*actang*180/Math.PI+" = "+abest*180/Math.PI);
    System.out.println("+++++++++++SO angle:"+hand.free[actj].beta*180/Math.PI);
     System.out.println("+++++++++++ best diff got:"+bestdiff);
   actj+=1;
   if (actj>=hand.freec)
    { mode=2; System.out.println("SWITCH to mode=1"); continue;}
   bestdiff=1e10; actc=0;
   hand.dumpAngles();
  }
  //System.out.println("mode:"+mode+" diff:"+diff);
   continue;
  }

  if (mode==0) j=(int)(3*Math.random());
  else if (mode==1) j=3+(int)((hand.freec-3)*Math.random());
  // else j=3+(int)((hand.freec-3)*Math.random());
   else j=(int)(hand.freec*Math.random());

 // System.out.println("No shuch mode");

  pick=hand.free[j];
 // if (mode!=0 && (pick==hand.wristx || pick==hand.wristx || pick==hand.wristx))
  //  System.out.println("######################## Shouldnot ha[ppen!!");
    

  if (Math.random()>0.5) ang=-(1+r*3*Math.random())*Math.PI/180;
                   else  ang= (1+r*3*Math.random())*Math.PI/180;

  hand.constrainedRotate(pick,ang);
  //pick.rotateJoint(ang);
  //hand.free[2].rotateJoint(0.5);
  hand.project(cv.eye);
  model.extract(f);
  diff=model.difference(real,f);



  while (diff<olddiff && Math.random()>0.3)
        {
            hand.constrainedRotate(pick,ang);
            olddiff=diff;
            hand.project(cv.eye);
            model.extract(f);
            diff=model.difference(real,f);
        }


  if (diff>olddiff)
    { hand.constrainedRotate(pick,-ang);
      hand.constrainedRotate(pick,-0.5*ang);
      hand.project(cv.eye);
      model.extract(f);
      diff=model.difference(real,f);
    }

  if ((olddiff==diff && diff<100)|| diff<10) conc++;
  else conc=0;
  olddiff=diff;
  if (mode==0 && (conc==15 || it==100 ) )
   {mode=3;r=1; actj=3; bestdiff=1e10; actc=0;
    hand.resetFingers();
    f=1;
    System.out.println("SWITHC to best initial fingers : mode="+mode);
    //myf= getFeature(mode);
    //olddiff=Fdiff(myf,f);

   }
  //System.out.println("mode:"+mode+" diff:"+diff);
  if (diff<10) goodc++;
      else goodc=0;

 if (goodc==50) {r=r*0.9; System.out.println("now r:"+r); goodc=0;}


 if (count==0) { System.out.println("LAST DIFF:"+diff);
                 r=r*0.99;
 if (r<0.01) r=0.5;
}
 count=(count+1)%50;
 it++;
 }
}

void stopMatch()
{
 stopRequest=true;
}

}

/*
 *
 * Erhan Oztop, 2000-2002  <br>
 * Source code by Erhan Oztop (erhan@atr.co.jp) <br>
 * Copyright August 2002 under <br>
 * University of Southern California Ph.D. publication copyright <br>
 */
 
