# Portal2D
This is a platformer game I started working around in February 2019, based on the game Portal. I had finished working on my 2D physics engine (SimplePhysics) and wanted to try
making a game that used it. Making it work with portals seemed like an interesting challange, and also I just really like the game so I thought it'd be fun to try something with
that inspiration.

While the physics engine I made definitely has some problems, for the most part during play it works out alright without many hickups. To get it working with the portals,
I ended up writing a lot of the game as more of an extension of the physics engine. To achieve the ability of interacting with the same object on both sides of the portals,
I would slice an object inside the portal into two seperate rigid bodies, with the side containing the center of mass being the main body. All impulses applied to the other half
would be translated back through the portal and applied on the main body, allowing its two halves to be interacted with.

I made all the sprites for the game and, am happy with out it turned out visually, although the performance is a bit lacking. I'm especially pleased with how the
character model turned out, I made sprites for all of his limbs and used some sin functions to define the run animation.

# Level editor

![imgur](https://i.imgur.com/SNMVs8l.png)

I also programmed a level editor to allow me to make levels with. Unfortunatly it's pretty bare bones and not user friendly, as I really saw it as a tool more for myself to 
make the levels for the game, and after I was done I wanted more to move onto other projects rather than spend more time polishing this feature.

# Gameplay video 
You can click the image below to see a youtube video of me playing through the game.

[![Gameplay](https://img.youtube.com/vi/n6Bz9d73C-g/0.jpg)](https://www.youtube.com/watch?v=n6Bz9d73C-g)

