#include "FileRead.hpp"
#include "Map.hpp"
#include "Character.hpp"
#include "Item.hpp"
#include <vector>
#include <string>
#include <iostream> 



int main() {
	FileRead fr;
	fr.readFile("GameLevels.txt");
	std::cout << "raw map from file:" << std::endl;
	std::vector<std::string> lvl1 = fr.getLvlAt(1);
	for (auto line : lvl1) {
		std::cout << line << std::endl;
	}

	
	Map myMap = Map();
	auto teams = myMap.constructMap(lvl1);

	std::cout << "first team size: " << teams.first.size() << std::endl;

	auto drawMap = myMap.drawMapDebug();

	std::cout << "draw map size: " << drawMap.size() << " x " << drawMap[0].size() << std::endl;
	std::cout << "map base: " << std::endl;
	for (auto line : drawMap) {
		std::cout << line << std::endl;
	}

	auto allUnits = myMap.getUnits();
	std::cout << "size of all units: " << allUnits.size() << std::endl;
	for (auto unit : allUnits) {
		std::cout << "position: " << "X: " << unit.second.first << " Y: " << unit.second.second << std::endl;
	}
	std::cout << std::endl;

	auto firstTeam = myMap.getTeamUnits(teams.first);
	std::cout << "size of first team: " << firstTeam.size() << std::endl;
	for (auto unit : firstTeam) {
		std::cout << "position: " << "X: " << unit.second.first << " Y: " << unit.second.second << std::endl;
		drawMap[unit.second.first][unit.second.second] = 's';
	}
	auto secondTeam = myMap.getTeamUnits(teams.second);
	for (auto unit : secondTeam) {
		drawMap[unit.second.first][unit.second.second] = 'S';
	}

	std::cout << "character positions using getTeamUnits()" << std::endl;
	for (auto line : drawMap) {
		std::cout << line << std::endl;
	}

	drawMap = myMap.drawMapDebug();


	std::cout << "visible characters to the firstTeam" << std::endl;


	auto visible = myMap.getVisibleUnits(teams.first, teams.second);
	std::cout << "visible length " << visible.size() << std::endl;

	for (auto unit : visible) {
		drawMap[unit.second.first][unit.second.second] = 'X';
	}
	for (auto line : drawMap) {
		std::cout << line << std::endl;
	}



	std::cout << "possible movable cells by unit in 18,1" << std::endl;
	auto movableMap = myMap.getMovableCells(myMap.getCell(18,1));

	std::cout << "movable count: " << movableMap.size() << std::endl;
	std::vector<std::pair<int, int>> movableKeys;
	for (auto it = movableMap.begin(); it != movableMap.end(); ++it) {
		movableKeys.push_back(it->first);
	}

	drawMap = myMap.drawMapDebug();
	for (auto key : movableKeys) {
		drawMap[key.first][key.second] = 'x';
	}
	for (auto line : drawMap) {
		std::cout << line << std::endl;
	}

	//next test is for truncating path, if you want to run this you must edit the function getmovablecells and increase the length to 20b
	//this is only until we can change character speeds
	/*
	
	auto path = movableMap[std::pair<int, int>(2, 5)];

	std::cout << "full path to point 2,5" << std::endl;
	drawMap = myMap.drawMapDebug();
	for (auto pos : path) {
		drawMap[pos.first][pos.second] = 'x';
	}
	for (auto line : drawMap) {
		std::cout << line << std::endl;
	}

	std::cout << "truncated path to point 2,5" << std::endl;
	drawMap = myMap.drawMapDebug();
	auto trunPath = myMap.checkPath(path);
	for (auto pos : trunPath) {
		drawMap[pos.first][pos.second] = 'x';
	}
	for (auto line : drawMap) {
		std::cout << line << std::endl;
	}
	*/


	std::cout << "test ended" << std::endl;




	while(1){}
}