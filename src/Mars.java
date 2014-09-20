 
import java.awt.*;
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

public class Mars
{
    static public boolean ignoreClear=false;
 static public int midx,midy,wi,he;
 static public final int MAXobjects=100;

 static public String[] objname=new String[MAXobjects];  //object
 static public Object3d[] obj=new Object3d[MAXobjects];  //object
 static int objc=0;
 static Eye eye=null;
 
 static public int debug=0;
 static public int addObject(Object3d o)
 {
  return addObject(null,o);
 }

 static public int addObject(String name,Object3d o)
 {
  if (o==null) { System.err.println("Mars: object list is null!!"); return -1; }
  if (objc>=obj.length) { System.err.println("Mars: full!!"); return -1; }
  if (name==null) objname[objc]=o.root.label;
  else objname[objc]=name;
  obj[objc++]=o;
  return objc-1;
 }

 static public Object3d getObject(String name)
 {
  for (int i=0;i<objc;i++)
    if (objname[i].equals(name)) return obj[i];
  System.out.println("Cannot find object named:"+name);
  return null;
 }
 
 static public void drawSolid(Graphics g,int opt)
 { 
   //System.out.println("Drawing solid...");
   for (int i=0;i<objc;i++)
    if (!obj[i].noshow) obj[i].sortPlanes(opt);

   sortObjects();
   for (int i=0;i<objc;i++)
     if (!obj[i].noshow) obj[i].drawSolid(g);
 }


 static public void drawSkel(Graphics g)
 {
   //System.out.println("Drawing Skeleton...");
   for (int i=0;i<objc;i++)
     if (!obj[i].noshow) obj[i].drawSkel(g);
 } 

 static public void drawWire(Graphics g)
 { 
   //System.out.println("Drawing Wireframe...");
   for (int i=0;i<objc;i++)
     if (!obj[i].noshow) obj[i].drawWire(g);
 }


 static public void setEye(Eye eye)
 {Mars.eye=eye;}

 static void sortObjects()
 {
  for (int i=0;i<objc;i++) 
    if (!obj[i].noshow) obj[i].updateObjectDepth();

  for (int i=0;i<objc-1;i++)
   {
    if (obj[i].noshow) continue;
    for (int j=i+1;j<objc;j++)
     {if (obj[j].noshow) continue;
      if (obj[i].objdepth<obj[j].objdepth)
        { Object3d t=obj[i]; obj[i]=obj[j]; obj[j]=t;
          String ss=objname[i]; objname[i]=objname[j]; objname[j]=ss;
        }
     }
   }
  }

 static void project()
 {
  for (int i=0;i<objc;i++)
    if (!obj[i].noshow) obj[i].project(eye);
 }

 static void setCube(double len)
 {
  len/=2;
  cube=new Point3d[8];
  cube2d=new Point[8];
  
  cube[0]=new Point3d(-len, len,-len); 
  cube[1]=new Point3d(-len,-len,-len); 
  cube[2]=new Point3d( len,-len,-len); 
  cube[3]=new Point3d( len, len,-len); 
  cube[4]=new Point3d(-len, len, len); 
  cube[5]=new Point3d(-len,-len, len); 
  cube[6]=new Point3d( len,-len, len); 
  cube[7]=new Point3d( len, len, len); 
  for (int i=0;i<8;i++) cube2d[i]=new Point();  
 }

    static int drawCube=Resource.getInt("drawCube");
 static public void drawCube(Graphics g)
 {  
   if (drawCube==0) return;
   for (int i=0;i<8;i++) eye._project(cube[i],cube2d[i]); 
   for (int i=0;i<4;i++) 
    {xx[i]=midx+cube2d[i].x;
     yy[i]=midy-cube2d[i].y;
    }

   g.setColor(Color.gray);
   g.drawPolygon(xx,yy,4);  
      for (int i=4;i<8;i++)  
    {xx2[i-4]=midx+cube2d[i].x;
     yy2[i-4]=midy-cube2d[i].y;
    }
   g.drawPolygon(xx2,yy2,4);
   for (int i=0;i<4;i++) g.drawLine(xx[i],yy[i],xx2[i],yy2[i]);
 }

 static public Point3d[] getStars()
 {
  Vector v=stars;
  int n=v.size();
  Point3d[] L=new Point3d[n];
  n=0;
  Enumeration e=v.elements();
  while (e.hasMoreElements())
  {
   Point3d p=(Point3d)e.nextElement();
   L[n++]=p.duplicate();
  }
  System.out.println("returnin stars with "+n+" elements");
  for (int i=0;i<n;i++) System.out.println(L[i].str());
  return L;
 }

 static public void drawStars(Graphics g)
 {
  drawComets(g);
  if (stars==null) return;
  Enumeration e=stars.elements();
  while (e.hasMoreElements())
  {
   Point3d p=(Point3d)e.nextElement();
   eye._project(p,temp);
   g.setColor(Color.red);
   //g.drawLine(midx+temp.x, midy-temp.y,midx+temp.x, midy-temp.y); 
   g.drawRect(-2+midx+temp.x, -2+midy-temp.y,4,4); 
  } 
  /*
  e=aperture.elements();
  int lastx=0,lasty=-10000;
  while (e.hasMoreElements())
  {
   Point3d p=(Point3d)e.nextElement();
   eye._project(p,temp);
   g.setColor(Color.orange);
 //g.drawLine(midx+temp.x, midy-temp.y,midx+temp.x, midy-temp.y);
   if (lasty!=-10000) g.drawLine(midx+lastx, midy-lasty,midx+temp.x, midy-temp.y);
   lastx=temp.x; lasty=temp.y;
  }

  e=velocity.elements();
  lastx=0;lasty=-10000;
  while (e.hasMoreElements())
  {
   Point3d p=(Point3d)e.nextElement();
   eye._project(p,temp);
   g.setColor(Color.green);
   //g.drawLine(midx+temp.x, midy-temp.y,midx+temp.x, midy-temp.y);
   if (lasty!=-10000) g.drawLine(midx+lastx, midy-lasty,midx+temp.x, midy-temp.y);
    lastx=temp.x; lasty=temp.y;

  }
  */

 }
 static private Point temp0=new Point();
 static private Point temp1=new Point();
 static public void drawComets(Graphics g)
 {
  if (comets==null) return;
  Enumeration e=comets.elements();
  while (e.hasMoreElements())
  {
   Line ll=(Line)e.nextElement();
   eye._project(ll.P[0],temp0);
   eye._project(ll.P[1],temp1);
   g.setColor(HV.pal.C[ll.line_color]);
   g.drawLine(midx+temp0.x, midy-temp0.y,midx+temp1.x, midy-temp1.y);
  }
 }

 static public void velocityProfile()
 {
  Point3d prev=null,cur=null;
  System.out.println("* velocity profile *");
  if (stars==null) return;
  int k=0;
  Enumeration e=stars.elements();
  if (e.hasMoreElements()) prev=(Point3d)e.nextElement();
  while (e.hasMoreElements())
  {
   cur=(Point3d)e.nextElement();
   double dis=VA.dist(prev,cur);
   prev=cur;
   System.out.println(dis);
   k++;
  }
 }
 static public void addStar()
 {
  addStar(HV.self.rHand.wristx.joint_pos);
 }

 static Point3d prev=null,cur=null;
 static int pos=0;
 static public void addStar(Point3d p)
 {double dis;

 starc++;
  cur=p.duplicate();
  stars.addElement(cur);
  if (ignoreClear) return; // special case don't plot. See HV.generataData
  if (prev!=null) 
  { dis=VA.dist(cur,prev); 
    dis/=HV.self.rHand.reach_deltat;
    velocity.addElement(new Point3d(-750+pos*10,-750+dis*0.4,-750));
  }

  dis=VA.dist(HV.self.rHand.thumb.child[0].child[0].child[0].limb_pos,
                     HV.self.rHand.index.child[0].limb_pos);
  aperture.addElement(new Point3d(-750+pos*10,-750+dis*2,-750));
  pos++;

  prev=cur;
  //System.out.println("Added:"+p.str());
 }

 static public void addComet(Point3d p0,Point3d p1,int col)
 {
  comets.addElement(new Line(p0.duplicate(), p1.duplicate(),col));
  
 }

static public void clearComets()
 {
     if (ignoreClear) return;
  comets=new Vector(40);
 }


    static public int getStarCount() {
	return starc;
    }

 static public void clearStars()
 {
     if (ignoreClear) return;
  stars=new Vector(40);
  aperture=new Vector(40);
  velocity=new Vector(40);
  prev=null; cur=null;
  pos=0;
  starc=0;
 }

 
 static Point3d[] cube=null;
 static Point[] cube2d=null;
 static int[] xx=new int[8]; 
 static int[] yy=new int[8]; 
 static int[] xx2=new int[8]; 
 static int[] yy2=new int[8]; 
 static private Point temp=new Point();
 static Vector comets=null;   
 static Vector stars=null;   //currently holds the trajectory points
 static Vector aperture=null;  // currently holds the velocity profile
 static Vector velocity=null;  // currently holds the velocity profile
    static int starc=0;
}
/*
 *
 * Erhan Oztop, 2000-2002  <br>
 * Source code by Erhan Oztop (erhan@atr.co.jp) <br>
 * Copyright August 2002 under <br>
 * University of Southern California Ph.D. publication copyright <br>
 */
 
