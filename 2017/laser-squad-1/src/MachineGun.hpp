/*
 * MachineGun.hpp
 *
 *  Created on: 26.11.2017
 *      Author: Katariina
 */

#ifndef MACHINEGUN_HPP_
#define MACHINEGUN_HPP_

#include "Gun.hpp"
#include "Item.hpp"

class MachineGun : public Gun
{
public:
	MachineGun() : Gun(5, 20, 5, "MachineGun", 4, 70) {}
};

#endif /* MACHINEGUN_HPP_ */
