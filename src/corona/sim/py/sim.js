'use strict';
import {__kwargtrans__, abs, dict, len, py_next, set, tuple} from "./runtime.js";

var __name__ = "__main__";


function person_dict(loc_x = 0, loc_y = 0, c_status = 0,
                     infection_radius = 0, infection_chance = 0,
                     x_home = 0, y_home = 0, x_dest = 0, y_dest = 0,
                     x_speed = 0, y_speed = 0, detected = 0, infection_timestamp = 0,
                     infected_by = -1, quarantine = 0) {
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
        "infection_timestamp":infection_timestamp, //day number
        "quarantine": quarantine,
    };
    return person
};


function getRandomInt(max) {// up to max, not including
    return Math.floor(Math.random() * Math.floor(max));
}

function get_random_chance() {//0 to 99
    return getRandomInt(100);
}


function player_quarantine(maparr, population, money_state, level) {// gets called once per level
    //הסגר (בידוד חלקי - אנשים יברחו החוצה בראנדום כלשהו)  (ברק)
    // 3 רמות, ככל שמשקיעים יותר מרוויחים פחות, וההוצאות האחרות עולות יותר, אבל אנשים שומרים על ההסגר יותר, 20%, 40%, 60%
    [maparr, population] = stay_the_fuck_at_home(maparr, population, level);
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
}

function player_detect_infecteds(maparr, population, level) {// gets called regularly
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
        if (p_stats === "i") {
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

    maparr[new_y][new_x][idnum] = maparr[cur_y][cur_x].py_pop(idnum);

    population[p_status][idnum]["loc_x"] = new_x;
    population[p_status][idnum]["loc_y"] = new_y;

    return [maparr, population]
};

export var _is_pos_in_map = function (y, x,
                                      max_y, max_x) {
        return max_x > x && x > 0 && (max_y > y && y > 0)
    }
;
export var _get_next_position = function (y, x, y_speed, x_speed) {
        return tuple([y + y_speed, x + x_speed])
    }
;

export var get_surrounding_people = function (y, x, r, max_y, max_x, maparr) {
        var people_loc = set();
        for (var i = x - r; i < x + r + 1; i++)
            for (var j = y - r; j < y + r + 1; j++)
                if (_is_pos_in_map(j, i, max_y, max_x) && (i != x || j != y)) if (len(maparr[j][i]) > 0) people_loc.add(tuple([j, i]));
        return people_loc
    }
;
export var get_new_destination = function (max_y, max_x) {
        var x = getRandomInt(max_x);

        var y = getRandomInt(max_y);
        return tuple([y, x])
    }
;
export var get_surroundings = function (y, x, r, max_y, max_x) {
        var dots = set();
        for (var i = x - r; i < x + r + 1; i++)
            for (var j = y - r; j < y + r + 1; j++)
                if (_is_pos_in_map(j, i, max_y, max_x) && (i != x || j != y)) dots.add(tuple([j, i]));
        return dots
    }
;

export var generate_person = function* (infection_in_population, max_y, max_x, starting_idnum = 0) {
        var idnum = starting_idnum;
        var infecteds = 0;
        while (true) {
            var __left0__ = get_new_destination(__kwargtrans__({
                    max_y: max_y, max_x: max_x
                }
            ));
            var y_dest = __left0__[0];
            var x_dest = __left0__[1];
            var __left0__ = get_new_destination(__kwargtrans__({
                    max_y: max_y,
                    max_x: max_x
                }
            ));
            var y = __left0__[0];
            var x = __left0__[1];
            var x_speed = x < x_dest ? 1 : -1;
            var y_speed = y < y_dest ? 1 : -1;
            var infection_radius = 1;
            var status = "s";
            if (infection_in_population >= 1) {
                if (infecteds < infection_in_population) var status = "i"
            } else if (infection_in_population < 1) if (get_random_chance() < infection_in_population * 100) var status = "i";
            if (status == "i") {
                infecteds++;
                infection_radius = 2
            }
            var person = person_dict(__kwargtrans__(dict({
                    "loc_x": x, "loc_y": y, "x_home": x, "y_home": y, "x_dest": x_dest, "y_dest": y_dest, "x_speed": x_speed,
                    "y_speed": y_speed, "status": status, "idnum": idnum, "infection_radius": infection_radius
                }
            )));
            idnum++;
            yield person
        }
    }
;

function recovery(population, current_day){//to be called after week 4 and every week afterwards
    let infected_status = "i";
    let recovered_status = "r";
    for (var idnum in population[infected_status]){
        let person = population[infected_status][idnum]
        let infection_day = person["infection_timestamp"]
        if (((infection_day - current_day)/7) > 4 ){ //4weeks have passed
            if (get_random_chance()<75){ // 75% recovery chance
                population[recovered_status][idnum] = population[infected_status].py_pop(idnum)
            }
        }
    }
    return population;
}


export var try_to_infect = function (infected_person, person) {
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
export var infection_spreading = function (maparr, population, max_y, max_x, current_day) {
        var newly_infecteds = set();
        for (var infected_person of population["i"].py_values()) {
            var people_locs = get_surrounding_people(__kwargtrans__({
                    x: infected_person["loc_x"],
                    y: infected_person["loc_y"],
                    r: infected_person["infection_radius"],
                    max_y: max_y,
                    max_x: max_x,
                    maparr: maparr
                }
            ));
            for (var [y, x]of people_locs) for (var idnum of maparr[y][x]) {
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
            population["i"][idnum] = population["s"].py_pop(idnum);
            population["i"][idnum]["status"] = "i";
            // maparr[population["i"][idnum]["loc_y"]][population["i"][idnum]["loc_x"]]["status"] = "i"
        }
        return tuple([maparr, population])
    }
;

export var move_one_step = function (maparr, population, max_y, max_x) {
        for (var p_status of population) for (var [idnum, person]of population[p_status].py_items()) if (!person["quarantine"]) {
            var y = person["loc_y"];
            var x = person["loc_x"];
            var y_speed = person["y_speed"];
            var x_speed = person["x_speed"];
            var y_dest = person["y_dest"];
            var x_dest = person["x_dest"];
            var __left0__ = _get_next_position(__kwargtrans__({
                    y: y, x: x, y_speed: y_speed, x_speed: x_speed
                }
            ));
            var new_y = __left0__[0];

            var new_x = __left0__[1];
            if (new_x == x_dest) var new_x_speed = 0;
            else if (abs(new_x - x_dest) >= abs(x - x_dest)) var new_x_speed = -1 * x_speed;
            else var new_x_speed = x_speed;
            if (new_y == y_dest) var new_y_speed = 0;
            else if (abs(new_y - y_dest) >= abs(y - y_dest)) var new_y_speed = -1 * y_speed;
            else var new_y_speed = y_speed;
            if (new_x == x_dest && new_y == y_dest) {
                var __left0__ = get_new_destination(__kwargtrans__({
                        max_y: max_y, max_x: max_x
                    }
                ));
                var new_y_dest = __left0__[0];
                var new_x_dest = __left0__[1];
                var new_x_speed = x < new_x_dest ? 1 : -1;
                var new_y_speed =
                    y < new_y_dest ? 1 : -1;
                var pu = dict({
                        "x_speed": new_x_speed, "y_speed": y < new_y_dest ? 1 : -1, "x_dest": new_x_dest, "y_dest": new_y_dest
                    }
                );
                population[p_status][idnum].py_update(pu)
            }
            if (_is_pos_in_map(__kwargtrans__({
                    y: new_y, x: new_x, max_y: max_y, max_x: max_x
                }
            ))) {
                // maparr[new_y][new_x][person["idnum"]] = maparr[y][x].py_pop(person["idnum"]); // TODO: use new function move_person
                var pu = dict({"x_speed": new_x_speed, "y_speed": new_y_speed});
                [maparr, population] = move_person(maparr, population, idnum, p_status, new_x, new_y);
                population[p_status][idnum].py_update(pu)
            }
        }
        return tuple([maparr, population])
    }
;
export var create_maparr = function (ylen, xlen) {
        return new Array(ylen).fill(new Array(xlen).fill(dict({})));
        // return np.full([ylen, xlen], dict({}))
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

export var populate_world = function (maparr, population_size, infection_in_population) {
        var population = dict({
                "s": dict({}
                ), "i": dict({}
                ), "r": dict({}
                )
            }
        );
        var people_generator = generate_person(infection_in_population, __kwargtrans__({
                max_x: maparr[0].length, max_y: maparr.length
            }
        ));

        for (var i = 0; i < population_size; i++) {
            var person = py_next(people_generator);
            maparr[person["loc_y"]][person["loc_x"]][person["idnum"]] = person;
            population[person["status"]][person["idnum"]] = person
        }
        return [maparr, population]
    }
;
export var construct = function (ylen,
                                 xlen, population_size, infection_in_population) {//first call to init the world
        var maparr = create_maparr(ylen, xlen);
        var __left0__ = populate_world(maparr, population_size, infection_in_population);
        var population = __left0__[0];
        maparr = __left0__[1];
        var money_state = initial_money_state();
        var investments_state = initial_investments_state();
        return tuple([maparr, population, money_state, investments_state])
    }
;


function main_loop(maparr, population, current_day) {
    let max_x = maparr[0].length;
    let max_y = maparr.length;

    [maparr, population] = infection_spreading(maparr, population, max_y, max_x, current_day);
    [maparr, population] = move_one_step(maparr, population, max_y, max_x);

    return [maparr, population];
}


function time_to_simulate(maparr, population, money_state, investments, current_day, time_passed) {
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

export var repopulate_world = function (maparr, population, population_size, infection_in_population, starting_idnum) {
        //need to pass population size somewhere to keep track of idnum so it wont overlap
        // population size is how many people are added
        var people_generator = generate_person(infection_in_population, maparr.length, maparr[0].length, starting_idnum);

        for (var p_status in population) { //repopulate existing population
            for (var idnum in population[p_status]) {
                person = population[p_status][idnum]
                maparr[person["loc_y"]][person["loc_x"]][person["idnum"]] = person;
            }
        }

        for (var i = 0; i < population_size; i++) { //add new people to population
            var person = py_next(people_generator);
            maparr[person["loc_y"]][person["loc_x"]][person["idnum"]] = person;
            population[person["status"]][person["idnum"]] = person
        }

        return [maparr, population]
    }
;


function next_level(maparr, population, money_state, investments, population_size, infection_in_population, starting_idnum) {
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
