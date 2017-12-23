#include "Soldier.hpp"
#include "Character.hpp"

#include <cmath>


void Soldier::changeHP(int change)
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

int Soldier::getMaxHp()
{
	return maxhp;
}

