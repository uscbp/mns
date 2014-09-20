/**
 *
 * @author Erhan Oztop, 2001-2002 <br>
 * <br>
 * Source code by Erhan Oztop (erhan@atr.co.jp) <br>
 * Copyright August 2002 via <br>
 * University of Southern California Ph.D. publication copyright <br>
 */
 

public class LearnPDF  {
    
    public LearnPDF() {
	
    }

    static Motor m;
    static int XBIN=20;
    static int YBIN=20;
    static boolean stochastic=false;
    static public void main(String[] argv) {
	double reward;

	Resource.read("KolParameters.res");
	double rotRandomness =Resource.getDouble("rotRandomness");
	double offRandomness =Resource.getDouble("offRandomness");
	m=new Motor();
	MotorPlan mp=null;
	Hand h=new Hand("erharm.seg",5,7);
	Graspable box=new Graspable(h,"box.seg",0,5,"PRECISION");

	if (argv.length>0) LearnPDF.VAR=Elib.toDouble(argv[0]);
	//Gplot.setUserCommand("set noxtics; set noztics; set noytics; set noborder; set nokey ");

	mp=m.nextNoisyMotorPlan(h,box,offRandomness,0,rotRandomness);
	//mp=m.nextMotorPlan(h,box);
	//mp=m.singleMotorPlan(h,box);
	XBIN = m.hand_rotBANK_code_len;
	YBIN = m.hand_rotPITCH_code_len;
	double minx=1e10;
	double maxx=-1e10;
	double miny=1e10;
	double maxy=-1e10;
	for (int i=0;i < 130000; i++) {
	    Point3d p=remap(mp);  // x=tilt, y=bank
	    if (p.x > maxx ) maxx=p.x;
	    if (p.y > maxy ) maxy=p.y;
	    if (p.x < minx ) minx=p.x;
	    if (p.y < miny ) miny=p.y; 
	    if (stochastic) {
		double pr=probabilityF(p.x,p.y,mp);
		double unif=Math.random();
		if (unif<pr)
		    m.reinforceRot_Off(1);
		else
		    m.reinforceRot_Off(-1);
	    } else { 
		reward=rewardF(p.x,p.y,mp);
		if (reward<0) reward=-0.01;
		m.reinforceRot_Off(reward);
	    }
	    if (i<10000)
		m.internalPass(110000,1000,110000);
	    else m.internalPass(110000,5000,110000);
	    if (i%1999==0) {
		mp=m.nextNoisyMotorPlan(h,box,offRandomness,0,rotRandomness);
		//System.out.println("");
	    }
	    else
		m.pickNoisyRotation(mp,rotRandomness);  
	    //mp=m.nextMotorPlan(h,box);
	    //m.pickNoisyRotation(mp,0.5);    
	}
	/*
	System.err.println("X range spanned:["+minx+","+maxx+"]");
	System.err.println("Y range spanned:["+miny+","+maxy+"]");
	int good=0;
	int bad=0;
	int tot=0; 
	for (int i=0;i<500;i++) {

	    m.internalPass(1000,1,0);
	    mp=m.nextMotorPlan(h,box);
	    Point3d p=normalize(mp.tilt, mp.bank, mp.heading);
	    System.out.println("=+= "+p.x+"  "+p.y+" reach offs:"+mp.reachOffset.str());
	    double r=rewardF(p.x,p.y,mp);
	    if (r>0.1) good++;
	    else bad++;
	    tot++;
	}
	System.err.println("Generation results:");
	System.err.println(good+"/"+tot+" is from the learned region");
	System.err.println(bad+"/"+tot+" is out of the learned region");
	*/

	double[][] hist=new double[XBIN][YBIN];
	double sx,sy;

	
	if (XBIN>1)
	     sx=(maxx-minx)/(XBIN-1);
	else
	    sx=(maxx-minx)/(XBIN);
	if (YBIN>1)
	    sy=(maxy-miny)/(YBIN-1);
	else
	    sy=(maxy-miny)/(YBIN);

	double[][] real=new double[XBIN][YBIN];

	
	/*
	for (int ix=0;ix<XBIN;ix++) {
	    for (int iy=0;iy<YBIN;iy++) {
		hist[ix][iy]=0;
		if (stochastic) 
		    real[ix][iy]=probabilityF(ix*sx+minx,iy*sy+miny,mp);
		else
		    real[ix][iy]=rewardF(ix*sx+ minx, iy*sy+miny,mp);
	    }
	}

	
	m.net3(); // maybe need to reset ?
	
	Gplot.resetGeom(300,0,275,255);
	double[][] M=m.hand_rot.getAvgSheet01();
	Gplot gp=new Gplot(M,M.length,M[0].length);

	// Let's see the real pdf
	Gplot.resetGeom(300,300,275,255);
	Gplot gpreal=new Gplot(real,XBIN,YBIN,"/tmp/GP-ACTUAL-");  
	
	*/

	Gplot.resetGeom(400,0,400,375);
	Gplot.spreadWindow(true);
	Gplot gg;
	for (int k=0; k<m.hand_off.layersize;k++) {
	    m.hand_off.setTombStone(k);
	    m.net3(); // propage to hand_rot starting from hand_off
	    
	    
	    for (int ix=0;ix<XBIN;ix++) {
		for (int iy=0;iy<YBIN;iy++) {
		    hist[ix][iy]=0;
		}
	    }

	    System.err.println("Generating data for hand_off pop ix:"+k);
	    int total=20000;
	    for (int i=0;i<total;i++) {
		m.safe_pickRotation(mp);
		Point3d p=remap(mp);
		int X=(int)(0.5+(1+p.x)/sx);
		int Y=(int)(0.5+(1+p.y)/sy);
		hist[X][Y]++;
	    }
	    for (int ix=0;ix<XBIN;ix++) 
		for (int iy=0;iy<YBIN;iy++) 
		    hist[ix][iy]/=(double)total;

	    gg=new Gplot(hist,XBIN,YBIN,"/tmp/GP-GEN-"+k+"-"); 
	    
	    /*
	    for (int ix=0;ix<XBIN;ix++) {
		System.out.println("@#@");
	    for (int iy=0;iy<YBIN;iy++) { 
		System.out.print(hist[ix][iy]/(double)total+" ");
	    }
	    System.out.println("");
	    */
	}

    }
    
    static Point3d remap(MotorPlan mp) {
	Point3d p=Motor.inconvertWrist(mp);  // now 0..1 range
	Point3d u=Motor.map2xy(Motor.callback_Wrist, p, 0);
        Point3d r=new Point3d();
	r.x=(2*u.x - 1);  // wristx, tilt
	r.y=(2*u.y - 1);  // wristz, bank
	r.z=0;
	return r;
    }
        


       
    static double VAR=0.2;
    static double rewardF(double x, double y, MotorPlan mp) {
	//System.out.print("PAR :"+m.lastpar+"   MER:"+m.lastmer);
	//if (1<2) return  
	//	     (2./XBIN)*(2./YBIN)*(Gaussian(VAR,-0.5,-0.5,x,y) +
	//	     				 Gaussian(VAR, 0.5, 0.5,x,y) )/2;
	if (1==1) return asym(x,y); //(2./XBIN)*(2./YBIN)*Gaussian(VAR,-0.5,-0.5,x,y);

	if (mp.offsetPOPIX==0) {
	    //////////////////////System.out.print("O");
	    return disk(x,y);
        } else if (mp.offsetPOPIX==1) {
	    return xor(x,y);
	} else if (mp.offsetPOPIX==-2) {
	    return tri(x,y);
	} else if (mp.offsetPOPIX==-3) {
	    return torus(x,y);
	} else if (mp.offsetPOPIX==-4) {
	    return diag(x,y);
	} else if (mp.offsetPOPIX==-5) { 
	    return gaussian(x,y);
	} else return -1; 

	    //////////////////////System.out.print("X");
	    //return tri(x,y);
	    //return diag(x,y);
	    //return disk(x,y);
	    //return torus(x,y);
	    //return xor(x,y);
	    //return gaussian(x,y);
	
    }

    static double probabilityF(double x, double y, MotorPlan mp) {
	//return tri(x,y);
	//return diag(x,y);
	//return disk(x,y);
	//return torus(x,y);
	
	double v=xor(x,y);
	if (v<0) return 0;
	else return v;

	//return (gaussian(x,y)+1)/2;

	//if (Math.random()>0.5) return (gaussian1(x,y)+1)/2;
	//else return  (gaussian2(x,y)+1)/2;
    
	//return ((gaussian1(x,y)+1)/2+(gaussian2(x,y)+1)/2)/2;

	//return (2./XBIN)*(2./YBIN)*Gaussian(VAR,0,0,x,y);
	
	//return (2./XBIN)*(2./YBIN)*(Gaussian(VAR,-0.5,-0.5,x,y) +
	//	    Gaussian(VAR, 0.5, 0.5,x,y) )/2;
    }

    static double asym(double x, double y) {
	System.out.println(x+","+y);
	if (y<0) return 1; 
	return 0;
    }
    static double gaussian(double x, double y) {
	double r = Math.exp(-(x*x+y*y)/VAR);
	return 2*r-1;
    } 
    static double gaussian1(double x, double y) {
	double r = Math.exp(-((x+0.5)*(x+0.5)+(y+0.5)*(y+0.5))/VAR)/2;
	return 2*r-1;
    } 
    static double gaussian2(double x, double y) {
	double r = Math.exp(-((x-0.5)*(x-0.5)+(y-0.5)*(y-0.5))/VAR);
	return 2*r-1;
    } 

    static double Gaussian (double var,double mux,double muy,double x, double y) {
	double r=Math.exp(-((x-mux)*(x-mux)+(y-muy)*(y-muy))/(2*var))/
	    (var*Math.sqrt(Math.PI*2));
	return r;
    }
    static double xor(double x, double y) {
	double reward=0;
	if (x*y<=0) { 
	    if (x>0) reward=1;
	    else reward=1;
	} 
	else reward=-1;
	return reward;
    }
    static double disk(double x, double y) {
	double reward=0;
	if (x*x+y*y<0.25) reward=1; 
	else reward=-1;
	return reward;
    }

   static double torus(double x, double y) {
	double reward=0;
	if (x*x+y*y<0.5 && x*x+y*y>0.3) reward=1; 
	else reward=-1;
	return reward;
    }
    static double diag(double x, double y) {
	return 2*Math.exp(-(y-x)*(y-x)/VAR)-1;
    } 


    static double tri(double x, double y) {
	if (Math.abs(x)<=Math.abs(y)) return 1;
	else return -1;
    }
} // LearnPDF
/*
 *
 * Erhan Oztop, 2000-2002  <br>
 * Source code by Erhan Oztop (erhan@atr.co.jp) <br>
 * Copyright August 2002 under <br>
 * University of Southern California Ph.D. publication copyright <br>
 */
 
