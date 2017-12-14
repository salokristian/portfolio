'''
Created on 27.3.2016o

@author: Krisu
'''
import filewindow
import endwindow

def main():
    running = True
    app = filewindow.QApplication(filewindow.sys.argv)
    while running:
        window = filewindow.FileWindow()
        window.show()
        app.exec_()
        if window.running:
            object_string, simulation, running = window.new_window.objects, window.new_window.simulation, window.new_window.running
            if not running:
                break
        else:
            break
        closed, collided = simulation.simulate()
        if closed:
            break
        window = endwindow.EndWindow(collided, object_string)
        window.show()
        app.exec_()
        running = window.running
    
main()    