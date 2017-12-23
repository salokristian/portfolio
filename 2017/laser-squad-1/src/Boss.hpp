#ifndef BOSS_HPP_
#define BOSS_HPP_

#include "Character.hpp"
#include "Item.hpp"
#include "MachineGun.hpp"
#include <memory>


//Boss has x amount of hp at the beginning and speed that is always y.
class Boss : public Character {
public:
	Boss() : Character(40, 1, "Boss") {} //Values might change!

	virtual void changeHP(int change);


	virtual int getMaxHp();

private:
	int maxhp = 40;
};

#endif /* BOSS_HPP_ */
