#include "Item.hpp"

#include <memory>

std::shared_ptr<Item> Item::operator= (const std::shared_ptr<Item> i)
{
	return i;
}

int Item::getWeight () const
{
	return weight;
}

std::string& Item::getName()
{
	return name;
}
