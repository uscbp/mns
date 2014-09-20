
 
import java.awt.*;
/**
 *
 * @author Erhan Oztop, 2001-2002 <br>
 * <br>
 * Source code by Erhan Oztop (erhan@atr.co.jp) <br>
 * Copyright August 2002 via <br>
 * University of Southern California Ph.D. publication copyright <br>
 * <b> This class is a collection of static Vector Algebra methods </b>
*/
public class VA
{

static Point3d center(Point3d p0,Point3d p)
 { return new Point3d(0.5*(p0.x+p.x),0.5*(p0.y+p.y),0.5*(p0.z+p.z));}

static Point3d symetric(Point3d org,Point3d p)
 { return new Point3d(2*org.x-p.x,2*org.y-p.y,2*org.z-p.z);}

/**
* Returns distance between (int) points (x0,y0),(x1,y1). 
*/
 static double dist(int x0,int y0,int x1,int y1)
 { return Math.sqrt((double)((x0-x1)*(x0-x1)+(y0-y1)*(y0-y1)));
 }

/**
* Returns square of the distance between (int) points (x0,y0),(x1,y1).
*/
 static double dist2(int x0,int y0,int x1,int y1)
 { return (double)((x0-x1)*(x0-x1)+(y0-y1)*(y0-y1));
 }

/**
* Returns distance between 2d two points.
*/
 static double dist(Point p0, Point p1)
 { return Math.sqrt((double)((p0.x-p1.x)*(p0.x-p1.x)+(p0.y-p1.y)*(p0.y-p1.y)));
 }


/**
*  Returns distance between points (x0,y0),(x1,y1). 
*/
 static double dist(double x0,double y0,double x1,double y1)
 {
   return Math.sqrt((x0-x1)*(x0-x1)+(y0-y1)*(y0-y1));
 }

/**
 * Returns distance between points p1 & p2 of Point3d class. 
*/
 static double dist(Point3d p1,Point3d p2)
 {
   return Math.sqrt( (p1.x-p2.x)*(p1.x-p2.x) +
                     (p1.y-p2.y)*(p1.y-p2.y) +
                     (p1.z-p2.z)*(p1.z-p2.z) );
 }

/**
 * Returns the square of the distance between points p1 & p2 of Point3d class.
*/
 static double dist2(Point3d p1,Point3d p2)
 {
   return  (p1.x-p2.x)*(p1.x-p2.x) +
           (p1.y-p2.y)*(p1.y-p2.y) +
           (p1.z-p2.z)*(p1.z-p2.z) ;
 }

 static void _resize(Point3d p0,Point3d p1,double sc)
 {
  Point3d mid=new Point3d( 0.5*(p0.x+p1.x), 0.5*(p0.y+p1.y), 0.5*(p0.z+p1.z));
  VA._subtract(p0,mid);
  VA._subtract(p1,mid);
  VA._scale(p0,sc);
  VA._scale(p1,sc);
  VA._add(p0,mid);
  VA._add(p1,mid);
 }
/** Returns a new Point3d equal to sc*p1 */
 static Point3d scale(Point3d p1,double sc)
 { 
  return new Point3d(p1.x*sc, p1.y*sc,p1.z*sc);
 }

/**  scales p1 to be equal to sc*p1 */
 static void _scale(Point3d p1,double sc)
 { 
  p1.x*=sc; p1.y*=sc;p1.z*=sc;
 }

/** Returns a new Point3d equal p1+(dx,dy,dz) */
 static Point3d translate(Point3d p1,double dx,double dy,double dz)
 {
  return new Point3d(p1.x+dx, p1.y+dy, p1.z+dz);
 }

/** translates p1 by (dx,dy,dz) */
 static void _translate(Point3d p1,double dx,double dy,double dz)
 {
  p1.x+=dx; p1.y+=dy; p1.z+=dz;
 }

/** Returns p1+p2 */
 static Point3d add(Point3d p1,Point3d p2)
 {
  return new Point3d(p1.x+p2.x, p1.y+p2.y, p1.z+p2.z);
 }

/** Modifies p1 so that p1=p1+p2 */
 static void _add(Point3d p1,Point3d p2)
 {
   p1.x+=p2.x;
   p1.y+=p2.y;
   p1.z+=p2.z;
 }

    static double[][] transpose(double[][] M) {
	int m=M.length;
	int n=M[0].length;
	double [][] Q=new double[n][m];
	for (int i=0;i<m;i++)
	    for (int j=0;j<n;j++)
		Q[j][i]=M[i][j];
	return Q;
    }

/** Returns p1-p2 */
 static Point3d subtract(Point3d p1,Point3d p2)
 {
  return new Point3d(p1.x-p2.x, p1.y-p2.y, p1.z-p2.z);
 }

/** Modifies p1 so that p1=p1-p2 */
 static void _subtract(Point3d p1,Point3d p2)
 {
   p1.x-=p2.x;
   p1.y-=p2.y;
   p1.z-=p2.z;
 }

/** Returns p1xp2. (cross product of p1 & p2) */
 static Point3d cross(Point3d p1,Point3d p2)
 { double x,y,z;
   x = p1.y*p2.z - p1.z*p2.y;
   y = p1.z*p2.x - p1.x*p2.z;
   z = p1.x*p2.y - p1.y*p2.x;
   return new Point3d(x,y,z);
 }


/** Returns the normal of the plane spanned by p1-p2,p2-p3 */
 static Point3d normal(Point3d p1,Point3d p2,Point3d p3)
 { double x,y,z;
   Point3d v1=subtract(p1,p2);
   Point3d v2=subtract(p2,p3);
   return cross(v1,v2);
 }



/** Returns p1.p2. (inner product of p1 & p2) */
 static double inner(Point3d p1,Point3d p2)
 {
  return (p1.x*p2.x)+(p1.y*p2.y)+(p1.z*p2.z);
 } 

/** Returns |p1|. (norm -length- of p1) */
 static double norm(Point3d p1)
 {
  return Math.sqrt((p1.x*p1.x)+(p1.y*p1.y)+(p1.z*p1.z));
 }

/** Returns the cosine of the angle between p1 & p2. A zero vector
    will result a zero angle A zero vector
    will result a zero angle that is 1 will be returned.*/
 static double cos(Point3d p1,Point3d p2)
 {
  double l=norm(p1)*norm(p2);
  if (l==0) return 1;
  else return inner(p1,p2)/l ;
 }

/** Projection of vector a on b. */
 static Point3d proj(Point3d b, Point3d a)
 {
  double n=norm(b);
  if (n==0) return new Point3d(0,0,0);
  Point3d u=normalize(b);
  double comp=inner(a,b)/n;
  _scale(u,comp);
  return u;
 }
 static public double cos(Point3d p1,Point3d p0, Point3d q1,Point3d q0)
 {
  Point3d pv=new Point3d(p1.x-p0.x, p1.y-p0.y,0);
  Point3d qv=new Point3d(q1.x-q0.x, q1.y-q0.y,0);
  return cos(pv,qv);
 }


 static public double cos(Point p1,Point p0, Point q1,Point q0)
 {
  Point3d pv=new Point3d(p1.x-p0.x, p1.y-p0.y,0);
  Point3d qv=new Point3d(q1.x-q0.x, q1.y-q0.y,0);
  return VA.cos(pv,qv);
 }

 static public double acos(Point3d p1,Point3d p2)
 {
     double c=cos(p1,p2);
     if (Math.abs(c)>1) { 
	 if (HV.DLEV>0)
	 System.err.println("Math roundoff error cos(p1,p2)="+c+" >1,assuming +-1.");
	 if (c>0) return 0;
	 else return Math.PI;
     }
     return Math.acos(c);
 }


 static public double acos(Point3d p1,Point3d p0, Point3d q1,Point3d q0)
 {
  Point3d pv=new Point3d(p1.x-p0.x, p1.y-p0.y,0);
  Point3d qv=new Point3d(q1.x-q0.x, q1.y-q0.y,0);
  return Math.acos(cos(pv,qv));
 }


 static public double acos(Point p1,Point p0, Point q1,Point q0)
 {
  Point3d pv=new Point3d(p1.x-p0.x, p1.y-p0.y,0);
  Point3d qv=new Point3d(q1.x-q0.x, q1.y-q0.y,0);
  return Math.acos(VA.cos(pv,qv));
 }

 static public double acosD(Point3d p1,Point3d p0, Point3d q1,Point3d q0)
 {
  Point3d pv=new Point3d(p1.x-p0.x, p1.y-p0.y,0);
  Point3d qv=new Point3d(q1.x-q0.x, q1.y-q0.y,0);
  return Math.acos(cos(pv,qv))*180/Math.PI;
 }


 static public double acosD(Point p1,Point p0, Point q1,Point q0)
 {
  Point3d pv=new Point3d(p1.x-p0.x, p1.y-p0.y,0);
  Point3d qv=new Point3d(q1.x-q0.x, q1.y-q0.y,0);
  return Math.acos(VA.cos(pv,qv))*180/Math.PI;
 }


/** Returns a Point3d equal to (p,0) */
 static Point3d promote3d(Point p)
 {
  return new Point3d(p.x, p.y, 0);
 }

/** Normalizes p */
 static public void _normalize(Point3d p)
 {
  double l=norm(p);
  if (l!=0) { p.x/=l; p.y/=l; p.z/=l;}
 }

/** Returns a normalized version of p*/
 static public Point3d normalize(Point3d p)
 {
  double l=norm(p);
  Point3d q=null;
  if (l==0) q=new Point3d(0,0,0);
       else q=new Point3d(p.x/l,p.y/l,p.z/l);
  return q;
 }

/** Check for zero vector. If zero, true is returned else false is returned.*/
 static public boolean zero(Point3d p1)
 {
  if (p1.x==0 && p1.y==0 && p1.z==0) return true;
  return false;
 }

/** Returns the angle determined by given cosine and sine. 
 *  Returned angle is in the range [-pi,pi]
*/
 static public double cosSin(double cosx,double sinx)
 {
  double a=Math.acos(cosx);
  if (sinx>=0) return a;
  else return -a;
 }

/** Returns rotated version of p  around Z axis by t radians */
 static public Point3d Zrotate(Point3d p,double t)
 {
    double xx= p.x*Math.cos(t) - p.y*Math.sin(t) ;
    double yy= p.x*Math.sin(t) + p.y*Math.cos(t) ;
    return new Point3d(xx,yy,p.z);
 }

/** Returns rotated version of p  around X axis by t radians */
 static public Point3d Xrotate(Point3d p,double t)
 {
    double zz= p.z*Math.cos(t) + p.y*Math.sin(t) ;
    double yy=-p.z*Math.sin(t) + p.y*Math.cos(t) ;
    return new Point3d(p.x,yy,zz);
 }

/** Returns rotated version of p  around Y axis by t radians */
 static public Point3d Yrotate(Point3d p,double t)
 {
    double xx= p.x*Math.cos(t) + p.z*Math.sin(t) ;
    double zz=-p.x*Math.sin(t) + p.z*Math.cos(t) ;
    return new Point3d(xx,p.y,zz);
 }

/** Rotate  p  around Z axis by t radians */
  static public void _Zrotate(Point3d p,double t)
 {
    double xx= p.x*Math.cos(t) + -p.y*Math.sin(t) ;
    double yy= p.x*Math.sin(t) + p.y*Math.cos(t) ;
    p.x=xx;
    p.y=yy;
 }

/** Rotate  p  around X axis by t radians */
 static public void _Xrotate(Point3d p,double t)
 {
    double zz= p.z*Math.cos(t) + p.y*Math.sin(t) ;
    double yy=-p.z*Math.sin(t) + p.y*Math.cos(t) ;
    p.y=yy;
    p.z=zz;
 }

/** Rotate  p  around Y axis by t radians */
 static public void _Yrotate(Point3d p,double t)
 {
    double xx= p.x*Math.cos(t) + p.z*Math.sin(t) ;
    double zz=-p.x*Math.sin(t) + p.z*Math.cos(t) ;
    p.x=xx;
    p.z=zz;
 }


 static public Point3d zap2Y(Point3d n)
 {
  double Xang=0,cosK=0,sinK=0,Yang=0,cosT=0,sinT=0;
  double bot1=Math.sqrt(n.x*n.x+n.z*n.z);
  if (bot1==0) {Yang=0;}
  else {
  cosT=n.z/bot1;
  sinT=n.x/bot1;
  Yang=cosSin(cosT,sinT);
  if (Math.abs(Math.sin(Yang)-sinT)>0.0001) System.out.println("===========> cosT,Yang:"+cosT+","+Yang);
  if (Math.abs(Math.cos(Yang)-cosT)>0.0001) System.out.println("===========> sinT,Yang:"+sinT+","+Yang);
  //if (sinT<=0) Yang=2*Math.PI-Yang;
  
  }
  Point3d np=Yrotate(n,-Yang);
  /*if (np.z>0) System.out.print("+");
    else if  (np.z<0) System.out.print("-");
      else System.out.print("0");
*/

  if (Math.abs(np.x)>=0.0001) System.err.println("------------> np:"+np.str()+
                             "\n n:"+n.str());
  
  double bot2=Math.sqrt(n.y*n.y+bot1*bot1);
  Xang=Math.acos(np.y/bot2);

   _Xrotate(np,-Xang);

  if (Math.abs(np.x)>=0.0001 || Math.abs(np.z)>=0.0001) System.err.println("##############> np:"+np.str()+ "\n n:"+n.str());
  //System.out.println("FUCK"+np.y);

/*
  if (Mars.debug>0) 
    System.out.println("(Yang,Xang) :"+Elib.nice(Yang,1e3)+","+
                                       Elib.nice(Xang,1e3)+"    "+n.str());
*/
/*
                                                  Elib.nice(cosT,1e3)+","+
                                                  Elib.nice(cosK,1e3));
*/
  return new Point3d(-Xang,-Yang,0); 
 }
 
/**
* Returns the rotated version of p around n by t radians. 
* The parameters are unchanged.
*/
 static public Point3d rotate(Point3d p, Point3d n,double t)
 {
  Point3d angle=zap2Y(n);
  //System.out.println("Zap angle:"+angle.str());
  Point3d py =Yrotate(p ,angle.y);
  Point3d pyx=Xrotate(py,angle.x);
  Point3d qyx=Yrotate(pyx,t);
  Point3d qy =Xrotate(qyx,-angle.x);
  Point3d q =Yrotate(qy,-angle.y);
  return q;
 } 

/**
* Rotates p around n by t radians. Note that this is slow and uses
* rotate(). Instead _rotate() should be used.
* @see #_rotate 
* @see #rotate 
*/
 static public void _slowrotate(Point3d p, Point3d n,double t)
 {Point3d q=rotate(p,n,t);
  p.x=q.x;
  p.y=q.y;
  p.z=q.z;
 }

/**
* rotates p around n by t radians. p modified 
*/
 static public void _rotate(Point3d p, Point3d n,double t)
 {
  Point3d angle=zap2Y(n);
  _Yrotate(p ,angle.y);
  _Xrotate(p,angle.x);
  _Yrotate(p,t);  
  _Xrotate(p,-angle.x);
  _Yrotate(p,-angle.y);
 }

/**
* Rotates p around the line passing from p2 to p1 by t radians.
*/
 static public void _Lrotate(Point3d p, Point3d p1,Point3d p2,double t)
 {
  Point3d n=subtract(p2,p1); //n=p2-p1;
  _subtract(p,p1);          //p-=p1;
  _rotate(p,n,t);
  _add(p,p1);
 }


/**
* Returns the rotated version of p around the line passing from p2 to p1 by 
* t radians. 
*/
 static public Point3d Lrotate(Point3d p, Point3d p1,Point3d p2,double t)
 {
  Point3d ret=new Point3d(p.x,p.y,p.z);
  Point3d n=subtract(p2,p1); //n=p2-p1;
  _subtract(ret,p1);          //p-=p1;
  _rotate(ret,n,t);
  _add(ret,p1);
  return ret;
 }
 /** a=b. No size check done. The array creatation size of a[][] 
 is used */
static public void setMatrix(double[][] a,double[][] b) {
	for (int i=0;i<a.length;i++) 
	    for (int j=0;j<a[i].length;j++) 
		a[i][j]=b[i][j];
}
 /** a=b. No size check done. rows,cols defines the loop boundry*/ 
static public void setMatrix(double[][] a,double[][] b,int rows,int cols) {
	for (int i=0;i<rows;i++) 
	    for (int j=0;j<cols;j++) 
		a[i][j]=b[i][j];
}

/**Returns the matrix [0   -p.z  p.y; 
	 	       p.z    0 -p x; 
		      -p.y p.x     0]  .
  if R is an orientation matrix then dR/dt is given by
  dR/dt=STAR(angular velocity)*R  where * is matrix multiplication 
*/
static public double[][] STAR(Point3d p) {
  double[][] M=new double[3][3];
  M[0][0]= 0;   M[0][1]=-p.z;   M[0][2]= p.y;
  M[1][0]= p.z; M[1][1]= 0  ;   M[1][2]=-p.x;
  M[2][0]=-p.y; M[2][1]= p.x;   M[2][2]=   0;
  return M;
}


    /** v represents the triple (meridian, parallel, radius); the return is
	the rectangular coordinates. The polor coordinates are defined on a left handed rectangular coordinate system (+x,+y as in 2D +z going into the 
screen)  as : The sphere stayin on the origin. 0 meridian is in +x+y semi-plane
pi/2 meridian is on the +y+z semiplane pi meridian is on -x+y semiplane and 
so on. The 0 parallel is the degenerate circle at the north pole (+y) and
pi parallel is the south pole. pi/2 parallel line is on xz plane. Meridian range is [0:2pi] (or [-pi:pi]) parallel range is [0:pi] */ 
    static public Point3d polor2rect(Point3d v) {
	return new Point3d( v.z * Math.cos(v.x)*Math.sin(v.y) ,
			   v.z * Math.cos(v.y)          ,
			   v.z * Math.sin(v.x)*Math.sin(v.y) );
    }

    static public Point3d polar(double meridian, double parallel, 
double radius) {
	return new Point3d (meridian,parallel,radius);
    }

    static public Point3d rect2polar(Point3d p) {
	double b=Math.sqrt(p.x*p.x+p.z*p.z);
	double r=Math.sqrt(b*b+p.y*p.y);
	double meridian=cosSin(p.x/b,p.z/b);   // -pi : pi
	double parallel=Math.acos(p.y/r);  // 0 : pi
        return new Point3d (meridian,parallel,r);
    }

/** compute r[]=M[][]*v[].  r=Mv, im size of r, jm is size of v (M is imxjm)*/
static public void multiply(double M[][], double[] v, int im, int jm,double r[]) {
    for (int i=0;i<im;i++)
	{  r[i]=0;
        for (int j=0;j<jm;j++) r[i]+=M[i][j]*v[j];
	}
}

/** compute r[]=M[][]*v[]+C.  r=Mv, im size of r, jm is size of v (M is imxjm)*/
static public void multiply(double M[][], double[] v, int im, int jm,double r[],double C) {
    for (int i=0;i<im;i++)
	{  r[i]=0;
        for (int j=0;j<jm;j++) r[i]+=M[i][j]*v[j];
	r[i]+=C;
	}
}

/** compute r=r+Mv, im size of r, jm is size of v (M is imxjm)*/
static public void multiadd(double M[][], double[] v, int im, int jm,double r[]) {
    for (int i=0;i<im;i++)
	{  
        for (int j=0;j<jm;j++) r[i]+=M[i][j]*v[j];
	}
}

/** compute r=r+Mv+C, im size of r, jm is size of v (M is imxjm)*/
static public void multiadd(double M[][], double[] v, int im, int jm,double r[],double C) {
    for (int i=0;i<im;i++)
	{  
        for (int j=0;j<jm;j++) r[i]+=M[i][j]*v[j];
	r[i]+=C;
	}
}
/** M=M+C, the size of the matrices: im x jm */
static public void addto(double M[][], int im,int jm, double[][] C ) {
    for (int i=0;i<im;i++)
    for (int j=0;j<jm;j++) M[i][j]+=C[i][j];
}

} // class VA
/*
 *
 * Erhan Oztop, 2000-2002  <br>
 * Source code by Erhan Oztop (erhan@atr.co.jp) <br>
 * Copyright August 2002 under <br>
 * University of Southern California Ph.D. publication copyright <br>
 */
 
