/**
 * Координаты объекта
 */
class Point {
    //В системе х,у
    decart = { x: 0, y: 0 };
    //В системе решётки, где х - номер ячейки по горизонтали
    hexagon = { x: 0, y: 0 };

    /**
     * Создаёт точку пространтсва
     * @param {*} x - координата в декартовом пространтсве!
     * @param {*} y - координата в декартовом пространтсве!
     */
    constructor(x = 0, y = 0) {
        this.setHex({ x: x, y: y });
        this.setDec(this.hexagon);
    }

    setHex(decadt) {
        if (decadt.x < 0 || decadt.x > worldMaxX-hexX) {
            while (decadt.x < 0)
                decadt.x += worldMaxX;
            while (decadt.x > worldMaxX)
                decadt.x -= worldMaxX;
        }
        decadt.y = Math.max(0,Math.min(decadt.y,worldMaxY-hexY))
        this.hexagon.x = Math.floor(decadt.x / hexX + 0.5);
        this.hexagon.y = Math.floor(decadt.y / hexY + 0.5);
        if (this.hexagon.x % 2 == 1)
            this.hexagon.y = this.hexagon.y + 0.5;
    }
    setDec(hexagon) {
        this.decart.x = hexagon.x * hexX;
        this.decart.y = hexagon.y * hexY;
    }

    //Сдвигает на одну позицию в нужную сторону
    next(derection) {
        switch (derection) {
            case DIRECTION.UP:
                this.decart.y -= hexY;
                break;
            case DIRECTION.UP_R:
                this.decart.x += hexX;
                this.decart.y -= hexY / 2;
                break;
            case DIRECTION.DOWN_R:
                this.decart.x += hexX;
                this.decart.y += hexY / 2;
                break;
            case DIRECTION.DOWN:
                this.decart.y += hexY;
                break;
            case DIRECTION.DOWN_L:
                this.decart.x -= hexX;
                this.decart.y += hexY / 2;
                break;
            case DIRECTION.UP_L:
                this.decart.x -= hexX;
                this.decart.y -= hexY / 2;
                break;
        }
        this.setHex(this.decart);
        this.setDec(this.hexagon);
        return this;
    }

    clone() {
        return new Point(this.decart.x, this.decart.y)
    }
}