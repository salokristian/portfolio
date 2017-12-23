#ifndef GAME_H
#define GAME_H

#include "Player.hpp"
#include "GameAudio.hpp"
#include "GUI.hpp"

#include<vector>



class Game {
public:
	Game() : maxActionPoints(20), newActionPoints(10) {}
	/*
	Initialize the game, i.e. configure the players, creatures, map and GUI. This 
	might be in the form of a graphical menu in the final version,
	but right now it can be hard-coded.
	*/
	void initGame(std::vector<std::string> level, std::pair<std::string, std::string> playerNames);

	/*
	This function contains the loop that runs the actual game. 
	It should be called from the main function after initializing the game. 
	It processes user events and acts accordingly, i.e. moves characters and draws the map. 
	It is exited when the game ends or the game window is closed.
	*/
	void playGame();

	~Game() {}

private:
	Game& operator=(const Game& obj); //A game cannot be copied or assigned, because gui contains non-copyable objects
	Game(const Game& obj);
	void renderEnd(std::string winner);
	void renderInfo();
        Cell* parseLeftClick(std::pair<int,int> click, std::map<std::pair<int,int>, std::vector<std::pair<int,int>>>& movableCellPaths, std::vector<std::pair<int,int>>& movableCells, Cell* currentCell, unsigned int& chosenCellItem, unsigned int& chosenCharItem);
	void parseAttackOutcome(HitInfo hitInfo, std::pair<int,int> click, Cell* currentCell);
	void renderAttack(std::vector<std::pair<int, int>>& targetableCells, HitInfo hitInfo, Cell* currentCell, std::pair<int,int> attackDestination, const std::vector<std::pair<int,int>> movableCells, std::pair<std::vector<CharacterWithPos>, std::vector<CharacterWithPos>>& visibleUnits, const std::vector<std::pair<int,int>> items, unsigned int chosenCharItem, unsigned int chosenCellItem);
	HitInfo attackChar(Cell* currentCell, std::pair<int,int> click, std::vector<std::pair<std::shared_ptr<Character>, std::pair<int,int>>> visibleEnemies);
	void dropItem(Cell* currentCell, unsigned int& chosenCharItem);
	void pickUpItem(Cell* currentCell, unsigned int& chosenCellItem);
	void useItem(Cell* currentCell, unsigned int& chosenCharItem);
	Cell* newTurn(unsigned int& chosenCharItem, unsigned int& chosenCellItembool, bool firstRound = false);
	void updateMapData(std::vector<std::pair<int, int>>& targetableCells, std::pair<std::vector<CharacterWithPos>, std::vector<CharacterWithPos>>& visibleUnits, std::map<std::pair<int,int>, std::vector<std::pair<int,int>>>& movableCellPaths, std::vector<std::pair<int,int>>& movableCells, Cell* currentCell);
	Player turnPlayer;
	Player waitingPlayer;
	GUI gui;
	Map map;
	int maxActionPoints;
	int newActionPoints;
	GameAudio audioPlayer;
};


#endif 
