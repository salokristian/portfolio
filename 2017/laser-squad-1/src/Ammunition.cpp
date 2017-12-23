/*
 * Ammunition.cpp
 *
 *  Created on: 16.11.2017
 *      Author: Katariina
 */

#include "Ammunition.hpp"
#include "Gun.hpp"
#include <memory>

int Ammunition::getAmount() const
{
	return amount;
}

void Ammunition::take(std::shared_ptr<Gun> gun)
{
	//calls Gun's member function increaseAmmo
	gun->increaseAmmo(amount);
}


