'''
Created on 6.3.2016

@author: kristiansalo
'''

from data import *
from derivatives import *
from pygame import math

class Object():
    
    
    def __init__(self, mass, radius, loc, vel, name, colour):
        #Loc and vel are lists containing x,y and z-coordinates. Colour is an rgb-tuple.
        self.mass = mass
        self.radius = radius
        self.name = name
        self.colour = colour
        self.data = self.createData(loc, vel, [0.0 , 0.0 , 0.0])
        
    def createData(self, loc, vel, acc):
        loc = math.Vector3(loc[0] , loc[1] , loc[2])
        vel = math.Vector3(vel[0] , vel[1] , vel[2])
        acc = math.Vector3(acc[0] , acc[1] , acc[2])
        return Data(loc, vel, acc)
        
    def newAcceleration(self, ObjectList, data):
        g_const = 6.67384*10**(-11) 
        acc = math.Vector3(0.0 , 0.0 , 0.0)
        for object in ObjectList:
            if object is self:
                pass
            else:
                dist_vec = object.data.loc - self.data.loc
                dist = dist_vec.length()
                if dist != 0:
                    force = g_const*self.mass*object.mass/(dist**2) #Scalar force
                    acc.x += force * dist_vec.x / dist #Acceleration components
                    acc.y += force * dist_vec.y / dist
                    acc.z += force * dist_vec.z / dist
        return acc / self.mass
   
    def initialDerivative(self, ObjectList):
        #Helper method for RK4. 
        acc = self.newAcceleration(ObjectList, self.data)
        return Derivatives(self.data.vel, acc)
    
    def otherDerivatives(self, ObjectList, data, derivatives, dt):
        #Helper method for RK4. 
        new_data = self.createData([0.0]*3,[0.0]*3,[0.0]*3)
        new_data.loc = data.loc + derivatives.dx * dt 
        new_data.vel = data.vel + derivatives.dv * dt
        acc = self.newAcceleration(ObjectList, new_data)
        return Derivatives(new_data.vel, acc)
    
    def newData(self,ObjectList, dt):
        #Calculate new location, velocity and acceleration for self using RK4-method. 
        a = self.initialDerivative(ObjectList)
        b = self.otherDerivatives(ObjectList, self.data, a, dt*0.5)
        c = self.otherDerivatives(ObjectList, self.data, b, dt*0.5)
        d = self.otherDerivatives(ObjectList, self.data, c, dt)
        #Approximate the best new values for derivates using Taylor-series.
        dx_x = 1.0 / 6.0 * (a.dx.x + 2.0 * (b.dx.x + c.dx.x) + d.dx.x)
        dx_y = 1.0 / 6.0 * (a.dx.y + 2.0 * (b.dx.y + c.dx.y) + d.dx.y)
        dx_z = 1.0 / 6.0 * (a.dx.z + 2.0 * (b.dx.z + c.dx.z) + d.dx.z)
        dv_x = 1.0 / 6.0 * (a.dv.x + 2.0 * (b.dv.x + c.dv.x) + d.dv.x)
        dv_y = 1.0 / 6.0 * (a.dv.y + 2.0 * (b.dv.y + c.dv.y) + d.dv.y)
        dv_z = 1.0 / 6.0 * (a.dv.z + 2.0 * (b.dv.z + c.dv.z) + d.dv.z)
        #New location and velocity using the derivatives just calculated.
        x_x = self.data.loc.x + dt*dx_x
        x_y = self.data.loc.y + dt*dx_y
        x_z = self.data.loc.z + dt*dx_z
        v_x = self.data.vel.x + dt*dv_x
        v_y = self.data.vel.y + dt*dv_y
        v_z = self.data.vel.z + dt*dv_z
        return self.createData([x_x,x_y,x_z],[v_x,v_y,v_z],[dv_x,dv_y,dv_z])