#include "Map.hpp"


Cell * Map::getCell(int x, int y)
{
	return &grid[x][y];
}


std::pair<std::vector<CharacterWithPos>, std::vector<CharacterWithPos>>  Map::getVisibleUnits(std::vector<std::shared_ptr<Character>> onMySide, std::vector<std::shared_ptr<Character>> onEnemySide)
{

	std::vector<CharacterWithPos> temp;
	auto myUnits = getTeamUnits(onMySide);
	auto enemyUnits = getTeamUnits(onEnemySide);
	//first cycle through own units to not get a list of enemyunits
	for (auto enemyUnit : enemyUnits) {
		bool visible = false;
		for (auto myUnit : myUnits) {
			visible = lineOfSight(myUnit.second, enemyUnit.second);
			if(visible)
				break;
		}
		if (visible)
			temp.push_back(enemyUnit);
	}
	return std::pair<std::vector<CharacterWithPos>, std::vector<CharacterWithPos>>(myUnits, temp);
}

std::vector<std::pair<int, int>> Map::getItems()
{
	std::vector<std::pair<int, int>> ret;
	for (auto i : grid) {
		for (auto j : i) {
			auto items = j.getItems();
			if (items.size() > 0) {
				std::pair<int, int> temp = j.getPosition();
				ret.push_back(temp);
			}
		}
	}
	return ret;
}

//uses recursion
MovableWithPath Map::getMovableCells(Cell * from, std::pair<std::vector<CharacterWithPos>, std::vector<CharacterWithPos>> currentVisble)
{
	auto retMap = MovableWithPath();
	size_t length = from->getCharacter().get()->getSpeed()*from->getCharacter().get()->getActionPoints(); //+20;
	auto emptyPath = std::vector<std::pair<int, int>>();
	std::pair<int, int> pos = from->getPosition();
	auto allvisible = currentVisble.first;
	allvisible.insert(allvisible.end(), currentVisble.second.begin(), currentVisble.second.end());
	//recursivePath(length, std::pair<int,int>(pos), emptyPath, &retMap);
	if (length > 0) {
		if (pos.second - 1 >= 0)
			recursivePath(length, std::pair<int, int>(pos.first, pos.second - 1), emptyPath, &retMap, allvisible);
		if (pos.first + 1 <= xSize)
			recursivePath(length, std::pair<int, int>(pos.first + 1, pos.second), emptyPath, &retMap, allvisible);
		if (pos.second + 1 <= ySize)
			recursivePath(length, std::pair<int, int>(pos.first, pos.second + 1), emptyPath, &retMap, allvisible);
		if (pos.first - 1 >= 0)
			recursivePath(length, std::pair<int, int>(pos.first - 1, pos.second), emptyPath, &retMap, allvisible);
	}

	return retMap;
}

std::vector<std::pair<int, int>> Map::checkPath(std::vector<std::pair<int, int>> fullPath)
{
	for (auto it = fullPath.begin(); it < fullPath.end(); it++) {
		auto enemyCell = getCell(it->first, it->second);
		if (enemyCell->getCharacter() != nullptr) {
			//std::cout << "ran into an enemy" << std::endl;
			//we have ran into an enemy, have to find out the first position where we can see the enemy
			for (auto last = it; last >= fullPath.begin(); last--) {
				auto lastVisible = getCell(last->first, last->second);
				auto lastPos = lastVisible->getPosition();
				auto enemyPos = enemyCell->getPosition();
				//std::cout << "lineOfSight callled with start " << lastPos.first << "," << lastPos.second << " and end: " << enemyPos.first << "," << enemyPos.second << " was " << lineOfSight(lastPos, enemyPos) << std::endl;
				if (!lineOfSight(lastPos, enemyPos)) {
					//take a subset of the fullPath until last
					last++;
					last++;
					std::vector<std::pair<int, int>> newPath(fullPath.begin(), last);
					return newPath;
				}
			}
		}
	}
	return fullPath;
}

double Map::gridDistance(std::pair<int, int> from, std::pair<int, int> to)
{
	auto xDif = to.first - from.first;
	auto yDif = to.second - from.second;
	return std::sqrt(std::pow(xDif, 2) + std::pow(yDif, 2));
}

void Map::recursivePath(std::size_t length, std::pair<int, int> pos, std::vector<std::pair<int, int>> path, MovableWithPathPtr map, std::vector<CharacterWithPos> allVisible)
{
	//std::cout << "recursive path called in position: " << pos.first << "," << pos.second << " with length: " << length << std::endl;
	//if the cell is free we add/update it to the map
	auto current = &grid[pos.first][pos.second];
	if (current->isFree()) {
		path.push_back(pos);
	}
	else {
		std::shared_ptr<Character> currentChar = current->getCharacter();
		if (currentChar.get() != nullptr) { //cell is taken, but can we see it?
			for (auto visUnit : allVisible) {
				if (visUnit.first == currentChar) {
					//we can see it -> cant move here
					return;
				}
			}
			path.push_back(pos);
		}
		else
			return;	//its a wall
	}
	auto it = (*map).find(pos);
	if (it != (*map).end()) {
		if (it->second.size() > path.size())
			//visited cell, update the map
			(*map)[pos] = path;
		else
			return;
	}
	else {
		(*map)[pos] = path;
	}
	//we can go only as far as we can move
	if (path.size() < length) {
		//advance clockwise starting from the top to the next cell if it exists within the map
		if (pos.second - 1 >= 0)
			recursivePath(length, std::pair<int, int>(pos.first, pos.second - 1), path, map, allVisible);
		if (pos.first + 1 <= xSize)
			recursivePath(length, std::pair<int, int>(pos.first + 1, pos.second), path, map, allVisible);
		if (pos.second + 1 <= ySize)
			recursivePath(length, std::pair<int, int>(pos.first, pos.second + 1), path, map, allVisible);
		if (pos.first - 1 >= 0)
			recursivePath(length, std::pair<int, int>(pos.first - 1, pos.second), path, map, allVisible);
	}
}

//lineOfSight using Bresenham's line algorithm
bool Map::lineOfSight(std::pair<int, int> start, std::pair<int, int> end)
{
	if (end.first - start.first == 0) { //on the same y coordinate, slope is infinite
		if (end.second >= start.second) {
			for (int y = start.second + 1; y < end.second; y++) {
				if (!getCell(start.first, y)->isFree()) {
					return false;
				}
			}
		}
		else {
			for (int y = start.second - 1; y > end.second; y--) {
				if (!getCell(start.first, y)->isFree()) {
					return false;
				}
			}
		}
		return true;
	}
	double slope = (double)(end.second - start.second) / (end.first - start.first);
	//check every cell on the path, if they are all free, the target is visible
	if (slope >= -1 && slope <= 1) {
		if (start.first < end.first) {
			for (int x = start.first + 1; x < end.first; x++) {
				int y = (int)(slope*(x - start.first) + start.second + 0.5);
				if (!getCell(x, y)->isFree()) {
					return false;
				}
			}
		}
		else {
			for (int x = start.first - 1; x > end.first; x--) {
				int y = (int)(slope*(x - start.first) + start.second + 0.5);
				if (!getCell(x, y)->isFree()) {
					return false;
				}
			}
		}
		
	}
	else {
		if (start.second < end.second) {
			for (int y = start.second + 1; y < end.second; y++) {
				int x = (int)((y - start.second) / slope + start.first + 0.5);
				if (!getCell(x, y)->isFree()) {
					return false;
				}
			}
		}
		else {
			for (int y = start.second - 1; y > end.second; y--) {
				int x = (int)((y - start.second) / slope + start.first + 0.5);
				if (!getCell(x, y)->isFree()) {
					return false;
				}
			}
		}
		
	}
	return true;
}

std::vector<CharacterWithPos> Map::getUnits()
{
	std::vector<CharacterWithPos> ret;
	for (auto i : grid) {
		for (auto j : i) {
			std::shared_ptr<Character> temp = j.getCharacter();
			if (temp != nullptr) {
				ret.push_back(std::pair<std::shared_ptr<Character>, std::pair<int,int>>(temp, j.getPosition()));
			}
		}
	}
	return ret;
}

std::vector<CharacterWithPos> Map::getTeamUnits(std::vector<std::shared_ptr<Character>> myTeam)
{
	std::vector<CharacterWithPos> ret;
	for (auto i : grid) {
		for (auto j : i) {
			std::shared_ptr<Character> temp = j.getCharacter();
			if (temp != nullptr) {
				//we only want the units on our side
				for (auto i : myTeam) {
					if(i == temp)
						ret.push_back(CharacterWithPos(temp, j.getPosition()));
				}
				
			}
		}
	}
	return ret;
}

//doesn't check if the move is valid, should always get cell to from getMovableCells
void Map::move(Cell * from, Cell* to)
{
	to->setCharacter(from->getCharacter());
	from->setCharacter(nullptr);
}

std::vector<std::pair<int, int>> Map::getTargetableCells(Cell * from, std::vector<CharacterWithPos> visibleEnemies)
{
	std::vector<std::pair<int, int>> retPos;
	int range = from->getCharacter()->getActiveGun()->getRange();
	for (auto enemy : visibleEnemies) {
		if (gridDistance(from->getPosition(), enemy.second) <= range) {
			retPos.push_back(enemy.second);
		}
	}
	return retPos;
}

HitInfo Map::attack(Cell * from, Cell * to)
{
	HitInfo retInfo;
	auto attacker = from->getCharacter();
	auto target = to->getCharacter();
	auto weapon = attacker->getActiveGun();
	retInfo.ptr = target;
	int hitChance = weapon->getProbability(); 
	int damage = weapon->getDamage(); 
	if (attacker->getActiveGun()->getAmmo() > 0) {
		int bulletsShot = weapon->shoot();
		for (int x = 0; x < bulletsShot; x++) {
			int dmgDealt = 0;
			if (rand() % 100 + 1 <= hitChance)
				dmgDealt = damage;
			else
				dmgDealt = 0;
			retInfo.hits.push_back(dmgDealt);
			target->changeHP(-dmgDealt);
			retInfo.isDead = (target->getHP() == 0);
			if (retInfo.isDead) {
				to->setCharacter(nullptr);
			}
		}
	}


	return retInfo;

}


//returns a vector<String> of the map for debugging
std::vector<std::string> Map::drawMapDebug()
{
	std::vector<std::string> map;
	for (auto i : grid) {
		std::string row;
		for (auto current : i) {
			//std::cout << "this cell was free" << current.isFree() << std::endl;
			if (current.isFree()) {
				
				row += " ";		//doesn't draw items
			}
			else {
				auto dude = current.getCharacter();
				if (dude == nullptr)
					row += "#";
				else {
					row += " ";
				}
				
			}
		}
		map.push_back(row);
	}
	return map;
}



Map::Map(const Map & obj)
{
	this->xSize = obj.xSize;
	this->ySize = obj.ySize;
	this->grid = obj.grid;
	this->unitFactory = obj.unitFactory;
}

Map & Map::operator=(const Map & obj)
{
	this->xSize = obj.xSize;
	this->ySize = obj.ySize;
	this->grid = obj.grid;
	this->unitFactory = obj.unitFactory;
	return *this;
}

std::pair<std::vector<std::shared_ptr<Character>>, std::vector<std::shared_ptr<Character>>> Map::constructMap(std::vector<std::string> blueprint)
{
	std::vector<std::shared_ptr<Character>> teamSmallLetters;
	std::vector<std::shared_ptr<Character>> teamBigLetters;
	std::pair<std::vector<std::shared_ptr<Character>>, std::vector<std::shared_ptr<Character>>> characters;
	int x = 0;
	int y = 0;
	for (auto i = blueprint.begin(); i < blueprint.end(); i++, x++) {
		std::vector<Cell> row;
		
		y = 0;
		for (auto j = (*i).begin(); j < (*i).end(); j++, y++) {
			Cell temp = Cell(x, y);

			auto unitMarker = (*j);
			auto unitMarkerLower = tolower(unitMarker);
			if (unitMarker == '#') {
				temp.setType(0);
			}
			else {
				auto unitPtr = unitFactory.returnCharacter(unitMarkerLower);
				if (unitPtr != nullptr) {
					temp.setCharacter(unitPtr);
					if (islower(unitMarker))
						teamSmallLetters.push_back(unitPtr);
					else
						teamBigLetters.push_back(unitPtr);
					
				}
				else {
					auto itemPtr = unitFactory.returnItem(unitMarkerLower);
					if (itemPtr != nullptr) {
						temp.addItem(itemPtr);
					}
					
				}
				temp.setType(1);
			}
			row.push_back(temp);
		}
		grid.push_back(row);
	}

	//get max indexies of the grid
	xSize = x;
	ySize = y;

	//debug
	//std::cout << grid.size() << std::endl;
	//std::cout << grid[0].size() << std::endl;
	return std::pair<std::vector<std::shared_ptr<Character>>, std::vector<std::shared_ptr<Character>>>(teamSmallLetters, teamBigLetters);
}

std::shared_ptr<Character> UnitFactory::returnCharacter(char a){
	if (a == 's')
		return std::make_shared<Soldier>();
	else if (a == 'b')
		return std::make_shared<Boss>();
	else if (a == 'c')
		return std::make_shared<Scout>();
	else
		return nullptr;
}

std::shared_ptr<Item> UnitFactory::returnItem(char a) {
	if (a == 'm')
		return std::make_shared<MachineGun>();
	else if (a == 'p')
		return std::make_shared<Pistol>();
	else if (a == 'r')
		return std::make_shared<Riffle>();
	else if (a == 'a')
		return std::make_shared<Ammunition>();
	else if (a == 'f')
		return std::make_shared<Food>();
	else
		return nullptr;
}
