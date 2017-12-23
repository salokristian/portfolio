#ifndef SCOUT_HPP_
#define SCOUT_HPP_

#include "Character.hpp"
#include "Item.hpp"
#include "Pistol.hpp"
#include <memory>

//Scout has x amount of hp at the beginning and speed that is always y.
class Scout : public Character {
public:
	Scout() : Character(10, 3, "Scout") {} //Values might change!

	virtual void changeHP(int change);

	virtual int getMaxHp();

private:
	int maxhp = 10;
};

#endif /* SCOUT_HPP_ */
