#ifndef GUI_H
#define GUI_H

#include "SFML/Graphics.hpp"
#include "Tilemap.hpp"
#include "Sidebar.hpp"
#include "Map.hpp"

// TODO Add FPS, window size, etc. to constructor with default values, i.e. minimize hard-coding
// TODO Create function createWindow() that is called before the first call to renderGameWindow()
// TODO try to make it so that the map or cells need not be handled here but in the game class

class GUI {
public:
	GUI() : tileSize(16), height(600), width(800) {};

	//Initialize and open the window. This should be called only once. 
	void initWindow(std::vector<std::string> level);

	/*
	Renders the game window, i.e. draws the background, characters, items and sidebar with player information.
	Should be called once during each iteration of the main game loop.
	*/
	void renderGameWindow(const std::vector<std::pair<int, int>>& targetableCells, const Player &turn, const std::vector<std::pair<int,int>> movableCells, std::pair<std::vector<CharacterWithPos>, std::vector<CharacterWithPos>> visibleUnits, const std::vector<std::pair<int,int>> items, Cell *currentCell, int chosenCharItem, int choseCellItem);

	// Renders the information window.
	void renderInfoWindow();

	// Get the latest event that has occured in the game window.
	bool getEvent(sf::Event& event);

	// Check if window is open.
	bool isOpen();

	// Close the active window.
	void closeWindow();

	// Move the map view according to user keyboard input.
	void moveView(sf::Keyboard::Key);

	// Change the view so that the cell pointed by currentCell is in center.
	void changeView(Cell* currentCell);

	// Get Map coordinates of a mouse click. If the click was not in the map region, <-1,-1> is returned.
	std::pair<int,int> getMapCoordinates(sf::Event::MouseButtonEvent event);

	// Display everything that has been drawn on the window
	void displayGameWindow();

	// Renders a gunshot and its damage number
	void renderAttack(int hitDamage, std::pair<int,int> attackOrigin, std::pair<int,int> attackDestination);

	// Renders the movement of a character along movementPath
	void renderMove(std::vector<std::pair<int,int>> movementPath, Cell* activeCell);

	// Displays the winner of the game
	void displayGameEnded(std::string winner);


private:
	void flipVisibleUnits(const Player& turn, std::pair<std::vector<CharacterWithPos>, std::vector<CharacterWithPos>>& visibleUnits);
	sf::RenderWindow window;
	Tilemap tilemap;
	Sidebar sidebar;
	sf::View mapView;
	sf::View sidebarView;
	int tileSize; 
	int height;
	int width;
	int xTileCount;
	int yTileCount;
};

#endif
