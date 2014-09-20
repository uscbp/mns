/**
 *
 * @author Erhan Oztop, 2001-2002 <br>
 * <br>
 * Source code by Erhan Oztop (erhan@atr.co.jp) <br>
 * Copyright August 2002 via <br>
 * University of Southern California Ph.D. publication copyright <br>
 */
 
class Graspable extends Object3d
{
 Point3d X,Y,Z;
    Point3d tilt=new Point3d(0,0,0);
 Segment[] hseg=null;//hand segmets
 int hsegc=0;
 Hand hand=null;     //hand
 Point3d target[]=null;  //limb targets
 String myname="noname";
 static int IDgenerator=0;
 public int ID=-1;
 public double objsize=0;  // emulates the object affordance. Learn uses this  
 
// later this will be a list of affordance classes
 static final String[] grasps={"PRECISION", "SIDE", "POWER","POWER2","REACHONLY","REACHOFFSET","REACHOFFSETBYNAME"};
 static final int graspc=grasps.length;
 public int affordance=-1;
 Point3d lastopposition=null;
 int tIndex[]=null;
 int tIc=0;

    Point3d wristTarget=new Point3d(0,0,0);
 int[] thumb={-1,-1,-1,-1};
 int[] index={-1,-1,-1};
 int[] middle={-1,-1,-1};
 int[] ring={-1,-1,-1};
 int[] pinky={-1,-1,-1};
 int wrist=-1;
 int fake=-1;
 int thumbTip=-1;
 int[][] fingers=new int[6][4];

 static final int NONE=0;
 static final int PRECISION=1;
 static final int POWER=2;
 static final int POWER2=3;
 static final int SIDE=4;
 static final int REACHONLY=5;
 static final int REACHOFFSET=6;
 static final int REACHOFFSETBYNAME=7;

 public int lastPlan=NONE;

 public double GRASP_TH=200; //reach to grasp transition error margin
 public double  RATE_TH=0.025; //was0.05
 public double BASERATE=0.0002; //

 Graspable(Hand hand,String name,int sidec,int rad,String afs)
 {
  super(name,sidec,rad);
  ID=Graspable.IDgenerator++; //start from 1
  myname=name;
  this.hand=hand;
  tIndex=new int[hand.segc];
  tIc=0;
  hseg=new Segment[hand.segc];
  System.arraycopy(hand.seg,0,hseg, 0,hand.segc);
  hsegc=hand.segc;
  setupHandValues();
  setupObjFrame();
  if (afs!=null) setAffordance(afs);
 } 

 Graspable(Hand hand,String name,int sidec,int rad)
 { this(hand,name,sidec,rad,null); 
   setPlaneProperties();
} //pick defualt affordance

 void setupObjFrame()
 {//fixed for now
  X=new Point3d(1,0,0);
  Y=new Point3d(0,1,0);
  Z=new Point3d(0,0,1);
 }
    
 public void setGraspablePlanes()
 {
     int u=0;
   objectCenter.x=0;
   objectCenter.y=0;
   objectCenter.z=0;
   for (int i=0;i<hpc;i++)
       { int k=i; //hindex[i];
       //System.out.println("Plane "+k+" Center:"+hplist[k].CP.str());
       //Mars.addStar(hplist[k].CP);
     u++;
     hplist[k].setCenter();
     hplist[k].setGeom();
     VA._add(objectCenter,hplist[k].CP); 
   }
   VA._scale(objectCenter,1.0/u);
   //System.out.println("Graspable center:"+objectCenter.str());
 }   
    static Point3d tilter=new Point3d(0,0,0);
 synchronized void setTilt(double t) {
	resetRot();
	tilter.z=t;
	Zrot(tilter.z);
    }
    synchronized void setTilt(Point3d p) {
	resetRot();
	Zrot(p.z);
    }

synchronized  void Zrot(double t) {
        setGraspablePlanes();
	tilt.z += t;

	Point3d cen=objectCenter;
	root._translate(VA.scale(cen,-1));
	root.Zrot(t);
        VA._Zrotate(X,t); VA._Zrotate(Y,t); VA._Zrotate(Z,t);
	root._translate(cen);
	
    } 

synchronized  void resetRot() {
	Zrot(-tilt.z);
}

 void setAffordance(String s)
 {
  int k=-1;
  for (int i=0;i<graspc;i++)
    if (s.equals(grasps[i])) k=i;
  if (k==-1) {System.err.println("No such grasp!"); return;}
  affordance=k;
 }

 static public String getGrasp(int i)
 {
  if (i<0) return "NONE";
  return grasps[i];
 }
 void natural ()
 {
  if (affordance<0) precision(); //default
  if (grasps[affordance].equals("PRECISION")) precision();
  else
  if (grasps[affordance].equals("SIDE")) side();
  else
  if (grasps[affordance].equals("POWER")) power();
  else
  if (grasps[affordance].equals("POWER2")) power2();
  else 
  if (grasps[affordance].equals("REACHONLY")) reachonly();
else
    if (grasps[affordance].equals("REACHOFFSET")) reachoffset();
  else  {System.err.println("internal error: No such grasp!"); return;}
 }

 void setupHandValues()
 {
  wrist=hand.getSegmentIndex("WRISTx");
  fake=hand.getSegmentIndex("FAKE");
  fingers[0][0] = thumb[0]=hand.getSegmentIndex("THUMB");
  fingers[0][1] = thumb[1]=hand.getSegmentIndex("THUMBin");
  fingers[0][2] = thumb[2]=hand.getSegmentIndex("THUMB2");
  fingers[0][3] = thumb[3]=hand.getSegmentIndex("THUMB3");

  if (thumb[3]>=0)  thumbTip=thumb[3];
           else    thumbTip=thumb[2];

  fingers[1][1] = index[0]=hand.getSegmentIndex("INDEX");
  fingers[1][2] = index[1]=hand.getSegmentIndex("INDEX1");
  fingers[1][3] = index[2]=hand.getSegmentIndex("INDEX2");

  fingers[2][1] = middle[0]=hand.getSegmentIndex("MIDDLE");
  fingers[2][2] = middle[1]=hand.getSegmentIndex("MIDDLE1");
  fingers[2][3] = middle[2]=hand.getSegmentIndex("MIDDLE2");

  fingers[3][1] = ring[0]=hand.getSegmentIndex("RING");
  fingers[3][2] = ring[1]=hand.getSegmentIndex("RING1");
  fingers[3][3] = ring[2]=hand.getSegmentIndex("RING2");

  fingers[4][1] = pinky[0]=hand.getSegmentIndex("PINKY");
  fingers[4][2] = pinky[1]=hand.getSegmentIndex("PINKY1");
  fingers[4][3] = pinky[2]=hand.getSegmentIndex("PINKY2");
}


public void beGrabbed()
{
 beGrabbed(-1,0);
}
public void beGrabbed(int k)
{
 beGrabbed(k,-1);
}


// grab but excluse joint exclude.jon. If jon==-1 means exclude.*
// exclude=-1 means no exclusion
public void beGrabbed(int exclude,int jon)
{ 

 double prec=5*Math.PI/180;
  int max=(int)(0.5+Math.PI/2.2/prec);


  for (int j=2;j<=3;j++)
  {
   for (int i=1;i<=4;i++) //run the fingers
   {
    if (i==exclude && (j==jon || jon==-1)) continue;
    int c=0;
    Segment pick=hand.seg[fingers[i][j]];
    pick.resetJoints();
    System.out.println("Picked:"+pick.label);
    do {
        hand.constrainedRotate(pick,prec);
        HV.cv.refreshDisplay();
       } while (hand.segmentCollision(this)<=0.0001 && c++<max);
    hand.constrainedRotate(pick,-2*prec);
   }
  }
}
public void beBabbleGrabbed()
{ 

  double prec=5*Math.PI/180;
  int max=(int)(0.5+Math.PI/2.2/prec);


  for (int j=0;j<=3;j++)
  {
      //for (int i=0;i<=4;i++) //run the fingers
      for (int i=0;i<=1;i++) //run the fingers
   {
    if (j<2 && i!=0) continue;
    if (i==0 && j==3) continue; //block thumb last joints move
    int c=0;
    Segment pick=hand.seg[fingers[i][j]];
    pick.resetJoints();
    System.out.println("Picked:"+pick.label);
    do {
        hand.constrainedRotate(pick,prec);
        HV.cv.refreshDisplay();
       } while (hand.segmentCollision(this)<=0.0001 && c++<max);
    hand.constrainedRotate(pick,-2*prec);
   }
  }
}
/*
  for (int j=0;j<=3;j++)
  {
    int c=0;
    if (j==1) continue;// c=max-10;
    Segment pick=hand.seg[fingers[0][j]]; //thumb
    System.out.println("Picked:"+pick.label);
    do {
        hand.constrainedRotate(pick,preferredValueIncrement);
        HV.cv.refreshDisplay();
       } while (hand.segmentCollision(this)<=0.0001 && c++<max);
    hand.constrainedRotate(pick,-preferredValueIncrement);
  }
*/



//                           thumb       opposer
public Point3d estimateWrist(Point3d P0, Point3d P1, double offset) 
 {
  Point3d dP =VA.subtract(P1,P0);
  Point3d handdir=VA.subtract(hand.wristx.limb_pos,P0);
  Point3d t1=VA.cross(handdir,dP);
  Point3d wr=VA.cross(dP,t1);
  VA._normalize(wr);
  VA._scale(wr,offset);
  return VA.add(wr,VA.center(P0,P1));
 }

 void side()
 {

  if (index[2]<0 || thumbTip<0) return ;
  
  if (HV.DLEV>0) System.out.println("Planning side....");
  lastPlan=SIDE;
  GRASP_TH=200; BASERATE=0.05; RATE_TH=0.1;
  setAvoidance();
  Plane[] PL=new Plane[6];
  Point3d[] PN=new Point3d[6];
  Point3d handdir;
  int PNc=0;
  
  setPlaneProperties();

  target=new Point3d[3];  // target angles to be achived

  tIc=0;                  //  the joints that corresponds to target
  tIndex[tIc++]=thumb[3];
  tIndex[tIc++]=index[1];
  tIndex[tIc++]=wrist;
  Segment thumb_seg=hand.seg[thumbTip];
  Segment index_seg=hand.seg[index[2]];
  Segment wrist_seg=hand.seg[wrist];

  if (myname.equals("mug.seg")) 
   {
    PN[0]=root.limb_pos.duplicate();
    PN[0].x-=50;
    PN[1]=PN[0].duplicate();
    PN[1].y-=50;

  HV.self.toggleTrace();
  Mars.clearComets();
   }
else
{
  Point3d ORG=VA.add(root.limb_pos,root.joint_pos);
  VA._scale(ORG,0.5);
  ORG=objectCenter;
  Point3d AXE=root.joint_axis;

  HV.self.toggleTrace();
  Mars.clearComets();
 
  Point3d opposition;
  //actually I should pick the thinnest side
 // else opposition=new Point3d(1,0,0);
  opposition=new Point3d(0,1,0);
  VA._normalize(opposition);
  PNc=lineIntersection(ORG, opposition, PL, PN);
  PNc=lineIntersection(PN[0].duplicate(), PL[0].normal, PL, PN);

  Point3d ppp; Plane pppl;
  if (PN[0].y < PN[1].y) //thumb up
       { ppp =PN[0]; PN[0]=PN[1]; PN[1]=ppp;
         pppl=PL[0]; PL[0]=PL[1]; PL[1]=pppl;
       }
  VA._resize(PN[0],PN[1],1.5);

}
  lastopposition=VA.subtract(PN[0],PN[1]);
  VA._normalize(lastopposition);

  PN[2]=estimateWrist(PN[0],PN[1],260);
  //PN[2].y-=150;
  if (myname.equals("mug.seg")) 
     PN[2]=VA.add(PN[0],new Point3d(200,0,0));
  System.out.println(PN[0].str()+","+PN[1].str()+","+ PN[2].str());
  ///Mars.addComet(VA.center(PN[0],PN[1]), PN[2],3);

  target=PN;

  ///Mars.addComet(target[0],target[1],10);
  objsize=VA.dist(target[1],target[0]);
  HV.self.toggleTrace();
  HV.cv.refreshDisplay();
 }

synchronized void reachoffset() {
    reachoffset(new Point3d(0,0,0));
}
synchronized void reachoffset(Point3d offset)
{
  if (HV.DLEV>0) System.out.println("Planning offset reach...");
  lastPlan=REACHOFFSET;
  GRASP_TH=2; BASERATE=0.01; RATE_TH=0.1;
  resetAvoidance();
  Plane[] PL=new Plane[6];
  Point3d[] PN=new Point3d[6];
  Point3d handdir;   
  int PNc=0;
  
  setPlaneProperties();

  target=new Point3d[3];  // target angles to be achived

  tIc=0;                  //  the joints that corresponds to target
  tIndex[tIc++]=wrist; // index[0]; // was middle[0] 
  target[0]=VA.add(objectCenter,offset);  
  // may need to add orientation targets too!!
  // hand flies open to the target and a touch initiates a closing.
  // try this for babble learning.
  // issue: initial orientation of hand or online corrections ?
  // what bout bottom and top grasps ?
}

synchronized void reachoffsetByName(String s) {
    reachoffsetByName(s,new Point3d(0,0,0));
}

synchronized void reachoffsetByName(String s, Point3d offset)
 {
  if (HV.DLEV>0) System.out.println("Planning offset reach ByName...");
  lastPlan=REACHOFFSETBYNAME;
  GRASP_TH=2; BASERATE=0.01; RATE_TH=0.1;
  resetAvoidance();
  Plane[] PL=new Plane[6];
  Point3d[] PN=new Point3d[6];
  Point3d handdir;   
  int PNc=0;
  
  setPlaneProperties();

  target=new Point3d[3];  // target angles to be achived

  tIc=0;                  //  the joints that corresponds to target
  tIndex[tIc]=index[2];    //default
  if (s.equals("INDEX2"))   tIndex[tIc++]=index[2];
  if (s.equals("INDEX1"))   tIndex[tIc++]=index[1];
  if (s.equals("INDEX0"))   tIndex[tIc++]=index[0];  
  if (s.equals("MIDDLE2"))  tIndex[tIc++]=middle[2];
  if (s.equals("MIDDLE1"))  tIndex[tIc++]=middle[1];
  if (s.equals("MIDDLE0"))  tIndex[tIc++]=middle[0];
  if (s.equals("THUMBTIP")) tIndex[tIc++]=thumbTip;
  if (s.equals("THUMB0")) tIndex[tIc++]=thumb[0];
  if (s.equals("THUMB1")) tIndex[tIc++]=thumb[1];
  if (s.equals("THUMB2")) tIndex[tIc++]=thumb[2];
  target[0]=VA.add(objectCenter,offset);  
  // may need to add orientation targets too!!
  // hand flies open to the target and a touch initiates a closing.
  // try this for babble learning.
  // issue: initial orientation of hand or online corrections ?
  // what bout bottom and top grasps ?
 }

 void reachonly()
 {

  if (HV.DLEV>0) System.out.println("Planning plain reach...");
  lastPlan=REACHONLY;
  GRASP_TH=2; BASERATE=0.01; RATE_TH=0.1;
  resetAvoidance();
  Plane[] PL=new Plane[6];
  Point3d[] PN=new Point3d[6];
  Point3d handdir;
  int PNc=0;
  
  setPlaneProperties();

  target=new Point3d[3];  // target angles to be achived

  tIc=0;                  //  the joints that corresponds to target
  tIndex[tIc++]=wrist;
  target[0]=objectCenter.duplicate();
 }

 void precision()
 {

  if (index[2]<0 || thumbTip<0) return ;
  
  if (HV.DLEV>0) System.out.println("Planning precision...");
  lastPlan=PRECISION;
  GRASP_TH=50; BASERATE=0.04; RATE_TH=0.25;
  resetAvoidance();
  Plane[] PL=new Plane[6];
  Point3d[] PN=new Point3d[6];
  Point3d handdir;
  int PNc=0;
  
  setPlaneProperties();

  target=new Point3d[3];  // target angles to be achived

  tIc=0;                  //  the joints that corresponds to target
  tIndex[tIc++]=thumbTip;
  tIndex[tIc++]=index[2];
  tIndex[tIc++]=wrist;
  Segment thumb_seg=hand.seg[thumbTip];
  Segment index_seg=hand.seg[index[2]];
  Segment wrist_seg=hand.seg[wrist];
  Point3d ORG=VA.add(root.limb_pos,root.joint_pos);
  VA._scale(ORG,0.5);
  ORG=objectCenter;
  Point3d AXE=root.joint_axis;

  HV.self.toggleTrace();
  Mars.clearComets();
 
  Point3d opposition;
  if (ORG.x<0) opposition=new Point3d(0,0,1);
  else opposition=new Point3d(1,0,0);
  VA._normalize(opposition);
  //-->Mars.addComet(ORG,VA.scale(opposition,150),1);

  PNc=lineIntersection(ORG, opposition, PL, PN);
//    if (PNc==0) {
//        ORG.x += (Math.random()-0.5)*0.1;
//        ORG.y += (Math.random()-0.5)*0.1;
//        ORG.z += (Math.random()-0.5)*0.1;
//        PNc=lineIntersection(ORG, opposition, PL, PN);
//    }
  PNc=lineIntersection(PN[0].duplicate(), PL[0].normal, PL, PN);
 
  /*
  if (VA.dist(PN[0],PN[1])>100) 
      { opposition=new Point3d(0,1,0);
  VA._normalize(opposition);
  PNc=lineIntersection(ORG, opposition, PL, PN);
  PNc=lineIntersection(PN[0].duplicate(), PL[0].normal, PL, PN);
      }
*/
  Point3d _dP=VA.subtract(PN[0],PN[1]);
  Point3d dP =VA.subtract(PN[1],PN[0]);
  Point3d dF =VA.subtract(index_seg.limb_pos , thumb_seg.limb_pos);


  Point3d ppp; Plane pppl;
  if (ORG.x<0  && PN[0].z>PN[1].z  ||
      ORG.x>=0 && PN[0].x>PN[1].x)
       { ppp =PN[0]; PN[0]=PN[1]; PN[1]=ppp;
         pppl=PL[0]; PL[0]=PL[1]; PL[1]=pppl;
       }

  VA._resize(PN[0],PN[1],1.4);
  lastopposition=VA.subtract(PN[0],PN[1]);
  VA._normalize(lastopposition);

// wr is the wrist offset from the center.
/*
  Point3d wr=VA.cross(AXE,dP);
  VA._normalize(wr);
  VA._scale(wr,250);
*/
  dP =VA.subtract(PN[1],PN[0]);
  handdir=VA.subtract(hand.wristx.limb_pos,PN[0]);
  Point3d t1=VA.cross(handdir,dP);
  Point3d wr=VA.cross(dP,t1);
  VA._normalize(wr);
  VA._scale(wr,250);
  PN[2]=VA.add(wr,VA.center(PN[0],PN[1]));
  ///Mars.addComet(VA.center(PN[0],PN[1]), PN[2],4);

  target=PN;

  //Mars.addComet(target[0],target[1],10);
  objsize=VA.dist(target[0],target[1]);
  HV.self.toggleTrace();
  HV.cv.refreshDisplay();
 }

/* Returns the quadrant which p is in. 
   000  0   +X  +Y  +Z 
   001  1   +X  +Y  -Z
   010  2   +X  -Y  +Z
   011  3   +X  -Y  -Z
   100  4   -X  +Y  +Z
   101  5   -X  +Y  -Z
   110  6   -X  -Y  +Z
   111  7   -X  -Y  -Z
*/
int quadrant(Point3d p) {
    int q=0;
    if (p.x<0) q+=4;
    if (p.y<0) q+=2;
    if (p.z<0) q+=1;
    return q;
}

/* thumb to index vector.*/
Point3d findPrecisionOpposition(Point3d P) {
    int q=quadrant(P);
    Point3d p=null;
    switch(q) {
    case 0: p=new Point3d(1,0,0);
	break;
    case 1: p=new Point3d(1,0,0);
	break;
    case 2: p=new Point3d(1,0,0);
	break;
    case 3: p=new Point3d(1,0,0);
	break;
    case 4: p=new Point3d(0,1,0);
	break;
    case 5: p=new Point3d(0,0,1);
	break;
    case 6: p=new Point3d(0,1,0);
	break;
    case 7: p=new Point3d(-1,0,0);
	break;
    }
    return p;
}

 void trial_precision()
 {

  if (index[2]<0 || thumbTip<0) return ;
  
 if (HV.DLEV>0) System.out.println("Planning precision...");
  lastPlan=PRECISION;
  GRASP_TH=50; BASERATE=0.04; RATE_TH=0.25;
  resetAvoidance();
  Plane[] PL=new Plane[6];
  Point3d[] PN=new Point3d[6];
  Point3d handdir;
  int PNc=0;
  
  setPlaneProperties();

  target=new Point3d[3];  // target angles to be achived

  tIc=0;                  //  the joints that corresponds to target
  tIndex[tIc++]=thumbTip;
  tIndex[tIc++]=index[2];
  tIndex[tIc++]=wrist;
  Segment thumb_seg=hand.seg[thumbTip];
  Segment index_seg=hand.seg[index[2]];
  Segment wrist_seg=hand.seg[wrist];
  Point3d ORG=VA.add(root.limb_pos,root.joint_pos);
  VA._scale(ORG,0.5);
  ORG=objectCenter;
  Point3d AXE=root.joint_axis;

  HV.self.toggleTrace();
  Mars.clearComets();
 
  Point3d opposition=findPrecisionOpposition(ORG);
 
  PNc=lineIntersection(ORG, opposition, PL, PN);
  PNc=lineIntersection(PN[0].duplicate(), PL[0].normal, PL, PN);

  Point3d _dP=VA.subtract(PN[0],PN[1]);
  Point3d dP =VA.subtract(PN[1],PN[0]);
  Point3d dF =VA.subtract(index_seg.limb_pos , thumb_seg.limb_pos);


  Point3d ppp; Plane pppl;
  if (ORG.x<0  && PN[0].z>PN[1].z  ||
      ORG.x>=0 && PN[0].x>PN[1].x)
       { ppp =PN[0]; PN[0]=PN[1]; PN[1]=ppp;
         pppl=PL[0]; PL[0]=PL[1]; PL[1]=pppl;
       }

  VA._resize(PN[0],PN[1],1.4);
  lastopposition=VA.subtract(PN[0],PN[1]);
  VA._normalize(lastopposition);

// wr is the wrist offset from the center.
/*
  Point3d wr=VA.cross(AXE,dP);
  VA._normalize(wr);
  VA._scale(wr,250);
*/
  dP =VA.subtract(PN[1],PN[0]);
  handdir=VA.subtract(hand.wristx.limb_pos,PN[0]);
  Point3d t1=VA.cross(handdir,dP);
  Point3d wr=VA.cross(dP,t1);
  VA._normalize(wr);
  VA._scale(wr,250);
  PN[2]=VA.add(wr,VA.center(PN[0],PN[1]));
  wristTarget.set(PN[2]);
  ///Mars.addComet(VA.center(PN[0],PN[1]), PN[2],4);

  target=PN;

  ///Mars.addComet(target[0],target[1],10);
  objsize=VA.dist(target[0],target[1]);
  HV.self.toggleTrace();
  HV.cv.refreshDisplay();
 }

 void power2()
 {

  if (index[2]<0 || thumbTip<0) return ;
  
  if (HV.DLEV>0) System.out.println("Planning power2...");
  GRASP_TH=50; BASERATE=0.04; RATE_TH=0.2;
  lastPlan=POWER2;
  resetAvoidance();
  Plane[] PL=new Plane[6];
  Point3d[] PN=new Point3d[6];
  Point3d handdir;
  int PNc=0;
  
  setPlaneProperties();

  target=new Point3d[3];  // target angles to be achived

  tIc=0;                  //  the joints that corresponds to target
  tIndex[tIc++]=thumbTip;
  tIndex[tIc++]=ring[2];
  tIndex[tIc++]=wrist;
  Segment thumb_seg=hand.seg[thumbTip];
  Segment index_seg=hand.seg[index[2]];
  Segment wrist_seg=hand.seg[wrist];
  Point3d ORG=VA.add(root.limb_pos,root.joint_pos);
  VA._scale(ORG,0.5);
  ORG=objectCenter;
  Point3d AXE=root.joint_axis;

  HV.self.toggleTrace();
  Mars.clearComets();
 
  Point3d opposition;
  if (ORG.x<0) opposition=new Point3d(0,0,1);
  else opposition=new Point3d(1,0,0);
  VA._normalize(opposition);
  PNc=lineIntersection(ORG, opposition, PL, PN);
  PNc=lineIntersection(PN[0].duplicate(), PL[0].normal, PL, PN);

  Point3d _dP=VA.subtract(PN[0],PN[1]);
  Point3d dP =VA.subtract(PN[1],PN[0]);
  Point3d dF =VA.subtract(index_seg.limb_pos , thumb_seg.limb_pos);


  Point3d ppp; Plane pppl;
  if (ORG.x<0  && PN[0].z>PN[1].z  ||
      ORG.x>=0 && PN[0].x>PN[1].x)
       { ppp =PN[0]; PN[0]=PN[1]; PN[1]=ppp;
         pppl=PL[0]; PL[0]=PL[1]; PL[1]=pppl;
       }

  VA._resize(PN[0],PN[1],1.4);
  lastopposition=VA.subtract(PN[0],PN[1]);
  VA._normalize(lastopposition);

// wr is the wrist offset from the center.
/*
  Point3d wr=VA.cross(AXE,dP);
  VA._normalize(wr);
  VA._scale(wr,250);
*/
  dP =VA.subtract(PN[1],PN[0]);
  handdir=VA.subtract(hand.wristx.limb_pos,PN[0]);
  Point3d t1=VA.cross(handdir,dP);
  Point3d wr=VA.cross(dP,t1);
  VA._normalize(wr);
  VA._scale(wr,250);
  PN[2]=VA.add(wr,VA.center(PN[0],PN[1]));
  ///Mars.addComet(VA.center(PN[0],PN[1]), PN[2],4);

  target=PN;

  ///Mars.addComet(target[0],target[1],10);
  objsize=VA.dist(target[0],target[1]); // is it?
  HV.self.toggleTrace();
  HV.cv.refreshDisplay();
 }


/* ERH
 you have to define a hand span vector set to be intersected with object.
*/

 void power()
 {

  if (index[2]<0 || thumbTip<0) return ;

  if (HV.DLEV>0)System.out.println("Planning power...");
  lastPlan=POWER;
  GRASP_TH=40; BASERATE=0.03; RATE_TH=0.15;
  setAvoidance();
  Plane[] PL=new Plane[6];
  Point3d[] PN=new Point3d[6];
  Point3d handdir;
  int PNc=0;

  HV.self.toggleTrace();
  Mars.clearComets();
  
  setPlaneProperties();
/*
  for (int i=0;i<hpc;i++)
  {
    Plane pl=hplist[i];
    ///Mars.addComet(pl.CP,VA.add(pl.CP,VA.scale(pl.normal,150)),1);
  }
*/
  

  target=new Point3d[3];  // target angles to be achived

  tIc=0;                  //  the joints that corresponds to target
  tIndex[tIc++]=ring[0]; //wrist;
  tIndex[tIc++]=thumb[2]; //thumbTip;

  Segment thumb_seg=hand.seg[thumbTip];
  Segment index_seg=hand.seg[index[2]];
  Segment wrist_seg=hand.seg[wrist];
  Point3d ORG=VA.add(root.limb_pos,root.joint_pos);
  VA._scale(ORG,0.5);
  Point3d AXE=root.joint_axis;


  Point3d opposition;      /// VA.subtract(ORG,hand.wristx.limb_pos);
  if (ORG.x<0) opposition=new Point3d(1,1.13,0);
  else opposition=new Point3d(0,1.13,-1);
  VA._normalize(opposition);

  PNc=lineIntersection(ORG, opposition, PL, PN);
  VA._resize(PN[0],PN[1],1.4);
  ///Mars.addComet(PN[0],PN[1],4);

  Point3d tempRi=null;
  if (PN[0].z-PN[0].y < PN[1].z-PN[1].y)
    tempRi=VA.subtract(PN[0],ORG);
  else
    tempRi=VA.subtract(PN[1],ORG);
  System.out.println(" middle Intersection:"+tempRi.str());
//-----
  if (ORG.x<0) opposition=new Point3d(0,0,-1);
  else opposition=new Point3d(-1,-1.13,0);
  VA._normalize(opposition);

  PNc=lineIntersection(ORG, opposition, PL, PN);
  VA._resize(PN[0],PN[1],1.4);
  ///Mars.addComet(PN[0],PN[1],7);

  Point3d tempT=null;
  if (PN[0].x+PN[0].z < PN[1].x+PN[1].z)
    tempT=VA.subtract(PN[0],ORG);
  else
    tempT=VA.subtract(PN[1],ORG);
  System.out.println(" thumb Intersection:"+tempT.str());



  int pnc=0;
  PN[pnc++]=tempRi;   //new Point3d(  20, 20,0); 
  PN[pnc++]=tempT;   //new Point3d(  20, 20,0); 

  lastopposition=VA.subtract(PN[0],PN[1]);
  VA._normalize(lastopposition);

  if (pnc!=tIc) {System.err.println("POWER grasp error!"); System.exit(0);}

  for (int i=0;i<pnc;i++) VA._add(PN[i],ORG);
  target=PN;

  //Mars.addComet(target[0],target[1],10);
  objsize=estimateSize();
  HV.self.toggleTrace();
  HV.cv.refreshDisplay();
 }

double[] init_cfg=null;
public void beforeAction()
{
    // System.out.println("---------------Before ACtion for "+lastPlan);
 init_cfg=new double[hand.segc];
 hand.storeAngles(init_cfg);
 maskHand(); //don't use in the error calculation
 if (lastPlan==POWER)
 {
  hand.wristz.resetJoints();
  if (objectCenter.x<0)
   {
    hand.constrainedRotate(hand.wristz,-80*Math.PI/180);
    hand.constrainedRotate(hand.wristy,-15*Math.PI/180);
   }
 }
 else
 if (lastPlan==SIDE)
 {
  hand.wristz.resetJoints();
  //hand.makeNeutral();
  hand.constrainedRotate(hand.wristy,80*Math.PI/180);
  hand.constrainedRotate(hand.wristx,40*Math.PI/180);
  resetAvoidance();
 }
 else
 if (lastPlan==REACHONLY)
 {
     //System.out.println("REACHONLY before");
     //hand.resetJoints();
     hand.truncateChildren("WRISTx");
 } 
 if (lastPlan==REACHOFFSET) {
     //hand.truncateChildren("WRISTx");
 }     
 else
 {
  //hand.constrainedRotate(hand.wristx,-Math.PI/2);
 }

}


public void afterAction()
{
 if (lastPlan==POWER)
 {
  beGrabbed();
  System.out.println("POWER it is!");
 } else 
  if (lastPlan==POWER2)
 {
  beGrabbed(3); //exclude ring
 } else
 if (lastPlan==SIDE)
 {
  beGrabbed(1,2);  //dont move the first joint of index
 } else
     if (lastPlan==REACHOFFSET) { // used at least by babbling
	 //hand.restoreChildren(init_cfg,"WRISTx");
     } else
 if (lastPlan==REACHONLY)
     {   //hand.resetJoints();
         hand.restoreChildren(init_cfg,"WRISTx");
     }
}
     
 

public void afterReach()
{
 unmaskHand(); // let the hand participate in minimization
 if (lastPlan==PRECISION) 
  {
    mask("WRISTx");
    //hand.makeNeutral();
  }
  if (lastPlan==POWER2)
  {
    mask("WRISTx");
    hand.makeNeutral();
  }
 if (lastPlan==SIDE)
  {
  setAvoidance();
    mask("WRISTx");
    //hand.makeFist();
  }
 if (lastPlan==REACHONLY)
     { 
       
     }

}


public void maskHand()
{
 if (lastPlan==PRECISION)
 {
  mask("THUMB3"); 
  mask("INDEX2");
 } else
 if (lastPlan==POWER)
 {
  mask("THUMB2"); 
  //mask("INDEX"); 
  //mask("RING"); 
 } else
 if (lastPlan==POWER2)
 {
  mask("THUMB3");
  mask("RING2");
 } else
 if (lastPlan==SIDE)
 {
  //mask("THUMB3");
  //mask("INDEX1");
  //mask("WRISTx");
 }  
 else 
 if (lastPlan==REACHOFFSET) { 
     // seems wrong place! hand.restoreChildren(init_cfg,"WRISTx");
 }
 else
     if (HV.DLEV>0) System.err.println("No grasp is programmed!!");
}


public void unmaskHand()
{
 if (lastPlan==PRECISION)
 {
  unmask("THUMB3");
  unmask("INDEX2");
 } else
 if (lastPlan==POWER)
 {
  unmask("THUMB2");
 } else
 if (lastPlan==POWER2)
 {
  unmask("THUMB3");
  unmask("RING2");
 } else
 if (lastPlan==SIDE)
 {
  //unmask("THUMB3");
  //unmask("INDEX1");
 } 
 else
 if (HV.DLEV>0) System.err.println("No grasp is programmed!!");
}



 public boolean avoidObjects=true;

 public void setAvoidance()
 {
  avoidObjects=true;
  if (HV.DLEV>0) System.out.println("Avoidance is ON.");
 }

 public void resetAvoidance()
 {
  avoidObjects=false;
  if (HV.DLEV>0) System.out.println("Avoidance is OFF.");
 }

 public void toggleAvoidance()
 {
  avoidObjects=!avoidObjects;
 }




 Point3d error_vector()
{
  Point3d  sum=new Point3d(0,0,0);
  int c=0;
  for (int i=0;i<tIc;i++)
   { int k=tIndex[i]; 
     if (k<0) continue;
     c++; 
     VA._add(sum,VA.subtract(target[i],hseg[k].limb_pos));
   }
  return sum;
}

static double softconGAIN=Resource.get("softconGAIN"); //500;
static final int softcon_J2_45=1;
static final int softcon_J2_60=2;

Point3d reach_error(int softcon) {
    Point3d p=new Point3d();
    p.x=grasp_error();
    switch(softcon) {
    case 0: p.y=0; break;
    case softcon_J2_45: p.y=softconGAIN*Math.abs(-Math.PI/4 - hand.j2.beta); break;
    case softcon_J2_60: p.y=softconGAIN*Math.abs(-Math.PI/3 - hand.j2.beta); break;
    }
    p.z=p.x+p.y;
    return p;
}

 double reach_error()
 {
  return grasp_error();
 }
 double grasp_error()
 {
  double sum=0;
  int c=0;
  for (int i=0;i<tIc;i++)
   { int k=tIndex[i]; 
     if (k<0) continue;
     c++; 
     sum+=VA.dist(hseg[k].limb_pos, target[i]);
   }
  if (avoidObjects)
  {
   double cl=hand.segmentCollision(this);
   if (cl!=0)   
    { 
       sum+=cl;
       //System.out.println("----------------------  Collision!!");
    }
  }
  return sum/c;
 }

 void mask(int l)
 {
    for (int i=0;i<tIc;i++)
     if (l==tIndex[i]) tIndex[i]*=-1;
 }

 void unmask(int l)
 {
    for (int i=0;i<tIc;i++)
     if (-l==tIndex[i])  tIndex[i]*=-1;
 }

 void mask(String s)
 {
    for (int i=0;i<tIc;i++)
     {int k=tIndex[i];
      if (k<0) continue;
      if (hand.seg[k].label.equals(s))
        { 
         tIndex[i]*=-1;
         if (HV.DLEV>0) System.out.println(s+" MASKED");
         return; 
        }
    }
 }

 void unmask(String s)
 {
    for (int i=0;i<tIc;i++)
     {int k=-tIndex[i];
      if (k<0) continue;
      if (hand.seg[k].label.equals(s))
        {
         tIndex[i]*=-1;
         if (HV.DLEV>0) System.out.println(s+" UNMASKED");
         return;
        }
    }
 }


 void unmaskAll()
 {
   for (int i=0;i<tIc;i++)
     if (tIndex[i]<0) tIndex[i]*=-1;

 }
}
/*
 *
 * Erhan Oztop, 2000-2002  <br>
 * Source code by Erhan Oztop (erhan@atr.co.jp) <br>
 * Copyright August 2002 under <br>
 * University of Southern California Ph.D. publication copyright <br>
 */
 
