#ifndef CHARACTER_HPP_
#define CHARACTER_HPP_


#include "Item.hpp"
#include "Gun.hpp"

#include <vector>
#include <memory>
#include <string>

class Character {

public:
	Character() {}
	Character(int h, int s, const std::string& n);
	virtual ~Character() {}

	std::shared_ptr<Character> operator= (const std::shared_ptr<Character> ch);

	int getHP() const;
	std::vector<std::shared_ptr<Item>> getItem();
	int getSpeed() const;
	std::shared_ptr<Gun> getActiveGun();
	std::string& getName();

	//Adds picked Item to items vector
	void addItem(std::shared_ptr<Item> item);
	//Removes item from items vector
	virtual void dropItem(std::shared_ptr<Item> item);
	//Pure virtual function. Increases or decreases hp with amount of change. Minimum value of hp is zero.
	//Maximum value depends on character type (Boss, Soldier or Scout). If hp = 0 character dies.
	virtual void changeHP(int change) = 0; //depends on the character type (Boss, Soldier or Scout)

	//pure virtual function that gets max hp from child class
	virtual int getMaxHp() = 0;



	/*
	Set the player's action points for a new round.
	The action point count will be increased by newActionPoints, but no higher than maxActionPoints.
	Moved from Player class here
	*/
	void newRound(int maxActionPoints, int newActionPoints);
	//Moved from Player class here
	int getActionPoints() const;
	// Decreases the player's action points by decreasePoints (default 1), but no lower than 0.
	//Moved from Player class here
	void decreaseActionPoints(int decreasePoints = 1);
	void changeActiveGun(std::shared_ptr<Gun> gun);

protected:
	int hp;
	std::vector<std::shared_ptr<Item>> items;
	int speed;
	std::shared_ptr<Gun> activeGun;
	int actionPoints;
	std::string name;
};

#endif /* CHARACTER_HPP_ */

