/*
 * Pistol.hpp
 *
 *  Created on: 26.11.2017
 *      Author: Katariina
 */

#ifndef PISTOL_HPP_
#define PISTOL_HPP_

#include "Gun.hpp"
#include "Item.hpp"

class Pistol : public Gun
{
public:
	Pistol() : Gun(1, 10, 1, "Pistol", 3, 90) {}
};

#endif /* PISTOL_HPP_ */
