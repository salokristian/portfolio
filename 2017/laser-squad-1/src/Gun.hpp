#ifndef GUN_HPP_
#define GUN_HPP_

#include "Item.hpp"
#include "Gun.hpp"

#include <string>


//Gun has weight that is common for all the items.
//In addition Gun has private variable damage that decreases HP when shooting another character.
//Another private variable is ammo that defines how many times Character can shoot with a Gun.
class Gun : public Item {
public:
	Gun(int d, int a, int b, const std::string& n, int r, int prob) : Item(3, n), damage(d), ammo(a), maxAmmo(a), probability(prob), bulletsPerGunshot(b), range(r) {} //values might change!
	int getDamage() const;
	int getAmmo() const;
	int getProbability() const;
	int getRange() const;
	int getMaxAmmo() const;
	int getbulletsPerGunShot() const;
	//increases ammo with amount
	void increaseAmmo(int amount);
	//Returns number of bullets shot, decreases ammo with bulletsPerGunshot.
	int shoot();
	//resers Gun's ammo to maxAmmo
	void resetAmmo();
private:
	int damage; //how much hp opponent will lose if shooting it
	int ammo; //how many times player can shoot. If ammo = 0, gun is empty.
	int maxAmmo; //maximum ammo
	int probability; //probability that character hits the opponent when shooting with the gun.
	int bulletsPerGunshot; //how many bullets are shot in one gunshot.
	int range; //how far Gun can shoot
};

#endif /* GUN_HPP_ */

