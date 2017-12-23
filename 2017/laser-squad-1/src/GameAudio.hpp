#ifndef GAMEAUDIO_HPP_
#define GAMEAUDIO_HPP_

#include "SFML/Audio.hpp"
#include <map>
#include <string>
#include <iostream>


//a simple class to use sfmls music and sounds easier

class GameAudio {
public:
	GameAudio();


	void playSound(std::string name);
	void playMusic(std::string music);
private:
	void addSound(std::string path);
	void addMusic(std::string path);
	std::map<std::string, sf::SoundBuffer> sounds;
	std::map <std::string, std::string> music;
	sf::Sound soundPlayer;
	sf::Music musicPlayer;
	std::string prefix;
};


#endif
