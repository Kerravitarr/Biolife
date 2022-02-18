
const LV_STATUS = Object.freeze({ LV_FREE: 'Место свободно', LV_ORGANIC_HOLD: 'Простая органика', LV_ORGANIC_SINK: 'Тонющая органика', LV_ALIVE: 'Живой' });
//Направления
const DIRECTION = Object.freeze({ UP: '0Вверх', UP_R: '1Верхний правый', DOWN_R: '2Нижний правый', DOWN: '3Нижний', DOWN_L: '4Нижний левый', UP_L: '5Верхний левый' ,
toNum: function(name) {return +name[0];},
toEnum: function(number) {while(number >= 6) number -=6; for (const iterator in this) {if(+this[iterator][0] == number)return this[iterator];}},
});
const OBJECT = Object.freeze({ WALL: 'Стена', CLEAN: 'Пустота', ORGANIC: 'Органика', FRIEND: 'Друг', ENEMY: 'Враг' });