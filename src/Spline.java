 
import java.lang.*;
import java.util.*;
/**
 *
 * @author Erhan Oztop, 2001-2002 <br>
 * <br>
 * Source code by Erhan Oztop (erhan@atr.co.jp) <br>
 * Copyright August 2002 via <br>
 * University of Southern California Ph.D. publication copyright <br>
 */

public class Spline 
{
 static int IDgenerator=0;
 int ID;
 public String name="unnamedspline";
 double[] t,y,h,b,z,A,B,C,u,v;
 int n;

/* 
  inputs:  k: # points
           x: x points
           y: y points
*/
 public Spline(int k, double[] x,double[] y) 
 { 
  ID =IDgenerator++; name="spline"+ID;
  setup(k,x,y);
 }

 public void setLabel(String s)
 { name=s; }
 public String getLabel()
 { return name;}

 private void setup(int k,double[] x,double[] yy)
 {
  h=new double[k-1];  // one less element than t & y
  b=new double[k-1];  // one less element than t & y
  u=new double[k-1];  // one less element than t & y
  v=new double[k-1];  // one less element than t & y
  z=new double[k];
  t=new double[k];
  y=new double[k];
  A=new double[k];
  B=new double[k];
  C=new double[k];
  
  for (int i=0;i<k;i++) {t[i]=x[i]; y[i]=yy[i];}
 
  n=k-1; //last index of t & y is k-1
  findCoef();
 }
 
 public Spline(int k, double[] y)
 {
  ID =IDgenerator++; name="spline"+ID;
  double x[]=new double[k];
  double step=1.0/(k-1);
  for (int i=0;i<k;i++)
     x[i]=i*step;
  setup(k,x,y);
 }

 public double[][] makeArray(double from, double to,int points)
 {
  double step=(to-from)/(points-1);
  double[][] A=new double[points][2];
  for (int i=0;i<points;i++)
   { 
     A[i][0]=step*i;
     A[i][1]=eval(step*i);
   }
  return A;
 }

    public double[][] convertPoints() {
	double[][] A=new double[n+1][2];
	for (int i=0;i<=n;i++) { A[i][0]=t[i]; A[i][1]=y[i]; }
	return A;
    }

 public void org_showSpline(double from, double to,int points)
 {
  double[][] A=makeArray(from,to,points);
  Gplot gp=new Gplot(A,points,2);
 }


 public void showSpline(double from, double to,int points)
 {

  double[][] A=convertPoints();
  Gplot gp=new Gplot(A,n,2);
 }

 private void findCoef()
 {
  

  for (int i=0;i<=n-1;i++)
    { h[i]=t[i+1]-t[i];
      b[i]=6*(y[i+1]-y[i])/h[i];
    }

 u[1]=2*(h[0]+h[1]);
 v[1]=b[1]-b[0];
 
 for (int i=2; i<=n-1; i++)
 { u[i]=2*(h[i]+h[i-1])-h[i-1]*h[i-1]/u[i-1];
   v[i]=b[i]-b[i-1]-h[i-1]*v[i-1]/u[i-1];
 }
 z[n]=0;  // we assing 0 to last 2nd derivative (i.e. natural cubic spline)
 z[0]=0;  // we assing 0 to last 1st derivative (i.e. natural cubic spline)
 for (int i=n-1;i>=1;i--) 
   z[i]=(v[i]-h[i]*z[i+1])/u[i];

 //System.out.println("oops done");
 // now lets compute some coef for eval convenience
  for (int i=0;i<n ; i++)
  {
   A[i]=(z[i+1]-z[i])/(6*h[i]);
   B[i]=z[i]/2;
   C[i]=-h[i]*z[i+1]/6-h[i]*z[i]/3+(y[i+1]-y[i])/h[i];
  }
 }

 public int position(double x)
 {
  if (x<t[0]) return 0;
  for (int i=1;i<=n;i++) 
   if (x<t[i]) return i-1;
  return n-1;
 }

 public double eval(double x)
 {
  int i=position(x);
  return y[i]+(x-t[i])*(C[i]+(x-t[i])*(B[i]+(x-t[i])*A[i]));
 } 

 public double eval (double x,int i)
 {
  return y[i]+(x-t[i])*(C[i]+(x-t[i])*(B[i]+(x-t[i])*A[i]));
 } 

 public double evalD1(double x)
 {
  int i=position(x);
  return -(t[i+1]-x)*(t[i+1]-x)*z[i]/(2*h[i]) +
          (x-t[i])*(x-t[i])*z[i+1]/(2*h[i])   +
          (y[i+1]-y[i])/h[i] + (z[i]-z[i+1])*h[i]/6  ;
 }

 public double evalD2(double x)
 {
  int i=position(x);
  return (t[i+1]-x)*z[i]/h[i] + (x-t[i])*z[i+1]/h[i]; 
 }

 public double evalInt(double x)
 {
  int i=position(x);
  return -(t[i+1]-x)*(t[i+1]-x)*(t[i+1]-x)*(t[i+1]-x)*z[i]/(24*h[i]) +
          (x - t[i])*(x - t[i])*(x - t[i])*(x - t[i])*z[i+1]/(24*h[i]) +
          (y[i+1]/h[i]-z[i+1]*h[i]/6)*0.5*(x-t[i]) 
        - (y[i]/h[i]-z[i]*h[i]/6)*0.5*(t[i+1]-x) ;
 }
 
static synchronized void showSplines_with(Spline[] traj,int k,int N,String fname,String with) 
 {
     System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
  String base="/tmp/";
  String command="";
  
  command+="plot [0:1] [-0.1:1.6] ";
  for (int i=0;i<k;i++)
  {
      double[][] A=traj[i].convertPoints(); N=traj[i].n+1;
      String fn=base+traj[i].name;
      Elib.array2file(A,N,2,fn);
  
      command+="\""+fn+"\" "+with;
   if (i!=k-1) command+=",";
  }
  //command+="x";
  
  System.out.println("GNUPLOT command:\n"+command);
  Gplot gp=new Gplot(command,fname);
//    if (HV.recordGif) {
//        System.out.println("WRITING GRAPH!!!!!");
//        HV.writeGraph();  
//    }

 
}
 static synchronized void showSplines(Spline[] traj,int k,int N,String fname) 
 {
     System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
  String base="/tmp/";
  String command="";
  
  command+="plot [0:1] [-0.1:1.6] ";
  for (int i=0;i<k;i++)
  {
      //double[][] A = traj[i].makeArray(0,1,N);
      double[][] A=traj[i].convertPoints(); N=traj[i].n+1;
      String fn=base+traj[i].name;
      Elib.array2file(A,N,2,fn);
  
      command+="\""+fn+"\" with lines";
      if (i!=k-1) command+=",";
  }
  //command+="x";
  
  System.out.println("GNUPLOT command:\n"+command);
  Gplot gp=new Gplot(command,fname);
//    if (HV.recordGif) {
//        System.out.println("WRITING GRAPH!!!!!");
//        HV.writeGraph();  
//    }

 
}

 static void showSplines_with(Spline[] traj,int N,String with)
 {
  showSplines_with(traj,traj.length,N,null,with);
 }

 static void showSplines(Spline[] traj,int N)
 {
  showSplines(traj,traj.length,N,null);
 }

 static void showSplines(Spline[] traj,String gname)
 {
  showSplines(traj,traj.length,50,gname);
 }
}
/*
 *
 * Erhan Oztop, 2000-2002  <br>
 * Source code by Erhan Oztop (erhan@atr.co.jp) <br>
 * Copyright August 2002 under <br>
 * University of Southern California Ph.D. publication copyright <br>
 */
 
