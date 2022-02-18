let w = c.width = window.innerWidth;
let h = c.height = window.innerHeight;
let ctx = c.getContext('2d');

let opts = {
	LiveBot: 0,
	maxLiveBot: 100,
	//Сколько энергии можно получить находясь прямо под солнцем
	sunPower: 10,
	//На сколько грязная вода
	woterDraw: 1,

	//Радиус клетки и шестиугольника
	baseRadius: 1,
	MINDE_SIZE: 8 * 6,//*6 - так как вокруг каждой клетки 6 соседних клеток
	COUNT_COMAND: 64, //Число возможных команд
	HP_FOR_DOUBLE: 150, //Энергии для диления
	forseMutation: 0.25, //Сила мутации
	StartHP: 5,
	StartMP: 5,
	//Сколько нужно жизней для размножения, по умолчанию
	maxHP: 999,
	//Сколько нужно жизней для размножения, по умолчанию
	maxMP: 999,
	//На сколько организм тяготеет к фотосинтезу (0-4)
	defFotosin: 2,
	//Вероятность того, что произойдёт мутация организма при делении
	AggressiveEnvironment: 0.25,

	templateColor: 'rgba(R,G,B,0.5)',
};
const hexX = opts.baseRadius * Math.sqrt(3);
const hexY = opts.baseRadius * 3 / 2;
const worldMaxX = Math.round(w-hexX);
const worldMaxY = Math.round(h-hexY*2);
const worldMaxHexX = Math.floor(worldMaxX / hexX + 0.5);
const worldMaxHexY = Math.floor(worldMaxY / hexY + 0.5);
const world = new World();
//Борта, верхний и левый
const boardX = hexX;
const boardY = hexY*1.5;
let stepSim = 0;




/*
document.addEventListener("DOMContentLoaded", () => {
	window.wallpaperPropertyListener = {
		applyUserProperties: (properties) => {
			if (properties.particles) opts.particles = properties.particles.value;
			if (properties.orbits) opts.orbits = properties.orbits.value;
			if (properties.particleInertia) opts.particleInertia = Math.pow(10, properties.particleInertia.value);

			setup();
		}
	};

	window.wallpaperRegisterAudioListener((audioArray) => {
		let bass = 0.0;
		let half = Math.floor(audioArray.length / 2);

		for (let i = 0; i < audioArray.length; i++) {
			bass += audioArray[i];
		}
		bass /= audioArray.length;
	});
});*/


function setup() {
	ctx.fillStyle = '#111';
	ctx.fillRect(0, 0, w, h);

	
	for (; opts.LiveBot < opts.maxLiveBot;) {
		world.push(new Bot());
	}

	c.addEventListener('mousemove', function (e) {
		//opts.cx = e.clientX;
		//opts.cy = e.clientY;
		let ret = new Point(e.clientX, e.clientY);
		let bot = world.get(0,0);
		bot.pos = ret;
       console.log(ret.hexagon.x + " " + Math.floor(ret.hexagon.y));
	});
	c.addEventListener('mouseleave', function () {
	});
	//Мы не можем такой метод использовать!!!
	window.addEventListener('resize', function () {
	
		/*w = c.width = window.innerWidth;
		h = c.height = window.innerHeight;
	
		ctx.fillStyle = '#111';
		ctx.fillRect(0, 0, w, h);*/
	})
}

function anim() {

	window.requestAnimationFrame(anim);
	console.log("step " + stepSim);
	redraw();

	var indexses = [];
	for (let y = 0; y < worldMaxHexY; y++) {
		for (let x = 0; x < worldMaxHexX; x++) {
			indexses.push({x:x,y:y});
		}
	}
	//shuffle(indexses);
	stepSim++;
	indexses.forEach(element => {
		let bot = world.get(element.x,element.y);
		if(bot != undefined){
			if(bot.stepCount != stepSim){
				bot.step();
				bot.paint();
				bot.stepCount = stepSim;
			}
		}
	});
}


setup();
anim();



//Переставляет массив в случайном порядке
function shuffle(array) {
	for (let i = array.length - 1; i > 0; i--) {
		let j = Math.floor(Math.random() * (i + 1)); // случайный индекс от 0 до i

		// поменять элементы местами
		// мы используем для этого синтаксис "деструктурирующее присваивание"
		// подробнее о нём - в следующих главах
		// то же самое можно записать как:
		// let t = array[i]; array[i] = array[j]; array[j] = t
		[array[i], array[j]] = [array[j], array[i]];
	}
}
//Обновить экран
function redraw() {

	ctx.fillStyle = '#111';
	ctx.fillRect( 0, 0, w, h );
}