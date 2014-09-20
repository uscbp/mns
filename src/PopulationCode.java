

/**
 *
 * @author Erhan Oztop, 2001-2002 <br>
 * <br>
 * Source code by Erhan Oztop (erhan@atr.co.jp) <br>
 * Copyright August 2002 via <br>
 * University of Southern California Ph.D. publication copyright <br>
 * This class keeps and array of doubles o represent the cell activity of a region.
 */
public class PopulationCode
{
    // Maximum 20 dimension of encoding!!
    static public int maxDIM=20;
    // Name of the layer
    public String layername;
    // Layer dimensionComment
    public String layerComment;
    // Size of the layer (number of units space is allocated for)
    public int layersize;
    // Actual size of the layer (total number of units actually added)
    public int actualsize;
    // Number of dimensions
    public int dimensionCount;
    // Size of the layer in each dimension
    public  int[] dimensionSize =new int[maxDIM];
    // Offset for each dimension in the main array
    public int[] dimensionOffset =new int[maxDIM];
    // Name of each dimension
    public String[] dimensionName =new String[maxDIM];
    // Comment for each dimension
    public String[] dimensionComment =new String[maxDIM];
    // Minimum of the encoded value in each dimension
    public double[] min=new double[maxDIM];
    // Maximum of the encoded value in each dimension
    public double[] max=new double[maxDIM];
    // Increment of the encoded value in each dimension
    public double[] prec=new double[maxDIM];
    // Variance of the population code
    public double variance;
    // Firing rate of each unit in the layer
    public double[] firingRate;
    // Synapses onto each unit in the layer
    public double[][] synapse;
    // Number of synapses on each unit
    int synapseCount;
    // Default minimum preferred value for a unit
    static final double def_min=0;
    // Default maximum preferred value for a unit
    static final double def_max=1;
    // Default variance of the population code
    static final double def_var=0.5;

    /**
     * Constructor
     * @param lname - Name of the layer
     * @param lsize - Number of units to allocate space for
     * @param lcomment - Layer comment
     * @param var - Variance of the population code
     */
    public PopulationCode(String lname, int lsize, String lcomment, double var)
    {
        dimensionCount =0;
        layername=lname;
        layersize=lsize;
        layerComment=lcomment;
        firingRate =new double[layersize];
        variance = var;
    }

    /** this is OK*/
    public Point3d[] getAdvAvgSheet01(int callback)
    {
        if (dimensionCount !=3)
            return null;
        double [][] M=getAvgSheet01();
        Point3d [] D=new Point3d[(M.length+1)* M[0].length];  // +1 is for nulls
        int k=0;

        for  (int i1=0;i1<dimensionSize[1];i1++)
        {
            for (int i0=0;i0<dimensionSize[0];i0++)
            {
                int ix=i1*dimensionOffset[1]+i0*dimensionOffset[0];
                double[] v=pref_value(ix);  // can speed up
                Point3d wr=Motor.convert(callback,v[0],v[1],v[2]); //
                D[k++] = Motor.map2xy(callback,wr,M[i0][i1]);
            }
            D[k++]=null;
        }
        return D;
    }

    /**
     * Gets a 2d representation of a 3d layer's firing rates
     * @return - Matrix of the layer's firing rates (summed over the 3rd dimension)
     */
    public double[][] getAvgSheet01()
    {
        if (dimensionCount !=3)
            return null;
        double [][] M=new double[dimensionSize[0]][dimensionSize[1]];
        for  (int i0=0;i0<dimensionSize[0];i0++)
        {
            for (int i1=0;i1<dimensionSize[1];i1++)
            {
                M[i0][i1]=0;
                // Sum over the 3rd dimension
                for (int i2=0;i2<dimensionSize[2];i2++)
                {
                    M[i0][i1] += firingRate[i2*dimensionOffset[2]+i1*dimensionOffset[1]+i0*dimensionOffset[0]];
                }
            }
        }
        return M;
    }

    /**
     * Plot firing rate of the layer
     * @param m
     * @param callback
     */
    public void plotFiringRate(Motor m,int callback)
    {
        Point3d[] H=getAdvAvgSheet01(callback);

        new Gplot(H,m.getTempPrefix(callback),m.getGplotCommand(callback));
    }

    /**
     * Add a dimension to the layer
     * @param name - Name of the dimension
     * @param size - Size of the dimension
     */
    public void addDimension(String name,int size)
    {
        addDimension(name,def_min,def_max,size,"");
    }

    /**
     * Add a dimension to the layer
     * @param name - Name of the dimension
     * @param min - Minimum of the encoded value in the dimension
     * @param max - Maximum of the encoded value in the dimension
     * @param size - Size of the dimension
     * @param comment - Comment for the dimension
     */
    public void addDimension(String name,double min,double max,int size,String comment)
    {
        if (dimensionCount ==0)
            actualsize=size;
        else
            actualsize*=size;
        if (dimensionCount ==0)
            dimensionOffset[dimensionCount]=1;
        else
            dimensionOffset[dimensionCount]=dimensionOffset[dimensionCount-1]*dimensionSize[dimensionCount -1];
        this.dimensionName[dimensionCount]=name;
        this.dimensionComment[dimensionCount]=comment;
        this.min[dimensionCount]=min;
        this.max[dimensionCount]=max;
        this.dimensionSize[dimensionCount]=size;
        if (size>1)
            this.prec[dimensionCount]=(max-min)/(size-1);
        else
            this.prec[dimensionCount]=(max-min)/(size);

        dimensionCount++;
        System.out.println("New dimension:"+name+" added with size:"+size);
        System.out.println("The actual layer size became:"+actualsize);
        System.out.println("The total # of dimensions became:"+dimensionCount);
    }

    /**
     * Returns the values that the specified unit encodes in each dimension
     * @param IX - Index of the unit in each dimension
     * @return - Array of values that the unit encodes in each dimension
     */
    public double[] pref_value(int[] IX)
    {
        double[] pr=new double[dimensionCount];
        for (int i=0;i<dimensionCount;i++)
            pr[i]=prec[i]*(IX[i])+min[i];
        return pr;
    }

    /**
     * Returns the values that the specified unit encodes in each dimension
     * @param lc - Index of the unit in the main array
     * @return - Array of values that the unit encodes in each dimension
     */
    public double[] pref_value(int lc)
    {
        return pref_value(decompressIX(lc));
    }

    /** Reads off the vector valued coarse coding. */
    /*
    public double[] decode(int C)
    {
        return decode(decompressIX(C));
    }
    */

    /**
     * Calculates the vector valued coarse coding of the specified unit.
     * this is wrong! Have to be local summing! How do you get double peaks workg?
     * @param IX - Index of the unit in each dimension
     * @return - Preferred value of the specified unit in each dimension weighted by firing rates of units in that dimension
     */
    /*
    public double[] decode(int[] IX)
    {
        int ix;
        // Calculate weighted sum of encoded values in each dimension and the sum of firing values
        double[] vsum=new double[dimensionCount],tsum=new double[dimensionCount];
        //get the preferred values of the unit in each dimension
        double[] prefs=pref_value(IX);

        // for each dimension
        for (int k=0;k<dimensionCount;k++)
        {
            // save it, we will overwrite
            ix=IX[k];
            vsum[k]=0; tsum[k]=0;
            // Loop over each unit in this dimension
            for (int i=0;i<dimensionSize[k];i++)
            {
                IX[k]=i;
                double firinglevel=getFiringRate(IX);
                vsum[k]+=prefs[k]*firinglevel;
                tsum[k]+=firinglevel;
            }
            // firingRate may not add up to one, normalize
            if (tsum[k]!=0)
                vsum[k]/=tsum[k];
            // restore the value
            IX[k]=ix;
        }
        return vsum;
    }
    */
    
    /**
     * Computes the index of the specified unit in the main array
     * @param IX - Index of a unit in each dimension
     * @return - Index of the specified unit in the main array
     */
    private int compressIX(int[] IX)
    {
        int sum=IX[dimensionCount -1];
        for (int i=dimensionCount -2;i>=0;i--)
            sum=sum*dimensionSize[i]+IX[i];
        return sum;
    }

    /**
     * Computes the index of the specified unit in each dimension
     * @param C - Index of a unit in the main array
     * @return - Index of the specified unit in each dimension
     */
    private int[] decompressIX(int C)
    {
        int[] IX=new int[dimensionCount];
        for (int i=dimensionCount -1;i>=0;i--)
        {
            IX[i]=C/dimensionOffset[i];
            C=C%dimensionOffset[i];
        }
        return IX;
    }

    /**
     * Encodes the given value in the firing rates of the layer
     * @param value - The n-dimensional vector to encode
     * @param confidence Confidence in the encoded value
     * @return - Index of the unit with the preferred value closest to the encoded value in the main array
     */
    public int encode(double[] value, double confidence)
    {
        int winner=-1;

        // if out of boundary force to the limits
        for (int i=0;i<dimensionCount;i++)
        {
            if (value[i]<min[i])
                value[i]=min[i];
            if (value[i]>max[i])
                value[i]=max[i];
        }

        double mindis=1e15;
        // Loop through each unit
        for (int lc=0;lc<actualsize;lc++)
        {
            // Get the preferred value of the unit in each dimension
            double[] prefs=pref_value(decompressIX(lc));
            // Get squared euclidean distance between the encoded value and the unit's preferred values
            double diff=0;
            for (int i=0; i < dimensionCount; i++)
                diff+=(prefs[i]-value[i])*(prefs[i]-value[i]);
            // Pick the unit with the preferred value closest to the encoded value
            if (diff < mindis)
            {
                mindis=diff;
                winner=lc;
            }
        }

        if (winner==-1)
        {
            System.err.println("RCode.encode : ERROR cannot find winner !!! ");
        }

        int[] winnerIX=decompressIX(winner);
        /*
        // this is not very good because it encodes the value as a plus
        // instead of a ball around the best point
        int[] IX=decompressIX(winner);
        for (int k=0;k<dimensionCount;k++)
        {
            int ix=IX[k];
            for (int i=0; i<dimensionSize[k]; i++)
            {
                IX[k]=i;
                setFiringRate(IX,confidence[k] * Math.exp(-euclideanDistance(IX,winnerIX)/variance[k]));
            }
            setFiringRate(winnerIX,1.0);
        }
        */

        // this has to be improved by probably adding the covariance matrix in distance
        // metric. Currently it ignores the variance and makes perfect sphere around the
        // winner instead of a ellipse for example.

        // Loop through each unit
        for (int lc=0;lc<actualsize;lc++)
        {
            int[] IX=decompressIX(lc);
            // Compute the unit's firing rate as a gaussian centered around the winning unit times the confidence
            double act=confidence * Math.exp(-euclideanDistance(IX,winnerIX)/variance);
            if (act<0.00001)
                act=0;
            setFiringRate(lc,act);
        }
        return winner;
    }

    /**
     * Euclidean distance
     * @param IX1 - Index of the first unit in each dimension
     * @param IX2 - Index of the second unit in each dimension
     * @return - Euclidean distance between the two specified units
     */
    public double euclideanDistance(int[] IX1, int[] IX2)
    {
        double sum=0;
        for (int i=0;i<dimensionCount;i++)
            sum+=(IX1[i]-IX2[i])*(IX1[i]-IX2[i]);
        return sum;
    }

    /**
     * Euclidean distance
     * @param IX1 - Index of the first unit in each dimension
     * @param C2 - Index of the second unit in the main array
     * @return - Euclidean distance between the two specified units
     */
    public double euclideanDistance(int[] IX1, int C2)
    {
        double sum=0;
        int[] IX2=decompressIX(C2);
        for (int i=0;i<dimensionCount;i++)
            sum+=(IX1[i]-IX2[i])*(IX1[i]-IX2[i]);
        return sum;
    }

    /**
     * Sets a units firing rate
     * @param ix - Index of the unit in the main array
     * @param val - Unit's firing rate
     */
    public void setFiringRate(int ix, double val)
    {
        firingRate[ix]=val;
    }

    /**
     * Sets a units firing rate
     * @param IX - Index of the unit in each dimension
     * @param val - Unit's firing rate
     */
    public void setFiringRate(int[] IX,double val)
    {
        firingRate[compressIX(IX)]=val;
    }

    /**
     * Gets the specified unit's firing rate
     * @param IX - Index of the unit in each dimension
     * @return - The unit's firing rate
     */
    public double getFiringRate(int[] IX)
    {
        return firingRate[compressIX(IX)];
    }

    /**
     * Sets all firing rates to 0, except that of the specified unit, set to 1
     * @param C - Index of the unit in the main array
     */
    public void setTombStone(int C)
    {
        for (int i=0;i<layersize;i++)
            firingRate[i]=0;
        firingRate[C]=1;
    }

    /**
     * Sets the population firing rates in a gaussian distribution centered around the given unit with the given
     * variance
     * @param C - Index of the unit in the main array at the center of the distribution
     * @param var2 - Variance of the distribution
     */
    public void setGaussBall(int C, double var2)
    {
        // Get the index of the unit in each dimension
        int[] IX=decompressIX(C);
        double sum=0;

        // For each unit
        for (int i=0;i<layersize;i++)
        {
            // Get the euclidean distance between it and the center unit
            double d=euclideanDistance(IX,i);
            // Set its firing rate using the gaussian equation
            firingRate[i]=Math.exp(-d*d/(2*var2));
            sum+=firingRate[i];
        }
        // Normalize so that the population firing rates represent a probability distribution
        for (int i=0;i<layersize;i++)
        {
            firingRate[i] /= sum;
        }
    }

    /**
     * Creates multiplicative synapses (cache vectors to be multiplied)
     * @param howmany - Number of synapses to create on each unit
     */
    public void makeSynapse(int howmany)
    {
        synapseCount =howmany;
        synapse=new double[howmany][];
        for (int i=0;i<howmany;i++)
        {
            synapse[i]=new double[layersize];
        }
    }

    /**
     * Multiply the synaptic probabilities to get the net probability
     */
    public void mergeSynapse()
    {
        for (int k=0;k<layersize;k++)
        {
            //prob[k]=1;
            firingRate[k]=1;
            for (int i=0;i<synapseCount;i++)
            {
                //prob[k]*=synapse[i][k];
                firingRate[k]*=synapse[i][k];
            }
        }
    }

    public static void main(String[] argv)
    {
        PopulationCode p1=new PopulationCode("testLayer",5*10*12,"testing",1);
        p1.addDimension("X",0,1e4,12,"X axis");
        p1.addDimension("Y",0,1e4,10,"Y axis");
        p1.addDimension("Z",0,1e4, 5,"Z axis");
        int[] vect=new int[3];
        for (int x=0;x<12;x++)
        {
            for (int y=0;y<10;y++)
            {
                for (int z=0;z<5;z++)
                {
                    vect[0]=x; vect[1]=y; vect[2]=z;
                    p1.setFiringRate(vect,x*y*z);
                    double verify=p1.getFiringRate(vect);
                    System.out.println((x*y*z)+"=="+verify);
                }
            }
        }

        double[] val={7500,2500,5000};
        double conf=1.0;

        for (int x=0;x<12;x++)
        {
            for (int y=0;y<10;y++)
            {
                for (int z=0;z<5;z++)
                {
                    vect[0]=x; vect[1]=y; vect[2]=z;
                    double v=p1.getFiringRate(vect);
                    System.out.println(v+" == "+x*y*z);
                }
            }
        }
        p1.encode(val,conf);
        for (int i=0;i<p1.actualsize;i++)
        {
            System.out.print(p1.firingRate[i]+"  ");
            if (i%12==0) System.out.println("       -> X comp end.");
            if (i%(12*10)==0) System.out.println("     -> Y comp end.");
        }
    } //main
} // PopulationCode
/*
 *
 * Erhan Oztop, 2000-2002  <br>
 * Source code by Erhan Oztop (erhan@atr.co.jp) <br>
 * Copyright August 2002 under <br>
 * University of Southern California Ph.D. publication copyright <br>
 */
 
