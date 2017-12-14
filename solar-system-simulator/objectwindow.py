'''
Created on 12.3.2016

@author: kristiansalo
'''
import sys
from window import *
import math
import object
class ObjectWindow(Window):
    
    def __init__(self, simulation, objects):
        super(ObjectWindow, self).__init__()
        self.simulation = simulation
        self.rows = {} #Has all linewidgets stored.
        self.objects = objects
        self.colour = [255,255,255]
        self.running = False
        self.initUI()
    
    def initUI(self):
        self.setGrid(10)
        self.setTitle('Insert the information of the object.', 1, 1, 1, 3)
        self.setRow(4, 'Name')
        self.setRow(5, 'Radius', 'km')
        self.setRow(6, 'Mass', 'kg')
        self.setRow(7, 'Velocity', 'km/s', 'Insert the x, y and z values seperated by a comma.')
        self.setRow(8, 'Location', 'au', 'Insert the x, y and z values seperated by a comma.')
        self.setColourDialog()
        self.setButton('Quit', 10, 5, None, self.close)
        self.setButton('Add', 10, 4, None, self.add)
        self.setButton('Simulate', 10, 3, None, self.simulate)
        self.setWindow('Solar system simulator', 400, 240)
        

    def setColourDialog(self):
        #Initializes colour dialog.
        self.label = QLabel('Colour')
        self.btn = QPushButton('Dialog', self)
        self.btn.clicked.connect(self.showDialog)
        self.grid.addWidget(self.label, 9, 1)
        self.grid.addWidget(self.btn, 9,3,1,2)
        
        col = QColor(255, 255, 255) 
        self.frm = QFrame(self)
        self.frm.setStyleSheet("QWidget { background-color: %s }" 
            % col.name())
        self.grid.addWidget(self.frm, 9, 5, 1, 1)  
        
    def showDialog(self):   
        #Shows color dialog from which the user can choose the object color. 
        col = QColorDialog.getColor()
        if col.isValid():
            self.frm.setStyleSheet("QWidget { background-color: %s }"
                % col.name())
            self.colour = [col.red(), col.green(), col.blue()]
        
    def simulate(self):
        if self.rows['Name'].text() == '': #If there is no name, assume there is no object user wants to add to the simulation.
            self.closeWindow()
        else:
            if self.add(): 
                self.closeWindow()
        
    def add(self):
        #Add object to simulation if it is correct.
        new_object = self.addObject()
        if new_object != None:
            QMessageBox.information(self, "Message", 'Object {:s} added to simulation.' .format(new_object.name))
            self.simulation.list.append(new_object) 
            for key in self.rows: #Clear textboxes. 
                self.rows[key].clear()
            return True
        return False
            
    def addObject(self):     
        #Check that given values are appropriate.
        helper = [False, False] #Assume that some input value is wrong.
        try: 
            velocity_list = self.rows['Velocity'].text().split(',')
            location_list = self.rows['Location'].text().split(',')
            if not len(location_list) == 3 or not len(velocity_list) == 3:
                raise AttributeError
        except AttributeError or IndexError:
            QMessageBox.information(self, "Message", 'Velocity and location must contain three values seperated by a comma.')
        else: #If there were no exceptions, let's do more tests.
            try: 
                radius = float(self.rows['Radius'].text()) * 1000 
                mass = float(self.rows['Mass'].text()) 
                name = self.rows['Name'].text()
                location = [0] * 3
                velocity = [0] * 3
                for i in range(3):
                    location[i] = float(location_list[i]) * 149597*10**6    #Change from au to meters.
                    velocity[i] = float(velocity_list[i]) * 1000            #Change from km/s to m/s.
                vel_magnitude = math.sqrt(velocity[0]**2 + velocity[1]**2 + velocity[2]**2)
                loc_magnitude = math.sqrt(location[0]**2 + location[1]**2 + location[2]**2)
                if not 0 < radius < 10**31:
                    QMessageBox.information(self, "Message", 'Radius must be a value in interval [0 , 10^31].')
                elif not 0 < len(name) < 20:
                    QMessageBox.information(self, "Message", 'Object name must be longer than 0 but shorter than 20 characters.')
                elif not 0 < mass < 10**31:
                    QMessageBox.information(self, "Message", 'Mass must be a value in interval [0 , 10^31].')
                elif vel_magnitude > 299.8*10**6:
                    QMessageBox.information(self, "Message", 'Velocity must not be greater than the speed of light.')
                elif loc_magnitude > 7500*10**9:
                    QMessageBox.information(self, "Message", 'Location coordinates must not be greater than Plutos distance from the sun (7500km).')                    
                else:
                    helper[0] = True       
            except TypeError:
                QMessageBox.information(self, "Message", 'Name can only contain letters.')
            except ValueError:
                QMessageBox.information(self, "Message", 'Mass, radius, location and velocity must be numbers.')
            else:
                helper[1] = True
        #If all of the values were appropriate.
        if helper[0] and helper[1]: 
            colour = ''     #Add new object to self.object-string so that saving the simulation if possible.
            loc_str = ''
            vel_str = ''
            for i in range(3):
                colour += ',' + str(self.colour[i]) 
                loc_str += ',' + str(location[i]) 
                vel_str += ',' + str(velocity[i]) 
            self.objects += name + ',' + str(mass) + ',' + str(radius) + colour + loc_str + vel_str + '\n'
            return object.Object(mass, radius, location, velocity, name, tuple(self.colour))
        else:
            return None 