#include "Game.hpp"
#include "Ammunition.hpp"
#include "Gun.hpp"
#include "Food.hpp"
#include <iostream>


void Game::initGame(std::vector<std::string> level, std::pair<std::string, std::string> playerNames)
{
	std::pair<std::vector<std::shared_ptr<Character>>, std::vector<std::shared_ptr<Character>>> teams = map.constructMap(level);
	Player player1(teams.first, playerNames.first, 0);
	Player player2(teams.second, playerNames.second, 1);
	turnPlayer = player1;
	waitingPlayer = player2;
	gui.initWindow(level);
	audioPlayer.playMusic("ingame");
}


void Game::playGame()
{
	sf::Event event;
	unsigned int chosenCharItem = 0;
	unsigned int chosenCellItem = 0;
	std::pair<std::vector<CharacterWithPos>, std::vector<CharacterWithPos>> visibleUnits;
	std::map<std::pair<int,int>, std::vector<std::pair<int,int>>> movableCellPaths;
	std::vector<std::pair<int,int>> movableCells;
	std::vector<std::pair<int, int>> targetableCells;

	Cell* currentCell = newTurn(chosenCharItem, chosenCellItem, true);
	updateMapData(targetableCells, visibleUnits, movableCellPaths, movableCells, currentCell);


	while (gui.isOpen()) {
		std::vector<std::pair<int,int>> items = map.getItems(); // This should be called after only certain actions

		while (gui.getEvent(event)) {

			switch (event.type) {

			// window closed
			case sf::Event::Closed:
				gui.closeWindow();
				break;

			// check which key was pressed and perform respective actions
			case sf::Event::KeyPressed:
				// Drop item
				if (event.key.code == sf::Keyboard::Key::D) {
					dropItem(currentCell, chosenCharItem);
				}
				// Pick up item
				else if (event.key.code == sf::Keyboard::Key::P) {
					pickUpItem(currentCell, chosenCellItem);
				}
				// Use item (change gun, use ammo, eat food)
				else if (event.key.code == sf::Keyboard::Key::U) {
					useItem(currentCell, chosenCharItem);
				}
				// Select next character item
				else if (event.key.code == sf::Keyboard::Key::X) {
					if (chosenCharItem < currentCell->getCharacter()->getItem().size() - 1) {
						++chosenCharItem;
						audioPlayer.playSound("button");
					} //check if the character has more items
				}
				else if (event.key.code == sf::Keyboard::Key::Z) {
					if (chosenCharItem > 0) {
						--chosenCharItem;
						audioPlayer.playSound("button");
					}
				}
				// Select next cell item
				else if (event.key.code == sf::Keyboard::Key::V) {
					if (chosenCellItem < currentCell->getItems().size() - 1) {
						++chosenCellItem;
						audioPlayer.playSound("button");
					} //check if the character has more items
				}
				// Select previous cell item
				else if (event.key.code == sf::Keyboard::Key::C) {
					if (chosenCellItem > 0) {
						--chosenCellItem;
						audioPlayer.playSound("button");
					}
				}
				// Open info screen
				else if (event.key.code == sf::Keyboard::Key::I) {
					renderInfo();
				}
				// Change character
				else if (event.key.code == sf::Keyboard::Key::Return) {
					currentCell = newTurn(chosenCharItem, chosenCellItem);
					updateMapData(targetableCells, visibleUnits, movableCellPaths, movableCells, currentCell);
				}

				else {
					gui.moveView(event.key.code);
				}

				break;

			// check where the mouse was clicked and perform respective actions
			case sf::Event::MouseButtonReleased:
			{
				std::pair<int,int> click = gui.getMapCoordinates(event.mouseButton);

					// Change active character or move
					if (event.mouseButton.button == sf::Mouse::Left) {
                                                Cell* retPtr = parseLeftClick(click, movableCellPaths, movableCells, currentCell, chosenCellItem, chosenCharItem);
						if (retPtr != nullptr) {
							currentCell = retPtr;
							updateMapData(targetableCells, visibleUnits, movableCellPaths, movableCells, currentCell);
						}
					}

					// Attack if possible and render it
					else if (event.mouseButton.button == sf::Mouse::Right) { // this needs to parse false clicks
						HitInfo hitInfo = attackChar(currentCell, click, visibleUnits.second);
						renderAttack(targetableCells, hitInfo, currentCell, click, movableCells, visibleUnits, items, chosenCharItem, chosenCellItem);
						parseAttackOutcome(hitInfo, click, currentCell);
						updateMapData(targetableCells, visibleUnits, movableCellPaths, movableCells, currentCell);
					}
				break;
			}
			// other kinds of events are left unprocessed
			default:
				break;
			}
		}
		
		gui.renderGameWindow(targetableCells, turnPlayer, movableCells, visibleUnits, items, currentCell, chosenCharItem, chosenCellItem);
		gui.displayGameWindow();

		if (turnPlayer.getActionPoints() == 0) {
			currentCell = newTurn(chosenCharItem, chosenCellItem);
			if (currentCell == nullptr)
				break;
			updateMapData(targetableCells, visibleUnits, movableCellPaths, movableCells, currentCell);
		}
	}
}

void Game::renderInfo() {
	sf::Event event;
	while (1) {
		sf::sleep(sf::microseconds(100));
		gui.renderInfoWindow();
		gui.getEvent(event);
		if (event.type == sf::Event::KeyPressed && event.key.code == sf::Keyboard::Key::I)
			break;
	}
}

void Game::renderEnd(std::string winner) {
	sf::Event event;
	audioPlayer.playMusic("dance");
	gui.displayGameEnded(winner);

	while (1) {
		sf::sleep(sf::microseconds(100));
		gui.getEvent(event);
		if (event.type == sf::Event::Closed) {
			gui.closeWindow();
			break;
		}
	}
}

Cell* Game::parseLeftClick(std::pair<int,int> click, std::map<std::pair<int,int>, std::vector<std::pair<int,int>>>& movableCellPaths, std::vector<std::pair<int,int>>& movableCells, Cell* currentCell, unsigned int& chosenCellItem, unsigned int& chosenCharItem) {
	if (click.first == -1)  // Parse click only if it was in the map view's region
		return nullptr;

	Cell* clickedCell = map.getCell(click.first, click.second);
	std::shared_ptr<Character> clickedChar = clickedCell->getCharacter();
	std::shared_ptr<Character> activeChar = currentCell->getCharacter();
	const std::vector<std::shared_ptr<Character>> turnCharacters = turnPlayer.getCharacters();

	if (std::find(movableCells.begin(), movableCells.end(), click) != movableCells.end()) {

		// Check that the cell to be moved to isn't occupied by a currently invisible enemy
		std::vector<std::pair<int,int>> truncatedPath = map.checkPath(movableCellPaths.at(click));
		std::pair<int,int> truncatedCell = truncatedPath.back();
		clickedCell = map.getCell(truncatedCell.first, truncatedCell.second);

		map.move(currentCell, clickedCell);
		gui.renderMove(truncatedPath, currentCell);

		int pathLen = truncatedPath.size();
		int speed = activeChar->getSpeed();
		activeChar->decreaseActionPoints(pathLen/speed + 1);

		return clickedCell;
	}

	else if (clickedChar != nullptr && std::find(turnCharacters.begin(), turnCharacters.end(), clickedChar) != turnCharacters.end()) {
		chosenCharItem = 0;
		chosenCellItem = 0;
		return clickedCell;
	}

	return nullptr;
}


void Game::parseAttackOutcome(HitInfo hitInfo, std::pair<int,int> click, Cell* currentCell) {
	int usedActionPoints = (int) hitInfo.hits.size();
	currentCell->getCharacter()->decreaseActionPoints(usedActionPoints);

	if (!hitInfo.isDead)
		return;
	audioPlayer.playSound("death");
	Cell* clickedCell = map.getCell(click.first, click.second);
	std::vector<std::shared_ptr<Item>> droppedItems = hitInfo.ptr->getItem();
	std::shared_ptr<Item> activeGun = hitInfo.ptr->getActiveGun();

	clickedCell->addItem(activeGun);

	for (auto it = droppedItems.begin(); it != droppedItems.end(); ++it)
		clickedCell->addItem(*it);

	waitingPlayer.removeCharacter(hitInfo.ptr);

	if (hitInfo.ptr->getName() == "Boss") {
		renderEnd(turnPlayer.getName());
	}
}

void Game::renderAttack(std::vector<std::pair<int, int>>& targetableCells, HitInfo hitInfo, Cell* currentCell, std::pair<int,int> attackDestination, const std::vector<std::pair<int,int>> movableCells, std::pair<std::vector<CharacterWithPos>, std::vector<CharacterWithPos>>& visibleUnits, const std::vector<std::pair<int,int>> items, unsigned int chosenCharItem, unsigned int chosenCellItem) {
	int hitNo = hitInfo.hits.size();
	int hitDamage;
	std::vector<int> hits = hitInfo.hits;
	std::pair<int,int> attackOrigin = currentCell->getPosition();

	if (hitInfo.hits.size() == 0)
		return;

	for (int i = 0; i < 2*hitNo; ++i) {

		if (i % 2 == 0) {
			hitDamage = hits.at(i / 2);
			gui.renderAttack(hitDamage, attackOrigin, attackDestination);
			audioPlayer.playSound("shoot");
		}
		else
			gui.renderGameWindow(targetableCells, turnPlayer, movableCells, visibleUnits, items, currentCell, chosenCharItem, chosenCellItem);

		gui.displayGameWindow();
		sf::sleep(sf::milliseconds(100));
	}
}

HitInfo Game::attackChar(Cell* currentCell, std::pair<int,int> click, std::vector<std::pair<std::shared_ptr<Character>, std::pair<int,int>>> allVisibleEnemies) {
	HitInfo hitInfo;

	if (click.first == -1)  // Parse click only if it was in the map view's region
		return hitInfo;

	Cell* clickedCell = map.getCell(click.first, click.second);
	std::shared_ptr<Character> clickedChar = clickedCell->getCharacter();
	const std::vector<std::shared_ptr<Character>> enemyCharacters = waitingPlayer.getCharacters();

	if (std::find(enemyCharacters.begin(), enemyCharacters.end(), clickedChar) != enemyCharacters.end()) {
		std::vector<std::pair<int,int>> currentVisibleEnemies = map.getTargetableCells(currentCell, allVisibleEnemies);

		if (std::find(currentVisibleEnemies.begin(), currentVisibleEnemies.end(), click) != currentVisibleEnemies.end()) {
			int actionPoints = currentCell->getCharacter()->getActionPoints();
			if (actionPoints >= currentCell->getCharacter()->getActiveGun()->getbulletsPerGunShot())
				hitInfo = map.attack(currentCell, clickedCell);
			return hitInfo;
		}
	}
	return hitInfo;
}

Cell* Game::newTurn(unsigned int& chosenCharItem, unsigned int& chosenCellItem, bool firstRound) {
	Player temp = turnPlayer;
	turnPlayer = waitingPlayer;
	waitingPlayer = temp;
	chosenCharItem = 0;
	chosenCellItem = 0;

	if (!firstRound)
		waitingPlayer.newRound(maxActionPoints, newActionPoints);

	std::vector<CharacterWithPos> team = map.getTeamUnits(turnPlayer.getCharacters());

	if (team.empty())
		return nullptr;

	Cell* currentCell = map.getCell(team.at(0).second.first, team.at(0).second.second);
	gui.changeView(currentCell);
	return currentCell;
}

void Game::updateMapData(std::vector<std::pair<int, int>>& targetableCells, std::pair<std::vector<CharacterWithPos>, std::vector<CharacterWithPos>>& visibleUnits, std::map<std::pair<int,int>, std::vector<std::pair<int,int>>>& movableCellPaths, std::vector<std::pair<int,int>>& movableCells, Cell* currentCell) {
	visibleUnits = map.getVisibleUnits(turnPlayer.getCharacters(), waitingPlayer.getCharacters());
	movableCellPaths = map.getMovableCells(currentCell, visibleUnits);
	movableCells.clear();
	for (auto const& it : movableCellPaths)
		movableCells.push_back(it.first);

	targetableCells = map.getTargetableCells(currentCell, visibleUnits.second);
}

void Game::pickUpItem(Cell* currentCell, unsigned int& chosenCellItem) {
	std::shared_ptr<Item> pickedItem = currentCell->removeItem(chosenCellItem);
	if (pickedItem != nullptr) {
		audioPlayer.playSound("button");
		currentCell->getCharacter()->addItem(pickedItem);
		if (chosenCellItem > 0) --chosenCellItem;
	}
}

void Game::dropItem(Cell* currentCell, unsigned int& chosenCharItem) {
	std::shared_ptr<Character> activeChar = currentCell->getCharacter();
	std::vector<std::shared_ptr<Item>> allItems = activeChar->getItem();

	if (!allItems.empty()) {
		audioPlayer.playSound("button");
		std::shared_ptr<Item> droppedItem = allItems.at(chosenCharItem);
		activeChar->dropItem(droppedItem);
		currentCell->addItem(droppedItem);
		if (chosenCharItem > 0) --chosenCharItem;
	}
}

void Game::useItem(Cell* currentCell, unsigned int& chosenCharItem) {
	std::shared_ptr<Character> activeChar = currentCell->getCharacter();
	std::vector<std::shared_ptr<Item>> allItems = activeChar->getItem();

	if (!allItems.empty()) {
		std::shared_ptr<Item> chosenItem = allItems.at(chosenCharItem);
		std::string itemType = chosenItem->getName();
		audioPlayer.playSound("button");
		if (itemType == "Ammunition") {
			std::shared_ptr<Ammunition> ammoItem = std::dynamic_pointer_cast<Ammunition>(chosenItem);

			if (!ammoItem) // If dynamic casting fails for some reason, do nothing
				return;

			std::shared_ptr<Gun> activeGun = activeChar->getActiveGun();
                        ammoItem->take(activeGun);
			activeChar->dropItem(chosenItem);
			if (chosenCharItem > 0) chosenCharItem--;
		}

		else if (itemType == "Food") {
			std::shared_ptr<Food> foodItem = std::dynamic_pointer_cast<Food>(chosenItem);

			if (!foodItem) // If dynamic casting fails for some reason, do nothing
				return;

			foodItem->eat(activeChar);
			if (chosenCharItem > 0) chosenCharItem--;
		}

		else if (itemType == "Pistol" || itemType == "MachineGun" || itemType == "Rifle") {
			std::shared_ptr<Gun> chosenGunItem = std::dynamic_pointer_cast<Gun>(chosenItem);
			std::shared_ptr<Item> oldGun = activeChar->getActiveGun();
			std::shared_ptr<Gun> oldGunCasted = std::dynamic_pointer_cast<Gun>(oldGun);

			if (!chosenGunItem || !oldGunCasted) // If dynamic casting fails for some reason, do nothing
				return;

			int oldAmmo = oldGunCasted->getAmmo();
			oldGunCasted->resetAmmo();
			chosenGunItem->increaseAmmo(oldAmmo);

			activeChar->changeActiveGun(chosenGunItem);
			activeChar->dropItem(chosenItem);
			activeChar->addItem(oldGun);
			if (chosenCharItem > 0) chosenCharItem--;
		}

	}
}
