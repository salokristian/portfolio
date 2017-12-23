#ifndef MAP_HPP_
#define MAP_HPP_


#include "Cell.hpp"
#include "Character.hpp"
#include "Item.hpp"
#include "Ammunition.hpp"
#include "Food.hpp"
#include "Soldier.hpp"
#include "Boss.hpp"
#include "Scout.hpp"
#include <vector>
#include <string>
#include <map>
#include <memory>
#include <algorithm>
#include <iostream>  
#include <ctype.h>
#include <stdlib.h>
#include <cmath>


typedef std::pair<std::shared_ptr<Character>, std::pair<int, int>> CharacterWithPos;
typedef std::map<std::pair<int, int>, std::vector<std::pair<int, int>>> MovableWithPath;
typedef std::map<std::pair<int, int>, std::vector<std::pair<int, int>>>* MovableWithPathPtr;

struct UnitFactory {
	std::shared_ptr<Character> returnCharacter(char a);
	std::shared_ptr<Item> returnItem(char a);
};


struct HitInfo {
	HitInfo() {}
	bool isDead = false;
	std::shared_ptr<Character> ptr;
	std::vector<int> hits;
};


class Map {
public:
	
	//there is non eed for any of copy, assign or destructor, so rule of 3 is filled
	Map() {}
	Map(const Map& obj);
	~Map() {}
	Map& operator= (const Map& obj);



	std::pair<std::vector<std::shared_ptr<Character>>, std::vector<std::shared_ptr<Character>>> constructMap(std::vector<std::string> blueprint);
	

	//returs a refrence to the selected cell
	Cell * getCell(int x, int y);

	//returns a vector of visible characters and their corresponding positions in the map
	std::pair<std::vector<CharacterWithPos>, std::vector<CharacterWithPos>> getVisibleUnits(std::vector<std::shared_ptr<Character>> onMySide, std::vector<std::shared_ptr<Character>> onEnemySide);
	std::vector<std::pair<int,int>> getItems();

	//returns a map of cellcoordinates that character can move to, keys are movable cells, values are path to them
	MovableWithPath getMovableCells(Cell * from, std::pair<std::vector<CharacterWithPos>, std::vector<CharacterWithPos>> currentVisible);
	//truncates path so you can't move into a already taken cell (by invisible enemy unit)
	std::vector<std::pair<int, int>> checkPath(std::vector<std::pair<int, int>>);

	//this should probably return a array of points to render unit movements properly...
	void move(Cell* from, Cell *to);
	
	//return a list of cellcoordinates that chara	cter in from has vision of
	std::vector<std::pair<int, int>> getTargetableCells(Cell * from, std::vector<CharacterWithPos> visibleEnemies);

	//would rather do this from cell to cell than char to char, since our characters dont have any position info.(project needed to have line of sight)
	//should probably return info on the result of the attack for rendering
	HitInfo attack(Cell* from, Cell* to);


	//for debugging, returns the playfield as strings, with no units drawn
	std::vector<std::string> drawMapDebug();
	
	//gets all characters in the map and their positions
	std::vector<CharacterWithPos> getUnits();
	//returns all characters on the same team and their positions
	std::vector<CharacterWithPos> getTeamUnits(std::vector<std::shared_ptr<Character>> myTeam);

private:
	
	int xSize;
	int ySize;
	std::vector<std::vector<Cell>> grid;
	double gridDistance(std::pair<int, int> from, std::pair<int, int> to);
	void recursivePath(std::size_t length, std::pair<int, int> pos, std::vector<std::pair<int, int>> path, MovableWithPathPtr map, std::vector<CharacterWithPos> allvisible);
	bool lineOfSight(std::pair<int, int> start, std::pair<int, int> end);
	UnitFactory unitFactory;
};


#endif /* MAP_HPP_*/
