# don't use tabs!!
# <> means this is dynamically loadable with source command
#------- HV.java    ------------
DLEV            0         # debug level
FANCY           0         # animation level -1, 0, +1...
slipCost        0.1       # 0 for none 0.1 is pretty high!
minPAR        -45          # these are object locations
maxPAR         45
minMER        -45
maxMER         45
minRAD        700
maxRAD       1300
obj_locPAR_code_len    10
obj_locMER_code_len    10
obj_locRAD_code_len     1
obj_encode_var          1

minTILT        0          # these are object axis
maxTILT        90 # 180


# Good move the bar to the right side edge align the reset eye
WeightFileDir   infant1/poorvision/
WeightFile      .LGM-1018940921
#WeightFileDir  good-rightside/
#WeightFile      .LGM-1018684643
### Use this for plate
##WeightFileDir  good-prec-plate/
##WeightFile      .LGM-LAST-1016844923
##### Use this for coin
##WeightFileDir  good-prec-coin/
##WeightFile     .LGM-LAST-1016753470
###

plotLabel       1         # <> shall Motor.java label the axis ?
plotKey         1         # <> shall Gplot axis/ticks/key ?
#------- Motor.java -------------
motor.DLEV      0         # motor debug level
motor.PLOTLEV   0         # plots shown in each GRASP or WRISTGRASP ?
eta             0.5        # base learning rate
GVAR            0.75      #0.74   # this gausball variance  [not effective now]
softVAR         2         # this 1/x where x is the softmax variance	
PDFthreshold    0.2       # in generating actions min. req prob.
WTA_ERR	        0.0       # the WTA maybe wrong by this amount (in prob units)


hand_rotBANK_code_len   9  # the wrist rotations of the hand
hand_rotPITCH_code_len  9  # ""
hand_rotHEADING_code_len 1  # the heading of the hand

hand_locMER_code_len     7  # allocentric target hand position  
hand_locPAR_code_len     7 
hand_locRAD_code_len     1

newton_MAX_IT         1000  # #iter. to gradient descend for force balance
#------- for Object3d.java -----
MAXREACH          30 # 10       # after each XX reaches change the input condition
MAXROTATE         25 #20 #50 # 30       # after each XX wrist trials, try next obj-off reach
MAXBABBLE         200000 # total babbles to be done
weightSave        4500  # save after each #weightsave babbles
costThreshold     0.75    # graspCosts  < this are good
palmThreshold     150    # less the distance means palm is close to the object
negReinforcement  -0.05   # 
Reach2Target     MIDDLE0  # {INDEX,MIDDLE,THUMB}x{0,1,2}   0 is the 1st knuckle 2 is the tip
                         # use THUMBTIP for the tip of the thumb 
#------- for LearnPDF and Object3d ----

rotRandomness     1      # 1=full random 0=from the pdf
offRandomness     1      # 1=full random 0=from the pdf


#------- Graspable.java -------------

softconGAIN 250     # When the softconstraint gradient is active (default) this used
                    # adjust the weight of soft constraint

#bgColor_R  0
#bgColor_G  0
#bgColor_B  0
#
#fgColor_R  0
#fgColor_G  255
#fgColor_B  255

bgColor_R  0
bgColor_G  255
bgColor_B  255

fgColor_R  0
fgColor_G  0
fgColor_B  0

drawCube   0   # use cube+/- from sim. commandline to toggle
