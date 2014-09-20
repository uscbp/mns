import java.io.*;
import java.net.*;
import java.applet.*;
import java.util.*;
import java.awt.*;
import java.lang.*;
import Acme.JPM.Encoders.*;

/**
 *
 * @author Erhan Oztop, 2001-2002 <br>
 * <br>
 * Source code by Erhan Oztop (erhan@atr.co.jp) <br>
 * Copyright August 2002 via <br>
 * University of Southern California Ph.D. publication copyright <br>
<br> <hr> <br>
This class is the main entry point for the MNS and LGM models and their
simulation environment. The whole system can be compiled using <br>
javac HV.java <br>
and can be run using <br>
java HV <br>
or <br>
java HV [.seg file [sidec radius [feat file?]]] <br>
.seg file defines the arm object to be loaded as a Segment class.
Each object would have many segments possible but currently only single
segment is allowed. 
sidec, radius defines the cylinder drawn around the skeleton.
feat file is not used anymore.

 */

//Change Applet <-> Frame to switch between application and html.
public class HV extends Frame
//public class HV extends Applet
{

    // --- runtime exec stuff
    Runtime r=Runtime.getRuntime();
    Process p=null;
    DataOutputStream pout;
    OutputStream rawpout;
    DataInputStream pin;
    InputStream rawpin;
    // ----

    String model="MNS";  // this is the nsl model to be linked to
    //Launcher nslL=null; // mns/nsl launcher
    static boolean negativeExample=false; // for Learn
    static boolean bellshape=true;
    static boolean isApplet=false;
    static HV self;
    static String RESfile="KolParameters.res";
    static Label infoLabel=null;
    static Panel infoPanel=null;
    static HVCanvas cv;
    HandBP bp=null;
    public Scrollbar wsc;
    public int wscMAX=100;
    private Gesture1 gest1=null;
    public Gesture  newgest=null;
    public Reach reach;
    private Match match1=null;
    private EyeMove eyemove=null;

    static Motor mcirc=null;
    public Hand rHand,obs,gazeat=null;
    public Segment goalL[]=new Segment[10];
    public int goalLc=0;
// scale is screen mag value for the eye
    public Scrollbar scale,xang,yang,zang,objscale;
    public int rx=30,ry= -90,rz=1300,sc=1,objsc=10;
    public double object_scale_fix=1;
    public Point3d U; // rotatio axis

    static public String[] grasps=
            {"NATURAL","SIDE", "POWER", "PRECISION", "POWER2","REACHONLY"};
    static public int graspi=0;
    static public int maxgraspc=grasps.length;
    static public boolean doubleBuffering=true;
    static public boolean recordCanvas=false;
    static public boolean recordGraphs=false;
    static public int recordSession=0;  // for Canvas
    static public int recordFrame=0;    // for Canvas
    static public String gifBase="canvas/C";  // for Canvas

    static public int LWrecordSession=0;
    static public boolean recordLW=false;
    static public int DLEV=0;
    static public int FANCY=0;
    static public boolean breakRequest=false;
    //the feature file to be loaded.
    public String featfile="real.ext";
    static final int SHOWSKEL=0;
    static final int SHOWWIRE=1;
    static final int SHOWHIDDEN=2;
    static final int SHOWSOLID=3;
    static final int SHOWSHADE=4;
    static final int SHOWLSHADE=5;
    static final int LASTMODE=5;
    public int showmode=SHOWSHADE;
    public String[] dispmodes={"Skeleton","Wireframe","HiddenLine","Solid","Shade","EdgedShade"};
    static public boolean showfeature=false;
    static public boolean traceon=false;


    static public Palette pal;
    static public URL baseURL=null;
    static public Button reachb=null;
    static public TextField speedtxt=null;
    static public TextField weight=null;
    static public TextField timeconst=null;
    static public TextField comwin=null;
    static public Button commandb=null;


    public Graspable[] objlist=new Graspable[10];
    int    objc=0;
    int    curobj=-1;
    int tiltAngle=0;


    static int minPAR,maxPAR, minMER, maxMER, minRAD, maxRAD;

    public HV()
    {
        //System.out.println(Mars.eye);
        Resource.read(RESfile);
        minPAR=Resource.getInt("minPAR");
        maxPAR=Resource.getInt("maxPAR");
        minMER=Resource.getInt("minMER");
        maxMER=Resource.getInt("maxMER");
        minRAD=Resource.getInt("minRAD");
        maxRAD=Resource.getInt("maxRAD");
        DLEV  =Resource.getInt("DLEV");
        FANCY =Resource.getInt("FANCY");
        rx=20; //(minPAR+maxPAR)/2;
        ry=(minMER+maxMER)/2;
        rz=maxRAD; //1125
        System.out.println("at this point 141, rx="+rx+"ry="+ry+"rz="+rz);
        Mars.clearComets();
        Mars.clearStars();  //create the lists
        setLayout(new BorderLayout());
        self=this;
        mcirc=new Motor();
        Panel p0=new Panel();
        p0.setLayout(new GridLayout(1,3));
        sc=1; //(int)(0.5+Mars.eye.Mag/5.0);
        scale=new Scrollbar(Scrollbar.VERTICAL, sc, 1, 1,901);
        objscale=new Scrollbar(Scrollbar.VERTICAL,objsc, 1, 1,101);
        xang=new Scrollbar(Scrollbar.VERTICAL, rx, 1, minPAR, maxPAR+1);
        System.out.println("at this point 155, xang="+xang);
        yang=new Scrollbar(Scrollbar.VERTICAL, ry, 1, minMER, maxMER+1);
        System.out.println("at this point 155, yang="+yang);
        zang=new Scrollbar(Scrollbar.VERTICAL, rz, 1, minRAD, maxRAD+1);
        System.out.println("at this point 155, zang="+zang);
        p0.add(scale);
        p0.add(objscale);
        p0.add(xang);
        p0.add(yang);
        p0.add(zang);
        add(p0,"West");
        Panel p1=new Panel();
        Panel p2=new Panel();
        infoPanel=new Panel();
        infoLabel=new Label("Learning to grasp. Erhan Oztop September 2001");
        infoLabel.setBackground(Color.black);
        infoLabel.setForeground(Color.white);


        p1.setLayout(new GridLayout(3,4));
        p2.setLayout(new GridLayout(2,4));
        Button lastb;
        if (IamFrame()) {
            p1.add(lastb=new Button("QUIT"));
            lastb.setBackground(Color.red); lastb.setForeground(Color.white);
        }
        else {
            p1.add(new Label("Applet"));
            DLEV=0;
        }

        //lastb.setBackground(Color.blue); lastb.setForeground(Color.white);
        // p1.add(lastb=new Button("gENCLOSE"));
        // lastb.setBackground(Color.blue); lastb.setForeground(Color.white);
        //p1.add(lastb=new Button("iSTATE"));
        //p1.add(lastb=new Button("RESTART"));
        // p1.add(lastb=new Button("RESETMODEL"));

        // p1.add(lastb=new Button("MATCHMANUAL"));
        // lastb.setBackground(Color.yellow);
        // p1.add(lastb=new Button("MATCHFAKE"));
        //  lastb.setBackground(Color.yellow);
        //  p1.add(lastb=new Button("MATCHREAL"));
        //  lastb.setBackground(Color.yellow);

        p2.add(lastb=new Button("BREAK"));
        lastb.setBackground(Color.magenta);
        p2.add(lastb=new Button("resetARM"));
        lastb.setBackground(Color.green);
        lastb.setForeground(Color.black);
        p2.add(lastb=new Button("resetEYE"));
        lastb.setBackground(Color.green);
        lastb.setForeground(Color.black);
        p2.add(lastb=new Button("PROFILE"));
        lastb.setBackground(Color.white);
        lastb.setForeground(Color.black);
        p2.add(lastb=new Button("CLEAR_TRAJ"));
        lastb.setBackground(Color.white);
        lastb.setForeground(Color.black);

        p2.add(lastb=new Button("REACH"));
        lastb.setBackground(Color.yellow);
        lastb.setForeground(Color.black);
        reachb=lastb;

//  	p2.add(lastb=new Button("RECORD LW"));
//      lastb.setBackground(Color.orange);
//          lastb.setForeground(Color.black);
//          reachb=lastb;
        p2.add(lastb=new Button("TILT OBJ"));
        lastb.setBackground(Color.white);
        lastb.setForeground(Color.black);

        p2.add(lastb=new Button("RECORD CANVAS"));
        lastb.setBackground(Color.orange);
        lastb.setForeground(Color.black);


        p2.add(lastb=new Button("RECORD GRAPHS"));
        lastb.setBackground(Color.orange);
        lastb.setForeground(Color.black);
        reachb=lastb;
        p1.add(lastb=new Button("xEYE"));
        lastb.setBackground(Color.green);
        lastb.setForeground(Color.black);
        p1.add(lastb=new Button("yEYE"));
        lastb.setBackground(Color.green);
        lastb.setForeground(Color.black);
        p1.add(lastb=new Button("zEYE"));
        lastb.setBackground(Color.green);
        lastb.setForeground(Color.black);
        // p1.add(lastb=new Button("GAZESW"));
        // p1.add(lastb=new Button("DUMPHAND"));
        // p1.add(lastb=new Button("SHOWFEATURE"));
        p1.add(lastb=new Button("JOINTCONTROL"));
        lastb.setBackground(Color.blue);
        lastb.setForeground(Color.white);
        p1.add(lastb=new Button("LW-OBJ"));
        lastb.setBackground(Color.red);
        lastb.setForeground(Color.black);
        p1.add(lastb=new Button("SHOWMODE"));
        lastb.setBackground(Color.white);
        lastb.setForeground(Color.red);

        p1.add(lastb=new Button("BUFFERING"));
        lastb.setBackground(Color.green);
        lastb.setForeground(Color.black);
        p1.add(lastb=new Button("BELLSHAPE"));
        lastb.setBackground(Color.white);
        lastb.setForeground(Color.red);
        p1.add(lastb=new Button("GRASPTYPE"));
        lastb.setBackground(Color.white);
        lastb.setForeground(Color.red);


        p1.add(lastb=new Button("OBJECT"));
        lastb.setBackground(Color.white);
        lastb.setForeground(Color.red);
        p1.add(lastb=new Button("VISREACH"));
        lastb.setBackground(Color.orange);
        lastb.setForeground(Color.black);
        p1.add(lastb=new Button("BABBLE"));
        lastb.setBackground(Color.orange);
        lastb.setForeground(Color.black);
        //p1.add(lastb=new Button("RECORD"));
        p1.add(lastb=new Button("GENERATE DATA"));
        lastb.setBackground(Color.red);
        lastb.setForeground(Color.white);
        p1.add(lastb=new Button("RECOGNIZE"));
        lastb.setBackground(Color.yellow);
        lastb.setForeground(Color.blue);

        // p1.add(lastb=new Button("*NSL*"));
        // lastb.setBackground(Color.magenta);
        // p1.add(lastb=new Button("INV_KIN"));
        //lastb.setBackground(Color.green);
        p2.add(new Label("Speed:",Label.RIGHT));
        p2.add(speedtxt=new TextField("    3"));
        p2.add(weight=new TextField("erh.wgt"));
        p2.add(timeconst=new TextField("1"));
        p2.add(lastb=new Button("GRASP"));
        lastb.setBackground(Color.black);
        lastb.setForeground(Color.green);
        p2.add(lastb=new Button("WRISTGRASP"));
        lastb.setBackground(Color.black);
        lastb.setForeground(Color.green);
        p2.add(commandb=new Button("EXECUTE"));
        commandb.setBackground(Color.black);
        commandb.setForeground(Color.green);

        add("North",p1);
        infoPanel.setLayout(new GridLayout(3,1));

        infoPanel.add(infoLabel);
        infoPanel.add(p2);
        infoPanel.add(comwin=new TextField("fancy+"));
        comwin.setBackground(Color.black);
        comwin.setForeground(Color.green);

        add("South",infoPanel);
    }

    static public int recbarReq=0;  // no request
    static public double precBar,sideBar,powBar;
    static public void updateRecBars(double prec,double side,double pow) {
        precBar=prec;
        sideBar=side;
        powBar=pow;
        recbarReq=1;  //request update
    }
    static public String setInfo(String s) {
        if (HV.DLEV < 0) return "";
        infoLabel.setText(s);
        return s;
    }
    static public void setInfoReady() {
        if (HV.DLEV < 0) return;
        infoLabel.setText("Ready.");
    }
    public boolean IamApplet()
    {
        return !IamFrame();
    }
    public boolean IamFrame()
    {
        Object f=null;
        f=this;
        if (f instanceof Frame) return true;
        else return false;
    }

    public Frame frameCast(Object o)
    {
        return (Frame)o;
    }
    public Applet appletCast(Object o)
    {
        return (Applet)o;
    }

    public Graspable getCurrentObject() {
        if (curobj>=0)
            return objlist[curobj];
        return null;
    }
    public void setTargetPosition(double x,double y,double z)
    {
        System.out.println("setTargetPosition is called with reals x y z.");
        System.out.println("x"+x+"y"+y+"z"+z);
        if (curobj>=0)
            objlist[curobj].moveto(x,y,z);
        System.out.println("Fancy:"+FANCY);
        System.out.println(objlist[curobj].objectCenter.str());
        if (FANCY>0) setInfo("Object position:"+ objlist[curobj].objectCenter.str());
    }
    public void setTargetPosition(int x,int y,int z)
    {
        System.out.println("setTargetPosition is called with integers x y z.");
        System.out.println("x"+x+"y"+y+"z"+z);
        if (curobj>=0)
            objlist[curobj].moveto(x,y,z);
        System.out.println("Fancy:"+FANCY);
        System.out.println(objlist[curobj].objectCenter.str());
        if (FANCY>0) setInfo("Object position:"+ objlist[curobj].objectCenter.str());
    }
    public void setTargetScale(double old,double newv)
    {
        if (curobj>=0)
        {

            Point3d p= objlist[curobj].objectCenter.duplicate();
            //System.out.println("Before moving:"+p.str());
            objlist[curobj].rect_moveto(0,0,0);
            //Point3d q= objlist[curobj].objectCenter.duplicate();
            //System.out.println("Now moved to zero:"+q.str());
            objlist[curobj].root.scale(newv/old);
            object_scale_fix*=newv/old;
            objlist[curobj].rect_moveto(p);
        }

    }

    public void updatePositionBars(int x,int y,int z)
    {
    }
    public void domoreHV()
    {
        if (IamFrame())
        {
            frameCast(this).setTitle("3D Hand - Erhan Oztop Jan2000");
            baseURL=null;
        }
        sc=(int)(0.5+Mars.eye.Mag/5.0);
        scale.setValue(sc);
        cv=new HVCanvas();
        cv.setShowmode(showmode);
        add("Center",cv);
        cv.setBackground(Color.black);
        add("Center",cv);

        pal=new Palette(256);
        pal.spread(20,20+31,175,175,175, 255,255,255);
        pal.spread(20+32,20+32+31, 75,75,75, 150,150,150);
        rHand=(Hand)Mars.getObject("HAND");
        //goalL[goalLc++]=rHand.getJoint("THUMB3");
        //goalL[goalLc++]=rHand.getJoint("INDEX2");
        //goalL[goalLc++]=rHand.getJoint("WRISTz");
        ////box  =Mars.getObject("BOX");
        ////box.udef_Pc=0;
        ////VA._normalize(box.root.joint_axis);
        //  box.moveto(new Point3d(rx,ry,rz));
        //  box.setPlaneProperties();


        //	box.udef_P=new Point3d[3];
        // 	box.udef_P[box.udef_Pc++]=new Point3d(-100,0,0);
        // 	box.udef_P[box.udef_Pc++]=new Point3d( 100,0,0);
        //	box.udef_P[box.udef_Pc++]=new Point3d( -100,0,-200);


        setObject("BAR");
        setGrasp("NATURAL");

        //rHand.mergeObject(box);
        //box.noshow=true;
        cv.refreshDisplay();
        Mars.project(); // firs project so that first repaint works OK.
        System.out.println("first project done");
        rHand.showObjectFrame();
        rHand.toggleObjectFrame(); //hide it for now
        rHand.makeUpright();
        //rHand.installCfg("initial.cfg");
        //setBounds(100,100,450,350);
        //show();
        System.out.println("Was it called from here? 433");
        updateScrollValues();
        cv.refreshDisplay();
        String we=Resource.getString("WeightFile");
        String wedir=Resource.getString("WeightFileDir");
        if (wedir==null) wedir="./";
        if (we!=null) {
            if (!we.equals("none") && !we.equals("")) {
                System.out.println(setInfo("Loading the weight file:"+wedir+
                        "/"+we));
                executeCommand("loadnet "+wedir+"/ "+we);
            }
        }
    }

    void addObj(Graspable o)
    {
        objlist[objc++]=o;
    }

    void setObject(String s)
    {
        Object3d o=Mars.getObject(s);
        if (o==null) return;
        int k=-1;
        for (int i=0;i<objc;i++)
            if (objlist[i]==o) k=i;
        if (k!=-1) setObject(s,k);
    }

    void setObject(int i) {
        setObject("Next in object list",i);
    }
    private boolean firsttimer=true;
    void setObject(String s,int i)
    {
        if (i>=objc) return;
        if (curobj!=-1)
        {
            rHand.discardMerged();
        }
        rHand.mergeObject(objlist[i]);
        curobj=i;
        rx=-3121; ry=-1415; //whatever
        if (!firsttimer) {
            setInfo("Ready.  - object set to "+s /*objlist[i].root.label*/ );
        } else firsttimer=false;
        System.out.println("Or.. Was it called from here? 490");
        updateScrollValues();
    }

    void nextObject()
    {
        setObject((curobj+1)%objc);
    }

    void setGrasp(String s)
    {   int j=-1;
        for (int i=0;i<maxgraspc;i++)
            if (grasps[i].equals(s)) j=i;
        if (j!=-1)
            graspi=j;
        System.out.println("Grasp selected:"+grasps[graspi]);
    }
    private Image im1,im2,buf1,buf2;
    private String urltarget;
    public void init()
    {
        Applet me;
        if (IamFrame()) return;
        else me=appletCast(this);
        isApplet=true;
        System.out.println("Coming...");

        URL codebase=me.getCodeBase();
        System.out.println("Codebase:"+codebase);
        String im1s=me.getParameter("IMAGE1");
        String im2s=me.getParameter("IMAGE2");
        urltarget=me.getParameter("TARGET");
        im1=me.getImage(codebase,im1s);
        im2=me.getImage(codebase,im2s);
        //printSize();
        System.out.println("2-Coming...");
        //baseURL=me.getDocumentBase();
        baseURL=me.getCodeBase();
        prepareHV(this,null);
        domoreHV();
        //System.out.println("URLDOCbase:"+baseURL);
    }

    static  public void prepareHV(HV already,String[] argv)
    {
        System.out.println("4#$%3453245");
        String s=null;
        String featfile="real.ext";
        int rad=20;
        int sidec=4;
        if (argv!=null) {
            if (argv.length>0 ) s=argv[0];
            else  s="objects/erharm.seg";
            if (argv.length==2)
            {
                featfile=argv[1];
            } else
            if (argv.length==3 || argv.length==4)
            {sidec=Elib.toInt(argv[1]);
                rad=Elib.toInt(argv[2]);
            }
            if (argv.length==4)
            {featfile=argv[3];}
            else;

        } else s="objects/erharm.seg";

        Hand hand=new Hand(s,sidec,rad);      // create hand
        Eye eye;
        if (hand.root.suggested_scale==0)
            eye=new Eye(10,20); // create an eye
        else
            eye=new Eye(hand.root.suggested_F,hand.root.suggested_scale);
        System.out.println("suggested Fz,F,scale:"+hand.root.suggested_Fz+","+hand.root.suggested_F+","+hand.root.suggested_scale);
        eye.lock(0,0,0);
        eye.YrotateViewPlane(Math.PI/25);
        eye.XrotateViewPlane(Math.PI/5);
        Graspable sheet=new Graspable(hand,"objects/sheet.seg",0,5,"SIDE");
        Graspable bar=new Graspable(hand,"objects/ibar.seg",6,100,"PRECISION"); // 4,75
        Graspable box=new Graspable(hand,"objects/box.seg",0,5,"PRECISION");
        Graspable pent=new Graspable(hand,"objects/pent.seg",0,5,"POWER");
        Graspable plate=new Graspable(hand,"objects/plate.seg",0,5,"PRECISION");
        Graspable coin=new Graspable(hand,"objects/coin.seg",7,100,"SIDE");
        Graspable mug=new Graspable(hand,"objects/ring.seg",5,50,"POWER");
        pent.root.scale(0.4);
        mug.root.scale(0.5);
        Mars.addObject("HAND",hand);
        Mars.addObject("PENT",pent);
        Mars.addObject("BOX",box);
        Mars.addObject("SHEET",sheet);
        Mars.addObject("BAR",bar);
        Mars.addObject("PLATE",plate);
        Mars.addObject("COIN",coin);
        Mars.addObject("MUG",mug);
        box.noshow=true;
        sheet.noshow=true;
        pent.noshow=true;
        bar.noshow=true;
        plate.noshow=true;
        coin.noshow=true;
        mug.noshow=true;
        Mars.setEye(eye);
        Mars.setCube(1900*1.3);
        HV hv=already;
        if (hv==null) hv=new HV();
        hv.addObj(mug);
        hv.addObj(coin);
        hv.addObj(plate);
        hv.addObj(bar);
        hv.addObj(pent);
        hv.addObj(box);
        hv.addObj(sheet);
        hv.featfile=featfile;

        hv.setBounds(100,100,650,650);
        hv.show();
    }



    public static void main(String[] argv)
    {
        HV mainhv=new HV();     // when applet this is done by netscape
        prepareHV(mainhv,argv); // then the init of applet does this
        mainhv.domoreHV();      // again init() calls domoreHV in applet case
    }

// -------------------------------------------------------------------
// AWT events


    int oldy=-1,oldx=-1;
    public boolean mouseUp(Event evt, int x, int y)
    {
        Point p=cv.getLocation();
        oldx=-1;
        oldy=-1;
        return true;
    }

    public boolean mouseDown(Event evt, int x, int y)
    {
        Point p=cv.getLocation();
        x-=p.x;
        y-=p.y;
        oldx=x;
        oldy=y;
        return true;
    }
    public boolean mouseDrag(Event evt, int x, int y)
    {
        Point p=cv.getLocation();
        x-=p.x;
        y-=p.y;
        if (oldx<0) {oldx=x; oldy=y; return true;}
        int dy=y-oldy;
        int dx=x-oldx;
        oldx=x; oldy=y;

        double xrot=dy*Math.PI/180;
        double yrot=dx*Math.PI/180;
/*
   Mars.eye.XrotateViewPlane(xrot);
   Mars.eye.YrotateViewPlane(yrot);
*/
        Mars.eye.adjustViewPlane(dx,dy);

        Mars.project();
        cv.repaint();
        return true;
    }


    public void updateScrollValues()
    {
        int newosc=objscale.getValue();
        int newrx=xang.getValue();
        int newry=yang.getValue();
        int newrz=zang.getValue();
        int newsc=scale.getValue();
        if (newsc!=sc) { Mars.eye.setMag(5*newsc); sc=newsc; }

        if (newrx!=rx || newry!=ry || newrz!=rz)
        {
            System.out.println("rx="+rx+"ry="+ry+"rz="+rz);
            System.out.println("newrx="+newrx+"newry="+newry+"newrz="+newrz);
            System.out.println("setTargetPosition is being called from updateScrollValues with xyz.");
            System.out.println(-newrx*Math.PI/180+"y"+-newry*Math.PI/180+"z"+newrz);
            setTargetPosition(-newrx*Math.PI/180,-newry*Math.PI/180,newrz);
            rx=newrx; ry=newry; rz=newrz;
            //double T=rHand.orientPalmWristz(objlist[curobj]);
            //System.out.println("Wristz angle:"+T);
            //rHand.constrainedRotate(rHand.wristz,T);
            // rHand.clearTrajectory();
            //dynamic trajectory later
            if (reach!=null)
            { //String nm=reach.dimensionName;
                //toggleReach(nm);
                //toggleReach(nm);
            }


        }
        if (newosc!=objsc)
        {
            setTargetScale(objsc/10.0+0.1,newosc/10.0+0.1);
            objsc=newosc;

        }

        cv.refreshDisplay();
/*
     if (match1!=null) {
     match1.updateReal(Feature.ftype_PALM);
     match1.resetMatching(Feature.ftype_PALM);
     }
*/
        //System.out.println("Weird value is:"+rHand.weirdval());
    }

    public void executeCommand(String com) {
        String[] pars=new String[40];
        for (int i=0;i<pars.length;i++) pars[i]=null;

        StringTokenizer st= new StringTokenizer(com," ");
        int parc=0;
        while (st.hasMoreTokens()) {
            pars[parc++]=st.nextToken();
        }
        String command=pars[0];
        if (command==null) {setInfo("Nothing to execute!"); return;}
        if (command.equals("grasp"))
        {
            toggleReach("PDFgrasp");
            return;
        }
        if (command.equals("wrist-grasp")) {
            toggleReach("PDFwristgrasp");
            return;
        }
        if (command.equals("dumpnet")) {
            Graspable g=objlist[curobj];
            String suf=".LGM-default";
            String pre="./";
            String us="["+g.myname+" at "+objlist[curobj].objectCenter.str()+"]";
            if (parc==2) suf=pars[1];
            if (parc==3) { pre=pars[1]; suf=pars[2];}
            if (parc>3) {
                pre=pars[1]; suf=pars[2];
                for (int i=3; i < parc; i++) us+=" "+pars[i];
            }
            mcirc.dumpNet(pre,suf,us);
            System.out.println(setInfo("Wrote "+pre+"W*"+suf+" #"+us));
            return;
        }
        if (command.equals("loadnet")) {
            String suf=".LGM-default";
            String pre="./";
            if (parc==2) suf=pars[1];
            if (parc==3) { pre=pars[1]; suf=pars[2];}
            mcirc.loadNet(pre,suf);
            System.out.println(setInfo("Loaded "+pre+"W*"+suf));
            return;
        }
        if (command.equals("tilt")) {
            if (parc==1) objlist[curobj].resetRot();
            else {
                objlist[curobj].setTilt(Math.PI/180*Elib.toDouble(pars[1]));
            }
            cv.refreshDisplay();
            return;
        }

        if (command.equals("source")) {
            String fname=RESfile;
            if (parc==2) fname=pars[1];
            System.out.println(setInfo("(re)reading resource file..."));
            Resource.read(fname);
            mcirc.updateResourcePars();
            return;
        }


        if (command.equals("gif")) {
            String fname="snapshot.gif";
            if (parc==2) fname=pars[1];
            if (doubleBuffering) {
                System.out.println(setInfo("Writing snapshot "+fname));
                createGif(fname);
            } else {
                System.out.println(setInfo("Double buffering must be on for gif snapshot!")); }
            return;
        }

        if (command.equals("get")) {
            if (parc<2) {setInfo("Need parameter");return;}
            if (pars[1].equals("PDFthreshold")) {
                setInfo("PDFthreshold is:"+HV.mcirc.PDFthreshold);
                return;
            }

            if (pars[1].equals("softconGAIN")) {
                setInfo("Graspable.softconGAIN is : "+Graspable.softconGAIN);
                return;
            }
            if (pars[1].equals("PDFgraspcINI")) {
                setInfo("HV.rhand.PDFgraspcINI is"+rHand.PDFgraspcINI);
                return;
            }
            if (pars[1].equals("objectpos")) {
                setInfo("Object is at:"+objlist[curobj].objectCenter.str());
                return;
            }
            setInfo("Unknown parameter dimensionName!");
            return;
        }


        if (command.equals("set")) {
            if (parc<3) {setInfo("Need parameter and a value");return;}

            if (pars[1].equals("bgcolor")) {
                HVCanvas.backColor=
                        new Color(Elib.toInt(pars[2]),Elib.toInt(pars[3]),Elib.toInt(pars[4]));
                return;
            }

            if (pars[1].equals("PDFthreshold")) {
                HV.mcirc.PDFthreshold=Elib.toDouble(pars[2]);
                setInfo("PDFthreshold set to:"+HV.mcirc.PDFthreshold);
                return;
            }

            if (pars[1].equals("softconGAIN")) {
                Graspable.softconGAIN=Elib.toDouble(pars[2]);
                setInfo("Graspable.softconGAIN is set to:"+Graspable.softconGAIN);
                return;
            }
            if (pars[1].equals("PDFgraspcINI")) {
                rHand.PDFgraspcINI=Elib.toInt(pars[2]);
                setInfo("HV.rhand.PDFgraspcINI set to:"+rHand.PDFgraspcINI);
                return;
            }
            if (pars[1].equals("objectpos")) {
                int x=Elib.toInt(pars[2]);
                int y=Elib.toInt(pars[3]);
                int z=Elib.toInt(pars[4]);
                System.out.println("setTargetPosition is being called from executecommand=set with xyz.");
                System.out.println("x"+x+"y"+y+"z"+z);
                setTargetPosition(x,y,z);
                cv.refreshDisplay();
                setInfo("Object is at:"+objlist[curobj].objectCenter.str());
                return;
            }
            setInfo("Unknown parameter dimensionName!");
            return;
        }
        if  (command.equals("softcon+")) {
            Object3d.softcon=Graspable.softcon_J2_45;
            setInfo("Softconstraint in gradient_arm is activated.");
            return;
        }
        if  (command.equals("softcon-")) {
            Object3d.softcon=0;
            setInfo("Softconstraint in gradient_arm is deactivated.");
            return;
        }
        if  (command.equals("dlev+")) {
            HV.DLEV=1;
            setInfo("Now -> HV.DLEV:"+HV.DLEV);
            return;
        }
        if  (command.equals("dlev-")) {
            HV.DLEV=0;
            setInfo("Now -> HV.DLEV:"+HV.DLEV);
            return;
        }
        if  (command.equals("plot+")) {
            Motor.PLOTLEV=1;
            setInfo("Now -> Motor.PLOTLEV:"+Motor.PLOTLEV);
            return;
        }
        if  (command.equals("plot-")) {
            Motor.PLOTLEV=0;
            setInfo("Now -> Motor.PLOTLEV:"+Motor.PLOTLEV);
            return;
        }
        if  (command.equals("cube+")) {
            Mars.drawCube=1;
            setInfo("drawCube enabled. Use cube- to turnoff...");
            return;
        }
        if  (command.equals("cube-")) {
            Mars.drawCube=0;
            setInfo("drawCube disabled. Use cube+ to turnon...");
            return;
        }
        if  (command.equals("pdf"))
        {
            System.out.println(setInfo(rHand.toggleActionSelection()));
            return;
        }
        if  (command.equals("fancy+")) {
            HV.FANCY=1;
            setInfo("Now -> HV.FANCY:"+HV.FANCY);
            return;
        }
        if  (command.equals("fancy-")) {
            HV.FANCY=0;
            setInfo("Now -> HV.FANCY:"+HV.FANCY);
            return;
        }
        if (command.equals("showrot")) {
            Gplot.resetGeom(100,100,400,400);
            HV.mcirc.hand_rot.plotFiringRate(HV.mcirc,HV.mcirc.callback_Wrist);
            return;
        }
        if (command.equals("showoff")) {
            Gplot.resetGeom(100,100,400,400);
            HV.mcirc.hand_off.plotFiringRate(HV.mcirc,HV.mcirc.callback_Offset);
            return;
        }
        if (command.equals("help")) {
            String cms="(see HV.java) set <par> <value>, fancy+/-, dlev+/, showrot, showoff, grasp, loadnet [p1], dumpnet [p1], plotlev+/-";
            System.out.println("HV- shell command list (may be incomplete see HV.java)");
            System.out.println("fancy+/:  Flag for the amount of grasphics update and visual helps");
            System.out.println("dlev+/ :  Flag for debug information");
            System.out.println("showrot:  Display softmaxed Wrist Rotation Matrix avareged over the heading");
            System.out.println("showoff:  Display softmaxed Offset Matrix avareged over the radius");
            System.out.println("grasp  :  Perform the grasp using the current motor planning net");
            System.out.println("dumpnet [p1]: save the network weights W_??.p1 (if no p1 is specified then p1=.default");
            System.out.println("loadnet [p1]: load the network weights W_??.p1 (if no p1 is specified then p1=.default\n             Note that the size of the weights should match the compiled code and/or Resource file.");
            System.out.println("plotlev+/-: turns on/off extensive plots for action generation (grasp command)");
            return;
        }
        setInfo("No such command!"); return;
    }

    public void createGif(String fname) {
        try {
            DataOutputStream d=Elib.openfileWRITE(fname);
            GifEncoder g=new GifEncoder(cv.lastFrame(),d);
            g.encode();
            System.out.println(setInfo("Snapshot written  as gif file:"+fname));
        } catch(IOException e) {System.out.println(setInfo("Cannot create file:"+fname));}
    }

    synchronized public void createGif() {
        String fname="---------";
        try {
            HV.recordFrame++;
            int dum=HV.recordSession*100000+HV.recordFrame;
            fname=HV.gifBase+dum;


            DataOutputStream d=Elib.openfileWRITE(fname);
            GifEncoder g=new GifEncoder(cv.lastFrame(),d);
            g.encode();
            System.out.println(setInfo("Wrote frame:"+fname));
        } catch(IOException e) {System.out.println(setInfo("Cannot create file:"+fname));}

    }

    public boolean handleEvent(Event evt)
    { if (evt.id == Event.WINDOW_DESTROY) exitHV();
    else if (evt.id == Event.SCROLL_ABSOLUTE ||
            evt.id == Event.SCROLL_LINE_DOWN ||
            evt.id == Event.SCROLL_LINE_UP ||
            evt.id == Event.SCROLL_PAGE_DOWN ||
            evt.id == Event.SCROLL_PAGE_UP
            )
    {
        updateScrollValues();
    }
        return super.handleEvent(evt);
    }
    public boolean action(Event evt, Object arg)
    {
        String s=(String)arg;
        if (!doaction(s))
            return super.action(evt,arg);
        return true;
    }

    // arguments should have absolute paths (if they are dirs or files)
    public void systemExec(String what) {
        try{
            p=r.exec(what);
        } catch (Exception e)
        { System.err.println("Error running gnuplot"); }
        pin =new DataInputStream(rawpin=p.getInputStream());
        pout=new DataOutputStream(rawpout=p.getOutputStream());
    }

    static String datadir="/tmp";
    static String ingif_base="/tmp/DI";
    static String outgif_base="/tmp/DO";
    static int dataSession=0;
    static int dataFrame=0;


    static int graphSession=0;
    static int outFilec=0;
    static String outFileBase="graphs/O";
    static public String genOutName() {
        if (!recordGraphs) return null;
        int id=graphSession*100000+outFilec++;
        return outFileBase+outFilec;
    }

    static int inParFilec=0;
    static String inParFileBase="graphs/I";
    static public String genInParName() {
        if (!recordGraphs) return null;
        int id=graphSession*10000+inParFilec++;
        return inParFileBase+id;
    }
    //  static public void writeGraph() {
//          dataFrame++;
//          int id=10000*dataSession+dataFrame;
//  	String s="inout2gif "+datadir+" "+ingif_base+id+" "+outgif_base+"-ALL";
//          System.out.println("EXec:"+s);
//  	self.systemExec(s);
//      }
    public boolean doaction(String arg)
    {int hit;

        if (arg.equals("SHOWMODE"))
        {
            String s="showmode was "+dispmodes[showmode]+". ";
            s="Ready. ";
            showmode=(showmode+1) ;
            if (showmode>LASTMODE) showmode=0;
            s+=" - showmode is set to "+dispmodes[showmode];
            cv.setShowmode(showmode);
            Mars.project();
            cv.repaint();
            setInfo(s);
        }
        else if (arg.equals("iSTATE"))
        {
            double a1[]=rHand.getAngleList();
            double a2[]=obs.getAngleList();
            double toterr=0;
            for (int i=0;i<a1.length;i++)
            {
                toterr+=Math.abs(a1[i]-a2[i]);
                System.out.println(a1[i]*180/Math.PI+" X "+a2[i]*180/Math.PI);
            }
            System.out.println("REAL"+" X "+"MODEL = (total error) = "+toterr*180/Math.PI);
            rHand.root.print();
            System.out.println("------------------");
            obs.root.print();
        }
        else if (arg.equals("MATCHMANUAL"))
        {
            toggleManualMatch();
        }
        else if (arg.equals("RESETMODEL"))
        {
            rHand.makeStanding();
            cv.refreshDisplay();
        }
        else if (arg.equals("resetARM"))
        {
            rHand.makeStanding();
            //rHand.makeUpright();
            cv.refreshDisplay();
        }
        else if (arg.equals("CLEAR"))
        {
            cv.clear();
            cv.repaint();
        }
        else if (arg.equals("gENCLOSE"))
        {
            // toggleGesture("power.ges");
            toggleEnclose();
        }
        else if (arg.equals("MATCHREAL"))
        {
            obs.resetJoints();
            Mars.eye.reset();
            toggleRealMatch();
        }
        else if (arg.equals("BREAK"))
        {
            setInfo("Ready. -user interrupt");
            rHand.kill_ifActive();
        }
        else if (arg.equals("PROFILE"))
        {
            toggleTrace();
        }
        else if (arg.equals("resetEYE"))
        {
            Mars.eye.reset();
            Mars.project();
            cv.paint(cv.getGraphics());
        }
        else if (arg.equals("xEYE"))
        {
            if (!toggleEyeMove(0.1,0,0)) System.out.println("EYE stopped.");
        }
        else if (arg.equals("yEYE"))
        {
            if (!toggleEyeMove(0,0.1,0)) System.out.println("EYE stopped.");
        }
        else if (arg.equals("zEYE"))
        {
            if (!toggleEyeMove(0,0,0.1)) System.out.println("EYE stopped.");
        }
        else if (arg.equals("DUMPHAND"))
        {
            rHand.dumpAngles();
        }
        else if (arg.equals("JOINTCONTROL"))
        {
            rHand.toggleObjectFrame();
        }
        else if (arg.equals("LW-OBJ"))
        {
            rHand.root.outputOBJ();
        }
        else if (arg.equals("WRISTGRASP"))
        {
            toggleReach("PDFwristgrasp");
        }
        else if (arg.equals("REACH"))
        {
            toggleReach("execute");
        }
        else if (arg.equals("EXECUTE"))
        {
            executeCommand(comwin.getText());
        }
        else if (arg.equals("GRASP"))
        {
            toggleReach("PDFgrasp");
        }
        else if (arg.equals("RECORD CANVAS"))
        {
            if (!recordCanvas) {
                recordSession++;
                recordFrame=0;
                recordCanvas=true;
                setInfo("Canvas recording ON. (Each refresh will be recorded).");
            } else {
                recordCanvas=false;
                setInfo("Canvas recording OFF. Last session recorded "+recordFrame+" frames.");
            }

        } else if (arg.equals("RECORD LW"))
        {
            if (!recordLW) {
                LWrecordSession++;
                recordLW=true;
                setInfo("LW data recording ON. (Each refresh will be recorded).");
            } else {
                recordLW=false;
                setInfo("LW recording OFF. Just recorded session's id was:"+LWrecordSession);
            }

        }

        else if (arg.equals("RECORD GRAPHS"))
        {
            if (!recordGraphs) {
                graphSession++;
                inParFilec=0;
                outFilec=0;
                recordGraphs=true;
                setInfo("Graph recording ON. (each recognize will be recorded).");
            } else {
                recordGraphs=false;
                setInfo("Graph recording OFF."+graphSession+" sessions recorded.");
            }

        }

        else if (arg.equals("EXEC"))
        {
            //systemExec("inout2gif /tmp /tmp/sik1 /tmp/sik2");
            System.out.println("runnning systemEXEC!");
            systemExec("fakeredir uuencode /home/erhan/test.txt /tmp/javres javadid");
        }
        /*
        else if (arg.equals("INV_KIN"))
        {
            rHand.makeInvKinMap(3,3,3,new Point3d(-800,0,0),new Point3d(800,800,500));
        }
        */
        else if (arg.equals("RECORD"))
        {
            toggleReach("record");
        }
        else if (arg.equals("GENERATE DATA"))
        {
            generateData(100);
            //generateRandomData(100);
        }
        else if (arg.equals("RECOGNIZE"))
        {
            setInfo("Reach started for recognition.");
            toggleTrace(); // show thee profile
            object_scale_fix=1;
            recognize();

        }
        else if (arg.equals("*NSL*"))
        {
            nslrun();
        }
        else if (arg.equals("CLEAR_TRAJ"))
        {
            rHand.clearTrajectory();
        }
        else if (arg.equals("VISREACH"))
        {
            setInfo("VISREACH: solving inverse kinematics...");
            toggleReach("visual");
        }
        else if (arg.equals("BABBLE"))
        {
            setInfo("BABBLE: babbling started...");
            startBabble();
            //toggleReach("babble");
        }
        else if (arg.equals("GRASPTYPE"))
        {
            graspi=(graspi+1)%maxgraspc;
            System.out.println("Grasp selected:"+grasps[graspi]);
        }
        else if (arg.equals("OBJECT"))
        {
            nextObject();
            cv.refreshDisplay();
        }
        else if (arg.equals("TILT OBJ"))
        {
            tiltAngle = (tiltAngle+15) % 360;
            objlist[curobj].setTilt(tiltAngle*Math.PI/180);
            setInfo("Object tilt angle is set to:"+tiltAngle);
            cv.refreshDisplay();
        }
        else if (arg.equals("BUFFERING"))
        {
            doubleBuffering=!doubleBuffering;
            if (doubleBuffering)  setInfo("Ready. (doubleBuffering is set to ON)");
            else setInfo("Ready. (doubleBuffering is set to OFF)");
            if (doubleBuffering==false) cv.buf=null;
            else cv.refreshDisplay();
        }
        else if (arg.equals("BELLSHAPE"))
        {
            bellshape=!bellshape;
            if (bellshape) setInfo("Ready. (Realistic reach is selected)");
            else setInfo("Ready. (linear reach is selected)");

        }
        else if (arg.equals("SHOWFEATURE"))
        {
            showfeature=!showfeature;
            Mars.project();
            cv.paint(cv.getGraphics());
        }

        else if  (arg.equals("GAZESW"))
        {
            if (gazeat==obs) gazeat=rHand;
            else  gazeat=obs;
            Mars.eye.lookAt(gazeat.wristx.joint_pos);
            Mars.project();
            cv.paint(cv.getGraphics());
        }
        else if (arg.equals("MATCHFAKE"))
        {
            if (match1==null)
            {
                if (obs.noshow) obs.noshow=false;
                match1=new Match(cv,rHand,obs);
                match1.start();
            } else
            { obs.noshow=true;
                match1.stop();
                match1=null;
            }
        }
        else if (arg.equals("QUIT"))
        {
            exitHV();
        }
        else return false;
        return true;
    }

    public void loadWeight()
    {
        if (bp==null)
        {
            bp=new HandBP();
            bp.resize(400,300);
            bp.show();
        }
        System.out.println("Making the network from Weight file"+weight.getText());
        System.out.println("Base url:"+baseURL);
        bp.netFromWeight(baseURL, weight.getText());
        System.out.println("Xdim->HiddenDim->outoytDim:"+bp.Xdim+"->"+bp.Ydim+"->"+bp.Zdim);

    }
    public void recognize()
    {

        if (bp==null) loadWeight();
        if (bp==null) { System.err.println("Doesn't have a network to recognize!");
            return;
        }
        toggleReach("recognize");
    }

    public void nslrun()
    {
        if (bp==null) loadWeight();
        if (bp==null) {
            System.err.println("Doesn't have a network to recognize!");
            return;
        }
/*
     if (nslL==null) {
	 System.out.println("launching NSL with model "+model);
	 System.out.println("You have to press the button again once NSL comes ");
	 nslL=new Launcher(model);
	 nslL.start();
	 return;
     } 
	 
     transferWeights(bp,Elib.toDouble(timeconst.getText()));
     //nslL.system.setIntegrationTimeStep(Elib.toDouble(timeconst.getText()));
     
     nslL.sendCommand("init");
     toggleReach("nslrun");
*/

    }

    public synchronized void transferInput(HandBP bp)
    {/*
    STS sts=(STS)MNS.sts;
    double[] inp=Learn.formInputArray(); 
    for (int i=0;i<inp.length;i++)
	sts.R.setFiringRate(i,inp[i]);
    //sts.R.setFiringRate(inp.length,1.0); // done in simRun clamp to zero
 */
    }


    public synchronized void transferWeights(HandBP bp, double amp)
    {
        /*
        PF pf=(PF)MNS.pf;
        STS sts=(STS)MNS.sts;
        F5mr f5mr=(F5mr)MNS.f5mr;
        //System.out.println("sts -> pf transforming");
       for (int i=0;i<bp.Ydim;i++)
        {
        for (int j=0;j<bp.Xdim;j++)
          {  pf.setSynapse(j,i,amp*bp.w[i][j]);
          //System.out.print(bp.w[i][j]+" ");
          }
        //System.out.println("");
            }
       System.out.println("pf -> f5mr transforming");
        for (int i=0;i<bp.Zdim;i++){
        for (int j=0;j<bp.Ydim;j++){
            f5mr.setSynapse(j,i,amp*bp.W[i][j]);
            System.out.print(f5mr.PF_w.get(j,i)+" ");
        }
        System.out.println("");
        }
        //have to run init to put those in effect
        */
    }

    public void generateData(double stepdeg)
    {
        String[] sey={"BOX","COIN","PENT"};
        double[] sc={1,1,1};
        int sdlev=DLEV;
        DLEV=0;


        int N=(int)(0.5+180.0/stepdeg);
        int M=(int)(0.5+180.0/stepdeg);
        double step=stepdeg*Math.PI/180;

        for (int j=0;j<sey.length;j++)
        {
            String tar=sey[j];

            Point3d q= rHand.root.joint_pos.duplicate();
            Point3d p=new Point3d(0,0,0);

            double RAD=1300;
            setObject(tar);
            double scv;

            for (int k=0;k<=M/2;k++)
            {
                for (int i=0;i<=N/2;i++)
                {
                    if (k==0 && i!=0) continue;
                    if (k==M && i!=0) continue;
                    if (tar.equals("PENT")) scv=Math.random()*0.5+0.75; // x.75 - x1.25
                    else if (tar.equals("BOX"))  scv=Math.random()*1.5+0.5;  // x.5 - x2
                    else scv=Math.random()*0.3+0.85;
                    objlist[curobj].root.scale(scv);
                    double par=step*k-Math.PI/4;
                    double mer=step*i-Math.PI/4;
                    p.x=q.x + RAD*Math.cos(par)*Math.sin(mer);
                    p.y=q.y + RAD*Math.sin(par);
                    p.z=q.z + RAD*Math.cos(par)*Math.cos(mer);

                    objlist[curobj].moveto(p.x,p.y,p.z);
                    System.out.println("\n"+(N*k+i)+"/"+(M*N)+" ) Reaching to:"+p.str());
                    doaction("resetARM");
                    toggleTrace();
                    Mars.ignoreClear=true;
                    Mars.addStar(p.duplicate());
                    toggleTrace();
                    doaction("VISREACH");
                    waitFinish();
                    negativeExample=false;
                    doaction("RECORD");
                    waitFinish();
                    if (rHand.lasterr<5) //then do the negative too.
                    {
                        double ranx=p.x+(Math.random()>0.5?-1:1)*(Math.random()*200+250);
                        double rany=p.y+(Math.random()>0.5?-1:1)*(Math.random()*200+250);
                        double ranz=p.z+(Math.random()>0.5?-1:1)*(Math.random()*200+250);
                        objlist[curobj].moveto(ranx,rany,ranz);
                        negativeExample=true;
                        doaction("RECORD");
                        waitFinish();
                        negativeExample=false;
                    } else System.out.println("Not doing negative example because last reach didn't succeed. Err was:"+rHand.lasterr);
                    objlist[curobj].root.scale(1.0/scv); //back to normal size
                }
                Mars.ignoreClear=false;
                toggleTrace();
                toggleTrace();;
            }

        }
        DLEV=sdlev;
        Mars.ignoreClear=false;
    }

    public void generateRandomData(int count)
    {
        String[] sey={"BOX","COIN","PENT"};
        int sdlev=DLEV;
        DLEV=0;


        System.out.println("Generating "+count+" random reach data...");
        for (int c=0;c<count;c++)
        {
            int j=(int)(Math.random()*sey.length);
            String tar=sey[j];
            Point3d p= new Point3d(Math.random()*2400-1200,
                    Math.random()*2400-1200,
                    Math.random()*1200+ 200);
            VA._add(p, rHand.root.joint_pos);
            setObject(tar);

            objlist[curobj].moveto(p.x,p.y,p.z);
            System.out.println(c+" ) Reaching to:"+p.str());
            doaction("resetARM");
            doaction("VISREACH");
            waitFinish();
            doaction("RECORD");
            waitFinish();
        }
        DLEV=sdlev;
    }

    private boolean _reachDone=true;
    public void waitFinish()
    {
        try {
            while (true)
            {
                Thread.sleep(250);
                if (reachDone()) return;
            }
        } catch (Exception e) {};
    }

    synchronized public void reportFinish(String s)
    {
        _reachDone=true;
        System.out.println("Reach thread finish:"+s);
    }

    synchronized public void reportStart(String s)
    {
        _reachDone=false;
        System.out.println("Reach thread start:"+s);
    }

    synchronized public boolean reachDone()
    {
        if (_reachDone) return true;
        return false;
    }

    public void toggleTrace()
    {
        if (traceon)
        { setTrace(false);
        } else
        { setTrace(true);
            Mars.clearStars(); // create new list
        }
    }
    synchronized public void setTrace(boolean b)
    { traceon=b;
        if (traceon) System.out.println("Trace is on now.");
        else System.out.println("Trace is off now.");
    }

    public Point3d[] computeOpposition()
    { return null; }



    synchronized public void startBabble() {
        //if (mcirc==null) {
        mcirc=new Motor();
        //}
        toggleReach("babble");
    }

    Point3d[] target; //=new Point3d[10];
    synchronized public void toggleReach(String s)
    {
        if (curobj<0)
            return;
        if (!rHand.reachActive())
        {
            if (s.equals("babble"))
                System.out.println("Babble coming...") ;
            else
            {
                if (s.equals("PDFgrasp"))
                    System.out.println("Grasping with pdfg...") ;
                else if (grasps[graspi].equals("PRECISION"))
                    objlist[curobj].precision();
                else if (grasps[graspi].equals("POWER"))
                    objlist[curobj].power();
                else if (grasps[graspi].equals("POWER2"))
                    objlist[curobj].power2();
                else if (grasps[graspi].equals("SIDE"))
                    objlist[curobj].side();
                else if (grasps[graspi].equals("REACHONLY"))
                    objlist[curobj].reachonly();
                else
                    objlist[curobj].natural();
            }

            reach=rHand.doReach(objlist[curobj],s);
        }
        else
        {
            rHand.kill_ifActive();
        }
    }


    synchronized public boolean toggleGesture(String s)
    {
        if (newgest==null)
        {
            newgest=rHand.doGesture(s);
            newgest.start();
        } else
        { newgest.stopGest();
            newgest=null;
        }
        if (newgest==null) return false;
        return true;
    }

    synchronized private boolean toggleEnclose()
    {
        if (gest1==null)
        {
            gest1=new Gesture1(cv,rHand);
            gest1.start();
        }
        else
        {
            gest1.stopGest();
            gest1=null;
        }
        if (gest1==null)
            return false;
        return true;
    }

    synchronized private boolean toggleManualMatch()
    {
        if (match1==null)
        {
            match1=new Match(cv,rHand);
            match1.start();
        }
        else
        {
            match1.stopMatch();
            match1=null;
        }

        if (match1==null)
            return false;
        return true;
    }

    synchronized private boolean toggleRealMatch()
    {
        if (match1==null)
        {
            match1=new Match(cv,featfile,rHand);
            match1.start();
        }
        else
        { match1.stopMatch();
            match1=null;
        }

        if (match1==null) return false;
        return true;
    }


    synchronized private boolean toggleEyeMove(double x,double y,double z)
    {  boolean start=false;
        if (eyemove==null)
        {
            eyemove=new EyeMove(cv,Mars.eye);
            eyemove.dxrot=0;
            eyemove.dyrot=0;
            eyemove.dzrot=0;
            start=true;
        }
        if (eyemove.dxrot!=0 && x!=0)
        {eyemove.dxrot=0;
            System.out.println("cease X rot");
        }
        else if (x!=0)
        {
            System.out.println("Engage X rot");
            eyemove.dxrot=x;
        }
        if (eyemove.dyrot!=0 && y!=0)
        {eyemove.dyrot=0;
            System.out.println("cease Y rot");
        }
        else if (y!=0)
        {
            System.out.println("Engage Y rot");
            eyemove.dyrot=y;
        }
        if (eyemove.dzrot!=0 && z!=0)
        {eyemove.dzrot=0;
            System.out.println("cease Z rot");
        }
        else if (z!=0)
        {
            System.out.println("Engage Z rot");
            eyemove.dzrot=z;
        }

        if (eyemove.dxrot==0 && eyemove.dyrot==0 && eyemove.dzrot==0)
        {
            System.out.println("stopping...");
            eyemove.stopSelf();
            eyemove=null;
        }
        if (start) eyemove.start();
        if (eyemove==null) return false;
        else return true;
    }

    public void exitHV()
    {
        Learn.close();
        System.exit(0);
    }
}

class Gesture1 extends Thread
{
    private HVCanvas cv;
    private Hand hand;
    private boolean stopRequested=false;

    public Gesture1(HVCanvas c, Hand h)
    {
        cv=c;
        hand=h;
    }

    public void run()
    {
        //System.out.println("Here:"+cv+","+hand);
        while (!stopRequested)
        {
            hand.act1(0.6,0.05);
            Mars.project();
            cv.paint(cv.getGraphics());
            try{sleep(50);} catch(InterruptedException e) {}
        }
        stop();
    }

    public void stopGest()
    { stopRequested=true;}
}



/*
<HTML>
<TITLE> Grasp  </TITLE>
<BODY>
Here is it. Have Fun!
<APPLET CODE="HV.class"  WIDTH=450 HEIGHT=550 ALIGN=LEFT HSPACE=20
HREF="http://www-hbp.usc.edu">
<PARAM NAME=IMAGE1 VALUE="test.jpg">
<PARAM NAME=IMAGE2 VALUE="image2.jpg">
<PARAM NAME=TARGET VALUE="http://www-hbp.usc.edu">
</APPLET>
</BODY>
</HTML>
*/
/*
 *
 * Erhan Oztop, 2000-2002  <br>
 * Source code by Erhan Oztop (erhan@atr.co.jp) <br>
 * Copyright August 2002 under <br>
 * University of Southern California Ph.D. publication copyright <br>
 */
 
