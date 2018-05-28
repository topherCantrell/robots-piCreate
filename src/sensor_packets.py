# TODO document how sensor groups work

def _signed_word_from_bytes(value,ofs):
        value = (value[ofs]<<8) | value[ofs+1]
        if value>32767:
            value = value - 65536
        return value
    
def _unsigned_word_from_bytes(value,ofs):
        value = (value[ofs]<<8) | value[ofs+1]        
        return value
    
class BumpsAndWheelDrops:    
    ID = 7
    SIZE = 1
    
    def __init__(self):
        self.bump_right = None
        self.bump_left  = None
        self.drop_right = None
        self.drop_left  = None
    
    def decode(self,packet,ofs):
        # print(packet)
        self.bump_right = ((packet[ofs]&1)>0)        
        self.bump_left  = ((packet[ofs]&2)>0) 
        self.drop_right = ((packet[ofs]&4)>0) 
        self.drop_left  = ((packet[ofs]&8)>0)         
            
    def __repr__(self):
        return ('BumpsAndDrops bump_left:'+str(self.bump_left)+' bump_right:'+str(self.bump_right)+
                ' drop_left:'+str(self.drop_left)+' drop_right:'+str(self.drop_right))
        
class Wall:
    ID = 8
    SIZE = 1
    def __init__(self):
        self.raw = None
    def decode(self,packet,ofs):
        self.raw = packet
    def __repr__(self):
        return 'TODO raw:'+str(self.raw)
        
class CliffLeft:
    ID = 9
    SIZE = 1
    
    def __init__(self):
        self.is_cliff = None
        
    def decode(self,packet,ofs):
        #print(packet)
        self.is_cliff = (packet[ofs]>0)
    
    def __repr__(self):
        return ('CliffLeft is_cliff:'+str(self.is_cliff))
    
class CliffFrontLeft:
    ID = 10
    SIZE = 1
    
    def __init__(self):
        self.is_cliff = None
        
    def decode(self,packet,ofs):
        #print(packet)
        self.is_cliff = (packet[ofs]>0)
    
    def __repr__(self):
        return ('CliffFrontLeft is_cliff:'+str(self.is_cliff))
    
class CliffFrontRight:
    ID = 11
    SIZE = 1
    
    def __init__(self):
        self.is_cliff = None
        
    def decode(self,packet,ofs):
        #print(packet)
        self.is_cliff = (packet[ofs]>0)
    
    def __repr__(self):
        return ('CliffFrontRight is_cliff:'+str(self.is_cliff))
    
class CliffRight:
    ID = 12
    SIZE = 1
    
    def __init__(self):
        self.is_cliff = None
        
    def decode(self,packet,ofs):
        #print(packet)
        self.is_cliff = (packet[ofs]>0)
    
    def __repr__(self):
        return ('CliffRight is_cliff:'+str(self.is_cliff))

class VirtualWall:
    ID = 13
    SIZE = 1   
    def __init__(self):
        self.raw = None
    def decode(self,packet,ofs):
        self.raw = packet
    def __repr__(self):
        return 'TODO raw:'+str(self.raw)

class WheelOvercurrents:
    ID = 14
    SIZE = 1
    def __init__(self):
        self.raw = None
    def decode(self,packet,ofs):
        self.raw = packet
    def __repr__(self):
        return 'TODO raw:'+str(self.raw)
    
class DirtDetect:
    ID = 15
    SIZE = 1
    def __init__(self):
        self.raw = None
    def decode(self,packet,ofs):
        self.raw = packet
    def __repr__(self):
        return 'TODO raw:'+str(self.raw)
    
class InfraredCharacterOmni:
    ID = 17
    SIZE = 1
    def __init__(self):
        self.raw = None
    def decode(self,packet,ofs):
        self.raw = packet
    def __repr__(self):
        return 'TODO raw:'+str(self.raw)
    
class Buttons:
    ID = 18
    SIZE = 1    
    
    def __init__(self):
        self.is_clock    = None
        self.is_schedule = None
        self.is_day      = None
        self.is_hour     = None
        self.is_minute   = None
        self.is_dock     = None
        self.is_spot     = None
        self.is_clean    = None
        
    def decode(self,packet,ofs):
        print(packet)
        self.is_clock    = ((packet[ofs]&128)>0)
        self.is_schedule = ((packet[ofs]&64)>0)
        self.is_day      = ((packet[ofs]&32)>0)
        self.is_hour     = ((packet[ofs]&16)>0)
        self.is_minute   = ((packet[ofs]&8)>0)
        self.is_dock     = ((packet[ofs]&4)>0)
        self.is_spot     = ((packet[ofs]&2)>0)
        self.is_clean    = ((packet[ofs]&1)>0)
    
    def __repr__(self):
        return ('Buttons is_clock:'+str(self.is_clock)+' is_schedule:'+str(self.is_schedule)+' is_day:'+str(self.is_day)+
                ' is_hour:'+str(self.is_hour)+' is_minute:'+str(self.is_minute)+' is_dock:'+str(self.is_dock)+
                ' is_spot:'+str(self.is_spot)+' is_clean:'+str(self.is_clean))

class Distance:
    ID = 19
    SIZE = 2
    
    def __init__(self):
        self.since_last_mm = None
        
    def decode(self,packet,ofs):
        self.since_last_mm = _signed_word_from_bytes(packet,ofs)
        
    def __repr__(self):
        return 'Distance since_last_mm:'+str(self.since_last_mm)

class Angle:
    ID = 20
    SIZE = 2
    
    def __init__(self):
        self.since_last_deg = None
        
    def decode(self,packet,ofs):
        self.since_last_deg = _signed_word_from_bytes(packet,ofs)
        
    def __repr__(self):
        return 'Angle since_last_deg:'+str(self.since_last_deg)
    
class ChargingState:
    ID = 21
    SIZE = 1
    
    def __init__(self):
        self.state = None
        
    def decode(self,packet,ofs):
        if packet[ofs]==0:
            self.state = (0,'Not charging')
        elif packet[ofs]==1:
            self.state = (1,'Reconditioning Charging')
        elif packet[ofs]==2:
            self.state = (2,'Full Charging')
        elif packet[ofs]==3:
            self.state = (3,'Trickle Charging')
        elif packet[ofs]==4:
            self.state = (4,'Waiting')
        elif packet[ofs]==5:
            self.state = (5,'Charging Fault Condition')
        else:
            self.state = (packet[ofs],'UNKNOWN VALUE')
            
    def __repr__(self):
        return 'ChargingState state:'+str(self.state)
    
class BatteryVoltage:
    ID = 22
    SIZE = 2
    
    def __init__(self):
        self.voltage = None
        
    def decode(self,packet,ofs):
        self.voltage = _unsigned_word_from_bytes(packet,ofs)/1000.0
        
    def __repr__(self):
        return 'BatteryVoltage voltage:'+str(self.voltage)
    
class BatteryCurrent:
    ID = 23
    SIZE = 2
    
    def __init__(self):
        self.current = None
        
    def decode(self,packet,ofs):
        self.current = _signed_word_from_bytes(packet,ofs)/1000.0
        
    def __repr__(self):
        return 'BatteryCurrent current:'+str(self.current)
    
class BatteryTemperature:
    ID = 24
    SIZE = 1
    
    def __init__(self):
        self.temperature_c = None
        
    def decode(self,packet,ofs):
        self.temperature_c = packet[ofs]
        if(self.temperature_c>127):
            self.temperature_c = self.temperature_c - 256
        
    def __repr__(self):
        return 'BatteryTemperature temperature_c:'+str(self.temperature_c)
    
class BatteryCharge:
    ID = 25
    SIZE = 2
    
    def __init__(self):
        self.charge_mah = None
        
    def decode(self,packet,ofs):
        self.charge_mah = _unsigned_word_from_bytes(packet,ofs)
        
    def __repr__(self):
        return 'BatteryCharge charge_mah:'+str(self.charge_mah)

class BatteryCapacity:
    ID = 26
    SIZE = 2
    
    def __init__(self):
        self.capacity_mah = None
        
    def decode(self,packet,ofs):
        self.capacity_mah = _unsigned_word_from_bytes(packet,ofs)
        
    def __repr__(self):
        return 'BatteryCapacity capacity_mah:'+str(self.capacity_mah)    
    
class WallSignal:
    ID = 27
    SIZE = 2
    def __init__(self):
        self.raw = None
    def decode(self,packet,ofs):
        self.raw = packet
    def __repr__(self):
        return 'TODO raw:'+str(self.raw)

class CliffLeftSignal:
    ID = 28
    SIZE = 2
    def __init__(self):
        self.raw = None
    def decode(self,packet,ofs):
        self.raw = packet
    def __repr__(self):
        return 'TODO raw:'+str(self.raw)
    
class CliffFrontLeftSignal:
    ID = 29
    SIZE = 2
    def __init__(self):
        self.raw = None
    def decode(self,packet,ofs):
        self.raw = packet
    def __repr__(self):
        return 'TODO raw:'+str(self.raw)
    
class CliffFrontRightSignal:
    ID = 30
    SIZE = 2
    def __init__(self):
        self.raw = None
    def decode(self,packet,ofs):
        self.raw = packet
    def __repr__(self):
        return 'TODO raw:'+str(self.raw)
    
class CliffRightSignal:
    ID = 31
    SIZE = 2
    def __init__(self):
        self.raw = None
    def decode(self,packet,ofs):
        self.raw = packet
    def __repr__(self):
        return 'TODO raw:'+str(self.raw)
    
class ChargingSourcesAvailable:
    ID = 34
    SIZE = 1
    def __init__(self):
        self.raw = None
    def decode(self,packet,ofs):
        self.raw = packet
    def __repr__(self):
        return 'TODO raw:'+str(self.raw)
    
class OIMode:
    ID = 35
    SIZE = 1
    def __init__(self):
        self.raw = None
    def decode(self,packet,ofs):
        self.raw = packet
    def __repr__(self):
        return 'TODO raw:'+str(self.raw)

class SongNumber:
    ID = 36
    SIZE = 1
    def __init__(self):
        self.raw = None
    def decode(self,packet,ofs):
        self.raw = packet
    def __repr__(self):
        return 'TODO raw:'+str(self.raw)
        
class SongPlaying:
    ID = 37
    SIZE = 1
    def __init__(self):
        self.raw = None
    def decode(self,packet,ofs):
        self.raw = packet
    def __repr__(self):
        return 'TODO raw:'+str(self.raw)
    
class NumberStreamPackets:
    ID = 38
    SIZE = 1
    def __init__(self):
        self.raw = None
    def decode(self,packet,ofs):
        self.raw = packet
    def __repr__(self):
        return 'TODO raw:'+str(self.raw)
    
class RequestedVelocity:
    ID = 39
    SIZE = 2
    def __init__(self):
        self.raw = None
    def decode(self,packet,ofs):
        self.raw = packet
    def __repr__(self):
        return 'TODO raw:'+str(self.raw)

class RequestedRadius:
    ID = 40
    SIZE = 2
    def __init__(self):
        self.raw = None
    def decode(self,packet,ofs):
        self.raw = packet
    def __repr__(self):
        return 'TODO raw:'+str(self.raw)

class RequestedRightVelocity:
    ID = 41
    SIZE = 2
    def __init__(self):
        self.raw = None
    def decode(self,packet,ofs):
        self.raw = packet
    def __repr__(self):
        return 'TODO raw:'+str(self.raw)
    
class RequestedLeftVelocity:
    ID = 42
    SIZE = 2
    def __init__(self):
        self.raw = None
    def decode(self,packet,ofs):
        self.raw = packet
    def __repr__(self):
        return 'TODO raw:'+str(self.raw)
    
class LeftEncoderCounts:
    ID = 43
    SIZE = 2
    def __init__(self):
        self.raw = None
    def decode(self,packet,ofs):
        self.raw = packet
    def __repr__(self):
        return 'TODO raw:'+str(self.raw)
    
class RightEncoderCounts:
    ID = 44
    SIZE = 2
    def __init__(self):
        self.raw = None
    def decode(self,packet,ofs):
        self.raw = packet
    def __repr__(self):
        return 'TODO raw:'+str(self.raw)
    
class LightBumper:
    ID = 45
    SIZE = 1
    def __init__(self):
        self.raw = None
    def decode(self,packet,ofs):
        self.raw = packet
    def __repr__(self):
        return 'TODO raw:'+str(self.raw)
    
class LightBumpLeftSignal:
    ID = 46
    SIZE = 2
    def __init__(self):
        self.raw = None
    def decode(self,packet,ofs):
        self.raw = packet
    def __repr__(self):
        return 'TODO raw:'+str(self.raw)
    
class LightBumpFrontLeftSignal:
    ID = 47
    SIZE = 2
    def __init__(self):
        self.raw = None
    def decode(self,packet,ofs):
        self.raw = packet
    def __repr__(self):
        return 'TODO raw:'+str(self.raw)
    
class LightBumpCenterLeftSignal:
    ID = 48
    SIZE = 2
    def __init__(self):
        self.raw = None
    def decode(self,packet,ofs):
        self.raw = packet
    def __repr__(self):
        return 'TODO raw:'+str(self.raw)
    
class LightBumpCenterRightSignal:
    ID = 49
    SIZE = 2
    def __init__(self):
        self.raw = None
    def decode(self,packet,ofs):
        self.raw = packet
    def __repr__(self):
        return 'TODO raw:'+str(self.raw)
    
class LightBumpFrontRightSignal:
    ID = 50
    SIZE = 2
    def __init__(self):
        self.raw = None
    def decode(self,packet,ofs):
        self.raw = packet
    def __repr__(self):
        return 'TODO raw:'+str(self.raw)
    
class LightBumpRightSignal:
    ID = 51
    SIZE = 2
    def __init__(self):
        self.raw = None
    def decode(self,packet,ofs):
        self.raw = packet
    def __repr__(self):
        return 'TODO raw:'+str(self.raw)
    
class InfraredCharacterLeft:
    ID = 52
    SIZE = 1
    def __init__(self):
        self.raw = None
    def decode(self,packet,ofs):
        self.raw = packet
    def __repr__(self):
        return 'TODO raw:'+str(self.raw)

class InfraredCharacterRight:
    ID = 53
    SIZE = 1
    def __init__(self):
        self.raw = None
    def decode(self,packet,ofs):
        self.raw = packet
    def __repr__(self):
        return 'TODO raw:'+str(self.raw)
    
class LeftMotorCurrent:
    ID = 54
    SIZE = 2
    def __init__(self):
        self.raw = None
    def decode(self,packet,ofs):
        self.raw = packet
    def __repr__(self):
        return 'TODO raw:'+str(self.raw)
    
class RightMotorCurrent:
    ID = 55
    SIZE = 2
    def __init__(self):
        self.raw = None
    def decode(self,packet,ofs):
        self.raw = packet
    def __repr__(self):
        return 'TODO raw:'+str(self.raw)
    
class MainBrushMotorCurrent:
    ID = 56
    SIZE = 2
    def __init__(self):
        self.raw = None
    def decode(self,packet,ofs):
        self.raw = packet
    def __repr__(self):
        return 'TODO raw:'+str(self.raw)
    
class SideBrushMotorCurrent:
    ID = 57
    SIZE = 2
    def __init__(self):
        self.raw = None
    def decode(self,packet,ofs):
        self.raw = packet
    def __repr__(self):
        return 'TODO raw:'+str(self.raw)
    
class Stasis:
    ID = 58
    SIZE = 1
    def __init__(self):
        self.raw = None
    def decode(self,packet,ofs):
        self.raw = packet
    def __repr__(self):
        return 'TODO raw:'+str(self.raw)

#

