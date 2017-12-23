#Configure g++ to use c++11
set (CMAKE_CXX_STANDARD 11)

#Minimum accepted cmake version is 3.5.1
cmake_minimum_required(VERSION 3.5.1)

#Define g++ options
SET(CMAKE_CXX_FLAGS "-Wall -Wextra -Wno-missing-field-initializers")

#Enable debug symbols by default
#must be done before project() statement
if(NOT CMAKE_BUILD_TYPE)
  set(CMAKE_BUILD_TYPE Debug CACHE STRING "Choose the type of build (Debug or Release)" FORCE)
endif()
#(you can also set it on the command line: -D CMAKE_BUILD_TYPE=Release)

project(myproject)

# Define sources and executable
set(EXECUTABLE_NAME "laser-squad_ex")
add_executable(${EXECUTABLE_NAME} GameAudio.cpp  Menuwindow.cpp Player.cpp Tilemap.cpp Sidebar.cpp GUI.cpp main.cpp Game.cpp FileRead.cpp Map.cpp Cell.cpp Character.cpp Boss.cpp Soldier.cpp Scout.cpp Item.cpp Ammunition.cpp Food.cpp Gun.cpp)


# Detect and add SFML
set(CMAKE_MODULE_PATH "${CMAKE_SOURCE_DIR}/cmake_modules" ${CMAKE_MODULE_PATH})
#Find any version 2.X of SFML
#See the FindSFML.cmake file for additional details and instructions
find_package(SFML 2 REQUIRED network audio graphics window system)
if(SFML_FOUND)
  include_directories(${SFML_INCLUDE_DIR})
  target_link_libraries(${EXECUTABLE_NAME} ${SFML_LIBRARIES} ${SFML_DEPENDENCIES})
endif()


# Install target
install(TARGETS ${EXECUTABLE_NAME} DESTINATION bin)

add_custom_command(TARGET ${EXECUTABLE_NAME} POST_BUILD
                   COMMAND ${CMAKE_COMMAND} -E copy_directory
                       ${CMAKE_SOURCE_DIR}/includes $<TARGET_FILE_DIR:${EXECUTABLE_NAME}>/includes
		   COMMAND ${CMAKE_COMMAND} -E copy_directory
                       ${CMAKE_SOURCE_DIR}/sounds $<TARGET_FILE_DIR:${EXECUTABLE_NAME}>/sounds)
