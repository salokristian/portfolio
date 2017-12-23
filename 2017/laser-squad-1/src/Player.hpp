#ifndef PLAYER_H
#define PLAYER_H

#include "Character.hpp"
#include<vector>
#include<string>

class Player {
public:
	Player() {}

	Player(std::vector<std::shared_ptr<Character>> playerCharacters, std::string playerName, int ID) : characters(playerCharacters), name(playerName), teamID(ID) {};

	Player(const Player& obj);

	Player& operator=(const Player &obj);

	~Player() {}

	const std::string getName() const;

	const std::vector<std::shared_ptr<Character>> getCharacters() const;

	int getActionPoints() const;

	int getCharCount() const;

	void newRound(int maxActionPoints, int newActionPoints);

	int getID() const;

	void removeCharacter(std::shared_ptr<Character> removableChar);

private:
	std::vector<std::shared_ptr<Character>> characters;
	std::string name;
	int teamID;
};

#endif 
