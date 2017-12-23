/*
 * Riffle.hpp
 *
 *  Created on: 26.11.2017
 *      Author: Katariina
 */

#ifndef RIFFLE_HPP_
#define RIFFLE_HPP_


#include "Gun.hpp"
#include "Item.hpp"

class Riffle : public Gun
{
public:
	Riffle() : Gun(3, 10, 2, "Rifle", 5, 80) {}
};


#endif /* RIFFLE_HPP_ */
