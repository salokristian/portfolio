#ifndef ITEM_HPP_
#define ITEM_HPP_


#include <string>
#include <memory>

class Item {
public:
	Item() {}
	Item(int weig, const std::string &n) : weight(weig), name(n) {}
	virtual ~Item() {}

	std::shared_ptr<Item> operator= (const std::shared_ptr<Item> i);

	int getWeight() const;
	std::string& getName();
private:
	int weight;
	std::string name;
};

#endif /* ITEM_HPP_ */
