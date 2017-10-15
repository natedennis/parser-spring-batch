# mvn clean compile package install

# java -jar "target/parser.jar" --duration=hourly --startDate="2017-01-01.13:00:00" --threshold=100  --accesslog=src/main/resources/access.log



current configuration is set for mysql 5.7 running on localhost:3306 using user root with no password. 

for easy of discovery, the ddl for the datalayer

create database parser;
CREATE TABLE parser.`access_log` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `access_date` datetime NOT NULL,
  `ip` varchar(15) NOT NULL,
  `request` varchar(2050) NOT NULL,
  `status` int(10) NOT NULL,
  `user_agent` varchar(512) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `ip` (`ip`),
  KEY `access_date` (`access_date`)
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8;



SQL
---

(1) Write MySQL query to find IPs that mode more than a certain number of requests for a given time period.

    mysql> select a.ip from access_log a where a.access_date>='2017-01-01 13:00:00.0' and a.access_date<'2017-01-01 14:00:00.0' group by a.ip having count(distinct a.id)>100

(2) Write MySQL query to find requests made by a given IP.
  
    mysql> select a.request from access_log a where a.ip='192.168.97.7' and  a.access_date>='2017-01-01 13:00:00.0' and a.access_date<'2017-01-01 14:00:00.0' ;
 	

but more importantly

CREATE TABLE access_log_filtered_copy SELECT b.* FROM access_log b inner join (select a.ip from access_log a WHERE a.access_date >= '2017-01-01 13:00:00.0' and a.access_date < '2017-01-01 14:00:00.0' GROUP BY a.ip HAVING COUNT(DISTINCT a.id)> 100 ) c on b.ip=c.ip where b.access_date >='2017-01-01 13:00:00.0' and b.access_date < '2017-01-01 14:00:00.0'
