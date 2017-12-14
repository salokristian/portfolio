'''
Created on 26.3.2016

@author: kristiansalo
'''
from window import *


class EndWindow(Window):
    
    def __init__(self, reason, object_list):
        super(EndWindow, self).__init__()
        self.reason = reason
        self.object_list = object_list
        self.running = False
        self.setUI()
        
    def setUI(self):
        self.setGrid(10)
        if self.reason[0]:
            self.setTitle('The simulation ended becouse objects {:s} and {:s} collided.' .format(self.reason[1].name, self.reason[2].name), 1, 1, 1 , 3)
        else:
            self.setTitle('The simulation ended because the time ran out.', 1, 1, 1, 3)
        self.grid.addWidget(QLabel('What do you want to do next?'), 2, 1)
        self.setButton('Save simulation', 3, 1, 'Saves simulation file.', self.saveFile)
        self.setButton('New Simulation', 3, 2, 'Create a new simulation.', self.simulate)
        self.setButton('Quit', 3, 3, None, self.close)
        self.setWindow('End of simulation', 220, 160)

    
    def simulate(self):
        self.closeWindow()
        
    def saveFile(self):
        filename = QFileDialog.getSaveFileName(self, 'Save File', '/Simulation', 'Text files (*.txt)')
        if filename != '':
            f = open(filename, 'w')
            f.write(self.object_list)
            f.close()