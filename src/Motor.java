/**
 *
 * @author Erhan Oztop, 2001-2002 <br>
 * <br>
 * Source code by Erhan Oztop (erhan@atr.co.jp) <br>
 * Copyright August 2002 via <br>
 * University of Southern California Ph.D. publication copyright <br>
 */


public class Motor
{
    // debug level
    static int DLEV=0;
    double[] input0, output0;
    int input0_s;
    double[] input0_p, output0_p;

    // Hand offset weights from input
    double[][] W_01;
    // Finger  weights from input
    double[][] W_02;
    // Hand rotation weights from the input
    double[][] W_03;
    // Hand rotation weights from hand offset
    double[][] W_13;
    // Hand rotation weights from Finger
    double[][] W_23;
    double eta01,eta02,eta03,eta12,eta13,eta23,eta=0;

    // F5canonical input unit specs
    int obj_type_code_len = 1;  // 10 object max
    int obj_size_code_len=  1;  // 20 
    int obj_locPAR_code_len  = Resource.getInt("obj_locPAR_code_len");
    int obj_locMER_code_len  = Resource.getInt("obj_locMER_code_len");
    int obj_locRAD_code_len  = Resource.getInt("obj_locRAD_code_len");

    int obj_axisMER_code_len = 1; //10
    int obj_axisPAR_code_len = 3; //10; //10
    int obj_axisRAD_code_len = 1;  // to make plot routines happy 
    String[] obj_name=new String[obj_type_code_len]; // to describe the object to us

    // F1 (output) unit specs
    // allocentric target hand position
    int hand_locPAR_code_len = Resource.getInt("hand_locPAR_code_len");
    int hand_locMER_code_len = Resource.getInt("hand_locMER_code_len");
    int hand_locRAD_code_len = Resource.getInt("hand_locRAD_code_len");
    // the wrist rotations of the hand
    int hand_rotBANK_code_len = Resource.getInt("hand_rotBANK_code_len");
    int hand_rotPITCH_code_len = Resource.getInt("hand_rotPITCH_code_len");
    int hand_rotHEADING_code_len = Resource.getInt("hand_rotHEADING_code_len");

    int finger_FORCE_code_len = 1; // the 3 fingers force output 
    int thumb_FORCE_code_len  = 1; // ^2 size!
    int index_FORCE_code_len = 1; // the index finger [now only speed of all]

    PopulationCode affordance;
    PopulationCode obj_loc;
    PopulationCode obj_ori;
    PopulationCode hand_off;
    PopulationCode hand_rot;
    PopulationCode finger_force;
    // the WTA maybe wrong by this amount (in prob units)
    double WTA_ERR=0;
    double PDFthreshold=0; // CONS for generating actions min. req prob.
    double unitBias=0;//-0.1;
    static int PLOTLEV=Resource.getInt("motor.PLOTLEV");

    static double obj_axisMER_scale = (1-0)*Math.PI/180;
    static double obj_axisMER_offset = 0*Math.PI/180;

    static double obj_axisPAR_scale = (Resource.get("maxTILT")-Resource.get("minTILT"))*Math.PI/180;
    static double obj_axisPAR_offset = Resource.get("minTILT")*Math.PI/180;

    // this is just to make plot routines happy
    static double obj_axisRAD_scale = (1-0)*Math.PI/180;
    static double obj_axisRAD_offset = 0*Math.PI/180;

    static double obj_locMER_scale = (Resource.get("maxMER")-Resource.get("minMER"))*Math.PI/180;
    static double obj_locMER_offset = Resource.get("minMER")*Math.PI/180;

    static double obj_locPAR_scale = (Resource.get("maxPAR")-Resource.get("minPAR"))*Math.PI/180;
    static double obj_locPAR_offset = Resource.get("minPAR")*Math.PI/180;

    static double obj_locRAD_scale  = -(Resource.get("maxRAD")-Resource.get("minRAD"));
    static double obj_locRAD_offset = Resource.get("maxRAD");

    static double hand_locPAR_scale  =120*(Math.PI/180);
    static double hand_locPAR_offset =-60*(Math.PI/180);

    static double hand_locMER_scale  =120*(Math.PI/180); // Math.PI*2;  
    static double hand_locMER_offset =-30*(Math.PI/180); //-Math.PI;

    static double hand_locRAD_scale  = 400;
    static double hand_locRAD_offset = 0;

    static double hand_rotBANK_scale =-(180+10)*Math.PI/180;
    static double hand_rotBANK_offset= 10*Math.PI/180;       // wrist bank [-180,+80]
    static double hand_rotPITCH_scale =Math.PI;
    static double hand_rotPITCH_offset=-Math.PI/2;    //wrist pitch [-90,+90]
    static double hand_rotHEADING_scale =Math.PI/2;
    static double hand_rotHEADING_offset=-Math.PI/4;    //wrist heading [-45,+45] 

    static double index_FORCE_scale  = -1;    //this is used for speed control!!
    static double index_FORCE_offset = 1.0; // WARNING!! ^^

    static double finger_FORCE_scale  = -1; // the fingers speed rate 
    static double finger_FORCE_offset = 1.0; // the 3 fingers force output 
    static double thumb_FORCE_scale   = -1;
    static double thumb_FORCE_offset   =1.0;

    static final public int callback_Wrist=1;
    static final public int callback_Offset=2;
    static final public int callback_Finger=3;
    static final public int callback_Thumb=4;
    static final public int callback_Input=10;

    static int plotLabel,plotKey;

    static final boolean CLAMPON=true;
    static final boolean CLAMPOFF=false;
    public int passc=0;
    int hand_off_pick;
    double[] offvalue;
    int hand_rot_pick;
    double[] rotvalue;
    int finger_force_pick;
    double[] forcevalue;
    static double GVAR=2; //was 2
    static double softVAR=1/0.5; //0.5 // this is 1/VAR actuall!

    static final int MAXREW3HIST=10000;
    double[] rew3_hist=new double [MAXREW3HIST];
    int rew3_c=0;

    double colW_VAR=1/1;  // bottom is the variance!

    public double max_value_after_softmax=-1;

    int inix=0;

    double lasttilt_deg=0;

    public double lastpar=0,lastmer=0;

    int rotix=0;
    boolean looped=false;

    public Motor()
    {
        setParameters();
        createPopulations();
        createWeights();
        forwardNoisyPass(1,1,1,1,null);  // generate some output
    }

    /**
     * The convert family is used to scaling and translating the 0..1 firingRate of units to actual values.
     */
    static public Point3d convert(int callbackID, double x, double y, double z)
    {
        switch (callbackID)
        {
            case callback_Wrist :
                return convertWrist(x,y,z);
            case callback_Offset:
                return convertOffset(x,y,z);
            case callback_Finger:
                return convertFinger(x,y,z);
            case callback_Thumb:
                return convertThumb(x,y,z);
            case callback_Input:
                return convertInput(x,y,z);
        }
        return null;
    }

    static public Point3d convertInput(double a0, double a1, double a2)
    {
        return new Point3d(0, a1*obj_axisMER_scale+obj_axisMER_offset, a0*obj_axisPAR_scale+obj_axisPAR_offset);
    }

    static public Point3d convertOffset(double a0, double a1, double a2)
    {
        return new Point3d (a0*hand_locPAR_scale+hand_locPAR_offset, a1*hand_locMER_scale+hand_locMER_offset,
                a2*hand_locRAD_scale+hand_locRAD_offset);
    }

    /* check maybe wrong now */
    /*
    static public Point3d inconvertOffset(MotorPlan mp)
    {
        Point3d p=new Point3d();
        p.x = (mp.offpar - hand_locPAR_offset)/hand_locPAR_scale;
        p.y = (mp.offmer - hand_locMER_offset)/hand_locMER_scale;
        p.z = (mp.offrad - hand_locRAD_offset)/hand_locRAD_scale;
        return p;
    }
    */

    static public Point3d convertThumb(double a0, double a1, double a2)
    {
        return new Point3d(a0*thumb_FORCE_scale+thumb_FORCE_offset, a1*thumb_FORCE_scale+thumb_FORCE_offset,
                a2*thumb_FORCE_scale+thumb_FORCE_offset);
    }

    static public Point3d convertFinger(double a0, double a1, double a2)
    {
        return new Point3d(a0*index_FORCE_scale+index_FORCE_offset, a1*finger_FORCE_scale+finger_FORCE_offset,
                a2*finger_FORCE_scale+finger_FORCE_offset);
    }

    static public Point3d convertWrist(double a0, double a1, double a2)
    {
        return new Point3d(a0*hand_rotBANK_scale+hand_rotBANK_offset, a1*hand_rotPITCH_scale+hand_rotPITCH_offset,
                a2*hand_rotHEADING_scale+hand_rotHEADING_offset);
    }

    /* check maybe wrong now */
    static public Point3d inconvertWrist(MotorPlan mp)
    {
        Point3d p=new Point3d();
        //System.out.println("mp bank:"+mp.bank+"  mp.tilt:"+mp.tilt+"   mp.heading:"+mp.heading);
        p.y = (mp.tilt - hand_rotPITCH_offset)/hand_rotPITCH_scale;
        p.z= (mp.heading - hand_rotHEADING_offset)/hand_rotHEADING_scale;
        p.x = (mp.bank - hand_rotBANK_offset)/hand_rotBANK_scale;
        return p;
    }

    static public Point3d map2xy(int callbackID, Point3d p,double Z)
    {
        switch(callbackID)
        {
            case callback_Wrist :
                return new Point3d(p.y, p.x, Z);
            case callback_Offset:
                return new Point3d(p.x, p.y, Z);
            case callback_Thumb :
                return new Point3d(p.x, p.y, Z);
            case callback_Input :
                return new Point3d(p.z, p.y, Z); // for objAXIS
            //case callback_Input : return new Point3d(p.x, p.y, Z); //for objLOC
        }
        return null;
    }

    public String getTempPrefix(int callbackID)
    {
        String base="/tmp/GP.";
        switch(callbackID)
        {
            case callback_Wrist :
                return base+"wrist"+passc;
            case callback_Offset:
                return base+"offset"+passc;
            case callback_Input :
                return base+"objtilt"+passc; // for objAXIS
            //case callback_Input : return base+"input"+passc; // for objLOC
        }
        return base+"none.";
    }

    public String getGplotCommand(int callbackID)
    {
        if (plotLabel==0)
            return "";
        switch(callbackID)
        {
            case callback_Wrist :
                return "set xlabel 'tilt'; set ylabel 'bank'; set title 'TILT:"+lasttilt_deg+" HANDOFF_par:"+lastpar+
                        "HANDOFF_mer:"+lastmer+"';";
            case callback_Offset:
                return "set xlabel 'parallel'; set ylabel 'meridian'";
            //case callback_Input : return "set xlabel 'parallel'; set ylabel 'meridian'"; // for objLOC
            case callback_Input :
                return "set xlabel 'tilt(z)'; set ylabel 'tilt(y)'";  // for objAXIS
        }
        return "set xlabel 'BUG IN getGplotCommand!!'";
    }

    public void setParameters()
    {
        eta=Resource.get("eta"); // other eta's depend on this so can't update.
        updateResourcePars();
    }

    /**
     * This will update some parameters from resource. Cannot do the hard core pars of course
     */
    public void updateResourcePars()
    {
        plotLabel=Resource.getInt("plotLabel");
        plotKey=Resource.getInt("plotKey");
        if (plotKey==0)
            Gplot.setUserCommand("set noxtics; set noztics; set noytics; set noborder; set nokey ");
        else
            Gplot.setUserCommand("");
        PDFthreshold=Resource.get("PDFthreshold");
        WTA_ERR=Resource.get("WTA_ERR");
        GVAR=Resource.getDouble("GVAR");
        softVAR=Resource.getDouble("softVAR");
        DLEV=Resource.getInt("Motor.DLEV");
    }

    private void createPopulations()
    {
        /*
        obj_loc=new PopulationCode("Object Location", obj_locPAR_code_len*obj_locMER_code_len*obj_locRAD_code_len,"");
        obj_loc.addDimension("ObjLoc-PAR",obj_locPAR_code_len);
        obj_loc.addDimension("ObjLoc-MER",obj_locMER_code_len);
	    obj_loc.addDimension("ObjLoc-RAD",obj_locRAD_code_len);
      	affordance=obj_loc;
      	affordance.setEncodingVar(Resource.get("obj_encode_var"));
        */

        // Define the object axis orientation
        obj_ori=new PopulationCode("Object Axis Orientation", obj_axisMER_code_len*obj_axisPAR_code_len*obj_axisRAD_code_len,"",0.001);
        obj_ori.addDimension("Obj-PAR",0,1,obj_axisPAR_code_len,/*0.001,*/"sharp encoding");
        obj_ori.addDimension("Obj-MER",0,1,obj_axisMER_code_len,/*0.001,*/"sharp encoding");
        obj_ori.addDimension("Obj-RAD",0,1,obj_axisRAD_code_len,/*0.001,*/"sharp encoding");
        affordance=obj_ori;
        //--------------------------------------------------------------

        //--------------------------------------------------------------
        hand_off=new PopulationCode("Hand Offset", hand_locPAR_code_len*hand_locMER_code_len*hand_locRAD_code_len,"",PopulationCode.def_var);
        hand_off.addDimension("Hand-PAR",hand_locPAR_code_len);
        hand_off.addDimension("Hand-MER",hand_locMER_code_len);
        hand_off.addDimension("Hand-RAD",hand_locRAD_code_len);
        //--------------------------------------------------------------

        //--------------------------------------------------------------
        hand_rot=new PopulationCode("Hand rotation", hand_rotBANK_code_len*hand_rotPITCH_code_len*hand_rotHEADING_code_len,"",PopulationCode.def_var);
        hand_rot.addDimension("Hand-BANK",hand_rotBANK_code_len); // wristz
        hand_rot.addDimension("Hand-PITCH",hand_rotPITCH_code_len);  // wristx
        hand_rot.addDimension("Hand-HEADING",hand_rotHEADING_code_len); // wristy
        // create 2 for now synapses
        hand_rot.makeSynapse(2);
        //--------------------------------------------------------------

        //--------------------------------------------------------------
        finger_force=new PopulationCode("Finger force", index_FORCE_code_len*finger_FORCE_code_len*thumb_FORCE_code_len*
                        thumb_FORCE_code_len*thumb_FORCE_code_len,"",PopulationCode.def_var);
        finger_force.addDimension("fingerRate", index_FORCE_code_len);
        finger_force.addDimension("OtherForce", finger_FORCE_code_len);
        finger_force.addDimension("ThumbForce0",thumb_FORCE_code_len);
        finger_force.addDimension("ThumbForce1",thumb_FORCE_code_len);
        finger_force.addDimension("ThumbForce2",thumb_FORCE_code_len);
        //--------------------------------------------------------------
    }

    /**
     * Creates the weight matrices. Cannot be called before createPopulation().
     */
    private void createWeights()
    {
        //input0  = obj_ori.firingRate;
        //input0_s   = obj_ori.layersize;
        input0 = affordance.firingRate;
        input0_s = affordance.layersize;
        for (int i=0;i<input0_s;i++)
            input0[i]=1;

        W_01=fixedWeight(hand_off.layersize,input0_s,0.1);
        eta01=5*eta/Resource.get("MAXROTATE"); //CHA
        W_02= fixedWeight(finger_force.layersize,input0_s,0.1);
        eta02=eta/Resource.get("MAXROTATE");
        //W_12=randomWeight(finger_force.layersize,hand_off.layersize,1);   eta12=eta;
        //W_03=randomWeight(hand_rot.layersize,input0_s,1);
        W_03=fixedWeight(hand_rot.layersize,input0_s,0.1);
        eta03=eta;
        W_13=fixedWeight(hand_rot.layersize,hand_off.layersize,0.1);
        eta13=eta;
        W_23=fixedWeight(hand_rot.layersize,finger_force.layersize,0.1);
        eta23=eta;

        //loadNet(".LGM-1014590286");
    }

    /**
     * note it is not random anymore :)
     * //@param rows
     * //@param cols
     * //@param scale
     * //@return - weight matrix
     */
    /*
    public double[][] randomWeight(int rows,int cols, double scale)
    {
        double[][] arr=new double[rows][cols];
        for (int i=0;i<rows;i++)
        {
            for (int j=0;j<cols;j++) {
                arr[i][j]=0.1; //*scale*(Math.random()-0.5);
            }
            normalizeWeight(arr[i]); //was active
        }

        System.out.println("random weight:"+rows+","+cols);
        return arr;
    }
    */

    public double[][] fixedWeight(int rows,int cols, double v)
    {
        double[][] arr=new double[rows][cols];
        for (int i=0;i<rows;i++)
        {
            for (int j=0;j<cols;j++)
            {
                arr[i][j]=v;
            }
            normalizeWeight(arr[i]);
        }
        return arr;
    }

    /**
     * startlevel
     * 1: full forward pass
     * 2: don't update hand offset
     * 3: don't update hand offset and FINGERS
     * 4: do nothing :)
     */
    synchronized  public void forwardPass(int startlevel)
    {
        if (startlevel<=1)
        {
            //VA.multiply(W_01, input0, hand_off.layersize, input0_s, hand_off.prob, unitBias);
            VA.multiply(W_01, input0, hand_off.layersize, input0_s, hand_off.firingRate, unitBias);
            //softmax(hand_off.prob, hand_off.layersize, hand_off.prob);
            softmax(hand_off.firingRate, hand_off.layersize, hand_off.firingRate);
            if (PLOTLEV>0)
            {
                Gplot.resetGeom((int)(0.5+lasttilt_deg*6),100,400,400);
                hand_off.plotFiringRate(this,callback_Offset);
            }
            //hand_off_pick=pick_WTA(hand_off.prob, hand_off.layersize);
            hand_off_pick=pick_WTA(hand_off.firingRate, hand_off.layersize);
            offvalue=hand_off.pref_value(hand_off_pick); // or decode ?
            hand_off.setTombStone(hand_off_pick); //CHA2
            //hand_off.setGaussBall(hand_off_pick,GVAR);
            //---> We picked the hand offset and marked the region with a ball now.
        }
        if (startlevel<=2)
        {
            //VA.multiply(W_02, input0, finger_force.layersize, input0_s, finger_force.prob, unitBias);
            VA.multiply(W_02, input0, finger_force.layersize, input0_s, finger_force.firingRate, unitBias);
            // VA.multiadd(W_12, hand_off.firingRate, finger_force.layersize, hand_off.layersize, finger_force.firingRate);
            //softmax(finger_force.prob, finger_force.layersize, finger_force.prob);
            softmax(finger_force.firingRate, finger_force.layersize, finger_force.firingRate);
            if (PLOTLEV>0)
            {
                //new Gplot(finger_force.prob);
                new Gplot(finger_force.firingRate);
            }
            //finger_force_pick=pick_WTA(finger_force.prob,finger_force.layersize);
            finger_force_pick=pick_WTA(finger_force.firingRate,finger_force.layersize);
            forcevalue=finger_force.pref_value(finger_force_pick); // or decode ?
            finger_force.setGaussBall(finger_force_pick,GVAR);
            //->> We picked the hand rotation and marked the region with a ball now
        }
        if (startlevel <=3)
        {
            net3();
            if (PLOTLEV>0)
            {
                Gplot.resetGeom((int)(0.5+lasttilt_deg*6),100,400,400);
                hand_rot.plotFiringRate(this,callback_Wrist);
            }
            //hand_rot_pick=pick_WTA(hand_rot.prob,hand_rot.layersize);
            hand_rot_pick=pick_WTA(hand_rot.firingRate,hand_rot.layersize);
            rotvalue=hand_rot.pref_value(hand_rot_pick); // or decode ?
            hand_rot.setGaussBall(hand_rot_pick,GVAR);
        }
        //-->> We picked the forces and marked the region with a ball now
    }

    /**
     * startlevel
     * 1: full forward pass
     * 2: don't update hand offset
     * 3: don't update hand offset and FINGERS
     * 4: do nothing :)
     */
    synchronized  public void forwardNoisyPass(int startlevel, double n1, double n2, double n3, MotorPlan m )
    {
        if (startlevel<=1)
        {
            //VA.multiply(W_01, input0, hand_off.layersize, input0_s , hand_off.prob,unitBias);
            VA.multiply(W_01, input0, hand_off.layersize, input0_s, hand_off.firingRate, unitBias);
            dprint("Hand_offset...");
            //softmax(hand_off.prob, hand_off.layersize, hand_off.prob);
            softmax(hand_off.firingRate, hand_off.layersize, hand_off.firingRate);
            //hand_off_pick=pick_my_NoisyPDF(hand_off.prob, hand_off.layersize,n1);
            hand_off_pick=pick_my_NoisyPDF(hand_off.firingRate, hand_off.layersize,n1);
            if (m!=null)
                m.offsetPOPIX=hand_off_pick;
            offvalue=hand_off.pref_value(hand_off_pick);
            //hand_off.setGaussBall(hand_off_pick,GVAR);
            hand_off.setTombStone(hand_off_pick);
            //Gplot gp=new Gplot(offvalue);
            //---> We picked the hand offset and marked the region with a ball now.
        }
        if (startlevel<=2)
        {
            //VA.multiply(W_02, input0,  finger_force.layersize,  input0_s, finger_force.prob,unitBias);
            VA.multiply(W_02, input0, finger_force.layersize, input0_s, finger_force.firingRate, unitBias);
            //softmax(finger_force.prob, finger_force.layersize, finger_force.prob);
            softmax(finger_force.firingRate, finger_force.layersize, finger_force.firingRate);
            //System.out.println("2nd softmax done.");
            //finger_force_pick=pick_my_NoisyPDF(finger_force.prob,finger_force.layersize,n2);
            finger_force_pick=pick_my_NoisyPDF(finger_force.firingRate,finger_force.layersize,n2);
            if (m!=null) m.fingerPOPIX=finger_force_pick;
            forcevalue=finger_force.pref_value(finger_force_pick);
            finger_force.setTombStone(finger_force_pick); //CHA
            //finger_force.setGaussBall(finger_force_pick,GVAR);
            //->> We picked the hand rotation and marked the region with a ball now
        }
        if (startlevel <=3)
        {
            dprint("Wrist rotation...");
            net3();
            //System.out.println("3rdsoftmax done.");
            //hand_rot_pick=pick_my_NoisyPDF(hand_rot.prob,hand_rot.layersize,n3);
            hand_rot_pick=pick_my_NoisyPDF(hand_rot.firingRate,hand_rot.layersize,n3);
            if (m!=null)  m.rotationPOPIX=hand_rot_pick;
            rotvalue=hand_rot.pref_value(hand_rot_pick);
            //hand_rot.setTombStone(hand_rot_pick); // CHA, CHA3
            hand_rot.setGaussBall(hand_rot_pick,GVAR);
        }
        //-->> We picked the forces and marked the region with a ball now
    }

    public void net3()
    {
        //qSystem.out.println("hand_rot.layersize:"+hand_rot.layersize);
        VA.multiply(W_03,   input0, hand_rot.layersize, input0_s , hand_rot.synapse[0]);
        softmax(hand_rot.synapse[0], hand_rot.layersize, hand_rot.synapse[0]);
        VA.multiply(W_13,  hand_off.firingRate, hand_rot.layersize, hand_off.layersize, hand_rot.synapse[1]);
        softmax(hand_rot.synapse[1], hand_rot.layersize, hand_rot.synapse[1]);
        //VA.multiply(W_23,  finger_force.firingRate, hand_rot.layersize,
        //	    finger_force.layersize, hand_rot.synapse[2],unitBias);
        //softmax(hand_rot.synapse[2], hand_rot.layersize, hand_rot.synapse[2]);
        hand_rot.mergeSynapse();
        //softmax(hand_rot.prob, hand_rot.layersize, hand_rot.prob);
        softmax(hand_rot.firingRate, hand_rot.layersize, hand_rot.firingRate);
        // last two step can be combined and optimized
        //Gplot gp=new Gplot(hand_rot.firingRate);
        //  hand_off.plotFiringRate(this,callback_Offset);
        //  hand_rot.plotFiringRate(this,callback_Wrist);
    }

    public void advancePass()
    {
        passc++;
        net3();// check this do you need to propagate each tick or not
        if (passc%250==1 && passc<10000 || passc%5000==1)
        {
            //Gplot gp1=new Gplot(hand_off.firingRate);
            int wi=1024/hand_locMER_code_len;
            int he=768 /hand_locPAR_code_len;
            int x=(int)(lastmer*hand_locMER_code_len);
            int y=(int)(lastpar*hand_locPAR_code_len);
            Gplot.resetGeom(x*wi,y*he,wi,he);
            hand_off.plotFiringRate(this,callback_Offset);
            hand_rot.plotFiringRate(this,callback_Wrist);
            //Gplot.resetGeom(50,50,25qqqqqqq5,255);
            //Gplot gp1=new Gplot(flatten(W_13));
        }
    }

    public void internalPass(int N,int K,int Kok)
    {
        Gplot gp;
        passc++;
        net3(); // check this do you need to propagate each tick or not

        if (passc%N==1 || (passc%K==1 && passc<Kok-1))
        {
            if (passc%K==1 && passc<Kok-1)
            {
                if (!Gplot.spread_window)
                {
                    Gplot.resetGeom(0,0,220,200);
                    Gplot.spreadWindow(true);
                }
                hand_rot.plotFiringRate(this,callback_Wrist);
                hand_off.plotFiringRate(this,callback_Offset);

            }
            else
            {
                Gplot.spreadWindow(false);
                Gplot.resetGeom(0,0,220,200);

                hand_rot.plotFiringRate(this,callback_Wrist);
            }
            /*
            Gplot.resetGeom(  0,300,255,255);
            Gplot gp2=new Gplot(W_13,W_13.length,W_13.length);
            Gplot.resetGeom(640,300,255,255);
            Gplot gp3=new Gplot(W_03,W_03.length,W_03.length);
            */
        }
    }

    /*
    public double[] flatten(double[][] M)
    {
        int m=M.length;
        int n=M[0].length;
        double[] h=new double[m*n];
        int k=0;
        for (int i=0;i<m; i++)
            for (int j=0;j<n;j++)
                h[k++]=M[i][j];
        return h;
    }
    */

    public void reinforceAll(double reward, MotorPlan mp)
    {
        reinforce(reward,3);
        reinforce(reward,2);
        reinforce(reward,1);
    }

    /*
    public void reinforceRot(double reward)
    {
        reinforce(reward,3);
    }
    */

    public void reinforceRot_Off(double reward)
    {
        reinforce(reward,3);
        reinforce(reward,1);
    }

    public void reinforceOff(double reward)
    {
        reinforce(reward,1);
    }

    /**
     * level 1: hand offset
     * level 2: finger force
     * level 3: hand rot level.
     * For updating all the weights must be called three times with level 1,2,3
     */
    public void reinforce(double R, int level)
    {
        // reinforce only the hand rotatio weights
        if (level==3)
        {
            //Gplot gg=new Gplot(input0);
            rew3_hist[rew3_c++]=R;
            if (rew3_c>=MAXREW3HIST)
            {
                rew3_c=0;
            }
            //if (rew3_c % 50==1) {
            //	Gplot gg=new Gplot(rew3_hist,rew3_c);
            //}
            for (int i=0;i<hand_rot.layersize;i++)
            {
                //for (int j=0;j<finger_force.layersize;j++)
                //W_23[i][j]+= eta23 * R * finger_force.firingRate[j]*hand_rot.firingRate[i];
                for (int j=0;j<hand_off.layersize;j++)
                    W_13[i][j]+= eta13 * R * hand_off.firingRate[j]*hand_rot.firingRate[i];

                for (int j=0;j<input0_s;j++)
                    W_03[i][j]+= eta03 * R * input0[j]*hand_rot.firingRate[i];
            }
        }
        // reinforce only the FINGER weights
        if (level==2)
        {
            for (int i=0;i<finger_force.layersize;i++)
            {
                for (int j=0;j<input0_s;j++)
                    W_02[i][j]+= eta02 * R * input0[j]*finger_force.firingRate[i];
            }
        }
        // reinforce only the hand_offset weights
        if (level==1)
        {
            for (int i=0;i<hand_off.layersize;i++)
            {
                for (int j=0;j<input0_s;j++)
                    W_01[i][j]+= eta01 * R * input0[j]*hand_off.firingRate[i];
            }
        }
    }

    /*
    public void softmax_colW(double[][] v, double[][] r, int col)
    {
        double sum;
        sum=0;
        int size=v.length;

        for (int i=0;i<size;i++)
            sum+=Math.exp(colW_VAR*v[i][col]);

        //System.out.print("softmax not divided yet:[");
        //for (int i=0;i<size;i++)
        //    System.out.print(v[i]+" ");
        //System.out.println("]");
        for (int i=0;i<size;i++)
            r[i][col]=Math.exp(colW_VAR*v[i][col])/sum;
    }
    */

    /**
     * Normalize over the columns. The column weight vector becomes unit length in ^2 norm
     */
    /*
    public void col_normalizeWeight(double w[][],int col)
    {
        double sum=0,maxw;

        if (1==1)
        {
            //softmax_colW(w,w,col);
            return;
        }
        if (1==2)
        {
            for (int i=0;i<w.length;i++)
            {
                if (w[i][col]>1)
                    w[i][col]=1;
            }
            return;
        }
        maxw=Math.abs(w[0][col]);
        for (int i=0;i<w.length;i++)
        {
            sum+=w[i][col]*w[i][col];
            if (Math.abs(w[i][col])>maxw)
            {
                maxw=Math.abs(w[i][col]);
                //    System.out.println("Current Max Weight is big : "+i+
                //  				       " value:"+maxw);
            }
        }
        //sum=Math.sqrt(sum);
        //for (int i=0;i<w.length;i++) {w[i][col]/=sum; }
        for (int i=0;i<w.length;i++)
            w[i][col]/=maxw;

        //System.out.println("MAXW FOUND for this normalization:"+maxw);
        //if (HV.mcirc!=null) {
        //Gplot gp=new Gplot(w);
        //}
    }
    */

    /**
     * Weight vector becomes unit length in ^2 norm
     */
    public void normalizeWeight2(double w[])
    {
        double sum=0,maxw;

        maxw=0;
        for (int i=0;i<w.length;i++)
        {
            sum+=w[i]*w[i];
            /*
            if (Math.abs(w[i])>maxw)
      		{
      		    maxw=w[i];
      		    System.out.println("Current Max Weight is big : "+i+" value:"+maxw);
      		}
            */
        }

        sum=Math.sqrt(sum);
        for (int i=0;i<w.length;i++)
            w[i]/=sum;

        //System.out.println("MAXW FOUND for this normalization:"+maxw);
        //if (HV.mcirc!=null) {
        //Gplot gp=new Gplot(w);
        //}
    }

    /**
     * Decaying weight
     */
    /*
    public void decayWeight(double w[], double rate)
    {
        for (int i=0;i<w.length;i++)
        {
            w[i] *= rate;
        }
    }
    */

    /**
     * Max component comes 1 the rest scaled accordingly
     */
    public void normalizeWeight(double w[])
    {
        double sum=0,maxw;

        //softmaxW(w,w.length,w,1);
        //decayWeight(w,0.9);
        normalizeWeight2(w);
        if (1==1) return;
        maxw=Math.abs(w[0]);
        sum=0;
        for (int i=0;i<w.length;i++)
        {
            if (Math.abs(w[i])>maxw)
                maxw=Math.abs(w[i]);
            sum+=Math.abs(w[i]);
        }
        if (maxw==0)
        {
            System.err.println("WARNING: maxw is zero, no sense in normalizing....");
            return;
        }

        double div=sum; //maxw*10;

        //if (maxw>10) div=10;
        //else div=1;

        for (int i=0;i<w.length;i++)
        {
            w[i]/=div; // maxw;
        }

        //if (HV.mcirc!=null) {
        //Gplot gp=new Gplot(w);
        //}
    }

    /*
    public void normalizeWeight2D(double[][] w)
    {
        double maxw=w[0][0];
        double sum=0;
        for (int i=0;i<w.length;i++)
        {
            for(int j=0;j<w[0].length;j++)
            {
                if (Math.abs(w[i][j])>maxw)
                    maxw=Math.abs(w[i][j]);
                sum+=Math.abs(w[i][j]);
            }
        }

        if (maxw==0)
        {
            System.err.println("WARNING: normalizeWeight2d: maxw is zero, no sense in normalizing....");
            return;
        }
        for (int i=0;i<w.length;i++)
            for(int j=0;j<w[0].length;j++)
                w[i][j] /= sum;
    }
    */

    /*
    public int pickNoisyPDF(double[] pdf,int size,double chance)
    {
        double x;
        double agg=0;
        int pick=-1;
        //System.out.print("PDF:");
        // make a random selection
        if (Math.random() <chance)
        {
            pick=(int)(Math.random()*size);
        }
        x=Math.random();
        for (int i=0;i<size;i++)
        {
            //System.out.print(pdf[i]+" ");
            agg+=pdf[i];
            if (pick==-1)
                if (x<agg) { pick=i;}
        }
        //System.out.println("\nsum of probabilities:"+agg);
        return pick;
    }
    */

    /*
    public int pickPDF(double[] pdf,int size)
    {
        double x=Math.random();
        double agg=0;
        int pick=-1;
        //System.out.print("PDF:");
        for (int i=0;i<size;i++)
        {
            //System.out.print(pdf[i]+" ");
            agg+=pdf[i];
            if (pick==-1)
            {
                if (x<agg)
                {
                    pick=i;
                    return pick;
                }
            }
        }
        //System.out.println("\nsum of probabilities:"+agg);
        return pick;
    }
    */

    /**
     * This assumes that the max.val of pdf is stored in max_value_after_softmax
     */
    public int pick_WTA(double[] pdf, int size)
    {
        if (1==1)
            return pick_my_PDF(pdf,size);
        double maxrate=-1;
        int maxix=-1;
        int tryc=0;
        //have to return something
        if (max_value_after_softmax==-1)
        {
            System.err.println("HEY: no max value set by softmax!!");
            return (int)(size*Math.random());
        }
        //have to return something
        if (max_value_after_softmax<=1e-40)
        {
            System.err.println("HEY NoisyPDF: maxvalue set by softmax is:"+max_value_after_softmax);
            return (int)(size*Math.random());
        }

        while (maxix==-1 && tryc++<5)
        {
            for (int i=0;i<size;i++)
            {
                double rate = pdf[i]/max_value_after_softmax; //+Math.random()*WTA_ERR;
                // winner till now ?
                if (rate>maxrate)
                {
                    maxrate=rate;
                    maxix=i;
                }
            }
            if (maxix==-1)
            {
                System.out.println("WARNING: no one fired in trial:"+tryc+" max_value_after_softmax was:"+max_value_after_softmax);
            }
            else
            {
                return  maxix;
            }
        }
        //have to return something
        return (int)(size*Math.random());
    }

    public int pick_my_PDF(double[] pdf, int size)
    {
        double maxrate=-1;
        int maxix=-1;
        int tryc=0;
        //have to return something
        if (max_value_after_softmax==-1)
        {
            System.err.println("HEY: no max value set by softmax!!");
            return (int)(size*Math.random());
        }
        //have to return something
        if (max_value_after_softmax<=1e-40)
        {
            System.err.println("HEY NoisyPDF: maxvalue set by softmax is:"+max_value_after_softmax);
            return (int)(size*Math.random());
        }
        while (maxix==-1 && tryc++<5)
        {
            for (int i=0;i<size;i++)
            {
                // fires
                if (Math.random()<(pdf[i]/max_value_after_softmax)-PDFthreshold)
                {
                    // with this rate
                    double rate=Math.random();
                    // winner till now ?
                    if (rate>maxrate)
                    {
                        maxrate=rate;
                        maxix=i;
                    }
                }
            }
            if (maxix==-1)
            {
                System.out.println("WARNING: no one fired in trial:"+tryc+" max_value_after_softmax was:"+max_value_after_softmax);
            }
            else
            {
                return  maxix;
            }
        }
        //have to return something
        return (int)(size*Math.random());
    }

    /**
     * This assumes that the max.val of pdf is stored in max_value_after_softmax
     */
    public int pick_my_NoisyPDF(double[] pdf, int size,double chance)
    {
        double maxrate=-1;
        int maxix=-1;
        int tryc=0;
        //have to return something
        if (max_value_after_softmax==-1)
        {
            System.err.println("HEY NoisyPDF: no max value set by softmax!!");
            return (int)(size*Math.random());
        }
        //have to return something
        if (max_value_after_softmax<=1e-40)
        {
            System.err.println("HEY NoisyPDF: maxvalue set by softmax is:"+max_value_after_softmax);
            return (int)(size*Math.random());
        }
        // if randomness lets we return now
        if (Math.random()<chance)
            return  (int)(size*Math.random());

        while (maxix==-1 && tryc++<5)
        {
            for (int i=0;i<size;i++)
            {
                double p=(pdf[i]/max_value_after_softmax);
                // fires
                if (Math.random()<p)
                {
                    // with this rate
                    double rate=Math.random();
                    // winner till now ?
                    if (rate>maxrate)
                    {
                        maxrate=rate;
                        maxix=i;
                    }
                }
            }
            if (maxix==-1)
            {
                System.out.println(HV.setInfo("WARNING: no one fired in trial (my_noisypdf):"+tryc));
            }
            else
            {
                return maxix;
            }
        }
        //have to return something
        return (int)(size*Math.random());
    }

    public void softmax(double[] v,int size, double[] r)
    {
        //maxsoftmax (v,size,r);
        //realnormalizedsoftmax(v,size,r);
        //hardcoresoftmax(v,size,r);
        avgsoftmax (v,size,r);
        dprintln(2,"max_value_after_softmax:"+max_value_after_softmax);
    }

    /*
    public void maxsoftmax(double[] v,int size, double[] r)
    {
        double max=Math.abs(v[0]);

        max_value_after_softmax=-1;
        for (int i=0;i<size;i++)
            if (Math.abs(v[i])>max)
                max=Math.abs(v[i]);

        for (int i=0;i<size;i++)
        {
            if (v[i]>0)
                r[i]=v[i]/max;
            else
                r[i]=0;
            if (r[i]>max_value_after_softmax) max_value_after_softmax=r[i];
        }
    }
    */

    /**
     * Softmax with G(x)= 1/(1+exp(-2bX)) with b=0.5. v[] and r[] can be the same
     */
    public void avgsoftmax(double[] v,int size, double[] r)
    {
        double sum;
        sum=0;
        max_value_after_softmax=-1;
        for (int i=0;i<size;i++)
            if (v[i]>0)
                sum+=v[i];

        for (int i=0;i<size;i++)
        {
            if (v[i]>0)
                r[i]=v[i]/sum;
            else
                r[i]=0;
            if (r[i]>max_value_after_softmax)
                max_value_after_softmax=r[i];
        }
    }

    /**
     * Softmax with G(x)= 1/(1+exp(-2bX)) with b=0.5. v[] and r[] can be the same
     */
    /*
    public void hardcoresoftmax(double[] v,int size, double[] r)
    {
        double sum;
        sum=0;
        max_value_after_softmax=-1;
        //System.out.println("SOFT ====================size:"+size);
        for (int i=0;i<size;i++)
            sum+=Math.exp(softVAR*v[i]);
        //System.out.print("softmax not divided yet:");
        for (int i=0;i<size;i++)
        {
            //System.out.print(r[i]+"  ");
            r[i]=Math.exp(softVAR*v[i])/sum;
            if (r[i]>max_value_after_softmax) max_value_after_softmax=r[i];
            //System.out.println(v[i]+" -> SOFT "+r[i]);
        }
        //System.out.println("");
    }
    */

    /**
     * Softmax with G(x)= 1/(1+exp(-2bX)) with b=0.5. v[] and r[] can be the same
     */
    /*
    public void realnormalizedsoftmax(double[] v,int size, double[] r)
    {
        double sum;
        double maxv;
        sum=0;
        max_value_after_softmax=-1;
        //System.out.println("SOFT ====================size:"+size);
        maxv=Math.abs(v[0]);
        for (int i=0;i<size;i++)
            if (Math.abs(v[i])>maxv) maxv=Math.abs(v[i]);

        for (int i=0;i<size;i++)
            sum+=Math.exp(softVAR*v[i]/maxv);

        //System.out.print("softmax not divided yet:[");
        //for (int i=0;i<size;i++)
        //    System.out.print(v[i]+" ");
        //System.out.println("]");

        for (int i=0;i<size;i++)
        {
            //System.out.print(r[i]+"  ");
            r[i]=Math.exp(softVAR*v[i]/maxv)/sum;
            if (r[i]>max_value_after_softmax) max_value_after_softmax=r[i];
            //System.out.println(v[i]+" -> SOFT "+r[i]);
        }
        //System.out.println("");
        //System.out.println("max_val_after.."+max_value_after_softmax);
        if (max_value_after_softmax==-1)
        {
            System.out.println("size:"+size+" sum:"+sum);
            for (int i=0;i<size;i++)
                System.out.print(v[i]+" ");
        }
    }
    */

    /**
     * this is sigmoid
     */
    /*
    public void sigsoftmax(double[] v,int size, double[] r)
    {
        max_value_after_softmax=-1;
        for (int i=0;i<size;i++)
        {
            r[i]=1.0/(1+Math.exp(-v[i]));
            if (r[i]>max_value_after_softmax) max_value_after_softmax=r[i];
        }
    }
    */

    /*
    public void softmaxW(double[] v,int size, double[] r, double V)
    {
        double sum;
        sum=0;

        for (int i=0;i<size;i++)
            sum+=Math.exp(V*v[i]);

        for (int i=0;i<size;i++)
        {
            r[i]=Math.exp(V*v[i])/sum;
        }
    }
    */

    /**
     * we are using G(x)= 1/(1+exp(-2bX)) with b=0.5. v[] and r[] can be the same
     */
    /*
    public void squash(double[] v,int size,double[] r)
    {
        for (int i=0;i<size;i++)
            r[i]=1.0/(1+Math.exp(-v[i]));
    }
    */

    /**
     * r=Mv, im size of r, jm is size of v (M is imxjm)
     */
    /*
    public void multiply(double M[][], double[] v, int im, int jm, double r[])
    {
        for (int i=0;i<im;i++)
        {
            r[i]=0;
            for (int j=0;j<jm;j++)
                r[i]+=M[i][j]*v[j];
        }
    }
    */

    /*
    public void addto(double M[][], int im,int jm, double[][] C)
    {
        for (int i=0;i<im;i++)
            for (int j=0;j<jm;j++)
                M[i][j]+=C[i][j];
    }
    */

    /**
     * Called by Object3d's doReach() before starting the babbling session
     */
    /*
    public void initBabble()
    {
        System.out.println("Motor.initBabble() entered.");
    }
    */

    /*
    public void resetInputSeq()
    {
        inix=0;
    }
    */

    /*
    public double[] nextInput()
    {
        affordance.setTombStone(inix);
        double[] pv=affordance.pref_value(inix);
        inix++;
        if (inix>=affordance.layersize)
        {
            inix=0;
        }
        return pv;
    }
    */

    public Point3d nextRandomInput()
    {
        int k=(int)(Math.random()*affordance.layersize);
        affordance.setTombStone(k);

        /*
        for (int e=0;e<affordance.layersize;e++)
        {
            affordance.firingRate[e]=1; //affordance.firingRate[e]+1*(0.5-Math.random());
            if  (affordance.firingRate[e]>1)
                affordance.firingRate[e]=1;
            if  (affordance.firingRate[e]<0)
                affordance.firingRate[e]=0;
            System.out.println("Input:"+input0[0]+","+input0[1]+","+input0[2]);
        }
        */
        double[] pv=affordance.pref_value(k);
        System.out.println("pv:"+pv[0]+","+pv[1]+","+pv[2]);
        //affordance.setGaussBall(k,GVAR);
        //double[] pv=affordance.decode(k);

        Point3d p=convertInput(pv);
        //updateObjectLocation(p);
        updateObjectAxis(p);
        return HV.self.objlist[HV.self.curobj].objectCenter.duplicate();
    }

    /*
    static public Point3d convertInput_Loc(double[] pv)
    {
        double par=pv[0]*obj_locPAR_scale + obj_locPAR_offset;
        double mer=pv[1]*obj_locMER_scale + obj_locMER_offset;
        double rad=pv[2]*obj_locRAD_scale + obj_locRAD_offset;

        //Point3d q= HV.self.rHand.root.joint_pos.duplicate();
        //Point3d p=new Point3d(0,0,0);
        //p.x=q.x + rad*Math.cos(par)*Math.sin(mer);
        //p.y=q.y + rad*Math.sin(par);
        //p.z=q.z + rad*Math.cos(par)*Math.cos(mer);
        //return p;

        Point3d p=new Point3d(par,mer,rad);
        return p;
    }
    */

    /*
    static public Point3d convertInput(double[] pv)
    {
     	double par=pv[0]*obj_axisPAR_scale + obj_axisPAR_offset;
      	double mer=pv[1]*obj_axisMER_scale + obj_axisMER_offset;

      	Point3d p=new Point3d(par,mer,0);
      	return p;
    }
    */

    static public Point3d convertInput(double[] pv)
    {
        return convertInput(pv[0],pv[1],pv[2]);
    }

    public void updateObjectAxis(Point3d p)
    {
        HV.self.objlist[HV.self.curobj].setTilt(p);
        lasttilt_deg=180/Math.PI*p.z;
        HV.cv.refreshDisplay();
        //System.out.println("\nNEW TARGET POSITION (x,y,z):"+
        //	   HV.self.objlist[HV.self.curobj].objectCenter.str());
    }

    /*
    public void updateObjectLocation(Point3d p)
    {
        HV.self.objlist[HV.self.curobj].moveto(p.x,p.y,p.z);
        HV.cv.refreshDisplay();
        //System.out.println("\nNEW TARGET POSITION (x,y,z):"+
        //	   HV.self.objlist[HV.self.curobj].objectCenter.str());
    }
    */

    /*
    public double [] inconvertInput_Loc(Hand h, Point3d pp)
    {
        int[] temp_ix=new int[3];
        double[] v=new double[3];
        Point3d q= h.root.joint_pos;
        Point3d p=VA.subtract(pp,q);
        double rad=Math.sqrt(p.x*p.x + p.y*p.y + p.z*p.z);
        double mer;
        if (p.z==0) mer=p.x<0?-Math.PI/2:Math.PI/2;
        else mer=Math.atan(p.x/p.z);
        double par=Math.asin(p.y/rad);

        v[0]=(par - obj_locPAR_offset)/obj_locPAR_scale;
        v[1]=(mer - obj_locMER_offset)/obj_locMER_scale;
        v[2]=(rad - obj_locRAD_offset)/obj_locRAD_scale;
        return v;
    }
    */

    public double [] inconvertInput(Hand h, Point3d pp)
    {
        double[] v=new double[3];
        v[0]=(pp.z - obj_axisPAR_offset)/obj_axisPAR_scale;
        v[1]=(pp.y - obj_axisMER_offset)/obj_axisMER_scale;
        v[2]=0;
        return v;
    }

    /**
     * bypass the value and just return the index
     */
    /*
    public int [] inconvertInput_hack(Hand h, Point3d pp)
    {
        int[] temp_ix=new int[3];
        double[] v=new double[3];
        Point3d q= h.root.joint_pos;
        Point3d p=VA.subtract(pp,q);
        double rad=Math.sqrt(p.x*p.x + p.y*p.y + p.z*p.z);
        double mer;
        if (p.z==0)
            mer=p.x<0?-Math.PI/2:Math.PI/2;
        else mer=Math.atan(p.x/p.z);
        double par=Math.asin(p.y/rad);

        temp_ix[0]=(int) (obj_locPAR_code_len*(par - obj_locPAR_offset)/obj_locPAR_scale);
        temp_ix[1]=(int)(obj_locMER_code_len*(mer - obj_locMER_offset)/obj_locMER_scale);
        temp_ix[2]=(int)(obj_locRAD_code_len*(rad - obj_locRAD_offset)/obj_locRAD_scale);
        return temp_ix;
    }
    */

    /*
    public void updateInputLayer_Loc(Hand h, Graspable obj)
    {
        Point3d p=obj.objectCenter.duplicate();
        System.out.println("original objecet location:"+p.str());

        //int[] ix=inconvertInput_hack(h,p);
        //affordance.setTombStone(ix);

        double[] v=inconvertInput_Loc(h,p);
        double[] conf={1,1,1};
        int ix=affordance.encode(v,conf);
        if (PLOTLEV>0)
        {
            Gplot.resetGeom(50,50,356,356);
            affordance.plotFiringRate(this,callback_Input);
        }

        //verify
        //double[] pv=affordance.pref_value(ix);
        ////double[] pv=affordance.decode(ix);

        //Point3d q=convertInput(pv);
        //updateObjectLocation(q);
        //Point3d codep=obj.objectCenter;
	
        //System.out.println("original object location:"+p.str());
        //System.out.println("codeced  object location :"+codep.str());
        //System.out.println("coding error vector      :"+VA.subtract(p,codep).str());
        //System.out.println("coding error norm        :"+VA.dist(p,codep));
    }
    */

    public void updateInputLayer(Hand h, Graspable obj)
    {
        Point3d p=obj.tilt.duplicate();
        //System.out.println("original objecet tilt:"+p.str());

        double[] v=inconvertInput(h,p);
        //System.out.println("inconvert:"+v[0]+","+v[1]+","+v[2]);
        double conf=1.0;
        int ix=affordance.encode(v,conf);
        //System.out.println("ix of encoding:"+ix);
        if (PLOTLEV>-10)
        {
            Gplot.resetGeom(550,550,256,256);
            new Gplot(W_03,";set title 'W_03';");
            new Gplot(W_01,";set title 'W_01';");
            new Gplot(W_13,";set title 'W_13';");
            affordance.plotFiringRate(this,callback_Input);
        }

        /*
        //verify
        double[] pv=affordance.pref_value(ix);
        System.out.println("pref value of "+ix+":"+pv[0]+","+pv[1]+","+pv[2]);
        //double[] pv=affordance.decode(ix);

        Point3d q=convertInput(pv);
        System.out.println("Conversion done:"+q.str());
        updateObjectAxis(q);
        Point3d codep=obj.tilt;
	
        System.out.println("original object tilt:"+p.str());
        System.out.println("codeced  object tilt :"+codep.str());
        System.out.println("coding error vector      :"+VA.subtract(p,codep).str());
        System.out.println("coding error norm        :"+VA.dist(p,codep));
        */
    }

    public MotorPlan nextMotorPlan(Hand h, Object3d obj)
    {
        double R=obj.objectRadius;
        MotorPlan m=new MotorPlan();

        if (PLOTLEV>0)
            Gplot.resetGeom(50,50,256,256);

        forwardPass(1);  // pick according to current PDFs
        if (PLOTLEV>0)
        {
            hand_off.plotFiringRate(this,callback_Offset);
            hand_rot.plotFiringRate(this,callback_Wrist);
        }

        lastpar=offvalue[1];
        lastmer=offvalue[0];

        Point3d q=convertOffset(offvalue[0], offvalue[1], offvalue[2]);
        q.z=400;
        m.setOffset(obj.objectCenter,q);

        //Point3d thfor=convertThumb(forcevalue[2],forcevalue[3],forcevalue[4]);
        Point3d infor=convertFinger(forcevalue[0],forcevalue[1],0);
        m.setFingerRate(0*infor.x);  // hack WARNING: index ~ speed
        //learning power grasp, speed up no need to enclose :)
        m.force[0][0]=1.0000; //thfor.x;
        m.force[0][1]=0.3362; //thfor.y;
        m.force[0][2]=0.3024; //thfor.z;
        m.force[1][0]=0.8169; //infor.x;
        m.force[1][1]=0.2220; //infor.x/2;
        m.force[2][0]=m.force[1][0]*0.9;
        m.force[2][1]=m.force[1][1]*0.85; //infor.y/2;

        Point3d wr=convertWrist(rotvalue[0], rotvalue[1], rotvalue[2]);
        m.setWrist(wr);

        h.thumb.torque                   = m.force[0][0];
        h.thumb.child[0].torque          = m.force[0][1];
        h.thumb.child[0].child[0].torque = m.force[0][2];

        h.index.torque           = m.force[1][0];
        h.index.child[0].torque  = m.force[1][1];

        h.middle.torque          = m.force[2][0];
        h.middle.child[0].torque = m.force[2][1];
        h.ring.torque            = m.force[2][0];
        h.ring.child[0].torque   = m.force[2][1];
        h.pinky.torque           = m.force[2][0];
        h.pinky.child[0].torque  = m.force[2][1];

        return m;
    }

    public MotorPlan nextNoisyMotorPlan(Hand h, Object3d obj, double n1,double n2,double n3)
    {
        double R=obj.objectRadius;
        MotorPlan m=new MotorPlan();
        forwardNoisyPass(1,n1,n2,n3,m);
        if (PLOTLEV>0)
        {
            Gplot.resetGeom((int)(0.5+lasttilt_deg*6),600,400,400);
            hand_off.plotFiringRate(this,callback_Offset);
        }

        lastpar=offvalue[1];
        lastmer=offvalue[0];

        Point3d q=convertOffset(offvalue[0], offvalue[1], offvalue[2]);
        q.z=400;
        m.setOffset(obj.objectCenter,q);

        //Point3d thfor=convertThumb(forcevalue[2],forcevalue[3],forcevalue[4]);
        Point3d infor=convertFinger(forcevalue[0],forcevalue[1],0);
        m.setFingerRate(0*infor.x);  // hack WARNING: index ~ speed
        //learning power grasp, speed up no need to enclose :)
        m.force[0][0]=1.0000; //thfor.x;
        m.force[0][1]=0.3362; //thfor.y;
        m.force[0][2]=0.3024; //thfor.z;
        m.force[1][0]=0.8169; //infor.x;
        m.force[1][1]=0.2220; //infor.x/2;
        m.force[2][0]=m.force[1][0]*0.9;
        m.force[2][1]=m.force[1][1]*0.85; //infor.y/2;

        Point3d wr=convertWrist(rotvalue[0], rotvalue[1], rotvalue[2]);
        m.setWrist(wr);

        h.thumb.torque                   = m.force[0][0];
        h.thumb.child[0].torque          = m.force[0][1];
        h.thumb.child[0].child[0].torque = m.force[0][2];

        h.index.torque           = m.force[1][0];
        h.index.child[0].torque  = m.force[1][1];

        h.middle.torque          = m.force[2][0];
        h.middle.child[0].torque = m.force[2][1];
        h.ring.torque            = m.force[2][0];
        h.ring.child[0].torque   = m.force[2][1];
        h.pinky.torque           = m.force[2][0];
        h.pinky.child[0].torque  = m.force[2][1];

        return m;
    }

    public boolean doneRotGen()
    {
        return looped;
    }

    synchronized  public void resetRotGen()
    {
        rotix=0;
        looped=false;
        System.out.println("@---------------------> rotix RESET");
    }

    /**
     * modify wrist rotation part of the motor plan in an ordered fashion, In each call next preferred value is
     * returned. Note that this is kind of fake. It reads the preferred value from the network but the network is
     * not notified. If you wanna learn using this you need to fake good
     **/
    synchronized  public void pickOrdRotation(MotorPlan m)
    {
        System.out.println("@---------------------> rotix:"+rotix+" hand_rot.firinglen:"+hand_rot.layersize);
        hand_rot_pick=rotix++;
        if (rotix >= hand_rot.layersize) { rotix=0; looped=true;}
        rotvalue=hand_rot.pref_value(hand_rot_pick);
        hand_rot.setGaussBall(hand_rot_pick,GVAR);
        // we overwrite (or write on elig, check PopulationCode) the hand_rot layer
        // with 0s except the choosen rotation. This way we can use the
        // grasp experience in learning....

        //rotvalue=hand_rot.decode(hand_rot_pick);
        //System.out.println("@" + rotvalue[1]+","+rotvalue[2]+","+rotvalue[0]);

        Point3d wr=convertWrist(rotvalue[0], rotvalue[1], rotvalue[2]);
        m.setWrist(wr);
        System.out.println("@TILT: "+180/Math.PI*m.tilt+"   HEADING: "+180/Math.PI*m.heading+"  BANK: "+180/Math.PI*m.bank);
    }

    /**
     * note that if net3 is not called [via advancePass for example] the pdf will not be updated
     */
    /*
    public void pickRotation(MotorPlan m)
    {
        //System.out.print("===> ROT ");
        //hand_rot_pick=pick_my_PDF(hand_rot.prob,hand_rot.layersize);
        hand_rot_pick=pick_my_PDF(hand_rot.firingRate,hand_rot.layersize);
        rotvalue=hand_rot.decode(hand_rot_pick);
        //hand_rot.setTombStone(hand_rot_pick,1.0);
        hand_rot.setGaussBall(hand_rot_pick,GVAR);
        m.rotationPOPIX=hand_rot_pick;
        Point3d wr=convertWrist(rotvalue[0], rotvalue[1], rotvalue[2]);
        m.setWrist(wr);
        System.out.println("@TILT: "+180/Math.PI*m.tilt+"   HEADING: "+180/Math.PI*m.heading+"  BANK: "+180/Math.PI*m.bank+" <pickRotation>");
    }
    */

    /**
     * note1: that if net3 is not called [via advancePass for example] the pdf will not be updated
     * note2: The hand_rot, hand_rot.firingRate will be overwritten with the gaussball
      */
    public void pickNoisyRotation(MotorPlan m,double chance)
    {
        //hand_rot_pick=pick_my_NoisyPDF(hand_rot.prob,hand_rot.layersize,chance);
        hand_rot_pick=pick_my_NoisyPDF(hand_rot.firingRate,hand_rot.layersize,chance);
        if (PLOTLEV>0)
        {
            /*
            int wi=1024/hand_locMER_code_len;
      	    int he=768 /hand_locPAR_code_len;
      	    int x=(int)(lastmer*hand_locMER_code_len);
      	    int y=(int)(lastpar*hand_locPAR_code_len);
      	    Gplot.resetGeom(x*wi,y*he,wi,he);
            */
            Gplot.resetGeom((int)(0.5+lasttilt_deg*6),100,400,400);
            hand_rot.plotFiringRate(this,callback_Wrist);
        }

        rotvalue=hand_rot.pref_value(hand_rot_pick);
        //hand_rot.setTombStone(hand_rot_pick); //CHA, CHA3
        hand_rot.setGaussBall(hand_rot_pick,GVAR);
        m.rotationPOPIX=hand_rot_pick;

        Point3d wr=convertWrist(rotvalue[0], rotvalue[1], rotvalue[2]);
        m.setWrist(wr);
        //System.out.println("@TILT: "+180/Math.PI*m.tilt+"   HEADING: "+180/Math.PI*m.heading+"  BANK: "+180/Math.PI*m.bank+" <pickRotation>");
    }

    /**
     * Samples from hand_rot.firingRate without destroying it
     * @param m
     */
    public void safe_pickRotation(MotorPlan m)
    {
        //System.out.print("===> ROT ");
        //hand_rot_pick=pick_my_PDF(hand_rot.prob,hand_rot.layersize);
        hand_rot_pick=pick_my_PDF(hand_rot.firingRate,hand_rot.layersize);
        rotvalue=hand_rot.pref_value(hand_rot_pick); // or pref_value ? no random maybe OK
        // if the layer is all zero, then a random return occurs which makes 0 very high
        // occurance as rotvalue, so I changed to pref_value
        m.rotationPOPIX=hand_rot_pick;
        Point3d wr=convertWrist(rotvalue[0], rotvalue[1], rotvalue[2]);
        m.setWrist(wr);
        if (PLOTLEV>0)
        {
            hand_rot.plotFiringRate(this,callback_Wrist);
        }
        //System.out.println("@TILT: "+180/Math.PI*m.tilt+"   HEADING: "+180/Math.PI*m.heading+"  BANK: "+180/Math.PI*m.bank+" <pickotation>");
    }

    public void updateWristLayer()
    {
        forwardPass(3);   // use the current finger and offset to generate
    }

    /*
    public static void main(String[] argv)
    {
        MotorPlan mp=null;
        Motor m=new Motor();
        Hand h=new Hand("erharm.seg",5,7);
        Graspable box=new Graspable(h,"box.seg",0,5,"PRECISION");
        for (int i=0;i<200;i++)
        {
            mp=m.nextMotorPlan(h,box);
            System.out.println("@  "+Elib.snice(mp.tilt,1e3,6)+" "+Elib.snice(mp.bank,1e3,6));
        }
    }
    */

    private void dprint(String s)
    {
        if (1>DLEV)
            return;
        System.out.print(s);
    }

    /*
    private void dprint(int level,String s)
    {
        if (level>DLEV)
            return;
        System.out.print(s);
    }
    */

    /*
    private void dprintln(String s)
    {
        if (1>DLEV)
            return;
        System.out.println(s);
    }
    */

    private void dprintln(int level,String s)
    {
        if (level>DLEV)
            return;
        System.out.println(s);
    }

    synchronized public void dumpNet(String prefix, String suffix, String us)
    {
        Elib.array2file(W_01,prefix+"W_01"+suffix,"W_01 "+Elib.arrayInfo(W_01)+"\n\n#"+us+"\n\n");
        Elib.array2file(W_02,prefix+"W_02"+suffix,"W_02 "+Elib.arrayInfo(W_02)+"\n\n#"+us+"\n\n");
        Elib.array2file(W_03,prefix+"W_03"+suffix,"W_03 "+Elib.arrayInfo(W_03)+"\n\n#"+us+"\n\n");

        Elib.array2file(W_13,prefix+"W_13"+suffix,"W_13 "+Elib.arrayInfo(W_13)+"\n\n#"+us+"\n\n");
        Elib.array2file(W_23,prefix+"W_23"+suffix,"W_23 "+Elib.arrayInfo(W_23)+"\n\n#"+us+"\n\n");
    }

    public void dumpNet(String suffix)
    {
        dumpNet("",suffix,"--------------------------------------------------");
    }

    /*
    public void dumpNet(String prefix, String suffix )
    {
        dumpNet(prefix,suffix,"--------------------------------------------------");
    }
    */

    public void loadNet(String prefix,String suffix)
    {
        W_01=Elib.file2array(prefix+"W_01"+suffix);
        W_02=Elib.file2array(prefix+"W_02"+suffix);
        W_03=Elib.file2array(prefix+"W_03"+suffix);
        W_13=Elib.file2array(prefix+"W_13"+suffix);
        W_23=Elib.file2array(prefix+"W_23"+suffix);
    }

    /*
    public void loadNet(String suffix)
    {
        loadNet("",suffix);
    }
    */
} // Motor

/*
 *
 * Erhan Oztop, 2000-2002  <br>
 * Source code by Erhan Oztop (erhan@atr.co.jp) <br>
 * Copyright August 2002 under <br>
 * University of Southern California Ph.D. publication copyright <br>
 */
 
