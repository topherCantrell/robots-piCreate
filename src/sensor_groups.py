from sensor_packets import *

class Group_0:
    ID = 0
    def __init__(self):    
        # 26, 7-26
        self.group = {
            'bumpsAndWheelDrops'    : BumpsAndWheelDrops(),    #  7 1
            'wall'                  : Wall(),                  #  8 1
            'cliffLeft'             : CliffLeft(),             #  9 1
            'cliffFrontLeft'        : CliffFrontLeft(),        # 10 1
            'cliffFrontRight'       : CliffFrontRight(),       # 11 1
            'cliffRight'            : CliffRight(),            # 12 1
            'virtualWall'           : VirtualWall(),           # 13 1
            'wheelOvercurrents'     : WheelOvercurrents(),     # 14 1
            'dirtDetect'            : DirtDetect(),            # 15 1            
                                                               # 16 1 UNUSED
            'infraredCharacterOmni' : InfraredCharacterOmni(), # 17 1
            'buttons'               : Buttons(),               # 18 1
            'distance'              : Distance(),              # 19 2
            'angle'                 : Angle(),                 # 20 2
            'chargingState'         : ChargingState(),         # 21 1
            'batteryVoltage'        : BatteryVoltage(),        # 22 2
            'batteryCurrent'        : BatteryCurrent(),        # 23 2
            'batteryTemperature'    : BatteryTemperature(),    # 24 1
            'batteryCharge'         : BatteryCharge(),         # 25 2
            'batteryCapacity'       : BatteryCapacity()        # 26 2
        }
        
class Group_1:
    ID = 1
    def __init__(self):    
        # 10, 7-16
        self.group = {
            'bumpsAndWheelDrops' : BumpsAndWheelDrops(),  #  7 1
            'wall'               : Wall(),                #  8 1
            'cliffLeft'          : CliffLeft(),           #  9 1
            'cliffFrontLeft'     : CliffFrontLeft(),      # 10 1
            'cliffFrontRight'    : CliffFrontRight(),     # 11 1
            'cliffRight'         : CliffRight(),          # 12 1
            'virtualWall'        : VirtualWall(),         # 13 1
            'wheelOvercurrents'  : WheelOvercurrents(),   # 14 1
            'dirtDetect'         : DirtDetect()           # 15 1            
                                                          # 16 1 UNUSED
        }
        
class Group_2:
    ID = 2
    def __init__(self):    
        # 6, 17-20
        self.group = {            
            'infraredCharacterOmni' : InfraredCharacterOmni(), # 17 1
            'buttons'               : Buttons(),               # 18 1
            'distance'              : Distance(),              # 19 2
            'angle'                 : Angle()                  # 20 2
        }
            
class Group_3:
    ID = 3
    SIZE = 10    
    def __init__(self):        
        self.chargingState      = ChargingState()      # 21 1
        self.batteryVoltage     = BatteryVoltage()     # 22 2
        self.batteryCurrent     = BatteryCurrent()     # 23 2
        self.batteryTemperature = BatteryTemperature() # 24 1
        self.batteryCharge      = BatteryCharge()      # 25 2
        self.batteryCapacity    = BatteryCapacity()    # 26 2
    def decode(self,packet,ofs):
        self.chargingState.decode(packet,ofs);      ofs+=1
        self.batteryVoltage.decode(packet,ofs);     ofs+=2
        self.batteryCurrent.decode(packet,ofs);     ofs+=2
        self.batteryTemperature.decode(packet,ofs); ofs+=1
        self.batteryCharge.decode(packet,ofs);      ofs+=2
        self.batteryCapacity.decode(packet,ofs)
    def __repr__(self):
        return (self.chargingState.__repr__()      +'\n'+
                self.batteryVoltage.__repr__()     +'\n'+
                self.batteryCurrent.__repr__()     +'\n'+
                self.batteryTemperature.__repr__() +'\n'+
                self.batteryCharge.__repr__()      +'\n'+
                self.batteryCapacity.__repr__())                   

class Group_4:
    ID = 4
    def __init__(self):    
        # 14, 27-34
        self.group = {
            'wallSignal'               : WallSignal(),              # 27 2
            'cliffLeftSignal'          : CliffLeftSignal(),         # 28 2
            'cliffFrontLeftSignal'     : CliffFrontLeftSignal(),    # 29 2
            'cliffFrontRightSignal'    : CliffFrontRightSignal(),   # 30 2
            'cliffRightSignal'         : CliffRightSignal(),        # 31 2
                                                                    # 32 2 UNUSED
                                                                    # 33 1 UNUSED
            'chargingSourcesAvailable' : ChargingSourcesAvailable() # 34 1
        }
        
class Group_5:
    ID = 5
    def __init__(self):    
        # 12, 35-42
        self.group = {
            'oIMode'                 : OIMode(),                 # 35 1
            'songNumber'             : SongNumber(),             # 36 1
            'songPlaying'            : SongPlaying(),            # 37 1
            'numberStreamPackets'    : NumberStreamPackets(),    # 38 1
            'requestedVelocity'      : RequestedVelocity(),      # 39 2
            'requestedRadius'        : RequestedRadius(),        # 40 2
            'requestedRightVelocity' : RequestedRightVelocity(), # 41 2
            'requestedLeftVelocity'  : RequestedLeftVelocity(),  # 42 2            
        }
        
class Group_6:
    ID = 6
    def __init__(self):    
        # 52, 7-42
        self.group = {
            'bumpsAndWheelDrops'       : BumpsAndWheelDrops(),       #  7 1
            'wall'                     : Wall(),                     #  8 1
            'cliffLeft'                : CliffLeft(),                #  9 1
            'cliffFrontLeft'           : CliffFrontLeft(),           # 10 1
            'cliffFrontRight'          : CliffFrontRight(),          # 11 1
            'cliffRight'               : CliffRight(),               # 12 1
            'virtualWall'              : VirtualWall(),              # 13 1
            'wheelOvercurrents'        : WheelOvercurrents(),        # 14 1
            'dirtDetect'               : DirtDetect(),               # 15 1            
                                                                     # 16 1 UNUSED
            'infraredCharacterOmni'    : InfraredCharacterOmni(),    # 17 1
            'buttons'                  : Buttons(),                  # 18 1
            'distance'                 : Distance(),                 # 19 2
            'angle'                    : Angle(),                    # 20 2
            'chargingState'            : ChargingState(),            # 21 1
            'batteryVoltage'           : BatteryVoltage(),           # 22 2
            'batteryCurrent'           : BatteryCurrent(),           # 23 2
            'batteryTemperature'       : BatteryTemperature(),       # 24 1
            'batteryCharge'            : BatteryCharge(),            # 25 2
            'batteryCapacity'          : BatteryCapacity(),          # 26 2
            'wallSignal'               : WallSignal(),               # 27 2
            'cliffLeftSignal'          : CliffLeftSignal(),          # 28 2
            'cliffFrontLeftSignal'     : CliffFrontLeftSignal(),     # 29 2
            'cliffFrontRightSignal'    : CliffFrontRightSignal(),    # 30 2
            'cliffRightSignal'         : CliffRightSignal(),         # 31 2
                                                                     # 32 2 UNUSED
                                                                     # 33 1 UNUSED
            'chargingSourcesAvailable' : ChargingSourcesAvailable(), # 34 1
            'oIMode'                   : OIMode(),                   # 35 1
            'songNumber'               : SongNumber(),               # 36 1
            'songPlaying'              : SongPlaying(),              # 37 1
            'numberStreamPackets'      : NumberStreamPackets(),      # 38 1
            'requestedVelocity'        : RequestedVelocity(),        # 39 2
            'requestedRadius'          : RequestedRadius(),          # 40 2
            'requestedRightVelocity'   : RequestedRightVelocity(),   # 41 2
            'requestedLeftVelocity'    : RequestedLeftVelocity(),    # 42 2 
        }
    
class Group_100:
    ID = 100
    def __init__(self):    
        # 80, 7-58
        self.group = {
            'bumpsAndWheelDrops'       : BumpsAndWheelDrops(),       #  7 1
            'wall'                     : Wall(),                     #  8 1
            'cliffLeft'                : CliffLeft(),                #  9 1
            'cliffFrontLeft'           : CliffFrontLeft(),           # 10 1
            'cliffFrontRight'          : CliffFrontRight(),          # 11 1
            'cliffRight'               : CliffRight(),               # 12 1
            'virtualWall'              : VirtualWall(),              # 13 1
            'wheelOvercurrents'        : WheelOvercurrents(),        # 14 1
            'dirtDetect'               : DirtDetect(),               # 15 1            
                                                                     # 16 1 UNUSED
            'infraredCharacterOmni'    : InfraredCharacterOmni(),    # 17 1
            'buttons'                  : Buttons(),                  # 18 1
            'distance'                 : Distance(),                 # 19 2
            'angle'                    : Angle(),                    # 20 2
            'chargingState'            : ChargingState(),            # 21 1
            'batteryVoltage'           : BatteryVoltage(),           # 22 2
            'batteryCurrent'           : BatteryCurrent(),           # 23 2
            'batteryTemperature'       : BatteryTemperature(),       # 24 1
            'batteryCharge'            : BatteryCharge(),            # 25 2
            'batteryCapacity'          : BatteryCapacity(),          # 26 2
            'wallSignal'               : WallSignal(),               # 27 2
            'cliffLeftSignal'          : CliffLeftSignal(),          # 28 2
            'cliffFrontLeftSignal'     : CliffFrontLeftSignal(),     # 29 2
            'cliffFrontRightSignal'    : CliffFrontRightSignal(),    # 30 2
            'cliffRightSignal'         : CliffRightSignal(),         # 31 2
                                                                     # 32 2 UNUSED
                                                                     # 33 1 UNUSED
            'chargingSourcesAvailable' : ChargingSourcesAvailable(), # 34 1
            'oIMode'                   : OIMode(),                   # 35 1
            'songNumber'               : SongNumber(),               # 36 1
            'songPlaying'              : SongPlaying(),              # 37 1
            'numberStreamPackets'      : NumberStreamPackets(),      # 38 1
            'requestedVelocity'        : RequestedVelocity(),        # 39 2
            'requestedRadius'          : RequestedRadius(),          # 40 2
            'requestedRightVelocity'   : RequestedRightVelocity(),   # 41 2
            'requestedLeftVelocity'    : RequestedLeftVelocity(),    # 42 2            
            'leftEncoderCounts'          : LeftEncoderCounts(),          # 43 2
            'rightEncoderCounts'         : RightEncoderCounts(),         # 44 2
            'lightBumper'                : LightBumper(),                # 45 1
            'lightBumpLeftSignal'        : LightBumpLeftSignal(),        # 46 2
            'lightBumpFrontLeftSignal'   : LightBumpFrontLeftSignal(),   # 47 2
            'lightBumpCenterLeftSignal'  : LightBumpCenterLeftSignal(),  # 48 2
            'lightBumpCenterRightSignal' : LightBumpCenterRightSignal(), # 49 2
            'lightBumpFrontRightSignal'  : LightBumpFrontRightSignal(),  # 50 2
            'lightBumpRightSignal'       : LightBumpRightSignal(),       # 51 2
            'infraredCharacterLeft'      : InfraredCharacterLeft(),      # 52 1
            'infraredCharacterRight'     : InfraredCharacterRight(),     # 53 1
            'leftMotorCurrent'           : LeftMotorCurrent(),           # 54 2
            'rightMotorCurrent'          : RightMotorCurrent(),          # 55 2
            'mainBrushMotorCurrent'      : MainBrushMotorCurrent(),      # 56 2
            'sideBrushMotorCurrent'      : SideBrushMotorCurrent(),      # 57 2
            'stasis'                     : Stasis(),                     # 58 1            
        }

class Group_101:
    ID = 101
    def __init__(self):    
        # 28, 43-58
        self.group = {                      
            'leftEncoderCounts'          : LeftEncoderCounts(),          # 43 2
            'rightEncoderCounts'         : RightEncoderCounts(),         # 44 2
            'lightBumper'                : LightBumper(),                # 45 1
            'lightBumpLeftSignal'        : LightBumpLeftSignal(),        # 46 2
            'lightBumpFrontLeftSignal'   : LightBumpFrontLeftSignal(),   # 47 2
            'lightBumpCenterLeftSignal'  : LightBumpCenterLeftSignal(),  # 48 2
            'lightBumpCenterRightSignal' : LightBumpCenterRightSignal(), # 49 2
            'lightBumpFrontRightSignal'  : LightBumpFrontRightSignal(),  # 50 2
            'lightBumpRightSignal'       : LightBumpRightSignal(),       # 51 2
            'infraredCharacterLeft'      : InfraredCharacterLeft(),      # 52 1
            'infraredCharacterRight'     : InfraredCharacterRight(),     # 53 1
            'leftMotorCurrent'           : LeftMotorCurrent(),           # 54 2
            'rightMotorCurrent'          : RightMotorCurrent(),          # 55 2
            'mainBrushMotorCurrent'      : MainBrushMotorCurrent(),      # 56 2
            'sideBrushMotorCurrent'      : SideBrushMotorCurrent(),      # 57 2
            'stasis'                     : Stasis(),                     # 58 1    
        }
    
class Group_106:
    ID = 106
    def __init__(self):    
        # 12, 46-51
        self.group = {            
            'lightBumpLeftSignal'        : LightBumpLeftSignal(),        # 46 2
            'lightBumpFrontLeftSignal'   : LightBumpFrontLeftSignal(),   # 47 2
            'lightBumpCenterLeftSignal'  : LightBumpCenterLeftSignal(),  # 48 2
            'lightBumpCenterRightSignal' : LightBumpCenterRightSignal(), # 49 2
            'lightBumpFrontRightSignal'  : LightBumpFrontRightSignal(),  # 50 2
            'lightBumpRightSignal'       : LightBumpRightSignal(),       # 51 2  
        }
    
class Group_107:
    ID = 107
    def __init__(self):    
        # 9, 54-58
        self.group = {            
            'leftMotorCurrent'           : LeftMotorCurrent(),           # 54 2
            'rightMotorCurrent'          : RightMotorCurrent(),          # 55 2
            'mainBrushMotorCurrent'      : MainBrushMotorCurrent(),      # 56 2
            'sideBrushMotorCurrent'      : SideBrushMotorCurrent(),      # 57 2
            'stasis'                     : Stasis(),                     # 58 1    
        }    