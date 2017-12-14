'''
Created on 2.3.2016

@author: kristiansalo
'''

class Data():
    
    def __init__(self, loc, vel, acc):
        #Attributes loc, vel and acc are pygame 3D vectors.
        self.loc = loc
        self.vel = vel
        self.acc = acc