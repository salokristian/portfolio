'''
Created on 25.3.2016

@author: kristiansalo
'''
import sys
from PyQt4.QtGui import * 

class Window(QWidget):
    
    def __init__(self):
        super(Window, self).__init__()
        
    def setGrid(self, spacing):
        self.grid = QGridLayout()
        self.grid.setSpacing(spacing)
        
    def setWindow(self, title, width, heigth):
        self.setLayout(self.grid)
        self.setWindowTitle(title) 
        self.resize(width, heigth)
        self.center()   
        
    def setTitle(self, text, row1, col1, row2, col2):
        qf = QFont()
        qf.setBold(True)
        title = QLabel(text)
        title.setFont(qf)
        self.grid.addWidget(title, row1, col1, row2, col2)    
        
    def center(self):
        screen = QDesktopWidget().screenGeometry()
        mysize = self.geometry()
        hpos = ( screen.width() - mysize.width() ) / 2
        vpos = ( screen.height() - mysize.height() ) /2 
        self.move(hpos, vpos)
        
    def setButton(self, text, row, col, tooltip, action):
        button = QPushButton(text)
        button.setToolTip(tooltip)
        button.clicked.connect(action)
        self.grid.addWidget(button, row, col)
        
    def setRow(self, row, title, unit = None, tooltip = None):
        #Adds one row that includes a title, a textbox and an unit.
        label, unit, lineWidget = QLabel(title), QLabel(unit), QLineEdit()
        lineWidget.setToolTip(tooltip)
        self.rows[title] = lineWidget
        self.grid.addWidget(label, row, 1, 1, 2)
        self.grid.addWidget(lineWidget, row, 3, 1, 2)
        self.grid.addWidget(unit, row, 5)
        
        
    def closeWindow(self):
        #Closes the window without shutting down the program.
        self.running = True
        self.close()
   
    
    