#include "Food.hpp"
#include "Character.hpp"
#include "Scout.hpp"
#include "Boss.hpp"
#include "Soldier.hpp"

#include <iostream>
#include <memory>
#include <vector>

int Food::getHealth() const
{
	return health;
}

void Food::eat(std::shared_ptr<Character> ch)
{
	int h = health;
	std::vector<std::shared_ptr<Item>> ite = ch->getItem();
	int eaten = 0;

	for (auto i = ite.begin(); i != ite.end();i++)
	{
		if ((*i)->getName() == "Food" && eaten == 0){
			ch->changeHP(h);
			ch->dropItem(*i);
			eaten++;
		}
	}
}

