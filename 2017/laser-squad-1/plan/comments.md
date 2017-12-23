# Comments about the plan

Good and comprehensive plan. The class structure seems well thought
out. To further tweak the class structure, I think both *Character* and
*Item* are some sort of "world objects" that have some common
properties, e.g., weight, hit points, graphical representation on map,
and so on. I.e., they could be inherited from common abstract base
class.

The plan does not say anything about libraries. SFML would be a good
choice for this sort of project, are you going with that?

Having maps specified as text file is a good idea. Then you can quite
easily create new maps just by a text editor, unless you have time for
map editor (doubtful).

Regarding the schedule, it is realistic to aim for a feature complete
version at least a week before final deadline. Almost always the
development takes more time than initially estimated, and decent testing and
consecutive bug fixing also takes days. If you apply unit tests, it is
good idea to implement them as soon as you implement the particular
classes and functions, so you can maintain ongoing testing also
earlier.

On which platforms are you developing and testing the project? Aalto
Linux was an "official" requirement, but if you happen to make it Mac
(Sierra) compatible, I don't complain to be able to test it on my own
laptop.
