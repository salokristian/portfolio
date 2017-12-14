'''
Created on 6.3.2016

@author: kristiansalo
'''
from object import *
import pygame
import sys

class Simulation():
    
    def __init__(self, ObjectList, timestep, simulationlength):
        self.list = ObjectList
        self.dt = timestep
        self.time_elapsed = 0
        self.length = simulationlength
        self.help = 0
        self.fps = 30
        self.radius_list = []

    def setScreen(self):
        pygame.init()
        fullscreen = pygame.display.Info()
        self.full_heigth = fullscreen.current_h
        self.full_width = fullscreen.current_w
        self.screen_width = fullscreen.current_w // 2
        self.screen_heigth = fullscreen.current_h // 2
        self.screen = pygame.display.set_mode((self.screen_width, self.screen_heigth))
        pygame.display.set_caption('Solar simulator')
        self.crd_max = 1
        self.vector_len = 30
        self.displayinfo = True
        self.vectordisplay = True
        self.setInfo()
        vector_font = pygame.font.SysFont('arial', 20)
        self.v = vector_font.render('v', True, (255,255,255))
        self.a = vector_font.render('a', True, (255,255,255))
        
    def setInfo(self):
        font = pygame.font.SysFont('arial', 16)
        
        if self.displayinfo: #Only display shortcuts if user wants them visible.
            zoom = font.render('Press i to zoom in and o to zoom out.', True, (255,255,255))
            speed = font.render('Press f to speed up and s to slow down.', True, (255,255,255))
            pause = font.render('Press space to pause the simulation.', True, (255,255,255))
            vectordisp = font.render('Press v to toggle vector visibility.', True, (255,255,255))
            dt = font.render('Press l to increase and k to decrease timestep.', True, (255,255,255))
            fullscreen = font.render('Press b to toggle fullscreen.', True, (255,255,255))
            hide = font.render('Press h to hide information.', True, (255,255,255))
            self.screen.blit(zoom, (5,5))
            self.screen.blit(speed, (5,20))
            self.screen.blit(pause, (5,35)) 
            self.screen.blit(vectordisp, (5,50))
            self.screen.blit(dt, (5,65))
            self.screen.blit(fullscreen, (5,80))
            self.screen.blit(hide, (5,95))
            
        if self.time_elapsed > 3600*24*365 :        
            timeinfo = font.render('Time elapsed: {:.0f} years.' .format(self.time_elapsed / (3600*24*365)), True, (255,255,255))
        else:
            timeinfo = font.render('Time elapsed: {:.0f} days.' .format(self.time_elapsed / (3600*24)), True, (255,255,255))
            
        self.screen.blit(timeinfo, (5,self.screen_heigth - 20))
        
        if self.dt < 3600:    
            dtinfo = font.render('Timestep: {:.1f} seconds' .format(self.dt), True, (255,255,255))
        elif 3600 <= self.dt <= 3600*24:
            dtinfo = font.render('Timestep: {:.1f} hours' .format(self.dt / 3600), True, (255,255,255))
        elif 3600*24 <= self.dt <= 3600*24*365:
            dtinfo = font.render('Timestep: {:.1f} days' .format(self.dt / (3600*24)), True, (255,255,255))
        else:
            dtinfo = font.render('Timestep: {:.1f} years' .format(self.dt / (3600*24*365)), True, (255,255,255))            
            
        self.screen.blit(dtinfo, (5, self.screen_heigth - 35))
        
        
    def setCrd_max(self):
        max = 0
        for object in self.list:
            if abs(object.data.loc.x) > max:
                max = abs(object.data.loc.x)
            if abs(object.data.loc.y) > max:
                max = abs(object.data.loc.y)
        self.crd_max = max
        
    def setScaledRadiusList(self):
        max_radius = 0
        for object in self.list:
            if object.radius > max_radius:
                max_radius = object.radius
        for object in self.list:
            scaled_radius = self.scale(object.radius, 0, max_radius, 1, 6)
            self.radius_list.append(scaled_radius)
        
    def calculateNextFrame(self):
        new_data = []
        max_vel = 0
        max_acc = 0
        i = 0
        for object in self.list:
            new_data.append(object.newData(self.list,self.dt))
        for object in self.list:
            object.data = new_data[i]
            if object.data.vel.length() > max_vel:
                max_vel = object.data.vel.length()
            if object.data.acc.length() > max_acc:
                max_acc = object.data.acc.length()
            i += 1
        return max_vel, max_acc
            
    def plotNextFrame(self, max_vel, max_acc):
        i = 0
        self.screen.fill((0,0,0))
        for object in self.list:
            plot_loc = self.scaleCoordinates(object.data.loc)
            if plot_loc != None: 
                if abs(plot_loc.x) < (2**32 - 1)/2 and abs(plot_loc.y) < (2**32 - 1)/2:     #To prevent error when plot_loc is bigger than c long (32 bytes).
                    pygame.draw.circle(self.screen, object.colour, (int(round(plot_loc.x)),int(round(plot_loc.y))), int(round(self.radius_list[i])) , 0)
                    if self.vectordisplay:
                        self.draw_vector(plot_loc, object.data.vel, max_vel, self.v)
                        self.draw_vector(plot_loc, object.data.acc, max_acc, self.a)
            i += 1
        self.setInfo()
        pygame.display.update()
        
    def scaleCoordinates(self, loc):
        if abs(loc.x) <= self.crd_max and abs(loc.y) <= self.crd_max:    #Check if objects location is visible to user on current zoom level.
            scaled_x = self.scale(loc.x, -self.crd_max, self.crd_max, 0, self.screen_width)
            scale_y_max = (self.screen_width - self.screen_heigth) / 2 + self.screen_heigth #To ensure that scaling in symmetrical in both x- and y-directions.
            scale_y_min = - (self.screen_width - self.screen_heigth) / 2
            scaled_y = self.scale(loc.y, -self.crd_max, self.crd_max, scale_y_min, scale_y_max)
            return pygame.math.Vector3(scaled_x, scaled_y, 0)
        else:
            return None
        
    def draw_vector(self, location, vector, max_val, letter):
        vec_len = self.scale(vector.length(), 0, max_val, 0, self.vector_len)
        a = pygame.math.Vector3(vector)
        #To avoid error when scaling a vector with length very close to zero
        if a.length() > 0.001:
            a.scale_to_length(vec_len)
            b = pygame.math.Vector3(a) * 0.5
            vec_endpoint = location + a
            letter_point = vec_endpoint + b
            pygame.draw.lines(self.screen, (255,255,255), False, [(int(round(location.x)),int(round(location.y))) , (int(round(vec_endpoint.x)), int(round(vec_endpoint.y)))] , 1)
            self.screen.blit(letter, (int(round(letter_point.x)), int(round(letter_point.y))))
            arrow1 = a.rotate_z(150.0)
            arrow2 = a.rotate_z(-150.0)
            endpoint1 = 1/4*arrow1 + vec_endpoint
            endpoint2 = 1/4*arrow2 + vec_endpoint
            pygame.draw.lines(self.screen, (255,255,255), False, [(int(vec_endpoint.x),int(round(vec_endpoint.y))) , (int(round(endpoint1.x)), int(round(endpoint1.y)))] , 1)
            pygame.draw.lines(self.screen, (255,255,255), False, [(int(vec_endpoint.x),int(round(vec_endpoint.y))) , (int(round(endpoint2.x)), int(round(endpoint2.y)))] , 1)
        
        
    def scale(self, value, in_min, in_max, out_min, out_max):
        #Scale value from current interval [in_min, in_max] to target interval [out_min, out_max].
        if in_max == 0:
            return 0
        return (value - in_min) * (out_max - out_min) / (in_max - in_min) + out_min
        
            
        
    def planetsCollide(self):
        collided = [False, None, None]
        for object1 in self.list:
            for object2 in self.list:
                if object2 != object1:
                    dist_vec = object1.data.loc - object2.data.loc
                    min_length = object2.radius + object1.radius
                    if dist_vec.length() < min_length:
                        collided[0] = True
                        collided[1] = object1
                        collided[2] = object2
        return collided
                        
    def checkEvents(self): 
        for event in pygame.event.get(): #Get all new events.
            try:    
                if event.type == pygame.QUIT: 
                    pygame.quit(); sys.exit();
            except SystemExit:  #To catch the error raised by sys.exit.
                return True
            if event.type == pygame.KEYDOWN: 
                if event.key == pygame.K_SPACE: #Pause until space is pressed again.
                    i = True
                    self.setInfo()
                    while i: 
                        for new_event in pygame.event.get():
                            if new_event.type == pygame.KEYDOWN and new_event.key == pygame.K_SPACE:
                                i = False
                            try:
                                if new_event.type == pygame.QUIT:
                                    pygame.quit(); sys.exit();  #To catch the error raised by sys.exit.
                            except SystemExit:
                                return True
                elif event.key == pygame.K_h: 
                    if self.displayinfo:
                        self.displayinfo = False
                    else:
                        self.displayinfo = True 
                elif event.key == pygame.K_v: 
                    if self.vectordisplay:
                        self.vectordisplay = False
                    else: 
                        self.vectordisplay = True
                                   
        if self.help > 0:  #To slow zooming speed down a little. 
            self.help -= 1 
        else:
            keys_down = pygame.key.get_pressed()
            if keys_down[pygame.K_o]: 
                self.zoom(False)
            elif keys_down[pygame.K_i]: 
                self.zoom(True)
            elif keys_down[pygame.K_f]:
                self.setSpeed(True)
            elif keys_down[pygame.K_s]:
                self.setSpeed(False)
            elif keys_down[pygame.K_l]:
                self.adjustDt(True)
            elif keys_down[pygame.K_k]:
                self.adjustDt(False)
            elif keys_down[pygame.K_b]:
                self.toggleFullscreen()
        return False
    
    def zoom(self, zoom_type):
        #Helper function for check_events.
        if zoom_type:
            self.crd_max /= 1.1 #Zoom the actual window.
            for i in range(len(self.radius_list)):  #Zoom the objects in the window.
                self.radius_list[i] *= 1.05
        else:
            self.crd_max *= 1.1
            for i in range(len(self.radius_list)):
                self.radius_list[i] /= 1.05

        self.help = self.fps // 8
        
    def setSpeed(self, faster):
        #Helper function for check_events.
        if faster:
            if self.fps < 500:
                self.fps += 5
        else:
            if self.fps > 5:
                self.fps -= 5
        self.help = self.fps // 15  
        
    def adjustDt(self, longer):
        #Helper function for check_events.
        if longer:
            self.dt += self.dt * 0.1
        elif self.dt > 1:
            self.dt -= self.dt * 0.1
        self.help = self.fps // 8
        
    def toggleFullscreen(self):
        #Helper function for check_events.
        if self.screen_width != self.full_width:
            self.screen_width = self.full_width
            self.screen_heigth = self.full_heigth
            pygame.display.set_mode((self.screen_width, self.screen_heigth), pygame.FULLSCREEN)
            
        else:
            self.screen_width = self.full_width // 2
            self.screen_heigth = self.full_heigth // 2
            pygame.display.set_mode((self.screen_width, self.screen_heigth))            
        
    def simulate(self):
        self.list = tuple(self.list)
        self.setScreen()
        self.setCrd_max()
        self.setScaledRadiusList()
        clock = pygame.time.Clock()
        closed, collide = None, [False]
        
        while self.time_elapsed <= self.length:
            max_vel, max_acc = self.calculateNextFrame()
            closed = self.checkEvents()
            collide = self.planetsCollide()
            if collide[0] or closed:
                break
            self.plotNextFrame(max_vel, max_acc)
            self.time_elapsed += self.dt
            clock.tick(self.fps)
            
        pygame.quit()
        return closed, collide
        
