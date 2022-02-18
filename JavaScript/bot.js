class Bot {
    //Позиция бота в трёх координатах
    pos;
    //Внутренний счётчик процессора
    processorTik = 0;
    //Сознание существа, его мозг
    mind = new Array(opts.MINDE_SIZE);
    //Состояние животного
    alive = LV_STATUS.LV_ALIVE;
    //Жизни
    health = opts.StartHP;
    //Минералы
    mineral = opts.StartMP;
    //Друзья, те, кто рядом с нами - мы один орагнизм!
    friends = new Array(6);
    //Жизни для размножения
    hpForDiv = opts.maxHP;
    //Максимально возможное хранение минералов
    maxMP = opts.maxMP;
    //Показывает на сколько организм тяготеет к фотосинтезу
    photosynthesisEffect = opts.defFotosin;
    //Цвет бота
    color = { red: 255, green: 255, blue: 255 };
    //Направление движения - одно из 6ти значений, где 0 - право
    direction = DIRECTION.UP;
    //Счётчик, показывает ходил бот в этот ход или нет
    stepCount = undefined;

    constructor() {
        this.pos = new Point(Math.random() * w, Math.random() * h);
    }

    step() {

        if (this.alive == LV_STATUS.LV_FREE || this.alive == LV_STATUS.LV_ORGANIC_HOLD || this.alive == LV_STATUS.LV_ORGANIC_SINK) {
            if (botMoveA(this, DIRECTION.DOWN))
                this.alive == LV_STATUS.LV_ORGANIC_SINK;
            else
                this.alive == LV_STATUS.LV_ORGANIC_HOLD;
            return;   //Это труп - выходим!
        }

        let endFor = false;
        for (let cyc = 0; (cyc < 15) && (!endFor); cyc++) {
            let command = this.mind[this.processorTik];  // текущая команда
            switch (command) {
                case undefined:
                case 20: // Фотосинтез
                    this.photosynthesis();
                    this.nextCommand(1);
                    endFor = true;
                    break;
                default:
                    this.nextCommand(command);
                    endFor = true;
                    break;
            }
        }

        //###########################################################################
        //.......  выход из функции и передача управления следующему боту   ........
        //.......  но перед выходом нужно проверить, входит ли бот в        ........
        //.......  многоклеточную цепочку и если да, то нужно распределить  ........
        //.......  энергию и минералы с соседями                            ........
        //.......  также проверить, количество накопленой энергии, возможно ........
        //.......  пришло время подохнуть или породить потомка              ........

        if (this.alive == LV_STATUS.LV_ALIVE) {
            let cells = this.isMulti();
            //Обработка многоклеточности..
            if (this.health > this.hpForDiv) {
                //Диление
                this.botDouble();
            }
            this.health -= 3; //Пожили - устали
            if (this.health < 1) { //Очень жаль, но мы того - всё
                this.bot2Organic();
                return;
            }
            // если бот находится на глубине ниже половины
            // то он автоматом накапливает минералы, но не более 999
            if (this.pos.decart.y > (h / 2)) {
                this.mineral += 1 + (this.pos.decart.y / h * 2 - 1) * (4 - this.photosynthesisEffect); //Эффективный фотосинтез мешает нам переваривать пищу
                this.mineral = Math.max(this.mineral, this.maxMP);
            }
        }
    }
    //=========================================================================================
    //============================       КОД КОМАНД   =========================================
    //=========================================================================================


    // ...  фотосинтез, этой командой забит геном первого бота     ...............
    // ...  бот получает энергию солнца в зависимости от глубины   ...............
    // ...  и количества минералов, накопленных ботом              ...............
    photosynthesis() {
        //Показывает эффективность нашего фотосинтеза
        let t = this.mineral / this.maxMP * this.photosynthesisEffect;
        // формула вычисления энергии ============================= SEZON!!!!!!!!!!
        let hlt = opts.sunPower - (opts.woterDraw * this.pos.decart.y / h) + t;
        if (hlt > 0) {
            this.health = this.health + hlt;   // прибавляем полученную энергия к энергии бота
            this.goGreen(hlt);                      // бот от этого зеленеет
        }
    }

    //=====  превращение бота в органику    ===========
    bot2Organic() {
        this.alive = LV_STATUS.LV_ORGANIC_HOLD;       // Мы теперь орагника
        for (let i = 0; i < this.friends.length; i++) {
            const element = this.friends[i];
            if (element != undefined) {
                element.removeFriend(this);
            }
        }
        opts.LiveBot--; // Мы умерли

        this.color.red = 139;
        this.color.green = 69;
        this.color.blue = 19;
    }

    /** 
     * Мы многоклеточный орагнизм? Если да, то в результате будут биты в тех местах, где есть соседи
     * @returns биты:
     * 				0 - правый
     * 				1 - следующий правее, нижний правый
     * 				2 - нижний левый
     * 				3 - левый
     * 		 /\
     * 	 4	/  \   5
     * 	 3 |    |  0
     * 	 2  \  /  1
     *       \/
     */
    isMulti() {
        let ret = 0;
        for (let i = 0; i < this.friends.length; i++) {
            const element = this.friends[i];
            if (element != undefined)
                ret |= 1 << i;
        }
        return ret;
    }

    //Передвигает счётчик команд на переданное число
    nextCommand(absoluteAdr) {
        let paramadr = absoluteAdr + this.processorTik;
        while (paramadr >= opts.MINDE_SIZE) {
            paramadr = paramadr - opts.MINDE_SIZE;
        }
        this.processorTik = paramadr;
    }

    //Передвигает счётчик команд на число, находящееся через adr от текущего счётчика команд
    indirectCommand(adr) {
        let paramadr = this.processorTik + adr;
        while (paramadr >= opts.MINDE_SIZE) {
            paramadr = paramadr - opts.MINDE_SIZE;
        }
        adr = this.mind[paramadr];
        nextCommand(adr);
    }

    //....................................................................
    // рождение нового бота делением
    botDouble() {
        this.health = this.health - opts.HP_FOR_DOUBLE;      // бот затрачивает 150 единиц энергии на создание копии
        if (this.health <= 0) {
            return;
        }   // если у него было меньше 150, то пора помирать

        let n = findEmptyDirection(this);    // проверим, окружен ли бот
        if (n == undefined) {           	// если бот окружен, то он в муках погибает
            this.health = 0;
            return;
        }

        let newbot = new Bot();
        newbot.pos = n;

        newbot.health = this.health / 2;   // забирается половина здоровья у предка
        this.health /= 2;
        newbot.mineral = this.mineral / 2; // забирается половина минералов у предка
        this.mineral /= 2;

        newbot.color = this.color;   // цвет такой же, как у предка
        newbot.direction = DIRECTION.toEnum(random(0, 6));   // направление, куда повернут новорожденный, генерируется случайно

        for (let i = 0; i < opts.MINDE_SIZE; i++) {  // копируем геном в нового бота
            newbot.mind[i] = this.mind[i];
        }

        newbot.hpForDiv = this.hpForDiv;
        newbot.maxMP = this.maxMP;
        newbot.photosynthesisEffect = this.photosynthesisEffect;

        if (Math.random() < opts.AggressiveEnvironment) {
            newbot.mutation();
        }
        world.push(newbot);
    }

    //Мутация нашего дружка.
    mutation() {
        let del = (1 - Math.random() * 2) * 0.01 + 1;
        switch (random(0, 7)) {
            case 0: //Мутирует количество энергии для диления
                this.hpForDiv *= del;
                break;
            case 1: //Мутирует максимально возможное хранилище минералов
                this.maxMP *= del;
                break;
            case 2: //Мутирует эффективность фотосинтеза
                this.photosynthesisEffect = Math.max(0, Math.min(this.photosynthesisEffect * (del), 4));
                break;
            case 3: //Мутирует геном
                let ma = Math.floor(Math.random() * opts.MINDE_SIZE); //Индекс гена
                let mc = Math.floor(Math.random() * opts.COUNT_COMAND); //Его значение
                this.mind[ma] = mc;
                break;
            case 4: //Мутирует красный цвет
                this.color.red = Math.max(0, Math.min(this.color.red * (del), 255));
                break;
            case 5: //Мутирует зелёный цвет
                this.color.green = Math.max(0, Math.min(this.color.green * (del), 255));
                break;
            case 6: //Мутирует синий цвет
                this.color.blue = Math.max(0, Math.min(this.color.blue * (del), 255));
                break;
        }
    }

    //жжжжжжжжжжжжжжжжжжжхжжжжжжжжжжжжжжжжжжжжжжжжжжжжжжжжжжжжжжжжжжжжжжжжжжж
    //=== делаем бота более зеленым на экране         ======
    //=== in - номер бота, на сколько озеленить       ======
    goGreen(num) {  // добавляем зелени
        this.color.green = Math.min(this.color.green + num, 255);
        let nm = num / 2;
        // убавляем красноту
        this.color.red = this.color.red - nm;
        if (this.color.red < 0) {
            this.color.blue += this.color.red;
        }
        // убавляем синеву
        this.color.blue = this.color.blue - nm;
        if (this.color.blue < 0) {
            this.color.red += this.color.blue;
        }
        this.color.red = Math.max(this.color.red, 0);
        this.color.blue = Math.max(this.color.blue, 0);
    }

    paint() {
        ctx.fillStyle = opts.templateColor.replace('R', this.color.red).replace('G', this.color.green).replace('B', this.color.blue);
        ctx.beginPath();
        ctx.arc(this.pos.decart.x + boardX, this.pos.decart.y + boardY, opts.baseRadius*0.8, 0, 2 * Math.PI);
        ctx.fill();
    }
}
