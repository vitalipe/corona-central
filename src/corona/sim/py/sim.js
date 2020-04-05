'use strict';

// var __name__ = "__main__";


function person_dict(loc_x = 0, loc_y = 0, c_status = 0,
                     infection_radius = 0, infection_chance = 0,
                     x_home = 0, y_home = 0, x_dest = 0, y_dest = 0,
                     x_speed = 0, y_speed = 0, detected = 0, infection_timestamp = 0,
                     infected_by = -1, quarantine = 0,idnum = 0) {
    let person = {
        "loc_x": loc_x,
        "loc_y": loc_y,
        "status": c_status,
        "infection_radius": infection_radius,
        "infection_chance": infection_chance,
        "x_home": x_home,
        "y_home": y_home,
        "x_dest": x_dest,
        "y_dest": y_dest,
        "x_speed": x_speed,
        "detected": detected,
        "y_speed": y_speed,
        "infected_by": infected_by,
        "infection_timestamp": infection_timestamp, //day number
        "quarantine": quarantine,
        "idnum":idnum,
    };
    return person
};


function getRandomInt(max) {// up to max, not including
    return Math.floor(Math.random() * Math.floor(max));
}

function get_random_chance() {//0 to 99
    return getRandomInt(100);
}


export function player_quarantine(maparr, population, money_state, level) {// gets called once per level
    //הסגר (בידוד חלקי - אנשים יברחו החוצה בראנדום כלשהו)  (ברק)
    // 3 רמות, ככל שמשקיעים יותר מרוויחים פחות, וההוצאות האחרות עולות יותר, אבל אנשים שומרים על ההסגר יותר, 20%, 40%, 60%
    [maparr, population] = stay_the_fuck_at_home(maparr, population, level);
    money_state["prices_modifier"] += 15;
    money_state["daily_income"] = Math.floor(money_state["daily_income"] * 0.85);

    return [maparr, population, money_state];
};

function getRandomSubarray(arr, size) { // we can also use
    /*
        _und = require('underscore');
    function sample(a, n) {
        return _und.take(_und.shuffle(a), n);
    }
     */
    var shuffled = arr.slice(0), i = arr.length, min = i - size, temp, index;
    while (i-- > min) {
        index = Math.floor((i + 1) * Math.random());
        temp = shuffled[index];
        shuffled[index] = shuffled[i];
        shuffled[i] = temp;
    }
    return shuffled.slice(min);
};


function player_educate_about_hygiene(maparr, population, money_state, level) {// gets called once per level
    let population_under_effect = level * 20;
    var people = [];
    for (var p_status in population) {
        for (var idnum in population[p_status]) {
            people.push([p_status, idnum])
        }
    }
    let amount_of_people_to_check = Math.floor((people.length * population_under_effect) / 100); //how many effected
    var people_to_affect = getRandomSubarray(people, amount_of_people_to_check);
    for (let i = 0; i < people_to_affect.length; i++) {
        let [p_status, idnum] = people[i];
        population[p_status][idnum]['infection_chance'] *= 0.5 // 20 -> 10 -> 5 -> 2.5
    }

    money_state["prices_modifier"] += 15;
    money_state["daily_income"] = Math.floor(money_state["daily_income"] * 0.85);

    return [maparr, population, money_state];
};


function player_keep_distance(maparr, population, money_state, level) {// gets called once per level
    let population_under_effect = level * 25;
    var people = [];
    for (var p_status in population) {
        for (var idnum in population[p_status]) {
            people.push([p_status, idnum])
        }
    }
    let amount_of_people_to_check = Math.floor((people.length * population_under_effect) / 100); //how many effected
    var people_to_affect = getRandomSubarray(people, amount_of_people_to_check);
    for (let i = 0; i < people_to_affect.length; i++) {
        let [p_status, idnum] = people[i];
        population[p_status][idnum]['infection_radius'] -= 1 // 2 -> 1 -> 0 -> -1
    }
    money_state["prices_modifier"] += 15;
    money_state["daily_income"] = Math.floor(money_state["daily_income"] * 0.85);

    return [maparr, population, money_state];
};


function stay_the_fuck_at_home(maparr, population, level) {
    let population_under_effect = level * 20;
    var people = new Array();
    for (var p_status in population) {
        // people_count += Object.keys(population[p_status]).length;
        for (var idnum in population[p_status]) {
            people.push([p_status, idnum]); //collect possible people to check
        }
    }
    let amount_of_people_to_check = Math.floor((people.length * population_under_effect) / 100); //how many people are checked
    var rand_array_id = getRandomInt(people.length);
    var checked = new Set();
    for (var i = 0; i < amount_of_people_to_check; i++) {  // will run for the amount of people needed
        while (checked.has(rand_array_id)) {
            rand_array_id = getRandomInt(people.length);
        }
        checked.add(rand_array_id);
        let [p_status, idnum] = people[rand_array_id];
        if (get_random_chance() < 90) {
            population[p_status][idnum]["quarantine"] = 1;
            let new_x = population[p_status][idnum]["x_home"];
            let new_y = population[p_status][idnum]["y_home"];
            [maparr, population] = move_person(maparr, population, idnum, p_status, new_x, new_y);
        }

    }
    return [maparr, population];
};

export function player_detect_infecteds(maparr, population, level) {// gets called regularly
    //בדיקות בשביל לבודד חולים (בידוד) (ברק)
    // 10 רמות, כל רמה היא 3% מהאוכלסייה שעוברת בדיקות, הבחירה היא ראנדומית (הבדיקות קורות כל יומיים), אם חולה זוהה כחולה, detected = true, והוא לא עובר בדיקה יותר (ומוכנס לבידוד)
    if (level === 0) {
        return population;
    }
    ;
    let population_percent_to_check = Math.max(level * 3, 30);
    var people = new Array(); //array with idnums of people that could be checked
    for (var p_status in population) {
        // people_count += Object.keys(population[p_status]).length;
        for (var idnum in population[p_status]) {
            if (population[p_status][idnum]["detected"] != 1) // not detected
                people.push([p_status, idnum]); //collect possible people to check
        }
    }
    let amount_of_people_to_check = Math.floor((people.length * population_percent_to_check) / 100); //how many people are checked
    var checked = new Set(); // set
    var rand_array_id = getRandomInt(people.length);
    for (var i = 0; i < amount_of_people_to_check; i++) {  // will run for the amount of people needed
        while (checked.has(rand_array_id)) {
            rand_array_id = getRandomInt(people.length);
        }
        checked.add(rand_array_id);
        let [p_status, idnum] = people[rand_array_id];
        if (p_status === "i") {
            population[p_status][idnum]["detected"] = 1;
            population[p_status][idnum]["quarantine"] = 1;
            let new_x = population[p_status][idnum]["x_home"];
            let new_y = population[p_status][idnum]["y_home"];
            [maparr, population] = move_person(maparr, population, idnum, p_status, new_x, new_y);
        }
    }
    return [maparr, population];
};

function move_person(maparr, population, idnum, p_status, new_x, new_y) {//will break if x and y are out of bounds
    let cur_x = population[p_status][idnum]["loc_x"];
    let cur_y = population[p_status][idnum]["loc_y"];

    maparr[new_y][new_x][idnum] = maparr[cur_y][cur_x][idnum];
    delete maparr[cur_y][cur_x][idnum];

    population[p_status][idnum]["loc_x"] = new_x;
    population[p_status][idnum]["loc_y"] = new_y;

    return [maparr, population]
};

function _is_pos_in_map(y, x, max_y, max_x) {
    return (((max_x > x) && (x > 0)) && ((max_y > y) && (y > 0)))
};

function _get_next_position(y, x, y_speed, x_speed) {
    return [y + y_speed, x + x_speed];
};

function get_surrounding_people(maparr, y, x, r, max_y, max_x) {
    var people_loc = new Set();
    for (var i = x - r; i < x + r + 1; i++)
        for (var j = y - r; j < y + r + 1; j++)
            if (_is_pos_in_map(j, i, max_y, max_x) && (i != x || j != y))
                //Object.keys(maparr[j][i]).length
                if (Object.keys(maparr[j][i]).length > 0)
                    people_loc.add([j, i]);
    return people_loc
}
;

function get_new_destination(max_y, max_x) {
    var x = getRandomInt(max_x);

    var y = getRandomInt(max_y);
    return [y, x];
}
;

function get_surroundings(y, x, r, max_y, max_x) {
    var dots = new Set();
    for (var i = x - r; i < x + r + 1; i++)
        for (var j = y - r; j < y + r + 1; j++)
            if (_is_pos_in_map(j, i, max_y, max_x) && (i != x || j != y))
                dots.add([j, i]);
    return dots
}
;

function* generate_person(infection_in_population, max_y, max_x, starting_idnum = 0) {
    var idnum = starting_idnum;
    var infecteds = 0;
    while (true) {
        var [y_dest, x_dest] = get_new_destination(max_y, max_x);
        var [y, x] = get_new_destination(max_y, max_x);

        var x_speed = x < x_dest ? 1 : -1;
        var y_speed = y < y_dest ? 1 : -1;
        var infection_radius = 1;
        var p_status = "s";
        if (infection_in_population >= 1) {
            if (infecteds < infection_in_population) p_status = "i"
        } else if (infection_in_population < 1) if (get_random_chance() < infection_in_population * 100) var status = "i";
        if (p_status == "i") {
            infecteds++;
            infection_radius = 2
        }
        var person = person_dict(x, y, p_status, infection_radius, 0, x, y,
            x_dest, y_dest, x_speed, y_speed, 0, 0, -1, 0);
        // var person = person_dict(__kwargtrans__({
        //         "loc_x": x, "loc_y": y, "x_home": x, "y_home": y, "x_dest": x_dest, "y_dest": y_dest, "x_speed": x_speed,
        //         "y_speed": y_speed, "status": p_status, "idnum": idnum, "infection_radius": infection_radius
        //     }
        // ));
        idnum++;
        yield person
    }
}
;

function recovery(population, current_day) {//to be called after week 4 and every week afterwards
    let infected_status = "i";
    let recovered_status = "r";
    for (var idnum in population[infected_status]) {
        let person = population[infected_status][idnum];
        let infection_day = person["infection_timestamp"];
        if (((infection_day - current_day) / 7) > 4) { //4weeks have passed
            if (get_random_chance() < 75) { // 75% recovery chance
                population[recovered_status][idnum] = population[infected_status][idnum];
                delete population[infected_status][idnum];
            }
        }
    }
    return population;
}

function try_to_infect(infected_person, person) {
    var actual_radius = infected_person["infection_radius"] + person["infection_radius"];
    if (actual_radius > 0) {
        var infection_chance = (infected_person["infection_chance"] + person["infection_chance"]) / 2;
        var success = infection_chance > get_random_chance();
        if (success) return true
    }
    ;
    return false
}
;

function infection_spreading(maparr, population, max_y, max_x, current_day) {
    var newly_infecteds = new Set();
    //Object.values(population["i"])
    for (var id_num in population["i"]) {
        var infected_person = population["i"][id_num];
        var people_locs = get_surrounding_people(maparr,
            infected_person["loc_y"],
            infected_person["loc_x"],
            infected_person["infection_radius"],
            max_y,
            max_x
            )
        ;
        for (var [y, x] of people_locs) for (var idnum of maparr[y][x]) {
            var person = maparr[y][x][idnum];
            if (person["status"] == "s") {
                var infection_success = try_to_infect(infected_person, person);
                if (infection_success) {
                    newly_infecteds.add(person["idnum"]);
                    person["infected_by"] = infected_person["idnum"]
                    person["infection_timestamp"] = current_day;
                }
            }
        }
    }
    for (var idnum of newly_infecteds) {
        population["i"][idnum] = population["s"][idnum];
        delete population["s"][idnum];
        population["i"][idnum]["status"] = "i";
        // maparr[population["i"][idnum]["loc_y"]][population["i"][idnum]["loc_x"]]["status"] = "i"
    }
    return [maparr, population];
}
;


function move_one_step(maparr, population, max_y, max_x) {
    for (var p_status of population)
        for (var idnum in population[p_status]) {
            var person = population[p_status][idnum];
            if (!person["quarantine"]) {
                var y = person["loc_y"];
                var x = person["loc_x"];
                var y_speed = person["y_speed"];
                var x_speed = person["x_speed"];
                var y_dest = person["y_dest"];
                var x_dest = person["x_dest"];
                var [new_y, new_x] = _get_next_position(y, x, y_speed, x_speed);

                if (new_x == x_dest) var new_x_speed = 0;
                else if (Math.abs(new_x - x_dest) >= Math.abs(x - x_dest)) var new_x_speed = -1 * x_speed;
                else var new_x_speed = x_speed;
                if (new_y == y_dest) var new_y_speed = 0;
                else if (Math.abs(new_y - y_dest) >= Math.abs(y - y_dest)) var new_y_speed = -1 * y_speed;
                else var new_y_speed = y_speed;
                if (new_x == x_dest && new_y == y_dest) {
                    var [new_y_dest, new_x_dest] = get_new_destination(max_y, max_x);

                    var new_x_speed = x < new_x_dest ? 1 : -1;
                    var new_y_speed = y < new_y_dest ? 1 : -1;

                    population[p_status][idnum]["x_speed"] = new_x_speed;
                    population[p_status][idnum]["y_speed"] = new_y_speed;
                    population[p_status][idnum]["x_dest"] = new_x_dest;
                    population[p_status][idnum]["y_dest"] = new_y_dest;

                }
                if (_is_pos_in_map(new_y, new_x, max_y, max_x)) {
                    [maparr, population] = move_person(maparr, population, idnum, p_status, new_x, new_y);
                    population[p_status][idnum][x_speed] = new_x_speed;
                    population[p_status][idnum][y_speed] = new_y_speed;
                }
            }
        }
    return [maparr, population];
}
;

function create_maparr(ylen, xlen) {
    var map_arr = [];
    for (let y = 0; y < ylen; y++) {
        map_arr[y] = []; // create nested array
        for (let x = 0; x < xlen; x++) {
            map_arr[y][x] = {}; // put empty dict
        }
    }
    return map_arr;
}
;

function initial_investments_state() {
    let investments = {
        "quarantine": 0,
        "infection_checks": 0,
        "hygiene_education": 0,
        "distance_education": 0, // percents
    };
    return investments;
};


function initial_money_state() {
    let money_state = {
        "current_holdings": 10000,
        "daily_income": 10000,
        "daily_spendings": 100,
        "prices_modifier": 100, // percents
    };
    return money_state;
};

function populate_world(maparr, population_size, infection_in_population) {
    var population = {
        "s": {},
        "i": {},
        "r": {}
    };
    var people_generator = generate_person(infection_in_population, maparr.length, maparr[0].length);

    for (var i = 0; i < population_size; i++) {
        var person = people_generator.next().value;
        maparr[person["loc_y"]][person["loc_x"]][person["idnum"]] = person;
        population[person["status"]][person["idnum"]] = person
    }
    return [maparr, population]
}
;

export function construct(ylen, xlen, population_size, infection_in_population) {//first call to init the world
    var maparr = create_maparr(ylen, xlen);
    var __left0__ = populate_world(maparr, population_size, infection_in_population);
    var population = __left0__[1];
    maparr = __left0__[0];
    var money_state = initial_money_state();
    var investments_state = initial_investments_state();
    return [maparr, population, money_state, investments_state];
}
;


function main_loop(maparr, population, current_day) {
    let max_x = maparr[0].length;
    let max_y = maparr.length;

    [maparr, population] = infection_spreading(maparr, population, max_y, max_x, current_day);
    [maparr, population] = move_one_step(maparr, population, max_y, max_x);

    return [maparr, population];
}


export function time_to_simulate(maparr, population, money_state, investments, current_day, time_passed) {
    let time_to_iterations_ratio = 2400; // magic number IDK
    let iterations_per_day = 2400; // magic number IDK

    let iterations = time_passed * time_to_iterations_ratio;
    let days = time_passed;

    for (var i = 0; i <= iterations; i++) {
        [maparr, population] = main_loop(maparr, population, current_day);
        if (i % (iterations_per_day * 2) === 0) {
            [maparr, population] = player_detect_infecteds(maparr, population, investments["detect_infecteds"]);
        }
    }
    return [maparr, population, money_state, investments]
}

function repopulate_world(maparr, population, population_size, infection_in_population, starting_idnum) {
    //need to pass population size somewhere to keep track of idnum so it wont overlap
    // population size is how many people are added
    var people_generator = generate_person(infection_in_population, maparr.length, maparr[0].length, starting_idnum);

    for (var p_status in population) { //repopulate existing population
        for (var idnum in population[p_status]) {
            person = population[p_status][idnum];
            maparr[person["loc_y"]][person["loc_x"]][person["idnum"]] = person;
        }
    }

    for (var i = 0; i < population_size; i++) { //add new people to population
        var person = people_generator.next().value;
        // var person = {};
        maparr[person["loc_y"]][person["loc_x"]][person["idnum"]] = person;
        population[person["status"]][person["idnum"]] = person;
    }

    return [maparr, population];
}
;


export function next_level(maparr, population, money_state, investments, population_size, infection_in_population, starting_idnum) {
    //making a larger array, adding people etc
    let lvl_modifier = 1.25;
    let max_x = Math.floor(maparr[0].length * lvl_modifier);
    let max_y = Math.floor(maparr.length * lvl_modifier);
    var new_maparr = create_maparr(max_y, max_x);
    [new_maparr, population] = repopulate_world(new_maparr, population, population_size, infection_in_population, starting_idnum);
    money_state["daily_income"] = money_state["daily_income"] * lvl_modifier;
    population = recovery(population);
    return [new_maparr, population, money_state, investments];
}
