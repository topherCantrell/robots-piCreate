from sensor_packets import Angle
from sensor_packets import Distance

import time

class Drive:
    
    def __init__(self,robot):
        self._robot = robot
        # We can reuse these
        self._angle_packet = Angle()
        self._distance_packet = Distance()
    
    def clear_angle(self):
        self._robot.get_sensor_packet(self._angle_packet)
        
    def clear_distance(self):
        self._robot.get_sensor_packet(self._distance_packet)
        
    def turn_cw_to_angle(self,angle_deg):        
        while True:
            self._robot.get_sensor_packet(self._angle_packet)
            if self._angle_packet.since_last_deg>=angle_deg:
                return
            time.sleep(.01)
            
    def turn_ccw_to_angle(self,angle_deg):        
        while True:
            self._robot.get_sensor_packet(self._angle_packet)
            if self._angle_packet.since_last_deg<=angle_deg:
                return
            time.sleep(.01)
    
    def drive_forward_to_distance(self, distance_mm):
        while True:
            self._robot.get_sensor_packet(self._distance_packet)
            if self._distance_packet.since_last_mm>=distance_mm:
                return
            time.sleep(.01)
            
    def drive_backward_to_distance(self, distance_mm):
        while True:
            self._robot.get_sensor_packet(self._distance_packet)
            if self._distance_packet.since_last_mm<=distance_mm:
                return
            time.sleep(.01)        
        