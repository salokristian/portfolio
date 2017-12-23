/* Gun.cpp
 *
 *  Created on: 16.11.2017
 *      Author: Katariina
 */

#include "Gun.hpp"
#include <iostream>
#include <cmath>

int Gun::getDamage() const
{
	return damage;
}
int Gun::getAmmo() const
{
	return ammo;
}

int Gun::getProbability() const
{
	return probability;
}

int Gun::getRange() const
{
	return range;
}

int Gun::getMaxAmmo() const
{
 return maxAmmo;
}

void Gun::increaseAmmo(int amount)
{

	if (maxAmmo - ammo >= amount)
	{
		ammo += amount;
	}
	else
	{
		ammo = maxAmmo;
	}
}
int Gun::getbulletsPerGunShot() const {
	return bulletsPerGunshot;
}

int Gun::shoot()
{
	int bulletsShot = ammo;
	if (ammo >= bulletsPerGunshot)
	{
		ammo -= bulletsPerGunshot;
		return bulletsPerGunshot;
	}
	else
	{
		ammo = 0;
	}
	return bulletsShot;
}

void Gun::resetAmmo()
{
	ammo = 0;
}

