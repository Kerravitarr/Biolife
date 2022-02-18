class World {
    map;

    constructor() {
        this.map = new Array(worldMaxHexX);
        for (let index = 0; index < worldMaxHexX; index++) {
            this.map[index] = new Array(worldMaxHexY);
        }
    }

    push(bot) {
        opts.LiveBot++;
        let x = bot.pos.hexagon.x;
        let y = Math.floor(bot.pos.hexagon.y);
        if(x > worldMaxHexX || y > worldMaxHexY)
            console.log("err push X,Y" + x + " " + y);
        this.map[x][y] = bot;
    }
    /**
     * Возвращает объект на координатах
     * @param x Координата в хексонах 
     * @param {*} y Координата в хексонах
     * @returns 
     *      Объект, если он там есть
     *      undefined - если клетка пустая
     *      null - если там стена
     */
    get(x, y) {
        y = Math.floor(y); //Избавляемся от сдвига по y
        if (y < 0 || y > worldMaxHexY) //Если у нас высота ушла - то там стена
            return null;
        if(x > worldMaxHexX)
            console.log("err get X,Y" + x + " " + y);
        return this.map[x][y];
    }
    /**
     * Возвращает объект на координатах
     * @param point Координата
     * @returns 
     *      Объект, если он там есть
     *      undefined - если клетка пустая
     *      null - если там стена
     */
    getP(point) {
        return this.get(point.hexagon.x, point.hexagon.y);
    }
    /**
     * Очищает переданную клетку
     * @param {} point 
     */
    clean(point){
        let y = Math.floor(point.hexagon.y); //Избавляемся от сдвига по y
        if (y < 0 || y > worldMaxHexY) //Если у нас высота ушла - то там стена
            return null;
        if(point.hexagon.x > worldMaxHexX)
            console.log("err get X,Y" + x + " " + y);
        this.map[point.hexagon.x][y] = undefined;
    }

    size() {
        return (worldMaxHexX) * (worldMaxHexY);
    }
}