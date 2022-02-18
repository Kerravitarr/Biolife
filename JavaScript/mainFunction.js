//=================ВСПОМОГАТЕЛЬНЫЕ ФУНКЦИИ
/**
 * Отдаёт следующие координаты относительно текущего смотра бота
 * @param {*} bot - кто в центре
 * @param {*} n - на сколько повернуть голову (+ по часовой)
 * @returns Координаты следующей точки в этом направлении
 */
function fromVektorR(bot, n) {
    return fromVektorA(bot, DIRECTION.toNum(bot.direction) + n);
}
/**
 * Отдаёт следующие координаты относительно глобальных координат
 * @param {*} bot - кто в центре
 * @param {*} n - на сколько повернуть голову (+ по часовой)
 * @returns Координаты следующей точки в этом направлении
 */
function fromVektorA(bot, n) {
    let point = bot.pos.clone();
    let dir = DIRECTION.toEnum(n);
    return point.next(dir);
}

//жжжжжжжжжжжжжжжжжжжхжжжжжжжжжжжжжжжжжжжжжжжжжжжжжжжжжжжжжжжжжжжжжжжжжжж
/**
 * Ищет свободные ячейки вокруг бота.
 * Сначала прямо, потом лево право, потом зад лево/право и наконец назад
 * @param bot - тот, кто в центре
 * @returns Point свободной ячейки или undefined
 */
function findEmptyDirection(bot) {
    for (let i = 0; i < 4; i++) {
        let point = fromVektorR(bot, i);
        let obj = world.getP(point);
        if (obj === undefined)
            return point;
        if (i != 0 && i != 3) {
            let point = fromVektorR(bot, -i);
            let obj = world.getP(point);
            if (obj === undefined)
                return point;
        }
    }
    return undefined;
}

/**
 * Подглядывает за бота в абсолютном направлении
 * @param {*} bot - бот 
 * @param {*} direction направление, DIRECTION
 * @returns параметры OBJECT
 */
function botSeeA(bot, direction) {
    let point = fromVektorA(bot, DIRECTION.toNum(direction));
    let obj = world.getP(point);
    if (obj === null)
        return OBJECT.WALL;
    else if (obj === undefined)
        return OBJECT.CLEAN;
    else if (obj.alive == LV_STATUS.LV_ORGANIC_HOLD || obj.alive == LV_STATUS.LV_ORGANIC_SINK)
        return OBJECT.ORGANIC;
    else if (isRelative(obj, bot))
        return OBJECT.FRIEND;
    else
        return OBJECT.ENEMY;
}

/**
 * Перемещает бота в абсолютном направлении
 * @param {*} bot - бот 
 * @param {*} direction направление, DIRECTION
 * @returns true, если сдвинулись
 */
function botMoveA(bot, direction) {
    if(botSeeA(bot,direction) == OBJECT.CLEAN){
        let point = fromVektorA(bot, DIRECTION.toNum(direction));
        world.clean(bot.pos);
        bot.pos = point;
        world.push(bot);
        return true
    } 
    return false
}

//жжжжжжжжжжжжжжжжжжжхжжжжжжжжжжжжжжжжжжжжжжжжжжжжжжжжжжжжжжжжжжжжжжжжжжж
//========   родственники ли боты?              =====
//========   in - номер 1 бота , номер 2 бота   =====
//========   out- 0 - нет, 1 - да               =====
/**
 * Родственные-ли боты?
 * Вообще пока смотрит только по ДНК, но возможно в будущем нужно смотреть будет не на 
 *      генотип, а на фенотип!
 * @param {*} bot0 
 * @param {*} bot1 
 * @returns 
 */
function isRelative(bot0, bot1) {
    if (bot0.alive != LV_STATUS.LV_ALIVE || bot1.alive != LV_STATUS.LV_ALIVE) {
        return false;
    }
    let dif = 0;    // счетчик несовпадений в геноме
    for (let i = 0; i < opts.MINDE_SIZE; i++) {
        if (bot0.mind[i] != bot1.mind[i]) {
            dif = dif + 1;
            if (dif == 2) {
                return false;
            } // если несовпадений в генеме больше 1
        }                               // то боты не родственики
    }
    return true;
}

/**
 * Возвращает случайное число
 * @param {} min - минимальное число, включительно
 * @param {*} max - максимальное число, не включительно
 * @returns 
 */
function random(min, max) {
    min = Math.ceil(min);
    max = Math.floor(max);
    return Math.floor(Math.random() * (max - min)) + min; //Максимум не включается, минимум включается
}
