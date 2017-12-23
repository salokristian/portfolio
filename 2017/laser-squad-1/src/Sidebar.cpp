#include "Sidebar.hpp"
#include "Ammunition.hpp"
#include "Gun.hpp"
#include "Food.hpp"
#include <memory>

enum TextIndex : unsigned int {
	PlayerData = 1,
	CharData = 3,
	CellData = 5
};


const std::vector<sf::Text>& Sidebar::renderSidebar(const Player& turn, Cell* currentCell, int chosenCharItem, int chosenCellItem)
{
	std::shared_ptr<Character> currentChar = currentCell->getCharacter();
	std::shared_ptr<Gun> currentGun = currentChar->getActiveGun();
	std::vector<std::shared_ptr<Item>> charItems = currentChar->getItem();
	std::vector<std::shared_ptr<Item>> cellItems = currentCell->getItems();

	std::string playerString = turn.getName() + "\n" + std::to_string(turn.getActionPoints()) +
			" APs\n" + std::to_string(turn.getCharCount()) + " Characters";
	std::string charString = currentChar->getName() + "\n" + std::to_string(currentChar->getActionPoints()) + " APs\n" +
			std::to_string(currentChar->getHP()) +  " HP\n" + std::to_string(currentChar->getSpeed()) +
			" Speed\n" + currentGun->getName() + "\n" + "Range: " + std::to_string(currentGun->getRange()) + "\nDmg: " +
			std::to_string(currentGun->getDamage()) + "\nHit%: " + std::to_string(currentGun->getProbability()) + "\n" +
			std::to_string(currentGun->getAmmo()) + "/" + std::to_string(currentGun->getMaxAmmo()) +
			" Ammo\n\nItems:";
	std::string cellString = "";

	if (!charItems.empty()) {
		
		auto first_it = charItems.begin() + chosenCharItem - chosenCharItem % 3;
		int count = 0;
		for (auto it = first_it; count < 3 && it != charItems.end(); ++it, count++) {
			charString += "\n" + (*it)->getName() + " " + parseItemData(*it);
		}
		charString.insert(find_Nth(charString, chosenCharItem%3 + 11, "\n") + 1, "*");
	}

	if (!cellItems.empty()) {
		int count = 0;
		auto first_it = cellItems.begin() + chosenCellItem - chosenCellItem % 3;
		for (auto it = first_it; count < 3 && it != cellItems.end(); ++it, count++) {
			cellString += (*it)->getName() + " " + parseItemData(*it) + "\n";
		}
			
		cellString.insert(find_Nth(cellString, chosenCellItem % 3, "\n") + 1, "*");
	}

	texts.at(CharData).setString(charString);
	texts.at(CellData).setString(cellString);
	texts.at(PlayerData).setString(playerString);

	return texts;
}

size_t Sidebar::find_Nth(const std::string& str, unsigned N, const std::string& find) {
    if ( 0==N ) {
    	return -1;
    }
    size_t pos,from=0;
    unsigned i=0;
    while ( i<N ) {
        pos=str.find(find,from);
        if ( std::string::npos == pos ) { break; }
        from = pos + 1;
        ++i;
    }
    return pos;
}


std::vector<sf::Text> Sidebar::renderInfoText() {
	std::string actions ="Scroll character items up\n\n"
						"Scroll character items down\n\n"
						"Scroll cell items up\n\n"
						"Scroll cell items down\n\n"
						"Drop selected character item\n\n"
						"Pick up selected cell item\n\n"
						"Use selected character item\n\n"
						"Shoot\n\n"
						"Move\n\n"
						"Select another character\n\n"
						"End turn\n\n"
						"Scroll map\n\n"
						"Exit this window\n\n";
	std::string commands = 	"Z\n\n"
							"X\n\n"
							"C\n\n"
							"V\n\n"
							"D\n\n"
							"P\n\n"
							"U\n\n"
							"Right mouse\n\n"
							"Left mouse\n\n"
							"Left mouse\n\n"
							"Enter\n\n"
							"Arrow keys\n\n"
							"I";
	sf::Text actionsText(actions, font, smallTextSize);
	sf::Text commandsText(commands, font, smallTextSize);
	std::vector<sf::Text> infoTexts = {actionsText, commandsText};
	return infoTexts;

}

bool Sidebar::initializeFonts(sf::FloatRect textArea)
{
	if (!font.loadFromFile("includes/font.ttf"))
	{
		return false;
	}

	sf::Text textHeading;
	textHeading.setFont(font);
	textHeading.setCharacterSize(headingTextSize);
	textHeading.setColor(sf::Color::White);

	sf::Text textSmall;
	textSmall.setFont(font);
	textSmall.setCharacterSize(smallTextSize);
	textSmall.setColor(sf::Color::White);

	sf::Text textInfo;
	textInfo.setFont(font);
	textInfo.setCharacterSize(infoTextSize);
	textInfo.setColor(sf::Color::White);



	textHeading.setPosition(sf::Vector2f(textArea.left, textArea.top + headingTextSize));
	textHeading.setString("Player");
	texts.push_back(textHeading);

	textSmall.setPosition(sf::Vector2f(textArea.left, textArea.top + headingTextSize + 10 + smallTextSize));
	// Player information is added at runtime
	texts.push_back(textSmall);

	textHeading.setPosition(sf::Vector2f(textArea.left, textArea.top + headingTextSize + textArea.height / 4));
	textHeading.setString("Character");
	texts.push_back(textHeading);

	textSmall.setPosition(sf::Vector2f(textArea.left, textArea.top + headingTextSize + 10 + smallTextSize + textArea.height / 4));
	// Character information is added at runtime
	texts.push_back(textSmall);

	textHeading.setPosition(sf::Vector2f(textArea.left, textArea.top + headingTextSize + textArea.height * 2/3));
	textHeading.setString("Cell");
	texts.push_back(textHeading);

	textSmall.setPosition(sf::Vector2f(textArea.left, textArea.top + headingTextSize + 10 + smallTextSize + textArea.height * 2/3));
	// Cell information is added at runtime
	texts.push_back(textSmall);

	textInfo.setPosition(sf::Vector2f(textArea.left, textArea.top + textArea.height - infoTextSize));
	textInfo.setString("Press i for info");
	texts.push_back(textInfo);

	return true;
}

std::string Sidebar::parseItemData(std::shared_ptr<Item> item) {
	std::string itemName = item->getName();
	std::string itemData;

	if (itemName == "Ammunition") {
		std::shared_ptr<Ammunition> ammoItem = std::dynamic_pointer_cast<Ammunition>(item); // Maybe check if (ammoItem)
		itemData = std::to_string(ammoItem->getAmount());
	}

	else if (itemName == "Food") {
		std::shared_ptr<Food> foodItem = std::dynamic_pointer_cast<Food>(item);
		itemData = std::to_string(foodItem->getHealth());
	}

	return itemData;
}

sf::Text Sidebar::renderGunDamage(int hitDamage, std::pair<int,int> attackOrigin, std::pair<int,int> attackDestination, int tileSize) {
	sf::Text textDamage;
	textDamage.setFont(font);
	textDamage.setCharacterSize(infoTextSize);
	textDamage.setColor(sf::Color::Red);
	textDamage.setString(std::to_string(hitDamage));
	int xMid = (attackOrigin.first*tileSize + attackDestination.first*tileSize) / 2;
	int yMid = (attackOrigin.second*tileSize + attackDestination.second*tileSize) / 2 + smallTextSize;
	sf::Vector2f middlePosition(xMid, yMid);
	textDamage.setPosition(middlePosition);
	return textDamage;
}

std::vector<sf::Text> Sidebar::getText() {
	return texts;
}


sf::Text Sidebar::renderEndText(std::string& winner, std::pair<int,int> pos) {
	sf::Text textEnd;
	textEnd.setFont(font);
	textEnd.setCharacterSize(smallTextSize);
	textEnd.setColor(sf::Color::White);
	textEnd.setString("Boss killed,\n\ngame ends in\n\n" + winner + "'s\n\nvictory!!");
	sf::Vector2f middlePosition(pos.first, pos.second);
	textEnd.setPosition(middlePosition);
	return textEnd;
}
