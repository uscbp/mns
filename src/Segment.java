 
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
 * <hl>
  * <b> This class is the basis for joint connected objects. Even though the
  * class hierarcy doesn't show the connection, Object3d is intimately 
  * connected to Segment. Currently Object3d can have only one segment but
  * in future implementations Object3d may contain multiple segments.
  * </b> 
  * The Segment structure is highly recursive. Most of the functions operate
  * in a recursive fashion. The main idea is a segment is limb attached to
  * a parent via a joint. All the children will affected if a movement occurs
  * in that joint. 
  * see <a href="objects/erharm.seg"> the sample <b>.seg </b> file used for 
  * constructing segments.</a>
  * @see Object3d
  * @see Point3d
  * @see VA
*/
public class Segment
{
 /** unique number for this segment */
 public int id;
 /** number of children this segment has. */
 public int noch;
 /** Array of child segments. */
 public Segment[] child;
 /** parent segment. It is null for the root segment.*/
 public Segment parent;

 /** used for truncating the kinematics chain and pasting on later.*/
 public int nochSave=-1;
 /** The rotation axis of this segment. null means the joint is fixed.*/
 public Point3d joint_axis;
 /** The 3d location of the end of the segment.*/
 public Point3d limb_pos;
 /** The 3d location of the joint.*/
 public Point3d joint_pos;
 /** The projected position of the joint axis. This is for display purposes.*/
 public Point joint_axis2d;
 /** The projected position of the end of the segment.*/
 public Point limb_pos2d;

 public int userTag=0;
 /** The projected position of the joint.*/
 public Point joint_pos2d;
 /** The total angle the joint rotated in radians. It always reflects the
   * the angle of the joint from the inital created orientation.
   * note that Segment itself does not check the limits. An upper module
   * like Object3d or Hand should check it.
 */
 /* reference frame for this segment. X= limpos-jointpos, Z=joint_axis,
    Y=X crossproduct Z. note that X,Y,Z are orthanormal.
 */
 public Point3d X=new Point3d(),Y=new Point3d(),Z=new Point3d();
 public double beta,minbeta=-Math.PI,maxbeta= Math.PI;  
 public boolean jointconstraint=false;  //is not monitored by Segment

    /** When the joint cant turn, e.g. hit to object */
    public boolean blocked=false;
 /**  The force the joint receives. Force system not implemented yet. 
 */
 public double torque=0;
 public double force=0 ;
    /** the following two are the components of the force when acting on a
       rigid body */
    public double linForce=0;
    public double rotForce=0;
 /**  The label of the segment. This label will be filled form the .seg file 
   *  and can be used to get refrence to the segment.
 */
 public String label;
 /**  The is the radius used to extend the skeletal joint system into solid
 *    cylindirical system.
 */
 public double Rad  =5;
 /** The view depth of this segment (no effect of children) imposed. 
 */
 public double segmentdepth=0;
 /**  The is the # of sides used to extend the skeletal joint system into solid
 *    cylindirical system. 4 will make extend the skeletal system into 
 * rectangular prisims.
 */
 public int sidec=0;

 /** All the segments under the topmost. Valid only for the topmost segment.
  *  The list also includes the topmost as the 0th element. The order in seg[]
  *  correspondances to the ID's of the segments. Also this is the appearance
  *  order in .seg file 
 */
 public Segment[] seg=null;
 int segc=0;
/** Array of planes. Only the topmost of a segment should use this field. */
 public Plane[] plane;
/** # of planes in this segment. */
 int planec=0;
/** # of total planes as itself+descendants -not sure of validity- */
 int totalplanec=0;   //itself+descendants
/** Array of 3d points for this segment */
 public Point3d[] pool=null;
/** Array of (projected) 2d points for this segment */
 public Point[] pool2d=null;
/** number of points in the pool[] */
 int poolc=0;

/** The segments which will not be draw are marked with this one. This happnes
    when you need multiple axis on one point (like the wrist). You just
    want the rotations not to draw the joint. It is a nice trick to simulate
    ball joints. By default all the limb-joint will be drawn.
*/
 boolean nodraw=false;
/** This must be redundant. It used to be used to hold non-joint simple 3d
  * objects. Even though when the segment file is read the shape parsing
  * is done, the created shape never gets attached to segment. 
  * In the future, either it will be removed from segment or be part of
  * segment system.
  * @see Shape
*/
 private Shape shape; 

/** Create the specified Segment 
  * @see constructSegment
*/
 public Segment(String lb,int ident,int MAXchild,Point3d jpos,Point3d lpos,Point3d ax, int axistype, int pointcount, int planecount)
 {
  constructSegment(lb,ident,MAXchild,jpos,lpos,ax, axistype,pointcount,planecount);
 }

/** Create the specified Segment. Point and plane counts are loaded defaults.
  * @see constructSegment
*/
 public Segment(String lb,int ident,int MAXchild,Point3d jpos,Point3d lpos,Point3d ax,int axistype)
 {
  constructSegment(lb,ident,MAXchild,jpos,lpos,ax,axistype);
 }

 // note that only top-down traverse will get truncated.
 public void truncateChildren()
 {
  nochSave=noch;
  noch=0;
 }
 
 public void restoreChildren()
 {
  if (nochSave!=-1) 
  { noch=nochSave;
    nochSave=-1;
  } else {System.err.println("Nothing to restore!!"); }
 }

 public void _updateFrame()
 {
  //System.out.println("Frame for:"+label);
  if (joint_axis==null)
  { Z=new Point3d(0,0,1);
    Y=new Point3d(0,1,0);
    X=new Point3d(1,0,0);
    return;
  }
  Z=joint_axis.duplicate();
  VA._normalize(Z);
  X=VA.subtract(limb_pos,joint_pos);
  VA._normalize(X);
  Y=VA.cross(X,Z);

 }

 public void updateFrame()
 {
  _updateFrame();
  for (int i=0;i<noch;i++)
    child[i].updateFrame();
 }

 public void pullTo(Point3d target)
 {
  Point3d f=VA.subtract(target,limb_pos);
  applyForce(f);
  if (label.equals("WRISTz")) return;
  for (int i=0;i<noch;i++)
    child[i].pullTo(target);
 }
// make it additive and provide clearForce
 public void applyForce(Point3d F)
 {
  _updateFrame();
  if (joint_axis==null) 
  {
   force=0;
  } else
  {
   Point3d f=VA.normalize(F);
   Point3d torque=VA.cross(f,X);
   double dir=VA.inner(torque,Z);
   force=dir*10;
   System.out.println(label+" force:"+force);
  }
 }

 public void moveSegment(double rate)
 {
   double delta=rate*force;
   if (delta>0.1) delta=0.1;
   if (delta<-0.1) delta=-0.1;
   if (joint_axis!=null)
      if (beta<2*Math.PI && beta>-2*Math.PI) rotateJoint(delta);
   for (int i=0;i<noch;i++)
    child[i].moveSegment(rate);
 }
  
 
    int lwid=0;
    private int outpointOBJ() {
	System.out.println("OBJ-VECT "+"v "+limb_pos.x+" "+limb_pos.y+" "+limb_pos.z);
	lwid++;
	return lwid;
    }

    private void outlineOBJ(int son,int par) {
	System.out.println("OBJ-LINE "+"l "+son+" "+par);
    }

    public void outputOBJ() {
	int i=_outputOBJ(null,0,0);
	System.err.println("LW object output done.["+i+" points.]");
    }
    private int _outputOBJ(Segment parent, int parentlwid,int lwidfromup) {
	int mylwid;
        int k;
        lwid=lwidfromup;
	if (parent==null) {
	    lwid=0;
	    mylwid=outpointOBJ();
	} else {
	    mylwid=outpointOBJ();
	    outlineOBJ(mylwid,parentlwid);
	}
	k=lwid;
	for (int i=0;i<noch;i++) {
	    k=child[i]._outputOBJ(this,mylwid,k);
	}
	return k;
    }


    public void unBlock() {
	blocked=false;
	for (int i=0;i<noch;i++)
	    child[i].unBlock();
    }

 public void setupSolid(int c,double R,boolean recursive)
 {
  setupSolid(this,c,R,recursive);
 }

 /** This method extends the skeletal system (joint_pos-limb_pos) to a
   * cylinderical form. First argument specifies the number of sides, the 
   * next is the radius of the cylinder. The recursive option let's the 
   * function to descent into the children segments with same parameters
 */
 public void setupSolid(Segment root, int c,double R,boolean recursive)
 {
  int cor0=0,cor1=0,oldcor0=0,oldcor1=0,first0=0,first1=0;

  if (joint_axis!=null && !nodraw) 
  {
  sidec=c;
  Rad=R;
  if (label.equals("WRISTz")) Rad*=2;
  else if (label.equals("J3")) Rad*=2;
 
  int[][] cor=new int[sidec+1][2];
  int corc=0;

  // axis may not be perp. to limb sometimes. Get a perp. offset for solidify
  Point3d limbdir=VA.subtract(limb_pos,joint_pos);
  System.out.println(label+" limb dir :"+limbdir.str());
  System.out.println(label+" joint_axis dir :"+joint_axis.str());
  VA._normalize(limbdir);
  Point3d perpoff=VA.cross(joint_axis,limbdir);
  perpoff=VA.cross(limbdir,perpoff);
  VA._normalize(perpoff);

  Point3d offset=VA.scale(perpoff,Rad);
  System.out.println(label+" perp cover:"+offset.str());
  Point3d gap=VA.scale(limbdir,2);
 
  Point3d stem=VA.add(joint_pos,offset); VA._add(stem,gap);
  Point3d tip =VA.add(limb_pos,offset);
  double pie=Math.PI*2/sidec;
  for (int i=0;i<sidec;i++)
   { Point3d ps=VA.Lrotate(stem,joint_pos,limb_pos,i*pie);
     Point3d pt=VA.Lrotate(tip,joint_pos,limb_pos,i*pie);
     cor[corc][0]= add2pool(ps,new Point(0,0));
     cor[corc++][1]=add2pool(pt,new Point(0,0));
  }
  cor[corc][0]=cor[0][0]; //close it
  cor[corc][1]=cor[0][1]; //close it

  int texture=0; int viewside=0;
  for (int i=0;i<corc;i+=1)
    {
    int fill=9;
    int line=4;
    texture=1;
    //if (i*pie<Math.PI) {fill=8; line=5; texture=0;}
    Point3d normal=VA.normal(pool[cor[i][0]],pool[cor[i][1]],pool[cor[i+1][1]]);
    if (normal.y<0) {fill=8; line=5; texture=0;}


    root.addPlane(
    pool[cor[i][0]],pool[cor[i][1]],pool[cor[i+1][1]],pool[cor[i+1][0]],
    pool2d[cor[i][0]],pool2d[cor[i][1]],pool2d[cor[i+1][1]],pool2d[cor[i+1][0]],
    fill,line,viewside,texture);
    
    }

  
  Vector v1=new Vector(10);
  Vector v2=new Vector(10);
  for (int i=0;i<corc;i++) 
    {
      v1.addElement(pool[cor[i][0]]);
      v1.addElement(pool2d[cor[i][0]]);
      v2.addElement(pool[cor[i][1]]);
      v2.addElement(pool2d[cor[i][1]]);
    }
   
  root.addPlane(v1,2,2,0,-1);
  root.addPlane(v2,2,2,0,-1);
 }
 if (recursive)
    for (int i=0;i<noch;i++)
     child[i].setupSolid(root,c,R,recursive);
}

public void setupPool(Vector pnts)
 {
   setupPool(pnts,0);
 }

public void setupPool(Vector pnts,int extra)
 {
   int maxpool=0;
   if (pnts==null) maxpool=extra;
   else maxpool=extra+pnts.size();
   pool  = new Point3d[maxpool];
   pool2d= new Point  [maxpool];
   poolc=0;
   //System.out.println("reserved "+maxpool+" point spaces for "+label);
 }

/*
public void setupPool(int npool)
 {
   pool  = new Point3d[npool]; poolc=0;
   pool2d= new Point  [npool]; 
 }

*/

public void setupPlane(int nplane,int extra)
 {
   plane = new Plane[nplane+extra] ; planec = 0;
 }

public void setupPlane(int nplane)
 { setupPlane(nplane,0);
 }


public void limbpoints2pool(Vector v)
 {
  limbpoints2pool(v,new Point3d(0,0,0));
 }

/** Adds the points defined in the segment file into the point pool. */
public void limbpoints2pool(Vector v, Point3d orig)
 {
   if (v==null) return;
   Enumeration e=v.elements();
   while (e.hasMoreElements())
   {
    Point3d pk=((Point3d)e.nextElement());
    add2pool(VA.add(orig,pk), new Point(0,0));
   }
 }

public void updateGeom()
 {
    for (int i=0;i<planec;i++)
   {
     plane[i].setCenter();
     plane[i].setGeom();
   }
 }
public void updateDepth(Eye eye,int mode)
 {
  int txt;
  segmentdepth=0;
  for (int i=0;i<planec;i++)
   {
     plane[i].setCenter();

     int fillc=3; int linec=5;
     if (mode==HV.SHOWSHADE || mode==HV.SHOWLSHADE)
     {
       Point3d u1=VA.subtract(plane[i].P[0],plane[i].P[1]);
       Point3d u2=VA.subtract(plane[i].P[0],plane[i].P[2]);
       Point3d pr=VA.cross(u1,u2);
       double shade = VA.cos(pr,eye.Z); //light source is on the eye
       txt=plane[i].texture;
       fillc=0;
       if (txt==0) {fillc=20+(int)(0.5+31*Math.abs(shade));} 
             else  {fillc=20+32+(int)(0.5+31*Math.abs(shade)); }
       if (mode==HV.SHOWLSHADE) linec=0; //changed from 5 by ERH
                         else   linec=-1;
     }
     else if (mode==HV.SHOWHIDDEN)
     {linec=0;  //black
      fillc=5; //cyanq
     } else if (mode==HV.SHOWSOLID)
     {
      if (plane[i].texture==0)
        {fillc=10+31; linec=3;}
      else  if (plane[i].texture==1)
        {fillc=10+32+31; linec=4;}
      else if (plane[i].texture==-1)
        {fillc=2; linec=2; } 

     }

    plane[i].fill_color=fillc;
    plane[i].line_color=linec;

    double depth=VA.dist(plane[i].CP,eye.Fpos);
    segmentdepth+=depth;
    plane[i].depth=depth;
   }
  segmentdepth/=planec;
 }

public void vplanes2plane(Vector v)
  {
   System.out.println("For "+label+" the segc:"+segc+" and segments:");
   for (int i=0;i<segc;i++)
    { System.out.print(seg[i].label+" "); }
   System.out.println("");
   if (v==null) return;
   Vector cvec=null;
   Enumeration pp=v.elements();
   while ( pp.hasMoreElements())
    {
      int[] pl=(int[])pp.nextElement();
      System.out.println("array len:"+pl.length);
      cvec=new Vector(pl.length);
      for (int i=0;i<pl.length;i+=2)
       { 
         cvec.addElement(seg[pl[i]].pool[pl[i+1]]); 
         cvec.addElement(seg[pl[i]].pool2d[pl[i+1]]); 
       }
       addPlane(cvec,5,7);
     }
  }
/*
      //System.out.println("");
      //for (int i=0;i<8;i++) System.out.print(pl[i]+" ");
      //System.out.println("\nAdding plane to:"+label);
      addPlane(
                          seg[pl[0]].pool[pl[1]],
                          seg[pl[2]].pool[pl[3]],
                          seg[pl[4]].pool[pl[5]],
                          seg[pl[6]].pool[pl[7]],
                          seg[pl[0]].pool2d[pl[1]],
                          seg[pl[2]].pool2d[pl[3]],
                          seg[pl[4]].pool2d[pl[5]],
                          seg[pl[6]].pool2d[pl[7]],5,7);

    }
   //System.out.println("=======Object"+label+" has now "+planec+" planes");
 }
*/

/** Returns and SETS the <b>totalplanec</b> of the segments */
 public int setTotalplanec()
 {
   totalplanec=planec;
   for (int i=0;i<noch;i++)
     totalplanec+=child[i].setTotalplanec();
   return totalplanec;
 }

   
/** calls constructSegment with 40 points and 20 planes. -check code- */
 public void constructSegment(String lb,int ident,int MAXchild,Point3d jpos,Point3d lpos,Point3d ax,int axistype)
 { constructSegment(lb,ident,MAXchild,jpos,lpos,ax,40,20,axistype);
 }

/** Sets up the variables and the arrays required to hold this segment.*/
 public void constructSegment(String lb,int ident,int MAXchild,Point3d jpos,Point3d lpos,Point3d ax,int MAXpoint, int MAXplane,int axistype)
 {
  label=lb;
  id=ident;
  child=new Segment[MAXchild];
  parent=null;
  limb_pos=lpos;
  joint_pos=jpos;
  if (VA.norm(ax)==0 || axistype==0) joint_axis=null; // fixed joint
  else if (axistype==-1)
     joint_axis=VA.cross(ax,VA.subtract(jpos,lpos));
  else joint_axis=ax;

  if (VA.dist(joint_pos,limb_pos)<0.0000001) nodraw=true;
  limb_pos2d=new Point(0,0);
  joint_pos2d=new Point(0,0);
  joint_axis2d=new Point(0,0);
  noch=0;
  beta=0; if (seg_pan!=null) update_panel();
 }

/** This is the constructor used to create a segment (and its descendants!) from
  * a .seg file.
  * @see readSegment
*/
 public Segment(String s)
 {
  this(s,0,0);
 }
 public Segment(String s,int extrapool,int extraplane)
 {
  Segment sg=readSegment(s,extrapool, extraplane);
  if (sg==null) System.err.println("Cannot create Segmen from file:"+s+"!!");
 }

/** Add the kid segment to this segment */
 public void addChild(Segment kid)
 {
  child[noch++]=kid;
  kid.parent=this;
  //System.out.print("Child added:");
  //System.out.println("-->"+str());
 }


/** Add 3d and corresponding 2d point to the point pool. */
public int add2pool(Point3d p,Point pp)
{
 //System.out.println(" ----> "+label+" adding point:"+p.str()+" poolc:"+poolc+"i pool:  "+pool+" pool2d:  "+pool2d);
 pool[poolc++]=p;
 pool2d[poolc-1]=pp;
 return poolc-1;
}

/** Add the plane defined by vector of 3d,2d pairs */
public int addPlane(Vector v)
{ return addPlane(v,0,1);
}

/** Add the plane defined by vector of 3d,2d pairs */
public int addPlane(Vector v,int fillcol,int linecol)
{ 
 Plane p=new Plane(v);
 p.setColor(fillcol,linecol);
 plane[planec++]=p;
 return planec-1;
}

/** Add the plane defined by vector of 3d,2d pairs */
public int addPlane(Vector v,int fillcol,int linecol, int side,int texture)
{ 
 Plane p=new Plane(v);
 p.setColor(fillcol,linecol);
 p.texture=texture;
 p.side=side;
 plane[planec++]=p;
 return planec-1;
}



/** Add the plane given by pointer  (from pool[]) with default color. */
public int addPlane(Point3d P0,Point3d P1,Point3d P2,Point3d P3,
              Point r0  ,Point r1  ,Point r2  , Point r3)
{ return addPlane(P0,P1,P2,P3,r0,r1,r2,r3,1,0);
}

/** Add the plane given by by pointer (from pool[]) with given fill 
  * and line color. 
*/
public int addPlane(Point3d P0,Point3d P1,Point3d P2,Point3d P3,
              Point r0  ,Point r1  ,Point r2  , Point r3,int fillcol,int linecol)
{
 Plane p=new Plane(P0,P1,P2,P3,r0,r1,r2,r3);
 p.setColor(fillcol,linecol);
 plane[planec++]=p;
 return planec-1;
}

public int addPlane(Point3d P0,Point3d P1,Point3d P2,Point3d P3,
              Point r0  ,Point r1  ,Point r2  , Point r3,int fillcol,int linecol,int side,int texture)
{
 Plane p=new Plane(P0,P1,P2,P3,r0,r1,r2,r3);
 p.setColor(fillcol,linecol);
 p.texture=texture;
 p.side=side;
 plane[planec++]=p;
 return planec-1;
}

/** Recursively reset the joint angles to zero. It does the required rotations
  * to achive this.
 */
 public void resetJoints()
 {
   //if (HV.DLEV>0) System.out.println("reset joints for id:"+id);
    if (beta!=0) 
	if (joint_axis!=null) rotateJoint(-beta);
    for (int i=0;i<noch;i++)
     child[i].resetJoints();
 }

/** Returns a vector of segments including this and its descendants.*/
 public Vector list()
 {
  Vector v=new Vector(40);
  _list(v,this);
  return(v);
 }

/** Used by <b>list()</b> to fetch the segments and its descendants.*/
 private void _list(Vector v, Segment seg)
 {
  v.addElement(seg);
  for (int i=0;i<seg.noch;i++)
    _list(v,seg.child[i]);
 }

/** Searches the segment for the labeled segment and returns the refrence.*/
 public Segment getJoint(String lb)
 {
  if (label.equals(lb)) return this;
  else   
  for (int i=0;i<noch;i++)
    { Segment r= child[i].getJoint(lb);
      if (r!=null) return r;
    }
  return null;
 }

 public void setuserTag(int what, boolean recursive)
 {
  userTag=what;
  if (recursive) 
    for (int i=0;i<noch;i++)
       child[i].setuserTag(what,true);
 }

 public int getuserTaget()
 {
  return userTag;
 }

/** Recersively prints the segment.*/
 private void recprint(Segment seg)
 {
  System.out.println("POOLC:"+seg.poolc);
  for (int i=0;i<seg.poolc;i++) System.out.println("pool["+i+"]:"+seg.pool[i].str());
  System.out.println(seg.str());
  for (int i=0;i<seg.noch;i++)
    recprint(seg.child[i]);
 }


/** Recersively prints the joint angles. This way it gives the state of the
  * joint object.
 */
 public void printJointAngles()
 {
  if (joint_axis==null) System.out.println(label+" NO JOINT");
  else System.out.println(label+" : "+ (int)(100*beta*180/Math.PI)/100.0+" deg.");
  for (int i=0;i<noch;i++)
    child[i].printJointAngles();
 }

/** Recersively prints the segment.
  * @see recprint
  */
public void print()
 {
  System.out.println("*PRE-ORDER TRAVERSAL*");
  recprint(this);
 }

/** returns the cosine of the angle between this limb and the other */
public double cosineTo(Segment other)
 {
  Point3d mine=VA.subtract(limb_pos,joint_pos);
  Point3d its =VA.subtract(other.limb_pos,other.joint_pos);
  return VA.cos(its,mine);
 }

/** returns the cosine of the angle between this projected limb and the other */
public double cosineTo2d(Segment other)
 {
  Point3d ml=VA.promote3d(limb_pos2d);
  Point3d mj=VA.promote3d(joint_pos2d);
  Point3d ol=VA.promote3d(other.limb_pos2d);
  Point3d oj=VA.promote3d(other.joint_pos2d);

  Point3d mine=VA.subtract(ml,mj);
  Point3d its =VA.subtract(ol,oj);
  return VA.cos(its,mine);
 }

/** Returns a string describing the segment. No child recursion is done. */
 public String str()
 {int pid;
  String jos;
  if (parent==null) pid=0;
  else pid=parent.id;
  if (joint_axis==null) jos="*NULL*";
                 else   jos=joint_axis.str();
  return label+" ID:"+id+" cc:"+noch+" jpos:"+joint_pos.str()+" lpos:"+limb_pos.str()+" rot axis:"+jos+" PAR:"+pid;
 }

 // for now eye can only be on Z axis
 /** project the joint_axis using eye. Normally project_axis need
   * not be projected.
 */
 public void projectaxis(Eye eye)
 {
    Point3d p=joint_axis;
    if (p==null) return;
    double x=joint_pos.x+50*p.x;
    double y=joint_pos.y+50*p.y;
    double z=joint_pos.z+50*p.x;
    eye._project(new Point3d(x,y,z),joint_axis2d);
 }

/** Project the pool/plane system points to pool2d. Note that the skeletal
  * projection is seperate from this process.
  */
 public void projectSolid(Eye eye)
 {
  
  //System.out.println("Solid projecting for:"+label);
  for (int i=0;i<poolc;i++)
  {
  eye._project(pool[i] ,pool2d[i]);
  }

 }

/** Project limb_pos, joint_pos (the skeletal system) using eye. Note that
    solid system is seperate.
*/
 public void project(Eye eye)
 {
  eye._project(limb_pos ,limb_pos2d);
  eye._project(joint_pos,joint_pos2d);
  projectaxis(eye);
  projectSolid(eye);

  for (int i=0;i<noch;i++)
    child[i].project(eye);
 }

/** This method rotates this+descendant segments joints by T radians.*/
 public void rotateEachJoint(double T)
 { if (joint_axis!=null) rotateJoint(T);
   for (int i=0;i<noch;i++)
    child[i].rotateEachJoint(T);
 }

/** This method rotates this segment's joint by T radians. The descending
  * segments are adjusted recursively using _rotateLimb and _rotateJoint. 
  * @see _rotateLimb
  * @see _rotateJoint
  */

 
 public void updateAllPanel()
 {
  if (seg_pan!=null) update_panel();
  for (int i=0;i<noch;i++)
    child[i].updateAllPanel( );
 }

 public void disablePanel()
 {
  savePan=seg_pan;
  seg_pan=null;
 }

 public void enablePanel()
 {
  if (savePan==null) return;
  seg_pan=savePan;
  savePan=null;
 }
 public void rotateJoint(double T)
 {
  if (joint_axis==null) return;
  // {System.err.println(label+" : No joint defined!"); return;}
  beta+=T; 
  if (seg_pan!=null) update_panel();

 
 //let's rotate the limbs connected to the joint first
   _rotateLimb(joint_pos,joint_axis,T);

 // now lets rotate the joint connected to this joint
  for (int i=0;i<noch;i++)
   child[i]._rotateJoint(joint_pos,joint_axis,T);
 } 

/** This method is called to rotate a JOINT because of a parent segment
  * rotation. The method adjust the axis and position of the joint. 
  * the rotate command is recursivelt transmitted to children
*/
 public void _rotateJoint(Point3d place, Point3d around, double T)
 { 
     //first rotate the axis of the  joint
     VA._translate(joint_axis,joint_pos.x-place.x,joint_pos.y-place.y,joint_pos.z-place.z);
     VA._rotate(joint_axis,around,T);
     VA._translate(joint_axis,place.x,place.y,place.z);
  
     //now rotate joint_pos (the connecting parts)
     VA._translate(joint_pos,-place.x,-place.y,-place.z);
     VA._rotate(joint_pos,around,T);
     VA._translate(joint_pos,place.x,place.y,place.z);
     VA._subtract(joint_axis,joint_pos);
     
   for (int i=0;i<noch;i++)
     child[i]._rotateJoint(place,around,T);
 } 

/** This method rotates the solid system (pool[]). This is NOT recursive. */
  private void _rotateSolid(Point3d place,Point3d around,double T)
 {                      //tip
  //System.out.println("rotate solid for:"+label);
  for (int i=0;i<poolc;i++)
  {
  VA._translate(pool[i],-place.x,-place.y,-place.z);
  VA._rotate(pool[i],around,T);
  VA._translate(pool[i],place.x,place.y,place.z);
  }
 }

/** This method rotates the limb (limb_pos) around the joint. If there is
    any solid system a call to _rotateSolid is made. Then the method
    recurses down to the children.
*/
 public void _rotateLimb(Point3d place,Point3d around,double T) 
 {                      //tip
  VA._translate(limb_pos,-place.x,-place.y,-place.z);
  VA._rotate(limb_pos,around,T);
  VA._translate(limb_pos,place.x,place.y,place.z);

  //System.out.println("_ratateLimb:dimensionName,poolc:"+label+","+poolc);
  if (poolc!=0) _rotateSolid(place,around,T);
  for (int i=0;i<noch;i++)
   child[i]._rotateLimb(place,around,T);
 } 

/** Moves the segment (and descendants) to a new position */
 public void movetoy(Point3d newposition)
 {
  double dx=newposition.x - joint_pos.x;
  double dy=newposition.y - joint_pos.y;
  double dz=newposition.z - joint_pos.z;
  _translate(dx,dy,dz);
 }
  
public void _translate(Point3d delta) {
    _translate(delta.x,delta.y,delta.z);
}

/** Translates the segment and its descendant by dx,dy,dz. */
 public void _translate(double dx,double dy, double dz)
 {
  joint_pos.x+=dx;
  joint_pos.y+=dy;
  joint_pos.z+=dz;
  limb_pos.x+=dx;
  limb_pos.y+=dy;
  limb_pos.z+=dz;
  for (int i=0;i<poolc;i++)
   { pool[i].x+=dx;
     pool[i].y+=dy;
     pool[i].z+=dz;
   }
  for (int i=0;i<noch;i++)
    child[i]._translate(dx,dy,dz);
 }


public void mirror()
 {
  limb_pos.x*=-1;
  joint_pos.x*=-1;
  //if (joint_axis!=null) joint_axis.x*=-1;
  for (int i=0;i<poolc;i++) pool[i].x*=-1;
  for (int i=0;i<noch;i++)
    child[i].mirror();

 }
/** Scales the segment and its descendant by sc. */
 public void scale(double sc)
 {
  joint_pos.x*=sc;
  joint_pos.y*=sc;
  joint_pos.z*=sc;
  limb_pos.x*=sc;
  limb_pos.y*=sc;
  limb_pos.z*=sc;
  for (int i=0;i<poolc;i++)
   { pool[i].x*=sc;
     pool[i].y*=sc;
     pool[i].z*=sc;
   }
  for (int i=0;i<noch;i++)
    child[i].scale(sc);
 }

/** Scales the segment and its descendant by sc taking place as the origin. */
 public void scale(Point3d place,double sc)
 {
  _translate(-place.x,-place.y,-place.z);
  joint_pos.x*=sc;
  joint_pos.y*=sc;
  joint_pos.z*=sc;
  limb_pos.x*=sc;
  limb_pos.y*=sc;
  limb_pos.z*=sc;
  for (int i=0;i<poolc;i++)
   { pool[i].x*=sc;
     pool[i].y*=sc;
     pool[i].z*=sc;
   }
  _translate(place.x,place.y,place.z);
  for (int i=0;i<noch;i++)
    child[i].scale(place,sc);
 }


/** returns the length of this segment |limb_pos-joint_pos|. */
 double limblen()
 {
  return VA.dist(limb_pos,joint_pos);
 }

/** rotate segment tree around X axis.*/
 public void Xrot(double t)
 {
  VA._Xrotate(joint_pos,t);
  VA._Xrotate(limb_pos,t);
  VA._Xrotate(joint_axis,t);
  for (int i=0;i<poolc;i++)
      VA._Xrotate(pool[i],t);
  //VA.Xrotate()
  for (int i=0;i<noch;i++)
     child[i].Xrot(t);
 }

/** rotate segment tree around Y axis.*/
 public void Yrot(double t)
 {
  VA._Yrotate(joint_pos,t);
  VA._Yrotate(limb_pos,t);
  VA._Yrotate(joint_axis,t);
  for (int i=0;i<poolc;i++)
      VA._Yrotate(pool[i],t);
  //VA.Yrotate()
  for (int i=0;i<noch;i++)
     child[i].Yrot(t);
 }

/** rotate segment tree around Z axis.*/
 public void Zrot(double t)
 {
  VA._Zrotate(joint_pos,t);
  VA._Zrotate(limb_pos,t);
  VA._Zrotate(joint_axis,t);
  for (int i=0;i<poolc;i++)
      VA._Zrotate(pool[i],t);
  //VA.Zrotate()
  for (int i=0;i<noch;i++)
     child[i].Zrot(t);
 }

private boolean easy_num(String u)
{
  if ( (u.charAt(0)>='0' && u.charAt(0)<='9') ||
   u.charAt(0)=='-' || u.charAt(0)=='+') return true;
  return false;
}

private int resolve(String u,Vector vlimbs, Vector labels)
{
  Enumeration v=vlimbs.elements();
  Enumeration l=labels.elements();
  int findid=-1;
  while (l.hasMoreElements())
  {
   String ss=(String)l.nextElement();
   double[] dd=(double[])v.nextElement();
   if (ss.equals(u)) { findid=(int)(0.001+dd[0]); break;}
  }
  return findid;
}

/**  open file for read. Should be moved to Elib. */
static public DataInputStream openfileREAD(String fn) throws IOException
 { DataInputStream in = new DataInputStream(new FileInputStream(fn));
     return in;
 }

/**  open file for write. Should be moved to Elib. */
static public DataOutputStream openfileWRITE(String fn) throws IOException
 {
   DataOutputStream out = new DataOutputStream(new FileOutputStream(fn));
     return out;
 }

/** .seg file suggested Eye parameters */
 public double  suggested_scale=0;
/** .seg file suggested Eye parameters */
 public double  suggested_F=0;
/** .seg file suggested Eye parameters */
 public double  suggested_Fz=0;

/** Field number for parent in .seg file */
 private static final int PAR=12;

/** Reads a .seg file and creates and returns a segment build from the file.*/
 public Segment readSegment(String fn,int extrapool,int extraplane)
 {
   Vector vpoints=new Vector(40);
   Vector vlines =new Vector(40);
   Vector vplanes =new Vector(40);

   Vector limbpointlistvector=new Vector(40);
   Vector vlimbs =new Vector(40);
   Vector labels =new Vector(40);
   String label="";

   int limbsadded=0;
   int allpoints=0;  // all points over the all segments
   int allplanes=0;  // all planes over the all segments
   boolean nopoints=false;
  
   suggested_scale=0;
   suggested_F=0;
   suggested_Fz=0;

   String t[]=new String[2];
   double[] pbuf=new double[3]; //point buffer
   int[]    lbuf=new int[3]   ; //line buffer
   int[]    plbuf=new int[40]   ; //plane buffer of quadriples of (seg,poolix)
   double[]    limbbuf=new double[PAR]   ; //limb buffer
   int tc=0,linec=0, mode=0;
   String s,u;
   boolean added=false;
   try {
   //DataInputStream in = openfileREAD(fn);
   DataInputStream in = Elib.openURLfile(HV.baseURL,fn);
   if (in==null) return null ;
   linec=0;
   while (null!=(s=in.readLine()))
   {
    linec++;
    if (s.equals("")) continue;
    if (s.charAt(0) == '#') continue;
    StringTokenizer st= new StringTokenizer(s," ");
    tc=0;
    added=false;
    while (st.hasMoreTokens())
    {

     u = st.nextToken();

     if (u.charAt(0)=='=') continue; //don't see this token
     if (u.charAt(0)=='#') break;   //ignore the rest of the line

     if (u.equals("Eye"))
     { suggested_Fz  = Elib.toDouble(st.nextToken());
       suggested_F   = Elib.toDouble(st.nextToken());
       suggested_scale=Elib.toDouble(st.nextToken());
       continue;
     }

     if (u.equals("Points"))
     { 
       if (mode!=4) 
        {
          System.err.println("Points must follow segment definitions. Line:"+linec);
        } 
      else mode=1;
      continue;
     }    
     if (u.equals("EndPoints"))
     { mode=5;
       continue;
     }    
    if (u.equals("Lines"))
     { mode=2;
       continue;
     }
     if (u.equals("Planes"))
     {
       if (mode!=4)
        {
          System.err.println("Planes can not be within segment definitions. Line:"+linec);
        } 
       else mode=3;
       continue;
     }
     if (u.equals("Limbs"))
     {
       mode=4;
       continue;
     }
     if (mode==1) { pbuf[tc]=Elib.toDouble(u);}
     else
     if (mode==2) { lbuf[tc]=Elib.toInt(u);}
     else
     if (mode==3) { 
                   if (easy_num(u)) plbuf[tc]=Elib.toInt(u);
                   else 
                   {
                    plbuf[tc]=resolve(u,vlimbs,labels);
                    if (plbuf[tc]<0)
                     System.err.println("Cannot find the reference : "+u+
                                        " at line "+linec);
                   }
                  }
     else
     if (mode==4) { 
                   if (tc==0) 
                     {
                       label=u;
                       limbbuf[tc]=limbsadded++;
                     }
                    else 
                     {
                      if (easy_num(u)) limbbuf[tc]=Elib.toDouble(u);
                      else // it is a label find the corresponding id
                      {
                       limbbuf[tc]=resolve(u,vlimbs,labels);
                       if (limbbuf[tc]<0) 
                          System.err.println("Cannot find the reference : "+u+
                                             " at line "+linec);
                     }
                    }
                 }
     added=true; tc++;
    }
     if (mode==5)
                {
                  limbpointlistvector.addElement(vpoints);
                  //need to start new vector for the next segment
                  vpoints=new Vector(40);
                  mode=4;
                  nopoints=false;
                  continue;
                }
    if (!added) continue;
    if (mode==1)
      if (tc==3) { 
                  vpoints.addElement(new Point3d(pbuf[0],pbuf[1],pbuf[2]));
                  allpoints++;
                 }
      else System.err.println("point description wrong in : "+fn+" line "+linec);
    if (mode==2)
      if (tc==2) vlines.addElement(new Line3d(lbuf[0]-1,lbuf[1]-1));
      else System.err.println("line description wrong in : "+fn+" line "+linec);
    if (mode==3)
      if (tc>=6) { 
		   int[] tt=new int[tc];
		   System.arraycopy(plbuf,0,tt,0,tc);
                   vplanes.addElement(tt);
                   allplanes++;
                  
                 }
      else System.err.println("plane description wrong in : "+fn+" line "+linec);
    if (mode==4) 
      if (tc==PAR) {if (nopoints)  // if previous segment had no points
                     limbpointlistvector.addElement(null);
                    nopoints=true;  // no points for  me yet
                    vlimbs.addElement(limbbuf); limbbuf=new double[tc]; 
                    labels.addElement(label);
                   }
      else System.err.println("limb description wrong in : "+fn+" line "+linec);
   }
   in.close();
  } catch (IOException e)
   { System.err.println(fn+" : Viewer.readShape() : EXCEPTION "+e);
   }

  if (nopoints)  // if previous segment had no points
    limbpointlistvector.addElement(null);
  
                   System.out.println("all planes:"+allplanes);
 //------------------------------
 // let's create Limb tree now
  setupPlane(allplanes,extraplane); // create the plane holders
  segc=vlimbs.size();
  seg=new Segment[segc];
  for (int i=0;i<segc;i++) seg[i]=null;
 Enumeration e=vlimbs.elements();
 Enumeration g=labels.elements();
 Enumeration v=limbpointlistvector.elements();
 while ( e.hasMoreElements())
   { int c=0;
     String lb    = (String)g.nextElement();
     double[] par = (double[])e.nextElement();
     Vector pnts  = (Vector)v.nextElement();

     //System.out.println("---->"+lb+" points:"+pnts);
     Enumeration f=vlimbs.elements();
     while ( f.hasMoreElements())
     {
      double[] ch=(double[])f.nextElement();
      if (par[0]==ch[PAR-1]) c++;
     } //now the segment par has c children

     if (par[PAR-1]<0)   // if the root segment
     { constructSegment(lb,(int)par[0],c,
                                 new Point3d(par[1],par[2],par[3]),
                                 new Point3d(par[4],par[5],par[6]),
                                 new Point3d(par[7],par[8],par[9]),
                                 (int)par[PAR-2]); //axistype
       setupPool(pnts,extrapool); // create the pool holders
       limbpoints2pool(pnts,new Point3d(0,0,0));
       seg[this.id]=this;
     }
     else
     { //find this guys parent
       Segment ps=null;
       if (par[PAR-1]>=segc) System.err.println("No such parent for :"+lb);
       else 
       ps=seg[(int)par[PAR-1]];

       if (ps==null) System.err.println("No parent defined for:"+lb);

       Segment kid=
       new Segment(lb,(int)par[0],c,
       new Point3d(ps.limb_pos.x+par[1],ps.limb_pos.y+par[2],ps.limb_pos.z+par[3]),
       new Point3d(ps.limb_pos.x+par[4],ps.limb_pos.y+par[5],ps.limb_pos.z+par[6]),
       new Point3d(par[7],par[8],par[9]), (int)par[PAR-2]); //axistype

       kid.setupPool(pnts,extrapool); // create the pool holders
       kid.limbpoints2pool(pnts,ps.limb_pos);

       //System.out.println("* Adding child ["+kid.id+"] to "+ps.id+" *");
       ps.addChild(kid);
       seg[kid.id]=kid;
     }
  }
     

// for (int i=0;i<segc;i++)
//  { System.out.println("Verify "+seg[i].label+" :  = "+i);}
 vplanes2plane(vplanes);
 return this;
 }

 Panel seg_pan=null;
 Panel savePan=null; //for enable disable
 Scrollbar beta_sb=null;
 Label     beta_txt=null;
 Label     beta_lb=null;
 Button    beta_bt=null;
 int dbeta=0;

 void unmakePanel()
 {
   beta_sb=null;
   beta_txt=null;
   beta_lb=null;
   beta_bt=null;
   seg_pan=null;  // this will ensure no updating will be done with beta change
 }

 Panel makePanel(String s)
 {
  if (joint_axis==null) return null; 
  Panel p=new Panel(); p.setLayout(new GridLayout(1,2));
  beta_lb =new Label(id+")"+label+" ("+dbeta+" degrees.)",Label.LEFT);
  beta_sb =new Scrollbar(Scrollbar.HORIZONTAL, 0, 1, -180*10 ,180*10+1);
  beta_sb.setBackground(Color.white);
  beta_sb.setUnitIncrement(10);
  beta_sb.setBlockIncrement(50);
  //beta_txt=new Label("0",Label.LEFT);
  //beta_bt =new Button("reset");

  p.add(beta_lb);
  p.add(beta_sb);
  //p.add(beta_txt);
  //p.add(beta_bt);
 
  update_panel();
  seg_pan=p;
  return p;
 }

 void update_panel()
 {
  if (seg_pan==null) return;
  dbeta=(int)(beta*1800/Math.PI);
  if (dbeta>180*10) dbeta-=360*10;
  beta_lb.setText(label+" ("+dbeta/10.0+" degrees.)") ;
  beta_sb.setValue(dbeta);
 }

 
}

/*
 *
 * Erhan Oztop, 2000-2002  <br>
 * Source code by Erhan Oztop (erhan@atr.co.jp) <br>
 * Copyright August 2002 under <br>
 * University of Southern California Ph.D. publication copyright <br>
 */
 
