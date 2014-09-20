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

class Path
{
 Spline x,y,z;
 static public Point3d[] toArray(Vector v)
 {
  int n=v.size();
  Point3d[] L=new Point3d[n];
  n=0;
  Enumeration e=v.elements();
  while (e.hasMoreElements())
  {
   Point3d p=(Point3d)e.nextElement();
   L[n++]=p.duplicate();
  }
  return L;
 }

 public Path(Point3d[] p,int n)
 {
  double[] val=new double[n];
  double[]   t=new double[n];
  for (int i=0;i<n;i++)
    { val[i]=p[i].x;
        t[i]=i/(n-1.0);
    }
  x=new Spline(n,t,val);

  for (int i=0;i<n;i++)
    val[i]=p[i].y;
  y=new Spline(n,t,val);

  for (int i=0;i<n;i++)
     val[i]=p[i].z;
  z=new Spline(n,t,val);
 }

 public Point3d eval(double t)
 {
  return new Point3d(x.eval(t),y.eval(t),z.eval(t));
 }

 // return a time function u so that Dpath/du is constant
 // delta must very small compared to eps.
 Spline constStep(int Q, double delta)
 {
  double len=length(delta);
  double eps=len/Q;
  System.out.println("Length of curve:"+len+" eps:"+eps);
  if (eps/delta < 10) {System.err.println("Warning you may have precision errors. Reduce delta in call to constStep");}
  double[] D=new double[Q+1];  //+1 in case :)
  Point3d p0,p1;
  double t=0;
  int k=0;
  p0=eval(t);
  D[k++]=t;
  t+=delta;
  while (t<=1)
  {
   p1=eval(t);
   double dis=VA.dist(p1,p0);
   if (dis>eps) { D[k++]=t; p0=p1;}
   t+=delta;
  }
  System.out.println("Asked "+Q+" points for interpolation, got "+k+" points");
  double[] T=new double[k];
  for (int i=0; i<k;i++) T[i]=i/(k-1.0);
  return new Spline(k,T,D);
 }

 /** length of curve integrated using delta.*/
 public double length(double delta)
 {
  Point3d p0,p1;
  double t=0,len=0;
  int k=0;
  p0=eval(t);
  t+=delta;
  while (t<=1)
  {
   p1=eval(t);
   double dis=VA.dist(p1,p0);
   len+=dis; k++;
   p0=p1;
   t+=delta;
  }

  double mean=len/k;
  double var=0;
  t=0;len=0;
  k=0;
  p0=eval(t);
  t+=delta;
  while (t<=1)
  {
   p1=eval(t);
   double dis=VA.dist(p1,p0);
   var+=(mean-dis)*(mean-dis); k++;
   p0=p1;
   t+=delta;
  }
  var/=k;
  System.out.println("Mean steplen:"+mean+" variance:"+var);

  return len;
 }


}


 
/*
 *
 * Erhan Oztop, 2000-2002  <br>
 * Source code by Erhan Oztop (erhan@atr.co.jp) <br>
 * Copyright August 2002 under <br>
 * University of Southern California Ph.D. publication copyright <br>
 */
 
