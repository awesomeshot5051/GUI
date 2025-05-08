use userDatabase;

delimiter //
create procedure updateExpirationTime(
in p_username varchar(255),
in time int
)
begin
	declare user_id int;
    call getID(p_username,user_id);
	update passwordexpiration set expires_after_days=time where id=user_id;
end //
delimiter ;
call getUserStatus("n01520193","Trae");
select * from passwordExpiration;
delimiter //
create procedure getPasswordExpiration(
in p_username varchar(255),
in p_password varchar(255))
begin
	declare user_id int;
    select id into user_id from users where username=p_username and password=p_password;
    select nextChangedDate from passwordExpiration where userID=user_id;
end //
delimiter ;

delimiter //

CREATE DEFINER=`root`@`localhost` PROCEDURE `getUID`(
in p_username varchar(255),
in p_name varchar(255),
out user_id int
)
begin
select id from users where username=p_username into user_id;
end //
delimiter ;
select * from users;
select * from passwordExpiration;