# Biolife 3: Differentiation

# Описание:
Приветствую всех неравнодушных!

Для начала хочу выразить благодарность проекту [CyberBiology](https://github.com/CyberBiology/CyberBiology). Именно он стал идейным вдохновителем моего проекта.

Biolife - игра, которая играет сама в себя. Посвящена она моделированию естественного отбора. Поддерживается изменчивость окружающего мира.
В этой игре для человека цель одна – наблюдать, для клеток - выжить. Эта игра показывает, насколько хорошо жизнь адаптируется к любым условиям.
Подробнее об этом можно прочитать в [Википедии](https://ru.wikipedia.org/wiki/%D0%AD%D0%B2%D0%BE%D0%BB%D1%8E%D1%86%D0%B8%D0%BE%D0%BD%D0%BD%D0%BE%D0%B5_%D0%BC%D0%BE%D0%B4%D0%B5%D0%BB%D0%B8%D1%80%D0%BE%D0%B2%D0%B0%D0%BD%D0%B8%D0%B5).

В данный момент вы можете влиять на мир только опосредованно:
- изменять энергию основного и движущегося солнца;
- изменять скорость движущегося солнца;
- изменять прозрачность воды;
- изменять высоту и насыщенность минерализации;
- изменять мутагенность воды;
- стирать с экрана объекты
- записывать gif работы;

Изначально я хотел адаптировать код под javascript. К сожалению, игра выдавала всего 5-10 шагов в секунду. Поэтому я быстро переписал код на Java. Но если вам интересно, вы также можете проверить старый код.
Сейчас на шестиядерной машине и поле 500х200 можно добиться 80-120 ходов в секунду и 20-30 кадров в секунду при минимальной графике.
Количество шагов в секунду от графики не зависит, но можно такую зависимость задать в настройках. Размер мира пропорционален разрешению экрана, причём каждая клетка мира 10 на 10 пикселей

## Планы на следующую версию:
- [ ] Реализовать механику движения многоклеточных организмов
- [ ] Выделение вирусных клеток в отдельные сущности
- [ ] Разработать механику общения между клетками и добавить на основе яда феромоны
- [ ] Разработать механику памяти и математического аппарата для клеток
- [ ] Разработать механику экипировки клетки, получаемую за минералы
- [ ] Создать API через локальную сеть для возможности запуска приложения в фоновом режиме без графического отображения
- [ ] Создать возможность автозапуска приложения в фоновом режиме с автоматической загрузкой последнего сохранения
- [ ] Разработать механику взаимодействия с миром, сохранения, загрузки, редактирования и создания клеток.
- [ ] Разработать больше объектов для карты с возможностью редактирования карты.


# Description:
Greetings to everyone who comes!

I am a self-taught programmer from Russia. And in the beginning I want to express my gratitude to the project [CyberBiology](https://github.com/CyberBiology/CyberBiology). It was he who became my main inspiration!

The project is dedicated to the simulation of natural selection. The variability of the surrounding world is supported.
In this game, there is only one goal - to survive. The player acts as an observer. This game shows how well life adapts.
You can read more about this on [Wikipedia](https://en.wikipedia.org/wiki/Evolutionary_computation).

What has already been implemented:
Two types of energy - the sun and minerals.
The sun is moving and constant.
A rich selection of functions for cells divided into 5 blocks.
Poison. And other and other.

Initially, I wanted to adapt the code for javascript. Unfortunately, the game gave out only 5-10 pulse per second. Therefore, I quickly rewrote the code in Java. But if you are interested, you can also check out the old code.
Now on a six-core machine and a 500x200 field, it is possible to achieve 80-120 moves per second and 20-30 fps.

## Plans for the next version:
- [ ] Develop the mechanics of the movement of multicellular organisms
- [ ] solation of viral cells into separate entities
- [ ] Develop the mechanics of communication between cells and add pheromones
- [ ] Develop the mechanics of memory for cells
- [ ] Develop the mechanics of equipping the cell, which can be obtained for minerals
- [ ] Create an API to run the application in the background without graphics
- [ ] Create autorun in the background with automatic loading of the last save
- [ ] Develop mechanics for interacting with the world. Save, load, edit and create cells.
- [ ] Design more objects for the map. Create the ability to edit the map

# Build:

## Windows:
1. Install JDK and JRE
1. Run Windows_make.bat from /Java folder
1. Launching the application from /Java/jarFile folder
1. Have fun

## Linux:
1. Install JDK and JRE
1. Run in terminal Linux_make.sh from /Java folder
1. Launching the application from /Java/jarFile folder
1. Have fun


# Известные проблемы при сборке и запуске:
1. linux has been compiled by a more recent version of the Java Runtime (class file version 60.0), this version of the Java Runtime only recognizes class file versions up to 55.0
Приложение компилируется на версии Java 16 (она-же Java 60). Если в системе по умолчанию стоит более старая версия, то запустить игру не получится.
Нужно сначала установить нужную версию, а потом системе объяснить какую версию использовать:
- Для Ubuntu и подобных: `sudo update-java-alternatives --set java-1.16.0-openjdk-amd64`. Для установки JRE по умолчанию (вместо openjdk-16-jre можно использовать openjdk-17-jre, openjdk-18-jre и т.д.).
- Для Windows: Необходимо обновить переменную среды _Path_, указав в ней путь к нужней версией Java.
1. В Linux/Ubuntu не отображается большинство символов unicode.
Это значит, что не хватает шрифтов. Установить необходимый пакет можно командой: `sudo apt install ttf-ancient-fonts`.
1. 'javadoc' не распознается как внутренняя или внешняя команда, работающая программа или пакетный файл.

# Startup problems?:
1. linux has been compiled by a more recent version of the Java Runtime (class file version 60.0), this version of the Java Runtime only recognizes class file versions up to 55.0
The application is compiled on Java 16 (Java 60). If the system defaults to an older version, then the game will not run.
You must first install the desired version, and then explain to the system which version to use:
- For Ubuntu/Linux: `sudo update-java-alternatives --set java-1.16.0-openjdk-amd64`. This cmd install the default JRE (you can use openjdk-17-jre, openjdk-18-jre, etc. instead of openjdk-16-jre)
- For Windows: You need to update the _Path_ environment variable to the correct version of Java.
1. Linux/Ubuntu does not display most unicode characters.
This means that there are not enough fonts. You can install the required package with the command: `sudo apt install ttf-ancient-fonts`.