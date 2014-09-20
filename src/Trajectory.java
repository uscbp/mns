/**
 *
 * @author Erhan Oztop, 2001-2002 <br>
 * <br>
 * Source code by Erhan Oztop (erhan@atr.co.jp) <br>
 * Copyright August 2002 via <br>
 * University of Southern California Ph.D. publication copyright <br>
 */
 
class Trajectory 
{
 int N;
 Spline[] teta;
 Spline TS,F;
 Path X;
 Point3d target;
/*
 public Trajectory(double betas[][],int n)
 {
  N=n; 
  teta=linearSpline(betas,n); 
 }
*/

 public Trajectory(Point3d p[])
 { this(p,p.length);}
 public Trajectory(Point3d p[],int n)
 {
  target=p[n-1].duplicate(); //check this
  N=n;
  X=new Path(p,n);
  System.out.println("Path created. with n:"+n);
  for (double t=0;t<=1;t+=0.05) System.out.println(X.eval(t).str());
TS=X.constStep(1000,0.001);
  //  System.out.println("TS [0,1]:");
  //  for (int i=0;i<50;i++)
  //   System.out.println(TS.eval(i/49.0));

  //TS is a (time) spline such that d|X|/dTS(u) is constant over u in [0,1]
  // now find an f to substutide u (u=f(s)) such that d|X|/ds has a bell
  // profile.
  double[] s=new double[5],f=new double[5];

  s[0]=0;        f[0]=0;
  s[1]=0.25;     f[1]=0.15;
  s[2]=0.5 ;     f[2]=0.5;
  s[3]=0.75;     f[3]=0.9;
  s[4]=1;        f[4]=1;
  F=new Spline(5,s,f);

/*
  s[0]=0;        f[0]=0;
  s[1]=0.5;     f[1]=0.5;
  s[2]=1.0 ;     f[2]=1;
  F=new Spline(3,s,f);
*/
 }

public Trajectory()
{
  double[] s=new double[5],f=new double[5];

  s[0]=0;        f[0]=0;
  s[1]=0.25;     f[1]=0.15;
  s[2]=0.5 ;     f[2]=0.5;
  s[3]=0.75;     f[3]=0.9;
  s[4]=1;        f[4]=1;
  F=new Spline(5,s,f);
  //F.showSpline(0,1,50);
}


// approx, bell shape profile
 public double stretchTime(double t)
 {
  return F.eval(t);
 }


// doing the acces to joint angles through timewrap should create
// the bell shape profile 
 public double timewrap(double t)
 {
  //return TS.eval(F.eval(t));
  return F.eval(t);
 }
  

 static public Segment[] usedseg;

 static public Spline[] jointSpline(Segment[] seg, double beta1[], double beta2[], int n)
 {
  int N;
  double[] x=new double[5],y=new double[5];
  Spline[] traj=new Spline[n];
  usedseg=seg;
  for (int k=0;k<n;k++)
  {

   if (seg[k].userTag==Hand.HANDJOINT)
   {N=0;
    x[N]=0;  y[N++]=beta1[k];
/*
    x[N]=0.3;  y[N++]=(beta1[k]+beta2[k])*0.5;
    x[N]=0.6;  y[N++]=beta2[k];
    x[N]=0.7;  y[N++]=beta2[k]+Math.PI/4;
*/
    x[N]=0.7;  y[N++]=beta2[k]-Math.PI/15; // was /10

    x[N]=1;  y[N++]=beta2[k];
   } 
   else

   { N=3;
     for (int r=0;r<N;r++)
     {
       x[r]=r*(1.0/(N-1));
       y[r]=beta1[k]+r*(beta2[k]-beta1[k])/(N-1);
     }
   }
   traj[k]=new Spline(N,x,y);
  }
  //showSplines(traj,traj.length);
  return traj;
 }

    // This has one via point at midway (ratio-way)
static public Spline[] jointSpline(Segment[] seg, double beta1[], double midbeta[], double beta2[], int n,double ratio)
 {
  int N;
  double[] x=new double[5],y=new double[5];
  Spline[] traj=new Spline[n];
  usedseg=seg;
  for (int k=0;k<n;k++)
  {

      if (seg[k].userTag==Hand.HANDJOINT) //if hand ignore the midbeta
   {N=0;
    x[N]=0;  y[N++]=beta1[k];
/*
    x[N]=0.3;  y[N++]=(beta1[k]+beta2[k])*0.5;
    x[N]=0.6;  y[N++]=beta2[k];
    x[N]=0.7;  y[N++]=beta2[k]+Math.PI/4;
*/
    x[N]=0.7;  y[N++]=beta2[k]-Math.PI/15; // was /10

    x[N]=1;  y[N++]=beta2[k];
   } 
   else {
       N=0;
       x[N]=0;       y[N++]=beta1[k];
       x[N]=ratio;   y[N++]=midbeta[k];
       x[N]=1.0;     y[N++]=beta2[k];
   }
   traj[k]=new Spline(N,x,y);
  }
  //showSplines(traj,traj.length);
  return traj;
 }

static void showSplines(Spline[] traj,int k)
{
  int N=50;
  String base="/tmp/SP_";
  String command="plot ";
  for (int i=0;i<k;i++)
  {
   double[][] A = traj[i].makeArray(0,1,N);
   String fn=base+usedseg[i].label+"="+i;
   Elib.array2file(A,N,2,fn);
   
   command+="\""+fn+"\" with linespoints,";
  }
  command+="x";
  System.out.println("GNUPLOT command:\n"+command);
  Gplot gp=new Gplot(command);

}


 
}
/*
 *
 * Erhan Oztop, 2000-2002  <br>
 * Source code by Erhan Oztop (erhan@atr.co.jp) <br>
 * Copyright August 2002 under <br>
 * University of Southern California Ph.D. publication copyright <br>
 */
 
