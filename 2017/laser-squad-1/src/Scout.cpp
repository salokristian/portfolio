#include "Scout.hpp"
#include "Character.hpp"

#include <cmath>
#include <iostream>


void Scout::changeHP(int change)
{
	
		if (change < 0 && (abs(change) > hp))
		{
			hp = 0;
		}
		else if (change < 0 && (abs(change) <= hp))
		{
			hp -= abs(change);
		}
		else if (change > 0 && (hp+change > maxhp))
		{
			hp = maxhp;
		}
		else
		{
			hp += change;
		}
}

int Scout::getMaxHp()
{
	return maxhp;
}
