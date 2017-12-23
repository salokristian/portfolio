#ifndef SRC_FILEREAD_HPP_
#define SRC_FILEREAD_HPP_

#include <iostream>
#include <fstream>
#include <string>
#include <vector>

/*
Class for reading the starting positions from a text file
Example use:

FileRead fr;
fr.readFile("GameLevels.txt");
vector<string> lvl1 = fr.getLvlAt(1);
//coordinates are accessed like so: lvl[y][x];

*/

class FileRead{
public:
	//reads the file contents to a vector called levels
	void readFile(std::string fileName);

	//returns how many levels were found in the file
	int getLvlNum() const;

	//returns a level, first level starts from 1, i.e. getLvlAt(1)
	std::vector<std::string> getLvlAt(int lvlNum) const;

	//returns a vector containing all the levels
	//use getLvlAt instead
	std::vector<std::vector<std::string>> getLevels() const;

	int maxWidth(int lvlNum) const; //returns the longest width of a level
	int maxHeight(int lvlNum) const; //returns the height of a level
	void checkValidLvls() const; //checks that the levels are valid

private:
	std::vector<std::vector<std::string>> levels;
	int levelNum; //number of levels found

};

#endif /* SRC_FILEREAD_HPP_ */
