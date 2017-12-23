#ifndef TILEMAP_H
#define TILEMAP_H

#include "SFML/Graphics.hpp"
#include <memory>
#include "Character.hpp"
#include "Cell.hpp"
#include "Map.hpp"


class Tilemap : public sf::Drawable, public sf::Transformable
{
public:
	// Initialize the background of the map. Should only be called once in the beginning of the game.
	bool load(const std::string& tileset, sf::Vector2u tileSize, std::vector<std::string> level, int width, int height);

	/*
	Update the contents on top of the background, i.e. characters and items. 
	Should always be called before draw(), if any objects are moved/modified.
	In the final version, this will get a Map * as a parameter to check the object states from.
	Right now this is just a dummy function that draws some characters somewhere.
	*/
	void update(const std::vector<std::pair<int, int>>& targetableCells, sf::Vector2u tileSize, const std::vector<std::pair<int,int>> movableCells, std::pair<std::vector<CharacterWithPos>, std::vector<CharacterWithPos>> visibleUnits, const std::vector<std::pair<int,int>> items, Cell* currentCell);

	void drawGunshot(std::pair<int,int> attackOrigin, std::pair<int,int> attackDestination, int tileSize);

	void drawMovingChar(std::pair<int,int> nextPos, int tileSize, bool first = false, std::pair<int,int> arrayPos = std::pair<int,int>());

private:
	virtual void draw(sf::RenderTarget& target, sf::RenderStates states) const;
	void drawUnits(sf::Vector2u tileSize, std::vector<std::pair<std::shared_ptr<Character>, std::pair<int,int>>> units, int yTex);
	void drawHealthBars(sf::Vector2u tileSize, std::pair<std::vector<std::pair<std::shared_ptr<Character>, std::pair<int, int>>>, std::vector<std::pair<std::shared_ptr<Character>, std::pair<int, int>>>> visibleUnits);
	sf::VertexArray background;
	sf::VertexArray map;
	sf::VertexArray highlights;
	sf::VertexArray characters;
	sf::VertexArray healthBars;
	sf::Texture tileSet;
	int width;
	int height;
};


#endif // !TILEMAP_H
