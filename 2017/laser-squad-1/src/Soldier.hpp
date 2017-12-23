#ifndef SOLDIER_HPP_
#define SOLDIER_HPP_

#include "Character.hpp"
#include "Item.hpp"
#include "Riffle.hpp"
#include <memory>



//Soldier has x amount of hp at the beginning and speed that is always y.
class Soldier : public Character {
public:
	Soldier() : Character(20, 2, "Soldier") {} //Values might change!

	virtual void changeHP(int change);

	virtual int getMaxHp();
private:
	int maxhp = 20;
};



#endif /* SOLDIER_HPP_ */
