
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
class Object3d
{

    public int[] udef_i;
    public int udef_ic=0;

    public int[] udef_d;
    public int udef_dc=0;

    public Point3d[] udef_P;
    public int udef_Pc=0;

    public Vector userPoints=new Vector(10);
    public int[] hindex=null; // plane list for this object
    //public Point[][] hplist=null; // plane list for this object
    public Plane[] hplist=null; // plane list for this object
    public int hpc=0; // # of items in hplist
    public Point3d objectCenter=new Point3d(0,0,0);
    public double objectRadius=0; // radius of the sphere containing plane centers

    public boolean noshow=false;

    /** Object view depth */
    public double objdepth;

    public int offx=0;  //these are offsets in two 2 for drawing
    public int offy=0;

    Segment seg[]=null;  // all segments
    int segc=0;
    Segment root=null;

    Spline[]   jointpath=null;
    double[] teta1;
    double[] teta2;
    double[] midteta;
    Trajectory lasttr=null;

    private Point3d intemp=new Point3d(0,0,0);

    static Point3d[] sPN = new Point3d[5];
    static Plane[] sPL = new Plane[5];

    Object3d merged=null;

    private ObjectFrame of=null;

    int[] xx=new int[100]; //max 100 sided polygon!
    int[] yy=new int[100];

    double zeroError=3.0;
    double[][] betas;

    public double reach_speed=1;
    public double reach_deltat=1;
    public double reach_basetime=0.01;

    double time=0;
    double endtime=1.0,basetimestep=0.01;

    public int ntick=0;
    public Reach activeReachThread=null;
    public String com="";
    public int search_mode=0;
    static final int VISUAL_SEARCH=0;
    static final int SILENT_SEARCH=1;
    static final int EXECUTE=2;
    static final int BABBLE=3;
    static final int PDFGRASP=4;
    public boolean callexe=false;
    double[] save;
    boolean chopped=false;

    public int PDFgraspcINI=1;
    boolean PDFwristgrasp=false;
    int PDFgraspc=0;

    public double lasterr=0;

    double speed;
    Segment kol;

    private int search_phase=0;
    //private boolean mapping=false;
    private boolean recordit=false;
    private boolean recognize=false;
    //private boolean nslrun=false;
    //private boolean babble=false;
    Graspable obj;

    double rtime=0;

    Segment bj1=null;
    Segment bj2=null;
    Segment bj3=null;
    Segment bj4=null;

    int babbleStatus=0;

    static int babble_REACH1ON=11;
    static int babble_REACH1OK=12;
    static int babble_REACH1FAIL=13;
    static int babble_REACH2ON=15;
    static int babble_REACH2OK=16;
    static int babble_REACH2FAIL=17;
    static int babble_GRASPON=21;
    static int babble_GRASPOK=22;
    static int babble_GRASPFAIL=23;
    static int babble_ROTATEON=1;
    static int babble_ROTATEOK=2;
    static int babble_ROTATEFAIL=3;
    String Reach2Target=Resource.getString("Reach2Target");  // reach2 will try to hit the center with this
    public double rotSave=-1, offSave=-1;
    double rotRandomness=Resource.get("rotRandomness");
    double offRandomness=Resource.get("offRandomness");
    double costThreshold=Resource.get("costThreshold");
    double negReinforcement=Resource.get("negReinforcement");
    double weightSave=Resource.get("weightSave");
    double cRATE=0.15;     // firming the enclose speed parameter (not read from mp)
    MotorPlan mp=null;
    Hand h=null;

    ParamsNode parnode=null;
    String[] params={"hand-ori","error"};

    static double wristGain=0.5;

    boolean newPDFgrasp=false;

    static double babbleZero=3.0;

    static int infoRate=1;
    static int babbleGradIt=5;  //number of gradient descent cycles per call
    static int babbleReach1MaxTick=250/babbleGradIt;
    static int babbleReach2MaxTick=80/babbleGradIt;

    static int bab_ph_NONE=0;
    static int bab_ph_WRIST1=1;
    static int bab_ph_WRIST2=2;;
    static int bab_ph_REACH1=10;
    static int bab_ph_REACH2=11;
    static int bab_ph_GRASP1=21;

    String[] phaseStr=new String[50];

    boolean showInfo=false;
    String infoStr=null;
    int infoStrC=0;;
    int pdf_phase=bab_ph_NONE;   // reach only
    int babble_phase=bab_ph_NONE;
    int babble_steps=0;
    int babble_trials=0;
    int babble_succ=0;
    int reach1failc=0;
    int blockc=0;
    boolean newBabbleAttempt=true;
    Point3d babbleOffset;
    int encloseTime=0;
    double[] lastOK;
    double[] initialConfig,reach1Config, reach2Config, rotateConfig,graspConfig,bestConfig;
    double searchZ=0;
    double goodZ=0;
    double minTorque=1e10;
    double minCost=1e10;  //minimum grasp cost
    int MAXREACH=Resource.getInt("MAXREACH");
    int MAXBABBLE=Resource.getInt("MAXBABBLE");
    int MAXROTATE=Resource.getInt("MAXROTATE");
    int location_trials=0;
    int rotate_trials=0;
    int MAXFINGER=15;
    int finger_trials=0;
    double JACOBIAN_th=1e20;  // if dis less then this use JACOBIAN TRANSPOSE in babbling

    public static final boolean ordered_pick=false;

    double dis=1e10;

    int slc;

    static public int softcon=Graspable.softcon_J2_45;

    double l1=800;
    double l2=700;
    double[][] J=new double[3][5];
    double[] dTeta=new double[5];

    public Object3d(String s)
    {
        this(s,0,1);
    }

    public Object3d(String s,int pipesidec,double piperad)
    {
        //+100 are very bad. I was just trying something remove them
        root=new Segment(s,2*pipesidec+100,15*(pipesidec+3)+100);
        //extra stuff required
        if (pipesidec>0) root.setupSolid(pipesidec,piperad,true);  // later will be read from file

        if (root==null) System.err.println("Cannot create Segment from file:"+s+"!!");
        setupObject();
        if (HV.DLEV>0) System.out.println("Total planes added:"+root.planec);
    }

    //not used for now
    //this returns reference to reference
    /*
    public Point3d[] getuserPoints()
    {
        Vector v=userPoints;
        int n=v.size();
        Point3d[] L=new Point3d[n];
        n=0;
        Enumeration e=v.elements();
        while (e.hasMoreElements())
        {
            Point3d p=(Point3d)e.nextElement();
            L[n++]=p; //p.duplicate();
        }
        if (HV.DLEV>0) System.out.println("returnin userPoints with "+n+" elements");
        if (HV.DLEV>0) for (int i=0;i<n;i++) System.out.println(L[i].str());
        return L;
    }
    */

    public void setPlaneProperties()
    {
        int u=0;
        objectCenter.x=0;
        objectCenter.y=0;
        objectCenter.z=0;
        for (int i=0;i<hpc;i++)
        {
            int k=hindex[i];
            if (k>=1000)
                continue;
            u++;
            hplist[k].setCenter();
            hplist[k].setGeom();
            VA._add(objectCenter,hplist[k].CP);
        }
        VA._scale(objectCenter,1.0/u);
        objectRadius=0;
        for (int i=0;i<hpc;i++)
        {
            int k=hindex[i];
            if (k>=1000)
                continue;
            double r=VA.dist(hplist[k].CP,objectCenter);
            if (r>objectRadius)
                objectRadius =r;
        }
        //choose the normals outward
        for (int i=0;i<hpc;i++)
        {
            int k=hindex[i];
            if (k>=1000)
                continue;
            hplist[k].adjustCenterSide(objectCenter);
        }
    }

    /* assumes plane properties are set */
    public double estimateSize()
    {
        double sum=0;
        int u=0;
        for (int i=0;i<hpc;i++)
        {
            int k=hindex[i];
            if (k>=1000)
                continue;
            u++;
            sum+=VA.dist(hplist[k].CP,objectCenter);
        }
        sum/=u;
        return sum*2;
    }


// returns true if the point is inside or on the surface of the object
    public boolean inside(Point3d p)
    {
        for (int i=0;i<hpc;i++)
        {
            int k=hindex[i];
            if (k>=1000)
                continue;
            intemp.x=p.x - objectCenter.x;
            intemp.y=p.y - objectCenter.y;
            intemp.z=p.z - objectCenter.z;
            double v=VA.inner(p,hplist[k].normal)+hplist[k].D;
            double v1=hplist[k].objectCenterValue;
            //System.out.println("inner with plane:"+k+"  "+Elib.nice(v,1e5)+" : "+Elib.nice(v1,1e5));
            if (v*v1<0)
                return false;
        }
        return true;
    }

    /*
    public void adduserPoints(Point3d p)
    {
        Point3d cur=p.duplicate();
        userPoints.addElement(cur);
    }
    */

    // o is generally the object to be grasped
    public double segmentCollision(Object3d o)
    {

        for (int i=0;i<segc;i++)
        {
            if (o.inside(seg[i].limb_pos))
                return (5000.0/VA.dist(seg[i].limb_pos,o.objectCenter)+1) ;
            if (o.inside(VA.center(seg[i].limb_pos,seg[i].joint_pos)))
                return (5000.0/VA.dist(seg[i].limb_pos,o.objectCenter)+1) ;
        }
        return 0;
    }

    public double new_segmentCollision(Segment seg, Object3d o)
    {
        if (o.inside(seg.limb_pos))
            return (5000.0/VA.dist(seg.limb_pos,o.objectCenter)+1) ;
        if (o.inside(VA.center(seg.limb_pos,seg.joint_pos)))
            return (5000.0/VA.dist(seg.limb_pos,o.objectCenter)+1) ;

        for (int i=0;i<seg.noch;i++)
        {
            double v=new_segmentCollision(seg.child[i],o);
            if (v>0) return v;
        }
        return 0;
    }
    /*
    TODO:
    start and end: convert to par,mer,R triplets.
    implement a I/O routines to save/load the map
    use the map for other reaches (beforeAction forexample can be?)
q    */

    /*
    private CfgCell[][][] workCell;
    private int clx,cly,clz,maxclx,maxcly,maxclz;
    private Point3d start,end;
    private Segment endeff=null;
    public CfgCell[][][] makeInvKinMap(int cellx,int celly,int cellz, Point3d start,Point3d end)
    {
        mapping=true;
        endeff=getJoint("J4");
        Mars.clearStars();
        this.start=start.duplicate();
        this.end=end.duplicate();
        maxclx=cellx; maxcly=celly; maxclz=cellz;
        clx=cly=clz=0;
        workCell=new CfgCell[cellx][celly][cellz];
        HV.self.setGrasp("REACHONLY");
        startReach();

        return null;
    }
    */
    /** check whether the object is in either side of P. 1: is normal side (check)
     -1:other side 0: means mixed (object intersects plane. Note that this is an
     approximation not all the vertex of the object is checked. Only the plane
     centers are used */
    /*
    public int sideOF(Plane p)
    {
        int allc=0;
        int neg=0;
        int pos=0;

        for (int i=0;i<hpc;i++)
        {
            int k=hindex[i];
            if (k>=1000)
                continue;
            double v = VA.inner(p.normal, hplist[k].CP)+p.D;
            if (v>0)
                pos++;
            else if (v<0)
                neg++;
            else
            {
                pos++;
                neg++;
            }
            allc++;
        }
        if (neg==allc)
            return -1;
        if (pos==allc)
            return 1 ;
        return 0;
    }
    */
    /*
    public void startReach()
    {

        root.resetJoints();
        Point3d tar=new Point3d(0,0,0);
        tar.x=start.x+clx*(end.x-start.x)/(maxclx-1);
        tar.y=start.y+cly*(end.y-start.y)/(maxcly-1);
        tar.z=start.z+clz*(end.z-start.z)/(maxclz-1);
        Mars.addStar(tar);
        HV.self.setTargetPosition((int)(0.5+tar.x),(int)(0.5+tar.y),(int)(0.5+tar.z));
        HV.self.toggleReach("visual");
    }
    */
    /*
    public void setCell(double err)
    {
        if (err>2) {workCell[clx][cly][cly]=null;}
        else
        {
            double[] a=new double[segc];
            storeAngles(a);
            workCell[clx][cly][cly]=new CfgCell(endeff.limb_pos,a,a.length);
        }
        nextCell();
    }
    */
    /*
    public void endInvKinMap()
    {
        System.out.println("Inverse Kinematics map formed.");
        //restoreChildren("J4");
        mapping=false;
    }
    */
    /*
    public void nextCell()
    {
        clx++;
        if (clx>=maxclx) {
            clx=0; cly++;
            if (cly>=maxcly) {
                cly=0; clz++;
                if (clz>=maxclz) {
                    endInvKinMap();
                    return;
                }
            }
        }
        startReach();
    }
    */

    //children of s will be truncated. Can be restored later.
    public void truncateChildren(String s)
    {
        Segment end=getJoint(s);
        if (end==null) return;
        end.truncateChildren();
    }

    synchronized public int segmentIntersection(Segment seg,Plane[] pl,Point3d[] pn)
    {
        Point3d P1=seg.joint_pos;
        Point3d P2=seg.limb_pos;
        double d =VA.dist(P1,P2);
        int k=lineIntersection(P1, VA.subtract(P2,P1),sPL,sPN);
        if (k==0)
            return k;
        int r=0;
        for (int i=0;i<k;i++)
        {
            if (VA.dist(sPN[i],P1) <= d && VA.dist(sPN[i],P2) <= d)
            {
                pl[r]=sPL[i];
                pn[r++]=sPN[i];
            }
        }
        return r;
    }

    /** returns the intersections (for now 2) of the object with the line
     passing through
     point p with direction vector u.*/
    synchronized public int lineIntersection(Point3d p, Point3d u,Plane[] PL, Point3d[] PN)
    {
        Point3d[] res=PN;
        int c=0;
        if (HV.DLEV>2) System.out.println("\n\n        The intersection of point "+p.str()+" with direction "+u.str()+" with box planes are:");
        for (int i=0;i<hpc;i++)
        {
            int k=hindex[i];
            if (k>=1000)
                continue; //ignore merger
            Plane pl=hplist[k];
            Point3d P=pl.intersection(p,u);
            if (HV.DLEV>2)
                System.out.println("        "+k+") "+pl.str());
            if (P!=null)
            {
                if (HV.DLEV>2)
                    System.out.println("           INTERSECTING at "+P.str());
                //Mars.addComet(P,root.limb_pos,5);
                ///Mars.addStar(P);
            }
            else
            {
                if (HV.DLEV>2)
                    System.out.println("           NONE\n ");
                continue;
            }
            if (HV.DLEV>2)
                System.out.println("");

            if (pl.contained(P) )
            {
                if (HV.DLEV>2)
                    System.out.println("         The above intersection is contained in the plane\n\n");
//         if (c>1) {
//  	   if (VA.dist2(P,res[0])  <  VA.dist2(P,res[1])) {
//                 PL[0]=pl;
//  	       res[0]=P;
//  	   } else {
//  	       PL[1]=pl;
//  	       res[1]=P;
//  	   }
//         } else {
//  	   PL[c]=pl;
//  	   res[c++]=P;
//         }

                PL[c]=pl;
                res[c++]=P;
            }
        }
        if (c==0)
        {
            if (HV.DLEV>2)
                System.out.println("!@!@!@@@@@@ Nothing in object !!!"+hpc+" planes");
        }
        if (c==1)
        {
            if (HV.DLEV>2)
                System.out.println("@@@@@@ Only 1 in object !!!"+hpc+" planes");
            res[1]=res[0];
        }
        if (c>2)
        {
            if (HV.DLEV>2)
                System.out.println("@@@@@@ More than 2  in object !!!"+hpc+" planes");
        }
        if (c==2)
        {
            if (HV.DLEV>2)
                System.out.println("@@@@@@ OK.  2 points in object !!!"+hpc+" planes");
        }

        return c;
    }

    public void restoreChildren(String s)
    {
        Segment end=getJoint(s);
        if (end==null)
            return;
        end.restoreChildren();
    }

    public void enablePanels()
    {
        for (int i=0;i<segc;i++)
            seg[i].enablePanel();
        root.updateAllPanel();
    }

    public void disablePanels()
    {
        for (int i=0;i<segc;i++)
            seg[i].disablePanel();
    }

    public void setJointConstraint(Segment s,double min,double max)
    {
        s.minbeta=min;
        s.maxbeta=max;
        s.jointconstraint=true;
    }

    /** checks if the joint constraint would be hit if rotated ang radians.*/
    /*
    public boolean hitConstraint(Segment seg, double ang)
    {
        if (!seg.jointconstraint)
            return false;
        if (seg.beta+ang > seg.maxbeta)
            return true;
        if (seg.beta+ang < seg.minbeta)
            return true;
        return false;
    }
    */

    public void constrainedRotate(Segment seg,double ang)
    {
        if (seg.jointconstraint)
        {
            //System.out.println("Ok constrained:"+seg.minbeta+","+seg.maxbeta);
            if (seg.beta+ang > seg.maxbeta)
            {
                ang=seg.maxbeta-seg.beta;
            }
            if (seg.beta+ang < seg.minbeta)
            {
                ang=seg.minbeta-seg.beta;
            }
        }
        if (ang==0)
        {
            if (seg.seg_pan!=null)
                seg.update_panel();
        }
        else seg.rotateJoint(ang); // this update automatically
    }

    public int collisionRotate(Segment seg,double ang,Object3d o)
    {
        double save=seg.beta;
        if (seg.blocked)
            return 1; //it has collided before
        if (seg.jointconstraint)
        { //System.out.println("Ok constrained:"+seg.minbeta+","+seg.maxbeta);
            if (seg.beta+ang > seg.maxbeta)
            {
                ang=seg.maxbeta-seg.beta;
            }
            if (seg.beta+ang < seg.minbeta)
            {
                ang=seg.minbeta-seg.beta;
            }
        }

        // if at the joint limit
        if (ang==0)
        {
            if (seg.seg_pan!=null)
                seg.update_panel();
            seg.blocked=true;
            return 1;
        }
        else
            seg.rotateJoint(ang); // this update automatically
        double colv=new_segmentCollision(seg,o);
        //System.out.println("Collision value:"+colv);
        if (colv>0.0001)
        {
            seg.blocked=true;
            return 1;
            ///////// seg.rotateJoint(-ang);
            //System.out.println("Collision occured.");
        }
        return 0;
    }

    public void setJointAngle(Segment seg,double ang)
    {
        seg.rotateJoint(ang-seg.beta);
    }

    /*
    public void set2doffset(int ox,int oy)
    {
        offx=ox;
        offy=oy;
    }
    */

    public void setupObject()
    {
        seg=root.seg;
        segc=root.segc;
        hplist=root.plane;
        hpc=root.planec;
        hindex=new int[hpc];
        if (HV.DLEV>0)
            System.out.println("********************** Total planes:"+hpc);
        for (int i=0;i<hpc;i++)
            hindex[i]=i;
        jointpath=new Spline[segc];
        setPlaneProperties();
    }

    public void paintAllSurfaces(int fillc, int linec)
    {
        for (int i=0;i<hpc;i++)
        {
            hplist[i].fill_color=fillc;
            hplist[i].line_color=linec;
        }
    }

    // this merge stuff is an quick hack to merge two objects to have
    // their planes sorted together. Maybe inefficient.
    // Can be extended easily though
    public void mergeObject(Object3d m)
    {
        hindex=new int[hpc+m.hpc];
        for (int i=0;i<hpc;i++)
            hindex[i]=i;
        for (int i=0;i<m.hpc;i++)
            hindex[hpc+i]=1000+i;
        hpc+=m.hpc;
        merged=m;
    }

    public void discardMerged()
    {
        hpc=root.planec;
        for (int i=0;i<hpc;i++)
            hindex[i]=i;
        merged=null;
    }

    public double[] getAngleList()
    {
        double a[]=new double[segc];
        for (int i=0;i<segc;i++)
            a[i]=seg[i].beta;
        return a;
    }

    void showObjectFrame()
    {
        of=new ObjectFrame(this);
        of.show();
        of.setBounds(600,100,400,500);
        of.resize(400,500);
    }

    void closeObjectFrame()
    {
        if (of==null)
            return;
        of.closeSelf();
        of=null;
    }

    void toggleObjectFrame()
    {
        if (of==null)
            showObjectFrame();
        else
            closeObjectFrame();
    }

    int getSegmentIndex(String name)
    {
        for (int i=0;i<segc;i++)
        {
            if (seg[i].label.equals(name))
                return i;
        }
        return -1;
    }

    Segment getJoint(String name)
    {
        return root.getJoint(name);
    }

    void project(Eye eye)
    {
        root.project(eye);
    }

// assumes hplistp[i][5].x containing the correct depth for planes!!
    public void updateObjectDepth()
    {
        objdepth=0;
        for (int i=0;i<hpc;i++)
        {
            if (hindex[i]>=1000)
                objdepth+=merged.hplist[hindex[i]-1000].depth;
            else
                objdepth+=hplist[hindex[i]].depth;
        }
        objdepth/=hpc;
    }
/*
public void sortPlanes(int mode)
 {
  root.updateDepth(Mars.eye,mode);
  for (int i=0;i<hpc-1;i++)
   for(int j=i+1;j<hpc;j++)
    {
     if (hplist[hindex[i]].depth < hplist[hindex[j]].depth)
      {int t=hindex[j];
       hindex[j]=hindex[i];
       hindex[i]=t;
      }
    }
 }
*/

//this is a quick cheat to merger objects for sorting planes
    public void sortPlanes(int mode)
    {
        int ii=0,jj=0;
        double depi=0,depj=0;
        root.updateDepth(Mars.eye,mode);
        if (merged!=null)
        {
            merged.root.updateDepth(Mars.eye,mode);
            merged.paintAllSurfaces(6,7);
        }
        for (int i=0;i<hpc-1;i++)
        {
            for(int j=i+1;j<hpc;j++)
            {
                ii=hindex[i];
                if (ii>=1000)
                    depi=merged.hplist[ii-1000].depth;
                else
                    depi=hplist[ii].depth;
                jj=hindex[j];
                if (jj>=1000)
                    depj=merged.hplist[jj-1000].depth;
                else
                    depj=hplist[jj].depth;

                if (depi < depj)
                {
                    int t=hindex[j];
                    hindex[j]=hindex[i];
                    hindex[i]=t;
                }
            }
        }
    }

    public void drawWire(Graphics g, Segment seg)
    {
        Plane[] HPL;
        if (noshow)
            return;
        if (merged!=null)
            merged.project(Mars.eye);
        g.setColor(HVCanvas.foreColor);
        for (int i=0;i<hpc;i++)
        {
            int ii=hindex[i];
            if (ii>=1000)
            {
                HPL=merged.hplist;
                ii-=1000;
            }
            else
                HPL=hplist;
            int N=hplist[ii].N;
            for (int k=0;k<N;k++)
            {
                xx[k]=Mars.midx+HPL[ii].r[k].x+offx;
                yy[k]=Mars.midy-HPL[ii].r[k].y+offy;
            }
            g.drawPolygon(xx,yy,N);
        }
    }

    public void drawWire(Graphics g)
    {
        drawWire(g,root);
    }

    public void drawSolid(Graphics g, Segment seg)
    {
        Plane[] HPL;
        int fill,line;
        if (noshow)
            return;
        if (merged!=null)
            merged.project(Mars.eye);
        //System.out.println("drawSolid!!");
        //System.out.println("hpc:"+hpc);
        for (int i=0;i<hpc;i++)
        {
            int ii=hindex[i];
            if (ii>=1000)
            {
                HPL=merged.hplist;
                ii-=1000;
            }
            else
                HPL=hplist;
            int N=HPL[ii].N;
            for (int k=0;k<N;k++)
            {
                xx[k]=Mars.midx+HPL[ii].r[k].x+offx;
                yy[k]=Mars.midy-HPL[ii].r[k].y+offy;
            }
            fill=HPL[ii].fill_color;
            line=HPL[ii].line_color;

            g.setColor(HV.pal.C[fill]);
            g.fillPolygon(xx,yy,N);
            if (line!=-1 )
            {
                g.setColor(HV.pal.C[line]);
                g.drawPolygon(xx,yy,N);
            }
        }
    }

    public void drawSolid(Graphics g)
    {
        drawSolid(g,root);
    }

    public void drawSkel(Graphics g)
    {
        drawSkel(g,root);
    }

    public void drawSkel(Graphics g, Segment seg)
    {
        if (noshow)
            return;
        if (merged!=null)
            merged.project(Mars.eye);
/*  if (seg.label.equals("PINKY") || seg.label.equals("RING") ||
     seg.label.equals("MIDDLE")  || seg.label.equals("INDEX") ||
     seg.label.equals("THUMB"))
  {  g.setColor(Color.green);
     g.drawString(seg.label, offx+Mars.midx+seg.limb_pos2d.x, Mars.midy-seg.limb_pos2d.y);
  }
*/
        g.setColor(HVCanvas.foreColor);
        g.drawLine(offx+Mars.midx+seg.joint_pos2d.x,offy+Mars.midy-seg.joint_pos2d.y,
                offx+Mars.midx+seg.limb_pos2d.x,offy+Mars.midy-seg.limb_pos2d.y);
        g.drawLine(offx+1+Mars.midx+seg.joint_pos2d.x,offy+Mars.midy-seg.joint_pos2d.y,
                offx+1+Mars.midx+seg.limb_pos2d.x,offy+Mars.midy-seg.limb_pos2d.y);
        g.setColor(Color.green);
        g.drawLine(offx+Mars.midx+seg.joint_pos2d.x,offy+Mars.midy-seg.joint_pos2d.y,
                offx+1+Mars.midx+seg.joint_pos2d.x,offy+Mars.midy-seg.joint_pos2d.y);

        for (int i=0;i<seg.noch;i++)
            drawSkel(g,seg.child[i]);
    }

    synchronized public void rect_moveto(Point3d newposition)
    {
        double dx=newposition.x - objectCenter.x; //root.joint_pos.x;
        double dy=newposition.y - objectCenter.y;  // root.joint_pos.y;
        double dz=newposition.z - objectCenter.z; // root.joint_pos.z;
        root._translate(dx,dy,dz);
        setPlaneProperties();
    }

    synchronized public void rect_moveto(double x,double y,double z)
    {
        double dx=x - objectCenter.x; // root.joint_pos.x;
        double dy=y - objectCenter.y;  //root.joint_pos.y;
        double dz=z - objectCenter.z; // root.joint_pos.z;
        root._translate(dx,dy,dz);
        setPlaneProperties();
    }

    synchronized public void moveto(double x,double y,double z)
    {
        Point3d q=HV.self.rHand.root.joint_pos;

        double rad=z;
        double par=x;
        double mer=y;
        System.out.println("move to spheric:  par:"+par*180/Math.PI+" mer:"+mer*180/Math.PI+" rad:"+rad);
        double xx=q.x + rad*Math.cos(par)*Math.sin(mer);
        double yy=q.y + rad*Math.sin(par);
        double zz=q.z + rad*Math.cos(par)*Math.cos(mer);

        double dx=xx - objectCenter.x; //root.joint_pos.x;
        double dy=yy - objectCenter.y; //root.joint_pos.y;
        double dz=zz - objectCenter.z; //root.joint_pos.z;
        root._translate(dx,dy,dz);
        setPlaneProperties();
    }

    /*
    synchronized public void moveto(Point3d R)
    {
        moveto(R.x,R.y,R.z);
    }
    */

    /*
    public void project()
    {
        root.project(Mars.eye);
    }
    */

    /** use a .cfg files destination configuration and install it */
    /*
    public void installCfg(String nm)
    {
        Gesture fake=doGesture(nm);
        if (fake==null) {System.out.println(nm+" cannot be opened."); return;}
        double[] angles=new double[segc];
        for (int i=0;i<segc;i++)
            angles[i]=betas[i][2];
        fake=null;
        restoreAngles(angles);
        root.updateAllPanel();
        HV.cv.refreshDisplay();
    }
    */

    /** ERH2001: looks like only for .cfg gesture execution */
    public Gesture doGesture(String nm)
    {
        betas=new double[segc][4];
        double[] x=new double[5],y=new double[5];
        for (int i=0;i<segc;i++)
            betas[i][0]=betas[i][1]=betas[i][2]=betas[i][3]=0;
        String gname="";

        String t[]=new String[2];
        int tc=0,linec=0, mode=0;
        String s,u;

        try
        {
            //DataInputStream in = Elib.openfileREAD(nm);
            DataInputStream in = Elib.openURLfile(HV.baseURL,nm);

            if (in==null)
                return null ;
            linec=0;
            while (null!=(s=in.readLine()))
            {
                linec++;
                if (s.equals(""))
                    continue;
                if (s.charAt(0) == '#')
                    continue;
                StringTokenizer st= new StringTokenizer(s," ");
                tc=0;
                while (st.hasMoreTokens())
                {
                    u = st.nextToken();
                    if (u.equals("Speed"))
                    {
                        reach_speed= Elib.toDouble(st.nextToken());
                        reach_deltat= reach_basetime*speed;
                        break;
                    }
                    if (u.equals("Gesture") || u.equals("Name"))
                    {
                        gname= st.nextToken();
                        break;
                    }
                    if (tc==0)
                    {
                        int k=findSegment(u);
                        if (k<0)
                        {
                            System.err.println("Cannot find joint "+u+" in the current segments. Line:"+linec);
                            break;
                        }
                        betas[k][0]= 1;  //drive it
                        betas[k][1]= Elib.toDouble(st.nextToken())*Math.PI/180;
                        betas[k][2]= Elib.toDouble(st.nextToken())*Math.PI/180;
                        betas[k][3]= speed*(betas[k][2]- betas[k][1])/100;

                        if (betas[k][0]<0)
                            jointpath[k]=null;
                        else
                        {
/*
          int NN=3;
          for (int r=0;r<NN;r++)
          {
           x[r]=r*(1.0/NN);
           y[r]=betas[k][1]+r*(betas[k][2]-betas[k][1])/NN;
           jointpath[k]=new Spline(NN,x,y);
          } 
*/
                            x[0]=0.0; y[0]=betas[k][1];
                            x[1]=0.1; y[1]=0.15*(betas[k][2]- betas[k][1])+ betas[k][1];
                            x[2]=0.5; y[2]=0.55*(betas[k][2]- betas[k][1])+ betas[k][1];
                            x[3]=1.0; y[3]=betas[k][2];
                            jointpath[k]=new Spline(3,x,y);
                        }
                    }
                    tc++;
                }
            }
            in.close();
        }
        catch (IOException e)
        {
            System.err.println(nm+" : Viewer.readShape() : EXCEPTION "+e);
        }

        for (int i=0;i<segc;i++)
        {
            if (betas[i][0]>0)
                s= betas[i][1] + " .. " + betas[i][2];
            else
                s="no drive";
            if (HV.DLEV>0)
                System.out.println(seg[i].label+" : "+s);
        }

        time=0;
        teta1=null;
        Gesture gesTh=new Gesture(this,jointpath,reach_speed,gname);
        return gesTh;
    }

    public void tickGesture(Gesture g,Spline[] jointpath,double speed)
    {
        // if (HV.DLEV>0)i
        System.out.println("Gesture executing time:"+time+" speed:"+speed);
        int c=0 ;

        for (int i=0;i<segc;i++)
        {
            if (jointpath[i]==null)
                continue;
            constrainedRotate(seg[i],jointpath[i].eval(time)-seg[i].beta);
        }
        if (teta1==null)
        {
            teta1=new double[segc];
            storeAngles(teta1);
        }
        time+=speed*basetimestep;
        if (HV.traceon)
            Mars.addStar();

        if (time>endtime && HV.self.newgest==g)
            HV.self.toggleGesture(""); //to make HV aware of this
        if (time>endtime)
        {
            if (of!=null)
            {
                if (of.testgest==g)
                    of.stopGesture(); //to make HV aware of this
            }
            if (HV.traceon)
                Mars.velocityProfile();
            double[] sol=getAngleList();
            lasttr=trimTrajectory(sol);
/*
    Point3d[] l=Mars.getStars();
    Trajectory tr=new Trajectory(l);
    if (HV.DLEV>0) System.out.println("Timewraped [0,1]:");
    for (int i=0;i<50;i++)
     System.out.println(tr.timewrap(i/49.0));
*/
        }

    }

    public int get_segIndex(String name)
    {
        for (int i=0;i<segc;i++)
        {
            if (seg[i].label.equals(name))
                return i;
        }
        return -1;
    }

    public void adjustPrecisionGrasp(double[] beta)
    {
        int e1,e2;
        int in1 =get_segIndex("INDEX1");
        int in2 =get_segIndex("INDEX2");
        e1=get_segIndex("MIDDLE1"); e2=get_segIndex("MIDDLE2");
        beta[e1]=beta[in1]-4*Math.PI/180;
        beta[e2]=beta[in2]-4*Math.PI/180;
        e1=get_segIndex("RING1"); e2=get_segIndex("RING2");
        beta[e1]=beta[in1]-6*Math.PI/180;
        beta[e2]=beta[in2]-4*Math.PI/180;
        e1=get_segIndex("PINKY1"); e2=get_segIndex("PINKY2");
        beta[e1]=beta[in1]-7*Math.PI/180;
        beta[e2]=beta[in2]-4*Math.PI/180;
    }

    public void restoreChildren(double[] init, String s)
    {
        int i=0,k;
        double[] bb=new double[segc];
        storeAngles(bb);
        Segment tr=null;
        do
        {
            tr=seg[i];
            setJointAngle(tr,init[i]);
            i++;
            k=i;
        }
        while (!tr.label.equals(s) && i<segc);
        restoreChildren(s);

        for (i=k-1;i>=0;i--)
            setJointAngle(seg[i],bb[i]);
    }

    public void restoreAngles(double[] teta)
    {
        for (int i=0;i<segc;i++)
            setJointAngle(seg[i],teta[i]);
    }

    public void storeAngles(double[] teta)
    {
        for (int i=0;i<segc;i++)
            teta[i]=seg[i].beta;
    }

    public void weighted_restoreAngles(double[] teta, double W)
    {
        for (int i=0;i<segc;i++)
            setJointAngle(seg[i],W*teta[i]+(1-W)*seg[i].beta);
    }

    public Reach fireSilentSearch()
    {
        ntick=0;
        slc=0;
        lasttr=null;
        save=new double[segc];
        search_mode=SILENT_SEARCH;
        System.out.println("Finding joint angles silently...");
        disablePanels();
        storeAngles(save);
        /*
        kol.truncateChildren();
        chopped=true;
        */
        activeReachThread= createReachThread("");
        return activeReachThread;
    }

    public Reach fireVisualSearch()
    {
        lasttr=null;
        ntick=0;
        obj.beforeAction();
        search_mode=VISUAL_SEARCH;
        /*
        kol.truncateChildren();
        chopped=true;
        */
        activeReachThread= createReachThread("");
        return activeReachThread;
    }

    synchronized public Reach firePDFwristgrasp()
    {
        Reach l=firePDFgrasp();
        PDFwristgrasp=true;   // use the same selection
        return l;
    }

    synchronized  public Reach firePDFgrasp()
    {
        PDFwristgrasp=false;
        PDFgraspc=PDFgraspcINI;
        lasttr=null;
        ntick=0;
        lastOK=new double[segc];
        reach1Config  = new double[segc];
        reach2Config  = new double[segc];
        rotateConfig  = new double[segc];
        graspConfig   = new double[segc];
        bestConfig    = new double[segc];
        obj.beforeAction();

        search_mode=PDFGRASP;
        newPDFgrasp=true;
        activeReachThread= createReachThread("");
        return activeReachThread;
    }

    public Reach fireBabbleLearn()
    {
        lasttr=null;
        ntick=0;
        lastOK=new double[segc];
        reach1Config  = new double[segc];
        reach2Config  = new double[segc];
        rotateConfig  = new double[segc];
        graspConfig   = new double[segc];
        bestConfig    = new double[segc];
        obj.beforeAction();
        newBabbleAttempt=true;
        // let's do this from tick babble this time. obj.beforeAction();
        search_mode=BABBLE;
        activeReachThread= createReachThread("");
        return activeReachThread;
    }

    public Reach fireExecution()
    {
        ntick=0;
        if (lasttr==null)
        {
            // next 2 lines added new by ERH
            HV.setInfo("Ready.  * Please first do a VISREACH to use this function *");
            if (true)
                return null;
            callexe=true;
            return fireSilentSearch();

        }
        else
            System.out.println("lastr is non null: executing...");
        search_mode=EXECUTE;
        obj.mask("WRISTx");  //make it more general. ok for now
        activeReachThread= createReachThread("");
        System.out.println("NOW executing...");
        return activeReachThread;
    }

    /** Instantiates a reach thread and starts it.*/
    public Reach createReachThread(String com)
    {
        search_phase=0;
        rtime=0;
        reach_speed=Elib.toDouble(HV.speedtxt.getText());
        reach_deltat=reach_speed*reach_basetime;
        activeReachThread=new Reach(this,lasttr,com);
        activeReachThread.start();
        return activeReachThread;
    }

    //you need  NONE search mode in case!
    public void finalizeReach(String com)
    {
        //if (search_mode==VISUAL_SEARCH)
        // obj.afterReach();

        HV.reachb.setBackground(Color.white);
        HV.cv.refreshDisplay();
        HV.setInfoReady();

        if (recordit)
        {
            if (dis<zeroError*3)  // somehow the dis usually is not smaller than zeroError
                Learn.writePattern((Hand)this,obj,"reach error:"+Elib.nice(dis,1e4));
            else if (HV.negativeExample)
                Learn.writeNegPattern((Hand)this,obj,"reach error:"+Elib.nice(dis,1e4));
            else
                System.out.println(" *** Discarding this reach. dis :"+dis+" not requested as negative but dis is too big.");
            lasterr=dis;
            // Learn.writeNegPattern((Hand)this,obj,"reach error:"+Elib.nice(dis,1e4));

            //Learn.showParSplines();
        }

        if (recognize)
        {
            String mirr=Learn.recognize(HV.self.bp,(Hand)this,obj,HV.genInParName(),true);
            System.out.println(" ========> "+mirr+" <========");
            HV.setInfo("Ready. - Action recognized as "+mirr);
            //HV.updateRecBars(Learn.lastout[0],Learn.lastout[1],Learn.lastout[2]);
            Learn.showtimeSeries(HV.genOutName()); //of hidden and output layer

            //HV.updateRecBars(Learn.lastout[0],Learn.lastout[1],Learn.lastout[2]);
            //cv.paint(cv.getGraphics());
            HV.recbarReq=0;
        }
        String tip="EXECUTE";
        if (search_mode==VISUAL_SEARCH)
            tip="VISUAL_SEARCH";
        else if  (search_mode==SILENT_SEARCH)
            tip="SILENT_SEARCH";

        HV.self.reportFinish("   ===> ("+tip+") Grasp Final error:"+dis+".  "+com);
        /*
        if (mapping) {
            System.out.println("MAPPIN IS ON");
            setCell(dis);
        }
        */
    }

    synchronized boolean reachActive()
    {
        return activeReachThread != null;
    }

    synchronized boolean kill_ifActive()
    {
        if (activeReachThread!=null)
        {
            //if (babble) obj.afterAction(); // connect the hand to the arm
            activeReachThread.stopRGest();
            HV.reachb.setBackground(Color.white); //in case it is silentReach
            activeReachThread=null;
            return true;
        }
        else return false;
    }

    public Trajectory trimTrajectory(double[] targetangles)
    {
        //int N=25; //not used
        //int c=0 ;
        //double time=0;
        //double dt=1.0/(N-1);
        //Point3d[] L=new Point3d[N];
        //double[] x=new double[5],y=new double[5];

        teta2=targetangles;
        //System.out.println("step 2: convert teta sweeps into linear splines");
        if (obj!=null)
        {
            if (obj.lastPlan==Graspable.PRECISION)
                adjustPrecisionGrasp(teta2);
        }
        jointpath=Trajectory.jointSpline(seg,teta1,teta2,segc);

/** it turned out that 1) this step has a bug 
 2) d|x|/dt is almost constant
 so it looks like we can skip constant stepping
 System.out.println("step 3: do the linear sweep uMath.Math.sing the desired dt (1/(N-1)) to get X path.");
 for (int i=0;i<segc;i++) setJointAngle(seg[i],teta1[i]);
 while (time<=1)
 {
 for (int i=0;i<segc;i++)
 { if (jointpath[i]==null) continue;
 constrainedRotate(seg[i],jointpath[i].eval(time)-seg[i].beta);
 }
 L[c++]=kol.limb_pos.duplicate();
 time+=dt;
 }
 System.out.println("step4: convert the PATH L, into the jointpathectory tr.");
 //Trajectory tr=new Trajectory(L,c);

 if (HV.DLEV>0) System.out.println("Timewraped [0,1]:");
 for (int i=0;i<50;i++)
 if (HV.DLEV>0) System.out.println(tr.timewrap(i/49.0));
 if (HV.DLEV>0) System.out.println("Trajectory formed return the jointpathectory.");
 */
        return new Trajectory();
    }

/*
 public double error()
 {double sum=0;
  for (int i=0;i<goalLimbc-1;i++)
   sum+=VA.dist(targets[i], goalLimbs[i].limb_pos); 
  return sum;
 }

 public double error_arm()
 {
  return 2*VA.dist(targets[goalLimbc-1], goalLimbs[goalLimbc-1].limb_pos);
 }

     
*/
    /** ERH2001: Main reach/grasp starting entry. All kinds of reaches,
     visearch, silet, execute, record etc. are started from here. */
    public Reach doReach(Graspable ob,String kind)
    {
        Reach r=null;
        callexe=false;
        recordit=false;
        recognize=false;
        //nslrun=false;
        Hand me=(Hand)this;
        obj=ob;
        me.makeNeutral();
        double[] pre_t=new double[5];
        double[] pre_v=new double[5];
        int pc=0;
        pre_t[pc]=0;
        pre_v[pc++]=0;
        pre_t[pc]=0.5;
        pre_v[pc++]=0.45;
        pre_t[pc]=0.75;
        pre_v[pc++]=1.2;
        pre_t[pc]=1;
        pre_v[pc++]=1;
        kol=getJoint("WRISTz");
        teta1=new double[segc];
        storeAngles(teta1);
        dis=1e10;
        HV.self.reportStart("     Starting reach of type:"+kind);
        if (kind.equals("visual") )
            r=fireVisualSearch();
        else if (kind.equals("silent") )
            r=fireSilentSearch();
        else if (kind.equals("execute") )
            r=fireExecution();
        else if (kind.equals("record") )
        {
            recordit=true;
            System.out.println("recorit =true done");
            Learn.prepareForPattern();
            r=fireExecution();
        }
        else if (kind.equals("recognize") )
        {
            recognize=true;
            System.out.println("recognize =true done");
            Learn.prepareForPattern();
            r=fireExecution();
        }
        /*
        else if (kind.equals("nslrun") )
        {
            nslrun=true;
            System.out.println("nslrun =true done");
            Learn.prepareForPattern();
            r=fireExecution();
        }
        */
        else if (kind.equals("babble") )
        {
            //babble=true;
            System.out.println(" babble=true done ...");
            // Motor.initBabble();
            r=fireBabbleLearn();
        }
        else if (kind.equals("PDFgrasp") )
        {
            r=firePDFgrasp();
        }
        else if (kind.equals("PDFwristgrasp") )
        {
            r=firePDFwristgrasp();
        }
        //if (r!=null) me.makeNeutral();
        return r;
    }

    public void clearTrajectory()
    {
        lasttr=null;
        //System.out.println("Trajectory cleaned.");
    }

    /** Called when executing a stored trajectory. */
    public void tickReachGesture(Reach r,Trajectory tr)
    {
        dis=obj.grasp_error();
        //if (HV.DLEV>0) System.out.println("Distance to target(error):"+Elib.nice(dis,1e5));
        if (rtime> 1.0)
        {
            System.out.println("Target must be reached.");
            System.out.println("Distance to target(error):"+Elib.nice(dis,1e5));
            // LETS calculate the forces
            //mp=HV.mcirc.nextMotorPlan(((Hand)this),obj);
            //((Hand)this).contact(obj);
            kill_ifActive();
            return;
        }
        else
        {
            /*
               if (nslrun) {
               HV.self.nslL.sendCommand("step 1");
               rtime=HV.self.nslL.getTime();
               //System.out.println("nsl time read:"+rtime);
               }
             else
            */
            rtime+=reach_deltat;

            for (int i=0;i<segc;i++)
            {
                if (jointpath[i]==null)
                    continue;
                // constrainedRotate(seg[i],jointpath[i].eval(rtime)-seg[i].beta);
                //constrainedRotate(seg[i],jointpath[i].eval(tr.timewrap(rtime))-seg[i].beta);
                double mytime=rtime;
                if (seg[i].userTag!=Hand.HANDJOINT && HV.bellshape )  //stretch time
                    mytime=tr.stretchTime(rtime);
                constrainedRotate(seg[i],jointpath[i].eval(mytime) -seg[i].beta);
            }
        }

        if (HV.traceon)
            Mars.addStar();
        if (recordit || recognize/* || nslrun*/)
            Learn.collect((Hand)this,obj);
        if (recognize)
        {
            String mirr;
            int hh=(int)(0.5+rtime/reach_deltat);
            //if (hh%2==0 && hh>3)
            if (hh>3)
            {
                mirr=Learn.recognize(HV.self.bp,(Hand)this,obj,HV.genInParName(), false);
                System.out.println(" --------> "+mirr+" <-------- (at t="+Elib.nice(rtime,1e3)+")");
                HV.setInfo("Grasp in progress. - so far action looks like (recognized as)  "+mirr);
                HV.updateRecBars(Learn.lastout[0],Learn.lastout[1],Learn.lastout[2]);
            }
        }
        /*
        else if (nslrun)
        {
            int hh=(int)(0.5+rtime/reach_deltat);
            if (hh>3)
            {
                //System.out.println("Transfering inputs..");
                HV.self.transferInput(HV.self.bp);
            }
        }
        */
    }

    public void PDFcondKill()
    {
        System.out.println("Remaining WRIST errors:"+(mp.bank-h.wristz.beta)+","+(mp.tilt-h.wristx.beta)+","+(mp.heading-h.wristy.beta));

        /*
        constrainedRotate(h.wristz, (mp.bank-h.wristz.beta));
        constrainedRotate(h.wristx, (mp.tilt-h.wristx.beta));
        constrainedRotate(h.wristy, (mp.heading-h.wristy.beta));
        */

        addWristData();
        Spline sp[]=parnode.getSplines();
        Spline.showSplines_with(sp,50," with linespoints");
        if (PDFgraspc<=0)
        {
            obj.afterAction();
            kill_ifActive();
        }
        else
        {
            newPDFgrasp=true;
            HV.cv.refreshDisplay();
            System.out.println(HV.setInfo("Holding 1q000 ms....["+PDFgraspc+" grasps to go...]"));
            try{Thread.sleep(1000);} catch(InterruptedException e) {}
        }
    }

    public void tickWrist(Hand h,MotorPlan mp)
    {
        //System.out.println("Wristx:"+h.wristx.beta+" -> "+mp.tilt);
        //  System.out.println("Wristy:"+h.wristy.beta+" -> "+mp.heading);
        //System.out.println("Wristz:"+h.wristz.beta+" -> "+mp.bank);

        constrainedRotate(h.wristx, wristGain*(mp.tilt-h.wristx.beta));
        //constrainedRotate(h.wristy, wristGain*(mp.heading-h.wristy.beta));
        constrainedRotate(h.wristz, wristGain*(mp.bank-h.wristz.beta));
    }

    synchronized public void tickPDFgrasp()
    {
        h=(Hand)this;

        if (newPDFgrasp)
        {
            newPDFgrasp=false;
            phaseStr[bab_ph_NONE]  ="NONE";
            phaseStr[bab_ph_WRIST1]="WRIST1";
            phaseStr[bab_ph_WRIST2]="WRIST2";
            phaseStr[bab_ph_REACH1]="REACH1";
            phaseStr[bab_ph_REACH2]="REACH2";
            phaseStr[bab_ph_GRASP1]="GRASP1";

            // lets create our configuration
            if (initialConfig==null)
            {
                initialConfig = new double[segc];
                reach1Config  = new double[segc];
                reach2Config  = new double[segc];
            }
            lasttr=new Trajectory();  // this is the timewarping for REACH
            // button. Have to be sent to Reach but needs Jointpath as
            // external to operate. Pretty much bad programming. 

            infoStrC=0;  infoStr=null; // no old info

            Hand h=((Hand)this);
            h.root.unBlock(); // reset the blocked fingers from prior babbles
            h.resetJoints();

            pdf_phase=bab_ph_REACH1;
            if (bj1==null)
            {
                bj1=getJoint("J1");
                bj2=getJoint("J2");
                bj3=getJoint("J3");
                bj4=getJoint("J4");
            }
            //setJointAngle(bj1,-Math.PI/2);
            //setJointAngle(bj4,80*Math.PI/180);
            resetRotCnt();
            System.out.println("PDFgraspc:"+PDFgraspc);
            HV.mcirc.updateInputLayer(h,obj);

            if (PDFwristgrasp)
            {
                if (mp==null)
                    mp=HV.mcirc.nextMotorPlan(h,obj);
                HV.mcirc.updateWristLayer();  // now can apply safepick
                HV.mcirc.safe_pickRotation(mp);
            }
            else
                mp=HV.mcirc.nextMotorPlan(h,obj);
            //--> Mars.addComet(obj.objectCenter,mp.reachTarget,1);
            PDFgraspc--;

            if (HV.DLEV>0)
                mp.printInfo();
            obj.reachoffset(mp.reachOffset);
            obj.beforeAction();
            Mars.clearStars();
            // --> Mars.addStar(mp.reachTarget);
            ntick=0;
            storeAngles(initialConfig);
            HV.cv.refreshDisplay();
            if (parnode==null)
                parnode=new ParamsNode(100,params);
            else
                parnode.reset();
        }

        // ---------------- phase REACH1  ------------------------
        // reach component
        if (pdf_phase==bab_ph_REACH1)
        {
            dis=obj.reach_error();
            double rate=(Elib.cube(dis/50)+10)*obj.BASERATE; // 0.0002;
            if (rate>obj.RATE_TH)
                rate=obj.RATE_TH; //0.05

            if (dis<JACOBIAN_th)
                jacobianTranspose(babbleGradIt,1.2*rate);
            else
                gradientDescent_arm(babbleGradIt,1.2*rate);
            tickWrist(h,mp);
            if (dis<babbleZero)
            {
                pdf_chmod(babble_REACH1OK);
            }
            else if (ntick>=babbleReach1MaxTick)
            {
                System.out.println("Reach 1 (Cannot reach 1st via point)!");

                PDFcondKill();
                return;
            }
            // ^^^^^^^^^^^^^^^^ phase REACH 1 ^^^^^^^^^^^^^^^^^^^^^^^^
            // ---------------- phase REACH 2 -----------------------
        }
        else if (pdf_phase==bab_ph_REACH2)
        {
            if (ntick==0)
            {
                //restoreAngles(reach1Config);
                h.root.unBlock();
                storeAngles(lastOK);
                //return;  // continue in next cycle
                //
                System.out.println("================!!!!!!!!!!!!!PDF REACH2 stating!!!!!!!!!!!!====================");
                constrainedRotate(h.wristz, mp.bank-h.wristz.beta);
                constrainedRotate(h.wristx, mp.tilt-h.wristx.beta);
                obj.reachoffsetByName(Reach2Target);  // reach to the center
                obj.beforeAction();
                HV.cv.refreshDisplay();
                if (HV.DLEV>0)
                    System.out.println("PDF-REACH2 has been started... ");
                HV.setInfo(phaseStr[pdf_phase]+ ": PDF-REACH2 has been started...");
            }
            ////tickWrist(h,mp);
            //---------------- also enclose --------------
            encloseTime++;
            blockc=0;
            blockc+=collisionRotate(h.thumb         , mp.force[0][0]*mp.fingerRate,obj);
            blockc+=collisionRotate(h.thumb.child[0], mp.force[0][1]*mp.fingerRate,obj);
            blockc+=collisionRotate(h.thumb.child[0].child[0], mp.force[0][2]*mp.fingerRate,obj);

            blockc+=collisionRotate(h.index,          mp.force[1][0]*mp.fingerRate,obj);
            blockc+=collisionRotate(h.index.child[0], mp.force[1][1]*mp.fingerRate,obj);

            blockc+=collisionRotate(h.middle,          mp.force[2][0]*mp.fingerRate,obj);
            blockc+=collisionRotate(h.middle.child[0], mp.force[2][1]*mp.fingerRate,obj);
            collisionRotate(h.ring,            mp.force[2][0]*mp.fingerRate,obj);
            collisionRotate(h.ring.child[0],   mp.force[2][1]*mp.fingerRate,obj);
            collisionRotate(h.pinky,            mp.force[2][0]*mp.fingerRate,obj);
            collisionRotate(h.pinky.child[0],   mp.force[2][1]*mp.fingerRate,obj);

            HV.cv.refreshDisplay();
            if (blockc==0)
                storeAngles(lastOK);
            else
                System.out.println("blockc:"+blockc);  // no collision at all
            // -------------------------------------------

            dis=obj.reach_error();
            double rate=(Elib.cube(dis/50)+10)*obj.BASERATE; // 0.0002;
            if (rate>obj.RATE_TH)
                rate=obj.RATE_TH; //0.05
            if (dis<JACOBIAN_th)
                jacobianTranspose(babbleGradIt,1.2*rate);
            else
                gradientDescent_arm(babbleGradIt,1.2*rate);

            if (dis<babbleZero)
            {
                System.out.println("PDF Reach2  FAILED (reached the center without collision!!!!");
                HV.setInfo(phaseStr[pdf_phase]+ ":Reach 2  FAILED (reached the center without collision!!!!.");

                PDFcondKill();
                return;
            }
            else if (ntick>=babbleReach2MaxTick)
            {
                System.out.println("Reach 2 FAILED (did not hit the target) FAILED!");HV.setInfo(phaseStr[pdf_phase]+"Reach 2 FAILED (did not hit the target) FAILED!");
                PDFcondKill();
                return;
            }
            else
            {
                //restoreAngles(lastOK);
                double colv=this.segmentCollision(obj);
                if (colv>0)
                    System.out.println("****GRASP SEGMENT COLLISION!!****");
                if (colv>0)
                {
                    double w=0;
                    double newc=0;
                    do
                    {
                        w=w+0.05;
                        weighted_restoreAngles(lastOK,w);
                        HV.cv.refreshDisplay();
                        newc=segmentCollision(obj);
                    }
                    while (newc>0);
                    pdf_chmod(babble_REACH2OK);
                }
                else
                    storeAngles(lastOK);
            }
        }
        // ^^^^^^^^^^^^^^^^^^^^^^^^ phase  REACH2 ^^^^^^^^^^^^^^^^^^^^^^^^
        // grasp component
        else if(pdf_phase==bab_ph_GRASP1)
        {
            encloseTime=0;
            h.root.unBlock();
            int ct;
            do
            {
                ct=0;
                encloseTime++;
                ct+=collisionRotate(h.thumb         , mp.force[0][0]*cRATE,obj);
                ct+=collisionRotate(h.thumb.child[0], mp.force[0][1]*cRATE,obj);
                ct+=collisionRotate(h.thumb.child[0].child[0], mp.force[0][2]*cRATE,obj);

                ct+=collisionRotate(h.index,          mp.force[1][0]*cRATE,obj);
                ct+=collisionRotate(h.index.child[0], mp.force[1][1]*cRATE,obj);

                ct+=collisionRotate(h.middle,          mp.force[2][0]*cRATE,obj);
                ct+=collisionRotate(h.middle.child[0], mp.force[2][1]*cRATE,obj);
                collisionRotate(h.ring,            mp.force[2][0]*cRATE,obj);
                collisionRotate(h.ring.child[0],   mp.force[2][1]*cRATE,obj);
                collisionRotate(h.pinky,            mp.force[2][0]*cRATE,obj);
                collisionRotate(h.pinky.child[0],   mp.force[2][1]*cRATE,obj);
                HV.cv.refreshDisplay();
                //// System.out.println("=====------------=== > blocked c:"+ct);
            }
            while  (encloseTime<40 && ct<7);
            // Let's see how well we grasped in babble_GRASPOK
            pdf_chmod(babble_GRASPOK);
        } // if ... ==bab_ph_GRASP1

        // ----------------------- info for the user --------------------
        //if (ntick % infoRate == 0 || showInfo) {
        //    HV.setInfo(phaseStr[pdf_phase]+ "("+ntick+")");
        //}
        ntick++;
        if (ntick%1==0)
        {
            addWristData();
        }
        if (ntick%1==0)
            HV.cv.refreshDisplay();
        if (HV.traceon)
            Mars.addStar();

        return;
    }

    public void  addWristData()
    {
        Hand hh=(Hand)this;
        Point3d handtilt=VA.subtract(hh.index.joint_pos,hh.pinky.joint_pos);
        Point3d objtilt=obj.X.duplicate();
        handtilt.z=0;
        objtilt.z=0;

        double cs=Math.acos(Math.abs(VA.cos(handtilt,	obj.X)));
        parnode.put("hand-ori",hh.wristz.beta);
        parnode.put("error",cs);
        parnode.advance();
        System.out.println("hand-ori error:"+(cs*180/Math.PI)+" degrees");
    }

    public void pdf_chmod(int newstat)
    {
        if (newstat==babble_REACH1OK)
        {
            infoStr="REACH1 succeeded with dis:"+dis;
            if (HV.DLEV>0)
                System.out.println("## "+infoStr);
            storeAngles(reach1Config);
            ntick=-1;   // will be zero soon
            //jointpath=Trajectory.jointSpline(seg,initialConfig,
            //      reach1Config,reach2Config,segc,0.8);

            pdf_phase=bab_ph_REACH2; // PHASE CHANGE
            return;
        }

        if (newstat==babble_REACH2OK)
        {
            infoStr="REACH2 * succeeded * with dis:"+dis;
            pdf_phase=bab_ph_GRASP1;
            if (HV.DLEV>0)
                System.out.println("## "+infoStr);
            ntick=-1;  // will be zero soon
            return;
        }

        if (newstat==babble_GRASPOK)
        {
            // train the network
            String s=mp.str();
            Point3d graspV=reinforceGrasp(h, obj);
            // some info to the user
            showInfo=true;infoStrC=0;
            infoStr="GRASP (cost, reward) :"+Elib.snice(graspV.x,1e3,4)+","+Elib.snice(graspV.y,1e3,4);
            System.out.println(infoStr);

            storeAngles(reach2Config);
            //jointpath=Trajectory.jointSpline(seg,initialConfig,
            //			     reach1Config,reach2Config,
            //			     segc,0.5);

            jointpath=Trajectory.jointSpline(seg,initialConfig, reach2Config, segc);
            //HV.mcirc.showSomething();
            PDFcondKill();
            return;
        }

        System.out.println(" I DONT KNOW this (PDF)newstat:"+newstat);
    }

    private void newBabbleAttempt_init()
    {
        // reset the wrist and finger joints (full open)
        Hand h=((Hand)this);
        resetRotCnt();  // Wrist rotation starts from zero

        h.root.unBlock(); // reset the blocked fingers from prior babbles
        h.resetJoints();
        ////setJointAngle(h.j1,-60*Math.PI/180);
        //setJointAngle(h.wristx,-80*Math.PI/180);
        babble_phase=bab_ph_REACH1;
        babbleStatus=babble_REACH1ON;
        System.out.println("Babbles ( success/attempts):"+babble_succ+"/"+babble_trials);
        if (bj1==null)
        {
            bj1=getJoint("J1");
            bj2=getJoint("J2");
            bj3=getJoint("J3");
            bj4=getJoint("J4");
        }
        setJointAngle(bj1,-Math.PI/2);
        //setJointAngle(bj2,-Math.PI/4);
        //setJointAngle(bj3,0);
        setJointAngle(bj4,80*Math.PI/180);
        //setJointAngle(h.thumb,90*Math.PI/180);
        //setJointAngle(h.thumb.child[0],10.7*Math.PI/180);
        //setJointAngle(h.thumb.child[0].child[0],55.7*Math.PI/180);
        h.makeBabblePose();
        HV.cv.refreshDisplay();
        //mp=HV.mcirc.nextMotorPlan(h,obj);
        // get new input
        if (location_trials==0)
        {
            System.out.println("Getting the next random input....");
            Point3d newinput=HV.mcirc.nextRandomInput();
            if (HV.FANCY>0)
            {
                if (Mars.starc>200)
                    Mars.clearStars();
            }
            else if (Mars.starc>5)
                Mars.clearStars();
            // Mars.addStar(newinput);
        }

        location_trials=(location_trials+1) % MAXREACH;

        mp=HV.mcirc.nextNoisyMotorPlan(h,obj,offRandomness,0,rotRandomness);
        //-> Mars.addComet(obj.objectCenter,mp.reachTarget,1);
        if (HV.DLEV>0)
            mp.printInfo();
        //restoreAngles(reachConfig);
        ///constrainedRotate(h.wristz, mp.bank-h.wristz.beta);
        ///constrainedRotate(h.wristx, mp.tilt-h.wristx.beta);
        // to show the offset reaches
        // ->Mars.addStar(VA.add(obj.objectCenter,mp.reachOffset));
        HV.cv.refreshDisplay();
        //Mars.addStar(VA.add(obj.objectCenter,mp.reachOffset));
        //System.out.println("Reach offset:"+mp.reachOffset.str());
        obj.reachoffset(mp.reachOffset);
        obj.beforeAction();
//  	if (HV.FANCY>0) {
//  	if (babble_trials % 100 == 0) Mars.clearStars();
//  	Mars.addStar(mp.reachTarget);
//  	}
        ntick=0;
        storeAngles(initialConfig);
        if (HV.FANCY>0 || HV.DLEV>0)
            infoRate=1;
        else
            infoRate=10;
    }

    public String toggleActionSelection()
    {
        if (rotSave<0)
        {
            rotSave=rotRandomness;
            offSave=offRandomness;
            rotRandomness=0;
            offRandomness=0.1; // let's keep some randomness for this
            return "Will generate plans from (almost) full pdf's.[Now rotRandomness:"+
                    rotRandomness+"    offRandomness:"+offRandomness+"]";
        }
        else
        {
            rotRandomness = rotSave ;
            offRandomness = offSave ;
            rotSave=-1;
            offSave=-1;
            return "Will generate pure random (uniform) plans. [Now rotRandomness:"+
                    rotRandomness+"    offRandomness:"+offRandomness+"]";
        }
    }

    /** 1 return: original randomness 0 return: 0 randomness */
    /*
    public int getActionSelection()
    {
        if (rotSave<0)
            return 1;
        else return 0;
    }
    */

    synchronized public void tickBabble()
    {
        h=(Hand)this;
        //HV.setInfo("Babble ntick:"+ntick);
        // a new babble attemp starting
        if (newBabbleAttempt)
        {
            newBabbleAttempt=false;
            babble_trials=0;
            babble_succ=0;
            reach1failc=0;
            goodZ=0;
            rotate_trials=0;
            minTorque=1e10;
            double minCost=1e10;

            phaseStr[bab_ph_NONE]  ="NONE";
            phaseStr[bab_ph_WRIST1]="WRIST1";
            phaseStr[bab_ph_WRIST2]="WRIST2";
            phaseStr[bab_ph_REACH1]="REACH1";
            phaseStr[bab_ph_REACH2]="REACH2";
            phaseStr[bab_ph_GRASP1]="GRASP1";

            // lets create our configuration
            if (initialConfig==null)
            {
                initialConfig = new double[segc];
                reach1Config  = new double[segc];
                reach2Config  = new double[segc];
            }
            lasttr=new Trajectory();  // this is the timewarping for REACH
            // button. Have to be sent to Reach but needs Jointpath as
            // external to operate. Pretty much bad programming. 

            infoStrC=0;  infoStr=null; // no old info

            newBabbleAttempt_init();
        }

        // ---------------- phase REACH1  ------------------------
        // reach component
        if (babble_phase==bab_ph_REACH1)
        {
            dis=obj.reach_error();
            double rate=(Elib.cube(dis/50)+10)*obj.BASERATE; // 0.0002;
            if (rate>obj.RATE_TH)
                rate=obj.RATE_TH; //0.05

            if (dis<JACOBIAN_th)
                jacobianTranspose(babbleGradIt,1.2*rate);
            else
                gradientDescent_arm(babbleGradIt,1.2*rate);

            //----> tickWrist(h,mp);
            if (dis<babbleZero)
            {
                chmod(babble_REACH1OK);
            }
            else if (ntick>=babbleReach1MaxTick)
            {
                chmod(babble_REACH1FAIL);
            }
            // ^^^^^^^^^^^^^^^^ phase REACH 1 ^^^^^^^^^^^^^^^^^^^^^^^^
            // ---------------- phase REACH 2 -----------------------
        }
        else if (babble_phase==bab_ph_REACH2)
        {
            if (ntick==0)
            {
                if (HV.DLEV>0 || (babble_trials%500==0))
                    System.out.println((MAXBABBLE-babble_trials)+" Babble attempts left...");
                babble_trials++;
                encloseTime=0;
                blockc=0;
                h.root.unBlock();

                if (babble_trials % weightSave==0)
                {
                    // this will write more info
                    HV.self.executeCommand("dumpnet "+".LGM-"+System.currentTimeMillis()/1000);
                    //HV.mcirc.dumpNet(".LGM-"+System.currentTimeMillis()/1000);
                    weightSave += weightSave ;
                }
                if (babble_trials>=MAXBABBLE)
                {
                    System.out.println("We did the babble_trials"+babble_trials+
                            ". Stopping now.");
                    obj.afterAction();
                    kill_ifActive();
                    // this will write more info
                    HV.self.executeCommand("dumpnet "+".LGM-LAST-"+System.currentTimeMillis()/1000);
                    //HV.mcirc.dumpNet(".LGM-"+System.currentTimeMillis()/1000);
                    return;
                }
                restoreAngles(reach1Config);
                storeAngles(lastOK);

                advanceRotCnt();
                if (doneRotCnt())
                {
                    storeAngles(reach1Config);
                    jointpath=Trajectory.jointSpline(seg,initialConfig,
                            reach1Config,segc);
                    newBabbleAttempt_init();
                    return;  // continue in next cycle
                }
                else
                {
                    pickNoisyRotation(mp,rotRandomness);
                    constrainedRotate(h.wristz, mp.bank-h.wristz.beta);
                    constrainedRotate(h.wristx, mp.tilt-h.wristx.beta);
                    obj.reachoffsetByName(Reach2Target);  // reach to the center
                    obj.beforeAction();
                    HV.cv.refreshDisplay();
                    if (HV.DLEV>0)
                        System.out.println("New wrist motor com:"+mp.strRot());
                    if (HV.DLEV>0)
                        System.out.println("REACH2 has been started... ");
                }
            }
            //---------------- start enclose --------------
            //tickWrist(h,mp);
            encloseTime++;
            blockc=0;
            blockc+=collisionRotate(h.thumb, mp.force[0][0]*mp.fingerRate,obj);
            blockc+=collisionRotate(h.thumb.child[0], mp.force[0][1]*mp.fingerRate,obj);
            blockc+=collisionRotate(h.thumb.child[0].child[0], mp.force[0][2]*mp.fingerRate,obj);

            blockc+=collisionRotate(h.index, mp.force[1][0]*mp.fingerRate,obj);
            blockc+=collisionRotate(h.index.child[0], mp.force[1][1]*mp.fingerRate,obj);

            blockc+=collisionRotate(h.middle, mp.force[2][0]*mp.fingerRate,obj);
            blockc+=collisionRotate(h.middle.child[0], mp.force[2][1]*mp.fingerRate,obj);
            collisionRotate(h.ring, mp.force[2][0]*mp.fingerRate,obj);
            collisionRotate(h.ring.child[0], mp.force[2][1]*mp.fingerRate,obj);
            collisionRotate(h.pinky, mp.force[2][0]*mp.fingerRate,obj);
            collisionRotate(h.pinky.child[0], mp.force[2][1]*mp.fingerRate,obj);
            // this is new addition after kol3.11 with kol3.12
            if (blockc==0)
            {
                storeAngles(lastOK);
            }
            //HV.cv.refreshDisplay();
            // -------------------------------------------
            dis=obj.reach_error();
            double rate=(Elib.cube(dis/50)+10)*obj.BASERATE; // 0.0002;
            if (rate>obj.RATE_TH)
                rate=obj.RATE_TH; //0.05
            if (dis<JACOBIAN_th)
                jacobianTranspose(babbleGradIt,1.2*rate);
            else
                gradientDescent_arm(babbleGradIt,1.2*rate); //gradientDescent_arm(1,2*rate);
            //System.out.println("Gradient FINISHED.");
            if (dis<babbleZero)
            {
                if (HV.DLEV>0)
                    System.out.println("Reach 2 =========== FAILED (reached the center without collision!!!!");
                chmod(babble_REACH2FAIL);
            }
            else if (ntick>=babbleReach2MaxTick)
            {
                if (HV.DLEV>0)
                    System.out.println("Reach 2 ========= (did not hit the target) FAILED!");
                chmod(babble_REACH2FAIL);
            }
            else
            {
                double colv=this.segmentCollision(obj);

                if (colv>0)
                {
                    //System.out.println("@Target touched.");
                    double w=0;
                    double newc=0;
                    do
                    {
                        w=w+0.05;
                        weighted_restoreAngles(lastOK,w);
                        //HV.cv.refreshDisplay();
                        newc=segmentCollision(obj);
                        if (HV.FANCY>0) HV.cv.refreshDisplay();
                        //System.out.println("New collision:"+newc);
                    }
                    while (newc>0);
                    storeAngles(lastOK); //corrected fron interpenetration
                    chmod(babble_REACH2OK);
                    //System.out.println("This is before auto grasping...");
                    //try{Thread.sleep(3000);} catch(InterruptedException e) {}
                    //System.out.println("Now starting auto grasping...");
                }
                else
                {
                    storeAngles(lastOK);
                }
            }

        }
        // ^^^^^^^^^^^^^^^^^^^^^^^^ phase  REACH2 ^^^^^^^^^^^^^^^^^^^^^^^^
        // grasp component
        else if(babble_phase==bab_ph_GRASP1)
        {
            encloseTime=0;
            h.root.unBlock();
            int ct;

            HV.cv.refreshDisplay();
            do
            {
                ct=0;
                encloseTime++;

                ct+=collisionRotate(h.thumb, mp.force[0][0]*cRATE,obj);
                ct+=collisionRotate(h.thumb.child[0], mp.force[0][1]*cRATE,obj);
                ct+=collisionRotate(h.thumb.child[0].child[0], mp.force[0][2]*cRATE,obj);

                ct+=collisionRotate(h.index, mp.force[1][0]*cRATE,obj);
                ct+=collisionRotate(h.index.child[0], mp.force[1][1]*cRATE,obj);

                ct+=collisionRotate(h.middle, mp.force[2][0]*cRATE,obj);
                ct+=collisionRotate(h.middle.child[0], mp.force[2][1]*cRATE,obj);
                collisionRotate(h.ring, mp.force[2][0]*cRATE,obj);
                collisionRotate(h.ring.child[0], mp.force[2][1]*cRATE,obj);
                collisionRotate(h.pinky, mp.force[2][0]*cRATE,obj);
                collisionRotate(h.pinky.child[0], mp.force[2][1]*cRATE,obj);
                if (HV.FANCY>0)
                    HV.cv.refreshDisplay();  //if you wanna see it
                //System.out.println("=====------------=== > blocked c:"+ct);
            }
            while  (encloseTime<40 && ct<7) ;
            // Let's see how well we grasped in babble_GRASPOK
            chmod(babble_GRASPOK);
        } // if ... ==bab_ph_GRASP1

        // ----------------------- info for the user --------------------
        if (ntick % infoRate == 0 || (showInfo && !(HV.FANCY>0 || HV.DLEV>0)))
        {
            if (infoStr!=null)
            {
                if (infoStrC==0)
                    infoStrC=75/infoRate;  //timer to stay on
                if (infoStrC--==1)
                    infoStr=null;
            }
            if (infoStr==null)
                HV.setInfo(phaseStr[babble_phase] + "("+babble_succ+"/"+babble_trials+":" +
                        ntick+") error:"+Elib.snice(dis,1e3,6));
            else
                HV.setInfo(phaseStr[babble_phase]+ "("+babble_succ+"/"+babble_trials+":" +
                        ntick+") error:"+Elib.snice(dis,1e3,6)+
                        " [last:"+infoStr+"] ");
            showInfo=false;
        }

        ntick++;
        if (HV.FANCY==0)
        {
            if (ntick%10==0)
                HV.cv.refreshDisplay();
        }
        else
        {
            if (HV.FANCY>0)
            {
                HV.cv.refreshDisplay();
                if (HV.traceon)
                    Mars.addStar();
            }
            else if (ntick%25==0)
                HV.cv.refreshDisplay();
        }
        return;
    }

    public void chmod(int newstat)
    {
        if (newstat==babble_REACH1OK)
        {

            infoStr="REACH1 succeeded with dis:"+dis;
            showInfo=true;
            infoStrC=0;

            storeAngles(reach1Config);
            ntick=-1;   // will be zero soon
            jointpath=Trajectory.jointSpline(seg,initialConfig, reach1Config,reach2Config,segc,0.8);

            rotate_trials=0;
            babble_phase=bab_ph_REACH2; // PHASE CHANGE
            HV.mcirc.resetRotGen();
            if (HV.DLEV>0)
                System.out.println("##"+infoStr);
            return;
        }

        if (newstat==babble_REACH1FAIL)
        {
            reach1failc++;
            showInfo=true;infoStrC=0;
            infoStr="REACH1 * failed * (tot reach1 fails:"+reach1failc+")";
            HV.mcirc.reinforceOff(negReinforcement*10);
            if (babble_trials<MAXBABBLE)
            {
                storeAngles(reach1Config); // ? why
                jointpath=Trajectory.jointSpline(seg,initialConfig,reach1Config,segc);
                newBabbleAttempt_init();
            }
            else
            {
                System.out.println("We did the babble_trials"+babble_trials+". Stopping now.[tot reach1 fails:"+reach1failc+"]");
                obj.afterAction();
                kill_ifActive();
                HV.mcirc.dumpNet(".LGM-"+System.currentTimeMillis()/1000);
            }
            if (HV.DLEV>0 || HV.FANCY>0)
                System.out.println("## "+infoStr);
            return;
        }

        if (newstat==babble_REACH2OK)
        {
            showInfo=true;infoStrC=0;
            if (HV.DLEV>0)
                infoStr="REACH2 * succeeded * with dis:"+dis;
            babble_phase=bab_ph_GRASP1;
            if (HV.DLEV>0)
                System.out.println("## "+infoStr);
            ntick=-1;  // will be zero soon
            return;
        }
        if (newstat==babble_REACH2FAIL)
        {
            showInfo=true;infoStrC=0;
            infoStr="REACH2 * failed * ";
            System.out.println("@mp.tilt:"+mp.tilt+"    mp.bank:"+mp.bank+"(reach2 failed)"+"    reward:-1/one");
            HV.mcirc.reinforceAll(negReinforcement,mp);
            if (HV.DLEV>0)
                System.out.println("## "+infoStr);
            ntick=-1;
            return;
        }
        if (newstat==babble_GRASPOK)
        {
            babble_succ++;

            // train the network
            String s=mp.str();
            Point3d rew=reinforceGrasp(h, obj);
            // some info to the user
            showInfo=true;infoStrC=0;

            infoStr="GRASP (cost, reward) :"+Elib.snice(rew.x,1e3,4)+","+Elib.snice(rew.y,1e3,4);
            if (rew.y>0)
            {
                System.out.println("======> "+(int)(180/Math.PI*mp.bank)+","+(int)(180/Math.PI*mp.tilt)+" gives reward :"+infoStr);
            }
            if (HV.DLEV>0 || HV.FANCY>0)
                System.out.println("GraspOK, babbletrials:"+babble_succ+"/"+babble_trials+" ["+infoStr+"]");
            // let's create a reach action with this. So when the BABBLE is
            // interrupted a REACH can generate the last grasp.
            //storeAngles(reach2Config);
            //jointpath=Trajectory.jointSpline(seg,initialConfig,
            //			     reach1Config,reach2Config,segc,0.8);

            if ((babble_succ%30)==1)
            {
                //HV.mcirc.showSomething();
                babble_phase=bab_ph_REACH2;
            }
            ntick=-1;  // will be zero soon
            return;
        }

        if (newstat==babble_GRASPFAIL)
        {
            babble_phase=bab_ph_REACH2;
            ntick=-1;  // will be zero soon
            return;
        }

        System.out.println(" I DONT KNOW this newstat:"+newstat);
    }

    /*
    private void resetRotCnt(int max)
    {
        rotate_trials=0;
        MAXROTATE=max;
    }
    */

    private void resetRotCnt()
    {
        if (!ordered_pick)
            rotate_trials=0;
        else
            HV.mcirc.resetRotGen();
    }

    // check here maybe buggy
    private void advanceRotCnt()
    {
        if (!ordered_pick)
        {
            rotate_trials++;
            HV.mcirc.advancePass();
        }
        else
            HV.mcirc.advancePass();
    }

    private boolean doneRotCnt()
    {
        if (!ordered_pick)
        {
            if (rotate_trials>MAXROTATE)
                return true;
        }
        else if (HV.mcirc.doneRotGen())
            return true;

        return false;
    }

    /*
    private void pickRotation(MotorPlan mp) {
        if (babble_trials>-1000)
            HV.mcirc.pickRotation(mp);
        else
            HV.mcirc.pickOrdRotation(mp);
    }
    */

    // Note: if ORD is on the c has no effect yet
    private void pickNoisyRotation(MotorPlan mp, double c)
    {
        if (babble_trials>-1000)
            HV.mcirc.pickNoisyRotation(mp,c);
        else
            HV.mcirc.pickOrdRotation(mp);
    }

    synchronized public Point3d reinforceGrasp(Hand h, Object3d obj)
    {
        double reward=0;
        double cost=1e20;
        h.contact(obj);
        ContactList CL=h.cl;
        if (CL.contc<2)
        {
            reward=negReinforcement;
        }
        else
        {
            //double tn=VA.norm(CL.netTorque);
            //double fn=VA.norm(CL.netForce);
            cost=CL.graspCost;
            if (cost<minCost && CL.contc>1)
            {
                minCost=cost;
                storeAngles(bestConfig);
            }
            //reward=Math.exp(-(tn+fn)/2)-Math.exp(-10/2);
            if (cost < costThreshold)
            {
                reward=0.5+1-cost;
            }
            else
                reward=negReinforcement;
        }
        if (search_mode!=PDFGRASP)
            HV.mcirc.reinforceAll(reward,mp);
        return new Point3d(cost,reward,0);
    }

    public void tickVisual(Reach r)
    {
        if (search_phase==0)
            dis=obj.reach_error();
        else
            dis=obj.grasp_error();

        if ((dis < zeroError && dis<obj.GRASP_TH) || ntick==450)
        {
            if (dis<zeroError)
                System.out.println("Target reached.");
            else
                System.out.println("Cannot reach; Trying my best.");
            Hand h=((Hand)this);
            h.contact(obj);
            ContactList CL=h.cl;
            //double tn=VA.norm(CL.netTorque);
            //double fn=VA.norm(CL.netForce);

            obj.afterAction();
            double[] sol=getAngleList();
            lasttr=trimTrajectory(sol);
            kill_ifActive();
            return;
        }

        double rate=(Elib.cube(dis/50)+1)*obj.BASERATE; // 0.0002;

        if (rate>obj.RATE_TH)
            rate=obj.RATE_TH; //0.05
        if (search_phase==0)
        {
            if (dis<JACOBIAN_th)
                jacobianTranspose(1,1.2*rate);
            else
                gradientDescent_wrist(1,1.2*rate); //gradientDescent_wrist(1,2*rate);
        }
        else
            gradientDescent(1,rate);

        if (search_phase==0 && dis<obj.GRASP_TH)
        {
            search_phase=1;
            obj.afterReach();
            //Hand me=(Hand)this;
        }
        if (ntick%50==0)
        {
            if (HV.DLEV>0)
                System.out.println("Step:"+ntick+" Distance to target:"+Elib.nice(dis,1e5)+" Aperture:"+Elib.nice(VA.dist(((Hand)this).index.child[0].limb_pos,((Hand)this).thumb.child[0].child[0].child[0].limb_pos),1e4));
        }
        ntick++;
        if (HV.traceon) Mars.addStar();
    }

    public void tickSilent(Reach r)
    {
        double dis=1e5;
        //dis=VA.dist(target, goalLimb.limb_pos);
        if (search_phase==0)
            dis=obj.reach_error();
        else
            dis=obj.grasp_error();
        if (dis < zeroError)
        {
            System.out.println("Target reached.");
            System.out.println("Angles must be restored!!!!!!!!!!!!!!!");
            ///kol.restoreChildren();
            enablePanels();
            HV.self.setTrace(false);
            double[] sol=getAngleList();
            lasttr=trimTrajectory(sol);
            restoreAngles(save);
            HV.cv.refreshDisplay();
            kill_ifActive();
            if (callexe)
            {
                fireExecution();
            }
            return;
        }
        double rate=dis*dis*dis*0.0002;
        if (rate>0.05)
            rate=0.05;
        if (search_phase==0)
            gradientDescent_arm(5,rate);
        else
            gradientDescent(5,rate);

        if (search_phase==0 && dis<20)
        {
            search_phase=1;
            Hand me=(Hand)this;
            kol.restoreChildren();
            chopped=false;
            me.makeNeutral();
        }
        if (slc%5==0)
        {
            if (HV.DLEV>0)
                System.out.println("Step:"+slc+" Distance to target:"+Elib.nice(dis,1e5)+" Aperture:"+Elib.nice(VA.dist(((Hand)this).index.child[0].limb_pos,((Hand)this).thumb.child[0].child[0].child[0].limb_pos),1e4));
        }
/* not working don't understand why?? 
 if (slc%5==0) 
   { Graphics g=HV.cv.getGfx();
     g.setColor(Color.green);
     g.drawLine(200+slc/5,250,500+slc/5,255);
   }
*/
        slc++;
        if ((slc%2)==0) HV.reachb.setBackground(Color.white);
        else  HV.reachb.setBackground(Color.red);
    }


    public double gradientDescent(int N,double lrate)
    {
        double rate;
        //rate=lrate;
        for (int k=0;k<N;k++)
        {
            for (int i=0;i<segc;i++)
            {
                //do {
                if (seg[i].userTag==Hand.ARMJOINT)
                    rate=lrate*0.3;
                else
                    rate=lrate;
                if (seg[i].joint_axis==null)
                    continue;
                //if (Math.random()>0.8) continue;
                double dis=obj.grasp_error(); //VA.dist(target, goalLimb.limb_pos);
                double dteta=(Math.random()-0.5)*rate;
                constrainedRotate(seg[i],dteta);
                double newdis=obj.grasp_error(); //VA.dist(target, goalLimb.limb_pos);
                if (newdis>dis)
                    constrainedRotate(seg[i],-1.5*dteta);
                //} while  (seg[i].label.charAt(0)=='W' && Math.random()>0.8);
            }
            //System.out.println("Gradient step."+k);
        }
        return obj.grasp_error(); //VA.dist(target, goalLimb.limb_pos);
    }

    private double gradientDescent_wrist(int N,double rate)
    {
        for (int k=0;k<N;k++)
        {
            for (int i=0;i<segc;i++)
            {
                if (seg[i].userTag==Hand.HANDJOINT)
                    break;
                if (seg[i].joint_axis==null)
                    continue;
                //if (Math.random()>0.8) continue;
                Point3d dis3=obj.reach_error(softcon); //VA.dist(target, goalLimb.limb_pos);
                double dteta=(Math.random()-0.5)*rate;
                constrainedRotate(seg[i],dteta);
                Point3d newdis3=obj.reach_error(softcon); //VA.dist(target, goalLimb.limb_pos);
                if (newdis3.z>dis3.z)
                    constrainedRotate(seg[i],-1.5*dteta);
            }
            //System.out.println("Gradient_wrist step."+k);
        }
        return obj.reach_error(); //VA.dist(target, goalLimb.limb_pos);
    }

    synchronized private double gradientDescent_arm(int N,double rate)
    {
        for (int k=0;k<N;k++)
        {
            for (int i=0;i<segc;i++)
            {
                if (seg[i].userTag==Hand.WRISTJOINT)
                    break;
                if (seg[i].joint_axis==null)
                    continue;
                //if (Math.random()>0.8) continue;
                Point3d dis3=obj.reach_error(softcon);
                //System.out.println("SoftCons error:"+dis3.str());
                double dteta=(Math.random()-0.5)*rate;
                // if (seg[i]==bj2 && dteta>0) dteta*=0.2;   // don't do too much duck move
                constrainedRotate(seg[i],dteta);
                Point3d newdis3=obj.reach_error(softcon);
                if (newdis3.z>dis3.z && Math.random()>0.01)
                    constrainedRotate(seg[i],-1.5*dteta);
                // ERHAN jan 2002: inserted random>
            }
            //System.out.println("Gradient_arm step."+k);
        }
        return obj.reach_error();
    }

    /*
    private double pow(double x, double pw){
        return Math.pow(x,pw);
    }
    */

    synchronized private double jacobianTranspose(int N,double rate)
    {
        for (int k=0;k<N;k++)
        {
            //System.out.println("Jac. Trans step."+k);
            //System.out.println("seg[1].label:"+seg[1].label);
            double t1=seg[1].beta;
            double t2=seg[2].beta;
            double t3=seg[3].beta;
            double t4=seg[4].beta;
            double t5=seg[5].beta;
            // ---from matlab

            J[0][0] = 0.0;
            J[0][1] = (-Math.cos(t2)*Math.sin(t4)+Math.sin(t2)*Math.sin(t3)*Math.cos(t4))*l2-Math.cos(t2)*l1;
            J[0][2] = -Math.cos(t2)*Math.cos(t3)*Math.cos(t4)*l2;
            J[0][3] = (-Math.sin(t2)*Math.cos(t4)+Math.cos(t2)*Math.sin(t3)*Math.sin(t4))*l2;
            J[0][4] = 0.0;

            J[1][0] = (Math.sin(t1)*Math.cos(t2)*Math.sin(t4)+(-Math.sin(t1)*Math.sin(t2)*Math.sin(t3)-Math.cos(t1)*Math.cos(t3))*Math.cos(t4))*l2+Math.sin(t1)*Math.cos(t2)*l1;
            J[1][1] = (Math.cos(t1)*Math.sin(t2)*Math.sin(t4)+Math.cos(t2)*Math.sin(t3)*Math.cos(t1)*Math.cos(t4))*l2+Math.cos(t1)*Math.sin(t2)*l1;
            J[1][2] = (Math.cos(t1)*Math.sin(t2)*Math.cos(t3)+Math.sin(t1)*Math.sin(t3))*Math.cos(t4)*l2;
            J[1][3] = (-Math.cos(t1)*Math.cos(t2)*Math.cos(t4)-(Math.cos(t1)*Math.sin(t2)*Math.sin(t3)-Math.sin(t1)*Math.cos(t3))*Math.sin(t4))*l2;
            J[1][4] = 0.0;

            J[2][0] = (-Math.cos(t1)*Math.cos(t2)*Math.sin(t4)+(Math.cos(t1)*Math.sin(t2)*Math.sin(t3)-Math.sin(t1)*Math.cos(t3))*Math.cos(t4))*l2-Math.cos(t1)*Math.cos(t2)*l1;
            J[2][1] = (Math.sin(t1)*Math.sin(t2)*Math.sin(t4)+Math.cos(t2)*Math.sin(t3)*Math.sin(t1)*Math.cos(t4))*l2+Math.sin(t1)*Math.sin(t2)*l1;
            J[2][2] = (Math.sin(t1)*Math.sin(t2)*Math.cos(t3)-Math.cos(t1)*Math.sin(t3))*Math.cos(t4)*l2;
            J[2][3] = (-Math.sin(t1)*Math.cos(t2)*Math.cos(t4)-(Math.sin(t1)*Math.sin(t2)*Math.sin(t3)+Math.cos(t1)*Math.cos(t3))*Math.sin(t4))*l2;
            J[2][4] = 0.0;

            // --from matlab up
            Point3d DX=obj.error_vector();
            // VA._normalize(DX);
            for (int i=0;i<5;i++)
            {
                dTeta[i]=(J[0][i]*DX.x+J[1][i]*DX.y+J[2][i]*DX.z)*0.0000005;
                if (dTeta[i]>0.1)
                    dTeta[i]=0.1;
                if (dTeta[i]<-0.1)
                    dTeta[i]=-0.1;
                //System.out.println("seg["+i+"].label:"+seg[i].label);
            }
            for (int i=0;i<5;i++)
                constrainedRotate(seg[i+1],dTeta[i]);
        }
        double err=obj.reach_error();
        //System.out.println("Err:"+err);
        return err;
    }

    /** Inverse kinematics. Find the joint angles given the target */
    /*
    public double[] findJointAngles(int giveup)
    {

//  double[] angles=new double[segc];
//  Thread inverse=new Inverse(this,giveup,angles);
//  inverse.start();
//  System.out.println("inverse has born .");
//  try{
//  inverse.join();
//  System.out.println("inverse is dead.");
//  } catch (InterruptedException e) {}
//  return angles;

        return null;
    }
    */

    public int findSegment(String s)
    {
        for (int i=0;i<segc;i++)
            if (seg[i].label.equals(s))
                return i;
        return -1;
    }
}


/*
 *
 * Erhan Oztop, 2000-2002  <br>
 * Source code by Erhan Oztop (erhan@atr.co.jp) <br>
 * Copyright August 2002 under <br>
 * University of Southern California Ph.D. publication copyright <br>
 */
 
