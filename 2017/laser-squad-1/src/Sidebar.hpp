#ifndef SIDEBAR_H
#define SIDEBAR_H

#include "SFML/Graphics.hpp"
#include "Player.hpp"
#include "Cell.hpp"
#include <vector>

/*
This class renders the text information displayed in the sidebar:
player name, action points, team members alive,
current character HP and items, current cell items.
Should be called from the renderGameWindow function in GUI class.
*/

class Sidebar {
public:
	Sidebar() : headingTextSize(22), smallTextSize(15), infoTextSize(10) {}
	// Initializes the font and text variables. Needs to be called before calling renderSidebar.
	bool initializeFonts(sf::FloatRect textArea);

	// Renders the information displayed in the sidebar. Returns them as drawable text objects.
	const std::vector<sf::Text>& renderSidebar(const Player& turn, Cell* currentCell, int chosenCharItem, int choseCellItem);

	// Renders the text in info window
	std::vector<sf::Text> renderInfoText();

	// Renders the number of damage dealt per gunshot
	sf::Text renderGunDamage(int hitDamage, std::pair<int,int> attackOrigin, std::pair<int,int> attackDestination, int tileSize);

	std::vector<sf::Text> getText();

	sf::Text renderEndText(std::string& winner, std::pair<int,int> pos);

private:
	size_t find_Nth(const std::string& str, unsigned N, const std::string& find);
	std::string parseItemData(std::shared_ptr<Item> item);
	sf::Font font;
	std::vector<sf::Text> texts;
	int headingTextSize;
	int smallTextSize;
	int infoTextSize;
};

#endif
