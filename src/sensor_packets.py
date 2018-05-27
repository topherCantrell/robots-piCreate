# TODO document how sensor groups work

def _signed_word_from_bytes(value):
        value = (value[0]<<8) | value[1]
        if value>32767:
            value = value - 65536
        return value
    
def _unsigned_word_from_bytes(value):
        value = (value[0]<<8) | value[1]        
        return value
    
class BumpsAndWheelDrops:    
    ID = 7
    SIZE = 1
    
    def __init__(self):
        self.bump_right = None
        self.bump_left  = None
        self.drop_right = None
        self.drop_left  = None
    
    def decode(self,packet):
        # print(packet)
        self.bump_right = ((packet[0]&1)>0)        
        self.bump_left  = ((packet[0]&2)>0) 
        self.drop_right = ((packet[0]&4)>0) 
        self.drop_left  = ((packet[0]&8)>0)         
            
    def __repr__(self):
        return ('BumpsAndDrops bump_left:'+str(self.bump_left)+' bump_right:'+str(self.bump_right)+
                ' drop_left:'+str(self.drop_left)+' drop_right:'+str(self.drop_right))
        
class CliffLeft:
    ID = 9
    SIZE = 1
    
    def __init__(self):
        self.is_cliff = None
        
    def decode(self,packet):
        #print(packet)
        self.is_cliff = (packet[0]>0)
    
    def __repr__(self):
        return ('CliffLeft is_cliff:'+str(self.is_cliff))
    
class CliffFrontLeft:
    ID = 10
    SIZE = 1
    
    def __init__(self):
        self.is_cliff = None
        
    def decode(self,packet):
        #print(packet)
        self.is_cliff = (packet[0]>0)
    
    def __repr__(self):
        return ('CliffFrontLeft is_cliff:'+str(self.is_cliff))
    
class CliffFrontRight:
    ID = 11
    SIZE = 1
    
    def __init__(self):
        self.is_cliff = None
        
    def decode(self,packet):
        #print(packet)
        self.is_cliff = (packet[0]>0)
    
    def __repr__(self):
        return ('CliffFrontRight is_cliff:'+str(self.is_cliff))
    
class CliffRight:
    ID = 12
    SIZE = 1
    
    def __init__(self):
        self.is_cliff = None
        
    def decode(self,packet):
        #print(packet)
        self.is_cliff = (packet[0]>0)
    
    def __repr__(self):
        return ('CliffRight is_cliff:'+str(self.is_cliff))
    
"""
class VirtualWall:
    ID = 13
    SIZE = 1
    #TODO

class WheelOvercurrents:
    ID = 14
    SIZE = 1
    #TODO
    
class DirtDetect:
    ID = 15
    SIZE = 1
    #TODO
    
class UnusedByte16:
    ID = 16
    SIZE = 1
    #TODO
    
class InfraredCharacterOmni:
    ID = 17
    SIZE = 1
    #TODO
    
class InfraredCharacterLeft:
    ID = 52
    SIZE = 1
    #TODO

class InfraredCharacterRight:
    ID = 53
    SIZE = 1
    #TODO    
"""
    
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
        
    def decode(self,packet):
        print(packet)
        self.is_clock    = ((packet[0]&128)>0)
        self.is_schedule = ((packet[0]&64)>0)
        self.is_day      = ((packet[0]&32)>0)
        self.is_hour     = ((packet[0]&16)>0)
        self.is_minute   = ((packet[0]&8)>0)
        self.is_dock     = ((packet[0]&4)>0)
        self.is_spot     = ((packet[0]&2)>0)
        self.is_clean    = ((packet[0]&1)>0)
    
    def __repr__(self):
        return ('Buttons is_clock:'+str(self.is_clock)+' is_schedule:'+str(self.is_schedule)+' is_day:'+str(self.is_day)+
                ' is_hour:'+str(self.is_hour)+' is_minute:'+str(self.is_minute)+' is_dock:'+str(self.is_dock)+
                ' is_spot:'+str(self.is_spot)+' is_clean:'+str(self.is_clean))

class Distance:
    ID = 19
    SIZE = 2
    
    def __init__(self):
        self.since_last_mm = None
        
    def decode(self,packet):
        self.since_last_mm = _signed_word_from_bytes(packet)
        
    def __repr__(self):
        return 'Distance since_last_mm:'+str(self.since_last_mm)

class Angle:
    ID = 20
    SIZE = 2
    
    def __init__(self):
        self.since_last_deg = None
        
    def decode(self,packet):
        self.since_last_deg = _signed_word_from_bytes(packet)
        
    def __repr__(self):
        return 'Angle since_last_deg:'+str(self.since_last_deg)
    
class ChargingState:
    ID = 21
    SIZE = 1
    
    def __init__(self):
        self.state = None
        
    def decode(self,packet):
        if packet[0]==0:
            self.state = (0,'Not charging')
        elif packet[0]==1:
            self.state = (1,'Reconditioning Charging')
        elif packet[0]==2:
            self.state = (2,'Full Charging')
        elif packet[0]==3:
            self.state = (3,'Trickle Charging')
        elif packet[0]==4:
            self.state = (4,'Waiting')
        elif packet[0]==5:
            self.state = (5,'Charging Fault Condition')
        else:
            self.state = (packet[0],'UNKNOWN VALUE')
            
    def __repr__(self):
        return 'ChargingState state:'+str(self.state)
    
class BatteryVoltage:
    ID = 22
    SIZE = 2
    
    def __init__(self):
        self.voltage = None
        
    def decode(self,packet):
        self.voltage = _unsigned_word_from_bytes(packet)/1000.0
        
    def __repr__(self):
        return 'BatteryVoltage voltage:'+str(self.voltage)
    
class BatteryCurrent:
    ID = 23
    SIZE = 2
    
    def __init__(self):
        self.current = None
        
    def decode(self,packet):
        self.current = _signed_word_from_bytes(packet)/1000.0
        
    def __repr__(self):
        return 'BatteryCurrent current:'+str(self.current)
    
class BatteryTemperature:
    ID = 24
    SIZE = 1
    
    def __init__(self):
        self.temperature_c = None
        
    def decode(self,packet):
        self.temperature_c = packet[0]
        if(self.temperature_c>127):
            self.temperature_c = self.temperature_c - 256
        
    def __repr__(self):
        return 'BatteryTemperature temperature_c:'+str(self.temperature_c)
    
class BatteryCharge:
    ID = 25
    SIZE = 2
    
    def __init__(self):
        self.charge_mah = None
        
    def decode(self,packet):
        self.charge_mah = _unsigned_word_from_bytes(packet)
        
    def __repr__(self):
        return 'BatteryCharge charge_mah:'+str(self.charge_mah)

class BatteryCapacity:
    ID = 26
    SIZE = 2
    
    def __init__(self):
        self.capacity_mah = None
        
    def decode(self,packet):
        self.capacity_mah = _unsigned_word_from_bytes(packet)
        
    def __repr__(self):
        return 'BatteryCapacity capacity_mah:'+str(self.capacity_mah)
    
class Group_BatteryInfo:
    ID = 3
    SIZE = 10
    
    def __init__(self):
        self.group = {
            'chargingState'      : ChargingState(),      # 1
            'batteryVoltage'     : BatteryVoltage(),     # 2
            'batteryCurrent'     : BatteryCurrent(),     # 2
            'batteryTemperature' : BatteryTemperature(), # 1
            'batteryCharge'      : BatteryCharge(),      # 2
            'batteryCapacity'    : BatteryCapacity()     # 2
        }
           
    