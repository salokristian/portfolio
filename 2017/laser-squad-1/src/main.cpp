#include "Game.hpp"
#include "FileRead.hpp"
#include "Menuwindow.hpp"
#include <iostream>

int main() {

	FileRead fr;
	Game game;

	try {
		fr.readFile("includes/GameLevels.txt");

		Menuwindow menu(fr.getLvlNum());
		menu.runWindow();

		std::vector<std::string> level = fr.getLvlAt(menu.getLevelNo());

		game.initGame(level, menu.getNames());
		game.playGame();
	}
	catch (std::runtime_error &e) {
		std::cout << e.what() << "\nShutting down." << std::endl;
	}
	catch (std::logic_error &e) {
		std::cout << e.what() << "\nShutting down." << std::endl;
	}

	return 1;
}
