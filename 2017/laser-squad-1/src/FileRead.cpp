#include "FileRead.hpp"

using namespace std;

std::istream& safeGetline(std::istream& is, std::string& t){
	//help function to read lines correctly on different platforms
	//credit goes to the best answer in a thread here:
	//https://stackoverflow.com/questions/6089231/getting-std-ifstream-to-handle-lf-cr-and-crlf

    t.clear();

    // The characters in the stream are read one-by-one using a std::streambuf.
    // That is faster than reading them one-by-one using the std::istream.
    // Code that uses streambuf this way must be guarded by a sentry object.
    // The sentry object performs various tasks,
    // such as thread synchronization and updating the stream state.

    std::istream::sentry se(is, true);
    std::streambuf* sb = is.rdbuf();

    for(;;) {
        int c = sb->sbumpc();
        switch (c) {
        case '\n':
            return is;
        case '\r':
            if(sb->sgetc() == '\n')
                sb->sbumpc();
            return is;
        case std::streambuf::traits_type::eof():
            // Also handle the case when the last line has no line ending
            if(t.empty())
                is.setstate(std::ios::eofbit);
            return is;
        default:
            t += (char)c;
        }
    }
}

void FileRead::readFile(string fileName){

	vector<string> textLines;
	vector<vector<string>> maps;
	levelNum = 0;

	string line;

	ifstream file (fileName);

	if (file.is_open()){
		//while ( getline (file,line) ){
		while(!safeGetline(file, line).eof()){
			//get one line
			if (line[0] != ';'){
				//if line doesnt start with ; aka comment:
				if (!line.empty()){
					//if line is not empty
					textLines.push_back(line);
				}
				else if (line.empty() && textLines.size() > 0){
					//if line is empty and textfiles has something
					//=>one map has been read to textlines
					maps.push_back(textLines);
					levelNum++;
					//reset variables
					textLines.clear();
				}

			}
		} //end of while-loop
		file.close();
	}

	//checking if file exists
	else {
		throw std::runtime_error("Can't open game level text file");
	}

	levels = maps;
	//check if every level is valid
	checkValidLvls();
}

int FileRead::getLvlNum() const {
	return levelNum;
}

vector<string> FileRead::getLvlAt(int lvlNum) const {
	//returns a vector<string> container with the starting positions of map objects
	//call example: vector<string> lvl = fr.getLvlAt(1);
	//access elements: lvl[y][x] where x is the width, and y is the height coordinate

	//levels start from 1, 2, 3,..
	if (lvlNum > levelNum || lvlNum <= 0){
		throw std::out_of_range("getLvlAt called with invalid level number");
	}
	return levels[lvlNum-1];
}

std::vector<std::vector<std::string>> FileRead::getLevels() const {
	//dont call this, call getLvlAt instead
	return levels;
}

int FileRead::maxWidth(int lvlNum) const {
	//levels start from 1, 2, 3,..
	if (lvlNum > levelNum || lvlNum <= 0){
			throw std::out_of_range("maxWidth called with invalid level number");
		}
	unsigned int maxWidth = 0;
	for (auto line : getLvlAt(lvlNum)){
		if (line.length() > maxWidth){
			maxWidth = line.length();
		}
	}
	return maxWidth;
}

int FileRead::maxHeight(int lvlNum) const {
	//levels start from 1, 2, 3,..
	if (lvlNum > levelNum || lvlNum <= 0){
			throw std::out_of_range("maxHeight called with invalid level number");
		}
	return getLvlAt(lvlNum).size();
}

void FileRead::checkValidLvls() const {
	//go through all levels
	for (int i = 0; i < getLvlNum(); i++){
		vector<string> curlvl = getLvlAt(i+1);
		int maxH = maxHeight(i+1);
		int maxW = maxWidth(i+1);

		for (int k = 0; k < maxW; k++){
			if (curlvl[0][k] != '#'){
				cout << "Level #" << (i+1) << " is invalid, level must be surrounded by walls (# character)" << endl;
				throw std::logic_error("Valid surrounding walls not found");
			}
			if (curlvl[maxH-1][k] != '#'){
				cout << "Level #" << (i+1) << " is invalid, level must be surrounded by walls (# character)" << endl;
				throw std::logic_error("Valid surrounding walls not found");
			}
		}

		int width = 0;
		int boss1 = 0;
		int boss2 = 0;
		for (int y = 0; y < maxH; y++){
			width = curlvl[y].length();
			if (width != maxW){
				cout << "Level #" << (i+1) << " is invalid, width is " << width << ", while maxWidth is " << maxW << endl;
				cout << "MaxHeight is " << maxH << endl;
				throw std::logic_error("Width is invalid, level needs to be a rectangle");
			}
			if (curlvl[y][0] != '#' || curlvl[y][width-1] != '#'){
				cout << "Level #" << (i+1) << " is invalid, level must be surrounded by walls (# character)" << endl;
				throw std::logic_error("Valid surrounding walls not found");
			}
			for (int x = 0; x < maxW; x++){
				if (curlvl[y][x] == 'B'){
					boss1++;
				}
				if (curlvl[y][x] == 'b'){
					boss2++;
				}
			}
		}
		if (boss1 != 1 || boss2 != 1){
			cout << "Level #" << (i+1) << " is invalid, level must have 1 boss character for both players!" << endl;
			cout << "Bosses found: Player1 " << boss1 << " bosses, Player2 " << boss2 << " bosses." << endl;
			throw std::logic_error("Invalid number of bosses found");
		}
	}
}

