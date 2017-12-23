#include "Cell.hpp"
#include <iostream>


Cell::Cell(const Cell & obj)
{
	this->celltype = obj.celltype;
	this->xPos = obj.xPos;
	this->yPos = obj.yPos;
	this->myChar = obj.myChar;
	this->items = obj.items;
}

Cell & Cell::operator=(const Cell & obj)
{
	this->celltype = obj.celltype;
	this->xPos = obj.xPos;
	this->yPos = obj.yPos;
	this->myChar = obj.myChar;
	this->items = obj.items;
	return *this;
}

std::shared_ptr<Character> Cell::getCharacter()
{
	return myChar;
}

void Cell::setCharacter(std::shared_ptr<Character> newChar)
{
	myChar = newChar;
}

std::vector<std::shared_ptr<Item>> Cell::getItems()
{
	return items;
}

void Cell::addItem(std::shared_ptr<Item> newItem)
{
	items.push_back(newItem);
}

std::shared_ptr<Item> Cell::removeItem(int index)
{
        int size = (int) items.size();
	if ((index >= 0) && (index < size)) {
		std::shared_ptr<Item> ret = items[index];
		items.erase(items.begin() + index);
		return ret;
	}
	else
		return nullptr;
}

bool Cell::isFree()
{
	//debug
	//std::cout << "celltype: " << celltype << " character: " << (myChar == nullptr) << "value: " << ((celltype != 0) && (myChar == nullptr)) << std::endl;
	return (celltype != 0 && myChar == nullptr);
}

std::pair<int, int> Cell::getPosition()
{
	return std::pair<int, int>(xPos, yPos);
}

void Cell::setType(int type)
{
	celltype = type;
}
