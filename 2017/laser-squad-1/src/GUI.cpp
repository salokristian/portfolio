#include "GUI.hpp"
#include <iostream>

void GUI::initWindow(std::vector<std::string> level)
{
	window.create(sf::VideoMode(width, height), "Laser Squad", sf::Style::Titlebar | sf::Style::Close); //A 800x600 window without resizing
	window.setFramerateLimit(60);

	mapView.reset(sf::FloatRect(0, 0, tileSize * 25, tileSize * 25));
	mapView.setViewport(sf::FloatRect(0, 0, 0.75f, 1.0f));

	if (!sidebar.initializeFonts(sf::FloatRect(605, 0, 800, 600)))
		throw std::runtime_error("Cannot load font");

	yTileCount = (int) level.at(0).size();
	xTileCount = (int) level.size();

	if (!tilemap.load("includes/tilesheet_plain.png", sf::Vector2u(tileSize, tileSize), level, xTileCount, yTileCount))
		throw std::runtime_error("Cannot load tilesheet picture");
		// This error needs to be handled, apparently this fails sometimes for no obvious reason
}

void GUI::renderGameWindow(const std::vector<std::pair<int, int>>& targetableCells, const Player &turn, const std::vector<std::pair<int,int>> movableCells, std::pair<std::vector<CharacterWithPos>, std::vector<CharacterWithPos>> visibleUnits, const std::vector<std::pair<int,int>> items, Cell *currentCell, int chosenCharItem, int choseCellItem)
{
	// Make sure that player 0's units are always the first element in visibleUnits to draw them with correct colour
	flipVisibleUnits(turn, visibleUnits);

	window.clear();
	
	//Draw the map
	window.setView(mapView);
	tilemap.update(targetableCells, sf::Vector2u(tileSize, tileSize), movableCells, visibleUnits, items, currentCell);
	window.draw(tilemap);

	//Draw the sidebar
	window.setView(window.getDefaultView());
	std::vector<sf::Text> texts = sidebar.renderSidebar(turn, currentCell, chosenCharItem, choseCellItem);
	for (auto it = texts.begin(); it != texts.end(); ++it)
		window.draw(*it);
}

void GUI::displayGameWindow() {
	window.display();
}

void GUI::changeView(Cell* currentCell) {
	std::pair<int,int> CellCoords = currentCell->getPosition();
	mapView.setCenter(sf::Vector2f(CellCoords.first*tileSize, CellCoords.second*tileSize));
}


void GUI::renderInfoWindow()
{
	window.setView(window.getDefaultView());
	sf::RectangleShape rect(sf::Vector2f(width,height));
	rect.setPosition(0, 0);
	rect.setFillColor(sf::Color::Black);

    std::vector<sf::Text> infoTexts = sidebar.renderInfoText();
    infoTexts.at(0).setPosition(40,55);
    infoTexts.at(1).setPosition(550,55);

	window.draw(rect);
    window.draw(infoTexts.at(0));
    window.draw(infoTexts.at(1));
	window.display();
}

bool GUI::getEvent(sf::Event & event)
{
	return window.pollEvent(event);
}

bool GUI::isOpen()
{
	return window.isOpen();
}

void GUI::closeWindow()
{
	window.close();
}

void GUI::moveView(sf::Keyboard::Key key)
{
	switch (key) {

	case sf::Keyboard::Up:
		if (sf::Keyboard::isKeyPressed(sf::Keyboard::Right)){
			mapView.move(20, -20);
		}
		else if(sf::Keyboard::isKeyPressed(sf::Keyboard::Left)){
			mapView.move(-20, -20);
		}
		else{
			mapView.move(0, -20);
		}
		break;

	case sf::Keyboard::Down:
		if (sf::Keyboard::isKeyPressed(sf::Keyboard::Left)){
			mapView.move(-20, 20);
		}
		else if(sf::Keyboard::isKeyPressed(sf::Keyboard::Right)){
			mapView.move(20, 20);
		}
		else{
			mapView.move(0, 20);
		}
		break;

	case sf::Keyboard::Left:
		if (sf::Keyboard::isKeyPressed(sf::Keyboard::Down)){
			mapView.move(-20, 20);
		}
		else if(sf::Keyboard::isKeyPressed(sf::Keyboard::Up)){
			mapView.move(-20, -20);
		}
		else{
			mapView.move(-20, 0);
		}
		break;

	case sf::Keyboard::Right:
		if (sf::Keyboard::isKeyPressed(sf::Keyboard::Up)){
			mapView.move(20, -20);
		}
		else if(sf::Keyboard::isKeyPressed(sf::Keyboard::Down)){
			mapView.move(20, 20);
		}
		else{
			mapView.move(20, 0);
		}
		break;

	default:
		break;
	}
}

std::pair<int,int> GUI::getMapCoordinates(sf::Event::MouseButtonEvent event) {
	const sf::FloatRect viewport = mapView.getViewport();

	if (event.x < viewport.left + viewport.width*width) {

		sf::Vector2f mapCoordinates = window.mapPixelToCoords(sf::Vector2i(event.x, event.y),mapView);

		if (mapCoordinates.x > 0 && mapCoordinates.y > 0 && mapCoordinates.x < xTileCount*tileSize && mapCoordinates.y < yTileCount*tileSize) {
			mapCoordinates.x = mapCoordinates.x / tileSize;
			mapCoordinates.y = mapCoordinates.y / tileSize;
			return std::pair<int,int>((int)mapCoordinates.x, (int)mapCoordinates.y);
		}
	}
	return std::pair<int,int>(-1,-1);
}

void GUI::flipVisibleUnits(const Player& turn, std::pair<std::vector<CharacterWithPos>, std::vector<CharacterWithPos>>& visibleUnits) {
	if (turn.getID() == 1) {
		std::vector<std::pair<std::shared_ptr<Character>, std::pair<int,int>>> temp = visibleUnits.first;
		visibleUnits.first = visibleUnits.second;
		visibleUnits.second = temp;
	}
}

void GUI::renderAttack(int hitDamage, std::pair<int,int> attackOrigin, std::pair<int,int> attackDestination) {
	tilemap.drawGunshot(attackOrigin, attackDestination, tileSize);
	sf::Text damageText = sidebar.renderGunDamage(hitDamage, attackOrigin, attackDestination, tileSize);
	std::vector<sf::Text> texts = sidebar.getText();

	window.clear();

	window.setView(mapView);
	window.draw(tilemap);
	window.draw(damageText);

	window.setView(window.getDefaultView());
	for (auto it = texts.begin(); it != texts.end(); ++it)
		window.draw(*it);
}

void GUI::renderMove(std::vector<std::pair<int,int>> movementPath, Cell* activeCell) {

	
	tilemap.drawMovingChar(activeCell->getPosition(), tileSize, true, activeCell->getPosition());

	int dx, dy;
	std::pair<int,int> pixelCoords;
	int framesPerTile = 8;
	std::vector<sf::Text> texts = sidebar.getText();

	for (auto it = movementPath.begin(); it != movementPath.end() - 1; ++it) {
		for (int i = 1; i <= framesPerTile; ++i) {
			dx = (double)i / (double)framesPerTile * ((it + 1)->first - it->first) * tileSize;
			dy = (double)i / (double)framesPerTile * ((it + 1)->second - it->second) * tileSize;

			pixelCoords.first = it->first*tileSize + dx;
			pixelCoords.second = it->second*tileSize + dy;

			window.clear();

			window.setView(window.getDefaultView());
			for (auto it = texts.begin(); it != texts.end(); ++it)
				window.draw(*it);

			window.setView(mapView);
			tilemap.drawMovingChar(pixelCoords, tileSize);
			window.draw(tilemap);

			window.display();
		}
	}
}


void GUI::displayGameEnded(std::string winner) {
	window.setView(window.getDefaultView());
	sf::RectangleShape rect(sf::Vector2f(width / 5 * 2,height / 3));
	rect.setPosition(width / 4, height / 4);
	rect.setFillColor(sf::Color::Black);

	sf::Text endText = sidebar.renderEndText(winner, std::pair<int,int>(width / 3, height / 7 * 2 + 20));
	window.draw(rect);
	window.draw(endText);
	window.display();
}
