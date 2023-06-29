create table user
(
    id               int unsigned auto_increment primary key,
    email            varchar(80) null,
    username         varchar(80) not null,
    password         char(128)   not null,
    firstname        varchar(80) not null,
    lastname         varchar(80) not null,
    shipping_address text        not null
);

create table auction
(
    id             int unsigned auto_increment            primary key,
    status         enum ('OPEN', 'CLOSED') default 'OPEN' not null,
    deadline       datetime                               not null,
    starting_price decimal(20, 2) unsigned                not null,
    minimum_rise   int unsigned                           not null,
    user_id        int unsigned                           not null,
    won_by         int unsigned                           null,
    final_price    decimal(20, 2) unsigned                null,
    constraint auction_created_by
        foreign key (user_id) references user (id)
            on update cascade on delete cascade,
    constraint auction_won_by
        foreign key (won_by) references user (id)
            on update cascade
);

create table item
(
    code        varchar(20)             not null primary key,
    name        varchar(255)            not null,
    description text                    null,
    image_path  text                    not null,
    price       decimal(10, 2) unsigned not null,
    user_id     int unsigned            not null,
    auction_id  int unsigned            null,
    constraint item_created_by
        foreign key (user_id) references user (id)
            on update cascade on delete cascade,
    constraint item_in_auction
        foreign key (auction_id) references auction (id)
            on update cascade on delete set null
);

create table offer
(
    id         int unsigned auto_increment        primary key,
    user_id    int unsigned                       not null,
    auction_id int unsigned                       not null,
    price      decimal(20, 2) unsigned            not null,
    created_at datetime default CURRENT_TIMESTAMP not null,
    constraint offer_made_by
        foreign key (user_id) references user (id)
            on update cascade,
    constraint offer_made_on
        foreign key (auction_id) references auction (id)
            on update cascade
);

create view winning_offer as
select `aste_online`.`offer`.`auction_id` AS `auction_id`,
       `aste_online`.`offer`.`user_id`    AS `user_id`,
       `aste_online`.`offer`.`price`      AS `price`
from `aste_online`.`offer`
where (`aste_online`.`offer`.`price` >= (select max(`o`.`price`)
                                         from `aste_online`.`offer` `o`
                                         where (`aste_online`.`offer`.`auction_id` = `o`.`auction_id`)));

