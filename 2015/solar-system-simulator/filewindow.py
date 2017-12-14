'''

Created on 12.3.2016
@author: kristiansalo
'''

import sys
from window import *
import objectwindow
import simulation
import object
import math

class FileWindow(Window):    

    def __init__(self):
        super(FileWindow, self).__init__()
        self.file = None
        self.objects = '' #Create a string of all the objects in csv-format for saving the simulation.
        self.simulation = None
        self.rows = {} #Has linewidgets stored so that you can conviniently check written text.
        self.running = False
        self.initUI()

    def initUI(self):
        self.setGrid(10)
        self.setTitle('Insert the parametres for the simulation.', 1, 1, 1, 5)
        self.setRow(2, 'Simulation length', 'days', None)
        self.setRow(3, 'Simulation timestep', 'days', None)
        self.setFileDialog()
        self.setButton('Next', 5, 4, None, self.next)
        self.setButton('Quit', 5, 5, None, self.close)
        self.setWindow('Solar system simulator', 400, 200)
        
    def setFileDialog(self):
        file = QLabel('Solar system file')
        fileName = QLabel()
        self.rows['File'] = fileName
        fileB = QPushButton('Browse')
        fileB.setToolTip('Open the solar system file used in simulation.')
        fileB.clicked.connect(self.fileDialog)
        self.grid.addWidget(file, 4, 1, 1, 2)
        self.grid.addWidget(fileName, 4, 3, 1, 2)
        self.grid.addWidget(fileB, 4, 5)
        
    
    def fileDialog(self):
        #Opens file dialog so that the user can choose a file.
        fname = QFileDialog.getOpenFileName(self, 'Open file', '/', 'Text files (*.txt)')
        if fname != '':
            if len(fname) < 30:
                self.rows['File'].setText(fname)
            else:
                self.rows['File'].setText(fname[-30:])
            self.file = fname

    def createSimulation(self):
        try:
            length = float(self.rows['Simulation length'].text())
            dt = float(self.rows['Simulation timestep'].text())
            if length <= 0 or dt <= 0:
                raise ValueError
            elif length >= 365*13*10**9:
                raise TypeError
        except ValueError:
            QMessageBox.information(self, "Message", 'Timestep and length must be positive numbers.')
            return False
        except TypeError:
            QMessageBox.information(self, "Message", 'Length must not be bigger than the lifespan of our universe (13 billion years).')
            return False
            
        else: #If everything is fine, create new simulation.
            dt = dt * 24 * 3600 
            length = length * 24 * 3600 
            self.simulation = simulation.Simulation([], dt, length)
            return True

    def readFile(self):
        if self.file == None:
            return True
        file = open(self.file, 'r')
        self.objects = ''
        counter = 1
        for line in file:
            clear_line = line.rstrip().split(',')
            if len(clear_line) != 12:
                QMessageBox.information(self, "Message", 'Line number {:d} had wrong number of values.' .format(counter))    
                file.close()
                return False
            new_object = self.checkData(clear_line, counter)       
            if new_object != None:
                self.objects += line
                self.simulation.list.append(new_object)
            else:
                file.close()
                return False
            counter += 1
        file.close()
        return True
    
    def checkData(self, line, line_no):
        #Checks that one line of file has correct data.  
        helper = [False, False] #Assume that some input value is wrong.
        try: 
            velocity_list = line[9:12]
            location_list = line[6:9]
            color_list = line[3:6]
            name = line[0]
            mass = float(line[1])
            radius = float(line[2])
            for i in range(3):
                velocity_list[i] = float(velocity_list[i])
                location_list[i] = float(location_list[i])
                color_list[i] = int(color_list[i])
            vel_magnitude = math.sqrt(velocity_list[0]**2 + velocity_list[1]**2 + velocity_list[2]**2)
            loc_magnitude = math.sqrt(location_list[0]**2 + location_list[1]**2 + location_list[2]**2)
            if not 0 < radius < 10**31:  #Check radius.
                QMessageBox.information(self, "Message", 'Error on line {:d}: Radius must be a value in interval [0 , 10^31].' .format(line_no))
            elif not 0 < len(name) < 20:  #Check name.
                QMessageBox.information(self, "Message", 'Error on line {:d}: Object name must be longer than 0 but shorter than 20 characters.' .format(line_no))
            elif not 0 < mass < 10**31:  #Check mass.
                QMessageBox.information(self, "Message", 'Error on line {:d}: Mass must be a value in interval [0 , 10^31].' .format(line_no))
            elif vel_magnitude > 299.8*10**6:  #Check velocity.
                QMessageBox.information(self, "Message", 'Error on line {:d}: Velocity must not be greater than the speed of light.' .format(line_no))
            elif not 0 <= color_list[0] <= 255 or not 0 <= color_list[1] <= 255 or not 0 <= color_list[2] <= 255:
                QMessageBox.information(self, "Message", 'Error on line {:d}: Color values must be in interval [0,255].' .format(line_no))
            elif loc_magnitude > 7500*10**9:
                QMessageBox.information(self, "Message", 'Location coordinates must not be greater than Plutos distance from the sun (7500km).')              
            else:
                helper[0] = True       
        except TypeError:
            QMessageBox.information(self, "Message", 'Error on line {:d}: Name can only contain letters.' .format(line_no))
        except ValueError:
            QMessageBox.information(self, "Message", 'Error on line {:d}: Mass, radius, color, location and velocity must be numbers.' .format(line_no))
        else:
            helper[1] = True
        #If all of the values were appropriate.
        if helper[0] and helper[1]: 
            return object.Object(mass, radius, location_list, velocity_list, name, tuple(color_list))
        else:
            return None
        
    def next(self):
        if self.createSimulation() and self.readFile():
            self.running = True
            if len(self.objects) != 0:
                if self.objects[-1] == '\n':
                    self.new_window = objectwindow.ObjectWindow(self.simulation, self.objects)
                else:
                    self.new_window = objectwindow.ObjectWindow(self.simulation, self.objects + '\n')
            else:
                self.new_window = objectwindow.ObjectWindow(self.simulation, self.objects)
            self.new_window.show()
            self.close()