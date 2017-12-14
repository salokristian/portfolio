'''
Created on 6.3.2016

@author: kristiansalo
'''

import unittest
from object import *
from simulation import *


class TestAcceleration(unittest.TestCase):
    
    def setUp(self):
        self.a = Object(2,10,[3,543,-224],[5,0,0],'a','s')
        self.b = Object(1000,23,[-50,25,3],[5,0,0],'b','d')
        self.list = [self.a,self.b]

    def testTwoObjectsEquals(self):
        acc_a = self.a.newAcceleration(self.list, self.a.data)
        acc_b = self.b.newAcceleration(self.list, self.b.data)
        self.assertEqual(acc_a, acc_b, 'The acceleration was not equal on both planets in a two-planet system')
        
    def testTwoObjectsAcceleration(self):    
        acc = self.a.newAcceleration(self.list, self.a.data)
        acc_scalar = acc.length()
        right_acc_a = 4.1367*10**(-13) / self.a.mass
        self.assertAlmostEqual(right_acc_a, acc_scalar, 17, 'Numbers had different first 17 decimals.')
        
        
class TestSimulation(unittest.TestCase):
    

    def testCircularOrbit(self):
        #Don't change timestep to bigger values because then inaccuracy will grow and the test will fail.
        sun = Object(1.988*10**30, 696.3*10**6,[0.0, 0.0, 0.0], [0.0, 0.0, 0.0],'sun',(255,153,51))
        earth = Object(5.972*10**24, 6.371*10**6, [-151.2434472*10**9, 0 , 0], [0, -29692.5846, 0], 'Earth', (50, 255,255))   
        start_dist = earth.data.loc.length()     
        
        list = [earth, sun]
        simulation = Simulation(list, 3600, 185*24*3600)
        simulation.simulate()
        
        end_dist = earth.data.loc.length()
        self.assertGreater(1.5, abs(start_dist - end_dist)/start_dist * 100, 'The mistake in radial distance was greater that 1.5%.')
    
    def testLagrangianPoints(self):
        #Don't change timestep to bigger values because then inaccuracy will grow and the test will fail.
        sun = Object(1.988*10**30, 696.3*10**6,[0.0, 0.0, 0.0], [0.0, 0.0, 0.0],'sun',(255,153,51))
        mercury = Object(330*10**21, 4879*10**3,[58.7*10**9, 0.0, 0.0], [0.0, 47548.861132129554, 0.0],'mercury',(0,160,160)) 
        lagrange3 = Object(1,1, [-58699994315.4158, 0, 0], [0, -47548.85654760611, 0], 'L3', (255,255,255))
        lagrange4 = Object(1, 1, [29350000000, 50835691202.146545, 0.0], [-41178.521678910, 23774.430576149774, 0.0], 'L4', (255,255,255))
        lagrange5 = Object(1, 1, [29350000000, -50835691202.146545, 0.0], [41178.521678910, 23774.430576149774, 0.0], 'L5', (255,255,255))
        l3_start_dist, l4_start_dist, l5_start_dist = lagrange3.data.loc.length(), lagrange4.data.loc.length(), lagrange5.data.loc.length()
        
        list = [sun, mercury, lagrange3, lagrange4, lagrange5]
        solar = Simulation(list, 3600, 87.968*24*3600)
        solar.simulate()
        
        l3_end_dist, l4_end_dist, l5_end_dist = lagrange3.data.loc.length(), lagrange4.data.loc.length(), lagrange5.data.loc.length()
        self.assertGreater(2, abs(l3_start_dist - l3_end_dist)/l3_start_dist * 100, 'The mistake in L3s radial distance was greater than 2%.')
        self.assertGreater(2, abs(l4_start_dist - l4_end_dist)/l4_start_dist * 100, 'The mistake in L4s radial distance was greater than 2%.')
        self.assertGreater(2, abs(l5_start_dist - l5_end_dist)/l5_start_dist * 100, 'The mistake in L5s radial distance was greater than 2%.')
        
    def testScale(self):
        simulation = Simulation([], 1, 1)
        result1 = simulation.scale(5, 0, 10, 10, 20)
        self.assertEqual(result1, 15, 'Incorrectly scaled value.')
        result2 = simulation.scale(-3,-5,4,-20,-11)
        self.assertEqual(result2, -18, 'Incorrectly scaled value.')
        
    def testPlanetsCollide(self):
        a = Object(20.0**4.9,10,[440.0,200.0,0.0],[0.0,0.85,0.0],'a','a')
        b = Object(20.0**4.9,23,[420.0,200.0,0.0],[0.0,-0.85,0.0],'b','d')
        list = [a, b]
        simulation = Simulation(list, 1, 100*100)
        self.assertEqual(simulation.planetsCollide(), [True, b, a], 'Function returned an invalid list when two planets collided.')


if __name__ == '__main__':
    unittest.main()