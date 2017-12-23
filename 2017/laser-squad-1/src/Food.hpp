#ifndef FOOD_HPP_
#define FOOD_HPP_

#include "Item.hpp"
#include "Character.hpp"

#include <string>
#include <memory>

//Ammunition has weight that is common for all the items.
//In addition Ammunition has private variable amount that defines how many bullets more Gun will get if Character is picking up Ammunition.
class Food : public Item {
public:
	Food() : Item(1, "Food"), health(3) {} //values might change!
	int getHealth() const;
	//When Character eats Food, Characters hp is increased with health and food is removed from Character's items list.
	void eat(std::shared_ptr<Character> ch);
private:
	int health;
};

#endif /* FOOD_HPP_ */


