//This file is made for testing classes Character and Item and their child classes

#include "Character.hpp"
#include "Soldier.hpp"
#include "Boss.hpp"
#include "Scout.hpp"

#include "Item.hpp"
#include "Gun.hpp"
#include "Pistol.hpp"
#include "MachineGun.hpp"
#include "Riffle.hpp"
#include "Food.hpp"
#include "Ammunition.hpp"

#include <vector>
#include <string>
#include <memory>
#include <iostream>

/*
 * Fuctions to be tested (succesfull tests marked with x):
 *
 * Character:
 * Copy constructor x
 * getHP		x
 * getItem		x
 * getSpeed		x
 * getActiveGun x
 * getName		x
 * addItem		x
 * dropItem	    x
 * changeHP		x
 * aciveGun		x
 * changeActiveGun x
 * operator=	x
 *
 * Item:
 * Copy constructor x
 * getWeight	x
 * getName		x
 * operator=	x
 *
 * Food:
 * getHealth	x
 * eat			x
 *
 * Gun:
 * getDamage	x
 * getAmmo		x
 * getProbability x
 * getRange		x
 * getMaxAmmo	x
 * increaseAmmo x
 * shoot		x
 * resetAmmo	x
 *
 * Ammunition:
 * getAmount	x
 * take			x
 */

int main() {

	//Test Character constructor

	std::cout << "Creating Characters:" << std::endl;
	std::shared_ptr<Soldier> s = std::make_shared<Soldier>();
	std::shared_ptr<Boss> b = std::make_shared<Boss>();
	std::shared_ptr<Scout> sol = std::make_shared<Scout>();

	std::cout << s->getName() << std::endl << b->getName() << std::endl << sol->getName() << std::endl << std::endl;

	//Test activeGun
	std::cout << "Boss, active gun: " << b->getActiveGun()->getName() << std::endl;
	std::cout << "Damage: " << b->getActiveGun()->getDamage() << std::endl << "Probability to hit: " << b->getActiveGun()->getProbability() << std::endl;
	std::cout << "Range: " << b->getActiveGun()->getRange() << std::endl << "Maximum ammo: " << b->getActiveGun()->getMaxAmmo() << std::endl << std::endl;
	std::cout << "Boss, Items: " << std::endl;
	for (auto i : b->getItem()){
		std::cout << i->getName() << std::endl;
	}
	std::cout << std::endl;

	//Test Item constructor
	std::cout << "Creating Items:" << std::endl;
	std::shared_ptr<Pistol> g = std::make_shared<Pistol>();
	std::shared_ptr<Food> f = std::make_shared<Food>();
	std::shared_ptr<Ammunition> a = std::make_shared<Ammunition>();

	std::shared_ptr<MachineGun> m = std::make_shared<MachineGun>();
	std::cout << g->getName() << std::endl << f->getName() << std::endl << a->getName() << std::endl << std::endl;

	//Test shoot()
	std::cout << g->getName() << " ammo: " << g->getAmmo() << std::endl << "Shooting four times:" << std::endl;
	g->shoot();
	g->shoot();
	g->shoot();
	g->shoot();
	std::cout << g->getName() << " ammo now: " << g->getAmmo() << std::endl << std::endl;

	int shot = m->shoot();
	std::cout << "Bullets shot: " << shot << std::endl;
	shot = m->shoot();
	std::cout << "Bullets shot: " << shot << std::endl;
	shot = m->shoot();
	std::cout << "Bullets shot: " << shot << std::endl;
	shot = m->shoot();
	std::cout << "Bullets shot: " << shot << std::endl;
	shot = m->shoot();
	std::cout << "Bullets shot: " << shot << std::endl << std::endl;
	std::cout << "Increasing ammo: " << std::endl;
	m->increaseAmmo(a->getAmount());
	std::cout << "Ammo now: " << m->getAmmo() << std::endl << std::endl;

	//Test take()
	std::cout << "Taking Ammunition for Pistol." << std::endl;
	std::cout << "Pistol ammo before taking:" << g->getAmmo() << std::endl;
	a->take(g, 10);
	std::cout << "Pistol ammo after taking Ammunition: " << g->getAmmo() << std::endl << std::endl;

	//Test Item get functions
	std::cout << "Food health:" << std::endl << f->getHealth() << std::endl;
	std::cout << "Pistol damage:" << std::endl << g->getDamage() << std::endl << "Ammo:" << std::endl << g->getAmmo() << std::endl;
	std::cout << "Ammunition amount:" << std::endl << a->getAmount() << std::endl << std::endl;

	//Test addItem
	std::cout << "Adding items Pistol, Food and Ammunition to Scout:" << std::endl;
	sol->addItem(g);
	sol->addItem(f);
	sol->addItem(a);

	//Test getItem and getWeight
	std::cout << "Items Scout has now:" << std::endl;
	for (auto i : sol->getItem()){
		std::cout << i->getName() << ", weight: " << i->getWeight() << std::endl;
	}

	//Test getHP and getSpeed and getActiveGun
	std::cout << std::endl << "Scout  HP: " << sol->getHP() << std::endl << "Scout speed: " << sol->getSpeed() << std::endl << "Scout active gun: " << sol->getActiveGun()->getName() << std::endl << std::endl;

	//Test dropItem
	sol->dropItem(a);
	sol->dropItem(g);
	std::cout << "Dropped items Ammunition and Pistol:" << std::endl;
	std::cout << "Items Scout has now: " << std::endl;
	for (auto i : sol->getItem()){
		std::cout << i->getName() << std::endl;
	}
	std::cout << std::endl;

	//Test changeHP
	std::cout << "Scout HP: " << sol->getHP() << std::endl;
	std::cout << "Decreasing HP by 100:" << std::endl;
	sol->changeHP(-100);
	std::cout << "Scout HP now: " << sol->getHP() << std::endl;
	std::cout << "Increasing HP by 15:" << std::endl;
	sol->changeHP(15);
	std::cout << "Scout HP now: " << sol->getHP() << " (maximum HP 10)" << std::endl;
	std::cout << "Decreasing HP by 6:" << std::endl;
	sol->changeHP(-6);
	std::cout << "Scout HP now: " << sol->getHP() << std::endl;
	std::cout << "Increasing HP by 2:" << std::endl;
	sol->changeHP(2);
	std::cout << "Scout HP now: " << sol->getHP() << std::endl << std::endl;

	//Test eat
	std::cout << "Scout eats some Food:" << std::endl;
	for (auto i:sol->getItem()){
		if (i->getName() == "Food"){
			f->eat(sol);
		}
	}
	std::cout << "Scout HP now: " << sol->getHP() << std::endl;
	std::cout << "Items Scout has now:" << std::endl;
	for (auto i : sol->getItem())
	{
		std::cout << i->getName() << std::endl;
	}

	//test changeActiveGun
	std::cout << std::endl << "Boss has active gun: " << b->getActiveGun()->getName() << std::endl << "Changing active gun..." << std::endl;
	b->changeActiveGun(g);
	std::cout << "Boss has now active gun: " << b->getActiveGun()->getName() << std::endl;

	//test resetAmmo
	std::cout << std::endl << "Pistol ammo now: " << g->getAmmo() << std::endl;
	std::cout << "Resetting ammo..." << std::endl;
	g->resetAmmo();
	std::cout << "Pistol has now ammo: " << g->getAmmo() << std::endl << std::endl;

	//test assignment operator=
	std::shared_ptr<Soldier> ss = std::make_shared<Soldier>();
	ss->addItem(f);
	std::cout << "Soldier s has before assignment items: " << std::endl;
	for (auto i : s->getItem()){
		std::cout << i->getName() << std::endl;
	}
	std::cout << "Created another Soldier ss and added one food to items list." << std::endl;
	std::cout << "Assigning Soldier s to Soldier ss..." << std::endl;
	std::cout << "Soldier s has now items: " << std::endl;
	s = ss;
	for (auto i : s->getItem()){
		std::cout << i->getName() << std::endl;
	}
	std::cout << std::endl;

	std::shared_ptr<Item> ff = std::make_shared<Food>();
	std::shared_ptr<Item> gg = std::make_shared<Pistol>();
	std::cout << "Item gg has before assignment name: " << g->getName() << std::endl;
	std::cout << "Created another Item ff." << std::endl;
	std::cout << "Assigning g to ff..." << std::endl;
	std::cout << "Item gg has now name: ";
	gg = ff;
	std::cout << gg->getName() << std::endl;

	return 0;
}
