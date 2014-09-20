/**
 *
 * @author Erhan Oztop, 2001-2002 <br>
 * <br>
 * Source code by Erhan Oztop (erhan@atr.co.jp) <br>
 * Copyright August 2002 via <br>
 * University of Southern California Ph.D. publication copyright <br>
 */

public class MotorPlan
{
    int offsetPOPIX, rotationPOPIX, fingerPOPIX;
    // offset from the target object center
    Point3d reachOffset;
    // obsolute target position
    Point3d reachTarget;
    // par,mer,rad of reachOffset
    double offpar,offmer,offrad;

    Point3d wristRotation;
    // copy of wristRotation
    double tilt, heading, bank;
    // for now this is hacked as the index fingers force
    double fingerRate=1;

    // Finger indexes are as follows: 
    // 0: knuckle joint, 1: side open, 
    // 2: secon finger joint, 3: the last joint 
    // Note that for thumb, 0 brings the thumb to the palm where 1 brings it next 
    // to index finger
    // Thus, as a start you can work on force[0..2][0..1]
    double[][] force=new double[5][3]; // thumb, index, middle, ring, pinky

    /** p=(par,mer,rad) defines the offset from the object */
    public void setOffset(Point3d objpos, Point3d p)
    {
        reachOffset = new Point3d(p.z*Math.cos(p.x)*Math.sin(p.y), p.z*Math.sin(p.x), -p.z*Math.cos(p.x)*Math.cos(p.y));
        reachTarget=VA.add(objpos,reachOffset);
        offpar=p.x; offmer=p.y; offrad=p.z;
    }

    /* x: tilt (pitch), y: heading, z: bank */
    public void setWrist(Point3d wr)
    {
        wristRotation=new Point3d(wr.x, wr.y, wr.z);
        tilt=wr.y; heading=wr.z; bank=wr.x;
    }

    public void setFingerRate(double v)
    {
        fingerRate=v;
    }

    public String str()
    {
        return offpar+","+offmer+","+offrad+"   "+bank+  ","+tilt  +","+heading+"   "+
                force[0][0]+","+force[0][1]+","+force[0][2]+"   "+
                force[1][0]+","+force[1][1]+"   "+
                force[2][0]+","+force[2][1]+"   ";
    }

    public String strRot()
    {
        return "(bank,tilt,heading):"+bank+  " "+tilt  +" "+heading;
    }

    public String strOff()
    {
        return "(par,mer,rad):"+offpar+" "+offmer+" "+offrad;
    }

    public String strForce()
    {
        return "(th0,th1,th2,index0,index2,rest0,rest1):"+
                force[0][0]+" "+force[0][1]+" "+force[0][2]+"   "+
                force[1][0]+" "+force[1][1]+"   "+
                force[2][0]+" "+force[2][1]+"   ";
    }

    public void printInfo()
    {
        System.out.println("*****************************************************");
        System.out.println("* "+"Polar:"+strOff());
        System.out.println("* "+"Rect :"+reachOffset.str());
        System.out.println("* "+strRot());
        System.out.println("* "+strForce());
        System.out.println("* "+"fingerRate:"+fingerRate);
        System.out.println("******************************************************");
    }
}
/*
 *
 * Erhan Oztop, 2000-2002  <br>
 * Source code by Erhan Oztop (erhan@atr.co.jp) <br>
 * Copyright August 2002 under <br>
 * University of Southern California Ph.D. publication copyright <br>
 */
 
