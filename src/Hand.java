 
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

class Hand extends Object3d {
    Point3d X,Y,Z; 
    Point3d palmNormal,palmCenter; // palm: Normal.P + D = 0
    double palmD;
    Plane palmPlane;

Segment free[]=null;  // all degrees of freedom segments
int freec=0;
    Segment armfree[]=null;
    int armfreec=0;
// these are the non-flexible joints +1,+2 gives the flexible joints
// Exception: Thumb 14 15 is flexible
static final int Thumb=14;
static final int IndexFinger=11;
static final int MiddleFinger=8;
static final int RingFinger=5;
static final int Pinky=2;
static final int Wrist=1;

static public final int ARMJOINT=1;
static public final int WRISTJOINT=2;
static public final int HANDJOINT=3;
static public final int BASEJOINT=4;

    double palmThreshold=Resource.get("palmThreshold");
public Hand(String s)
{ super(s);
  setupHand();
  adjustHand();
}
public Hand(String s,int pipesidec,double piperad)
{ super(s,pipesidec,piperad);
  setupHand();
  adjustHand();
}

public void adjustHand()
{
  double diff=info3dHand();
  double cosang=VA.cos(pinky.joint_pos, index.joint_pos);
  double ang=Math.acos(cosang);
  System.out.println("The triangle wrist angle:"+Elib.nice(180*ang/Math.PI,1e2));
  System.out.println("The triangle knuckle miss match:"+Elib.nice(diff,1e2));
  //enlargePalm(Math.PI/3-ang);
  extendPalm(1.5*diff); // I modified diff normally no coeff. here
  cosang=VA.cos(pinky.joint_pos, index.joint_pos);
  ang=Math.acos(cosang);
  diff=info3dHand();
  System.out.println("NOW: The triangle wrist angle:"+Elib.nice(180*ang/Math.PI,1e2));
  System.out.println("The triangle knuckle miss match:"+Elib.nice(diff,1e2));

  makeStanding();
}
public Segment j1,j2,j3,j4,arm,wristx,wristy,wristz,pinky,ring,middle,index,thumb,kol;

public void setHandFrame()
{
 X=VA.subtract(pinky.joint_pos,index.joint_pos);
 Y=VA.subtract(index.limb_pos,index.joint_pos);
 Z=VA.cross(Y,X);
 VA._normalize(X);
 VA._normalize(Y);
 VA._normalize(Z);

 palmCenter=VA.add(pinky.joint_pos,index.joint_pos);
 VA._add(palmCenter,wristx.joint_pos);
 VA._scale(palmCenter,1.0/3);

 
 Point3d indexB=VA.subtract(index.joint_pos,palmCenter);
 Point3d wristB=VA.subtract(wristx.joint_pos,palmCenter);
 palmNormal=VA.cross(indexB, wristB);   // pointing the inside of the palm [we have left handed CF]  
 VA._normalize(palmNormal);
 palmD     =-VA.inner(palmNormal,palmCenter); 
 // palm plane has the eq: palmNormal.P + D = 0

 // actually above variable should be accesible via the plane structure
 // anyway..
 palmPlane=new Plane(index.joint_pos,pinky.joint_pos,wristx.joint_pos,
		     new Point(),new Point(), new Point());
}

int cmc=100;
public double orientPalmWristz(Object3d thing) {
    Point3d X=VA.subtract(pinky.joint_pos,index.joint_pos);
    Point3d Y=VA.subtract(wristx.joint_pos,index.joint_pos);
    Point3d palm=VA.cross(Y,X);      // palm normal
    VA._normalize(palm);
    //if (cmc>15) { cmc=0; Mars.clearComets();}
    ///Mars.addComet(index.joint_pos,VA.add(index.joint_pos,VA.scale(palm,400)),0);
    cmc++;
    
    
    Point3d Z=VA.subtract(wristz.limb_pos,wristz.joint_pos);  // arm direction
    //Mars.addComet(wristz.limb_pos,wristz.joint_pos,2);
    Point3d r=VA.cross(palm,Z);       // the plane normal of wrist rotat sweep
    VA._normalize(r);
    ///Mars.addComet(index.joint_pos,VA.add(index.joint_pos,VA.scale(r,400)),2);
    Point3d o=VA.subtract(thing.objectCenter,index.joint_pos);
    ///Mars.addComet(thing.objectCenter,index.joint_pos,3);
    double k1=VA.inner(palm,o);
    if (Math.abs(k1)<0.001) return 0;
    Point3d palmPrj=VA.scale(palm,k1);
    double k2=VA.inner(r,o);
    if (Math.abs(k2)<0.001) return 0;
    Point3d rPrj=VA.scale(r,k2);
    Point3d oPrj=VA.add(rPrj,palmPrj);
    VA._normalize(oPrj);
    //ERH  System.out.println("palm,r,o prj and Z:"+palmPrj.str()+","+rPrj.str()+","+oPrj.str()+","+Z.str());
    Point3d test=VA.cross(oPrj,palm);
    double ca=VA.cos(test,Z);
    //ERH System.out.println("Must be:+/- "+ca);
    
    double T=-VA.acos(oPrj,palm); 
    
    /*
    if (hitConstraint(wristz,T)) {
	T=-T;
	System.out.println("Angle [reversal]:"+T);
    } else
    */

    if (thing.objectCenter.x>index.joint_pos.x) {
	T=-T;
	System.out.println("Angle [reversed]:"+T);
    } else
	//System.out.println("Angle:"+T )
      ;
    return T;
    
}

Segment lw_shoulder,lw_elbow,lw_wrist,lw_indexKN,lw_pinkyKN,lw_thumbKN,
    lw_indexFG,lw_middleFG,lw_ringFG,lw_pinkyFG,lw_thumbFG;

private void printLWtarget(Segment s) {
    System.out.print(Elib.nice(s.limb_pos.x,1e4)+" "+Elib.nice(s.limb_pos.y,1e4)+" "+Elib.nice(-1*s.limb_pos.z,1e4)+" ");
}
public void dumpLWTargets(int session) {
    System.out.print("LWTSESSION "+session+" ");
    printLWtarget(lw_elbow);   
    printLWtarget(lw_wrist); 
    printLWtarget(lw_indexKN); 
    printLWtarget(lw_pinkyKN); 
    printLWtarget(lw_thumbKN); 
    printLWtarget(lw_indexFG);
    printLWtarget(lw_middleFG); 
    printLWtarget(lw_ringFG); 
    printLWtarget(lw_pinkyFG); 
    printLWtarget(lw_thumbFG);
    printLWtarget(lw_shoulder);
    System.out.println("");
}
public void setupHand()
{
 //-----------------------------
 
 free=new Segment[segc];  // more than enough, but it is OK
 armfree=new Segment[segc];  // more than enough, but it is O

 
 arm=kol=getJoint("WRISTz");
 wristx=getJoint("WRISTx");
 wristy=getJoint("WRISTy");
 wristz=getJoint("WRISTz");
 pinky=getJoint("PINKY").child[0];
 ring =getJoint("RING").child[0];
 middle=getJoint("MIDDLE").child[0];
 index=getJoint("INDEX").child[0];
 thumb=getJoint("THUMB");

 lw_elbow=getJoint("J3");
 lw_wrist=arm;
 lw_indexKN=getJoint("INDEX");
 lw_pinkyKN=getJoint("PINKY");
 lw_thumbKN=getJoint("THUMB");
 
 lw_indexFG = index.child[0];
 lw_middleFG= middle.child[0];
 lw_ringFG  = ring.child[0];
 lw_pinkyFG = pinky.child[0];
 lw_thumbFG = thumb.child[0].child[0].child[0];
 lw_shoulder=getJoint("BASE");

 setHandFrame();
 root.setuserTag(Hand.ARMJOINT,true);

 root.setuserTag(Hand.BASEJOINT,false);

 wristx.setuserTag(Hand.HANDJOINT,true);

 wristx.setuserTag(Hand.WRISTJOINT,false);
 wristy.setuserTag(Hand.WRISTJOINT,false);
 wristz.setuserTag(Hand.WRISTJOINT,false);
 int i=0;
 free[i++]=wristz; 
 free[i++]=wristx;
 free[i++]=wristy; 
 free[i++]=pinky; free[i++]=pinky.child[0];
 free[i++]=ring; free[i++]=ring.child[0];
 free[i++]=middle; free[i++]=middle.child[0];
 free[i++]=index; free[i++]=index.child[0];
 free[i++]=thumb; free[i++]=thumb.child[0];
 free[i++]=thumb.child[0].child[0];
 freec=i;

 armfreec=0;
 j1=armfree[armfreec++]=getJoint("J1");
 j2=armfree[armfreec++]=getJoint("J2");
 j3=armfree[armfreec++]=getJoint("J3");
 j4=armfree[armfreec++]=getJoint("J4");

 putJointConstraints();
}

ContactList cl=null;

void addseg_and_child(Segment tseg, Segment seg, Plane[] PL, Point3d[] PN) {
    if (seg==null) seg=tseg;
    int k=obj.segmentIntersection(seg,PL,PN);
    if (k!=0) cl.addContact(seg,PL[0],PN[0],tseg, tseg.torque);
    
    for (int i=0;i<seg.noch;i++) { // now each segment can give indep torque
	addseg_and_child(seg.child[i],seg.child[i],PL,PN);
	//addseg_and_child(tseg,seg.child[i],PL,PN);
    }
   
}
/** Returns the ContactList with Object obj. */
ContactList contact(Object3d obj) {
    if (cl==null) {
	cl=new ContactList(obj);
    } else cl.resetList(obj);

    // first index finger

    Point3d[] PN=new Point3d[13];
    Plane[] PL=new Plane[13];
    setHandFrame();   // does unnecessary things too!
    // done by Object3d tickbabble now:
    // int side=obj.sideOF(palmPlane); if (side!=1) return cl;
    addseg_and_child(index,null,PL,PN);
    if (HV.DLEV>0) System.out.println("Number of intersections of index "+cl.contc);
    addseg_and_child(thumb,null,PL,PN);
    if (HV.DLEV>0) System.out.println("Number of intersection of index+thumb:"+cl.contc);
    // THIS LOOKS LIKE A *BUG* TO ME Feb, 25 2002 
    // why you add the child again, addseg_... is recursive anyways ??
    /////////////////  addseg_and_child(thumb.child[0],null,PL,PN);

    // note that the thumb intersections are counted twice for two joints
    // at the thumb vertex
    addseg_and_child(middle,null,PL,PN);
    addseg_and_child(ring,null,PL,PN);
    addseg_and_child(pinky,null,PL,PN);
   if (HV.DLEV>0) System.out.println("Number1 of intersection of all fingers (thumb counted twice):"+cl.contc);
    ///cl.newton();
    double side=(VA.inner(palmNormal,obj.objectCenter) + palmD);
    if (side>0) { 
	int k=obj.lineIntersection(obj.objectCenter, palmNormal, PL,PN);
	if (HV.DLEV>0) System.out.println("@@@@@@@@@@@@@@@ OBJ INTERSECTION COUNT:"+k);
	Point3d G;
	if (k==1 || k==2) {  // pick the one closes to the palm
	    if (k==1) G=PN[0];
	    else if (VA.dist(PN[0],palmCenter) < 
		     VA.dist(PN[1],palmCenter))  G=PN[0];
	    else G=PN[1];
	    
	    double d=VA.dist(G,palmCenter);
	    
	    if (d<palmThreshold) {  // close enough
		if (HV.DLEV>0) System.out.println("Object facing the palm and close. Good.[d="+d+"]");
		cl.addContact(palmNormal,1);
	    } else {if (HV.DLEV>0) System.out.println("-------->Object is too far to the palm:"+d);}
	} else { if (HV.DLEV>0) System.out.println("----------> object cannot be intersected!!!!");}
    } else { if (HV.DLEV>0)System.out.println("Object not in the right side of the hand!");
    cl.graspCost=1e30;
    if (HV.DLEV>0) System.out.println("---------------------> Cost of grasping:"+cl.graspCost);
    return cl;
}
    
    double cost=cl.searchNewton();
    if (HV.DLEV>0) System.out.println("---------------------> Cost of grasping:"+cost);
    double Fnorm=VA.norm(cl.netForce);
    if (HV.DLEV>0) System.out.println("Net Force  :"+cl.netForce.str()+" norm:"+Fnorm);
    if (HV.DLEV>0) System.out.println("Net Torque :"+cl.netTorque.str()+" norm:"+VA.norm(((Hand)this).cl.netTorque));
    return cl;
}

// this will scale the hand by sc
public void scaleHand(double sc)
{
 root.scale(sc);
}

public void dumpAngles()
{
     System.out.println("Feature angles for the fingers:");
     Segment fing=null;
     Segment w=wristx;
     for (int k=0;k<5;k++)
     {
      if (k==0) { fing=pinky;}
      else if (k==1) { fing=ring;}
      else if (k==2) { fing=middle;}
      else if (k==3) { fing=index;}
      else if (k==4) { fing=thumb;}
     double bang=180/Math.PI*VA.acos(w.joint_pos2d,fing.joint_pos2d,
                                 fing.joint_pos2d,fing.limb_pos2d);
     double cang=180/Math.PI*VA.acos(fing.joint_pos2d,fing.limb_pos2d,
                               fing.limb_pos2d,fing.child[0].limb_pos2d);
     System.out.println(fing.parent.label+" base,center ang:"+bang+","+cang);
   }
}

public double info3dHand()
{
 double l0=VA.dist(pinky.joint_pos,wristx.joint_pos);
 double l1=VA.dist(index.joint_pos,wristx.joint_pos);
 double l2=VA.dist(index.joint_pos,pinky.joint_pos);

 System.out.println("pinky-wrist:"+Elib.nice(l0,1e4));
 System.out.println("index-wrist:"+Elib.nice(l1,1e4));
 System.out.println("index-pinky:"+Elib.nice(l2,1e4));
 
 return ((l0+l1)*0.5-l2);
}
public Point3d thumbDir()
{
  Point3d d=VA.subtract(thumb.child[0].child[0].child[0].limb_pos,thumb.joint_pos);
 VA._normalize(d);
 return d;
}
public Point3d indexAperDir()
{
  Point3d d=VA.subtract(index.child[0].limb_pos,thumb.child[0].child[0].child[0].limb_pos);
 VA._normalize(d);
 return d;
}
public double indexAperture()
{
 return
  VA.dist(index.child[0].limb_pos,thumb.child[0].child[0].child[0].limb_pos);
}
public Point3d indexApertureCenter()
{
 return
  VA.center(index.child[0].limb_pos,thumb.child[0].child[0].child[0].limb_pos);
}
public double sideAperture()
{
 return
  VA.dist(index.limb_pos,thumb.child[0].child[0].child[0].limb_pos);
}
public Point3d sideAperDir()
{
  Point3d d=VA.subtract(index.limb_pos,thumb.child[0].child[0].child[0].limb_pos);
 VA._normalize(d);
 return d;
}
public double middleAperture()
{
 return
 VA.dist(middle.child[0].limb_pos,thumb.child[0].child[0].child[0].limb_pos);
}

public void infoPalm()
 {
  int x0,y0,x1,y1,wx,wy;
  double l0,l1,l2;

  x0=pinky.joint_pos2d.x;
  y0=pinky.joint_pos2d.y;

  x1=index.joint_pos2d.x;
  y1=index.joint_pos2d.y;


  wx=wristx.joint_pos2d.x;
  wy=wristx.joint_pos2d.y;

  l0=VA.dist(x0,y0,wx,wy);
  l1=VA.dist(x1,y1,wx,wy);
  l2=VA.dist(x1,y1,x0,y0);

  double max=l0;
  if (l1>max) max=l1;
  if (l2>max) max=l2;
  double min=l0;
  if (l1<min) min=l1;
  if (l2<min) min=l2;

  double comp=max;
  if (min<max/2) comp+=min*.25;
  //System.out.println("TRI:("+x0+","+y0+") ("+x1+","+y1+") ("+wx+","+wy+")");
  System.out.println("L0,L1,L2:"+Elib.snice(l0,1e4,7)+" , "+Elib.snice(l1,1e4,7)+" , "+Elib.snice(l2,1e4,7)+"  ** MAX:"+Elib.snice(max,1e4,7)+" COMP:"+Elib.snice(comp,1e4,7));
 }


// this will make seg limb length equal to l
public void scaleHand(Segment seg, double l)
{
 double sl=seg.limblen();
 root.scale(l/sl);
}
// this will make seg1.joint_pos -seg2.joint_pos  distance  equal to l
public void scaleHand(Segment seg1,Segment seg2, double l)
{
 double jl=VA.dist(seg1.joint_pos ,seg2.joint_pos);
 root.scale(l/jl);
}


public void enlargePalm(double ang)
{
 Segment[] fing=new Segment[5];
 Segment wrist=getJoint("WRISTx");
 fing[0]=getJoint("PINKY");
 fing[1] =getJoint("RING");
 fing[2]=getJoint("MIDDLE");
 fing[3]=getJoint("INDEX");
 fing[4]=getJoint("THUMB");

 Point3d axis=new Point3d(0,0,1);
 for (int i=0;i<4;i++)
  {double t=0;
   if (i==0) t=+ang/2.0;
   if (i==1) t=+ang/4.0;
   if (i==2) t=-ang/4.0;
   if (i==3) t=-ang/2.0;
   //System.out.println("t:"+t);
   fing[i]._rotateLimb(wrist.joint_pos,axis,t);
   fing[i].child[0]._rotateJoint(wrist.joint_pos,axis,t);
  } 
}

public void extendPalm(double val)
{
 Segment[] fing=new Segment[5];
 Segment wrist=getJoint("WRISTx");
 fing[0]=getJoint("PINKY");
 fing[1] =getJoint("RING");
 fing[2]=getJoint("MIDDLE");
 fing[3]=getJoint("INDEX");
 fing[4]=getJoint("THUMB");

 Point3d axis=new Point3d(0,0,1);
 for (int i=0;i<4;i++)
  {double t=0;
   if (i==0) t=-val/2.0;
   if (i==1) t=-val/4.0;
   if (i==2) t= val/4.0;
   if (i==3) t= val/2.0;
   //System.out.println("t:"+t);
   //fing[i].child[0]._translate(t,0,0);
   fing[i]._translate(t,0,0);
  }
}

public double Fdiff(double[][] f1,double[][] f2)
{
 double diff=0;
 for (int i=0;i<f1.length;i++)
  diff+=(f1[i][0]-f2[i][0])*(f1[i][0]-f2[i][0]);
 return diff;
}

private void putJointConstraints()
{
 for (int i=0;i<freec;i++)
   setJointConstraint(free[i],0,+Math.PI/2+Math.PI/10);

 setJointConstraint(j1,-180*Math.PI/180,+180*Math.PI/180);
 // setJointConstraint(j2,-90*Math.PI/180,+90*Math.PI/180);
 setJointConstraint(j2,-90*Math.PI/180,+20*Math.PI/180);
 setJointConstraint(j3,-90*Math.PI/180,+90*Math.PI/180);
 setJointConstraint(j4,-90*Math.PI/180,+80*Math.PI/180);
 setJointConstraint(wristx,-90*Math.PI/180,+90*Math.PI/180);
 setJointConstraint(wristy,-20*Math.PI/180,+80*Math.PI/180);
 setJointConstraint(wristz,-180*Math.PI/180,+80*Math.PI/180);

 setJointConstraint(thumb,0.0,+Math.PI/2);
 setJointConstraint(thumb.child[0], 0,+Math.PI/4-0.1);  //was pi/2 -erh oct2001
 setJointConstraint(thumb.child[0].child[0],0.0,+Math.PI/2);
 setJointConstraint(thumb.child[0].child[0].child[0], 0.0,+Math.PI/2);

// these are mounting points of these fingers to the wrist.
 setJointConstraint(pinky.parent,0.0,0.0);
 setJointConstraint(ring.parent,0.0,0.0);
 setJointConstraint(middle.parent,0.0,0.0);
 setJointConstraint(index.parent,0.0,0.0);

}
/*  
public void constrainedRotate(Segment seg,double ang)
{
 //System.out.println(seg.label+" : "+seg.beta+" [tot:"+(seg.beta+ang)+".");
 double upper=Math.PI/2;
 double lower=0.0;


 if (seg==wristx || seg==wristy ||  seg==wristz) 
   {  lower=-Math.PI; upper=Math.PI;}
   //{  lower=-Math.PI/2; upper=Math.PI/2;}
 else 
  if (seg==thumb)
   {  lower=0; upper=Math.PI/5;}  //aperture closing
  else
  if (seg==thumb.child[0])
  { lower=-Math.PI/10; upper=Math.PI/2-0.1;}  // carrying thumb inside
  else if (seg==thumb.child[0].child[0]) 
    {lower=0; upper=Math.PI/2;}


 
 if ( (seg.beta+ang)>upper || (seg.beta+ang)<lower) 
 {
  System.out.println(seg.parent.label+" : AT THE LIMIT "+(180/Math.PI)*seg.beta+"."+" upper:"+(180/Math.PI)*upper+" lower:"+(180/Math.PI)*lower+" asked:"+ang*(180/Math.PI));
  return;
 }
 seg.rotateJoint(ang);
}
*/
 

public void makeFlat()
{
 resetJoints();
}

public void makeBabblePose() {
    resetJoints();
    //makeUpright();
    /*
    j1.rotateJoint(Math.PI/180*(-67.9));
    j2.rotateJoint(Math.PI/180*(-63.6));
    j3.rotateJoint(Math.PI/180*( 22.6));
    j4.rotateJoint(Math.PI/180*( 1));
    wristz.rotateJoint(Math.PI/180*(-25.7));
    */
}

public void makeFist()
{
 wristx.resetJoints();
 pinky.rotateEachJoint(Math.PI/2.5);
 ring.rotateEachJoint(Math.PI/2.3);
 middle.rotateEachJoint(Math.PI/2.2);
 index.rotateEachJoint(Math.PI/2.2);
 thumb.rotateJoint(23.7*Math.PI/180);
 thumb.child[0].rotateJoint(47.5*Math.PI/180);
 thumb.child[0].child[0].rotateJoint(90*Math.PI/180);
 thumb.child[0].child[0].child[0].rotateJoint(5*Math.PI/180);
}

public void makeNeutral()
{
 wristx.resetJoints();
 wristz.rotateJoint(-65*Math.PI/180);
 pinky.rotateJoint(52*Math.PI/180); pinky.child[0].rotateJoint(15*Math.PI/180);
 ring.rotateJoint(32*Math.PI/180); ring.child[0].rotateJoint(44*Math.PI/180);
 middle.rotateJoint(37*Math.PI/180); middle.child[0].rotateJoint(33*Math.PI/180);
 index.rotateJoint(50.2*Math.PI/180); index.rotateJoint(18.5*Math.PI/180);
 thumb.rotateJoint(60.7*Math.PI/180);
 thumb.child[0].rotateJoint(34.7*Math.PI/180);
 thumb.child[0].child[0].rotateJoint(36.9*Math.PI/180);
 thumb.child[0].child[0].child[0].rotateJoint(5*Math.PI/180);
}

public void makeUpright() {
    resetJoints();
    j4.rotateJoint(80*Math.PI/180);
}
public void makeStanding()
{
 resetJoints();
 makeNeutral();
}
public void resetJoints()
{
 root.resetJoints();
}
 
public void resetFingers()
{
 pinky.resetJoints();
 ring.resetJoints();
 middle.resetJoints();
 index.resetJoints();
 thumb.resetJoints();
}


double phase=0; 
public void act1(double freq,double amp)
{
  double v;
  int i=0;
  v=amp*Math.sin(freq*(phase-i*0.1));

  thumb.rotateJoint(0.25*v);  
  thumb.child[0].rotateJoint(0.5*v);
  thumb.child[0].child[0].rotateJoint(1.5*v);
  index.rotateJoint(v);  index.child[0].rotateJoint(v/2);
  middle.rotateJoint(v); middle.child[0].rotateJoint(v/2);
  ring.rotateJoint(v);   ring.child[0].rotateJoint(v/2);
  pinky.rotateJoint(v);  pinky.child[0].rotateJoint(v/2);
  phase+=0.1;
  //System.out.println("-------------------------");
  //root.printJointAngles();
}

public void act0(double freq,double amp)
{
  double v;
  int i=0;
  v=amp*Math.sin(freq*(phase-i*0.1));

  thumb.rotateJoint(v/2);  thumb.child[0].rotateJoint(v);
  index.rotateJoint(v);  index.child[0].rotateJoint(v/2);
  middle.rotateJoint(v); middle.child[0].rotateJoint(v/2);
  ring.rotateJoint(v);   ring.child[0].rotateJoint(v/2);
  pinky.rotateJoint(v);  pinky.child[0].rotateJoint(v/2);
  phase+=0.1;
  //System.out.println("-------------------------");
  //root.printJointAngles();
}

public double weirdval()
{
 return pinky.joint_pos2d.x*index.joint_pos2d.y
        -pinky.joint_pos2d.y*index.joint_pos2d.x; 
}


private void drawThickLine(Graphics g,int x0,int y0,int x1,int y1)
 {
  g.drawLine(x0,y0,x1,y1);
  g.drawLine(x0+1,y0,x1+1,y1);
  g.drawLine(x0,y0+1,x1,y1+1);

 }

public void drawTriangle(int midx,int midy,Graphics g)
{
 int x0=pinky.joint_pos2d.x;
 int y0=pinky.joint_pos2d.y;
 int x1=index.joint_pos2d.x;
 int y1=index.joint_pos2d.y;
 int wx=wristx.joint_pos2d.x;
 int wy=wristx.joint_pos2d.y;

 g.setColor(HV.pal.C[10]); //orange
 drawThickLine(g,x0+midx,midy-y0,x1+midx,midy-y1);  //pinkyknuckle to indexk.
 drawThickLine(g,x1+midx,midy-y1,midx,midy);   //to wrist
 drawThickLine(g,midx,midy,x0+midx,midy-y0);   //to back to pincky knuckle
}

}

/*
 *
 * Erhan Oztop, 2000-2002  <br>
 * Source code by Erhan Oztop (erhan@atr.co.jp) <br>
 * Copyright August 2002 under <br>
 * University of Southern California Ph.D. publication copyright <br>
 */
 
