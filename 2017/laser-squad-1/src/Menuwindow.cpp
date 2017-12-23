#include "Menuwindow.hpp"
#include <iostream>
#include <sstream>
#include <ctype.h>

enum dataIndex : unsigned int {
	PlayerOneName = 2,
	PlayerTwoName = 4,
	MapNo = 6,
};

Menuwindow::Menuwindow(int mapNo) {
	height = 600;
	width = 800;

	mapCount = mapNo;

	window.create(sf::VideoMode(width, height), "Laser Squad", sf::Style::Titlebar | sf::Style::Close);
	window.setFramerateLimit(30);

	if (!font.loadFromFile("includes/font.ttf"))
		throw std::runtime_error("Error when opening font");

	text[0].setFont(font);
	text[0].setCharacterSize(30);
	text[0].setColor(sf::Color::White);
	text[0].setString("Laser squad");
	text[0].setPosition(sf::Vector2f(width / 7 * 2, height / (MAX_NUMBER_OF_ITEMS ) * 1));

	text[1].setFont(font);
	text[1].setCharacterSize(16);
	text[1].setColor(sf::Color::Red);
	text[1].setString("Player 1 name");
	text[1].setPosition(sf::Vector2f(width / 7 * 2, height / (MAX_NUMBER_OF_ITEMS ) * 3));

	text[2].setFont(font);
	text[2].setCharacterSize(16);
	text[2].setColor(sf::Color::White);
	text[2].setString("");
	text[2].setPosition(sf::Vector2f(width / 7 * 2, height / (MAX_NUMBER_OF_ITEMS) * 3 + 30));

	text[3].setFont(font);
	text[3].setCharacterSize(16);
	text[3].setColor(sf::Color::White);
	text[3].setString("Player 2 name");
	text[3].setPosition(sf::Vector2f(width / 7 * 2, height / (MAX_NUMBER_OF_ITEMS ) * 4));

	text[4].setFont(font);
	text[4].setCharacterSize(16);
	text[4].setColor(sf::Color::White);
	text[4].setString("");
	text[4].setPosition(sf::Vector2f(width / 7 * 2, height / (MAX_NUMBER_OF_ITEMS) * 4 + 30));

	text[5].setFont(font);
	text[5].setCharacterSize(16);
	text[5].setColor(sf::Color::White);
	text[5].setString("Map number");
	text[5].setPosition(sf::Vector2f(width / 7 * 2, height / (MAX_NUMBER_OF_ITEMS) * 5));

	text[6].setFont(font);
	text[6].setCharacterSize(16);
	text[6].setColor(sf::Color::White);
	text[6].setString("");
	text[6].setPosition(sf::Vector2f(width / 7 * 2, height / (MAX_NUMBER_OF_ITEMS) * 5 + 30));

	text[7].setFont(font);
	text[7].setCharacterSize(16);
	text[7].setColor(sf::Color::White);
	text[7].setString("Start game");
	text[7].setPosition(sf::Vector2f(width / 7 * 2, height / (MAX_NUMBER_OF_ITEMS) * 6));

	selectedItem = 1;
}

void Menuwindow::draw() {
	for (auto& t : text) {
		window.draw(t);
	}
	window.display();
}

void Menuwindow::move(bool down) {
	if (!down && selectedItem > 1) {
		text[selectedItem--].setColor(sf::Color::White);
		text[--selectedItem].setColor(sf::Color::Red);
	}

	else if (down && selectedItem < MAX_NUMBER_OF_ITEMS - 2) {
		text[selectedItem++].setColor(sf::Color::White);
		text[++selectedItem].setColor(sf::Color::Red);
	}
}

void Menuwindow::parseText(sf::Event event) {
    if (event.text.unicode >= 128) {
    	return;
    }

	if (event.text.unicode == 8) { //If backspace is clicked
		std::string old = text[selectedItem + 1].getString();
		if (old.size() > 0) {
			old.erase(old.end() - 1);
			text[selectedItem + 1].setString(old);
		}
	}
	else if (event.text.unicode == 13 && selectedItem != 7) { //If enter is clicked
		if (text[selectedItem + 1].getString().getSize() > 0)
			move(true);
	}

	else if (event.text.unicode == 13 && selectedItem == 7) { //If enter is clicked
		if (checkInputValues())
			window.close();
		else
			showFalseInput();
	}

	else {
		char name = static_cast<char>(event.text.unicode);
		if (isalpha(name) && selectedItem < 5)
			text[selectedItem + 1].setString(text[selectedItem + 1].getString() + name);

		if (isdigit(name) && selectedItem == 5)
			text[selectedItem + 1].setString(text[selectedItem + 1].getString() + name);
	}
}


bool Menuwindow::checkInputValues() {
	std::string nameOne = text[PlayerOneName].getString();
	std::string nameTwo = text[PlayerTwoName].getString();

	int mapNo;
	std::istringstream mapNoText(text[MapNo].getString());
	if (!(mapNoText >> mapNo) || mapNo > mapCount || mapNo < 1)
		return false;

	if (nameOne.size() > MAX_NAME_LEN || nameTwo.size() > MAX_NAME_LEN)
		return false;

	if (nameOne.empty() || nameTwo.empty())
		return false;

	return true;
}

void Menuwindow::showFalseInput() {
	sf::RectangleShape rect(sf::Vector2f(width/4*3, height/4 + 30));
	rect.setPosition(width/8, height/4 - 10);
	rect.setFillColor(sf::Color::Black);

	sf::Text infoText;
	infoText.setFont(font);
	infoText.setCharacterSize(16);
	infoText.setColor(sf::Color::White);
	infoText.setPosition(width/5, height/7*2);
	std::string infoString = "Your input is incorrect.\n\nMaximum map count is: " + std::to_string(mapCount) +
			std::string("\n\nMaximum player name length is: ") + std::to_string(MAX_NAME_LEN) +
			std::string("\n\nAll fields must be filled.");
	infoText.setString(infoString);

	window.draw(rect);
	window.draw(infoText);
	window.display();

	sf::sleep(sf::seconds(4));
}

std::pair<std::string, std::string> Menuwindow::getNames() {
	std::string nameOne = text[PlayerOneName].getString();
	std::string nameTwo = text[PlayerTwoName].getString();
	return std::pair<std::string, std::string>(nameOne, nameTwo);
}

int Menuwindow::getLevelNo() {
	int mapNo;
	std::istringstream mapNoText(text[MapNo].getString());
	mapNoText >> mapNo;
	return mapNo;
}

void Menuwindow::runWindow() {

	while (window.isOpen())
		{
			sf::Event event;

			while (window.pollEvent(event))
			{
				switch (event.type)
				{
				case sf::Event::KeyReleased:
					switch (event.key.code)
					{
					case sf::Keyboard::Up:
						move(false);
						break;

					case sf::Keyboard::Down:
						move(true);
						break;

					case sf::Keyboard::Return:
						// start game
						break;

					default:
						break;
					}
					break;

				case sf::Event::TextEntered:
					parseText(event);
					break;


				case sf::Event::Closed:
					window.close();
					throw(std::runtime_error("Menu window closed"));
					break;

				default:
					break;
				}

			}

			window.clear(sf::Color(50,50,50));

			draw();
		}
}

