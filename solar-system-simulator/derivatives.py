'''
Created on 6.3.2016

@author: kristiansalo
'''

class Derivatives():
    
    def __init__(self, vel, acc):
        #Attributes dx and dv are pygame 3D vectors.
        self.dx = vel
        self.dv = acc