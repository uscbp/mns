 
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

class Line 
{
 public Point[] r;  //projected
 public Point3d[] P;  //3d
 public Point3d CP; // 3d center
 public Point   Cr; // 2d center
 public Point3d normal; //normal
 int N;

 public int line_color=1;
 public double  depth;     // what is the depth wrt an eye


 public Line(Point3d P0,Point3d P1,int col)
 {
  this(P0,P1,new Point(),new Point(),col);
 }

 public Line(Point3d P0,Point3d P1, Point r0  ,Point r1,int col)
 { 
  N=2;
  P=new Point3d[N];
  r=new Point[N];
  this.P[0]=P0; this.P[1]=P1; 
  this.r[0]=r0; this.r[1]=r1;
  CP=new Point3d();
  Cr=new Point();
  line_color=col;
  setCenter();
 }


 public void set2dCenter()
 {
  Cr.x=0; Cr.y=0;
  for (int i=0;i<N;i++)
   { Cr.x+=r[i].x; Cr.y+=r[i].y; }
  Cr.x/=N; Cr.y/=N;
 }

 public void setCenter()
 {
  CP.x=0; CP.y=0; CP.z=0;
  for (int i=0;i<N;i++)
   { CP.x+=P[i].x; CP.y+=P[i].y; CP.z+=P[i].z; }
  CP.x/=N; CP.y/=N; CP.z/=N;
 }


 public String str()
 { 
  String s="Line[";
  for (int i=0;i<N;i++)
     s+=P[i].str()+" ";
  return s;
 }


 public void setColor(int fill,int line)
 {
  line_color=line;
 } 

   

}
/*
 *
 * Erhan Oztop, 2000-2002  <br>
 * Source code by Erhan Oztop (erhan@atr.co.jp) <br>
 * Copyright August 2002 under <br>
 * University of Southern California Ph.D. publication copyright <br>
 */
 
