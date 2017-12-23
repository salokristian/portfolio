#include "GameAudio.hpp"



GameAudio::GameAudio()
{
	prefix = "sounds/";
	addSound("shoot.wav");
	addSound("death.ogg");
	addSound("button.ogg");
	addMusic("ingame.ogg");
	addMusic("dance.ogg");
        musicPlayer.setVolume(60);
	
}


void GameAudio::addSound(std::string file){
	sf::SoundBuffer buffer;
	if (!buffer.loadFromFile(prefix + file)) {
		std::cout << "Failed to open audio: " << file << std::endl;
		return;

	}
	auto truncatePath = file.substr(0, file.find_last_of("."));;
	sounds[truncatePath] = buffer;
}

void GameAudio::addMusic(std::string path)
{
	music[path.substr(0, path.find_last_of("."))] = prefix + path;
}



void GameAudio::playSound(std::string name)
{
	auto it = sounds.find(name);
	if (it != sounds.end()) {
		soundPlayer.setBuffer(it->second);
		soundPlayer.play();
	}

}

void GameAudio::playMusic(std::string name)
{
	auto it = music.find(name);
        if (it != music.end()) {
		if (!musicPlayer.openFromFile(music[name])) {
			std::cout << "Failed to open audio: " << name << std::endl;
			return;
		}


		musicPlayer.play();
		musicPlayer.setLoop(true);
	}
}
