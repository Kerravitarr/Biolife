# Biolife
Greetings to everyone who comes!

I am a self-taught programmer from Russia. And in the beginning I want to express my gratitude to the project CyberBiology - github.com/CyberBiology/CyberBiology. It was he who became my main inspiration!

The project is dedicated to the simulation of natural selection. The variability of the surrounding world is supported.
In this game, there is only one goal - to survive. The player acts as an observer. This game shows how well life adapts.
You can read more about this on Wikipedia: en.wikipedia.org/wiki/Evolutionary_computation

What has already been implemented:
Two types of energy - the sun and minerals.
The sun is moving and constant.
A rich selection of functions for cells divided into 5 blocks.
Poison. But for now I don't like it.

Initially, I wanted to adapt the code for javascript. Unfortunately, the game gave out only 5-10 moves per second. Therefore, I quickly rewrote the code in Java. But if you are interested, you can also check out the old code.
Now on a six-core machine and a 500x200 field, it is possible to achieve 80-120 moves per second and 2-5 fps.

BUILDING

Currently only available for Windows. I promise to add Linux support soon.

Windows:
1. Install JDK and JRE
2. Run WindowsMake.bat
3. Launching the application from the /build
4. Have fun
