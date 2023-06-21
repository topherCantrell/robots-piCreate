# The iRobot Create 2 Open Interface (OI)

https://edu.irobot.com/learning-library/create-2-oi-spec

Click on the PDF button to download.

# Connecting to the roomba

```python
import roomba

# Typical for Raspberry Pi
# roomba = Roomba('/dev/ttyUSB0')

# Typical for PC
roomba = Roomba('COM3')

roomba.set_mode_safe()
```

# TODO a lot more documentation here

For now, just the new sensor packet stream.

# Sensor Packets and Sensor Groups

Reading a single packet:

```python
    import roomba
    import sensor_packets      
    
    roomba = Roomba('COM3')

    p1 = sensor_packets.Buttons()
    roomba.get_sensor_packet(p1)
    print(p1.is_clock)  # Is the CLOCK button pressed
    print(p1.is_spot)  # Is the SPOT button pressed

    print(p1)  # Print everything in the packet
```

Reading a list of packets:
```python
    p1 = sensor_packets.Buttons()
    p2 = sensor_packets.ChargingState()
    p3 = sensor_packets.OIMode()
    roomba.get_sensor_multi_packets([p1, p2, p3])
    print(p1)
    print(p2)
    print(p3)   
```

# Sensor Groups

A sensor group is a pre-defined list of sensors. You can use a `sensor_group` object in place of a
`sensor_packet` object. The OI will return the data for all the packets in the group.

You cannot define new sensor groups. You can use the predefined sensor groups in the OI API.

The `sensor_group.Group_100` covers every sensor packet -- a whopping 80 bytes of data.

# Streaming packet data

You can ask the OI API to return you updated packet/group data every 15ms. You call the `start_packet_stream`
method with a list of packets/groups. The library calls your callback function every 15ms with new data
for all the sensors you requested.

When you create the `roomba` object, you must pass in a callback method. Every 15ms, when the roomba sends
new information, this callback is called with the update list of sensor packets.

Make this function fast. Remember, the library is calling you every 15ms. If you stall in this callback, the
data will back up and eventually get dropped.

```python

def got_stream_update(packets):
    for p in packets:
        print(p)

roomba = Roomba('COM3',stream_update_cb=got_stream_update)

p1 = sensor_packets.Buttons()
p2 = sensor_packets.ChargingState()
p3 = sensor_packets.OIMode()

roomba.start_packet_stream([p3,p2,p1])

# Data flows in to your callback "got_stream_update" every 15ms

time.sleep(5)
```

You can pause and resume the stream with `roomba.pause_packet_stream()` and `roomba.resume_packet_stream()`.