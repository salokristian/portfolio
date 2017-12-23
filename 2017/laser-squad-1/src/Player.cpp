#include "Player.hpp"
#include <iostream>


const std::vector<std::shared_ptr<Character>> Player::getCharacters() const {
	return characters;
}

Player::Player(const Player& obj) {
	this->characters = obj.characters;
	this->name = obj.name;
	this->teamID = obj.teamID;
}

Player& Player::operator=(const Player &obj) {
	this->characters = obj.characters;
	this->name = obj.name;
	this->teamID = obj.teamID;
	return *this;
}



const std::string Player::getName() const {
	return name;
}

int Player::getActionPoints() const {
	int actionPoints = 0;
	for (auto const& value : characters) {
		actionPoints += value->getActionPoints();
	}
	return actionPoints;
}

int Player::getCharCount() const {
	return characters.size();
}

void Player::newRound(int maxActionPoints, int newActionPoints) {
	for (auto const& value : characters) {
		value->newRound(maxActionPoints, newActionPoints);
	}
}

int Player::getID() const{
	return teamID;
}

void Player::removeCharacter(std::shared_ptr<Character> removableChar) {
	int i = 0;
	for (auto const& value : characters) {
		if (value == removableChar) {
			characters.erase(characters.begin() + i);
			break;
		}
		i++;
	}
}

