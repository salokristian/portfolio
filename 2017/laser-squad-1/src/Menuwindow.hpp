#ifndef MENUWINDOW_HPP
#define MENUWINDOW_HPP

#include "SFML/Graphics.hpp"

#define MAX_NUMBER_OF_ITEMS 8
#define MAX_NAME_LEN 10

class Menuwindow {
public:
	Menuwindow(int mapNo);
	void runWindow();
	std::pair<std::string, std::string> getNames();
	int getLevelNo();

private:
	void draw();
	void move(bool up);
	void parseText(sf::Event event);
	bool checkInputValues();
	void showFalseInput();
	sf::Text text[MAX_NUMBER_OF_ITEMS];
	int selectedItem;
	sf::Font font;
	sf::RenderWindow window;
	int height;
	int width;
	int mapCount;
};

#endif
