#include "Tilemap.hpp"
#include <iostream>

bool Tilemap::load(const std::string & tilesetName, sf::Vector2u tileSize, std::vector<std::string> level, int wdth, int hght)
{
	if (!tileSet.loadFromFile(tilesetName))
		return false;

	width = wdth;
	height = hght;

	background.setPrimitiveType(sf::Quads);
	background.resize(width * height * 4 + 4);


	characters.setPrimitiveType(sf::Quads);
	characters.resize(width * height * 4 + 4);

	healthBars.setPrimitiveType(sf::Quads);


	highlights.setPrimitiveType(sf::Lines);

	for (int i = 0; i < width; ++i)
		for (int j = 0; j < height; ++j)
		{

			char tileCode = level[i][j];

			int ty, tx; //Maybe add enums for these?
			if (tileCode == '#') {
				tx = 4;
				ty = 0;
			}
			else {
				tx = 15;
				ty = 17;
			}
			sf::Vertex* quad = &background[(i + j * width) * 4];


			quad[0].position = sf::Vector2f(i * tileSize.x, j * tileSize.y);
			quad[1].position = sf::Vector2f((i + 1) * tileSize.x, j * tileSize.y);
			quad[2].position = sf::Vector2f((i + 1) * tileSize.x, (j + 1) * tileSize.y);
			quad[3].position = sf::Vector2f(i * tileSize.x, (j + 1) * tileSize.y);

			quad[0].texCoords = sf::Vector2f(tx * tileSize.x, ty * tileSize.y);
			quad[1].texCoords = sf::Vector2f((tx + 1) * tileSize.x, ty * tileSize.y);
			quad[2].texCoords = sf::Vector2f((tx + 1) * tileSize.x, (ty + 1) * tileSize.y);
			quad[3].texCoords = sf::Vector2f(tx * tileSize.x, (ty + 1) * tileSize.y);
		}

	return true;
}


void Tilemap::update(const std::vector<std::pair<int, int>>& targetableCells, sf::Vector2u tileSize, const std::vector<std::pair<int,int>> movableCells, std::pair<std::vector<CharacterWithPos>, std::vector<CharacterWithPos>> visibleUnits, const std::vector<std::pair<int,int>> items, Cell* currentCell)
{
	map = background;
	int xTex, yTex;
	int index = 0;
	const std::vector<std::pair<std::shared_ptr<Character>, std::pair<int,int>>> playerZeroUnits = visibleUnits.first;
	const std::vector<std::pair<std::shared_ptr<Character>, std::pair<int,int>>> playerOneUnits = visibleUnits.second;
	int playerZeroTexRow = 22;
	int playerOneTexRow = 24;

	// Items
	for (auto it = items.begin(); it != items.end(); ++it) {

		sf::Vertex* quad = &map[(it->first + it->second * width) * 4];

		xTex = 26;
		yTex = 4;

		quad[0].texCoords = sf::Vector2f(xTex * tileSize.x, yTex * tileSize.y);
		quad[1].texCoords = sf::Vector2f((xTex + 1) * tileSize.x, yTex * tileSize.y);
		quad[2].texCoords = sf::Vector2f((xTex + 1) * tileSize.x, (yTex + 1) * tileSize.y);
		quad[3].texCoords = sf::Vector2f(xTex * tileSize.x, (yTex + 1) * tileSize.y);
	}
	healthBars.clear();
	characters.clear();
	characters.resize(width * height * 4 + 4);

	drawUnits(tileSize, playerZeroUnits, playerZeroTexRow);
	drawUnits(tileSize, playerOneUnits, playerOneTexRow);
	
	
	
	drawHealthBars(tileSize, visibleUnits);






	highlights.resize(8 * (movableCells.size() + targetableCells.size() + 1));
	int offset = 3;

	for (auto const& position : movableCells) {

		sf::Vertex* quad = &highlights[index];
		quad[0].position = sf::Vector2f(position.first * tileSize.x + offset, position.second * tileSize.y + offset);
		quad[1].position = sf::Vector2f((position.first + 1) * tileSize.x - offset, position.second * tileSize.y + offset);
		quad[2].position = quad[1].position;
		quad[3].position = sf::Vector2f((position.first + 1) * tileSize.x - offset, (position.second + 1) * tileSize.y - offset);
		quad[4].position = quad[3].position;
		quad[5].position = sf::Vector2f(position.first * tileSize.x + offset, (position.second + 1) * tileSize.y - offset);
		quad[6].position = quad[5].position;
		quad[7].position = sf::Vector2f(position.first * tileSize.x + offset, position.second * tileSize.y + offset);

		for (int i = 0; i < 8; ++i)
			quad[i].color = sf::Color(100,100,100);

		index += 8;
	}

	for (auto const& position : targetableCells) {

		sf::Vertex* quad = &highlights[index];
		quad[0].position = sf::Vector2f(position.first * tileSize.x, position.second * tileSize.y);
		quad[1].position = sf::Vector2f((position.first + 1) * tileSize.x, position.second * tileSize.y);
		quad[2].position = quad[1].position;
		quad[3].position = sf::Vector2f((position.first + 1) * tileSize.x, (position.second + 1) * tileSize.y);
		quad[4].position = quad[3].position;
		quad[5].position = sf::Vector2f(position.first * tileSize.x, (position.second + 1) * tileSize.y);
		quad[6].position = quad[5].position;
		quad[7].position = sf::Vector2f(position.first * tileSize.x, position.second * tileSize.y);

		for (int i = 0; i < 8; ++i)
			quad[i].color = sf::Color(255,0,0);

		index += 8;
	}

	std::pair<int,int> position = currentCell->getPosition();
	sf::Vertex* quad = &highlights[index];
	quad[0].position = sf::Vector2f(position.first * tileSize.x, position.second * tileSize.y);
	quad[1].position = sf::Vector2f((position.first + 1) * tileSize.x, position.second * tileSize.y);
	quad[2].position = quad[1].position;
	quad[3].position = sf::Vector2f((position.first + 1) * tileSize.x, (position.second + 1) * tileSize.y);
	quad[4].position = quad[3].position;
	quad[5].position = sf::Vector2f(position.first * tileSize.x, (position.second + 1) * tileSize.y);
	quad[6].position = quad[5].position;
	quad[7].position = sf::Vector2f(position.first * tileSize.x, position.second * tileSize.y);

	for (int i = 0; i < 8; ++i)
		quad[i].color = sf::Color::Blue;
}


void Tilemap::drawUnits(sf::Vector2u tileSize, std::vector<std::pair<std::shared_ptr<Character>, std::pair<int,int>>> units, int yTex) {
	int xTex;

	for (auto it = units.begin(); it != units.end(); ++it) {
		sf::Vertex* quad = &characters[(it->second.first + it->second.second * width) * 4];
		
		const std::string charName = it->first->getName();
		if (charName == "Scout") {
			xTex = 0;
		}
		else if (charName == "Soldier") {
			xTex = 12;
		}
		else if (charName == "Boss") {
			xTex = 14;
		}
		
		quad[0].position = sf::Vector2f(it->second.first * tileSize.x, it->second.second * tileSize.y);
		quad[1].position = sf::Vector2f((it->second.first + 1) * tileSize.x, it->second.second * tileSize.y);
		quad[2].position = sf::Vector2f((it->second.first + 1) * tileSize.x, (it->second.second + 1) * tileSize.y);
		quad[3].position = sf::Vector2f(it->second.first * tileSize.x, (it->second.second + 1) * tileSize.y);


		quad[0].texCoords = sf::Vector2f(xTex * tileSize.x, yTex * tileSize.y);
		quad[1].texCoords = sf::Vector2f((xTex + 1) * tileSize.x, yTex * tileSize.y);
		quad[2].texCoords = sf::Vector2f((xTex + 1) * tileSize.x, (yTex + 1) * tileSize.y);
		quad[3].texCoords = sf::Vector2f(xTex * tileSize.x, (yTex + 1) * tileSize.y);

		if (it->first->getActionPoints() == 0) {
			for (int i = 0; i < 4; i++) {
				quad[i].color.r *= 0.5;
				quad[i].color.g *= 0.5;
				quad[i].color.b *= 0.5;
			}
		}

		
	}
}

void Tilemap::drawHealthBars(sf::Vector2u tileSize, std::pair<std::vector<std::pair<std::shared_ptr<Character>, std::pair<int, int>>>, std::vector<std::pair<std::shared_ptr<Character>, std::pair<int, int>>>> visibleUnits)
{
	//healthbars
	for (auto temp : visibleUnits.second) {
		auto pos = temp.second;

		double health = temp.first->getHP();
		double maxHealth = temp.first->getMaxHp();
		double ratio = health / maxHealth;
		sf::Vertex Back0;
		sf::Vertex Back1;
		sf::Vertex Back2;
		sf::Vertex Back3;

		Back0.texCoords = sf::Vector2f(0, 0);
		Back1.texCoords = sf::Vector2f(1, 0);
		Back2.texCoords = sf::Vector2f(1, 1);
		Back3.texCoords = sf::Vector2f(0, 1);

		Back0.position = sf::Vector2f(pos.first * tileSize.x, pos.second * tileSize.y + 14);
		Back1.position = sf::Vector2f((pos.first + 1) * tileSize.x, pos.second * tileSize.y + 14);
		Back2.position = sf::Vector2f((pos.first + 1) * tileSize.x, (pos.second + 1) * tileSize.y);
		Back3.position = sf::Vector2f(pos.first * tileSize.x, (pos.second + 1) * tileSize.y);

		healthBars.append(Back0);
		healthBars.append(Back1);
		healthBars.append(Back2);
		healthBars.append(Back3);

		sf::Vertex Front0;
		sf::Vertex Front1;
		sf::Vertex Front2;
		sf::Vertex Front3;

		Front0.texCoords = sf::Vector2f(1, 0);
		Front1.texCoords = sf::Vector2f(2, 0);
		Front2.texCoords = sf::Vector2f(2, 1);
		Front3.texCoords = sf::Vector2f(1, 1);

		Front0.position = sf::Vector2f(pos.first * tileSize.x, pos.second * tileSize.y + 14);
		Front1.position = sf::Vector2f((pos.first + ratio) * tileSize.x, pos.second * tileSize.y + 14);
		Front2.position = sf::Vector2f((pos.first + ratio) * tileSize.x, (pos.second + 1) * tileSize.y);
		Front3.position = sf::Vector2f(pos.first * tileSize.x, (pos.second + 1) * tileSize.y);

		healthBars.append(Front0);
		healthBars.append(Front1);
		healthBars.append(Front2);
		healthBars.append(Front3);
	}
	for (auto temp : visibleUnits.first) {
		auto pos = temp.second;

		double health = temp.first->getHP();
		double maxHealth = temp.first->getMaxHp();
		double ratio = health / maxHealth;
		sf::Vertex Back0;
		sf::Vertex Back1;
		sf::Vertex Back2;
		sf::Vertex Back3;

		Back0.texCoords = sf::Vector2f(0, 0);
		Back1.texCoords = sf::Vector2f(1, 0);
		Back2.texCoords = sf::Vector2f(1, 1);
		Back3.texCoords = sf::Vector2f(0, 1);

		Back0.position = sf::Vector2f(pos.first * tileSize.x, pos.second * tileSize.y + 14);
		Back1.position = sf::Vector2f((pos.first + 1) * tileSize.x, pos.second * tileSize.y + 14);
		Back2.position = sf::Vector2f((pos.first + 1) * tileSize.x, (pos.second + 1) * tileSize.y);
		Back3.position = sf::Vector2f(pos.first * tileSize.x, (pos.second + 1) * tileSize.y);

		healthBars.append(Back0);
		healthBars.append(Back1);
		healthBars.append(Back2);
		healthBars.append(Back3);

		sf::Vertex Front0;
		sf::Vertex Front1;
		sf::Vertex Front2;
		sf::Vertex Front3;

		Front0.texCoords = sf::Vector2f(1, 0);
		Front1.texCoords = sf::Vector2f(2, 0);
		Front2.texCoords = sf::Vector2f(2, 1);
		Front3.texCoords = sf::Vector2f(1, 1);

		Front0.position = sf::Vector2f(pos.first * tileSize.x, pos.second * tileSize.y + 14);
		Front1.position = sf::Vector2f((pos.first + ratio) * tileSize.x, pos.second * tileSize.y + 14);
		Front2.position = sf::Vector2f((pos.first + ratio) * tileSize.x, (pos.second + 1) * tileSize.y);
		Front3.position = sf::Vector2f(pos.first * tileSize.x, (pos.second + 1) * tileSize.y);

		healthBars.append(Front0);
		healthBars.append(Front1);
		healthBars.append(Front2);
		healthBars.append(Front3);
	}

}

void Tilemap::drawGunshot(std::pair<int,int> attackOrigin, std::pair<int,int> attackDestination, int tileSize) {
	sf::Vertex originVertex(sf::Vector2f(attackOrigin.first*tileSize + 0.5*tileSize, attackOrigin.second*tileSize + 0.5*tileSize), sf::Color::Red);
	sf::Vertex destinationVertex(sf::Vector2f(attackDestination.first*tileSize + 0.5*tileSize, attackDestination.second*tileSize + 0.5*tileSize), sf::Color::Red);
	highlights.append(originVertex);
	highlights.append(destinationVertex);
}


void Tilemap::draw(sf::RenderTarget & target, sf::RenderStates states) const
{
	// apply the transform
	states.transform *= getTransform();

	// apply the tileset texture
	states.texture = &tileSet;

	// draw the vertex array
	target.draw(map, states);
	target.draw(characters, states);
	target.draw(healthBars, states);
	
	target.draw(highlights);
}

void Tilemap::drawMovingChar(std::pair<int,int> nextPos, int tileSize, bool first, std::pair<int,int> arrayPos) {
	if (first) {
		healthBars.clear();
		highlights.clear(); // don't draw the highlights when moving
		sf::Vertex* oldQuad = &characters[(arrayPos.first + arrayPos.second * width) * 4];
		sf::Vertex* newQuad = &characters[map.getVertexCount() - 4];
		
		newQuad[0] = oldQuad[0];
		newQuad[1] = oldQuad[1];
		newQuad[2] = oldQuad[2];
		newQuad[3] = oldQuad[3];

		

		int xTex = 15;
		int yTex = 17;
		oldQuad[0].texCoords = sf::Vector2f(xTex * tileSize, yTex * tileSize);
		oldQuad[1].texCoords = sf::Vector2f((xTex + 1) * tileSize, yTex * tileSize);
		oldQuad[2].texCoords = sf::Vector2f((xTex + 1) * tileSize, (yTex + 1) * tileSize);
		oldQuad[3].texCoords = sf::Vector2f(xTex * tileSize, (yTex + 1) * tileSize);

		return;
		
	}

	sf::Vertex* quad = &characters[characters.getVertexCount() - 4];

	quad[0].position = sf::Vector2f(nextPos.first, nextPos.second);
	quad[1].position = sf::Vector2f(nextPos.first + tileSize, nextPos.second);
	quad[2].position = sf::Vector2f(nextPos.first + tileSize, nextPos.second + tileSize);
	quad[3].position = sf::Vector2f(nextPos.first, nextPos.second + tileSize);

}
