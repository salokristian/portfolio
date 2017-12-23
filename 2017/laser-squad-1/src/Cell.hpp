#ifndef CELL_HPP_
#define CELL_HPP_


#include "Character.hpp"

class Cell {
public:

	Cell(const Cell& obj);
	~Cell() {}
	Cell& operator= (const Cell& obj);

	Cell(int x, int y) : xPos(x), yPos(y), items() {}
	
	
	
	
	std::shared_ptr<Character> getCharacter();
	void setCharacter(std::shared_ptr<Character> newChar);
	std::vector<std::shared_ptr<Item>> getItems();
	void addItem(std::shared_ptr<Item> it);
	std::shared_ptr<Item> removeItem(int index);
	bool isFree();
	std::pair<int, int> getPosition();
	void setType(int type);

private:
	
	
	int xPos; //coordinates in the map array
	int yPos;
	std::shared_ptr<Character> myChar; //character in the cell, null if doesn't exist
	std::vector<std::shared_ptr<Item>> items; //items in the cell, empty if none exists
	int celltype; //defines the type of the cell, as in 0 is a wall 1 is a normal floor and so on

};


#endif /*CELL_HPP_*/