 
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

class Pentagon 
 {
  int N=5; int SN=12;
  double cen_ang; 
  double cen_he,cen_headd,he3d;
  Point3d[][] cor;

  public Pentagon(double R)
  {
   cen_ang=2*Math.PI/N;
   cen_he=R*Math.cos(cen_ang/2);
   cen_headd=cen_he+R*Math.sin(cen_ang/4);

   he3d=(cen_he+R)*Math.sin(63.4352*Math.PI/180)+
        cen_headd*Math.sin(63.4352*Math.PI/180);
   cor=new Point3d[SN][N];
   Point3d around;
   around=new Point3d(cen_he*2*Math.cos(cen_ang/2),
                    cen_he*2*Math.sin(cen_ang/2),0);

   cor[0]=planar(R,new Point3d(0,0,0),0);
   for (int i=1;i<6;i++)
    {
   around=new Point3d(cen_he*2*Math.cos((i-1)*cen_ang+cen_ang/2),
                       cen_he*2*Math.sin((i-1)*cen_ang+cen_ang/2),0);

     cor[i]=planar(R,around,cen_ang/2+(i-1)*cen_ang);
     //cor[i]=planar(R,around,0);
     
    }
  
  for (int i=0;i<5;i++)
  {
   Point3d p0=cor[0][i];
   Point3d p1=cor[0][(i+1)%(N)];
   for (int j=0;j<N;j++)
    { 
      VA._Lrotate(cor[i+1][j],p1,p0,63.4352*Math.PI/180);
    }
  } 

//--second half
   //double upz=2*cen_he*Math.sin(63.4352*Math.PI/180)+
   //           cen_headd*Math.sin(63.4352*Math.PI/180);
   double upz=he3d;
   cor[6]=planar(R,new Point3d(0,0,upz) ,cen_ang/2);
   for (int i=1;i<6;i++)
    {
   around=new Point3d(cen_he*2*Math.cos((i-0)*cen_ang),
                       cen_he*2*Math.sin((i-0)*cen_ang),upz);

     cor[i+6]=planar(R,around,(i-1)*cen_ang);
     //cor[i+6]=planar(R,around,0);
     
    }
 
  for (int i=0;i<5;i++)
  {
   Point3d p0=cor[0+6][i];
   Point3d p1=cor[0+6][(i+1)%(N)];
   for (int j=0;j<N;j++)
    {
      VA._Lrotate(cor[i+1+6][j],p1,p0,-63.4352*Math.PI/180);
    }
  } 


  System.out.println("Points");
  int k=0;
  for (int i=0;i<6+6;i++)
  {
    for (int j=0;j<N;j++)
     {
      System.out.println("    "+cor[i][j].x+" "+cor[i][j].y+" "+cor[i][j].z);
      k++;
     }
  }
  System.out.println("EndPoints");
  System.out.println("Planes");
   k=0;
  for (int i=0;i<6+6;i++)
  {
    for (int j=0;j<5;j++)
     {
      System.out.print("BASE "+k+" ");
      k++;
     }
/*
     System.out.println("");
     k--; k--;
     for (int j=2;j<5;j++)
     {
      System.out.print("BASE "+k+" ");
      k++;
     }
      System.out.print("BASE "+(k-5)+" ");
*/

    System.out.println("");
  }

 }
 
  public Point3d[] planar(double R, Point3d pos, double angleTilt) 
  {Point3d[] c=new Point3d[N]; 
   for (int i=0;i<N;i++)
     {
      c[i]=new Point3d(pos.x+R*Math.cos(angleTilt+i*cen_ang),
                       pos.y+R*Math.sin(angleTilt+i*cen_ang),
                       pos.z+0);
     }
   return c;
  }


  public static void main(String[] argv)
  {
     String s=null;
     double rad=200;
     int sidec=5;
     if (argv.length==1)
          { //sidec=Elib.toInt(argv[1]);
           rad=Elib.toDouble(argv[1]);
          }

     Pentagon pent=new Pentagon(rad);
  }    
 }
 
 
/*
 *
 * Erhan Oztop, 2000-2002  <br>
 * Source code by Erhan Oztop (erhan@atr.co.jp) <br>
 * Copyright August 2002 under <br>
 * University of Southern California Ph.D. publication copyright <br>
 */
 
