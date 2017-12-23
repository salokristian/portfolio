#include "Character.hpp"
#include "Item.hpp"
#include "Gun.hpp"
#include "Pistol.hpp"
#include "MachineGun.hpp"
#include "Riffle.hpp"
#include "Food.hpp"
#include "Ammunition.hpp"

#include <vector>
#include <memory>
#include <iostream>
#include <algorithm>

//Character class implementation

Character::Character(int h, int s, const std::string& n) : hp(h), speed(s), actionPoints(10), name(n)
{
	if (hp == 10)
	{
		activeGun = std::make_shared<Pistol>();
	}
	else if (hp == 40)
	{
		activeGun = std::make_shared<MachineGun>();
	}
	else
	{
		activeGun = std::make_shared<Riffle>();
	}
	std::shared_ptr<Food> food = std::make_shared<Food>();
	std::shared_ptr<Ammunition> ammo = std::make_shared<Ammunition>();
	items.push_back(food);
	items.push_back(ammo);
}

std::shared_ptr<Character> Character::operator= (const std::shared_ptr<Character> ch)
{
	return ch;
}

int Character::getHP() const
{
	return hp;
}

std::vector<std::shared_ptr<Item>> Character::getItem()
{
	return items;
}

int Character::getSpeed() const
{
	return speed;
}

std::shared_ptr<Gun> Character::getActiveGun()
{
	return activeGun;
}

void Character::addItem(std::shared_ptr<Item> item)
{
	items.push_back(item);
}

void Character::dropItem(std::shared_ptr<Item> item)
{
	int deleted = 0;

	for (auto i = items.begin(); i != items.end();){
		if(((*i)->getName() == item->getName()) && (deleted == 0)){
			i = items.erase(i);
			deleted++;
		}
		else{
			i++;
		}
	}
}

void Character::newRound(int maxActionPoints, int newActionPoints)
{
	actionPoints += newActionPoints;
	if (actionPoints > maxActionPoints)
		actionPoints = maxActionPoints;
}

int Character::getActionPoints() const
{
	return actionPoints;
}

std::string& Character::getName()
{
	return name;
}

void Character::decreaseActionPoints(int decreasePoints)
{
	if (decreasePoints == 1 && actionPoints > 0)
		actionPoints--;
	else if (actionPoints > decreasePoints)
		actionPoints -= decreasePoints;
	else
		actionPoints = 0;
}

void Character::changeActiveGun(std::shared_ptr<Gun> gun)
{
	activeGun = gun;
}
