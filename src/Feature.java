 
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

class Feature
{
 static final int type_IMAGE=0;
 static final int type_RAWIMAGE=1;
 static final int type_MODEL=2;
 static final int type_MANUAL=3;

 static final int ftype_PALM=0;
 static final int ftype_LIMBS=1;
 static final int ftype_ALL=2;
 int type;
 int ftype;
 double scale=1;
 Image im=null;
 Hand   h=null;

 double BOK=1;

 ImageProcess ip=null;
 double [][][] F;
 int[] Flen;
 private void createF()
 {
  F=new double[3][50][2];
  Flen=new int[3];
  Flen[0]=0; Flen[1]=0; Flen[2]=0;
 }
 public Feature(String fname)
 {
  type=type_IMAGE;
  createF();
  //ip=new ImageProcess(fname,100,100,100); 
   System.out.println("coming----");
  fillImex(fname);  
  Imex2F();
  for (int f=0;f<3;f++)
  {
  for (int i=0;i<Flen[f];i++)
   System.out.println("F["+i+"]:"+F[f][i][0]+"  conf:"+F[f][i][1]);
   System.out.println("----");
  }
 }

 public Feature()
 {
  type=type_MANUAL;
  createF();
 }
 public Feature(Hand h)
 { 
  type=type_MODEL;
  this.h=h;
  createF();
 } 

 public void extract(int f)
 {
  if (type==type_IMAGE) extract_from_image(f);
  else if (type==type_RAWIMAGE) extract_from_rawimage(f);
  if (type==type_MODEL) extract_from_model(f);
  else if (type==type_MANUAL) extract_from_manual(f);
 }


 public double getAngle()
 {
  if (type==type_IMAGE)    
   {
    double meanx=0; 
    double meany=0;
    int c=0;
    for (int k=1;k<=15;k++)
    { 
      if (Imex[k][2]!=0)
       { c++; 
         meanx+=scale*Imex[k][0];
         meany+=scale*Imex[k][1];
       }
     }  
     meanx/=c;
     meany/=c;
     System.out.println("meanx,meany:"+meanx+","+meany);
     double len=VA.dist(meanx,meany,0,0);
     return VA.cosSin(meanx/len,meany/len);
   }
   else if (type==type_MODEL)
    {
    //i do itreturn (hand.wristx.joint_pos,hand.middle.joint_pos);
    }
    else return 0;
    return 0;
 }
 public double difference(Feature other, int f)
 {
  double sum=0;
  int c=0;
  //System.out.println("------> Calculating difference of feature:"+f+" len:"+Flen[f]);
  for (int i=0;i<Flen[f];i++)
   { if (F[f][i][1]>=0 && other.F[f][i][1]>=0) 
       {
         //sum+=Elib.sqr(F[f][i][0]-other.F[f][i][0])*F[f][i][1]*other.F[f][i][1];
         sum+=Elib.sqr(F[f][i][0]-other.F[f][i][0]);
         c++;
        
/*
         System.out.println(Elib.snice(F[f][i][0],1e5,7)+" X "+
                            Elib.snice(other.F[f][i][0],1e5,7));
*/
       }
   }
  //System.out.println("***********************");
  return (sum);
 }

 public String[] Imex_names=new String[30];
 public double[][] Imex=new double[30][3];
 public int Imexc=0;

 private void fillImex(String fn)
 {
   String s;
   int i=0;
   int linec=0;
   Imexc=0;
   try {
   DataInputStream in = openfileREAD(fn);
   if (in==null) return ;
   linec=0;
   while (null!=(s=in.readLine()))
   {
    linec++;
    System.out.println("line:"+linec+" : "+s);
    if (s.equals("")) continue;
    if (s.charAt(0) == '#') continue;
    StringTokenizer st= new StringTokenizer(s," ");
    int tc=0;
    String u0 = st.nextToken();
    String u1 = st.nextToken();
    String u2 = st.nextToken();
    String u3 = st.nextToken();
    Imex_names[i]=u0; 
    if (i==0)  Imex[i][0]=Elib.toDouble(u1); //this is orient., ang
    Imex[i][0]=Elib.toDouble(u1);
    Imex[i][1]=Elib.toDouble(u2);
    Imex[i][2]=Elib.toDouble(u3);
    i++;
  }
  Imexc=i;
  in.close();
  } catch (IOException e)
   { System.err.println("BP.readPattern() : EXCEPTION "+e);
   }

   System.out.println(linec+" read.");
   // You can do a lot better than this moruk!
   for ( i=0;i<Imexc;i++)
    { 
      if (Imex[i][2]==0)
       { if (i==0) System.out.println("cannot do without WRIST!!");
         else
         {
          if (i-3>0) { Imex[i][0]=Imex[i-3][0];
                       Imex[i][1]=Imex[i-3][1];
                       Imex[i][2]=Imex[i-3][2];
                       System.out.println("Tried to correct:"+Imex_names[i]);
                     }
          else 
          if (i+3<Imexc) { Imex[i][0]=Imex[i+3][0];
                           Imex[i][1]=Imex[i+3][1];
                           Imex[i][2]=Imex[i+3][2];
                       System.out.println("Tried to correct:"+Imex_names[i]);
                         }
         }
      }

    }
 }

 public void setScale(double sc)
 {
  scale=sc;
 }
 private void drawThickLine(Graphics g,int x0,int y0,int x1,int y1)
 {
  g.drawLine(x0,y0,x1,y1);
  g.drawLine(x0+1,y0,x1+1,y1);
  g.drawLine(x0,y0+1,x1,y1+1);

 }

 private void drawMark(Graphics g,int x0,int y0)
 {
  g.fillRect(x0-2,y0-2,5,5);
 }


 public void drawImex(int midx,int midy,Graphics g)
 { int x2,y2,x0,x1,y0,y1;

  x0=(int)(0.5+scale*Imex[1][0]);
  y0=(int)(0.5+scale*Imex[1][1]);
  x1=(int)(0.5+scale*Imex[10][0]);
  y1=(int)(0.5+scale*Imex[10][1]);
  g.setColor(HV.pal.C[2]);   //red

  drawThickLine(g,x0+midx,midy-y0,x1+midx,midy-y1);  //pinkycorner to indexk.
  drawThickLine(g,x1+midx,midy-y1,midx,midy);   //to wrist
  drawThickLine(g,midx,midy,x0+midx,midy-y0);   //to back to pincky corner

  g.setColor(Color.red);
  drawMark(g,midx,midy);

  for (int k=0;k<5;k++)
  { x0=(int)(0.5+scale*Imex[2+k*3][0]);
    y0=(int)(0.5+scale*Imex[2+k*3][1]);
    x1=(int)(0.5+scale*Imex[2+k*3+1][0]);
    y1=(int)(0.5+scale*Imex[2+k*3+1][1]);
    x2=(int)(0.5+scale*Imex[2+k*3-1][0]);
    y2=(int)(0.5+scale*Imex[2+k*3-1][1]);
    if (k==0) g.setColor(HV.pal.C[12]);
    else if (k==1) g.setColor(HV.pal.C[11]);
    else if (k==2) g.setColor(HV.pal.C[10]);
    else if (k==3) g.setColor(HV.pal.C[5]);
    else if (k==4) g.setColor(HV.pal.C[6]);              //magenta
 
    drawMark(g,x0+midx,midy-y0);
    drawMark(g,x1+midx,midy-y1);
    drawMark(g,x2+midx,midy-y2);
  }


 }
  
 int last=-1;
 public int get(String s)
 {
/*
  if (last!=-1) 
    if (s.equals(Imex_names[last])) return last;
*/

  for (int i=0;i<Imexc;i++)
   if (s.equals(Imex_names[i]))
     {last=i; return i;}

  System.err.println("No label as:"+s);
  last=-1;
  return -1;
 }

 public Point3d pget(String s)
 {
  int k=get(s);
  if (k==-1) return null;
  //                    X          Y         confidence 
  return new Point3d(Imex[k][0],Imex[k][1],Imex[k][2]);
 }

 public void Imex2F()
 { int linec;
   Point3d pivot,ix,joint,tip;
  //----------------------------------------------------------------
    int i=0; int f=ftype_PALM;
    F[f][i++][0]=scale*0; F[f][i-1][1]=Imex[0][2];
    F[f][i++][0]=scale*0; F[f][i-1][1]=Imex[0][2];

    pivot=new Point3d(0,0,0);
    ix=pget("index0");

    Point3d w=new Point3d(0,0,0);
    Point3d pinky=pget("pinky0");  //this is the coord.
    Point3d index=pget("index0");  //this is the coord.

    F[f][i][0]=BOK*VA.acosD(pinky,w,index,w);
    F[f][i][1]=1; //pinky.z*index.z; 
    i++;
    F[f][i][0]=BOK*VA.acosD(w,pinky,index,pinky);
    F[f][i][1]=1; //pinky.z*index.z; 
    i++;


    F[f][i][0]=BOK*VA.acosD(index, w, zero3d,vertical3d);
    F[f][i][1]=1; //index.z*index.z; 
    i++;
    
    F[f][i][0]=BOK*VA.acosD(index,w, zero3d,horizontal3d);
    F[f][i][1]=1; //index.z*index.z; 
    i++;

    F[f][i][0]=BOK*VA.acosD(pinky, w, zero3d,vertical3d);
    F[f][i][1]=1; //index.z*pinky.z;
    i++;

    F[f][i][0]=BOK*VA.acosD(pinky,w, zero3d,horizontal3d);
    F[f][i][1]=1; //pinky.z*index.z;
    i++;
    Flen[f]=i;
  dumpF(f); 

  //----------------------------------------------------------------
  f=ftype_LIMBS;
 System.out.println("IMAGE cos:"+(Elib.nice( F[f][i-2][0]/10,1e4))+","+(Elib.nice( F[f][i-1][0]/10,1e4)));
  String[] nm=new String[5];
  nm[0]="pinky";
  nm[1]="ring";
  nm[2]="middle";
  nm[3]="index";
  nm[4]="thumb";

  i=0;
  for (int k=0;k<5;k++)
    {
     joint=pget(nm[k]+"1");
     tip=pget(nm[k]+"2");
     F[f][i][0]=BOK*VA.acosD(joint, pivot, ix, pivot);
     F[f][i][1]=1;
     i++;
     F[f][i][0]=BOK*VA.acosD(tip, pivot, ix, pivot);
     F[f][i][1]=1;
     i++;
     if (k==4)
      {
       F[f][i][0]=BOK*VA.acosD(pget(nm[k]+"0"), pivot, ix, pivot);
       F[f][i][1]=1;
       i++;
      }

    }


   Flen[f]=i;
  System.out.println("********* IMAGE Flen:"+Flen[0]+","+Flen[1]+","+Flen[2]);
  dumpF(f); 
 

  //----------------------------------------------------------------
   f=ftype_ALL;
   i=0;
    F[f][i++][0]=scale*0; F[f][i-1][1]=Imex[0][2];
    F[f][i++][0]=scale*0; F[f][i-1][1]=Imex[0][2];

    F[f][i][0]=BOK*VA.acosD(pinky,w,index,w);
    F[f][i][1]=1; //pinky.z*index.z;
    i++;
    F[f][i][0]=BOK*VA.acosD(w,pinky,index,pinky);
    F[f][i][1]=1; //pinky.z*index.z;
    i++;


/*
    F[f][i][0]=BOK*VA.acosD(index, w, zero3d,vertical3d);
    F[f][i][1]=1; //index.z*index.z;
    i++;

    F[f][i][0]=BOK*VA.acosD(index,w, zero3d,horizontal3d);
    F[f][i][1]=1; //index.z*index.z;
    i++;

    F[f][i][0]=BOK*VA.acosD(pinky, w, zero3d,vertical3d);
    F[f][i][1]=1; //index.z*pinky.z;
    i++;

    F[f][i][0]=BOK*VA.acosD(pinky,w, zero3d,horizontal3d);
    F[f][i][1]=1; //pinky.z*index.z;
    i++;
*/
/*
   for (int k=0;k<5;k++)
    {

     joint=pget(nm[k]+"1");
     tip=pget(nm[k]+"2");
     F[f][i][0]=BOK*VA.acosD(joint, pivot, ix, pivot);
     F[f][i][1]=1;
     i++;
     F[f][i][0]=BOK*VA.acosD(tip, pivot, ix, pivot);
     F[f][i][1]=1;
     i++;
     if (k==4) 
      {
       F[f][i][0]=BOK*VA.acosD(pget(nm[k]+"0"), pivot, ix, pivot);
       F[f][i][1]=1;
       i++;
      }

    }
*/
    for (int k=0;k<5;k++)
    {

     joint=pget(nm[k]+"1");
     tip=pget(nm[k]+"2");
     if (k!=4) {
     F[f][i][0]=scale*(joint.x-w.x); F[f][i][1]=1; i++;
     F[f][i][0]=scale*(joint.y-w.y); F[f][i][1]=1; i++;
     }
     F[f][i][0]=scale*(tip.x-w.x); F[f][i][1]=1; i++;
     F[f][i][0]=scale*(tip.y-w.y); F[f][i][1]=1; i++;
     if (k==4)
      {
       Point3d th=pget(nm[k]+"0");
       F[f][i][0]=scale*(th.x-w.x); F[f][i][1]=1; i++;
       F[f][i][0]=scale*(th.y-w.y); F[f][i][1]=1; i++;
      }
    }



   //finger-thumb tip dist.
   F[f][i++][0]=3*scale*Elib.dist(Imex[get("thumb2")][0],
                                  Imex[get("thumb2")][1],
                                  Imex[get("index2")][0],
                                  Imex[get("index2")][1]);
   F[f][i++][0]=2*scale*Elib.dist(Imex[get("thumb1")][0],
                                  Imex[get("thumb1")][1],
                                  Imex[get("index1")][0],
                                  Imex[get("index1")][1]);

   Flen[f]=i;
  System.out.println("********* IMAGE Flen:"+Flen[0]+","+Flen[1]+","+Flen[2]);
  dumpF(f);
 
}      

private void dumpF(int k)
 {
   { System.out.println("F["+k+"]");
     for (int i=0;i<Flen[k];i++)
      {
       //System.out.println("   "+i+") "+Elib.nice(F[k][i][0],1e4)+"  conf:"+Elib.nice(F[k][i][1],1e4) );
       System.out.println("   "+i+") "+F[k][i][0]+"  conf:"+F[k][i][1]);
      }
    }
  }

     
static public DataInputStream openfileREAD(String fn) throws IOException
 { DataInputStream in = new DataInputStream(new FileInputStream(fn));
     return in;
 }

static public DataOutputStream openfileWRITE(String fn) throws IOException
 {
   DataOutputStream out = new DataOutputStream(new FileOutputStream(fn));
     return out;
 }


 private void extract_from_manual(int f)
 {
  System.out.println("------> Extracting manual feature:"+f);
  int wx=767;
  int wy=361;
  int i;

  i=0;
  if (f==ftype_PALM )
  {
   F[f][i++][0]=wx-wx;
   F[f][i++][0]=wy-wy;
   F[f][i++][0]=455-wx;
   F[f][i++][0]=217-wy;
   F[f][i++][0]=449-wx;
   F[f][i++][0]=246-wy;
   F[f][i++][0]=5*VA.dist(449,246, 455,217);
   Flen[f]=i;
   for (i=0;i<Flen[f];i++) {F[f][i][1]=1.0;}  //full conf.

  } else
  if (f==ftype_LIMBS || f==ftype_ALL)
  {

   F[f][i++][0]=358-wx;
   F[f][i++][0]=220-wy;
   F[f][i++][0]=215-wx;
   F[f][i++][0]=208-wy;

   F[f][i++][0]=271-wx;
   F[f][i++][0]=279-wy;
   F[f][i++][0]=116-wx;
   F[f][i++][0]=405-wy;


   F[f][i++][0]=251-wx;
   F[f][i++][0]=336-wy;
   F[f][i++][0]=167-wx;
   F[f][i++][0]=550-wy;

   F[f][i++][0]=274-wx;
   F[f][i++][0]=388-wy;
   F[f][i++][0]=256-wx;
   F[f][i++][0]=585-wy;

   F[f][i++][0]=511-wx;
   F[f][i++][0]=501-wy;
   F[f][i++][0]=307-wx;
   F[f][i++][0]=577-wy;
  Flen[f]=i;
   for (i=0;i<Flen[f];i++) {F[f][i][1]=1.0;}  //full conf.

  } else
  if (f==ftype_ALL)
  {
   //implemented this as OR of the previous can use different scheme
  } else System.err.println("NO SUCH FEATURE TYPE:"+f+"!!");

//  for (i=0;i<Flen[f];i++) { F[f][i][0]-=0; }
  double sc=-0.2;
  for (i=0;i<Flen[f];i++) { F[f][i][0]*=sc; }
 }

 
 private void extract_from_image(int f)
 { 
   //fillImex();
   System.out.println("doing imex2F.");
   Imex2F();
 }
 private void extract_from_rawimage(int f)
 { }
 

 private Point vertical=new Point(0,1);
 private Point horizontal=new Point(1,0);
 private Point zero=new Point(0,0);
 private Point3d vertical3d=new Point3d(0,1,0);
 private Point3d horizontal3d=new Point3d(1,0,0);
 private Point3d zero3d=new Point3d(0,0,0);
 // extract feature f from model h
 private void extract_from_model(int f)
 { 
   Point pivot,ix,corner,joint,tip;
  //System.out.println("------> Extracting feature:"+f);
  
  
  pivot=h.wristx.limb_pos2d;
     ix=h.index.joint_pos2d;

  int wx=h.wristx.limb_pos2d.x;
  int wy=h.wristx.limb_pos2d.y;
  int i;

  i=0;
  if (f==ftype_PALM )
  {
   F[f][i++][0]=wx;
   F[f][i++][0]=wy;
    

   F[f][i++][0]=BOK*VA.acosD(h.pinky.joint_pos2d, h.wristx.joint_pos2d,
                         h.index.joint_pos2d, h.wristx.joint_pos2d);
   F[f][i++][0]=BOK*VA.acosD(h.wristx.joint_pos2d, h.pinky.limb_pos2d,
                         h.index.joint_pos2d, h.pinky.limb_pos2d);


   F[f][i++][0]=BOK*VA.acosD(h.index.joint_pos2d, h.wristx.joint_pos2d,
                             zero,vertical);
   F[f][i++][0]=BOK*VA.acosD(h.index.joint_pos2d, h.wristx.joint_pos2d,
                             zero,horizontal);
   F[f][i++][0]=BOK*VA.acosD(h.pinky.joint_pos2d, h.wristx.joint_pos2d,
                             zero,vertical);
   F[f][i++][0]=BOK*VA.acosD(h.pinky.joint_pos2d, h.wristx.joint_pos2d,
                             zero,horizontal);

   Flen[f]=i;
   for (i=0;i<Flen[f];i++) {F[f][i][1]=1.0;}  //full conf.

  } else
  if (f==ftype_LIMBS)
  {
     Segment fing=null;
     Segment w=h.wristx;
     for (int k=0;k<5;k++)
     { 
      if (k==0) { fing=h.pinky;}
      else if (k==1) { fing=h.ring;}
      else if (k==2) { fing=h.middle;}
      else if (k==3) { fing=h.index;}
      else if (k==4) { fing=h.thumb;}

     joint=fing.limb_pos2d;
     tip=fing.child[0].limb_pos2d;
     F[f][i++][0]=BOK*VA.acosD(joint,pivot,ix,pivot);
     F[f][i++][0]=BOK*VA.acosD(tip,  pivot,ix,pivot);
     if (k==4)
      {
       F[f][i++][0]=BOK*VA.acosD(h.thumb.joint_pos2d, pivot, ix, pivot);
      }

   }

   Flen[f]=i;
   for (i=0;i<Flen[f];i++) {F[f][i][1]=1.0;}  //full conf.
  } else
  if (f==ftype_ALL)
  {
   i=0;
   F[f][i++][0]=wx;
   F[f][i++][0]=wy;


   F[f][i++][0]=BOK*VA.acosD(h.pinky.joint_pos2d, h.wristx.joint_pos2d,
                         h.index.joint_pos2d, h.wristx.joint_pos2d);
   F[f][i++][0]=BOK*VA.acosD(h.wristx.joint_pos2d, h.pinky.limb_pos2d,
                         h.index.joint_pos2d, h.pinky.limb_pos2d);

/*
   F[f][i++][0]=BOK*VA.acosD(h.index.joint_pos2d, h.wristx.joint_pos2d,
                             zero,vertical);
   F[f][i++][0]=BOK*VA.acosD(h.index.joint_pos2d, h.wristx.joint_pos2d,
                             zero,horizontal);
   F[f][i++][0]=BOK*VA.acosD(h.pinky.joint_pos2d, h.wristx.joint_pos2d,
                             zero,vertical);
   F[f][i++][0]=BOK*VA.acosD(h.pinky.joint_pos2d, h.wristx.joint_pos2d,
                             zero,horizontal);
*/

     Segment fing=null;
     Segment w=h.wristx;
     corner=w.joint_pos2d;
/*
     for (int k=0;k<5;k++)
     {
      if (k==0) { fing=h.pinky;}
      else if (k==1) { fing=h.ring;}
      else if (k==2) { fing=h.middle;}
      else if (k==3) { fing=h.index;}
      else if (k==4) { fing=h.thumb;}

     joint=fing.limb_pos2d;
     tip=fing.child[0].limb_pos2d;
     F[f][i++][0]=BOK*VA.acosD(joint,pivot,ix,pivot);
     F[f][i++][0]=BOK*VA.acosD(tip,  pivot,ix,pivot);
    if (k==4)
      {
       F[f][i++][0]=BOK*VA.acosD(h.thumb.joint_pos2d, pivot, ix, pivot);
      }

   }
*/

    for (int k=0;k<5;k++)
    {
      if (k==0) { fing=h.pinky;}
      else if (k==1) { fing=h.ring;}
      else if (k==2) { fing=h.middle;}
      else if (k==3) { fing=h.index;}
      else if (k==4) { fing=h.thumb;}

     joint=fing.limb_pos2d;
     tip=fing.child[0].limb_pos2d;
     if (k!=4)
     {
     F[f][i++][0]=joint.x-wx;
     F[f][i++][0]=joint.y-wy;
     }
     F[f][i++][0]=tip.x-wx;
     F[f][i++][0]=tip.y-wy;
     if (k==4)
      {
       Point th=h.thumb.joint_pos2d;
       F[f][i++][0]=th.x-wx;
       F[f][i++][0]=th.y-wy;
      }
    }



  // this is thumb-index tip distance
   F[f][i++][0]=3*VA.dist(h.thumb.child[0].child[0].limb_pos2d,
                          h.index.child[0].limb_pos2d);
   F[f][i++][0]=2*VA.dist(h.thumb.child[0].limb_pos2d, h.index.limb_pos2d);

   Flen[f]=i;
   for (i=0;i<Flen[f];i++) {F[f][i][1]=1.0;}  //full conf.
  } else System.err.println("NO SUCH FEATURE TYPE:"+f+"!!");

  
  //System.out.println("********* MODEL Flen:"+Flen[0]+","+Flen[1]+","+Flen[2]);
  //dumpF();
 }
}
/*
 *
 * Erhan Oztop, 2000-2002  <br>
 * Source code by Erhan Oztop (erhan@atr.co.jp) <br>
 * Copyright August 2002 under <br>
 * University of Southern California Ph.D. publication copyright <br>
 */
 
