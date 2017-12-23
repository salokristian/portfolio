#ifndef AMMUNITION_HPP_
#define AMMUNITION_HPP_

#include "Item.hpp"
#include <memory>
#include "Gun.hpp"

#include <string>

//Ammunition has weight that is common for all the items.
//In addition Ammunition has private variable amount that defines how many bullets more Gun will get if Character is picking up Ammunition.
class Ammunition : public Item {
public:
	Ammunition() : Item(1, "Ammunition"), amount(3) {} //Values might change!
	int getAmount() const;
	//When Character takes Ammunition, Gun's ammo is increased with amount.
        void take(std::shared_ptr<Gun> gun);
private:
	int amount;
};

#endif /* AMMUNITION_HPP_ */

