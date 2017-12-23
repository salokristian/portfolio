# README file for Laser Squad

C++ programming project done for the course ELEC-A7150 - C++ Programming, 
Autumn 2017. The project uses the SFML library for the user interface for 
the game.

  * `plan/` -- Here is the project plan and instructor's comments and other
    group's reviews about the project.
  
  * `doc/` -- Here is the documentation about the project as a PDF file.

  * `src/` -- Here are the C++ source files. `tests` subfolder contains some
    unit tests that we did.

  * `README.md` -- This file you are reading.

# How to compile with CMake

`CMakeLists.txt` in the `src/` folder contains the script for generating 
the makefile. `cmake_modules` folder contains the file `FindSFML.cmake`, 
which is a helper file for `CMakeLists.txt` for finding the SFML sources.

CMake was tested to function in Aalto Linux machines. The
procedure for an out-of-place build using it is:

- go to git/laser-squad-1/ directory
- create a new directory for the build  
- navigate to the build directory       
- run cmake from the build directory    
- generate the executable               
- run the executable                    

```
mkdir build
cd build
cmake ../src/
make
./laser-squad_ex
```


# How to play the game

When you run the executable, the game should show you a starting menu screen. 
You need to choose names for Players 1 and 2, and also the Map number you want 
to play. Player names only accept letters, and the Map takes numbers. Move 
between the choices using ***arrowkeys UP and DOWN or ENTER,*** then finally select 
Start game at the bottom and press ***ENTER*** to start the game.

In the game, press ***i*** to open and close the info screen. Scroll the map 
with ***arrowkeys*** and move/select your units with ***Left mouse click,*** 
and attack enemy units with ***Right mouse click.*** Press ***ENTER*** to pass 
the turn to the other player. Both players have one ***Boss*** character. The 
game ends when the ***Boss*** character is defeated.

