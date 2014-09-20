 
import java.io.*;
import java.net.*;
import java.applet.*;
import java.util.*;
import java.awt.*;
import java.lang.*;
import java.util.Vector;
import java.util.Enumeration;
import java.awt.image.*;
/**
 *
 * @author Erhan Oztop, 2001-2002 <br>
 * <br>
 * Source code by Erhan Oztop (erhan@atr.co.jp) <br>
 * Copyright August 2002 via <br>
 * University of Southern California Ph.D. publication copyright <br>
 */


class ImageProcess extends Frame
{
 static ImageProcess top=null;
 IPcanvas cv;
 Image   im; 
 double VR,VG,VB;
 int wi=0,he=0,midx=0,midy=0 ;
 public boolean im_done=false;
 Panel upper;
 boolean splitdone=false;

  public PixelGrabber pick=null;
  public int[] pix=null;
  public short[] R,G,B=null;
  public short[] resR,resG,resB=null;
  public int sz=0;


 public double[] askbuf;
 public boolean networkset=false;
 public BP bp=null;
 public int Xdim,Ydim, Zdim;
 public double learningrate    =   0.1;
 public double momentum        =   0.9;
 public double learningincrease=   0.01;
 public double learningdecrease=   0.1;
 
 public Vector pats=null;
 public int patc=0;
 public boolean recording=false;

 public Panel cp;
 public Button colbut;
 public int pickR,pickG,pickB;
 public int pickmode=0;
 



 static public final int COLORSEP=30; //each color is at least this much diff.

 String[] names;
 int[] mask;
 int[] markerR,markerG,markerB;
 Color[] markerC;
 double[][] marker;
 int markerc=0;
 int dstart;
 public ImageProcess(String s)
 { this(s,30,30,30);
 }
 String def_pattern_file="default.pat";
 String def_picture_file="default.jpg";
 TextField picture,pattern,weight,input,hidden,output;
 public void setupButtons()
 {
  setTitle("Hand tracker Jan2000");
    Button lastb;
    setLayout(new BorderLayout());
    cv=new IPcanvas(this);
    cv.setBackground(Color.white);
    add("Center",cv);
    Panel p1=new Panel();
    Panel p2=new Panel();
    cp=new Panel();
    p1.setLayout(new GridLayout(4,4));
    p2.setLayout(new GridLayout(2,8));
    cp.setLayout(new GridLayout(dstart+1,1));
    for (int i=0;i<dstart;i++) 
      {
       Button cb=new Button(names[i]);
       cb.setBackground(markerC[i]);
       cb.setForeground(Color.gray);
       cp.add(cb);
      }
    cp.add(colbut=new Button("     "));
    colbut.setBackground(Color.red);

    p2.add(new Label("Image to load:",Label.LEFT));
    p2.add(new Label("Pattern File:",Label.LEFT));
    p2.add(new Label("Weight File:",Label.LEFT));
    p2.add(new Label("Input Dim:",Label.LEFT));
    p2.add(new Label("Hidden Dim:",Label.LEFT));
    p2.add(new Label("Output Dim:",Label.LEFT));
    picture=new TextField(def_picture_file,20);
    p2.add(picture);
    pattern=new TextField(def_pattern_file,12);
    p2.add(pattern);
    weight=new TextField("none.wgt",12);
    p2.add(weight);

    input=new TextField(Xdim+"",5);
    p2.add(input);


    hidden=new TextField(Ydim+"",5);
    p2.add(hidden);

    output=new TextField(Zdim+"",5);
    p2.add(output);

    p1.add(new Button("Load"));
    p1.add(new Button("Save"));
    p1.add(new Button("Clear"));
    p1.add(new Button("TEST"));
    p1.add(new Button("MEANFILT"));
    p1.add(new Button("MEANBRFILT"));
    p1.add(new Button("MAXFILT"));
    p1.add(new Button("MEDIANFILT"));
    p1.add(new Button("BP_FIND"));
    p1.add(new Button("HSV_FIND"));
    p1.add(new Button("SaveExtract"));
    p1.add(new Button("Palette"));
    p1.add(lastb=new Button("Create Net from Pattern"));
    lastb.setForeground(Color.white); lastb.setBackground(Color.blue);
    p1.add(lastb=new Button("Create Net from Weight"));
    lastb.setForeground(Color.white); lastb.setBackground(Color.blue);
    p1.add(lastb=new Button("Train Network"));
    lastb.setForeground(Color.white); lastb.setBackground(Color.blue);
    p1.add(lastb=new Button("Start Recording"));
    lastb.setForeground(Color.white); lastb.setBackground(Color.blue);
    p1.add(lastb=new Button("Stop Recording"));
    lastb.setForeground(Color.white); lastb.setBackground(Color.blue);
    p1.add(lastb=new Button("Save Recorded"));
    lastb.setForeground(Color.white); lastb.setBackground(Color.blue);
    p1.add(lastb=new Button("Save Weights"));
    lastb.setForeground(Color.white); lastb.setBackground(Color.blue);
    p1.add(lastb=new Button("Clear Records(!)"));
    lastb.setForeground(Color.white); lastb.setBackground(Color.blue);
    p1.add(lastb=new Button("QUIT"));
    lastb.setForeground(Color.white); lastb.setBackground(Color.red);
    add("North",p1);
    add("South",p2);
    add("East",cp);
    //System.out.println("Curve constructed.");

   ///for (int i=0;i<MAXSEG;i++) seg[i]=i*(2*Math.PI/MAXSEG);
   ///for (int i=0;i<MAXPOINT;i++) omags[i]=0;
  }

public void resetMarker()
{
 for (int i=0;i<markerc;i++)
 {
  marker[i][0]=0;
  marker[i][1]=0;
  marker[i][2]=0;
  marker[i][3]=0;
 } 
}
 
 int dupstart=0,dupend=0;
 public ImageProcess(String s,double VR,double VG, double VB)
 {
  this.VR=VR;
  this.VG=VG;
  this.VB=VB;

 def_pattern_file=s+".pat";
 def_picture_file=s;
 markerR=new int[20];
 markerG=new int[20];
 markerB=new int[20];
 markerC=new Color[20];
 marker=new double[20][4];
 names =new String[20];
 mask  =new int[20];
 int k=0;
 //markerR[k]=150; markerG[k]=130; markerB[k++]=130; names[k-1]="gray";
 markerR[k]=255; markerG[k]=255; markerB[k++]=255; names[k-1]="white";
 markerR[k]=  0; markerG[k]=  0; markerB[k++]=  0; names[k-1]="black";
 markerR[k]=105; markerG[k]=187; markerB[k++]= 80; names[k-1]="green";
 markerR[k]=206; markerG[k]= 19; markerB[k++]= 30; names[k-1]="red";
 markerR[k]=100; markerG[k]= 45; markerB[k++]= 22; names[k-1]="brown"; 
 markerR[k]=230; markerG[k]=198; markerB[k++]=  5; names[k-1]="yellow";
 dupstart=k;
 markerR[k]= 33; markerG[k]= 30; markerB[k++]=153; names[k-1]="blue";
 markerR[k]=155; markerG[k]= 15; markerB[k++]= 50; names[k-1]="magenta";
 markerR[k]= 32; markerG[k]= 80; markerB[k++]= 66; names[k-1]="gray-gr";
 markerR[k]= 80; markerG[k]= 32; markerB[k++]=100; names[k-1]="violet";
 markerR[k]=250; markerG[k]=108; markerB[k++]=  5; names[k-1]="orange";
 markerR[k]=140; markerG[k]=195; markerB[k++]=178; names[k-1]="cyan";
 dupend=k-1;
 dstart=k;
 markerR[k]= 33; markerG[k]= 30; markerB[k++]=153; names[k-1]="blue";
 markerR[k]=155; markerG[k]= 15; markerB[k++]= 50; names[k-1]="magenta";
 markerR[k]= 32; markerG[k]= 80; markerB[k++]= 66; names[k-1]="gray-gr";
 markerR[k]= 80; markerG[k]= 32; markerB[k++]=100; names[k-1]="violet";
 markerR[k]=250; markerG[k]=108; markerB[k++]=  5; names[k-1]="orange";
 markerR[k]=140; markerG[k]=195; markerB[k++]=178; names[k-1]="cyan";
 markerc=k;
 for (int i=0;i<markerc;i++) markerC[i]=new Color(markerR[i],markerG[i],markerB[i]);
 //System.out.println("MArkerc:"+markerc);
  resetMarker();
  Xdim=3; 
  Ydim=15;
  Zdim=dstart; 
  askbuf=new double[Xdim];
  bp=new BP();
  bp.resize(400,300);
  //bp.setBounds(0,400,400,300);
  bp.show();
  setupButtons();
  loadPicture(s);
  top=this;
 
 }
 
public void loadPicture(String s)
{
  im_done=false;
  if (im!=null) {im=null;}
  Toolkit tk=Toolkit.getDefaultToolkit();
  System.out.println("Opening:"+s);
  im=tk.getImage(s);
  if (im==null) {System.out.println("Cannot open:"+s); return ;}
  he=im.getHeight(this);
  wi=im.getWidth(this);
  midx=wi/2;
  midy=he/2;
}

double rgbAngle(double r0,double g0,double b0,double r1,double g1,double b1)
{
 //if (r0+g0+b0<45) return 255;
 //if (r1+g1+b1<45) return 255;
// r0-=128;
// g0-=128;
// b0-=128;
// r1-=128;
// g1-=128;
// b1-=128;
 double l0=Math.sqrt(r0*r0+g0+g0+b0*b0);
 double l1=Math.sqrt(r1*r1+g1+g1+b1*b1);
 r0/=l0; g0/=l0; b0/=l0;
 r1/=l1; g1/=l1; b1/=l1;

 return 255*rgbdist(r0,g0,b0,r1,g1,b1);
}

double rgbdist0(double r0,double g0,double b0,double r1,double g1,double b1)
{
 return Math.sqrt((r0-r1)*(r0-r1) + (g0-g1)*(g0-g1) + (b0-b1)*(b0-b1));
}

double rgbdist(double r0,double g0,double b0,double r1,double g1,double b1)
{
 return Math.sqrt(0.2*(r0-r1)*(r0-r1) + 0.4*(g0-g1)*(g0-g1) + 0.4*(b0-b1)*(b0-b1));
}

public Point3d tohsv(double r, double g, double b)
{double min,max;
 double h=0,s=0,v=0;
 if (g>r) max=g; else max=r; if (b>max) max=b;
 if (g<r) min=g; else min=r; if (b<min) min=b;
 double delta=max-min;
 v=max;
 if (max!=0) s=delta/max;
 else s=0;
 if (s==0) h=-1;
 else
 {
  if (r==max) h=(g-b)/delta;
  else if (g==max) h=2+(b-r);
  else if (b==max) h=4+(r-g);
  h*=360;
 }
 if (h<0) h=0;
 h/=360;
 return new Point3d(h,s,v);
}
 
double hsvdist(double r0,double g0,double b0,double r1,double g1,double b1)
{
 r0/=255; g0/=255; b0/=255;
 r1/=255; g1/=255; b1/=255;
 Point3d p0=tohsv(r0,g0,b0);
 Point3d p1=tohsv(r1,g1,b1);
 return 255*Math.sqrt(0.4*Elib.sqr((p0.x-p1.x))+
                      0.3*Elib.sqr((p0.y-p1.y))+
                      0.3*Elib.sqr((p0.z-p1.z)));
}

int getHSVclose(float r,float g,float b)
{
 double dis;
 double mindis=1e10;
 int ix=-1;
 for (int i=0;i<dstart;i++)
  {
    dis=hsvdist(r,g,b,markerR[i],markerG[i],markerB[i]);
    //dis=rgbdist0(n.mR,n.mG,n.mB,markerR[i],markerG[i],markerB[i]);
    //dis=rgbAngle(n.mR,n.mG,n.mB,markerR[i],markerG[i],markerB[i]);
    if (dis<mindis)
     {
       mindis=dis;
       ix    =i;
     }
  }

  return ix;
}


double rgbmindis;
int getRGBclose(float r,float g,float b)
{
 double dis;
 rgbmindis=1e10;
 int ix=-1;
 for (int i=0;i<dstart;i++)
  {
    dis=rgbdist0(r,g,b,markerR[i],markerG[i],markerB[i]);
    if (dis<rgbmindis)
     {
       rgbmindis=dis;
       ix    =i;
     }
  }

  return ix;
}



public void applyMaxMask(int Rad)
 {
  int maxv; 
  int sumR=0,sumG=0,sumB=0;
  short maxR=0,maxG=0,maxB=0;

  double cc=(2*Rad+1)*(2*Rad+1)-1;
  if (!im_done) return;
  Graphics gg=cv.getGraphics();

  for (int x=Rad;x<wi-Rad;x++)
   for (int y=Rad;y<he-Rad;y++)
  {
   gg.setColor(Color.white);
   gg.drawLine(x,y,x+1,y);

  maxv=-1;
  maxR=0;maxG=0;maxB=0;
  sumR=0;sumG=0;sumB=0;
  for (int i=-Rad;i<=Rad;i++)
  for (int j=-Rad;j<=Rad;j++)
   //if (i!=0 || j!=0)
    {
      sumR=R[(y+i)*wi+(x+j)];
      sumG=G[(y+i)*wi+(x+j)];
      sumB=B[(y+i)*wi+(x+j)];
      if (sumR+sumG+sumB >= maxv)
      { maxv=sumR+sumG+sumB;
        maxR=(short)sumR;
        maxG=(short)sumG;
        maxB=(short)sumB;
      }
    }
   if (maxv>-1){
   resR[y*wi+x]=maxR;
   resG[y*wi+x]=maxG;
   resB[y*wi+x]=maxB;
   } else
   {
   resR[y*wi+x]=R[y*wi+x];
   resG[y*wi+x]=G[y*wi+x];
   resB[y*wi+x]=B[y*wi+x];
   }
   gg.setColor(new Color(resR[y*wi+x], resG[y*wi+x], resB[y*wi+x]));
   gg.drawLine(x,y,x,y);
  }
 }

public void applyBRMeanMask(int Rad)
 {
  double cc=(2*Rad+1)*(2*Rad+1)-1;
  if (!im_done) return;
  Graphics gg=cv.getGraphics();

  for (int x=Rad;x<wi-Rad;x++)
   for (int y=Rad;y<he-Rad;y++)
  {
   gg.setColor(Color.white);
   gg.drawLine(x,y,x+1,y);

  int sumR=0,sumG=0,sumB=0;
  for (int i=-Rad;i<=Rad;i++)
  for (int j=-Rad;j<=Rad;j++)
   if (i!=0 || j!=0)
    {
      sumR+=R[(y+i)*wi+(x+j)];
      sumG+=G[(y+i)*wi+(x+j)];
      sumB+=B[(y+i)*wi+(x+j)];
    }
  short newR=(short)(sumR/cc+0.5);
  short newG=(short)(sumG/cc+0.5);
  short newB=(short)(sumB/cc+0.5);

  if ((newR+newG+newB) > (resR[y*wi+x]+resG[y*wi+x]+ resB[y*wi+x]))
  {
   resR[y*wi+x]=newR;
   resG[y*wi+x]=newG;
   resB[y*wi+x]=newB;
  }
   gg.setColor(new Color(resR[y*wi+x], resG[y*wi+x], resB[y*wi+x]));
   gg.drawLine(x,y,x,y);
  }
 }


public void applyMeanMask(int Rad)
 {
  double cc=(2*Rad+1)*(2*Rad+1)-1;
  if (!im_done) return;
  Graphics gg=cv.getGraphics();

  for (int x=Rad;x<wi-Rad;x++)
   for (int y=Rad;y<he-Rad;y++)
  {
   gg.setColor(Color.white);
   gg.drawLine(x,y,x+1,y);

  int sumR=0,sumG=0,sumB=0;
  for (int i=-Rad;i<=Rad;i++)
  for (int j=-Rad;j<=Rad;j++)
   if (i!=0 || j!=0)
    {
      sumR+=R[(y+i)*wi+(x+j)];
      sumG+=G[(y+i)*wi+(x+j)];
      sumB+=B[(y+i)*wi+(x+j)];
    }
  resR[y*wi+x]=(short)(sumR/cc+0.5);
  resG[y*wi+x]=(short)(sumG/cc+0.5);
  resB[y*wi+x]=(short)(sumB/cc+0.5);
  gg.setColor(new Color(resR[y*wi+x], resG[y*wi+x], resB[y*wi+x]));
  gg.drawLine(x,y,x,y);
  }
 }

public void applyMask(double[][] M,int Rad)
{ 
  if (!im_done) return;
  Graphics gg=cv.getGraphics();
  for (int x=Rad;x<wi-Rad;x++)
   for (int y=Rad;y<he-Rad;y++)
    {
     double dr=0,dg=0,db=0;
     for (int i=-Rad;i<=Rad;i++)
       for (int j=-Rad;j<=Rad;j++)
        {
          dr+= M[i+Rad][j+Rad]*R[(y+i)*wi+(x+j)];
          dg+= M[i+Rad][j+Rad]*G[(y+i)*wi+(x+j)];
          db+= M[i+Rad][j+Rad]*B[(y+i)*wi+(x+j)];
        }
     resR[y*wi+x]=(short)(0.5+dr);
     resG[y*wi+x]=(short)(0.5+dg);
     resB[y*wi+x]=(short)(0.5+db);
    if (resR[y*wi+x]>255) resR[y*wi+x]=255;
    if (resG[y*wi+x]>255) resG[y*wi+x]=255;
    if (resB[y*wi+x]>255) resB[y*wi+x]=255;
    if (resR[y*wi+x]<0) resR[y*wi+x]=0;
    if (resG[y*wi+x]<0) resG[y*wi+x]=0;
    if (resB[y*wi+x]<0) resB[y*wi+x]=0;
   

   gg.setColor(new Color(resR[y*wi+x], resG[y*wi+x], resB[y*wi+x]));
   gg.drawLine(x,y,x,y);
  }
}


public void switchRGB()
{ short[] t;
  t=R;  R=resR; resR=R;
  t=G;  G=resG; resG=G;
  t=B;  B=resB; resB=B;
}
public void applyMedianMask(int Rad)
{
  int sz=(Rad*2+1)*(Rad*2+1);
  double[] dis=new double[sz];
  Point3d[] col=new Point3d[sz];
  for (int i=0;i<sz;i++) col[i]=new Point3d(0,0,0);
  
  if (!im_done) return;
  Graphics gg=cv.getGraphics();
  for (int x=Rad;x<wi-Rad;x++)
   for (int y=Rad;y<he-Rad;y++)
    { 
   gg.setColor(Color.white);
   gg.drawLine(x,y,x+1,y);
     int k=0;
     for (int i=-Rad;i<=Rad;i++)
       for (int j=-Rad;j<=Rad;j++)
        {
          col[k].x=R[(y+i)*wi+(x+j)];
          col[k].y=G[(y+i)*wi+(x+j)];
          col[k].z=B[(y+i)*wi+(x+j)];
          //dis[k]=rgbdist0(col[k].x,col[k].y,col[k].z,0,0,0);
          dis[k]=hsvdist(col[k].x,col[k].y,col[k].z,0,col[k].y,col[k].z);
          k++;
       }

     int i=0;
     for ( i=0;i<(sz/2)+1;i++)
      for (int j=i+1;j<sz;j++)
       {
        if (dis[i]>dis[j])
         {double t=dis[j];
          dis[j]=dis[i];
          dis[i]=t;
          Point3d p=col[j];
          col[j]=col[i];
          col[i]=p;
         }
       }
     resR[y*wi+x]=(short)(0.5+col[i-1].x);
     resG[y*wi+x]=(short)(0.5+col[i-1].y);
     resB[y*wi+x]=(short)(0.5+col[i-1].z);

   gg.setColor(new Color(resR[y*wi+x], resG[y*wi+x], resB[y*wi+x]));
   gg.drawLine(x,y,x,y);
  }
}



double[][] makeMeanMask(int rad)
{
 double[][] m=new double[2*rad+1][2*rad+1];
 for (int i=-rad;i<=rad;i++)
 for (int j=-rad;j<=rad;j++)
  m[rad+i][rad+j]=1.0/((2.0*rad+1)*(2.0*rad+1)-1);
 return m;
}
 

public int histmode=1;
public void histit(Node n)
{
 if (histmode==0) hsv_histit(n);
 else bp_histit(n);
}

//note that connect doesn't use BP in even this!!!
public void bp_histit(Node n)
{
 int i=0;
 double dis=0;
 double A=n.wi*n.he;
 double mindis2=1e11;
 double mindis=1e10;
 int ix=-1,ix2=-1,sel;

  if (n.wi*n.he<=4) return;
  sel=-1;
  askbuf[0]=n.mR/255.0;
  askbuf[1]=n.mG/255.0;
  askbuf[2]=n.mB/255.0;
  double[] ans=bp.ask(askbuf,Xdim);
  ix=interpretAns(ans);
  sel=ix;
  if (ix>=dupstart && ix<=dupend )   // a duplicate color
  {  
     ix2=ix+dstart-dupstart;
     //System.out.println("Check must be equal:"+names[ix]+"="+names[ix2]);
     if ((marker[ix][2]==0 && marker[ix2][2]==0))
     { sel=ix; }
     else if (marker[ix][2]!=0)
     {
      if (connected(ix,n)) sel=ix;
                      else sel=ix2;
     }
     else if (marker[ix2][2]!=0)
     {
     if (connected(ix2,n)) sel=ix2;
                     else  sel=ix;
     }
     else //both non zero
     { 
       if (connected(ix2,n)) 
       {
        if (connected(ix,n))
         System.out.println("-*-*-*Oh no! double connected!!! "+names[ix]+"  "+ix+"  "+ix2);
        else
        sel=ix2;
       }
       else
       {
         if (connected(ix,n))  sel=ix;
         else
         System.out.println("-*-*-*Oh no double unconnected!!!"+names[ix]);
       } 
     }
   }
 
 if (sel<0) {  //ambigious do nothing 
  System.out.println("Ambigous. do nothing.");
  return;
 }
 if (marker[sel][2]!=0)
 {
  double dm=Elib.dist(n.x+n.wi/2 , n.y+n.he/2 , marker[sel][0]/marker[sel][2], marker[sel][1]/marker[sel][2]);
  if (dm>50) return;
 }
 marker[sel][0]+=A*(n.x+n.wi/2.0);
 marker[sel][1]+=A*(n.y+n.he/2.0);
 marker[sel][2]+=A;
}

public void hsv_histit(Node n)
{
 int i=0;
 double dis=0;
 double A=n.wi*n.he;
 double mindis2=1e11;
 double mindis=1e10;
 int ix=-1,ix2=-1;
  if (n.wi*n.he<=4) return;
 for (i=0;i<markerc;i++)
  {
    dis=hsvdist(n.mR,n.mG,n.mB,markerR[i],markerG[i],markerB[i]);
    //dis=rgbdist0(n.mR,n.mG,n.mB,markerR[i],markerG[i],markerB[i]);
    //dis=rgbAngle(n.mR,n.mG,n.mB,markerR[i],markerG[i],markerB[i]);
    if (dis<mindis)
     {
       mindis=dis;
       ix    =i;
     }
  }

 for (i=0;i<markerc;i++)
  { if (i==ix) continue;
   dis=rgbdist(n.mR,n.mG,n.mB,markerR[i],markerG[i],markerB[i]);
   if (dis<mindis2)
     {
       mindis2=dis;
       ix2   =i;
     }
  }

 if (ix2==-1 || ix==-1) return;
 //System.out.println("NODE["+n.wi+"x"+n.he+":"+n.x+","+n.y+"] 1st,2nd winner:"+names[ix]+"["+ix+"] "+names[ix2]+"["+ix2+"]");
 if (mindis>COLORSEP) return;
 //if (mindis2>COLORSEP) return;

 //markerR[ix]+=(int)Elib.sgn(n.mR-markerR[ix]);
 //markerG[ix]+=(int)Elib.sgn(n.mG-markerG[ix]);
 //markerB[ix]+=(int)Elib.sgn(n.mB-markerB[ix]);

  if (ix!=ix2 && names[ix].equals(names[ix2]))
   {
     if ((marker[ix][2]==0 && marker[ix2][2]==0))
     {}
     else if (marker[ix2][2]==0)
     {
      if (!connected(ix,n)) ix=ix2;
     }
     else if (marker[ix][2]==0)
     {
     if (connected(ix2,n)) ix=ix2;
     }
     else //both non zero
     {
       if (connected(ix2,n))
       {
        if (connected(ix,n))
         System.out.println("-*-*-*Oh no! double connected!!! "+names[ix]+"  "+ix+" by "+ix2+","+ix);
        else
        ix=ix2;
       }
       else
       {
         if (!connected(ix,n))
         System.out.println("-*-*-*Oh no double unconnected!!!"+names[ix]+","+ix);
       }
     }
   }
 if (marker[ix][2]!=0)
 {
  double dm=Elib.dist(n.x+n.wi/2 , n.y+n.he/2 , marker[ix][0]/marker[ix][2], marker[ix][1]/marker[ix][2]);
  if (dm>50) return;
 }
 marker[ix][0]+=A*(n.x+n.wi/2.0);
 marker[ix][1]+=A*(n.y+n.he/2.0);
 marker[ix][2]+=A;
 if ((names[ix].equals("cyan")) || (names[ix2].equals("cyan"))) 
   System.out.println("A:"+A+" winner1:"+names[ix]+"("+ix+")"+" winner2:"+names[ix2]+"("+ix2+")");
}



public boolean connected(int ix,Node n)
{
 int x,y; 
 double x0,y0;
 double x1,y1;
 double dx,dy=0;
 int N=0;
 int other=0;

 if (marker[ix][2]==0) return true; //new addition

 Graphics gg=cv.getGraphics();
 x=n.x+n.wi/2; y=n.y+n.he/2;
 x0=x; y0=y;
 x1=marker[ix][0]/marker[ix][2]; 
 y1=marker[ix][1]/marker[ix][2]; 

 gg.setColor(Color.green);
 gg.drawLine(x,y,(int)x1,(int)y1);
 double dist=Elib.dist(x0,y0,x1,y1);
 //System.out.println("$$$$$ "+names[ix]+" ["+ix+"]  "+dist+" pos of n:"+x0+" "+y0+" pos of marker:"+x1+","+y1);

 if (dist<40) 
  {
   System.out.println("close. assume connected:"+names[ix]);
   return true;
  }


 
 dx=x1-x0;
 dy=y1-y0;
 if (Math.abs(dy)>Math.abs(dx)) //sign     //sing
 { dx=dx/Math.abs(dy); N=Elib.abs((int)dy); dy=Elib.sgn(dy);}
 else
 { dy=dy/Math.abs(dx); N=Elib.abs((int)dx); dx=Elib.sgn(dx);}
 for (int i=0;i<N;i++)
 {
  x=(int)x0;
  y=(int)y0;
  //gg.fillRect(x,y,2,2); 
/* this is obsolete change to network 
 for hsv use that one though! 

  short r=R[y*wi+x];
  short g=G[y*wi+x];
  short b=B[y*wi+x];
  double dis=rgbdist(r,g,b,n.mR,n.mG,n.mB);
  if (dis>COLORSEP) other++;
*/
    askbuf[0]=R[y*wi+x]/255.0;
    askbuf[1]=G[y*wi+x]/255.0;
    askbuf[2]=B[y*wi+x]/255.0;
    double[] ans=bp.ask(askbuf,Xdim);
    int k=interpretAns(ans);
    if (k!=ix) other++;  // different color on the  line
  x0+=dx; y0+=dy;
 }
 //System.out.println("off ratio:"+(double)other/(double)N);
 if ((double)other/(double)N > 0.4) return false;
 else return true;
/*
      double dd=Elib.sqr(n.x+n.wi/2 - marker[ix][0]/marker[ix][2]) +
                Elib.sqr(n.y+n.he/2 - marker[ix][1]/marker[ix][2]);
*/
}

public void showColors()
{
 
 for (int i=0;i<markerc;i++)
  {
   if (marker[i][2]==0) 
   {System.out.println(i+"."+names[i]+":(AREA ZERO)"); continue;}
   marker[i][0]/=marker[i][2];
   marker[i][1]/=marker[i][2];
   System.out.println(i+"."+names[i]+":("+marker[i][0]+","+marker[i][1]+")");
   Graphics g=cv.getGraphics();
   g.setColor(Color.red);
   int x=(int)(0.5+marker[i][0]);
   int y=(int)(0.5+marker[i][1]);
   g.fillArc(x-5,y-5,10,10,0,360);
   g.setColor(Color.yellow);
   g.drawArc(x-3,y-3,6,6,0,360);
   //g.setColor(Color.white);
   //g.fillRect(x+10,y-10,70,20);
   g.setColor(Color.red);
   g.drawString(names[i],x+10,y);
   g.drawString(names[i],x+11,y+1);
 
 }
}


 public Point3d color2ix_bot(String c)
 {
  for (int i=markerc-1;i>=0;i--)
   if (names[i].equals(c)) 
     if (marker[i][2]==0.0) return null;
     else return new Point3d(marker[i][0],marker[i][1],i);
  return null;
 }
 public Point3d color2ix_top(String c)
 {
  for (int i=0;i<markerc;i++)
   if (names[i].equals(c)) 
     if (marker[i][2]==0.0) return null;
     else return new Point3d(marker[i][0],marker[i][1],i);
  return null;
 }
 public Point3d color2ix(String c)
 {
  return color2ix_top(c);
 }

 public double getPalmLen()
 {
  double d1=dist(fing[WRIST][0],fing[INDEX][0]);
  double d2=dist(fing[WRIST][0],fing[PINKY][0]);
  if (fing[PINKY][0]==null) d2=1.5*dist(fing[WRIST][0],fing[MIDDLE][0]);
  double d3=dist(fing[INDEX][0],fing[PINKY][0]);
  if (fing[PINKY][0]==null) d3=3*dist(fing[INDEX][0],fing[MIDDLE][0]);
  
  double max=d1;
  if (d2>d1) max=d2;
  if (d3>max) max=d3;
  return max;
 }
   
 public double getLen(int l1,int j1,int l2,int j2)
 {
  return dist(fing[l1][j1],fing[l2][j2]);
 }
  
//should be smarter
 public Point3d getOrientation()
 {
  Point3d p=null;
  Point3d w=fing[WRIST][0];
  Point3d p1=fing[PINKY][0];
  Point3d p2=fing[MIDDLE][0];
  Point3d p3=fing[INDEX][0];

  if (p2!=null) p=VA.subtract(p2,w);
  else if (p1!=null & p3!=null) 
    { Point3d c=VA.add(p1,p3);
      VA._scale(c,0.5);
      p=VA.subtract(c,w);
    }
  p.z=0;
  VA._normalize(p);
  System.out.println("Normalized arm-orientation:"+p.str());
  double angle=VA.cosSin(p.x,p.y);
  p.x=angle;
  p.y=getPalmLen();
  p.z=0;
  return p;
 }

 static final int PINKY=0;
 static final int RING=1;
 static final int MIDDLE=2;
 static final int INDEX=3;
 static final int THUMB=4;
 static final int WRIST=5;
 Point3d wrist;
 Point3d[][] fing=new Point3d[6][3];
 String[] limbs=new String[6]; 
 public void extract0()
 {
  limbs[PINKY]="pinky";
  limbs[RING]="ring";
  limbs[MIDDLE]="middle";
  limbs[INDEX]="index";
  limbs[THUMB]="thumb";
  limbs[WRIST]="wrist";

  wrist=null; 
  fing[WRIST][0]=color2ix_top("blue");
  fing[WRIST][1]=color2ix_bot("blue");

  // find the wrist mid point. If no blue is visible wrist stays null.
  if  (fing[WRIST][0]!=null && fing[WRIST][1]!=null)
  { wrist=VA.add(fing[WRIST][0],fing[WRIST][1]);
    VA._scale(wrist,0.5);
  } else
  if (fing[WRIST][0]!=null) wrist=fing[WRIST][0];
  else  wrist=fing[WRIST][1];


  fing[PINKY][0]=color2ix("brown"); 
     fing[PINKY][1]=color2ix_top("gray-gr"); 
     fing[PINKY][2]=color2ix_bot("gray-gr"); 
  fing[RING][0]=null; //no color color2ix("brown"); 
    fing[RING][1]=color2ix_top("violet"); 
    fing[RING][2]=color2ix_bot("violet"); 
  fing[MIDDLE][0]=color2ix("red"); 
    fing[MIDDLE][1]=color2ix_top("orange"); 
    fing[MIDDLE][2]=color2ix_bot("orange"); 
  fing[INDEX][0]=color2ix("green"); 
    fing[INDEX][1]=color2ix_top("cyan"); 
    fing[INDEX][2]=color2ix_bot("cyan"); 
  fing[THUMB][0]=color2ix("yellow"); 
    fing[THUMB][1]=color2ix_top("magenta"); 
    fing[THUMB][2]=color2ix_bot("magenta"); 


  //pull the center of thumb towards to the base of thumb
  // usually it is too close to the tip.
  if (fing[THUMB][1]!=null && fing[THUMB][2]!=null)
   if (fing[THUMB][0]!=null) 
    fing[THUMB][1]=VA.add(fing[THUMB][0],
          VA.scale(VA.subtract(fing[THUMB][1],fing[THUMB][0]),0.8));
 }

 public double dist(Point3d p1,Point3d p2)
 {
  if (p1==null) return -1;
  if (p2==null) return -2;
  return VA.dist(p1,p2);
 }

 public void extract1()
 {
  if (wrist==null) {System.err.println("Screwed! WRIST not found.!!"); return;}
  // Estimate ring base
  if (fing[PINKY][0]!=null && fing[MIDDLE][0]!=null)
   fing[RING][0]=new Point3d(0.5*(fing[PINKY][0].x+fing[MIDDLE][0].x),
                             0.5*(fing[PINKY][0].y+fing[MIDDLE][0].y),-1);


 // try to assign double colors to correct joints.
 // if visible use finger root else use wrist
 // if one of them is not visible assume visible is not the tip.
 Point3d q,base;
 for (int i=PINKY;i<=THUMB;i++)
  { 
   
    base=fing[i][0];
    if (base==null) base=wrist;
    double d1=dist(base,fing[i][1]);
    double d2=dist(base,fing[i][2]);
    if (d1>=0 && d2>=0)
     {
     if (d1>d2) {q=fing[i][1]; fing[i][1]=fing[i][2]; fing[i][2]=q;}
     }
    else
     {
     if (fing[i][1]==null) 
       {q=fing[i][1]; fing[i][1]=fing[i][2]; fing[i][2]=q;}
     }
  }  
 }


 public void saveExtract(String fn)
 {
   int k=0;
   double wx,wy;
   extract0();
   extract1();
   
   try {
   DataOutputStream patout = openfileWRITE(fn);
   patout.writeBytes("# This hand marker file is generated by ImageProcess (Erhan Oztop -Jan'00)\n");
   patout.writeBytes("# At the time of writing this file:  picture & weight file used :"+picture.getText()+","+weight.getText()+"\n");

  
  patout.writeBytes("# WRIST\n"+"##"+limbs[WRIST]+"  ");
  if (wrist==null) { patout.writeBytes(" 0 0 0\n"); wx=0; wy=0;}
  else {patout.writeBytes(Elib.snice(wrist.x,1e3,6)+" "+
                          Elib.snice(he-wrist.y,1e3,6)+" 1.0\n"); wx=wrist.x; wy=wrist.y;}
  Point3d or=getOrientation();
  patout.writeBytes("# rotation by z axis, palmlen, N/A\n"+"orientation"+"  ");
  System.out.println("orientation:"+or.str());
  patout.writeBytes(Elib.snice(or.x,1e3,6)+" "+
                    Elib.snice(or.y,1e3,6)+" "+
                    Elib.snice(or.z,1e3,6)+"\n"); 

  patout.writeBytes("# now comes pinky_base, pinky_center, pinky_tip ... index_..\n");
  for (int i=PINKY;i<=THUMB;i++)
   {
   for (int j=0;j<=2;j++)
     {patout.writeBytes(limbs[i]+j+"  "); 
      if (fing[i][j]==null) patout.writeBytes("0 0 0\n");
      else patout.writeBytes(Elib.snice(fing[i][j].x-wx, 1e3, 6)+" "+
                             Elib.snice(-fing[i][j].y+wy, 1e3, 6)+" 1.0\n");
     }
   }
  patout.writeBytes("# THUMB\n"+limbs[THUMB]+"0  ");
  if (fing[THUMB][0]==null) patout.writeBytes("0 0 0\n");
  else patout.writeBytes(Elib.snice(fing[THUMB][0].x-wx, 1e3, 6)+" "+
                         Elib.snice(-fing[THUMB][0].y+wy, 1e3, 6)+" 1.0\n");
  patout.writeBytes("# THUMB\n"+limbs[THUMB]+"1 ");
  if (fing[THUMB][1]==null) patout.writeBytes("0 0 0\n");
  else patout.writeBytes(Elib.snice(fing[THUMB][1].x-wx, 1e3, 6)+" "+
                         Elib.snice(-fing[THUMB][1].y+wy, 1e3, 6)+" 1.0\n");

  patout.close();
  } catch (IOException e)
  { System.err.println("closePattern : EXCEPTION "+e);
   }
  System.out.println("Wrote extracted hand info in "+fn);
 }

  public void initimg()
  {
   sz=wi*he;
   pix=new int[sz];
   pick=new PixelGrabber(im,0,0,wi,he,pix,0,wi);
   try{
   pick.grabPixels();
   } catch (InterruptedException e) {System.err.println("Exception4981!");}
   R=new short[sz];
   G=new short[sz];
   B=new short[sz];
   resR=new short[sz];
   resG=new short[sz];
   resB=new short[sz];
   fillRGB();
  }

  public void fillRGB()
  { 

   for (int i=0;i<sz;i++)
   {
   int r=(pix[i] & 0x00FF0000)>>16;
   int g=(pix[i] & 0x0000FF00)>>8;
   int b=(pix[i] & 0x000000FF);
   R[i]=(short)r; B[i]=(short)b; G[i]=(short)g;
   }
   //System.out.println("# colors:"+ findcolors(3));
  }

  public boolean mouseDown(Event evt, int x, int y)
  {
   Point p=cv.getLocation();
   x-=p.x;
   y-=p.y;
   int i=y*wi+x;
   if (i>=sz || i<0) return true;
   
   //System.out.println(x+","+y+":(RGB)"+R[i]+","+G[i]+","+B[i]);
   if (pickmode==0 || pickmode==1) 
   {
    pickmode=1; //picked
    pickR=R[i]; pickG=G[i]; pickB=B[i];
    colbut.setBackground(new Color(R[i],G[i],B[i]));
   } else
   if (pickmode==2);
  
   return true;
  }

 public boolean mouseMove(Event evt, int x, int y)
  {
   Point p=cv.getLocation();
   x-=p.x;
   y-=p.y;
   int i=y*wi+x;
   if (i>=sz || i<0) return true;
   if (pickmode==0 ) 
   {
    setTitle(x+","+y+":(RGB)"+R[i]+","+G[i]+","+B[i]+ ((recording)?" * RECORDED:"+patc :""));
     colbut.setBackground(new Color(R[i],G[i],B[i]));
   } else
   if (pickmode==1) 
   {
     setTitle(x+","+y+":(RGB)"+R[i]+","+G[i]+","+B[i]+((recording)?" * RECORDED:"+patc:""));
   } else
   if (pickmode==2)     
   {
    askbuf[0]=R[i]/255.0;
    askbuf[1]=G[i]/255.0;
    askbuf[2]=B[i]/255.0;
    double[] ans=bp.ask(askbuf,Xdim);
    int k=interpretAns(ans);
    setTitle("I think it is:"+names[k]+((recording)?" * RECORDED:"+patc:""));
    colbut.setBackground(markerC[k]);
   }
   
   return true;
  }

public int interpretAns(double[] ans)
 {double min=1e10;
  double max=-1e10;
  int minix=-1,maxix=-1;
  for (int i=0;i<Zdim;i++)
   { 
    //System.out.print(" "+Elib.snice(ans[i],1e3,6));
    if (ans[i]<min) 
      { minix=i;
        min=ans[i];
      }
    if (ans[i]>max) 
      { maxix=i;
        max=ans[i];
      }
   }
   //System.out.println("");
   if ((max-min)>0.1)
     return Zdim-maxix-1;
   else return 0;
 }
 
  public boolean handleEvent(Event evt)
  { if (evt.id == Event.WINDOW_DESTROY) System.exit(0);
    return super.handleEvent(evt);
  }

  public boolean action(Event evt, Object arg)
  {int hit;

   //System.out.println("arg:"+arg);
   if (arg.equals("Init"))
     {
     // run, endrun,break,cont, step x
    }
   else if (arg.equals("Load"))
     {
      loadPicture(picture.getText());
     }
   else if (arg.equals("MEDIANFILT"))
     {
     applyMedianMask(1);
     switchRGB();
     }
   else if (arg.equals("MEANBRFILT"))
     {
      System.out.println("preping...\n");
      applyBRMeanMask(2);
      switchRGB();
      System.out.println("preped\n");
     }
   else if (arg.equals("MAXFILT"))
     {
      System.out.println("preping...\n");
      applyMaxMask(3);
      switchRGB();
      System.out.println("preped\n");
     }
   else if (arg.equals("MEANFILT"))
     {
      System.out.println("preping...\n");
      applyMeanMask(1);
      switchRGB();
      System.out.println("preped\n");
     }
   else if (arg.equals("BP_FIND") || arg.equals("HSV_FIND"))
     {
     if (!networkset)
      {
       System.out.println("Making the network from Weight file"+weight.getText());
       bp.netFromWeight(weight.getText());
       networkset=true;
       input.setText(bp.Xdim+"");
       hidden.setText(bp.Ydim+"");
       output.setText(bp.Zdim+"");
     }
     
       if (arg.equals("BP_FIND")) histmode=1; 
       else histmode=0;
       resetMarker();
       //if (!splitdone)
       {
        splitdone=true;
        System.out.println("I am working...be patient!");
        Node nn=new Node(null,10,10,wi-20,he-20);
        nn.split(VR,VG,VB,3);
        cv.repaint();
      }
     
    }
   else if (arg.equals("SaveExtract"))
   {
    if (!splitdone) { System.err.println("not extracted!\n"); }
    else saveExtract(picture.getText()+".ext");
   } 
   else if (arg.equals("     "))
   {
     pickmode=0; 
   }
   else if (arg.equals("TEST"))
   {
     if (pickmode==2) pickmode=0;
     else
     if (!networkset)
     { System.err.println("You must create a network first!!"); }
     else
     {
      System.err.println("in Test mode!"); 
      pickmode=2;
      recording=false;
     }

   }
   else if (arg.equals("Palette"))
   {
    Graphics g=cv.getGraphics();
    for (int i=0;i<dstart;i++)
    { g.setColor(markerC[i]);g.fillRect(10+i*40,10,40,40); }
   }
   else if (arg.equals("Train Network"))
    {
     if (!networkset)
     { System.err.println("You must create a network first!!"); }
     else
     {
     System.err.println("Training... Epoch size:"+bp.epochs.getText());
     bp.train(Elib.toInt(bp.epochs.getText()));
     double err=bp.testPattern();
     }
    }
    else if (arg.equals("Clear Records(!)"))
    {
     clearPattern();
     System.err.println("Patterns cleared and Recording set to OFF.");
     recording=false;
    }
    else if (arg.equals("Start Recording"))
    {
     startPattern();
     setTitle("NOW RECORDING. pick color from image and click matching button.");
     pickmode=0;
    } 
    else if (arg.equals("Stop Recording"))
    {
     closePattern();
    }
    else if (arg.equals("Save Recorded"))
    {
     saveRecorded(pattern.getText());
    }
     else if (arg.equals("Create Net from Pattern"))
    {
     bp.netFromPattern(pattern.getText());
     System.err.println("Loaded Patterns ["+pattern.getText()+"] and created a new network. Now you can train or test.");
     networkset=true;
    }
    else if (arg.equals("Save Weights"))
    {
    bp.writeWeight(weight.getText());
    System.err.println("Wrote weight file "+weight.getText());
    }
    else if (arg.equals("Create Net from Weight"))
    {
     bp.netFromWeight(weight.getText());
     System.err.println("Created a new network from the weight file "+weight.getText());
     networkset=true;
     input.setText(bp.Xdim+"");
     hidden.setText(bp.Ydim+"");
     output.setText(bp.Zdim+"");
    }
    else if (arg.equals("QUIT"))
    {
      System.exit(0);
    }
    else if (recording && pickmode==1)
    {
     for (int i=0;i<dstart;i++)
      if (arg.equals(names[i])) 
      { addPattern(i); }
    }
   else return super.action(evt,arg);
   return true;
  }

static public void main(String[] argv)
  {
       double vr=25,vg=25,vb=25;
       String s=null;
       if (argv.length>0) s=argv[0];
                    else  s="test.jpg";
       if (argv.length==4) 
         {  vr=Elib.toInt(argv[1]);
            vg=Elib.toInt(argv[2]);
            vb=Elib.toInt(argv[3]);
         }

       ImageProcess ip=new ImageProcess(s,vr,vg,vb); 
       //ip.setBounds(100,100,450,350);
       //ip.show();
  }


public boolean imageUpdate(Image img,int infoflags,int x,int y,int width,int height)
 {
  if ((infoflags & ImageObserver.ALLBITS) != 0)
   { // image is complete
      wi=img.getWidth(null);
      he=img.getHeight(null);
      System.out.println("HEYY im size:"+wi+"x"+he);
      im_done=true;
      Point p=cv.getLocation();
      Dimension cpd=cp.getSize();
      ///System.out.println(d.width+" x " +d.height+" image:"+wi+" x "+he);
      //System.out.println("panel size:"+cpd.width+"x" +cpd.height+" canvas pos:"+p.x+","+p.y+"  image size:"+wi+" x "+he);
      //setBounds(0,0,wi+p.x+cpd.width,he+p.y+cpd.height);
      if (wi+100<512 || he+200<512)  setBounds(0,0,512,512);
      else
      setBounds(0,0,wi+100,he+200);
      show();
      cv.repaint();
      initimg();
      return false;
   }
         return true; // want more info
 }

public void repaint()
{  System.out.println("Frame repaint!");
  paint(getGraphics());
}

public void paint(Graphics g)
{  System.out.println("Frame PAINT!");
}


 public String compactbitcode(int tot,int i)
 {String s="";
  for (int k=0;k<tot;k++)
  {
   if (i%2==1) s=" 1"+s;
          else s=" 0"+s;
   i/=2;
  }
  s+=" ";
  return s;
 }

 public String bitcode(int tot,int i)
 {String s="";
  for (int k=0;k<tot;k++)
  {
   if (k==i) s=" 1"+s;
        else s=" 0"+s;
  }
  s+=" ";
  return s;
 }


 public void addPattern(int i)
 {
  if (pats==null || !recording) return;
  String s="# "+names[i]+" "+i+"\n"+
           pickR/255.0+" "+pickG/255.0+" "+pickB/255.0+"   "+bitcode(Zdim,i);
  System.out.println("pattern to be added:"+s);
  pats.addElement(s+"\n");
  patc++;
  setTitle("Added Pattern:"+patc+" (as "+names[i]+")");
 }


 public void saveRecorded(String fn)
 {
   int k=0;
   Xdim=Elib.toInt(input.getText());
   Ydim=Elib.toInt(hidden.getText());
   Zdim=Elib.toInt(output.getText());
   try {
   DataOutputStream patout = openfileWRITE(fn);
   patout.writeBytes("# This pattern file is generated by ImageProcess (Erhan Oztop -Jan'00)\n");
   patout.writeBytes("outputdim  "+Zdim+"\nhiddendim  "+Ydim+"\ninputdim   "+Xdim+"\n\n");
   patout.writeBytes("#these are optional network settings. If not supplied defaults will be used\n");
   patout.writeBytes("\nlearningrate       "+learningrate );
   patout.writeBytes("\nmomentum           "+momentum);
   patout.writeBytes("\nlearningincrease   "+learningincrease);
   patout.writeBytes("\nlearningdecrease   "+learningdecrease+"\n");

 
  Enumeration f=pats.elements();
   while ( f.hasMoreElements())
   { String s=(String)f.nextElement();
     patout.writeBytes(s);
     //System.out.println("WROTE:"+s);
     k++;
   }
    patout.close();
  } catch (IOException e)
  { System.err.println("closePattern : EXCEPTION "+e);
   }
  System.out.println("Wrote "+k+"/"+patc+" patterns in "+fn);
  System.out.println("Also wrote learning related parameters. You may change them manualy");
 }

//pause recording
public void closePattern()
 { recording=false; }

//start or resume recording
public void startPattern()
 {
  if (recording) System.out.println("Already recording!");
  if (pats==null) System.out.println("Creating new Pattern list");
  if (pats==null) pats=new Vector(40);
  recording=true;
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



 public void clearPattern()
 {
  pats=null;
  patc=0;
 }
}

class IPcanvas extends Canvas
{
 public int wi,he,midx,midy;
 
 ImageProcess owner=null;

 public IPcanvas(ImageProcess owner)
 { 
  this.owner=owner;
 }

public void clear()
{
 Graphics g=getGraphics();
 g.clearRect(0,0,wi,he);
}

public void drawit(Graphics g)
{
  g.drawImage(owner.im,0,0,null);

}

public void repaint()
{  System.out.println("Canvas repaint!");
  paint(getGraphics());
}

public void paint(Graphics g)
{  System.out.println("Canvas PAINT!");
   if (owner.im_done) drawit(g);
   if (owner.splitdone) owner.showColors();
}

}


class Node
{
 int x,y,wi,he;
 double  maxR,maxG,maxB,maxBR;
 double mR,mG,mB;
 double vR,vG,vB;
 Node[] kids=null;
 Node parent=null;
 int kidc=0;
 
 Node(Node par,int x, int y,int wi, int he)
 {
//  if (par==null) {
//     Graphics g=ImageProcess.top.cv.getGraphics();
//     g.setColor(Color.white);
//     g.drawRect(30,30,300,500);
//}

  parent=par; 
  this.x=x;
  this.y=y;
  this.wi=wi;
  this.he=he;
  //System.out.println("New:"+x+","+y+"  "+wi+"x"+he); 
}

 void addKid(Node n)
 {
  if (kidc>=4) System.err.println("MAX 4 kids allowed!!");
  if (kids==null) kids=new Node[4];
  kids[kidc++]=n;
 }

 void split()
 {
  if (wi<2 || he <2) return;
  if (kids!=null) System.err.println("Already has kids!!");
  Node k0=new Node(this,x      , y      ,wi/2,he/2);
  Node k1=new Node(this,x+wi/2 , y      ,wi-wi/2,he/2);
  Node k2=new Node(this,x+wi/2 , y+he/2 ,wi-wi/2,he-he/2);
  Node k3=new Node(this,x      , y+he/2 ,wi/2,he-he/2);
  addKid(k0);
  addKid(k1);
  addKid(k2);
  addKid(k3);
 }

 void mean()
 {
  double sumR=0,sumG=0,sumB=0;
  int c=0;
  double t=0,r,g,b;
  ImageProcess top=ImageProcess.top;
  maxR=top.R[y*top.wi+x]; 
  maxG=top.G[y*top.wi+x]; 
  maxB=top.B[y*top.wi+x]; 
  maxBR=maxR+maxG+maxB;
  for (int xx=x;xx<x+wi;xx++)
    for (int yy=y;yy<y+he;yy++)
     { 
       r=top.R[yy*top.wi+xx];
       g=top.G[yy*top.wi+xx];
       b=top.B[yy*top.wi+xx];
       sumR+=r;
       sumG+=g;
       sumB+=b;
       c++;
       t=r+g+b; 
       if (t>maxBR) {maxBR=t; maxR=r; maxG=g; maxB=b;}
     }
  mR=sumR/c;
  mG=sumG/c;
  mB=sumB/c;
 }

//!! must have means calculated before!!
 void var()
 {
  double sumR=0,sumG=0,sumB=0;
  int c=0;
  ImageProcess top=ImageProcess.top;
  
  for (int xx=x;xx<x+wi;xx++)
    for (int yy=y;yy<y+he;yy++)
     { 
       sumR+=Elib.sqr(top.R[yy*top.wi+xx]-mR);
       sumG+=Elib.sqr(top.G[yy*top.wi+xx]-mG);
       sumB+=Elib.sqr(top.B[yy*top.wi+xx]-mB);
       c++;
     }
  vR=sumR/(c-1);
  vG=sumG/(c-1);
  vB=sumB/(c-1);
 }

       


 void split(double vtR,double vtG,double vtB,int depth)
 { boolean divide=true; 
   if (depth<0) 
   {
     mean();
     var();
     if (!(vR>vtR || vG>vtG || vB>vtB)) divide=false;
   }

   if (divide)
   {
    split();
    for (int i=0;i<kidc;i++) kids[i].split(vtR,vtG,vtB,depth-1);
   } else 
   { 
     Graphics g=ImageProcess.top.cv.getGraphics();
     g.setColor(Color.white);
     g.drawRect(x,y,wi,he); 
 //    g.setColor(Color.red);
 //    g.drawRect(x-1,y-1,wi+2,he+2); 
     ImageProcess.top.histit(this);
   }
        
 }
}




























/*
 *
 * Erhan Oztop, 2000-2002  <br>
 * Source code by Erhan Oztop (erhan@atr.co.jp) <br>
 * Copyright August 2002 under <br>
 * University of Southern California Ph.D. publication copyright <br>
 */
 
