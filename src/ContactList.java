
/**
 *
 * @author Erhan Oztop, 2001-2002 <br>
 * <br>
 * Source code by Erhan Oztop (erhan@atr.co.jp) <br>
 * Copyright August 2002 via <br>
 * University of Southern California Ph.D. publication copyright <br>
 */

public class ContactList
{
    static final int MAXCONT=20;
    ContactNode[] clist;
    int contc;
    double totFTOR=0;
    double graspCost=1e10; // this is a measure of how well is the grasp
    public Point3d netForce,netTorque;
    public Point3d externalForce,externalTorque;
    Object3d targetObj;
    double slipCost;
    double slip;
    double MIN_FTOR=1;
    double MIN_TOTFTOR=1;
    double newton_MAX_IT=Resource.getInt("newton_MAX_IT");
    double TORW=1;
    double pert_rate=0.1;
    double newton_zero=0.5;

    public ContactList(Object3d obj)
    {
        clist=new ContactNode[MAXCONT];
        contc=0;
        targetObj=obj;
        netTorque=new Point3d(0,0,0);
        netForce=new Point3d(0,0,0);
        slipCost=Resource.get("slipCost");
    }

    /** link is the intersecting link. torqueseg is the one who applies the torque. We assume the distal joints only transduce the root joints torque. */
    public int addContact(Segment link, Plane pl, Point3d p, Segment torqueseg,double ftor)
    {
        clist[contc++]=new ContactNode(link,pl,p,torqueseg,ftor);
        return contc;
    }

    public int addContact(Point3d Fdir, double ftor)
    {
        clist[contc++]=new ContactNode(Fdir,ftor);
        return contc;
    }

    public void resetList(Object3d obj)
    {
        contc=0;
        targetObj=obj;
    }

    /** Must be applied after netForce is calculated */
    public void setExternalForce(Point3d F)
    {
        externalForce=F.duplicate();
        VA._add(netForce, F);
    }

    public void newton()
    {
        netTorque.x=0; netTorque.y=0; netTorque.z=0;
        netForce.x=0;  netForce.y =0; netForce.z =0;
        totFTOR=0;
        slip=0;
        double maxslip=-1;
        Point3d C=targetObj.objectCenter;
        //Mars.clearComets();

        for (int i=0;i<contc;i++)
        {
            ContactNode n=clist[i];
            if (n.trueContact)
            {
                Point3d  F=VA.scale(n.Fdir,n.ftor);
                // Now it got the magnitude from ftor value
                // We have to seperate the normal force
                // and parallel force to the surface
                totFTOR += n.ftor;
                // vertical force
                n.Fver=VA.scale(n.pl.normal, VA.inner(n.pl.normal,F));
                // horizontal
                n.Fpar=VA.cross(VA.cross(n.pl.normal, F), n.pl.normal);
                // now the horizontal should not be big to be balanced by friction
                if (VA.norm(n.Fver)==0)
                    maxslip= 1000;
                else
                {
                    if (VA.norm(n.Fpar)/VA.norm(n.Fver) > maxslip)
                    {
                        maxslip=VA.norm(n.Fpar)/VA.norm(n.Fver);
                    }
                }
                // the vertical will be used for object motion
                VA._add(netForce,n.Fver); //was Fver
                /////Mars.addComet(n.p,VA.add(VA.scale(n.Fver,-250),n.p),4);
                Point3d L=VA.subtract(n.p,C);
                Point3d T=VA.cross(F,L);
                VA._scale(T,1/100.0);
                VA._add(netTorque,T);
            }
            else
            {
                Point3d F=VA.scale(n.Fdir, n.ftor);
                totFTOR += n.ftor;
                VA._add(netForce,F); //injected to the object
                // no torque at this time
            }

        }
        if (maxslip>1.0)
            slip=maxslip;
    }

    double costNewton()
    {
        return ((2-TORW)*VA.norm(netForce) + TORW*VA.norm(netTorque))/contc + slipCost*slip;
    }

    public double searchNewton()
    {
        double cost, newcost;
        if (contc<2)
        {
            graspCost=1e10;
            return graspCost;
        }
        for (int i=0;i<contc;i++)
            clist[i].ftor=1+Math.random();
        newton();
        cost=costNewton();

        int k=0;
        while (k<newton_MAX_IT && cost>newton_zero)
        {
            int g=(int)(Math.random()*contc);
            double pert=(0.1+Math.random())*pert_rate;
            if (Math.random() < 0.5)
                pert=-pert;
            if (clist[g].ftor+pert > MIN_FTOR)
                clist[g].ftor+=pert;
            else
                continue;
            newton();
            newcost=costNewton();
            if (newcost > cost)
            {
                clist[g].ftor -= pert*(1+Math.random()*0.5);
                if (clist[g].ftor < MIN_FTOR)
                    clist[g].ftor=MIN_FTOR;
                newton();
                cost=costNewton();
            }
            else
            {
                cost=newcost;
            }
            //if (k%50==0) {
            //	System.out.println(k+") cost: "+cost);//+" [ "+str());
            //}
            k++;
        }
        graspCost=cost;
        return cost;
    }


    public String str()
    {
        String s="";
        for (int i=0;i<contc;i++)
            s+=clist[i].ftor+"  ";
        return s;
    }

} // ContactList

class ContactNode
{
    Segment link;       // this is the one intersecting
    Segment torquelink; // this is the one applying torque
    Plane   pl;
    Point3d   p;
    double ftor;    // scalar measure of force like finger torque
    Point3d F;      // force on object 
    Point3d Fdir;   // the effective force direction ~ F=Fdir+Fver
    Point3d Fver;   // the vertical component of F
    Point3d Fpar;   // the plane parallel component of F 
    Point3d torque; // torque on object
    Point3d virtualL; // p-torquelink_jointpos, "virtual torque arm"
    boolean trueContact;

    public ContactNode(Segment link, Plane pl, Point3d p,Segment torquelink, double ftor)
    {
        this.link=link;
        this.pl=pl;
        this.p=p;
        this.ftor=ftor;
        this.torquelink=torquelink;
        virtualL=VA.subtract(p,torquelink.joint_pos);
        trueContact=true;
        this.Fdir=VA.cross(this.torquelink.joint_axis,this.virtualL);
        VA._normalize(this.Fdir);
    }

    public ContactNode(Point3d Fdir, double ftor)
    {
        this.ftor=ftor;
        trueContact=false;
        this.Fdir=VA.normalize(Fdir);
    }
}
/*
 *
 * Erhan Oztop, 2000-2002  <br>
 * Source code by Erhan Oztop (erhan@atr.co.jp) <br>
 * Copyright August 2002 under <br>
 * University of Southern California Ph.D. publication copyright <br>
 */
 
